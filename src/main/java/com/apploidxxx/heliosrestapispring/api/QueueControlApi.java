package com.apploidxxx.heliosrestapispring.api;

import com.apploidxxx.heliosrestapispring.api.util.ErrorResponseFactory;
import com.apploidxxx.heliosrestapispring.api.util.RepositoryManager;
import com.apploidxxx.heliosrestapispring.entity.queue.Notification;
import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.apploidxxx.heliosrestapispring.entity.user.UserType;
import com.apploidxxx.heliosrestapispring.queue.QueueManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;


/**
 * @author Arthur Kupriyanov
 */
@Api("Controls of queue")
@RestController
@RequestMapping("/api/queue/{queueId}")
public class QueueControlApi {


    private final RepositoryManager repositoryManager;

    public QueueControlApi(RepositoryManager repositoryManager) {

        this.repositoryManager = repositoryManager;
    }

    @ApiOperation("Init a user passed event")
    @GetMapping
    public Object nextUser(
            HttpServletResponse response,
            @PathVariable("queueId") String queueId,
            @RequestParam("access_token") String accessToken,

            @RequestParam(value = "passed_user", required = false) String username
    ){

        User user = this.repositoryManager.getUser().byAccessToken(accessToken);

        Queue queue = this.repositoryManager.getQueue().byQueueName(queueId);

        if (queue.getMembers().isEmpty()) return ErrorResponseFactory.getInvalidParamErrorResponse("queue is empty", response);

        if (!queue.getMembers().get(0).equals(user) && !isCanManageQueue(user, queue))
            return ErrorResponseFactory.getForbiddenErrorResponse( "you are not in cursor to move", response);

        Queue newQueue = QueueManager.nextUser(queue, user);
        this.repositoryManager.saveQueue(newQueue);
        return newQueue;
    }

    private boolean isCanManageQueue(User user, Queue queue){
        return queue.getSuperUsers().contains(user) || user.getUserType() == UserType.ADMIN || user.getUserType() == UserType.TEACHER;
    }

    @PutMapping
    public Object action(
            HttpServletResponse response,

            @PathVariable("queueId") String queueId,

            @ApiParam(value = "Action type: (shuffle, settype, setadmin, clearnotifications)", required = true)
            @RequestParam("action") String action,

            @RequestParam("access_token") String accessToken,

            @ApiParam(value = "Required for 'settype' action")
            @RequestParam(value = "type", required = false) String newType,

            @ApiParam(value = "Required for 'setadmin' action")
            @RequestParam(value = "admin", required = false) String newAdmin) {


        User user = this.repositoryManager.getUser().byAccessToken(accessToken);
        Queue queue = this.repositoryManager.getQueue().byQueueName(queueId);


        if (!queue.getSuperUsers().contains(user))
            return ErrorResponseFactory.getForbiddenErrorResponse("enough permissions to manage this queue", response);


        action = action.toLowerCase();

        // TODO: rewrite with CommandChain

        switch (action) {
            case "shuffle":
                return shuffle(queue, user);
            case "settype":
                return setType(newType, queue, user, response);
            case "setadmin":
                return setAdmin(newAdmin, queue, response);
            case "clearnotifications":
                return clearNotifications(queue, user, response);
            case "setstarted":
                return setStarted(queue);
            default:
                return ErrorResponseFactory.getInvalidParamErrorResponse("Invalid action param. Please, check allowed actions", response);
        }
    }

    private Object shuffle(Queue queue, User byUser) {

        queue.shuffle();
        queue.getNotifications().add(new Notification(byUser, "Очередь перемешана"));
        this.repositoryManager.saveQueue(queue);

        return queue;
    }

    private Object setType(String newType, Queue queue, User byUser, HttpServletResponse response) {
        if (newType == null)
            return ErrorResponseFactory.getInvalidParamErrorResponse("You should add a type param", response);

        if (queue.setGenerationType(newType)) {
            queue.getNotifications().add(new Notification(byUser, "Изменен тип генерации очереди на " + newType));
            this.repositoryManager.saveQueue(queue);

            return null;
        } else {
            return ErrorResponseFactory.getInvalidParamErrorResponse("Unknown type: " + newType + ". Please, check your request", response);
        }
    }

    private Object setAdmin(String newAdmin, Queue queue, HttpServletResponse response) {
        if (newAdmin == null)
            return ErrorResponseFactory.getInvalidParamErrorResponse("You should add a type param", response);

        User newAdminUser = this.repositoryManager.getUser().byUsername(newAdmin);

        if (newAdminUser == null)
            return ErrorResponseFactory.getInvalidParamErrorResponse("user not found", response);

        queue.addSuperUser(newAdminUser);
        this.repositoryManager.saveQueue(queue);

        return null;
    }

    private Object clearNotifications(Queue queue, User byUser, HttpServletResponse response){
        if ( queue.getSuperUsers().contains(byUser) || byUser.getUserType() == UserType.ADMIN){
            queue.getNotifications().clear();
            queue.getNotifications().add(new Notification(null, "История очищена"));
            this.repositoryManager.saveQueue(queue);
            return null;
        } else {
            return ErrorResponseFactory.getForbiddenErrorResponse(response);
        }
    }

    private Object setStarted(Queue queue) {

        queue.setStarted(!queue.isStarted());
        this.repositoryManager.saveQueue(queue);

        return queue;
    }


}
