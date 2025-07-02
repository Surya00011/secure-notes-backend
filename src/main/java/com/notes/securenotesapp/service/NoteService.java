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
    private final EncryptionUtil encryptionUtil;

    public NoteService(NotesRepository notesRepository, EncryptionUtil encryptionUtil) {
        this.notesRepository = notesRepository;
        this.encryptionUtil = encryptionUtil;
    }

    public Note saveNote(NoteRequest noteRequest, User user) {
        try {
            Note note = new Note();
            note.setCreated(LocalDateTime.now());
            note.setDeadline(noteRequest.getDeadline());
            note.setNoteTitle(encryptionUtil.encrypt(noteRequest.getTitle()));
            note.setNote(encryptionUtil.encrypt(noteRequest.getNote()));
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
                note.setNoteTitle(encryptionUtil.decrypt(note.getNoteTitle()));
                note.setNote(encryptionUtil.decrypt(note.getNote()));
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
            note.setNoteTitle(encryptionUtil.decrypt(note.getNoteTitle()));
            note.setNote(encryptionUtil.decrypt(note.getNote()));
            return note;
        } catch (Exception e) {
            throw new DecreptionFailedException("Decryption failed for the note: " + e.getMessage());
        }
    }

    public boolean updateNote(NoteRequest noteRequest, Long id, User user) {
        Optional<Note> optionalNote = notesRepository.findById(id);
        if (optionalNote.isEmpty()) {
            throw new NoteNotFoundException("Note not found");
        }

        Note existingNote = optionalNote.get();
        boolean isUpdated = false;

        try {
            if (noteRequest.getNote() != null && !noteRequest.getNote().isBlank()) {
                existingNote.setNote(encryptionUtil.encrypt(noteRequest.getNote()));
                isUpdated = true;
            }
            if (noteRequest.getDeadline() != null) {
                existingNote.setDeadline(noteRequest.getDeadline());
                isUpdated = true;
            }
            if (noteRequest.getTitle() != null && !noteRequest.getTitle().isBlank()) {
                existingNote.setNoteTitle(encryptionUtil.encrypt(noteRequest.getTitle()));
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