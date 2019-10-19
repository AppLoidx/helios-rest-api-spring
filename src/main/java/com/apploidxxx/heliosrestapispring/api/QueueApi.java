package com.apploidxxx.heliosrestapispring.api;



import com.apploidxxx.heliosrestapispring.api.exception.VulnerabilityException;
import com.apploidxxx.heliosrestapispring.api.model.ErrorMessage;
import com.apploidxxx.heliosrestapispring.api.util.ErrorResponseFactory;
import com.apploidxxx.heliosrestapispring.api.util.VulnerabilityChecker;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.apploidxxx.heliosrestapispring.entity.access.repository.SessionRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.UserRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.queue.QueueRepository;
import com.apploidxxx.heliosrestapispring.entity.queue.Notification;
import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;


// TODO: Add user filter by group

/**
 * @author Arthur Kupriyanov
 */
@Api("Queue management")
@Slf4j
@RestController
@RequestMapping(value = "/api/queue", produces = "application/json")
public class QueueApi {

    private final UserRepository userRepository;
    private final QueueRepository queueRepository;
    private final SessionRepository sessionRepository;

    public QueueApi(UserRepository userRepository, QueueRepository queueRepository, SessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.queueRepository = queueRepository;
        this.sessionRepository = sessionRepository;
    }

