package com.apploidxxx.heliosrestapispring.api;

import com.apploidxxx.heliosrestapispring.api.util.ErrorResponseFactory;
import com.apploidxxx.heliosrestapispring.api.util.RepositoryManager;
import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.apploidxxx.heliosrestapispring.entity.user.UserType;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.LinkedList;

/**
 * @author Arthur Kupriyanov
 */
@RestController
@RequestMapping("/api/insert")
public class InsertApi {
    private final RepositoryManager repositoryManager;

    public InsertApi(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    @PutMapping
    public Object insertInto(
            HttpServletResponse response,
            @RequestParam("access_token") String accessToken,
            @RequestParam("old_index") int oldIndex,
            @RequestParam("new_index") int newIndex,
            @RequestParam("queue_name") String queueName
    ){
        User teacher = this.repositoryManager.getUser().byAccessToken(accessToken);

        if (teacher.getUserType() != UserType.TEACHER){
            return ErrorResponseFactory.getForbiddenErrorResponse(response);
        }

        Queue queue = this.repositoryManager.getQueue().byQueueName(queueName);

        int queueSize = queue.getMembers().size();

        if (oldIndex > queueSize - 1 || newIndex > queueSize - 1 || oldIndex < 0 || newIndex < 0){
            return ErrorResponseFactory.getInvalidParamErrorResponse("index out of bounds", response);
        }

        LinkedList<User> users = new LinkedList<>(queue.getMembers());
        users.add(newIndex, users.remove(oldIndex));
        queue.setMembers(users);

        if (oldIndex < 2 || newIndex < 2){
            queue.getCursoredUsers().clear();   // TODO: re-think
        }
        this.repositoryManager.saveQueue(queue);
        return null;
    }

}
