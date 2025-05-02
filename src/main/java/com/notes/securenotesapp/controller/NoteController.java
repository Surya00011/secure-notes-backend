package com.notes.securenotesapp.controller;

import com.notes.securenotesapp.dto.NoteRequest;
import com.notes.securenotesapp.entity.Note;
import com.notes.securenotesapp.entity.User;
import com.notes.securenotesapp.exception.NoteNotFoundException;
import com.notes.securenotesapp.exception.UserNotFoundException;
import com.notes.securenotesapp.service.NoteService;
import com.notes.securenotesapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "Notes", description = "Operations related to user notes")
@SecurityRequirement(name = "bearerAuth") // Secured with JWT token
public class NoteController {

    private final NoteService noteService;
    private final UserService userService;

    public NoteController(NoteService noteService, UserService userService) {
        this.noteService = noteService;
        this.userService = userService;
    }

    @Operation(
            summary = "Add or update a note",
            description = "Saves a new note if it doesn't exist or updates it if already present for the authenticated user"
    )
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

    @Operation(
            summary = "Get all notes",
            description = "Returns all notes belonging to the authenticated user"
    )
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

    @Operation(
            summary = "Update note by ID",
            description = "Updates the content or title of a specific note belonging to the authenticated user"
    )
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

    @Operation(
            summary = "Delete note by ID",
            description = "Deletes a specific note if it exists and belongs to the authenticated user"
    )
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
