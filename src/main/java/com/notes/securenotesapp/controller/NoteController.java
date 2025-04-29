package com.notes.securenotesapp.controller;

import com.notes.securenotesapp.dto.NoteRequest;
import com.notes.securenotesapp.entity.Note;
import com.notes.securenotesapp.entity.User;
import com.notes.securenotesapp.exception.UserNotFoundException;
import com.notes.securenotesapp.service.NoteService;
import com.notes.securenotesapp.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class NoteController {

    private final NoteService noteService;
    private final UserService userService;

    public NoteController(NoteService noteService, UserService userService) {
        this.noteService = noteService;
        this.userService = userService;
    }

    @PostMapping("/add-notes")
    public ResponseEntity<Map<String,String>> addNote(@RequestBody NoteRequest noteRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User retrivedUser = userService.findUserByEmail(email);
        Map<String,String> response = new HashMap<>();
        if(retrivedUser == null) {
            throw new UserNotFoundException("User not found");
        }
        Note newNote = new Note();
        newNote.setNote(noteRequest.getNote());
        newNote.setCreated(LocalDateTime.now());
        newNote.setDeadline(noteRequest.getDeadline());
        newNote.setUser(retrivedUser);
        Note savedNote = noteService.saveNote(newNote);
        if(savedNote == null) {
            response.put("message", "Note saved");
        }else {
            response.put("message", "Note updated");
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/view-notes")
    public ResponseEntity<List<Note>> getAllNotes() {
         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
         String email = auth.getName();
         User retrivedUser = userService.findUserByEmail(email);
         if(retrivedUser == null) {
             throw new UserNotFoundException("User not found");
         }
         List<Note> myNotes = noteService.getNotes(retrivedUser);
         return ResponseEntity.ok(myNotes);
    }
}
