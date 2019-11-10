package com.apploidxxx.heliosrestapispring.entity.access.repository.note;

import com.apploidxxx.heliosrestapispring.entity.note.Note;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Arthur Kupriyanov
 */
public interface NoteRepository extends JpaRepository<Note, Long> {
}
