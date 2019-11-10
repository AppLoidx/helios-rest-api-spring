package com.apploidxxx.heliosrestapispring.api;


import com.apploidxxx.heliosrestapispring.api.model.ErrorMessage;
import com.apploidxxx.heliosrestapispring.api.model.UserInfo;
import com.apploidxxx.heliosrestapispring.api.util.ErrorResponseFactory;
import com.apploidxxx.heliosrestapispring.entity.Session;
import com.apploidxxx.heliosrestapispring.entity.access.repository.SessionRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.UserRepository;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Arthur Kupriyanov
 */
@Api("User's data")
@RestController
@RequestMapping(value = "/api/user", produces = "application/json")
public class UserApi {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public UserApi(SessionRepository sessionRepository, UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @ApiOperation("Get user info")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Get a user info as json", response = UserInfo.class),
            @ApiResponse(code = 400, message = "Invalid params", response = ErrorMessage.class)
    })
    @GetMapping
    public Object getInfo(
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
