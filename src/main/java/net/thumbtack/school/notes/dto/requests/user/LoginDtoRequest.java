package net.thumbtack.school.notes.dto.requests.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.thumbtack.school.notes.dto.requests.validator.NameSize;
import net.thumbtack.school.notes.dto.requests.validator.PasswordSize;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginDtoRequest {

    @NotNull(message = "Login cannot be null")
    @NameSize(message = "The login of the wrong size")
    @Pattern(regexp = "[A-Za-zА-я0-9]+", message = "The login can only contain Latin and Russian letters and numbers")
    private String login;

    @NotNull(message = "Password cannot be null")
    @PasswordSize
    private String password;
}
