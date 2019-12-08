package com.apploidxxx.heliosrestapispring.api;

import com.apploidxxx.heliosrestapispring.api.util.ErrorResponseFactory;
import com.apploidxxx.heliosrestapispring.api.util.RepositoryManager;
import com.apploidxxx.heliosrestapispring.api.util.VulnerabilityChecker;
import com.apploidxxx.heliosrestapispring.entity.access.repository.commentaries.CommentaryRepository;
import com.apploidxxx.heliosrestapispring.entity.commentary.Commentary;
import com.apploidxxx.heliosrestapispring.entity.commentary.CommentaryType;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.apploidxxx.heliosrestapispring.entity.user.UserType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Arthur Kupriyanov
 */
@RestController
@RequestMapping("/api/commentaries")
public class CommentariesApi {

    private final RepositoryManager repositoryManager;
    private final CommentaryRepository commentaryRepository;

    public CommentariesApi(RepositoryManager repositoryManager, CommentaryRepository commentaryRepository) {
        this.repositoryManager = repositoryManager;
        this.commentaryRepository = commentaryRepository;
    }

    @GetMapping
    public Object getCommentaries(
            HttpServletResponse response,
            @RequestParam("username") String username,
            @RequestParam("access_token") String accessToken
    ){
        User requestedUser = this.repositoryManager.getUser().byAccessToken(accessToken);
        User target = this.repositoryManager.getUser().byUsername(username);

        if (accessedToWatchCommentaries(requestedUser, target) || isTeacher(requestedUser)){
            if (!isTeacher(requestedUser)) return getOnlyPublicCommentaries(target.getCommentaries());

            return target.getCommentaries();

        } else
            return ErrorResponseFactory.getForbiddenErrorResponse(response);

    }

    @PutMapping
    public Object putComment(
            HttpServletResponse response,
            @RequestParam("text") String text,
            @RequestParam("username") String username,
            @RequestParam("access_token") String accessToken,
            @RequestParam(value = "type", defaultValue = "private") String type
    ){
        User requestedUser = this.repositoryManager.getUser().byAccessToken(accessToken);
        User target = this.repositoryManager.getUser().byUsername(username);

        if (!isTeacher(requestedUser)) return ErrorResponseFactory.getForbiddenErrorResponse(response);

        VulnerabilityChecker.checkWord(text);

        if ("".equals(text)) return ErrorResponseFactory.getInvalidParamErrorResponse("text is empty", response);

        Commentary commentary = new Commentary(target, requestedUser, text, CommentaryType.getType(type));

        commentaryRepository.save(commentary);

        return null;
    }

    @DeleteMapping
    public Object deleteComment(
            HttpServletResponse response,
            @RequestParam("access_token") String accessToken,
            @RequestParam("commentary_id") Long commentaryId
    ){
        User requestedUser = this.repositoryManager.getUser().byAccessToken(accessToken);

        Optional<Commentary> commentaryOpt = this.commentaryRepository.findById(commentaryId);
        if (!commentaryOpt.isPresent()) return ErrorResponseFactory.getInvalidParamErrorResponse("commentary mot found", response);

        Commentary commentary = commentaryOpt.get();

        if (!isTeacher(requestedUser)){
            return ErrorResponseFactory.getForbiddenErrorResponse("You don't have access to private comments", response);
        }

        this.commentaryRepository.delete(commentary);

        return null;
    }

    private boolean accessedToWatchCommentaries(User observer, User target){
        return observer.equals(target) || isTeacher(observer);
    }

    private boolean isTeacher(User observer){
        return observer.getUserType() == UserType.TEACHER;
    }

    private List<Commentary> getOnlyPublicCommentaries(List<Commentary> commentaryList){
        return commentaryList.stream().filter(commentary -> commentary.getCommentaryType() == CommentaryType.PUBLIC).collect(Collectors.toList());
    }



}
