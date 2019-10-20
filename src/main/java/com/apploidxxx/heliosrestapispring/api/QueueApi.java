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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;


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

            @RequestParam("queue_name") String queueName,
            @RequestParam("access_token") String token,

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

    private Object deleteUser(String username, String queueName, User user, HttpServletResponse response) {

        Queue q = this.queueRepository.findByName(queueName);
        if (q == null) return prepareQueueNotFoundErrorResponse(response);

        if (username.equals(user.getUsername())) {

            // user deletes self

            if (!q.getMembers().contains(user)) return prepareUserNotFoundErrorResponse(response);
            q.getMembers().remove(user);
            q.getNotifications().add(prepareUserExitQueueNotification(user));
            this.queueRepository.save(q);
            return null;
        }

        if (!q.getSuperUsers().contains(user)) return prepareForbiddenErrorResponse(response);

        User delUser = this.userRepository.findByUsername(username);
        if (delUser == null || !q.getMembers().contains(delUser)) return prepareUserNotFoundErrorResponse(response);

        q.getMembers().remove(delUser);
        q.getNotifications().add(prepareUserWasDeletedFromQueue(user, delUser));
        this.queueRepository.save(q);
        return null;
    }

    @Transactional
    protected Object deleteQueue(String queueName, User user, HttpServletResponse response){
        Queue q = this.queueRepository.findByName(queueName);

        if (q == null) return prepareQueueNotFoundErrorResponse(response);
        if (!q.getSuperUsers().contains(user)) return prepareForbiddenErrorResponse(response);
        this.queueRepository.deleteById(q.getName());
        return null;
    }

    private ErrorMessage prepareQueueNotFoundErrorResponse(HttpServletResponse response){
        return ErrorResponseFactory.getInvalidParamErrorResponse("queue_not_found", "queue with this params not found", response);
    }

    private ErrorMessage prepareUserNotFoundErrorResponse(HttpServletResponse response){
        return ErrorResponseFactory.getInvalidParamErrorResponse("user_not_found", "User not found", response);
    }

    private ErrorMessage prepareForbiddenErrorResponse(HttpServletResponse response){
        return ErrorResponseFactory.getForbiddenErrorResponse("you don't have enough permissions to manage queue", response);
    }

    private Notification prepareUserExitQueueNotification(User user){
        return new Notification(null, String.format("Пользователь %s %s вышел из очереди",user.getFirstName(), user.getLastName()));
    }

    private Notification prepareUserWasDeletedFromQueue(User whoDelete, User whoWasDeleted){
        return new Notification(whoDelete, "Пользователь " + whoWasDeleted.getFirstName() + " " + whoWasDeleted.getLastName() + " был удален из очереди");
    }
}
