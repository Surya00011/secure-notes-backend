package com.notes.securenotesapp.service;

import com.notes.securenotesapp.entity.Note;
import com.notes.securenotesapp.entity.User;
import com.notes.securenotesapp.exception.NoteNotFoundException;
import com.notes.securenotesapp.repository.NotesRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class NoteService {
    private final NotesRepository notesRepository;
    public NoteService(NotesRepository notesRepository) {
        this.notesRepository = notesRepository;
    }

    public Note saveNote(Note note) {
        return notesRepository.save(note);
    }

    public List<Note> getNotes(User user) {
        Optional<List<Note>> retrivedNotes = notesRepository.findByUser(user);
        if(retrivedNotes.isEmpty()) {
            throw new NoteNotFoundException("No Notes Added yet");
        }
        return  retrivedNotes.get();
    }
}
