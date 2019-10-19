package com.apploidxxx.heliosrestapispring.api;


import com.apploidxxx.heliosrestapispring.api.model.ErrorMessage;
import com.apploidxxx.heliosrestapispring.api.model.NoteModel;
import com.apploidxxx.heliosrestapispring.api.model.UserNotes;
import com.apploidxxx.heliosrestapispring.api.util.ErrorResponseFactory;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.apploidxxx.heliosrestapispring.entity.user.UserType;
import com.apploidxxx.heliosrestapispring.entity.access.repository.SessionRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.UserRepository;
import com.apploidxxx.heliosrestapispring.entity.access.repository.note.NoteRepository;
import com.apploidxxx.heliosrestapispring.entity.note.Note;
import com.apploidxxx.heliosrestapispring.entity.note.NoteType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

import java.util.Optional;

/**
 * @author Arthur Kupriyanov
 */
@Controller
@RequestMapping(value = "/api/user/note", produces = "application/json")
public class UserNoteApi {
    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final SessionRepository sessionRepository;

    public UserNoteApi(UserRepository userRepository, NoteRepository noteRepository, SessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.noteRepository = noteRepository;
        this.sessionRepository = sessionRepository;
    }

    @DeleteMapping
    public @ResponseBody ErrorMessage deleteNote(
            HttpServletResponse response,
            @RequestParam("note_id") Long noteId,
            @RequestParam("access_token") String accessToken){
        Note note;
        User user;
        Optional<Note> optNote = this.noteRepository.findById(noteId);

        if (!optNote.isPresent()){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return new ErrorMessage("note_not_found", "Note is not found. Invalid note id");
        } else {
            note = optNote.get();
        }

        if ( (user = this.sessionRepository.findByAccessToken(accessToken).getUser()) == null ){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return ErrorResponseFactory.getInvalidParamErrorResponse("invalid token", response);
        }

        // TODO: rewrite to equal for TEACHER
        if (user.equals(note.getAuthor().getUser())){

            note.getAuthor().removeWrittenNote(note);
            note.getTarget().removeNote(note);

            this.noteRepository.delete(note);
            return null;
        } else {
            return ErrorResponseFactory.getForbiddenErrorResponse(response);
        }
    }

    @GetMapping
    public @ResponseBody Object getNotes(
            HttpServletResponse response,
            @RequestParam("type") String noteTypeParam,
            @RequestParam("access_token") String accessToken,
            @RequestParam("username") String username){
        NoteType noteType;
        User requestUser;
        User user;

        if ((noteType = NoteType.getNoteType(noteTypeParam)) == NoteType.UNKNOWN)
            return ErrorResponseFactory.getInvalidParamErrorResponse("Invalid type param", response);

        requestUser = this.sessionRepository.findByAccessToken(accessToken).getUser();
        user = this.userRepository.findByUsername(username);

        if (requestUser == null || user == null) {
            return ErrorResponseFactory.getInvalidParamErrorResponse("entity(-ies) not found", response);
        }


        // TODO : maybe add all notes return action?

        if (noteType == NoteType.PRIVATE){
            if (requestUser.getUserType() == UserType.TEACHER){
                return new UserNotes(user.getUserdata().getPrivateNotes());
            } else {
                return ErrorResponseFactory.getForbiddenErrorResponse("Only teacher can read private notes", response);
            }
        } else {
            if (requestUser.getUserType() == UserType.TEACHER || requestUser.equals(user)){
                return new UserNotes(user.getUserdata().getPublicNotes());
            } else {
                return ErrorResponseFactory.getForbiddenErrorResponse("Only student or another teacher can read notes", response);
            }
        }
    }

    @PostMapping
    public @ResponseBody Object createNote(
            HttpServletResponse response,
            @RequestParam("access_token") String accessToken,
            @RequestParam("note_type") String queryType,
            @RequestParam("target") String queryTarget,
            @RequestParam("message") String message){


        NoteType type;

        User user = this.sessionRepository.findByAccessToken(accessToken).getUser();
        User target = this.userRepository.findByUsername(queryTarget);

        if (user == null || target == null) {
            return ErrorResponseFactory.getInvalidParamErrorResponse("entity not found", response);
        }

        if ((type = NoteType.getNoteType(queryType)) == NoteType.UNKNOWN){
            return ErrorResponseFactory.getInvalidParamErrorResponse("Invalid note_type param", response);
        }

        if ("".equals(message)){
            return ErrorResponseFactory.getInvalidParamErrorResponse("Your body is empty", response);
        }

        // TODO: teachers only method
        //if (user.getUserType() == UserType.TEACHER)

        Note note = new Note(user.getUserdata(), target.getUserdata(), type, message);

        user.getUserdata().addWrittenNote(note);
        target.getUserdata().addNote(note);
        this.noteRepository.save(note);
        this.userRepository.save(user);
        this.userRepository.save(target);

        return NoteModel.getModel(note);
    }
}
