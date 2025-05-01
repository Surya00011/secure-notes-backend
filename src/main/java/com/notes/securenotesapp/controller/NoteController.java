package com.notes.securenotesapp.controller;

import com.notes.securenotesapp.dto.NoteRequest;
import com.notes.securenotesapp.entity.Note;
import com.notes.securenotesapp.entity.User;
import com.notes.securenotesapp.exception.NoteNotFoundException;
import com.notes.securenotesapp.exception.UserNotFoundException;
import com.notes.securenotesapp.service.NoteService;
import com.notes.securenotesapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class NoteController {

    private final NoteService noteService;
    private final UserService userService;

    public NoteController(NoteService noteService, UserService userService) {
        this.noteService = noteService;
        this.userService = userService;
    }

    @PostMapping("/add-notes")
    public ResponseEntity<Map<String,String>> addNote(@Valid @RequestBody NoteRequest noteRequest){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User retrivedUser = userService.findUserByEmail(email);
        Map<String,String> response = new HashMap<>();
        if(retrivedUser == null) {
            throw new UserNotFoundException("User not found");
        }
        Note savedNote = noteService.saveNote(noteRequest,retrivedUser);
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

    @PutMapping("/update-note/{id}")
    public ResponseEntity<Map<String, String>> updateNote(@RequestBody NoteRequest noteRequest, @PathVariable Long id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User retrivedUser = userService.findUserByEmail(email);

        if (retrivedUser == null) {
            throw new UserNotFoundException("User not found");
        }

        boolean isUpdated = noteService.updateNote(noteRequest, id,retrivedUser);

        Map<String, String> response = new HashMap<>();
        if (!isUpdated) {
            response.put("message", "No valid fields provided to update");
            return ResponseEntity.badRequest().body(response);
        }
        response.put("message", "Note updated successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete-note/{id}")
    public ResponseEntity<Map<String, String>> deleteNote(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User retrievedUser = userService.findUserByEmail(email);

        if (retrievedUser == null) {
            throw new UserNotFoundException("User not found");
        }

        Note existingNote = noteService.getNote(id);
        if (existingNote == null) {
            throw new NoteNotFoundException("Note not found");
        }

        noteService.deleteNote(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Note deleted successfully");
        return ResponseEntity.ok(response);
    }

}
