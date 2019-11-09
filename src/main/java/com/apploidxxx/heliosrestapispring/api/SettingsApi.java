package com.apploidxxx.heliosrestapispring.api;

import com.apploidxxx.heliosrestapispring.api.exception.VulnerabilityException;
import com.apploidxxx.heliosrestapispring.api.exception.persistence.EntityNotFoundException;
import com.apploidxxx.heliosrestapispring.api.exception.persistence.InvalidAccessTokenException;
import com.apploidxxx.heliosrestapispring.api.exception.persistence.PersistenceException;
import com.apploidxxx.heliosrestapispring.api.model.UserSettings;
import com.apploidxxx.heliosrestapispring.api.util.ErrorResponseFactory;
import com.apploidxxx.heliosrestapispring.api.util.Password;
import com.apploidxxx.heliosrestapispring.api.util.RepositoryManager;
import com.apploidxxx.heliosrestapispring.api.util.VulnerabilityChecker;
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


    private final RepositoryManager repositoryManager;

    public SettingsApi(RepositoryManager repositoryManager) {

        this.repositoryManager = repositoryManager;
    }

    @ApiOperation(value = "Get user's settings", response = UserSettings.class)
    @GetMapping(produces = "application/json")
    public Object getSettings(
            HttpServletResponse response,
            @PathVariable("username") String username,
            @RequestParam("access_token") String accessToken
    ){
        User user;
        User target;
        try {
            user = this.repositoryManager.getUser().byUsername(username);
            target = this.repositoryManager.getUser().byAccessToken(accessToken);
        } catch (InvalidAccessTokenException e){
            return e.getResponse(response);
        } catch (PersistenceException e) {
            return ErrorResponseFactory.getInvalidParamErrorResponse("User with this username not found", response);
        }

        if (target.equals(user)) return new UserSettings(user);

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


        User target;
        try {
            this.repositoryManager.getUser().byUsername(username);  // check exist
            target = this.repositoryManager.getUser().byAccessToken(accessToken);
        }catch (InvalidAccessTokenException e){
            return e.getResponse(response);
        } catch (EntityNotFoundException e) {
            return ErrorResponseFactory.getInvalidParamErrorResponse("user not found", response);
        }

        switch (property){
            case "img":
                return changeUserImg(target, value, response);
            case "username":
                return changeUsername(target, value, response);
            case "password":
                return changeUserPassword(target, value, oldPassword, response);
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
            return ErrorResponseFactory.getInvalidParamErrorResponse(e.getMessage(), response);
        }

        user.getContactDetails().setImg(value);
        this.repositoryManager.saveUser(user);
        return null;
    }

    private Object changeUsername(User user, String value, HttpServletResponse response){
        if (repositoryManager.isUserExist(value)) {
            return ErrorResponseFactory.getInvalidParamErrorResponse("This username already is taken", response);

        }
        user.setUsername(value);
        this.repositoryManager.saveUser(user);
        return null;
    }

    private Object changeUserPassword(User user, String value, String oldPassword, HttpServletResponse response){

        if (!Password.isEqual(oldPassword, user.getPassword())) return ErrorResponseFactory.getInvalidParamErrorResponse("invalid password", response);

        user.setPassword(Password.hash(value));
        this.repositoryManager.saveUser(user);
        return null;

    }
}
