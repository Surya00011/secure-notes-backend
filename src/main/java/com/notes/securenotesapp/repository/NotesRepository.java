package com.notes.securenotesapp.repository;

import com.notes.securenotesapp.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotesRepository extends JpaRepository<Note, Long> {
}