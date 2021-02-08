// REVU net.thumbtack.school.notes.dto.responses.note.notelist;
// а вообще-то вполне можно отправить в net.thumbtack.school.notes.dto.responses.note
package net.thumbtack.school.notes.dto.responses.note;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetCommentDtoResponse {
    private int id;
    private String body;
    private int authorId;
    private Integer revisionId;
    private String created;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetCommentDtoResponse that = (GetCommentDtoResponse) o;
        return authorId == that.authorId && Objects.equals(body, that.body) && Objects.equals(revisionId, that.revisionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(body, authorId, revisionId);
    }
}
