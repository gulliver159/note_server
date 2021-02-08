package net.thumbtack.school.notes.views;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.thumbtack.school.notes.model.Comment;
import net.thumbtack.school.notes.model.Note;
import net.thumbtack.school.notes.model.Section;
import net.thumbtack.school.notes.model.UserType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@NoArgsConstructor
public class UserView {
    private int id;
    private String firstName;
    private String lastName;
    private String patronymic;

    private String login;
    private String password;
    private double rating;
    private UserType userType;
    private boolean online;
    private boolean deleted;
    private LocalDateTime timeRegistered;

    private List<Section> sections = new ArrayList<>();
    private List<Note> notes = new ArrayList<>();
    private List<Comment> comments = new ArrayList<>();

    private List<UserView> followings = new ArrayList<>();
    private List<UserView> followers = new ArrayList<>();

    private List<UserView> ignore = new ArrayList<>();
    private List<UserView> ignoreBy = new ArrayList<>();

    public UserView(int id, String firstName, String lastName, String patronymic, String login, String password, double rating,
                UserType userType, boolean deleted, LocalDateTime timeRegistered) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.patronymic = patronymic;
        this.login = login;
        this.password = password;
        this.userType = userType;
        this.deleted = deleted;
        this.timeRegistered = timeRegistered;
        this.rating = rating;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserView userView = (UserView) o;
        return rating == userView.rating && online == userView.online && deleted == userView.deleted &&
                Objects.equals(firstName, userView.firstName) && Objects.equals(lastName, userView.lastName) &&
                Objects.equals(patronymic, userView.patronymic) && Objects.equals(login, userView.login) &&
                Objects.equals(password, userView.password) && userType == userView.userType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, patronymic, login, password, rating, userType, online, deleted);
    }
}
