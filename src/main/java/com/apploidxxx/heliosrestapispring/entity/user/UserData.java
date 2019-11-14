package com.apploidxxx.heliosrestapispring.entity.user;


import com.apploidxxx.heliosrestapispring.entity.note.Note;
import com.apploidxxx.heliosrestapispring.entity.note.NoteType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Данные о заметких записанных на пользователя
 *
 * Имеет отношение One-to-One с {@link User}
 *
 * @author Arthur Kupriyanov
 */
@Entity
@NoArgsConstructor
@Getter
@Setter
public class UserData implements Serializable {
    public UserData(User user){
        this.user = user;
        notes = new HashSet<>();
        writtenNotes = new HashSet<>();
    }

    @Id
    @GeneratedValue
    Long id;

    @OneToOne
    private User user;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "target")
    private Set<Note> notes;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "author")
    private Set<Note> writtenNotes;

    /**
     * Добавить запись на пользователя
     * @param note добавляемая запись
     */
    public void addNote(Note note){
        notes.add(note);
    }

    /**
     * Удалить указанную запись на пользователя
     * @param note удаляемая запись
     */
    public void removeNote(Note note){
        notes.remove(note);
    }

    /**
     * Добавить исходящую запись
     * @param note запись которую записал пользователь
     */
    public void addWrittenNote(Note note) {writtenNotes.add(note);}

    /**
     * УДалить исходящую запись
     * @param note запись которую необходимо удалить
     */
    public void removeWrittenNote(Note note) {writtenNotes.remove(note);}

    /**
     * @return публичные записи на пользователя
     */
    public Set<Note> getPublicNotes(){
        return notes.stream().filter(n -> n.getType() == NoteType.PUBLIC).collect(Collectors.toSet());
    }

    /**
     *
     * @return приватные записи на этого пользователя
     */
    public Set<Note> getPrivateNotes(){
        return notes.stream().filter(n -> n.getType() == NoteType.PRIVATE).collect(Collectors.toSet());
    }

}
