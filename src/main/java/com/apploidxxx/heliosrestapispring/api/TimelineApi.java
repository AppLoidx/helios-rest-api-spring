package com.apploidxxx.heliosrestapispring.api;

import com.apploidxxx.heliosrestapispring.api.util.ErrorResponseFactory;
import com.apploidxxx.heliosrestapispring.entity.Session;
import com.apploidxxx.heliosrestapispring.entity.access.repository.SessionRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.UserRepository;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.apploidxxx.heliosrestapispring.entity.user.timeline.Timeline;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Arthur Kupriyanov
 */
@RestController
@RequestMapping(value = "/api/timeline", produces = "application/json")
public class TimelineApi {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public TimelineApi(SessionRepository sessionRepository, UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @ApiOperation(value = "Get user's timeline", response = Timeline.class)
    @GetMapping
    public Object getUserTimeline(
            HttpServletResponse response,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam("access_token") String accessToken
    ){
        Session session = this.sessionRepository.findByAccessToken(accessToken);
        if (session == null) return ErrorResponseFactory.getInvalidTokenErrorResponse(response);
        User user;
        if (username == null) user = session.getUser();
        else user = this.userRepository.findByUsername(username);

        if (user == null) return ErrorResponseFactory.getInvalidParamErrorResponse("user not found", response);

        return user.getTimelines();
    }
}
