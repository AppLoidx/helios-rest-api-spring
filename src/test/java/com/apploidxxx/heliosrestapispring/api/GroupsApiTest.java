package com.apploidxxx.heliosrestapispring.api;

import com.apploidxxx.heliosrestapispring.HeliosRestApiSpringApplication;
import com.apploidxxx.heliosrestapispring.api.model.GroupModel;
import com.apploidxxx.heliosrestapispring.api.testutil.PathResolver;
import com.apploidxxx.heliosrestapispring.api.testutil.UserBuilder;
import com.apploidxxx.heliosrestapispring.api.util.RequestUtil;
import com.apploidxxx.heliosrestapispring.entity.Session;
import com.apploidxxx.heliosrestapispring.entity.access.repository.UserRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.group.GroupRepository;
import com.apploidxxx.heliosrestapispring.entity.group.UsersGroup;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Arthur Kupriyanov
 */
@SpringBootTest(classes = HeliosRestApiSpringApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class GroupsApiTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @LocalServerPort
    private int port;

    private RestTemplate restTemplate = new RestTemplate();

    @Test
    public void test_group_creation_without_password() throws Throwable {
        User user = getUser();

        String accessToken = user.getSession().getAccessToken();
        String groupName = "groupName";
        String groupFullname = "fullname of group";
        String description = "something description";
        Map<String , String> uriVariables = RequestUtil.getMap(
                "access_token", accessToken,
                "group_name", groupName,
                "fullname", groupFullname,
                "description", description
        );

        try {
            ResponseEntity responseEntity = restTemplate.postForEntity(
                    PathResolver.getEndpointPath(RequestUtil.generatePathWithParams("groups", uriVariables), port), null, String.class, uriVariables);
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        } catch (HttpStatusCodeException e){
            System.err.println(e.getResponseBodyAsString());
            throw e.getCause();
        }

        UsersGroup group = this.groupRepository.findByName(uriVariables.get("group_name"));
        assertNotNull(group);
        assertEquals(uriVariables.get("fullname"), group.getFullname());
        assertEquals(uriVariables.get("description"), group.getDescription());
    }

    @Test
    public void test_group_with_password() throws Throwable {
        User user = getUser();
        String accessToken = user.getSession().getAccessToken();

        Map<String , String> uriVariables = RequestUtil.getMap(
                "access_token", accessToken,
                "group_name", "groupName",
                "fullname", "groupFullname",
                "password", "somePassword"
        );

        try {
            ResponseEntity responseEntity = restTemplate.postForEntity(
                    PathResolver.getEndpointPath(RequestUtil.generatePathWithParams("groups", uriVariables), port), null, String.class, uriVariables);
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        } catch (HttpStatusCodeException e){
            System.err.println(e.getResponseBodyAsString());
            throw e.getCause();
        }

        UsersGroup group = this.groupRepository.findByName(uriVariables.get("group_name"));
        assertNotNull(group);
        assertNotNull(group.getPassword());
    }

    @Test
    public void try_access_group_with_password() throws Throwable {
        User user = getUser();
        String groupName = "123";
        String password = "123";
        UsersGroup usersGroup = new UsersGroup(user, groupName, "", "", password);
        this.groupRepository.save(usersGroup);

        User notAccessedUser = getUser();
        Map<String, String> uriVariables = RequestUtil.getMap(
                "access_token", notAccessedUser.getSession().getAccessToken(),
                "group_name", groupName
        );


        try {
            restTemplate.put(getEndpointWithParams(uriVariables), null, uriVariables);

        } catch (HttpStatusCodeException e){
            assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
        }

        uriVariables.put("password", password);

        try {
            restTemplate.put(getEndpointWithParams(uriVariables), null, uriVariables);
        } catch (HttpStatusCodeException e){
            System.err.println(e.getResponseBodyAsString());
            throw e.getCause();
        }

        UsersGroup newGroup = this.groupRepository.findByName(groupName);
        assertTrue(newGroup.getUsers().contains(notAccessedUser));

    }

    @Test
    public void get_group_test(){
        User user = getUser();
        String groupName = "123";
        String fullname = "Fullname of group";
        String description = "description";
        String password = "zxcwgweg22t!";
        UsersGroup usersGroup = new UsersGroup(user, groupName, fullname, description, password);
        usersGroup.addUser(user);
        this.groupRepository.save(usersGroup);
        Map<String, String> uriVariables = RequestUtil.getMap("access_token", user.getSession().getAccessToken(), "group_name", groupName);

        ResponseEntity<GroupModel> usersGroupResponseEntity = restTemplate.getForEntity(getEndpointWithParams(uriVariables), GroupModel.class, uriVariables);
        assertEquals(HttpStatus.OK, usersGroupResponseEntity.getStatusCode());

        GroupModel group = usersGroupResponseEntity.getBody();

        assertNotNull(group);
        assertFalse(group.getUsers().isEmpty());
        Map<String, String> userMap = getUserMap(user);

        assertTrue(group.getUsers().contains(userMap));

        assertEquals(groupName, group.getGroup().getName());
        assertEquals(fullname, group.getGroup().getFullname());
        assertEquals(description, group.getGroup().getDescription());

        assertNull(group.getGroup().getUsers());
        assertNull(group.getGroup().getGroupSuperUsers());
        assertNull(group.getGroup().getPassword());
    }

    private String getEndpointWithParams(Map<String, String> uriVariables){
        return PathResolver.getEndpointPath(RequestUtil.generatePathWithParams("groups", uriVariables), port);
    }

    private User getUser(){
        User user = UserBuilder.createUser().build();
        new Session().generateSession(user);
        this.userRepository.save(user);

        return user;
    }

    private Map<String, String> getUserMap(User user){
        Map<String, String> userMap = new HashMap<>();
        userMap.put("username", user.getUsername());
        userMap.put("fullname", user.getFirstName() + " " + user.getLastName());
        userMap.put("img", user.getContactDetails().getImg());
        return userMap;
    }

}