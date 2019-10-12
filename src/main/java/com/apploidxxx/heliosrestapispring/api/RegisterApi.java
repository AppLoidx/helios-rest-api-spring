package com.apploidxxx.heliosrestapispring.api;


import com.apploidxxx.heliosrestapispring.api.exception.VulnerabilityException;
import com.apploidxxx.heliosrestapispring.api.model.ErrorMessage;
import com.apploidxxx.heliosrestapispring.api.util.ErrorResponseFactory;
import com.apploidxxx.heliosrestapispring.api.util.GroupChecker;
import com.apploidxxx.heliosrestapispring.api.util.Password;
import com.apploidxxx.heliosrestapispring.api.util.VulnerabilityChecker;
import com.apploidxxx.heliosrestapispring.entity.User;
import com.apploidxxx.heliosrestapispring.entity.access.repository.ContactDetailsRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;


/**
 * @author Arthur Kupriyanov
 */
@Controller
@RequestMapping("/api/register")
public class RegisterApi {
    private final ContactDetailsRepository contactDetailsRepository;
    private final UserRepository userRepository;

    public RegisterApi(ContactDetailsRepository contactDetailsRepository, UserRepository userRepository) {
        this.contactDetailsRepository = contactDetailsRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public @ResponseBody Object register(
            HttpServletResponse response,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("first_name") String firstName,
            @RequestParam("last_name") String lastName,
            @RequestParam("email") String email,
            @RequestParam(value = "group", required = false) String group){

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
            checkVulnerability(firstName, lastName, username, group);
        } catch (VulnerabilityException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return new ErrorMessage("vulnerability_warning", "sent params may have dangerous words");
        }

        return saveNewUser(username, password, firstName, lastName, email, group, response);

    }

    @DeleteMapping(produces = "application/json")
    public @ResponseBody Object deleteUser(
            HttpServletResponse response,
            @RequestParam("username") String username,
            @RequestParam("password") String password)
    {
        User user = this.userRepository.findByUsername(username);
        if (user!=null && Password.isEqual(password, user.getPassword())) {
            this.userRepository.delete(user);
            return null;
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return new ErrorMessage("invalid_credentials", "invalid username or password");
        }
    }

    // TODO: add redirect
    private Object saveNewUser(String username, String password, String firstName, String lastName, String email, String group, HttpServletResponse response){
        boolean usernameExist = usernameExist(username);
        boolean emailExist = emailExist(email);

        if (!usernameExist && !emailExist){
            this.userRepository.save(new User(username, Password.hash(password), firstName, lastName, email, group));
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

    private void checkVulnerability(String ... args) throws VulnerabilityException {
        for (String word : args){
            VulnerabilityChecker.checkWord(word);
        }
    }
}
