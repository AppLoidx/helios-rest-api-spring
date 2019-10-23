package com.apploidxxx.heliosrestapispring.api;


import com.apploidxxx.heliosrestapispring.api.model.Check;
import com.apploidxxx.heliosrestapispring.api.util.ErrorResponseFactory;
import com.apploidxxx.heliosrestapispring.entity.ContactDetails;
import com.apploidxxx.heliosrestapispring.entity.access.repository.ContactDetailsRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.UserRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.queue.QueueRepository;
import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

/**
 * @author Arthur Kupriyanov
 */
@Slf4j
@RestController
@RequestMapping(path = "/api/check", produces = "application/json")
public class CheckApi {
    private final UserRepository userRepository;
    private final QueueRepository queueRepository;
    private final ContactDetailsRepository contactDetailsRepository;

    public CheckApi(UserRepository userRepository, QueueRepository queueRepository, ContactDetailsRepository contactDetailsRepository) {
        this.userRepository = userRepository;
        this.queueRepository = queueRepository;
        this.contactDetailsRepository = contactDetailsRepository;
    }

    @GetMapping
    public Object check(
            HttpServletResponse response,

            @ApiParam("(user_exist, queue_exist, email_exist, queue_match, queue_private)")
            @RequestParam("check") String check,

            @ApiParam("required for 'user_exist'")
            @RequestParam(value = "username", required = false) String username,

            @ApiParam("required for 'queue_exist, queue_match, queue_private'")
            @RequestParam(value = "queue_name", required = false) String queueName,

            @ApiParam("required for 'email_exist'")
            @RequestParam(value = "email", required = false) String email
    ) {

        switch (check) {
            case "user_exist":
                return checkUserExist(username, response);
            case "queue_exist":
                return checkQueueExist(queueName, response);
            case "email_exist":
                return checkEmailExist(email);
            case "queue_match":
                return queueMatch(queueName, response);
            case "queue_private":
                return queuePrivate(queueName, response);
            default:
                return ErrorResponseFactory.getInvalidParamErrorResponse("invalid check param", response);
        }

    }

    private Object checkUserExist(String username, HttpServletResponse response) {
        if (username == null)
            return ErrorResponseFactory.getInvalidParamErrorResponse("invalid username param", response);
        return new Check(this.userRepository.findByUsername(username) != null);
    }

    private Object checkQueueExist(String queueName, HttpServletResponse response) {
        if (queueName == null)
            return ErrorResponseFactory.getInvalidParamErrorResponse("invalid queue_name param", response);
        return new Check(this.queueRepository.findByName(queueName) != null);
    }

    // TODO: REFACTOR
    private Object queueMatch(String queueName, HttpServletResponse response) {
        if (queueName == null)
            return ErrorResponseFactory.getInvalidParamErrorResponse("invalid queue_name param", response);

        List<String[]> queueNames = new ArrayList<>();
        for (Queue q : this.queueRepository.findAll()) {
            try {
                if (q.getName().matches("(" + queueName + ").*")
                        || q.getFullname().matches("(" + queueName + ").*")) {
                    queueNames.add(new String[]{q.getName(), q.getFullname()});
                }
            } catch (PatternSyntaxException ignored) {

            }
        }


        return queueNames;
    }

    private Object queuePrivate(String queueName, HttpServletResponse response) {
        if (queueName == null) return ErrorResponseFactory.getInvalidParamErrorResponse("invalid queue_name param", response);

        Queue q = this.queueRepository.findByName(queueName);
        if (q == null) return ErrorResponseFactory.getInvalidParamErrorResponse("queue not found", response);
        return new Check(q.getPassword() != null);
    }

    private Object checkEmailExist(String email) {
        ContactDetails c = this.contactDetailsRepository.findByEmail(email);
        return new Check(c != null);
    }
}
