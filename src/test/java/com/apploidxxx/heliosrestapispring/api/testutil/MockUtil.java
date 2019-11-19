package com.apploidxxx.heliosrestapispring.api.testutil;

import com.apploidxxx.heliosrestapispring.entity.Session;
import com.apploidxxx.heliosrestapispring.entity.access.repository.SessionRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.UserRepository;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.stereotype.Component;

import static org.mockito.Mockito.when;

/**
 *
 * Class for generate entities with mocking via {@link org.mockito.Mockito#when(Object)}
 * <br><br>
 * <strong style="color: orange">WARING</strong>:
 *      remember not to declare such MockBean declared here
 *      because you can get an IllegalStateException that is thrown
 *      when declared two similar MockBean
 *
 * <br><br>
 * <hr>
 * <strong>Declared MockBeans:</strong>
 *      <ul>
 *          <li>{@link UserRepository}</li>
 *      </ul>
 * <hr>
 *
 *     <img src="https://i.pinimg.com/564x/4b/1f/25/4b1f25db68feafeea9fa57d5da4dab6a.jpg" />
 *
 * @version 1.0.0
 * @author Arthur Kupriyanov
 */
@Component
public class MockUtil {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private SessionRepository sessionRepository;

    /**
     * Create {@link User} entity via {@link UserBuilder} and
     * mock {@link UserRepository} as mockedBean
     *
     * Mocked: {@link UserRepository#findByUsername(String)}
     *
     * @return random generated and mocked User
     */
    public User getRandomUserWithMockedRepository(){
        User user = UserBuilder.createUser().build();
        new Session().generateSession(user);
        when(this.userRepository.findByUsername(user.getUsername())).thenReturn(user);
        when(this.sessionRepository.findByAccessToken(user.getSession().getAccessToken())).thenReturn(user.getSession());
        return user;
    }

    /**
     * Get a mocked Repository if you want mock custom entity
     *
     * @return {@link UserRepository} with {@link MockBean}
     */
    public UserRepository getUserRepositoryMockBean(){
        return userRepository;
    }
}
