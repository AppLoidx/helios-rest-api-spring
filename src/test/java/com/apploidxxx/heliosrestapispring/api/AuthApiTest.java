package com.apploidxxx.heliosrestapispring.api;

import com.apploidxxx.heliosrestapispring.HeliosRestApiSpringApplication;
import com.apploidxxx.heliosrestapispring.api.model.Tokens;
import com.apploidxxx.heliosrestapispring.api.testutil.PathResolver;
import com.apploidxxx.heliosrestapispring.api.testutil.UserBuilder;
import com.apploidxxx.heliosrestapispring.entity.access.repository.UserRepository;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Arthur Kupriyanov
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = HeliosRestApiSpringApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthApiTest {
    @Autowired
    private UserRepository userRepository;

    private RestTemplate restTemplate;

    @Before
    public void init(){
        restTemplate = new RestTemplate();
    }

    @LocalServerPort
    private int port;

    @Test
    public void check_user_auth() throws URISyntaxException, IOException {

        String password = UserBuilder.generatePassword();
        User user = UserBuilder.createUser().withPassword(password).build();

        this.userRepository.save(user);

        Map<String , String> uriVariables = new HashMap<>();
        uriVariables.put("login", user.getUsername());
        uriVariables.put("password", password);
        Tokens tokens;

        assertNotNull(tokens = restTemplate.getForEntity(PathResolver.getEndpointPath("auth", port) + "?login={login}&password={password}", Tokens.class, uriVariables).getBody());

        assertNotNull(tokens.getToken());
        assertNotNull(tokens.getRefreshToken());

        URI uri = new URI(PathResolver.getEndpointPath("auth", port) + "?access_token=" + tokens.getToken());

        ClientHttpResponse response =
                restTemplate
                        .getRequestFactory()
                        .createRequest
                                (uri, HttpMethod.OPTIONS).execute();

        assertEquals(HttpStatus.OK, response.getStatusCode());

        this.userRepository.delete(user);

    }

    @Test
    public void fail_auth(){
        String password = UserBuilder.generatePassword();
        User user = UserBuilder.createUser().withPassword(password).build();

        this.userRepository.save(user);

        Map<String , String> uriVariables = new HashMap<>();
        uriVariables.put("login", user.getUsername());
        uriVariables.put("password", "incorrect password");
        Tokens tokens = null;
        try {
            tokens = restTemplate.getForEntity(PathResolver.getEndpointPath("auth", port) + "?login={login}&password={password}", Tokens.class, uriVariables).getBody();
        } catch (HttpStatusCodeException e){
            assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
        }

        assertNull(tokens);

        this.userRepository.delete(user);
    }

}