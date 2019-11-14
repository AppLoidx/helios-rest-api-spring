package com.apploidxxx.heliosrestapispring.api.model;


import com.apploidxxx.heliosrestapispring.entity.note.Note;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.NoArgsConstructor;


/**
 *
 * POJO объект для заметок преподавателей
 *
 * @author Arthur Kupriyanov
 */
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class NoteModel {

    public Long noteId;
    public String author;
    public String target;
    public String content;

    public NoteModel(Note note){
        this.author = note.getAuthor().getUser().getUsername();
        this.target = note.getTarget().getUser().getUsername();
        this.content = note.getContent();
        this.noteId = note.getId();
    }

    public static NoteModel getModel(Note note){
        return new NoteModel(note);
    }

}
