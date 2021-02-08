package net.thumbtack.school.notes.dto.requests.note;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.thumbtack.school.notes.dto.requests.validator.NotAllNull;

import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@NotAllNull
public class EditOrTransferNoteDtoRequest {

    @Size(min = 1, message = "The note body cannot be empty")
    private String body;

    private Integer sectionId;
}
