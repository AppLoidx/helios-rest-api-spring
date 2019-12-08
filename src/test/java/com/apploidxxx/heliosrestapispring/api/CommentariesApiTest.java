package com.apploidxxx.heliosrestapispring.api;

import com.apploidxxx.heliosrestapispring.HeliosRestApiSpringApplication;
import com.apploidxxx.heliosrestapispring.api.testutil.MockUtil;
import com.apploidxxx.heliosrestapispring.entity.access.repository.commentaries.CommentaryRepository;
import com.apploidxxx.heliosrestapispring.entity.commentary.Commentary;
import com.apploidxxx.heliosrestapispring.entity.commentary.CommentaryType;
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

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Arthur Kupriyanov
 */
@SpringBootTest(classes = HeliosRestApiSpringApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class CommentariesApiTest {
    @Autowired
    private CommentariesApi commentariesApiRestController;

    @Autowired
    private MockUtil mockUtil;

    @MockBean
    private CommentaryRepository commentaryRepository;

    private MockMvc mockMvc;

    @Before
    public void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(commentariesApiRestController).build();
    }

    @Test
    public void commentary_add_test() throws Exception {
        User target = mockUtil.getRandomUserWithMockedRepository();
        User teacher = mockUtil.getRandomUserWithMockedRepository();
        teacher.setUserType(UserType.TEACHER);

        String commentaryText = "Some commentary";

        mockMvc.perform(put("/api/commentaries")
                .param("username", target.getUsername())
                .param("access_token", teacher.getSession().getAccessToken())
                .param("text", commentaryText))
                .andExpect(status().isOk());
    }

    @Test
    public void get_commentaries() throws Exception {
        User target = mockUtil.getRandomUserWithMockedRepository();
        User teacher = mockUtil.getRandomUserWithMockedRepository();
        teacher.setUserType(UserType.TEACHER);

        String commentaryText = "Some commentary";

        target.getCommentaries().add(new Commentary(target, teacher, commentaryText));

        mockMvc.perform(get("/api/commentaries")
                .param("username", target.getUsername())
                .param("access_token", teacher.getSession().getAccessToken()))
                .andExpect(jsonPath("$[0].creation_date").isNotEmpty())
                .andExpect(jsonPath("$[0].author.username", is(teacher.getUsername())))
                .andExpect(jsonPath("$[0].target.username", is(target.getUsername())))
                .andExpect(jsonPath("$[0].text", is(commentaryText)))
                .andExpect(jsonPath("$[0].commentary_type", is("PRIVATE")));

        String secondCommentaryText = "Second text";
        target.getCommentaries().add(new Commentary(target, teacher, secondCommentaryText, CommentaryType.PUBLIC));

        mockMvc.perform(get("/api/commentaries")
                .param("username", target.getUsername())
                .param("access_token", teacher.getSession().getAccessToken()))
                .andExpect(jsonPath("$[1].creation_date").isNotEmpty())
                .andExpect(jsonPath("$[1].author.username", is(teacher.getUsername())))
                .andExpect(jsonPath("$[1].target.username", is(target.getUsername())))
                .andExpect(jsonPath("$[1].text", is(secondCommentaryText)))
                .andExpect(jsonPath("$[1].commentary_type", is("PUBLIC")));
    }

    @Test
    public void access_by_not_teacher() throws Exception {
        User target = mockUtil.getRandomUserWithMockedRepository();
        User ordinaryUser = mockUtil.getRandomUserWithMockedRepository();

        String commentaryText = "Some commentary";

        // create private commentary which access only for teachers
        target.getCommentaries().add(new Commentary(target, ordinaryUser, commentaryText));

        mockMvc.perform(get("/api/commentaries")
                .param("username", target.getUsername())
                .param("access_token", ordinaryUser.getSession().getAccessToken()))
                .andExpect(status().isForbidden());

        // target gets empty array because all commentaries is private
        mockMvc.perform(get("/api/commentaries")
                .param("username", target.getUsername())
                .param("access_token", target.getSession().getAccessToken()))
                .andExpect(jsonPath("$").isEmpty());

        // create public commentary which access for target and teacher
        target.getCommentaries().add(new Commentary(target, ordinaryUser, commentaryText, CommentaryType.PUBLIC));

        // target have access to public commentaries
        mockMvc.perform(get("/api/commentaries")
                .param("username", target.getUsername())
                .param("access_token", target.getSession().getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].creation_date").isNotEmpty())
                .andExpect(jsonPath("$[0].author.username", is(ordinaryUser.getUsername())))
                .andExpect(jsonPath("$[0].target.username", is(target.getUsername())))
                .andExpect(jsonPath("$[0].text", is(commentaryText)))
                .andExpect(jsonPath("$[0].commentary_type", is("PUBLIC")));

        mockMvc.perform(get("/api/commentaries")
                .param("username", target.getUsername())
                .param("access_token", ordinaryUser.getSession().getAccessToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    public void check_access_to_delete() throws Exception {
        User target = mockUtil.getRandomUserWithMockedRepository();
        User teacher = mockUtil.getRandomUserWithMockedRepository();
        teacher.setUserType(UserType.TEACHER);

        Commentary commentary = new Commentary(target, teacher, "some text");
        commentary.setId(9999L);
        target.getCommentaries().add(commentary);

        when(commentaryRepository.findById(commentary.getId())).thenReturn(Optional.of(commentary));

        mockMvc.perform(delete("/api/commentaries")
                .param("access_token", teacher.getSession().getAccessToken())
                .param("commentary_id", commentary.getId().toString()))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/commentaries")
                .param("access_token", target.getSession().getAccessToken())
                .param("commentary_id", commentary.getId().toString()))
                .andExpect(status().isForbidden());
    }
}