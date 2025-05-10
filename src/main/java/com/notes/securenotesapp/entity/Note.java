package com.notes.securenotesapp.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "notes") // PostgreSQL prefers lowercase table names
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "note_seq")
    @SequenceGenerator(name = "note_seq", sequenceName = "note_seq", allocationSize = 1)
    @Column(name = "note_id")
    private Long noteId;

    @NotBlank(message = "Note title cannot be blank")
    @Column(name = "note_title", columnDefinition = "TEXT")
    private String noteTitle;

    @NotBlank(message = "Note cannot be blank")
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "created")
    private LocalDateTime created;

    @Column(name = "deadline")
    private LocalDate deadline;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;
}
