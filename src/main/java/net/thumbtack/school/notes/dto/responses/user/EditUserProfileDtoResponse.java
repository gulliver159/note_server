package net.thumbtack.school.notes.dto.responses.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditUserProfileDtoResponse {
    private int id;
    private String firstName;
    private String lastName;
    private String patronymic;
    private String login;
}
