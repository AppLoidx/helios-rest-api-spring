package com.apploidxxx.heliosrestapispring.api;

import com.apploidxxx.heliosrestapispring.api.exception.VulnerabilityException;
import com.apploidxxx.heliosrestapispring.api.model.UserSettings;
import com.apploidxxx.heliosrestapispring.api.util.ErrorResponseFactory;
import com.apploidxxx.heliosrestapispring.api.util.Password;
import com.apploidxxx.heliosrestapispring.api.util.VulnerabilityChecker;
import com.apploidxxx.heliosrestapispring.entity.Session;
import com.apploidxxx.heliosrestapispring.entity.access.repository.SessionRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.UserRepository;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/**
 * @author Arthur Kupriyanov
 */
@Api("Manage user settings")
@RestController
@RequestMapping("/api/settings/{username}")
public class SettingsApi {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;

    public SettingsApi(UserRepository userRepository, SessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
    }

    @ApiOperation(value = "Get user's settings", response = UserSettings.class)
    @GetMapping(produces = "application/json")
    public Object getSettings(
            HttpServletResponse response,
            @PathVariable("username") String username,
            @RequestParam("access_token") String accessToken
    ){
        User user;

        if ((user = userRepository.findByUsername(username)) == null){
            return ErrorResponseFactory.getInvalidParamErrorResponse("User with this username not found", response);
        }

        Session userSession = sessionRepository.findByAccessToken(accessToken);
        if (userSession != null && userSession.getUser().equals(user)){
            return new UserSettings(user);
        }

        return ErrorResponseFactory.getForbiddenErrorResponse(response);

    }

    @ApiOperation("Change user's settings")
    @PutMapping(produces = "application/json")
    public Object changeSettings(
            HttpServletResponse response,
            @PathVariable("username") String username,
            @RequestParam("access_token") String accessToken,

            @ApiParam("(img, username, password)")
            @RequestParam("property") String property,

            @RequestParam("value") String value,

            @ApiParam("Old password needed if you want change password")
            @RequestParam(value = "password", required = false) String oldPassword
    ){

        try {
            VulnerabilityChecker.checkWord(value);
        } catch (VulnerabilityException e){
            return e.getResponse(response);
        }

        User user;


        if ((user = userRepository.findByUsername(username)) == null){
            return ErrorResponseFactory.getInvalidParamErrorResponse("User with this username not found", response);
        }

        Session userSession = sessionRepository.findByAccessToken(accessToken);

        if (userSession == null || !userSession.getUser().equals(user)) return ErrorResponseFactory.getForbiddenErrorResponse(response);

        switch (property){
            case "img":
                return changeUserImg(userSession.getUser(), value, response);
            case "username":
                return changeUsername(userSession.getUser(), value, response);
            case "password":
                return changeUserPassword(userSession.getUser(), value, oldPassword, response);
            default:
                return ErrorResponseFactory.getInvalidParamErrorResponse("Property not found", response);
        }
    }

    private Object changeUserImg(User user, String value, HttpServletResponse response){

        try {
            URL url = new URL(value);
            BufferedImage image = ImageIO.read(url);

            if (image == null){
                return ErrorResponseFactory.getInvalidParamErrorResponse("your image url is invalid", response);
            } else {
                if (image.getWidth() > 1000 || image.getHeight() > 1000){
                    return ErrorResponseFactory.getInvalidParamErrorResponse("Your image is too large", response);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ErrorResponseFactory.getInternalServerError(e.getMessage(), response);
        }

        user.getContactDetails().setImg(value);
        userRepository.save(user);
        return null;
    }

    private Object changeUsername(User user, String value, HttpServletResponse response){
        User userExistCheck = userRepository.findByUsername(value);
        if (userExistCheck != null)
            return ErrorResponseFactory.getInvalidParamErrorResponse("This username already is taken", response);

        user.setUsername(value);
        userRepository.save(user);
        return null;
    }

    private Object changeUserPassword(User user, String value, String oldPassword, HttpServletResponse response){
        if (Password.isEqual(oldPassword, user.getPassword())) {
            user.setPassword(Password.hash(value));
            userRepository.save(user);
            return null;
        } else {
            return ErrorResponseFactory.getInvalidParamErrorResponse("invalid password", response);
        }
    }
}
