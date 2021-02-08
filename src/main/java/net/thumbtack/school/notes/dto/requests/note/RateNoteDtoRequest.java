package net.thumbtack.school.notes.dto.requests.note;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RateNoteDtoRequest {

    @NotNull(message = "The rating cannot be null")
    @Min(value = 1, message = "The rating must be at least 1")
    @Max(value = 5, message = "The rating should not be more than 5")
    private Integer rating;
}
