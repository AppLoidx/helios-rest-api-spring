package com.apploidxxx.heliosrestapispring.api;


import com.apploidxxx.heliosrestapispring.api.model.ErrorMessage;
import com.apploidxxx.heliosrestapispring.api.util.ErrorResponseFactory;
import com.apploidxxx.heliosrestapispring.entity.Chat;
import com.apploidxxx.heliosrestapispring.entity.Message;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.apploidxxx.heliosrestapispring.entity.access.repository.SessionRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.queue.QueueRepository;
import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Arthur Kupriyanov
 */
@Controller
@RequestMapping(value = "/api/chat", produces = "application/json")
public class ChatApi {
    private final QueueRepository queueRepository;
    private final SessionRepository sessionRepository;

    public ChatApi(QueueRepository queueRepository, SessionRepository sessionRepository) {
        this.queueRepository = queueRepository;
        this.sessionRepository = sessionRepository;
    }

    @GetMapping("/{queueName}")
    public Set<Message> getMessages(
            HttpServletResponse response,
            @PathVariable("queueName") String queueName,
            @RequestParam("lastMsgId") int lastMsgId){
        Queue queue = this.queueRepository.findByName(queueName);
        if (queue==null){
            return null;
        }

        Set<Message> messages = queue.getChat().getMessages();
        Set<Message> messageLinkedHashSet = new LinkedHashSet<>();
        for (Message m : messages){
            if (m.getId() > lastMsgId){
                messageLinkedHashSet.add(m);
            }
        }
        response.setStatus(HttpServletResponse.SC_OK);
        return messageLinkedHashSet;
    }
    @PutMapping("/{queueName}")
    public ErrorMessage addMessage(
            HttpServletResponse response,
            @PathVariable("queueName") String queueName,
            @RequestParam("message") String message,
            @RequestParam("access_token") String token){

        User user;
        Queue q;

        if ( (user = this.sessionRepository.findByAccessToken(token).getUser()) == null){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return new ErrorMessage("invalid_token", "Your token is invalid or expired. Please, take new token or refresh existing");
        }
        if ((q = this.queueRepository.findByName(queueName)) == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return new ErrorMessage("queue_not_found", "Queue with this name not found");
        }

        if (!q.getSuperUsers().contains(user) && !q.getMembers().contains(user)){
            return ErrorResponseFactory.getForbiddenErrorResponse(response);
        }

        Chat chat = q.getChat();
        if ("".equals(message))
            return ErrorResponseFactory.getInvalidParamErrorResponse("Your message is invalid", response);

        chat.newMessage(user, message);
        this.queueRepository.save(q);
        response.setStatus(HttpServletResponse.SC_OK);
        return null;

    }
}
