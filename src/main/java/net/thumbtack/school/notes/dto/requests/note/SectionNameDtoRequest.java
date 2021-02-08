package net.thumbtack.school.notes.dto.requests.note;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SectionNameDtoRequest {

    @NotNull(message = "The section name cannot be null")
    @Size(min = 1, message = "The section name cannot be empty")
    @Pattern(regexp = "[A-Za-zА-я-\\d_ ]+", message = "The section name can only contain Latin and Russian letters, " +
            "numbers, spaces, and underscores")
    private String name;
}
