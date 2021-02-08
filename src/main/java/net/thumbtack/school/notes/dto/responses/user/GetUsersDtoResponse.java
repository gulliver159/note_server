package net.thumbtack.school.notes.dto.responses.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetUsersDtoResponse {
    private int id;
    private String firstName;
    private String lastName;
    private String patronymic;
    private String login;

    private String timeRegistered;
    private boolean online;
    private boolean deleted;
    private Boolean superV; // как мне быть с этим полем не понятно
    private double rating;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetUsersDtoResponse that = (GetUsersDtoResponse) o;
        return online == that.online && deleted == that.deleted && rating == that.rating && Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(patronymic, that.patronymic) && Objects.equals(login, that.login) && Objects.equals(superV, that.superV);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, patronymic, login, online, deleted, superV, rating);
    }
}
