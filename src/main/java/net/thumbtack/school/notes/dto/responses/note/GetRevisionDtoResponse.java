package net.thumbtack.school.notes.dto.responses.note;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetRevisionDtoResponse {
    private Integer id;
    private String body;
    private String created;
    private List<GetCommentDtoResponse> comments;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetRevisionDtoResponse that = (GetRevisionDtoResponse) o;
        return Objects.equals(id, that.id) && Objects.equals(body, that.body) && Objects.equals(comments, that.comments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, body, comments);
    }
}
