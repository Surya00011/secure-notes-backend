package com.notes.securenotesapp.repository;

import com.notes.securenotesapp.entity.Note;
import com.notes.securenotesapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotesRepository extends JpaRepository<Note, Long> {
    Optional<List<Note>> findByUser(User user);
}