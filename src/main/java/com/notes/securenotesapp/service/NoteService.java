package com.notes.securenotesapp.service;

import com.notes.securenotesapp.entity.Note;
import com.notes.securenotesapp.entity.User;
import com.notes.securenotesapp.exception.NoteNotFoundException;
import com.notes.securenotesapp.repository.NotesRepository;
import com.notes.securenotesapp.utils.EncryptionUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NoteService {
    private final NotesRepository notesRepository;

    public NoteService(NotesRepository notesRepository) {
        this.notesRepository = notesRepository;
    }

    public Note saveNote(Note note) {
        try {
            note.setNoteTitle(EncryptionUtil.encrypt(note.getNoteTitle()));
            note.setNote(EncryptionUtil.encrypt(note.getNote()));
            return notesRepository.save(note);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed while saving note: " + e.getMessage());
        }
    }

    public List<Note> getNotes(User user) {
        Optional<List<Note>> retrievedNotes = notesRepository.findByUser(user);
        if (retrievedNotes.isEmpty()) {
            throw new NoteNotFoundException("No Notes Added yet");
        }

        List<Note> notes = retrievedNotes.get();
        for (Note note : notes) {
            try {
                note.setNoteTitle(EncryptionUtil.decrypt(note.getNoteTitle()));
                note.setNote(EncryptionUtil.decrypt(note.getNote()));
            } catch (Exception e) {
                throw new RuntimeException("Decryption failed for a note: " + e.getMessage());
            }
        }
        return notes;
    }

    public Note getNote(Long id) {
        Optional<Note> retrievedNote = notesRepository.findById(id);
        if (retrievedNote.isEmpty()) {
            throw new NoteNotFoundException("No Note Found");
        }

        Note note = retrievedNote.get();
        try {
            note.setNoteTitle(EncryptionUtil.decrypt(note.getNoteTitle()));
            note.setNote(EncryptionUtil.decrypt(note.getNote()));
            return note;
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed for the note: " + e.getMessage());
        }
    }

    public void deleteNote(Long id) {
        Optional<Note> retrievedNote = notesRepository.findById(id);
        if (retrievedNote.isEmpty()) {
            throw new NoteNotFoundException("No Note Found");
        }
        notesRepository.deleteById(id);
    }
}
