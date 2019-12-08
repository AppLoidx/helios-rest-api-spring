package com.apploidxxx.heliosrestapispring.entity.access.repository.commentaries;

import com.apploidxxx.heliosrestapispring.entity.commentary.Commentary;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Arthur Kupriyanov
 */
public interface CommentaryRepository extends JpaRepository<Commentary, Long> {
    Commentary findByTarget(User target);
    Commentary findByAuthor(User author);

}