    @ApiOperation("Get queue")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Get queue", response = Queue.class),
            @ApiResponse(code = 404, message = "Queue not found", response = ErrorMessage.class)
    })
    @GetMapping
    public Object getQueue(
            HttpServletResponse response,

            @ApiParam(value = "Queue short name", required = true)
            @RequestParam("queue_name") String queueName
    ) {
        Queue queue = this.queueRepository.findByName(queueName);

        if (queue == null)
            return ErrorResponseFactory.getNotFoundErrorResponse("queue_not_found", "Queue with this name not found", response);

        return queue;
    }

    @ApiOperation("Get queue")
    @ApiResponses({
            @ApiResponse(code = 200, message = "User successfully joined queue"),
            @ApiResponse(code = 400, message = "Invalid params", response = ErrorMessage.class),
            @ApiResponse(code = 404, message = "Queue not found", response = ErrorMessage.class),
            @ApiResponse(code = 403, message = "Invalid or empty password", response = ErrorMessage.class)
    })
    @PutMapping
    public Object joinQueue(
            HttpServletResponse response,

            @ApiParam(value = "Queue short name", required = true)@RequestParam("queue_name") String queueName,
            @RequestParam("access_token") String token,

            @ApiParam(value = "Password of queue. Not required if queue doesn't have password")
            @RequestParam(value = "password", required = false) String password
    ) {

        User user = sessionRepository.findByAccessToken(token).getUser();
        if (user == null)
            return ErrorResponseFactory.getInvalidTokenErrorResponse(response);

        Queue queue = this.queueRepository.findByName(queueName);
        if (queue == null)
            return ErrorResponseFactory.getNotFoundErrorResponse("queue_not_found", "Queue with this name not found", response);

        if (queue.getMembers().contains(user))
            return ErrorResponseFactory.getInvalidParamErrorResponse("repeated_request", "you already in queue", response);


        if (queue.getPassword() == null){
            addNewUserToQueue(queue, user);
            this.queueRepository.save(queue);

            return null;
        }

        if (!queue.getPassword().equals(password))
            return getForbiddenErrorResponse(response);

        addNewUserToQueue(queue, user);
        this.queueRepository.save(queue);

        return null;
    }

    private void addNewUserToQueue(Queue queue, User user){
        queue.addUser( user);
        addNewUserNotification(queue, user);
    }

    private void addNewUserNotification(Queue queue, User user){
        queue.getNotifications()
                .add(new Notification(
                        user,
                        String.format("Пользователь %s %s присоеденился к очереди", user.getFirstName(), user.getLastName())
                ));
    }

    private ErrorMessage getForbiddenErrorResponse(HttpServletResponse response){
        return ErrorResponseFactory.getForbiddenErrorResponse("you don't have access to this queue", response);
    }

    @ApiOperation("Create queue")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Queue created"),
            @ApiResponse(code = 400, message = "Invalid params", response = ErrorMessage.class)
    })
    @PostMapping
    public Object createQueue(
            HttpServletResponse response,

            @ApiParam(value = "Queue short name", required = true)@RequestParam("queue_name") String queueName,
            @RequestParam("access_token") String token,

            @ApiParam(value = "Fullname of queue", required = true)
            @RequestParam("fullname") String fullname,

            @ApiParam(value = "Provide password if queue is private")
            @RequestParam(value = "password", required = false) String password,

            @ApiParam(value = "ONE_WEEK, TWO_WEEKS, NOT_STATED (not required)")
            @RequestParam(value = "generation", required = false) String generationType
    ){

        User user = this.sessionRepository.findByAccessToken(token).getUser();
        if (user == null) return ErrorResponseFactory.getInvalidTokenErrorResponse(response);

        try {
            VulnerabilityChecker.checkWord(queueName, fullname);
        } catch (VulnerabilityException e) {
            return e.getResponse(response);
        }

        Queue q = new Queue(queueName, fullname);
        q.addSuperUser(user);
        if (password != null && !"".equals(password)) q.setPassword(password);
        if (generationType != null && !"".equals(generationType)) q.setGenerationType(generationType);

        try {
            q.getNotifications().add(new Notification(null, "Создана очередь"));
            this.queueRepository.save(q);
            return null;
        } catch (Exception e){
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return new ErrorMessage("internal_server_error", e.getMessage());
        }
    }

    @ApiOperation("Delete queue or user from queue")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Queue deleted"),
            @ApiResponse(code = 400, message = "Invalid params", response = ErrorMessage.class),
            @ApiResponse(code = 401, message = "Not enough permissions", response = ErrorMessage.class),
            @ApiResponse(code = 400, message = "Queue or user not found", response = ErrorMessage.class)
    })
    @DeleteMapping(produces = "application/json")
    @Transactional
    public @ResponseBody Object delete(
            HttpServletResponse response,
            @ApiParam(value = "Queue short name", required = true)
            @RequestParam("queue_name") String queueName,

            @ApiParam(value = "Provide this param if you want delete user")
            @RequestParam(value = "username", required = false) String userName,

            @ApiParam(value = "USER or QUEUE", required = true)
            @RequestParam("target") String target,

            @RequestParam("access_token") String token
    ) {
        target = target.toUpperCase();
        User user = this.sessionRepository.findByAccessToken(token).getUser();
        if (user == null) return ErrorResponseFactory.getInvalidTokenErrorResponse(response);

        if (!target.matches("(USER)|(QUEUE)"))
            return ErrorResponseFactory.getInvalidParamErrorResponse("invalid_target", "Unknown target value", response);


        if (isDeletingAnotherUser(userName, target)) {
            return deleteUser(userName, queueName, user, response);
        }

        if (isDeleteSelf(userName, target)) {
            return deleteUser(user.getUsername(), queueName, user, response);
        }

        if (target.equals("QUEUE")){
            return deleteQueue(queueName, user, response);
        }

        return ErrorResponseFactory.getInvalidParamErrorResponse("target not found", response);

    }

    private boolean isDeletingAnotherUser(String userName, String target){
        return userName != null && target.equals("USER");
    }

    private boolean isDeleteSelf(String userName, String target){
        return userName == null && target.equals("USER");
    }

    // TODO: control point of refactoring

    private Object deleteUser(String username, String queueName, User user, HttpServletResponse response) {
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        Queue q = this.queueRepository.findByName(queueName);

        if (q == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return new ErrorMessage("queue_not_found", "queue with this params not found");
        }
        if (username.equals(user.getUsername())) {
            if (!q.getMembers().contains(user)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return new ErrorMessage("user_not_found", "User with name " + username + " not found");
            }
            q.getMembers().remove(user);
            q.getNotifications().add(new Notification(null, "Пользователь " + user.getFirstName() + " " + user.getLastName() + " вышел из очереди"));
            queueRepository.save(q);
            return null;
        }
        if (q.getSuperUsers().contains(user)) {
            User delUser = this.userRepository.findByUsername(username);
            if (delUser == null || !q.getMembers().contains(delUser)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return new ErrorMessage("user_not_found", "User with name " + username + " not found");
            }
            q.getMembers().remove(delUser);
            q.getNotifications().add(new Notification(user, "Пользователь " + delUser.getFirstName() + " " + delUser.getLastName() + " был удален из очереди"));
            this.queueRepository.save(q);
            response.setStatus(200);
            return null;
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return new ErrorMessage("forbidden", "you don't have permissions to delete this queue");

        }
    }

    @Transactional
    protected Object deleteQueue(String queueName, User user, HttpServletResponse response){
        Queue q = this.queueRepository.findByName(queueName);
        log.info("Deleting queue");
        if (q!=null){
            if (q.getSuperUsers().contains(user)){
                this.queueRepository.deleteById(q.getName());
                List<Queue> queueList = new ArrayList<>();
                queueList.add(q);
                this.queueRepository.deleteInBatch(queueList);
//                queueEMRepository.deleteQueue(q);
                log.info("Queue deleted");
                response.setStatus(HttpServletResponse.SC_OK);
                return null;
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return null;
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return new ErrorMessage("queue_not_found", "Queue with name " + queueName + " not found");

        }
    }
}
