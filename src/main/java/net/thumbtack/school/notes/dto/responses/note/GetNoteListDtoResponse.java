package net.thumbtack.school.notes.dto.responses.note;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetNoteListDtoResponse {
    private int id;
    private String subject;
    private String body;
    private int sectionId;
    private int authorId;
    private String created;
    private List<GetRevisionDtoResponse> revisions;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetNoteListDtoResponse that = (GetNoteListDtoResponse) o;
        return sectionId == that.sectionId && authorId == that.authorId && Objects.equals(subject, that.subject) && Objects.equals(body, that.body) && Objects.equals(revisions, that.revisions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subject, body, sectionId, authorId, revisions);
    }
}
