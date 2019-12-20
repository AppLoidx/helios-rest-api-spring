package com.apploidxxx.heliosrestapispring.api;

import com.apploidxxx.heliosrestapispring.api.model.QueueShortInfo;
import com.apploidxxx.heliosrestapispring.api.util.ErrorResponseFactory;
import com.apploidxxx.heliosrestapispring.api.util.RepositoryManager;
import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Arthur Kupriyanov
 */
@Api("Favorite queues")
@RestController
@RequestMapping("/api/favorite")
public class FavoriteApi {

    private final RepositoryManager repositoryManager;

    public FavoriteApi(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    @ApiOperation(value = "Get favorite queues", response = List.class)
    @GetMapping
    public List<QueueShortInfo> getFavorites(
            @RequestParam("access_token") String accessToken
    ) {
        return
                this.repositoryManager
                        .getUser().byAccessToken(accessToken)
                        .getFavorites()
                        .stream()
                        .map(QueueShortInfo::new)
                        .collect(Collectors.toList());
    }

    @PutMapping
    public void addToFavorites(
            @RequestParam("access_token") String accessToken,
            @RequestParam("queue_name") String queueName
    ){
        User user = this.repositoryManager.getUser().byAccessToken(accessToken);
        Queue queue = this.repositoryManager.getQueue().byQueueName(queueName);
        if (user.getFavorites().contains(queue)){
            return;
        }
        user.getFavorites().add(queue);
        this.repositoryManager.saveUser(user);
    }

    @DeleteMapping
    public Object deleteFromFavorites(
            HttpServletResponse response,
            @RequestParam("access_token") String accessToken,
            @RequestParam("queue_name") String queueName
    ){
        User user = this.repositoryManager.getUser().byAccessToken(accessToken);

        if (!user.getFavorites().remove(this.repositoryManager.getQueue().byQueueName(queueName))){
            return ErrorResponseFactory
                            .getInvalidParamErrorResponse(
                                    "This queue didn't contained in favorites", response);
        } else {
            this.repositoryManager.saveUser(user);
            return null;
        }
    }

}
