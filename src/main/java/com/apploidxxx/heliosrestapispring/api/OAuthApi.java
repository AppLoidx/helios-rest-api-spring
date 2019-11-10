package com.apploidxxx.heliosrestapispring.api;

import com.apploidxxx.heliosrestapispring.api.model.ErrorMessage;
import com.apploidxxx.heliosrestapispring.api.model.Tokens;
import com.apploidxxx.heliosrestapispring.api.util.ErrorResponseFactory;
import com.apploidxxx.heliosrestapispring.entity.AuthorizationCode;
import com.apploidxxx.heliosrestapispring.entity.Session;
import com.apploidxxx.heliosrestapispring.entity.access.repository.AuthorizationCodeRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.UserRepository;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Arthur Kupriyanov
 */
@Api(value = "Client OAuth authorization")
@RestController
@RequestMapping("/api/oauth")
public class OAuthApi {

    private final AuthorizationCodeRepository authorizationCodeRepository;
    private final UserRepository userRepository;

    public OAuthApi(AuthorizationCodeRepository authorizationCodeRepository, UserRepository userRepository) {
        this.authorizationCodeRepository = authorizationCodeRepository;
        this.userRepository = userRepository;
    }


    @ApiOperation(value = "OAuth 2.0 provider")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "You get an access_token and refresh_token", response = Tokens.class),
            @ApiResponse(code = 400, message = "Invalid params or authorization_code", response = ErrorMessage.class)
    })
    @GetMapping(produces = "application/json")
    public Object getAccessTokens(
            HttpServletResponse response,

            @ApiParam(value = "authorization_code reached with OAuth user authorization", required = true)
            @RequestParam(value = "authorization_code") String authorizationCode
    ){

        AuthorizationCode authCode = this.authorizationCodeRepository.findByAuthCode(authorizationCode);
        if (authCode == null)
            return ErrorResponseFactory.getInvalidParamErrorResponse("invalid_code", "Your authorization code is invalid", response);

        User user = authCode.getUser();
        this.authorizationCodeRepository.delete(authCode);
        Session s = setUserSession(user);

        return new Tokens(s.getAccessToken(), s.getRefreshToken(), user);
    }

    private Session setUserSession(User user) {

        Session s = user.getSession();
        s.generateSession(user);

        this.userRepository.save(user);
        return s;
    }
}
