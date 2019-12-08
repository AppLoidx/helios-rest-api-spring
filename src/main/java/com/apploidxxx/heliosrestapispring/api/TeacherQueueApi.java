package com.apploidxxx.heliosrestapispring.api;

import com.apploidxxx.heliosrestapispring.api.model.CurrentAndNextUser;
import com.apploidxxx.heliosrestapispring.api.model.ErrorMessage;
import com.apploidxxx.heliosrestapispring.api.util.ErrorResponseFactory;
import com.apploidxxx.heliosrestapispring.api.util.RepositoryManager;
import com.apploidxxx.heliosrestapispring.entity.queue.CursoredUsersWrapper;
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
import java.util.LinkedHashSet;
import java.util.Set;


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
        User teacher = getUserByAccessToken(teacherAccessToken);
        if (teacher.getUserType() != UserType.TEACHER) return ErrorResponseFactory.getForbiddenErrorResponse(response);


        Queue queue = this.repositoryManager.getQueue().byQueueName(queueName);

        if (queue.getMembersList().size() == 0) {
            return ErrorResponseFactory.getInvalidParamErrorResponse("Queue don't have users yet", response);
        }



        Set<User> currentUser = null;
        if (queue.getCursoredUsers().containsKey(teacher) && queue.getCursoredUsers().get(teacher) != null && !queue.getCursoredUsers().get(teacher).getCursoredUsers().isEmpty()){
            currentUser = queue.getCursoredUsers().get(teacher).getCursoredUsers();
        }
        else {

            final Set<User> cursoredUsers = queue.getSetOfCursoredUsers();

            for (User member : queue.getMembersList()) {
                if (cursoredUsers.contains(member)) continue;

                LinkedHashSet<User> set = new LinkedHashSet<>();
                set.add(member);
                currentUser = set;
                break;
            }
        }

        if (currentUser == null){
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            return new ErrorMessage("No users left", "All users cursored by teachers");
        }

        queue.getCursoredUsers().put(teacher, new CursoredUsersWrapper(currentUser));
        this.repositoryManager.saveQueue(queue);

        CurrentAndNextUser currentAndNextUserModel  = new CurrentAndNextUser(queue.getCursoredUsers().get(teacher).getCursoredUsers());


        // TODO: refactor
        if (queue.getMembers().size() > 1){
            final Set<User> cursoredUsers = queue.getSetOfCursoredUsers();
            for (User member : queue.getMembersList()){
                if (!cursoredUsers.contains(member)){
                    currentAndNextUserModel.setNextUser(new CurrentAndNextUser.User(member));
                    break;
                }
            }
        }

        return currentAndNextUserModel;

    }

    /**
     * Add user for cursored set (when teacher want to accept multithreaded)
     */
    @PostMapping
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Object startNewUserAcceptSession(
            HttpServletResponse response,
            @RequestParam("username") String username,
            @RequestParam("access_token") String teacherAccessToken,
            @RequestParam("queue_name") String queueName
    ){
        User teacher = getUserByAccessToken(teacherAccessToken);
        if (teacher.getUserType() != UserType.TEACHER) return ErrorResponseFactory.getForbiddenErrorResponse(response);


        Queue queue = this.repositoryManager.getQueue().byQueueName(queueName);

        if (queue.getMembersList().size() == 0) {
            return ErrorResponseFactory.getInvalidParamErrorResponse("Queue don't have users yet", response);
        }

        User newUser = this.repositoryManager.getUser().byUsername(username);

        if (!queue.getMembers().contains(newUser))
            return ErrorResponseFactory.getInvalidParamErrorResponse("user not in queue", response);

        Set<User> teacherUsersSet = queue.getCursoredUsers().get(teacher).getCursoredUsers();
        if (teacherUsersSet == null) {
            LinkedHashSet<User> linkedHashSet = new LinkedHashSet<>();
            linkedHashSet.add(newUser);
            queue.getCursoredUsers().put(teacher, new CursoredUsersWrapper(linkedHashSet));
        } else {
            Set<User> oldSet = queue.getCursoredUsers().get(teacher).getCursoredUsers();
            oldSet.add(newUser);
            queue.getCursoredUsers().put(teacher, new CursoredUsersWrapper(oldSet));
        }
        this.repositoryManager.saveQueue(queue);

        return null;

    }

    @ApiOperation("Initialize user passed event")
    @PutMapping
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Object userPassed(
            HttpServletResponse response,
            @RequestParam("passed_user") String passedUsername,
            @RequestParam("access_token") String teacherAccessToken,
            @RequestParam("queue_name") String queueName
    ){

        User teacher = getUserByAccessToken(teacherAccessToken);
        if (teacher.getUserType() != UserType.TEACHER) return ErrorResponseFactory.getForbiddenErrorResponse(response);


        Queue queue = this.repositoryManager.getQueue().byQueueName(queueName);
        User passedUser = this.repositoryManager.getUser().byUsername(passedUsername);

        if (queue.getCursoredUsers() == null || queue.getCursoredUsers().isEmpty()){
            return ErrorResponseFactory.getInvalidParamErrorResponse("queue is empty", response);
        }
        Set<User> teacherStudents = queue.getCursoredUsers().get(teacher).getCursoredUsers();

        if (!teacherStudents.contains(passedUser)) return ErrorResponseFactory.getInvalidParamErrorResponse("User not found", response);

        teacherStudents.remove(passedUser);
        QueueManager.moveUserToEnd(queue, passedUser);

        this.repositoryManager.saveQueue(queue);

        return null;
    }

    private User getUserByAccessToken(String accessToken){
        return this.repositoryManager.getUser().byAccessToken(accessToken);
    }
}
