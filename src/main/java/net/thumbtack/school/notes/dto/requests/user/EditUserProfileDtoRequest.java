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
public class EditUserProfileDtoRequest {

    @NotNull(message = "First name cannot be null")
    @NameSize(message = "The first name of the wrong size")
    @Pattern(regexp = "[A-Za-zА-я- ]+", message = "The first name can only contain Latin and Russian letters, spaces, " +
            "and a minus sign")
    private String firstName;

    @NotNull(message = "Last name cannot be null")
    @NameSize(message = "The last name of the wrong size")
    @Pattern(regexp = "[A-Za-zА-я- ]+", message = "The last name can only contain Latin and Russian letters, spaces, " +
            "and a minus sign")
    private String lastName;

    @NameSize(message = "The patronymic of the wrong size")
    @Pattern(regexp = "[A-Za-zА-я- ]+", message = "The patronymic can only contain Latin and Russian letters, spaces, " +
            "and a minus sign")
    private String patronymic;

    @NotNull(message = "Old password cannot be null")
    @PasswordSize(message = "The old password of the wrong size")
    private String oldPassword;

    @NotNull(message = "New password cannot be null")
    @PasswordSize(message = "The new password of the wrong size")
    private String newPassword;
}
