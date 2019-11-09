package com.apploidxxx.heliosrestapispring.api;


import com.apploidxxx.heliosrestapispring.api.exception.persistence.PersistenceException;
import com.apploidxxx.heliosrestapispring.api.util.ErrorResponseFactory;
import com.apploidxxx.heliosrestapispring.api.util.RepositoryManager;
import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.apploidxxx.heliosrestapispring.entity.user.UserType;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Arthur Kupriyanov
 */
@RestController
@RequestMapping("/api/swap")
public class SwapApi {

    private final RepositoryManager repositoryManager;

    public SwapApi(RepositoryManager repositoryManager) {

        this.repositoryManager = repositoryManager;
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

        User user = this.repositoryManager.getUser().byAccessToken(accessToken);
        User targetUser = this.repositoryManager.getUser().byUsername(targetUsername);
        Queue queue = this.repositoryManager.getQueue().byQueueName(queueName);

        if (!queue.getMembers().contains(user) || !queue.getMembers().contains(targetUser)){
            return ErrorResponseFactory.getInvalidParamErrorResponse("user_not_found", "User not found in requested queue", response);
        }

        if (user.equals(targetUser)){
            return ErrorResponseFactory.getInvalidParamErrorResponse("self_request", "You can't request to swap yourself", response);
        }
        if (queue.getSwapContainer().hasRequest(user) != null) return ErrorResponseFactory.getInvalidParamErrorResponse("you already requested this user to swap", response);
        boolean isSwapped = queue.getSwapContainer().addSwapRequest(user , targetUser);
        this.repositoryManager.saveQueue(queue);

        // 200 (ok) - users swapped
        // 202 (accepted) - user's request was successful, but not mutually
        if (isSwapped) response.setStatus(HttpServletResponse.SC_OK);
        else response.setStatus(HttpServletResponse.SC_ACCEPTED);

        return null;
    }

    @ApiOperation("Swap users")
    @PutMapping(produces = "application/json")
    public Object swapUsers(
            HttpServletResponse response,
            @RequestParam("access_token") String accessToken,
            @RequestParam("target_one") String targetOneUsername,
            @RequestParam("target_two") String targetTwoUsername,
            @RequestParam("queue_name") String queueName
    ){
        try {
            User user = this.repositoryManager.getUser().byAccessToken(accessToken);
            User targetUserOne = this.repositoryManager.getUser().byUsername(targetOneUsername);
            User targetUserTwo = this.repositoryManager.getUser().byUsername(targetTwoUsername);
            Queue queue = this.repositoryManager.getQueue().byQueueName(queueName);


            // TODO: refactor this shit
            // TODO: Think about error handler (in Spring like response filter)
            if (queue.getSuperUsers().contains(user) || user.getUserType() == UserType.TEACHER || user.getUserType() == UserType.ADMIN){
                if (queue.getMembers().contains(targetUserOne) && queue.getMembers().contains(targetUserTwo)) {
                    queue.swap(targetUserOne, targetUserTwo);
                    return null;
                } else {
                    return ErrorResponseFactory.getInvalidParamErrorResponse("user_not_found", "User(-s) with this name not found in queue", response);
                }
            } else {
                return ErrorResponseFactory.getForbiddenErrorResponse(response);
            }

        } catch (PersistenceException e){
            return ErrorResponseFactory.getInvalidParamErrorResponse("entity(-ies) not found", response);
        }
    }

}
