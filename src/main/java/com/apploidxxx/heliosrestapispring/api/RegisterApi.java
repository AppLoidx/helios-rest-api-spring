package com.apploidxxx.heliosrestapispring.api;


import com.apploidxxx.heliosrestapispring.api.exception.ResponsibleException;
import com.apploidxxx.heliosrestapispring.api.exception.VulnerabilityException;
import com.apploidxxx.heliosrestapispring.api.model.ErrorMessage;
import com.apploidxxx.heliosrestapispring.api.util.*;
import com.apploidxxx.heliosrestapispring.entity.access.repository.ContactDetailsRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.UserRepository;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * @author Arthur Kupriyanov
 */
@Api("User registration and deleting")
@RestController
@RequestMapping("/api/register")
public class RegisterApi {
    private final ContactDetailsRepository contactDetailsRepository;
    private final UserRepository userRepository;

    public RegisterApi(ContactDetailsRepository contactDetailsRepository, UserRepository userRepository) {
        this.contactDetailsRepository = contactDetailsRepository;
        this.userRepository = userRepository;
    }

    @ApiOperation("Register a user")
    @PostMapping
    public Object register(
            HttpServletResponse response,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("first_name") String firstName,
            @RequestParam("last_name") String lastName,
            @RequestParam("email") String email,
            @RequestParam(value = "group", required = false) String group,
            @RequestParam(value = "redirect_uri", defaultValue = "/auth/login.html") String redirectUri,
            @RequestParam(value = "state", required = false) String state) throws IOException {

        if ("".equals(password) || password.length() < 8){
            return ErrorResponseFactory.getInvalidParamErrorResponse("Your password length is too small", response);
        }

        if (!email.matches(".+@.+")){
            return ErrorResponseFactory.getInvalidParamErrorResponse("Invalid email param", response);
        }

        if (!firstName.matches("[^\\s]+") || !lastName.matches("[^\\s]+")){
            return ErrorResponseFactory.getInvalidParamErrorResponse("Invalid first_name or last_name param", response);
        }

        if (!username.matches("[^\\s]+")){
            return ErrorResponseFactory.getInvalidParamErrorResponse("Invalid username param", response);
        }

        if ("".equals(group)) group = null;
        if (group != null && !group.matches("[^\\s]+")){
            return ErrorResponseFactory.getInvalidParamErrorResponse("Invalid group name", response);
        } else {
            if (group != null && !GroupChecker.isValid(group)) return ErrorResponseFactory.getInvalidParamErrorResponse("Group not found", response);
        }


        try {
            VulnerabilityChecker.checkWord(firstName, lastName, username, group);
        } catch (VulnerabilityException e) {
            return e.getResponse(response);
        }

        return saveNewUser(username, password, firstName, lastName, email, group, response, redirectUri, state);

    }

    @ApiOperation("Delete user from system")
    @DeleteMapping(produces = "application/json")
    public Object deleteUser(
            HttpServletResponse response,
            @RequestParam("username") String username,
            @RequestParam("password") String password)
    {
        User user = this.userRepository.findByUsername(username);
        if (user!=null && Password.isEqual(password, user.getPassword())) {
            this.userRepository.delete(user);
            return null;
        }

        return ErrorResponseFactory.getInvalidParamErrorResponse("invalid_credentials", "invalid username or password", response);

    }

    private Object saveNewUser(
            String username, String password, String firstName, String lastName, String email, String group,
            HttpServletResponse response, String redirectUri, String state
    ) throws IOException {

        boolean usernameExist = usernameExist(username);
        boolean emailExist = emailExist(email);

        if (!usernameExist && !emailExist){
            this.userRepository.save(new User(username, Password.hash(password), firstName, lastName, email, group));
            if (RedirectUriChecker.checkIsSafe(redirectUri)){
                response.sendRedirect("/auth/login.html?redirect_uri=" + redirectUri + "&state=" + state);  //lgtm [java/unvalidated-url-redirection]
            } else throw getRedirectUriIsNotSafeException();

            return null;
        }
        else {
            if (usernameExist) return ErrorResponseFactory.getInvalidParamErrorResponse("This username already is taken", response);
            else return ErrorResponseFactory.getInvalidParamErrorResponse("This email already is taken", response);
        }
    }

    private boolean usernameExist(String username) {
        return this.userRepository.findByUsername(username) != null;
    }

    private boolean emailExist(String email){
        return this.contactDetailsRepository.findByEmail(email) != null;
    }

    private ResponsibleException getRedirectUriIsNotSafeException(){
        return new VulnerabilityException(
                "Your redirect Uri is not safe",
                new ErrorMessage("redirect_uri_is_not_safe",
                        "Your provided redirect uri is not safe")
        );
    }
}
