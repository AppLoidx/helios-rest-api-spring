package com.apploidxxx.heliosrestapispring.api;

import com.apploidxxx.heliosrestapispring.api.model.ErrorMessage;
import com.apploidxxx.heliosrestapispring.api.util.ErrorResponseFactory;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.apploidxxx.heliosrestapispring.entity.access.repository.SessionRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.UserRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.queue.QueueRepository;
import com.apploidxxx.heliosrestapispring.entity.queue.Notification;
import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
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

    // TODO: rename queueId to queue_name !WARNING see web-application
    @PutMapping
    public @ResponseBody Object action(
            HttpServletResponse response,
            @ApiParam(value = "Provide this param if you want delete user", required = true)@PathVariable("queueId") String queueId,
            @ApiParam(value = "Action type: (shuffle, settype, setadmin)", required = true)@RequestParam("action") String action,
            @RequestParam("access_token") String accessToken,
            @ApiParam(value = "Required for 'settype' action")@RequestParam(value = "type", required = false) String newType,
            @ApiParam(value = "Required for 'setadmin' action")@RequestParam(value = "admin", required = false) String newAdmin){


        User user;
        if ((user = this.sessionRepository.findByAccessToken(accessToken).getUser()) == null ){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            // TODO: template for invalid_token
            return new ErrorMessage("invalid_token", "your token expired");
        }

        Queue queue;
        if ((queue = this.queueRepository.findByName(queueId)) == null){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return new ErrorMessage("queue_not_found", "Queue with name " + queueId + " not found");
        }

        if (!queue.getSuperUsers().contains(user)){
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        action = action.toLowerCase();
        switch (action){
            case "shuffle": return shuffle(queue, user, response);
            case "settype": return setType(newType, queue, user, response);
            case "setadmin": return setAdmin(newAdmin, queue, response);
            default:
                return ErrorResponseFactory.getInvalidParamErrorResponse("Invalid action param. Please, check allowed actions", response);
        }
    }

    private Object shuffle(Queue queue, User byUser, HttpServletResponse response){
        try {
            queue.shuffle();
            queue.getNotifications().add(new Notification(byUser, "Очередь перемешана"));
            this.queueRepository.save(queue);
        } catch (Exception e){
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return new ErrorMessage("internal_server_error", "Error during shuffle queue");
        }
        response.setStatus(HttpServletResponse.SC_OK);
        return queue;
    }

    private Object setType(String newType, Queue queue, User byUser, HttpServletResponse response){
        if (newType == null) return ErrorResponseFactory.getInvalidParamErrorResponse("You should add a type param", response);

        if (queue.setGenerationType(newType)){
            queue.getNotifications().add(new Notification(byUser, "Изменен тип генерации очереди на " + newType));
            this.queueRepository.save(queue);
            response.setStatus(HttpServletResponse.SC_OK);
            return null;
        } else {
            return ErrorResponseFactory.getInvalidParamErrorResponse("Unknown type: " + newType + ". Please, check your request", response);
        }
    }

    private Object setAdmin(String newAdmin, Queue queue, HttpServletResponse response){
        if (newAdmin == null) return ErrorResponseFactory.getInvalidParamErrorResponse("You should add a type param", response);

        User newAdminUser;

        if ((newAdminUser = this.userRepository.findByUsername(newAdmin)) == null ){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return new ErrorMessage("user_not_found", "requested user not found");
        }

        queue.addSuperUser(newAdminUser);
        this.queueRepository.save(queue);
        response.setStatus(HttpServletResponse.SC_OK);
        return null;
    }


}
