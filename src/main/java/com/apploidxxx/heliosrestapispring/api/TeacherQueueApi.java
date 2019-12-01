package com.apploidxxx.heliosrestapispring.api;

import com.apploidxxx.heliosrestapispring.api.model.CurrentAndNextUser;
import com.apploidxxx.heliosrestapispring.api.model.ErrorMessage;
import com.apploidxxx.heliosrestapispring.api.util.ErrorResponseFactory;
import com.apploidxxx.heliosrestapispring.api.util.RepositoryManager;
import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.apploidxxx.heliosrestapispring.entity.user.UserType;
import com.apploidxxx.heliosrestapispring.queue.QueueManager;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;


// TODO: write test for teacher queue management [/api/teacher/queue]

/**
 * @author Arthur Kupriyanov
 */
@RestController
@RequestMapping("/api/teacher/queue")
public class TeacherQueueApi {

    private RepositoryManager repositoryManager;

    public TeacherQueueApi(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    @ApiOperation(value = "Initialize next User event which support multithreaded queues")
    @ApiResponses(
            {
                    @ApiResponse(code = 409, message = "All users cursored by teachers.No users left for this request", response = ErrorMessage.class),
                    @ApiResponse(code = 200, message = "Giving current user and next if queue not multithreaded", response = CurrentAndNextUser.class)
            }
    )
    @GetMapping
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Object getNextUser(
            HttpServletResponse response,
            @RequestParam("queue_name") String queueName,
            @RequestParam("access_token") String teacherAccessToken
    ){
        User teacher = this.repositoryManager.getUser().byAccessToken(teacherAccessToken);
        if (teacher.getUserType() != UserType.TEACHER) return ErrorResponseFactory.getForbiddenErrorResponse(response);


        Queue queue = this.repositoryManager.getQueue().byQueueName(queueName);

        if (queue.getMembersList().size() == 0) {
            return ErrorResponseFactory.getInvalidParamErrorResponse("Queue don't have users yet", response);
        }


        User currentUser = null;
        for (User member : queue.getMembersList()){
            if (queue.getCursoredUsers().contains(member)) continue;

            currentUser = member;
            break;
        }

        if (currentUser == null){
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            return new ErrorMessage("No users left", "All users cursored by teachers");
        }

        queue.getCursoredUsers().add(currentUser);
        CurrentAndNextUser currentAndNextUserModel  = new CurrentAndNextUser(currentUser);


        // TODO: refactor
        if (queue.getMembers().size() > 1){
            for (User member : queue.getMembersList()){
                if (!queue.getCursoredUsers().contains(member)){
                    currentAndNextUserModel.setNextUser(new CurrentAndNextUser.User(member));
                    break;
                }
            }
        }
        this.repositoryManager.saveQueue(queue);
        return currentAndNextUserModel;

    }

    @ApiOperation("Initialize user passed event")
    @PutMapping
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Object userPassed(
            HttpServletResponse response,
            @RequestParam("passed_user") String passedUsername,
            @RequestParam("access_token") String accessToken,
            @RequestParam("queue_name") String queueName
    ){

        User teacher = this.repositoryManager.getUser().byAccessToken(accessToken);
        if (teacher.getUserType() != UserType.TEACHER) return ErrorResponseFactory.getForbiddenErrorResponse(response);


        Queue queue = this.repositoryManager.getQueue().byQueueName(queueName);
        User passedUser = this.repositoryManager.getUser().byUsername(passedUsername);

        if (!queue.getCursoredUsers().contains(passedUser)) return ErrorResponseFactory.getInvalidParamErrorResponse("User not found", response);

        queue.getCursoredUsers().remove(passedUser);
        QueueManager.moveUserToEnd(queue, passedUser);

        this.repositoryManager.saveQueue(queue);

        return null;
    }
}
