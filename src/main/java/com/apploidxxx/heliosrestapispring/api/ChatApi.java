package com.apploidxxx.heliosrestapispring.api;


import com.apploidxxx.heliosrestapispring.api.model.ErrorMessage;
import com.apploidxxx.heliosrestapispring.api.util.ErrorResponseFactory;
import com.apploidxxx.heliosrestapispring.entity.Chat;
import com.apploidxxx.heliosrestapispring.entity.Message;
import com.apploidxxx.heliosrestapispring.entity.access.repository.SessionRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.queue.QueueRepository;
import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashSet;
import java.util.Set;


/**
 * @author Arthur Kupriyanov
 */
@RestController
@RequestMapping(value = "/api/chat/{queueName}", produces = "application/json")
public class ChatApi {
    private final QueueRepository queueRepository;
    private final SessionRepository sessionRepository;

    public ChatApi(QueueRepository queueRepository, SessionRepository sessionRepository) {
        this.queueRepository = queueRepository;
        this.sessionRepository = sessionRepository;
    }

    @GetMapping
    public Set<Message> getMessages(
            HttpServletResponse response,
            @PathVariable("queueName") String queueName,
            @RequestParam("last_msg_id") int lastMsgId
    ){
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

    @PutMapping
    public ErrorMessage addMessage(
            HttpServletResponse response,
            @PathVariable("queueName") String queueName,
            @RequestParam("message") String message,
            @RequestParam("access_token") String token
    ){

        User user = this.sessionRepository.findByAccessToken(token).getUser();
        if (user == null) return ErrorResponseFactory.getInvalidTokenErrorResponse(response);

        Queue queue = this.queueRepository.findByName(queueName);
        if (queue  == null) return generateQueueNotFoundErrorResponse(response);

        if ("".equals(message))
            return ErrorResponseFactory.getInvalidParamErrorResponse("Your message is invalid", response);

        if (isUserHaveAccess(queue, user)) return ErrorResponseFactory.getForbiddenErrorResponse(response);


        Chat chat = queue.getChat();

        chat.newMessage(user, message);
        this.queueRepository.save(queue);
        return null;
    }

    private ErrorMessage generateQueueNotFoundErrorResponse(HttpServletResponse response){
        return ErrorResponseFactory
                .getInvalidParamErrorResponse(
                        "queue_not_found", "Queue with this name not found", response);
    }

    private boolean isUserHaveAccess(Queue queue, User user){
        return queue.getSuperUsers().contains(user) || queue.getMembers().contains(user);
    }
}
