package net.thumbtack.school.notes.dto.requests.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.thumbtack.school.notes.dto.requests.validator.PasswordSize;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteUserDtoRequest {
    @NotNull(message = "Password cannot be null")
    @PasswordSize
    private String password;
}
