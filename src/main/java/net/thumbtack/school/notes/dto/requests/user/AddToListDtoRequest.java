package net.thumbtack.school.notes.dto.requests.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddToListDtoRequest {
    @NotNull(message = "Login cannot be null")
    private String login;
}
