package com.apploidxxx.heliosrestapispring.api.model;

import com.apploidxxx.heliosrestapispring.entity.note.Note;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * Список заметок.
 *
 * @see NoteModel
 * @author Arthur Kupriyanov
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class UserNotes implements Serializable {

    private Set<NoteModel> notes;
    public UserNotes(Set<Note> notes){
        this.notes = notes.stream().map(NoteModel::getModel).collect(Collectors.toSet());
    }
}
