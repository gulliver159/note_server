package net.thumbtack.school.notes.dto.responses.note;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetNoteInfoDtoResponse {
    private int id;
    private String subject;
    private String body;
    private int sectionId;
    private int authorId;
    private String created;
    private int revisionId;
}
