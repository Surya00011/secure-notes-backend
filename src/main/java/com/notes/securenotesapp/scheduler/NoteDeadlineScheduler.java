package com.notes.securenotesapp.scheduler;

import com.notes.securenotesapp.entity.Note;
import com.notes.securenotesapp.entity.User;
import com.notes.securenotesapp.event.DeadLineDayEvent;
import com.notes.securenotesapp.event.PreviousDayRemainderEvent;
import com.notes.securenotesapp.exception.EmailSendException;
import com.notes.securenotesapp.repository.NotesRepository;
import com.notes.securenotesapp.utils.EncryptionUtil;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class NoteDeadlineScheduler {

    private final NotesRepository notesRepository;
    private final ApplicationEventPublisher eventPublisher;

    public NoteDeadlineScheduler(NotesRepository notesRepository, ApplicationEventPublisher eventPublisher) {
        this.notesRepository = notesRepository;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(cron = "0 0 8 * * ?") // runs at 8:00 AM daily
    public void sendDeadlineNotification() {
        try {
            LocalDate today = LocalDate.now();
            List<Note> notesDueToday = notesRepository.findByDeadline(today);

            for (Note note : notesDueToday) {
                User user = note.getUser();
                String email = user.getEmail();
                String username = user.getUsername();
                String noteTitle = EncryptionUtil.decrypt(note.getNoteTitle());
                eventPublisher.publishEvent(new DeadLineDayEvent(email, username, noteTitle));
            }
        } catch (Exception e) {
            throw new EmailSendException("Error sending deadline notification");
        }
    }

    @Scheduled(cron = "0 0 8 * * ?") // runs at 8:00 AM daily
    public void sendRemainder() {
        try {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            List<Note> notesDueTomorrow = notesRepository.findByDeadline(tomorrow);

            for (Note note : notesDueTomorrow) {
                User user = note.getUser();
                String email = user.getEmail();
                String username = user.getUsername();
                String noteTitle = EncryptionUtil.decrypt(note.getNoteTitle());
                eventPublisher.publishEvent(new PreviousDayRemainderEvent(email, username, noteTitle));
            }
        } catch (Exception e) {
            throw new EmailSendException("Error sending reminder for tomorrow's notes");
        }
    }
}
