package net.thumbtack.school.notes.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Comment {
    private int id;
    private String body;
    private LocalDateTime timeCreated;

    private Revision revision;
    private User owner;

    public Comment(String body, LocalDateTime timeCreated) {
        this.body = body;
        this.timeCreated = timeCreated;
    }
}
