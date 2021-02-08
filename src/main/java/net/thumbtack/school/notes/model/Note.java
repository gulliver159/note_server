package net.thumbtack.school.notes.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class Note {
    private int id;
    private String subject;
    private double rating;
    private LocalDateTime timeCreated;

    private User owner;
    private Section section;

    private List<Revision> revisions;


    public Note(String subject, LocalDateTime timeCreated) {
        this.subject = subject;
        this.timeCreated = timeCreated;
    }
}
