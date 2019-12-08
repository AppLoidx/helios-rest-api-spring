package com.apploidxxx.heliosrestapispring.entity.commentary;

import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

/**
 * @author Arthur Kupriyanov
 */
@Slf4j
@Data
@NoArgsConstructor
@Entity
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Commentary implements Comparable<Commentary>{
    {
        this.creationDate = new Date();
    }
    public Commentary(User target, User author, String text){
        this(target, author, text, CommentaryType.PRIVATE);
    }

    public Commentary(User target, User author, String text, CommentaryType commentaryType){

        this.target = target;
        this.text = text;

        if (commentaryType == null){
            commentaryType = CommentaryType.PRIVATE;
            log.warn("CommentaryType undefined - selected default value private");
        }
        this.commentaryType = commentaryType;
        this.author = author;
    }

    @Id
    @GeneratedValue
    Long id;

    @ManyToOne(cascade = CascadeType.PERSIST)
    private User target;

    @ManyToOne(cascade = CascadeType.PERSIST)
    private User author;

    private Date creationDate;

    private String text;
    private CommentaryType commentaryType;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Commentary that = (Commentary) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(Commentary o) {
        return (int) (o.creationDate.getTime() - this.creationDate.getTime());
    }
}
