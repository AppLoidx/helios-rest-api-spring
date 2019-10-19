package com.apploidxxx.heliosrestapispring.api;

import com.apploidxxx.heliosrestapispring.api.util.Badges;
import com.apploidxxx.heliosrestapispring.api.util.ErrorResponseFactory;
import com.apploidxxx.heliosrestapispring.entity.Session;
import com.apploidxxx.heliosrestapispring.entity.access.repository.SessionRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.UserRepository;
import com.apploidxxx.heliosrestapispring.entity.user.Badge;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.apploidxxx.heliosrestapispring.entity.user.UserType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Arthur Kupriyanov
 */
@RequestMapping("/api/badges")
@RestController
public class BadgesApi {
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public BadgesApi(SessionRepository sessionRepository, UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public Object getBadges(
            @RequestParam("access_token") String accessToken,
            @RequestParam("username") String username,
            HttpServletResponse response
    ){
        Session session = sessionRepository.findByAccessToken(accessToken);
        if (session != null && session.getUser() != null){
            User user;
            if (username != null){
                if (( user = userRepository.findByUsername(username)) == null){
                    return ErrorResponseFactory.getInvalidParamErrorResponse("User with this username not found", response);
                }
            } else user = session.getUser();

            return user.getBadges();
        } else {
            return ErrorResponseFactory.getInvalidParamErrorResponse("invalid access token", response);
        }
    }

    @PutMapping
    public Object putBadge(
            @RequestParam("username") String username,
            @RequestParam("access_token") String accessToken,
            @RequestParam("badge_name") String badgeName,
            HttpServletResponse response){

        Session session = sessionRepository.findByAccessToken(accessToken);
        if (session == null || session.getUser() == null){
            return ErrorResponseFactory.getInvalidParamErrorResponse("invalid access token", response);
        }

        if (session.getUser().getUserType() != UserType.ADMIN){
            return ErrorResponseFactory.getForbiddenErrorResponse("Only admin can add badges", response);
        }

        User target = userRepository.findByUsername(username);
        Badges badges = Badges.getBadge(badgeName);
        if (badges != null){
            Badge badge = badges.getInstance(target);
            target.getBadges().remove(badge);   // if you want to change badge's color - you have to delete old version
            target.getBadges().add(badge);
            userRepository.save(target);
            return null;
        } else {
            return ErrorResponseFactory.getInvalidParamErrorResponse("invalid badge name", response);
        }
    }

    @DeleteMapping
    public Object removeBadge(
            @RequestParam("username") String username,
            @RequestParam("access_token") String accessToken,
            @RequestParam("badge_name") String badgeName,
            HttpServletResponse response){

        Session session = sessionRepository.findByAccessToken(accessToken);
        if (session == null || session.getUser() == null){
            return ErrorResponseFactory.getInvalidParamErrorResponse("invalid access token", response);
        }

        if (session.getUser().getUserType() != UserType.ADMIN){
            return ErrorResponseFactory.getForbiddenErrorResponse("Only admin can remove badges", response);
        }

        User target = userRepository.findByUsername(username);
        Badges badges = Badges.getBadge(badgeName);
        if (badges != null){
            Badge badge = badges.getInstance(target);
            target.getBadges().remove(badge);
            userRepository.save(target);
            return null;
        } else {
            return ErrorResponseFactory.getInvalidParamErrorResponse("invalid badge name", response);
        }
    }
}
