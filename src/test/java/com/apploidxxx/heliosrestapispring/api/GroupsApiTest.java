package com.apploidxxx.heliosrestapispring.api;

import com.apploidxxx.heliosrestapispring.HeliosRestApiSpringApplication;
import com.apploidxxx.heliosrestapispring.api.testutil.UserBuilder;
import com.apploidxxx.heliosrestapispring.entity.Session;
import com.apploidxxx.heliosrestapispring.entity.access.repository.SessionRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.group.GroupRepository;
import com.apploidxxx.heliosrestapispring.entity.group.UsersGroup;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

/**
 * @author Arthur Kupriyanov
 */
@SpringBootTest(classes = HeliosRestApiSpringApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class GroupsApiTest {


    private MockMvc mockMvc;

    @Autowired
    private GroupsApi groupsApiController;

    @MockBean
    private GroupRepository groupRepository;

    @MockBean
    private SessionRepository sessionRepository;

    @Before
    public void setup() {
        this.mockMvc = standaloneSetup(this.groupsApiController).build();
    }

    @Transactional
    @Test
    public void test_group_creation_without_password() throws Throwable {
        User user = getUser();

        String accessToken = user.getSession().getAccessToken();
        String groupName = "groupName";
        String groupFullname = "fullname of group";
        String description = "something description";

        mockMvc.perform(post("/api/groups")
                .param("access_token", accessToken)
                .param("group_name", groupName)
                .param("fullname", groupFullname)
                .param("description", description))
                .andExpect(status().isOk());
    }

    @Test
    public void test_group_with_password() throws Throwable {
        User user = getUser();
        String accessToken = user.getSession().getAccessToken();


        mockMvc.perform(post("/api/groups")
                .param("access_token", accessToken)
                .param("group_name", "test_group_with_password")
                .param("fullname", "groupFullname")
                .param("password", "somePassword")
        ).andExpect(status().isOk());

    }

    @Transactional
    @Test
    public void try_access_group_with_password() throws Throwable {
        User user = getUser();

        String groupName = "try_access_group_with_password";
        String password = "123";

        UsersGroup usersGroup = new UsersGroup(user, groupName, "", "", password);
        when(this.groupRepository.findByName(groupName)).thenReturn(usersGroup);

        mockMvc.perform(get("/api/groups")
                .param("group_name", groupName)
                .param("access_token", user.getSession().getAccessToken())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        User notAccessedUser = getUser();

        mockMvc.perform(put("/api/groups")
                .param("group_name", groupName)
                .param("access_token", notAccessedUser.getSession().getAccessToken())
                .param("password", password)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/groups")
                .param("group_name", groupName)
                .param("access_token", notAccessedUser.getSession().getAccessToken())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.super_users[0].username", is(user.getUsername())))
                .andExpect(jsonPath("$.super_users[0].fullname", is(user.getFirstName() + " " + user.getLastName())))
                .andExpect(jsonPath("$.users[0].username", is(notAccessedUser.getUsername())))
                .andExpect(jsonPath("$.users[0].fullname", is(notAccessedUser.getFirstName() + " " + notAccessedUser.getLastName())));
    }

    @Test
    public void get_group_test() throws Exception {
        User user = getUser();
        String groupName = "123";
        String fullname = "Fullname of group";
        String description = "description";
        String password = "zxcwgweg22t!";
        UsersGroup usersGroup = new UsersGroup(user, groupName, fullname, description, password);
        usersGroup.addUser(user);

        when(this.groupRepository.findByName(groupName)).thenReturn(usersGroup);


        mockMvc.perform(get("/api/groups")
                .param("access_token", user.getSession().getAccessToken())
                .param("group_name", groupName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").isNotEmpty())
                .andExpect(jsonPath("$.super_users").isNotEmpty())
                .andExpect(jsonPath("$.group.name", is(groupName)))
                .andExpect(jsonPath("$.group.fullname", is(fullname)))
                .andExpect(jsonPath("$.group.description", is(description)))
                .andExpect(jsonPath("$.group.password").doesNotExist());


    }

    private User getUser() {
        User user = UserBuilder.createUser().build();
        new Session().generateSession(user);
        when(this.sessionRepository.findByAccessToken(user.getSession().getAccessToken())).thenReturn(user.getSession());

        return user;
    }

}