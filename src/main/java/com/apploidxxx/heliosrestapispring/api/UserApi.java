package com.apploidxxx.heliosrestapispring.api;


import com.apploidxxx.heliosrestapispring.api.model.ErrorMessage;
import com.apploidxxx.heliosrestapispring.api.model.UserInfo;
import com.apploidxxx.heliosrestapispring.api.util.ErrorResponseFactory;
import com.apploidxxx.heliosrestapispring.api.util.GroupChecker;
import com.apploidxxx.heliosrestapispring.entity.Session;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.apploidxxx.heliosrestapispring.entity.access.repository.SessionRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Arthur Kupriyanov
 */

@Controller
@RequestMapping(value = "/api/user", produces = "application/json")
public class UserApi {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public UserApi(SessionRepository sessionRepository, UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public @ResponseBody Object getInfo(
            HttpServletResponse response,
            @RequestParam("access_token") String token,
            @RequestParam(value = "username", required = false) String username) {

        User user;
        Session session = this.sessionRepository.findByAccessToken(token);
        if (session == null || (user = this.sessionRepository.findByAccessToken(token).getUser()) == null) {
            return ErrorResponseFactory.getInvalidParamErrorResponse("invalid access token", response);
        }

        if (username != null){
            if (( user = userRepository.findByUsername(username)) == null){
                return ErrorResponseFactory.getInvalidParamErrorResponse("User with this username not found", response);
            }
        }

        return new UserInfo(user);
    }

}
