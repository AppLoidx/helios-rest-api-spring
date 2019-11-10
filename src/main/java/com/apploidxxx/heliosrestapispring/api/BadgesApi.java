package com.apploidxxx.heliosrestapispring.api;

import com.apploidxxx.heliosrestapispring.api.util.BadgesFactory;
import com.apploidxxx.heliosrestapispring.api.util.ErrorResponseFactory;
import com.apploidxxx.heliosrestapispring.api.util.RepositoryManager;
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
    private final RepositoryManager repositoryManager;

    public BadgesApi( RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    @ApiOperation(value = "Get user badges", response = Set.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 400, message = "Invalid param")
    })
    @GetMapping(produces = "application/json")
    public Object getBadges(

            @ApiParam(value = "user's access token", required = true)
            @RequestParam("access_token") String accessToken,

            @ApiParam(value = "username of badges owner", required = true)
            @RequestParam("username") String username
    ){

        // ! WARNING: if you want refactor these two lines be careful. We need to verify user with him access_token
        User user = this.repositoryManager.getUser().byAccessToken(accessToken);

        if (username == null) return user.getBadges();

        user = this.repositoryManager.getUser().byUsername(username);

        return user.getBadges();
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

        User user = this.repositoryManager.getUser().byAccessToken(accessToken);


        if (checkUserIsNotAdmin(user))
            return ErrorResponseFactory.getForbiddenErrorResponse("Only admin can add badges", response);


        User target = this.repositoryManager.getUser().byUsername(username);
        BadgesFactory badgesFactory = BadgesFactory.getBadge(badgeName);

        if (badgesFactory == null)
            return ErrorResponseFactory.getInvalidParamErrorResponse("invalid badge name", response);

        addBadgeToUser(target, badgesFactory);
        this.repositoryManager.saveUser(target);

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

        User user = this.repositoryManager.getUser().byAccessToken(accessToken);

        if (checkUserIsNotAdmin(user))
            return ErrorResponseFactory.getForbiddenErrorResponse("Only admin can remove badges", response);


        User target = this.repositoryManager.getUser().byUsername(username);
        BadgesFactory badgesFactory = BadgesFactory.getBadge(badgeName);
        if (badgesFactory == null)
            return ErrorResponseFactory.getInvalidParamErrorResponse("invalid badge name", response);

        removeBadge(target, badgesFactory);
        this.repositoryManager.saveUser(target);
        return null;

    }

    private boolean checkUserIsNotAdmin(User user){
        return user.getUserType() != UserType.ADMIN;
    }
}
