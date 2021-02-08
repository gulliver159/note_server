package net.thumbtack.school.notes.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Session {
    private int id;
    private String sessionId;
    private LocalDateTime timeLogin;
    private User user;

    public Session(String sessionId, LocalDateTime timeLogin) {
        id = 0;
        this.sessionId = sessionId;
        this.timeLogin = timeLogin;
    }

    public Session(String sessionId, LocalDateTime timeLogin, User user) {
        id = 0;
        this.sessionId = sessionId;
        this.timeLogin = timeLogin;
        this.user = user;
    }
}

