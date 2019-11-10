package com.apploidxxx.heliosrestapispring.api;


import com.apploidxxx.heliosrestapispring.api.exception.VulnerabilityException;
import com.apploidxxx.heliosrestapispring.api.model.ErrorMessage;
import com.apploidxxx.heliosrestapispring.api.util.ErrorResponseFactory;
import com.apploidxxx.heliosrestapispring.api.util.RepositoryManager;
import com.apploidxxx.heliosrestapispring.api.util.TimelineManager;
import com.apploidxxx.heliosrestapispring.api.util.VulnerabilityChecker;
import com.apploidxxx.heliosrestapispring.entity.queue.Notification;
import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
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

    private final RepositoryManager repositoryManager;

    public QueueApi(RepositoryManager repositoryManager) {

        this.repositoryManager = repositoryManager;
    }

    @ApiOperation("Get queue")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Get queue", response = Queue.class),
            @ApiResponse(code = 404, message = "Queue not found", response = ErrorMessage.class)
    })
    @GetMapping
    public Object getQueue(

            @ApiParam(value = "Queue short name", required = true)
            @RequestParam("queue_name") String queueName
    ) {
        return this.repositoryManager.getQueue().byQueueName(queueName);

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

        User user = this.repositoryManager.getUser().byAccessToken(token);

        Queue queue = this.repositoryManager.getQueue().byQueueName(queueName);
        if (queue.getMembers().contains(user))
            return ErrorResponseFactory.getInvalidParamErrorResponse("repeated_request", "you already in queue", response);


        if (queue.getPassword() == null){
            addNewUserToQueue(queue, user);
            this.repositoryManager.saveQueue(queue);

            return null;
        }

        if (!queue.getPassword().equals(password))
            return getForbiddenErrorResponse(response);

        addNewUserToQueue(queue, user);
        this.repositoryManager.saveQueue(queue);
        this.repositoryManager.saveUser(user);

        return null;
    }

    private void addNewUserToQueue(Queue queue, User user){
        queue.addUser( user);
        addNewUserNotification(queue, user);
    }

    private void addNewUserNotification(Queue queue, User user){
        queue.getNotifications().add(prepareUserJoinedQueueNotification(user));
        addJoinedQueueTimeline(user , queue);
    }

    private void addJoinedQueueTimeline(User user, Queue queue){
        TimelineManager.addQueueJoinedTimeline(user, queue);
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
            @RequestParam("access_token") String accessToken,

            @RequestParam("fullname") String fullname,

            @ApiParam(value = "Provide password if queue is private")
            @RequestParam(value = "password", required = false) String password,

            @ApiParam(value = "ONE_WEEK, TWO_WEEKS, NOT_STATED (not required)")
            @RequestParam(value = "generation", required = false) String generationType
    ){

        User user = this.repositoryManager.getUser().byAccessToken(accessToken);
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
            TimelineManager.addQueueCreatedTimeline(user, q);
            this.repositoryManager.saveQueue(q);
            this.repositoryManager.saveUser(user);
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
    public Object delete(
            HttpServletResponse response,

            @RequestParam("queue_name") String queueName,

            @ApiParam(value = "Provide this param if you want delete user")
            @RequestParam(value = "username", required = false) String userName,

            @ApiParam(value = "USER or QUEUE", required = true)
            @RequestParam("target") String target,

            @RequestParam("access_token") String accessToken
    ) {
        target = target.toUpperCase();
        User user = this.repositoryManager.getUser().byAccessToken(accessToken);

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

        Queue q = this.repositoryManager.getQueue().byQueueName(queueName);
        if (q == null) return prepareQueueNotFoundErrorResponse(response);

        if (username.equals(user.getUsername())) {

            // user deletes self

            if (!q.getMembers().contains(user)) return prepareUserNotFoundErrorResponse(response);
            q.getMembers().remove(user);
            q.getNotifications().add(prepareUserExitQueueNotification(user));
            this.repositoryManager.saveQueue(q);
            return null;
        }

        if (!q.getSuperUsers().contains(user)) return prepareForbiddenErrorResponse(response);

        User delUser = this.repositoryManager.getUser().byUsername(username);
        if (delUser == null || !q.getMembers().contains(delUser)) return prepareUserNotFoundErrorResponse(response);

        q.getMembers().remove(delUser);
        q.getNotifications().add(prepareUserWasDeletedFromQueueNotification(user, delUser));
        this.repositoryManager.saveQueue(q);
        return null;
    }

    @Transactional
    protected Object deleteQueue(String queueName, User user, HttpServletResponse response){
        Queue q = this.repositoryManager.getQueue().byQueueName(queueName);

        if (q == null) return prepareQueueNotFoundErrorResponse(response);
        if (!q.getSuperUsers().contains(user)) return prepareForbiddenErrorResponse(response);
        List<Queue> list = new ArrayList<>();
        list.add(q);
        this.repositoryManager.deleteQueue(list);

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

    private Notification prepareUserWasDeletedFromQueueNotification(User whoDelete, User whoWasDeleted){
        return new Notification(whoDelete, "Пользователь " + whoWasDeleted.getFirstName() + " " + whoWasDeleted.getLastName() + " был удален из очереди");
    }

    private Notification prepareUserJoinedQueueNotification(User user){
        return new Notification(user, String.format("Пользователь %s %s присоеденился к очереди", user.getFirstName(), user.getLastName()));
    }


}
