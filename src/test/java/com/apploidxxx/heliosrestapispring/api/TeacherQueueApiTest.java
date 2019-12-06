package com.apploidxxx.heliosrestapispring.api;

import com.apploidxxx.heliosrestapispring.HeliosRestApiSpringApplication;
import com.apploidxxx.heliosrestapispring.api.testutil.MockUtil;
import com.apploidxxx.heliosrestapispring.api.testutil.UserBuilder;
import com.apploidxxx.heliosrestapispring.entity.Session;
import com.apploidxxx.heliosrestapispring.entity.access.repository.SessionRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.queue.QueueRepository;
import com.apploidxxx.heliosrestapispring.entity.queue.Queue;
import com.apploidxxx.heliosrestapispring.entity.queue.QueueType;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.apploidxxx.heliosrestapispring.entity.user.UserType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Arthur Kupriyanov
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = HeliosRestApiSpringApplication.class)
public class TeacherQueueApiTest {

    private final String API_PATH = "/api/teacher/queue";

    @Autowired
    MockUtil mockUtil;

    @Autowired
    TeacherQueueApi teacherQueueApi;

    @MockBean
    QueueRepository queueRepository;

    private MockMvc mockMvc;

    @Before
    public void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(this.teacherQueueApi).build();
    }


    @Test
    public void check_access_to_endpoint_by_not_teachers() throws Exception {
        User superUser = mockUtil.getRandomUserWithMockedRepository();

        Queue queue = getQueue(superUser);


        // check access for superuser
        mockMvc.perform(get(API_PATH)
                .param("queue_name", queue.getName())
                .param("access_token", superUser.getSession().getAccessToken())
        ).andExpect(status().isForbidden());

        mockMvc.perform(put(API_PATH)
                .param("queue_name", queue.getName())
                .param("access_token", superUser.getSession().getAccessToken())
                .param("passed_user", "some username")
        ).andExpect(status().isForbidden());


        User user = mockUtil.getRandomUserWithMockedRepository();
        queue.addUser(user);

        // check access for member
        mockMvc.perform(get(API_PATH)
                .param("queue_name", queue.getName())
                .param("access_token", user.getSession().getAccessToken())
        ).andExpect(status().isForbidden());

        // check access for member
        mockMvc.perform(get(API_PATH)
                .param("queue_name", queue.getName())
                .param("access_token", user.getSession().getAccessToken())
                .param("passed_user", "some username")
        ).andExpect(status().isForbidden());

    }

    @Test
    public void init_userpass_in_empty_cursored_users() throws Exception {
        User teacher = getTeacher();
        Queue queue = getQueue(teacher);

        User member;
        queue.addUser(member = mockUtil.getRandomUserWithMockedRepository());

        mockMvc.perform(put(API_PATH)
                .param("queue_name", queue.getName())
                .param("access_token", teacher.getSession().getAccessToken())
                .param("passed_user", member.getUsername())
        ).andExpect(status().isBadRequest());

    }

    @Test
    public void init_get_nextUser_with_empty_queue() throws Exception {
        User teacher = getTeacher();
        Queue queue = getQueue(teacher);

        mockMvc.perform(get(API_PATH)
                .param("queue_name", queue.getName())
                .param("access_token", teacher.getSession().getAccessToken())
                .param("passed_user", "some username")
        ).andExpect(status().isBadRequest());

    }

    @Test
    public void request_nextUser_to_endpoint() throws Exception {

        SessionRepository sessionRepository = mockUtil.getSessionRepositoryMockBean();
        User teacher = UserBuilder.createUser().withUsertype(UserType.TEACHER).build();
        new Session().generateSession(teacher);
        when(sessionRepository.findByAccessToken(teacher.getSession().getAccessToken())).thenReturn(teacher.getSession());

        Queue queue = getQueue(teacher);

        User user1, user2, user3;
        queue.addUser(user1 = mockUtil.getRandomUserWithMockedRepository("1"));
        queue.addUser(user2 = mockUtil.getRandomUserWithMockedRepository("2"));
        queue.addUser(user3 = mockUtil.getRandomUserWithMockedRepository("3"));

        mockMvc.perform(get(API_PATH)
                .param("queue_name", queue.getName())
                .param("access_token", teacher.getSession().getAccessToken())
        ).andExpect(status().isOk())
        .andExpect(jsonPath("$.current_users[0]username", is(user1.getUsername())))
        .andExpect(jsonPath("$.next_user.username", is(user2.getUsername())));

        mockMvc.perform(get(API_PATH)
                .param("queue_name", queue.getName())
                .param("access_token", teacher.getSession().getAccessToken())
        ).andExpect(status().isOk())
        .andExpect(jsonPath("$.current_users[0]username", is(user1.getUsername())))
        .andExpect(jsonPath("$.next_user.username", is(user2.getUsername())));

        // we get same result because we don't request user passed


        mockMvc.perform(put(API_PATH)
                .param("queue_name", queue.getName())
                .param("access_token", teacher.getSession().getAccessToken())
                .param("passed_user", user1.getUsername())
        ).andExpect(status().isOk());


        mockMvc.perform(get(API_PATH)
                .param("queue_name", queue.getName())
                .param("access_token", teacher.getSession().getAccessToken())
        ).andExpect(status().isOk())
        .andExpect(jsonPath("$.current_users[0]username", is(user2.getUsername())))
        .andExpect(jsonPath("$.next_user.username", is(user3.getUsername())));


        mockMvc.perform(put(API_PATH)
                .param("queue_name", queue.getName())
                .param("access_token", teacher.getSession().getAccessToken())
                .param("passed_user", user2.getUsername())
        ).andExpect(status().isOk());


        mockMvc.perform(get(API_PATH)
                .param("queue_name", queue.getName())
                .param("access_token", teacher.getSession().getAccessToken())
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.current_users[0]username", is(user3.getUsername())))
                .andExpect(jsonPath("$.next_user.username", is(user1.getUsername())));
    }

    @Test
    public void two_teachers_accept() throws Exception {

        User teacher1 = getTeacher();
        User teacher2 = getTeacher();
        Queue queue = getQueue(teacher1);

        User user1, user2, user3;
        queue.addUser(user1 = mockUtil.getRandomUserWithMockedRepository("1"));
        queue.addUser(user2 = mockUtil.getRandomUserWithMockedRepository("2"));
        queue.addUser(user3 = mockUtil.getRandomUserWithMockedRepository("3"));

        /*
            cursored : 0
            members: {1,2,3}
         */

        mockMvc.perform(get(API_PATH)
                .param("queue_name", queue.getName())
                .param("access_token", teacher1.getSession().getAccessToken())
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.current_users[0]username", is(user1.getUsername())))
                .andExpect(jsonPath("$.next_user.username", is(user2.getUsername())));

        /*
            cursored: {teacher1: 1}
            members: {2,3}
         */

        mockMvc.perform(get(API_PATH)
                .param("queue_name", queue.getName())
                .param("access_token", teacher2.getSession().getAccessToken())
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.current_users[0]username", is(user2.getUsername())))
                .andExpect(jsonPath("$.next_user.username", is(user3.getUsername())));

        /*
            cursored: {teacher1: 1, teacher2: 2}
            members: {3}
         */

        mockMvc.perform(put(API_PATH)
                .param("queue_name", queue.getName())
                .param("access_token", teacher1.getSession().getAccessToken())
                .param("passed_user", user1.getUsername())
        ).andExpect(status().isOk());

        /*
            cursored: {teacher2: 2}
            members: {3, 1}
         */


        mockMvc.perform(get(API_PATH)
                .param("queue_name", queue.getName())
                .param("access_token", teacher1.getSession().getAccessToken())
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.current_users[0]username", is(user3.getUsername())))
                .andExpect(jsonPath("$.next_user.username", is(user1.getUsername())));

        /*
            cursored: {teacher2: 2, teacher1: 3}
            members: {1}
         */

//        mockMvc.perform(get(API_PATH)
//                .param("queue_name", queue.getName())
//                .param("access_token", teacher1.getSession().getAccessToken())
//        ).andExpect(status().isOk())
//                .andExpect(jsonPath("$.current_user.username", is(user1.getUsername())))
//                .andExpect(jsonPath("$.next_user").isEmpty());
//
//        /*
//            cursored: {teacher2: 2, teacher1: 3 1}
//            members: 0
//         */
//
//
//        mockMvc.perform(get(API_PATH)
//                .param("queue_name", queue.getName())
//                .param("access_token", teacher2.getSession().getAccessToken())
//        ).andExpect(status().is4xxClientError());   // free members ended
//
//        mockMvc.perform(put(API_PATH)
//                .param("queue_name", queue.getName())
//                .param("access_token", teacher1.getSession().getAccessToken())
//                .param("passed_user", user1.getUsername())
//        ).andExpect(status().isOk());
//
//        /*
//            cursored: {teacher2: 2, teacher1: 3}
//            members: {1}
//         */
//
//        mockMvc.perform(get(API_PATH)
//                .param("queue_name", queue.getName())
//                .param("access_token", teacher2.getSession().getAccessToken())
//        ).andExpect(status().isOk())
//                .andExpect(jsonPath("$.current_user.username", is(user1.getUsername())))
//                .andExpect(jsonPath("$.next_user").isEmpty());
//
//        /*
//            cursored: {teacher2: 2 1, teacher1: 3}
//            members: 0
//         */
    }


    // TODO: write to Utility class
    private Queue getQueue(User u){
        Queue queue = new Queue("123", "555", QueueType.SINGLE);
        queue.addSuperUser(u);
        when(queueRepository.findByName(queue.getName())).thenReturn(queue);
        return queue;
    }

    private User getTeacher(){
        SessionRepository sessionRepository = mockUtil.getSessionRepositoryMockBean();
        User teacher = UserBuilder.createUser().withUsertype(UserType.TEACHER).build();
        new Session().generateSession(teacher);
        when(sessionRepository.findByAccessToken(teacher.getSession().getAccessToken())).thenReturn(teacher.getSession());

        return teacher;
    }

}