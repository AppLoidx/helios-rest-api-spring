package com.apploidxxx.heliosrestapispring.api;

import com.apploidxxx.heliosrestapispring.api.testutil.MockUtil;
import com.apploidxxx.heliosrestapispring.entity.access.repository.group.GroupRepository;
import com.apploidxxx.heliosrestapispring.entity.group.UsersGroup;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Arthur Kupriyanov
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class GroupsControlApiTest {

    @Autowired
    MockUtil mockUtil;

    private MockMvc mockMvc;

    @MockBean
    private GroupRepository groupRepository;

    @Autowired
    private GroupsControlApi groupsControlApiController;

    @Before
    public void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(this.groupsControlApiController).build();
    }

    @Test
    public void change_fullname() throws Exception {
        User user = mockUtil.getRandomUserWithMockedRepository();
        String fullname = "old Fullname Of Queue";
        UsersGroup usersGroup = new UsersGroup(user, "zxcs", fullname, "");
        when(groupRepository.findByName(usersGroup.getName())).thenReturn(usersGroup);

        String newFullname = "new Queue fullname example";

        doAnswer(i -> {
            assertEquals(newFullname, ((UsersGroup) i.getArgument(0)).getFullname());
            return null;
        }).when(groupRepository).save(usersGroup);

        mockMvc.perform(put("/api/groups.control/" + usersGroup.getName())
                .param("access_token", user.getSession().getAccessToken())
                .param("property", "change_fullname")
                .param("value", newFullname))
                .andExpect(status().isOk());

    }
}