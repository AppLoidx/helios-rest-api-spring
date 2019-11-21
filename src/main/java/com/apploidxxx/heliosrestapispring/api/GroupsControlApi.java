package com.apploidxxx.heliosrestapispring.api;

import com.apploidxxx.heliosrestapispring.api.util.ErrorResponseFactory;
import com.apploidxxx.heliosrestapispring.api.util.RepositoryManager;
import com.apploidxxx.heliosrestapispring.api.util.VulnerabilityChecker;
import com.apploidxxx.heliosrestapispring.api.util.chain.Chain;
import com.apploidxxx.heliosrestapispring.api.util.chain.Command;
import com.apploidxxx.heliosrestapispring.api.util.chain.CommandChain;
import com.apploidxxx.heliosrestapispring.entity.access.repository.group.GroupRepository;
import com.apploidxxx.heliosrestapispring.entity.group.UsersGroup;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.apploidxxx.heliosrestapispring.entity.user.UserType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Arthur Kupriyanov
 */
@Slf4j
@RestController
@RequestMapping("/api/groups.control/{groupName}")
public class GroupsControlApi {

    private final GroupRepository groupRepository;
    private final RepositoryManager repositoryManager;

    public GroupsControlApi(GroupRepository groupRepository, RepositoryManager repositoryManager) {
        this.groupRepository = groupRepository;
        this.repositoryManager = repositoryManager;
    }

    private Chain<Action> chain;    // TODO: rewrite it to independent bean?

    @PutMapping
    public Object putSetting(
            HttpServletResponse response,

            @PathVariable("groupName") String groupName,

            @RequestParam("access_token") String accessToken,

            @RequestParam("property") String property,
            @RequestParam("value") String value
    ){

        UsersGroup group = this.groupRepository.findByName(groupName);
        User user = repositoryManager.getUser().byAccessToken(accessToken);
        if (group == null) return ErrorResponseFactory.getInvalidParamErrorResponse("group not found", response);

        property = property.toLowerCase();

        return chain.getAction(property).execute(value, group, user, response);

    }

    private boolean checkAccess(User target, User employer, UsersGroup usersGroup){
        return target.equals(employer) || isSuperForGroup(employer, usersGroup);
    }

    private boolean isSuperForGroup(User employer, UsersGroup usersGroup){
        return employer.getUserType() == UserType.TEACHER || employer.getUserType() == UserType.ADMIN || usersGroup.getGroupSuperUsers().contains(employer);
    }

    @Command("add_user")
    private class AddUserCommand implements Action {

        @Override
        public Object execute(String value, UsersGroup usersGroup, User user, HttpServletResponse response) {
            User target = repositoryManager.getUser().byUsername(value);
            if (!checkAccess(target, user, usersGroup)) return ErrorResponseFactory.getForbiddenErrorResponse(response);

            if (usersGroup.getUsers().contains(target)) return ErrorResponseFactory.getInvalidParamErrorResponse("user already exist", response);
            usersGroup.addUser(target);
            groupRepository.save(usersGroup);
            return null;
        }
    }

    @Command("remove_user")
    private class RemoveUserCommand implements Action {

        @Override
        public Object execute(String value, UsersGroup usersGroup, User user, HttpServletResponse response) {
            User target = repositoryManager.getUser().byUsername(value);
            if (!checkAccess(target, user, usersGroup)) return ErrorResponseFactory.getForbiddenErrorResponse(response);

            if (!usersGroup.getUsers().contains(target)) return ErrorResponseFactory.getInvalidParamErrorResponse("user doesn't exist in group", response);
            usersGroup.deleteUser(target);
            groupRepository.save(usersGroup);
            return null;
        }
    }

    @Command("change_password")
    private class ChangePasswordCommand implements Action {

        @Override
        public Object execute(String value, UsersGroup usersGroup, User user, HttpServletResponse response) {
            if (!isSuperForGroup(user, usersGroup)) return ErrorResponseFactory.getForbiddenErrorResponse(response);
            usersGroup.setPassword(value);  // TODO: add validation
            groupRepository.save(usersGroup);
            return null;
        }
    }

    @Command("change_fullname")
    private class ChangeFullnameCommand implements Action {

        @Override
        public Object execute(String value, UsersGroup usersGroup, User user, HttpServletResponse response) {
            if (!isSuperForGroup(user, usersGroup)) return ErrorResponseFactory.getForbiddenErrorResponse(response);
            VulnerabilityChecker.checkWord(value);
            usersGroup.setFullname(value);  // TODO: add validation
            groupRepository.save(usersGroup);
            return null;
        }
    }


    @FunctionalInterface
    private interface Action{
        Object execute(String value, UsersGroup usersGroup, User user, HttpServletResponse response);
    }

    @PostConstruct
    private void init(){
        chain = new CommandChain().init(this, Action.class);
    }

}
