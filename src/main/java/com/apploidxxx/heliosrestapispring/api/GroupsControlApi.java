package com.apploidxxx.heliosrestapispring.api;

import com.apploidxxx.heliosrestapispring.api.util.ErrorResponseFactory;
import com.apploidxxx.heliosrestapispring.api.util.RepositoryManager;
import com.apploidxxx.heliosrestapispring.api.util.VulnerabilityChecker;
import com.apploidxxx.heliosrestapispring.entity.access.repository.group.GroupRepository;
import com.apploidxxx.heliosrestapispring.entity.group.UsersGroup;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.apploidxxx.heliosrestapispring.entity.user.UserType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

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

    private Chain chain;    // TODO: rewrite it to independent bean?

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

        return chain.getAction(property, response).execute(value, group, user, response);

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
        Map<String, Action> actionsMap = new HashMap<>();
        for( Class<?> declaredClass: this.getClass().getDeclaredClasses()){

            Command annotation;

            if ((annotation = declaredClass.getAnnotation(Command.class)) == null) continue;

            boolean implementedAction = false;
            for (Class i : declaredClass.getInterfaces()){
                if (i == Action.class){
                    implementedAction = true;
                    break;
                }
            }
            if (!implementedAction){
                log.error("Class " + declaredClass.getName() + " not implemented " + Action.class.getName());
                continue;
            }

            try {
                for (Constructor c: declaredClass.getDeclaredConstructors()){
                    if (c.getParameterTypes().length == 1){
                        c.setAccessible(true);
                        actionsMap.put(annotation.value(), (Action) c.newInstance(this));
                        break;
                    }
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                log.error("Can't initialize class", e);
            }
        }

        chain = new Chain(actionsMap);
    }

    private static class Chain{
        Chain(Map<String, Action> actionsMap){
            this.actionsMap = actionsMap;
        }
        Map<String, Action> actionsMap;

        Action getAction(String property, HttpServletResponse response){
            Action action = actionsMap.get(property);
            if (action == null){
                return (v, u, g, r) -> ErrorResponseFactory.getInvalidParamErrorResponse("Property param not found", response);
            }
            return action;
        }
    }



    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    private @interface Command {
        String value();
    }




}
