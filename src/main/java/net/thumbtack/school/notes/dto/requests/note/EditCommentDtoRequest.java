package net.thumbtack.school.notes.dto.requests.note;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.thumbtack.school.notes.dto.requests.validator.NotAllNull;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditCommentDtoRequest {

    @Size(min = 1, message = "The comment body cannot be empty")
    @NotNull(message = "The comment body cannot be null")
    private String body;
}
