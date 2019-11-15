package com.apploidxxx.heliosrestapispring.api.testutil;

import com.apploidxxx.heliosrestapispring.entity.access.repository.UserRepository;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.stereotype.Component;

import static org.mockito.Mockito.when;

/**
 *
 * Class for generate entities with mocking via {@link org.mockito.Mockito#when(Object)}
 *
 * WARING:
 *      don't forget to don't declare a such MockBean declared here
 *      because you can get an IllegalStateException, which invoked
 *      when declared two similar MockBean
 *
 * Declared MockBeans:
 *      * {@link UserRepository}
 *
 * @version 1.0.0
 * @author Arthur Kupriyanov
 */
@Component
public class MockUtil {

    @MockBean
    private UserRepository userRepository;


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
        when(this.userRepository.findByUsername(user.getUsername())).thenReturn(user);

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
