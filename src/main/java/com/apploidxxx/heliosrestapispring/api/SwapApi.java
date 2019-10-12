package com.apploidxxx.heliosrestapispring.api;


import com.apploidxxx.heliosrestapispring.api.model.ErrorMessage;
import com.apploidxxx.heliosrestapispring.api.util.ErrorResponseFactory;
import com.apploidxxx.heliosrestapispring.entity.User;
import com.apploidxxx.heliosrestapispring.entity.access.repository.SessionRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.UserRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.queue.QueueRepository;
import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Arthur Kupriyanov
 */
@Controller
@RequestMapping("/api/swap")
public class SwapApi {
    private final UserRepository userRepository;
    private final QueueRepository queueRepository;
    private final SessionRepository sessionRepository;

    public SwapApi(UserRepository userRepository, QueueRepository queueRepository, SessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.queueRepository = queueRepository;
        this.sessionRepository = sessionRepository;
    }

    // TODO: Add priority selection !!!

    @PostMapping(produces = "application/json")
    public @ResponseBody Object requestSwap(
            HttpServletResponse response,
            @RequestParam("access_token") String accessToken,
            @RequestParam("target") String targetUsername,
            @RequestParam("queue_name") String queueName){

        User user;
        User targetUser;
        Queue queue;
        if ((user = this.sessionRepository.findByAccessToken(accessToken).getUser()) == null ||
                (targetUser = this.userRepository.findByUsername(targetUsername)) == null ||
                (queue = this.queueRepository.findByName(queueName)) == null){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return ErrorResponseFactory.getInvalidParamErrorResponse("entity(-ies) not found by passed params", response);
        }


        if (!queue.getMembers().contains(user) || !queue.getMembers().contains(targetUser)){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return new ErrorMessage("user_not_found", "User not found in requested queue");
        }

        if (user.equals(targetUser)){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return new ErrorMessage("self_request", "You can't request to swap yourself");
        }

        boolean isSwapped = queue.getSwapContainer().addSwapRequest(user , targetUser);
        this.queueRepository.save(queue);

        // 200 (ok) - users swapped
        // 202 (accepted) - user's request was successful, but not mutually
        if (isSwapped) response.setStatus(HttpServletResponse.SC_OK);
        else response.setStatus(HttpServletResponse.SC_ACCEPTED);

        return null;
    }
}
