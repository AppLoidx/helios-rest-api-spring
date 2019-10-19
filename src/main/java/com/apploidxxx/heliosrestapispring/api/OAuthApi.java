package com.apploidxxx.heliosrestapispring.api;

import com.apploidxxx.heliosrestapispring.api.model.ErrorMessage;
import com.apploidxxx.heliosrestapispring.api.model.Tokens;
import com.apploidxxx.heliosrestapispring.entity.AuthorizationCode;
import com.apploidxxx.heliosrestapispring.entity.Session;
import com.apploidxxx.heliosrestapispring.entity.access.repository.AuthorizationCodeRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.UserRepository;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.*;

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
    public @ResponseBody
    Object getAccessTokens(
            HttpServletResponse response,
            @ApiParam(value = "authorization_code reached with OAuth user authorization", required = true)@RequestParam(value = "authorization_code", required = false)
                    String authorizationCode
    ){

        AuthorizationCode authCode = this.authorizationCodeRepository.findByAuthCode(authorizationCode);
        if (authCode == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            // we need to response our error message instead of spring invalid param
            return new ErrorMessage("invalid_code", "Your authorization code is invalid");
        }

        User user = authCode.getUser();
        this.authorizationCodeRepository.delete(authCode);
        Session s = setUserSession(user);
        response.setStatus(HttpServletResponse.SC_OK);

        return new Tokens(s.getAccessToken(), s.getRefreshToken(), user);
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
