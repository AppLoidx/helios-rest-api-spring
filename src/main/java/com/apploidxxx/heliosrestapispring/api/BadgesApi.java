package com.apploidxxx.heliosrestapispring.api;

import com.apploidxxx.heliosrestapispring.api.util.BadgesFactory;
import com.apploidxxx.heliosrestapispring.api.util.ErrorResponseFactory;
import com.apploidxxx.heliosrestapispring.entity.Session;
import com.apploidxxx.heliosrestapispring.entity.access.repository.SessionRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.UserRepository;
import com.apploidxxx.heliosrestapispring.entity.user.Badge;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.apploidxxx.heliosrestapispring.entity.user.UserType;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Set;

/**
 * @author Arthur Kupriyanov
 */
@Api("Badges management")
@RequestMapping("/api/badges")
@RestController
public class BadgesApi {
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public BadgesApi(SessionRepository sessionRepository, UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @ApiOperation(value = "Get user badges", response = Set.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 400, message = "Invalid param")
    })
    @GetMapping(produces = "application/json")
    public Object getBadges(
            HttpServletResponse response,

            @ApiParam(value = "user's access token", required = true)
            @RequestParam("access_token") String accessToken,

            @ApiParam(value = "username of badges owner", required = true)
            @RequestParam("username") String username
    ){

        // !WARNING: if you want refactor these two lines be careful. We need to verify user with him access_token
        Session session = sessionRepository.findByAccessToken(accessToken);
        if (session == null) return ErrorResponseFactory.getInvalidParamErrorResponse("invalid access token", response);

        if (username == null) return session.getUser().getBadges();

        User user = userRepository.findByUsername(username);

        if (user != null) return user.getBadges();

        return ErrorResponseFactory.getInvalidParamErrorResponse("User with this username not found", response);
    }

    @ApiOperation("Add badge. Works with admins only")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Badge added"),
            @ApiResponse(code = 400, message = "Invalid param")
    })
    @PutMapping
    public Object putBadge(
            HttpServletResponse response,

            @RequestParam("username") String username,
            @RequestParam("access_token") String accessToken,

            @ApiParam("Badge's name like developer, teacher and etc.")
            @RequestParam("badge_name") String badgeName

    ){

        Session session = this.sessionRepository.findByAccessToken(accessToken);
        if (session == null )
            return ErrorResponseFactory.getInvalidParamErrorResponse("invalid access token", response);


        if (checkUserIsNotAdmin(session.getUser()))
            return ErrorResponseFactory.getForbiddenErrorResponse("Only admin can add badges", response);


        User target = this.userRepository.findByUsername(username);
        BadgesFactory badgesFactory = BadgesFactory.getBadge(badgeName);

        if (badgesFactory == null)
            return ErrorResponseFactory.getInvalidParamErrorResponse("invalid badge name", response);

        addBadgeToUser(target, badgesFactory);
        this.userRepository.save(target);

        return null;

    }

    private void addBadgeToUser(User user, BadgesFactory badgesFactoryType ){
        Badge badge = badgesFactoryType.getInstance(user);

        // if we want to change badge's color - we have to delete old version
        user.getBadges().remove(badge);
        user.getBadges().add(badge);
    }

    private void removeBadge(User user, BadgesFactory badgesFactoryType ){
        Badge badge = badgesFactoryType.getInstance(user);
        user.getBadges().remove(badge);
    }

    @ApiOperation("Delete badge. Works with admins only")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Badge deleted"),
            @ApiResponse(code = 400, message = "Invalid param")
    })
    @DeleteMapping
    public Object removeBadge(
            HttpServletResponse response,

            @RequestParam("username") String username,
            @RequestParam("access_token") String accessToken,

            @ApiParam("Badge's name like developer, teacher and etc.")
            @RequestParam("badge_name") String badgeName
    ){

        Session session = sessionRepository.findByAccessToken(accessToken);
        if (session == null)
            return ErrorResponseFactory.getInvalidParamErrorResponse("invalid access token", response);

        if (checkUserIsNotAdmin(session.getUser()))
            return ErrorResponseFactory.getForbiddenErrorResponse("Only admin can remove badges", response);


        User target = userRepository.findByUsername(username);
        BadgesFactory badgesFactory = BadgesFactory.getBadge(badgeName);
        if (badgesFactory == null)
            return ErrorResponseFactory.getInvalidParamErrorResponse("invalid badge name", response);

        removeBadge(target, badgesFactory);
        userRepository.save(target);
        return null;

    }

    private boolean checkUserIsNotAdmin(User user){
        return user.getUserType() != UserType.ADMIN;
    }
}
