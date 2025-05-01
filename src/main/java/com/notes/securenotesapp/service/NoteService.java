package com.notes.securenotesapp.service;

import com.notes.securenotesapp.dto.NoteRequest;
import com.notes.securenotesapp.entity.Note;
import com.notes.securenotesapp.entity.User;
import com.notes.securenotesapp.exception.DecreptionFailedException;
import com.notes.securenotesapp.exception.EncryptionFailedException;
import com.notes.securenotesapp.exception.NoteNotFoundException;
import com.notes.securenotesapp.repository.NotesRepository;
import com.notes.securenotesapp.utils.EncryptionUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NoteService {
    private final NotesRepository notesRepository;

    public NoteService(NotesRepository notesRepository) {
        this.notesRepository = notesRepository;
    }

    public Note saveNote(NoteRequest noteRequest,User user) {
        try {
            Note note = new Note();
            note.setCreated(LocalDateTime.now());
            note.setDeadline(noteRequest.getDeadline());
            note.setNoteTitle(EncryptionUtil.encrypt(noteRequest.getTitle()));
            note.setNote(EncryptionUtil.encrypt(noteRequest.getNote()));
            note.setUser(user);
            return notesRepository.save(note);
        } catch (Exception e) {
            throw new EncryptionFailedException("Encryption failed while saving note: " + e.getMessage());
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
                throw new DecreptionFailedException("Decryption failed for a note: " + e.getMessage());
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
            throw new DecreptionFailedException("Decryption failed for the note: " + e.getMessage());
        }
    }

    public boolean updateNote(NoteRequest noteRequest, Long id, User user) {
        Note existingNote = null;


        Optional<Note> optionalNote = notesRepository.findById(id);
        if (optionalNote.isEmpty()) {
            throw new NoteNotFoundException("Note not found");
        }

        existingNote = optionalNote.get();
        boolean isUpdated = false;


        try {
            if (noteRequest.getNote() != null && !noteRequest.getNote().isBlank()) {
                existingNote.setNote(EncryptionUtil.encrypt(noteRequest.getNote()));
                isUpdated = true;
            }
            if (noteRequest.getDeadline() != null) {
                existingNote.setDeadline(noteRequest.getDeadline());
                isUpdated = true;
            }
            if (noteRequest.getTitle() != null && !noteRequest.getTitle().isBlank()) {
                existingNote.setNoteTitle(EncryptionUtil.encrypt(noteRequest.getTitle()));
                isUpdated = true;
            }
            existingNote.setUser(user);
            notesRepository.save(existingNote);
            return isUpdated;

        } catch (Exception e) {
            throw new EncryptionFailedException("Encryption failed while updating note: " + e.getMessage());
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
