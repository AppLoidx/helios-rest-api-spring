package com.apploidxxx.heliosrestapispring.entity;

import com.apploidxxx.heliosrestapispring.entity.user.User;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Arthur Kupriyanov
 */
public class ChatTest {

    @Test
    public void newMessage() {
        Chat chat = new Chat();
        User user = new User();

        chat.newMessage(user, "text");

        assertEquals( "text", chat.getMessages().iterator().next().getMessage());
        chat.newMessage(user, "text");
        assertEquals(2, chat.getMessages().size());
    }
}