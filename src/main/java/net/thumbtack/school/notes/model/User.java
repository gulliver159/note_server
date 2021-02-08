package net.thumbtack.school.notes.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@NoArgsConstructor
public class User {
    private int id;
    private String firstName;
    private String lastName;
    private String patronymic;

    private String login;
    private String password;
    private UserType userType;
    private boolean deleted;
    private LocalDateTime timeRegistered;

    private List<Section> sections = new ArrayList<>();
    private List<Note> notes = new ArrayList<>();
    private List<Comment> comments = new ArrayList<>();

    private List<User> followings = new ArrayList<>();
    private List<User> followers = new ArrayList<>();

    private List<User> ignore = new ArrayList<>();
    private List<User> ignoreBy = new ArrayList<>();

    public User(int id, String firstName, String lastName, String patronymic, String login, String password,
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
    }

    public User(String firstName, String lastName, String patronymic, String login, String password) {
        this(0, firstName, lastName, patronymic, login, password, UserType.USER,
                false, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return deleted == user.deleted && Objects.equals(firstName, user.firstName) &&
                Objects.equals(lastName, user.lastName) && Objects.equals(patronymic, user.patronymic) &&
                Objects.equals(login, user.login) && Objects.equals(password, user.password) && userType == user.userType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, patronymic, login, password, userType, deleted);
    }
}
