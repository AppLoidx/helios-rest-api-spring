package com.apploidxxx.heliosrestapispring.api;

import com.apploidxxx.heliosrestapispring.api.util.ErrorResponseFactory;
import com.apploidxxx.heliosrestapispring.entity.access.repository.SessionRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.UserRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.queue.QueueRepository;
import com.apploidxxx.heliosrestapispring.entity.queue.Notification;
import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

//TODO rewrite with template responses factory

/**
 * @author Arthur Kupriyanov
 */
@Api("Controls of queue")
@Controller
@RequestMapping("/api/queue/{queueId}")
public class QueueControlApi {

    private final QueueRepository queueRepository;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;

    public QueueControlApi(QueueRepository queueRepository, UserRepository userRepository, SessionRepository sessionRepository) {
        this.queueRepository = queueRepository;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
    }

    @PutMapping
    public @ResponseBody
    Object action(
            HttpServletResponse response,

            @PathVariable("queueId") String queueId,

            @ApiParam(value = "Action type: (shuffle, settype, setadmin)", required = true)
            @RequestParam("action") String action,

            @RequestParam("access_token") String accessToken,

            @ApiParam(value = "Required for 'settype' action")
            @RequestParam(value = "type", required = false) String newType,

            @ApiParam(value = "Required for 'setadmin' action")
            @RequestParam(value = "admin", required = false) String newAdmin) {


        User user = this.sessionRepository.findByAccessToken(accessToken).getUser();
        if (user == null) return ErrorResponseFactory.getInvalidTokenErrorResponse(response);

        Queue queue = this.queueRepository.findByName(queueId);
        if (queue == null) return ErrorResponseFactory.getInvalidParamErrorResponse("queue not found", response);


        if (!queue.getSuperUsers().contains(user))
            return ErrorResponseFactory.getForbiddenErrorResponse("enough permissions to manage this queue", response);


        action = action.toLowerCase();
        switch (action) {
            case "shuffle":
                return shuffle(queue, user);
            case "settype":
                return setType(newType, queue, user, response);
            case "setadmin":
                return setAdmin(newAdmin, queue, response);
            default:
                return ErrorResponseFactory.getInvalidParamErrorResponse("Invalid action param. Please, check allowed actions", response);
        }
    }

    private Object shuffle(Queue queue, User byUser) {

        queue.shuffle();
        queue.getNotifications().add(new Notification(byUser, "Очередь перемешана"));
        this.queueRepository.save(queue);

        return queue;
    }

    private Object setType(String newType, Queue queue, User byUser, HttpServletResponse response) {
        if (newType == null)
            return ErrorResponseFactory.getInvalidParamErrorResponse("You should add a type param", response);

        if (queue.setGenerationType(newType)) {
            queue.getNotifications().add(new Notification(byUser, "Изменен тип генерации очереди на " + newType));
            this.queueRepository.save(queue);

            return null;
        } else {
            return ErrorResponseFactory.getInvalidParamErrorResponse("Unknown type: " + newType + ". Please, check your request", response);
        }
    }

    private Object setAdmin(String newAdmin, Queue queue, HttpServletResponse response) {
        if (newAdmin == null)
            return ErrorResponseFactory.getInvalidParamErrorResponse("You should add a type param", response);

        User newAdminUser = this.userRepository.findByUsername(newAdmin);

        if (newAdminUser == null)
            return ErrorResponseFactory.getInvalidParamErrorResponse("user not found", response);

        queue.addSuperUser(newAdminUser);
        this.queueRepository.save(queue);

        return null;
    }


}
