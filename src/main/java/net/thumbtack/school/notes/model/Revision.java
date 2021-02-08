package net.thumbtack.school.notes.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Revision {
    private int id;
    private String body;
    private int revisionIdForNote;

    private Note note;

    private List<Comment> comments;

    public Revision(String body) {
        this.body = body;
    }

    public Revision(int id, String body) {
        this.id = id;
        this.body = body;
    }
}
