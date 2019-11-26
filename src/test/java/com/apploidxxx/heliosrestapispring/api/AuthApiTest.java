package com.apploidxxx.heliosrestapispring.api;

import com.apploidxxx.heliosrestapispring.HeliosRestApiSpringApplication;
import com.apploidxxx.heliosrestapispring.api.testutil.MockUtil;
import com.apploidxxx.heliosrestapispring.api.testutil.UserBuilder;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

/**
 * @author Arthur Kupriyanov
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = HeliosRestApiSpringApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthApiTest {

    @Autowired
    private AuthApi authApiController;

    @Autowired
    private MockUtil mockUtil;

    private MockMvc mockMvc;

    @Before
    public void init() {
        this.mockMvc = standaloneSetup(this.authApiController).build();
    }

    @Test
    public void check_user_auth() throws Exception {

        // password hashed and we can't un-hash it
        // and we generate our known password
        String password = UserBuilder.generatePassword();
        User user = UserBuilder.createUser().withPassword(password).build();


        when(mockUtil.getUserRepositoryMockBean().findByUsername(user.getUsername())).thenReturn(user);

        mockMvc.perform(get("/api/auth")
                .param("login", user.getUsername())
                .param("password", password))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token", is(user.getSession().getAccessToken())));

    }

    @Test
    public void fail_auth() throws Exception {

        User user = mockUtil.getRandomUserWithMockedRepository();

        mockMvc.perform(get("/api/auth")
                .param("login", user.getUsername())
                .param("password", "another password"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void bad_request() throws Exception {

        User user = mockUtil.getRandomUserWithMockedRepository();

        // don't pass password param
        mockMvc.perform(get("/api/auth")
                .param("login", user.getUsername()))
                .andExpect(status().isBadRequest());

        // don't pass login param
        mockMvc.perform(get("/api/auth")
                .param("password", user.getPassword()))
                .andExpect(status().isBadRequest());
    }

}