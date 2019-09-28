package com.apploidxxx.heliosrestapispring.api;


import com.apploidxxx.heliosrestapispring.api.model.ErrorMessage;
import com.apploidxxx.heliosrestapispring.api.model.Tokens;
import com.apploidxxx.heliosrestapispring.api.util.Password;
import com.apploidxxx.heliosrestapispring.entity.AuthorizationCode;
import com.apploidxxx.heliosrestapispring.entity.Session;
import com.apploidxxx.heliosrestapispring.entity.User;
import com.apploidxxx.heliosrestapispring.entity.access.repository.AuthorizationCodeRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
/**
 * @author Arthur Kupriyanov
 */
@Controller
@RequestMapping("/api/")
public class AuthApi {
    private final UserRepository userRepository;
    private final AuthorizationCodeRepository authorizationCodeRepository;

    public AuthApi(UserRepository userRepository, AuthorizationCodeRepository authorizationCodeRepository) {
        this.userRepository = userRepository;
        this.authorizationCodeRepository = authorizationCodeRepository;
    }

    @RequestMapping("/auth")
    @GetMapping(produces = "application/json")

    public @ResponseBody Object authorize( HttpServletResponse response,
            @RequestParam("login") String username,
            @RequestParam("password") String password,
            @RequestParam(value = "redirect_uri", required = false) String redirectUri,
            @RequestParam(value = "state", required = false) String state) throws IOException {
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

    @RequestMapping("/oauth")
    @GetMapping(produces = "application/json")
    public @ResponseBody Object getAccessTokens(HttpServletResponse response, @RequestParam("authorization_code") String authorizationCode){
        AuthorizationCode authCode = this.authorizationCodeRepository.findByAuthCode(authorizationCode);
        if (authCode == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
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
