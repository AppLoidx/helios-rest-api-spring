package com.apploidxxx.heliosrestapispring.entity.commentary;

import com.apploidxxx.heliosrestapispring.api.testutil.UserBuilder;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Arthur Kupriyanov
 */
public class CommentaryTest {

    @Test
    public void default_comment_type(){
        Commentary commentary = new Commentary(new User(), new  User(), "some text");

        assertEquals(CommentaryType.PRIVATE, commentary.getCommentaryType());
    }

    @Test
    public void comment_init(){

        User author = UserBuilder.createUser().withName("author").build();
        User target = UserBuilder.createUser().withName("target").build();
        CommentaryType type = CommentaryType.PUBLIC;
        String text = "g'pawienjgpqi2nj3gp2";

        Commentary commentary = new Commentary(target, author, text, type);

        assertEquals(author, commentary.getAuthor());
        assertEquals(target, commentary.getTarget());
        assertEquals(CommentaryType.PUBLIC, commentary.getCommentaryType());
        assertEquals(text, commentary.getText());
    }

}