package com.apploidxxx.heliosrestapispring.api;


import com.apploidxxx.heliosrestapispring.api.util.ErrorResponseFactory;
import com.apploidxxx.heliosrestapispring.entity.access.repository.SessionRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.UserRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.queue.QueueRepository;
import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Arthur Kupriyanov
 */
@RestController
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

    @ApiOperation("Add swap request")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Users swapped"),
            @ApiResponse(code = 201, message = "Successful request, but another user not confirmed it")
    })
    @PostMapping(produces = "application/json")
    public Object requestSwap(
            HttpServletResponse response,
            @RequestParam("access_token") String accessToken,
            @RequestParam("target") String targetUsername,
            @RequestParam("queue_name") String queueName){

        User user = this.sessionRepository.findByAccessToken(accessToken).getUser();
        User targetUser = this.userRepository.findByUsername(targetUsername);
        Queue queue = this.queueRepository.findByName(queueName);

        if (user == null || targetUser == null || queue == null){
            return ErrorResponseFactory.getInvalidParamErrorResponse("entity(-ies) not found by passed params", response);
        }

        if (!queue.getMembers().contains(user) || !queue.getMembers().contains(targetUser)){
            return ErrorResponseFactory.getInvalidParamErrorResponse("user_not_found", "User not found in requested queue", response);
        }

        if (user.equals(targetUser)){
            return ErrorResponseFactory.getInvalidParamErrorResponse("self_request", "You can't request to swap yourself", response);
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
