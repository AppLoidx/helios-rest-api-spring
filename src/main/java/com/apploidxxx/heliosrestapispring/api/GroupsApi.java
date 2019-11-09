package com.apploidxxx.heliosrestapispring.api;

import com.apploidxxx.heliosrestapispring.api.model.ErrorMessage;
import com.apploidxxx.heliosrestapispring.api.model.GroupModel;
import com.apploidxxx.heliosrestapispring.api.util.ErrorResponseFactory;
import com.apploidxxx.heliosrestapispring.api.util.VulnerabilityChecker;
import com.apploidxxx.heliosrestapispring.entity.Session;
import com.apploidxxx.heliosrestapispring.entity.access.repository.SessionRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.UserRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.group.GroupRepository;
import com.apploidxxx.heliosrestapispring.entity.group.UsersGroup;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.apploidxxx.heliosrestapispring.entity.user.UserType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Arthur Kupriyanov
 */
@Api
@RestController
@RequestMapping("/api/groups")
public class GroupsApi {


    private final GroupRepository groupRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public GroupsApi(GroupRepository groupRepository, SessionRepository sessionRepository, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @ApiOperation("Get queue model")
    @GetMapping
    public Object getGroup(
            HttpServletResponse response,

            @RequestParam("access_token") String accessToken,
            @RequestParam("group_name") String groupName
    ){
        Session creatorSession = this.sessionRepository.findByAccessToken(accessToken);
        if (creatorSession == null){
            return ErrorResponseFactory.getInvalidTokenErrorResponse(response);
        }

        UsersGroup group = this.groupRepository.findByName(groupName);

        if (group != null) return new GroupModel(group);

        return ErrorResponseFactory.getInvalidParamErrorResponse("invalid group_name param", response);
    }

    @ApiOperation("Create new group")
    @PostMapping
    public Object createGroup(
            HttpServletResponse response,

            @RequestParam("access_token") String accessToken,
            @RequestParam("group_name") String groupName,
            @RequestParam("fullname") String fullName,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "password", required = false) String password
    ){
        if (isGroupExist(groupName))
            return ErrorResponseFactory.getInvalidParamErrorResponse("queue with this name already exist", response);

        Session creatorSession = this.sessionRepository.findByAccessToken(accessToken);
        if (creatorSession == null){
            return ErrorResponseFactory.getInvalidTokenErrorResponse(response);
        }

        VulnerabilityChecker.checkWord(fullName);

        if (groupName.length() == 0 || groupName.matches("\\s+") ){
            return ErrorResponseFactory.getInvalidParamErrorResponse("invalid group_name param", response);
        }

        if (fullName.length() == 0 || fullName.matches("\\s+")){
            return ErrorResponseFactory.getInvalidParamErrorResponse("invalid fullname param", response);
        }

        UsersGroup group = new UsersGroup(creatorSession.getUser(), groupName, fullName, description, password);


        this.groupRepository.save(group);

        return null;
    }

    @ApiOperation("Put new user to group")
    @PutMapping
    public Object putSettings(
            HttpServletResponse response,
            @RequestParam("access_token") String accessToken,
            @RequestParam("group_name") String groupName,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "password", required = false) String password
    ){
        Session creatorSession = this.sessionRepository.findByAccessToken(accessToken);
        if (creatorSession == null){
            return ErrorResponseFactory.getInvalidTokenErrorResponse(response);
        }

        UsersGroup group = this.groupRepository.findByName(groupName);
        if (group == null) return ErrorResponseFactory.getInvalidParamErrorResponse("invalid group_name param", response);

        User user;
        if (username == null) user = creatorSession.getUser();
        else {
            if (creatorSession.getUser().getUserType() == UserType.ADMIN || creatorSession.getUser().getUserType() == UserType.TEACHER){
                user = this.userRepository.findByUsername(username);
                if (user == null) return ErrorResponseFactory.getInvalidParamErrorResponse("user not found", response);
            } else {
                return ErrorResponseFactory.getForbiddenErrorResponse("You don't have permissions to add another user", response);
            }
        }

        if (group.getUsers().contains(user)) return ErrorResponseFactory.getInvalidParamErrorResponse("User already in group", response);
        if (group.getPassword() != null){
            if (!group.getPassword().equals(password)) return ErrorResponseFactory.getForbiddenErrorResponse(response);
        }

        group.addUser(user);
        this.groupRepository.save(group);

        return null;
    }

    @DeleteMapping
    public ErrorMessage deleteGroup(
            HttpServletResponse response,

            @RequestParam("access_token") String accessToken,
            @RequestParam("group_name") String groupName
    ){
        Session creatorSession = this.sessionRepository.findByAccessToken(accessToken);
        if (creatorSession == null){
            return ErrorResponseFactory.getInvalidTokenErrorResponse(response);
        }

        UsersGroup group = this.groupRepository.findByName(groupName);
        if (group == null) return ErrorResponseFactory.getInvalidParamErrorResponse("invalid group_name param", response);

        User user = creatorSession.getUser();
        if (user.getUserType() == UserType.ADMIN || group.getGroupSuperUsers().contains(user)){
            this.groupRepository.deleteByName(groupName);
            return null;
        } else {
            return ErrorResponseFactory.getForbiddenErrorResponse(response);
        }
    }

    public boolean isGroupExist(String groupName){
        return this.groupRepository.findByName(groupName) != null;
    }

}
