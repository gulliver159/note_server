package net.thumbtack.school.notes.dto.requests.note;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCommentDtoRequest {

    @NotNull(message = "The note body cannot be null")
    @Size(min = 1, message = "The note body cannot be empty")
    private String body;

    @NotNull(message = "The note id cannot be null")
    private Integer noteId;
}
