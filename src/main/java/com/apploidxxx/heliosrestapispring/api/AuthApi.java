package com.apploidxxx.heliosrestapispring.api;


import com.apploidxxx.heliosrestapispring.api.model.ErrorMessage;
import com.apploidxxx.heliosrestapispring.api.model.Tokens;
import com.apploidxxx.heliosrestapispring.api.util.Password;
import com.apploidxxx.heliosrestapispring.entity.AuthorizationCode;
import com.apploidxxx.heliosrestapispring.entity.Session;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.apploidxxx.heliosrestapispring.entity.access.repository.AuthorizationCodeRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.UserRepository;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
/**
 * @author Arthur Kupriyanov
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthApi {
    private final UserRepository userRepository;
    private final AuthorizationCodeRepository authorizationCodeRepository;

    public AuthApi(UserRepository userRepository, AuthorizationCodeRepository authorizationCodeRepository) {
        this.userRepository = userRepository;
        this.authorizationCodeRepository = authorizationCodeRepository;
    }

    @ApiOperation(value = "Authorize with user's login and password")
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "User authorized successfully. Client will redirect to redirect_uri or gets Tokens if redirect_uri is declared"
            ),
            @ApiResponse(code = 401, message = "Invalid password or login", response = ErrorMessage.class),
            @ApiResponse(code = 400, message = "Invalid params", response = ErrorMessage.class)
    })
    @GetMapping(produces = "application/json")
    public Object authorize(
            HttpServletResponse response,

            @ApiParam(value = "user's login", required = true)
            @RequestParam("login") String username,

            @ApiParam(value = "user's password", required = true)
            @RequestParam("password") String password,

            @ApiParam(value = "uri to redirect after successful auth")
            @RequestParam(value = "redirect_uri", required = false) String redirectUri,

            @ApiParam(value = "custom state value which returns as param")
            @RequestParam(value = "state", required = false) String state

    ) throws IOException {

        User user = this.userRepository.findByUsername(username);
        if (user!=null && Password.isEqual(password, user.getPassword())) {

            Session s = setUserSession(user);
            if (redirectUri == null) {
                response.setStatus(HttpServletResponse.SC_OK);
                return new Tokens(s.getAccessToken(), "refresh-accessToken", user);
            } else {
                AuthorizationCode authorizationCode = new AuthorizationCode(user);
                this.authorizationCodeRepository.save(authorizationCode);
                if (state == null) state = "state";

                response.sendRedirect(
                        String.format(redirectUri + "?authorization_code=%s&state=%s", authorizationCode.getAuthCode(), state));
                return null;
            }
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return new ErrorMessage("invalid_credentials", "invalid login or password");

        }
    }

    private Session setUserSession(User user) {

        Session s;
        if (user.getSession()!=null) s = user.getSession();
        else s = new Session();

        s.generateSession(user);
        this.userRepository.save(user);
        return s;
    }
}
