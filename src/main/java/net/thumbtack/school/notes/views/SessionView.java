package net.thumbtack.school.notes.views;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class SessionView {
    private int id;
    private String sessionId;
    private LocalDateTime timeLogin;
    private UserView user;
}
