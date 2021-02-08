package net.thumbtack.school.notes.dto.responses.note;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetCommentInfoDtoResponse {
    private int id;
    private String body;
    private int noteId;
    private int authorId;
    private int revisionId;
    private String created;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetCommentInfoDtoResponse that = (GetCommentInfoDtoResponse) o;
        return noteId == that.noteId && authorId == that.authorId && revisionId == that.revisionId && Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(body, noteId, authorId, revisionId);
    }
}
