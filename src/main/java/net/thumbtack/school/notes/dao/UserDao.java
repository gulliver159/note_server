package net.thumbtack.school.notes.dao;

import net.thumbtack.school.notes.endpoint.request_param.IncludeType;
import net.thumbtack.school.notes.endpoint.request_param.SearchParams;
import net.thumbtack.school.notes.endpoint.request_param.SortOrder;
import net.thumbtack.school.notes.exception.ServerException;
import net.thumbtack.school.notes.model.Session;
import net.thumbtack.school.notes.model.User;
import net.thumbtack.school.notes.views.SessionView;
import net.thumbtack.school.notes.views.UserView;

import java.time.LocalDateTime;
import java.util.List;

public interface UserDao {

    void registerUser(User user, String sessionId) throws ServerException;

    Session getSession(String sessionId);

    SessionView getSessionView(String sessionId, SortOrder sortByRating, int from, Integer count);

    User getUserByLoginAndPassword(String login, String password);

    User getUserByLogin(String login);

    User getUserById(int id);

    void updateProfile(int id, String firstName, String lastName, String patronymic, String newPassword);

    void deleteUser(int id);

    void transferToSuperuser(int id);

    void login(Session session);

    void logout(int id);

    void updateSession(int userId, LocalDateTime currentTime);

    void addToFollowing(int userId, int followerId);

    void addToIgnore(int userId, int ignoredId);

    void deleteFromFollowing(int userId, int followerId);

    void deleteFromIgnore(int userId, int ignoredId);

    List<UserView> getAllUsers(SortOrder sortByRating, int from, Integer count);

    List<UserView> getUsersWithHighOrLowRating(SearchParams type, SortOrder sortByRating, int from, Integer count);

    List<UserView> getDeletedUsers(SortOrder sortByRating, int from, Integer count);

    List<UserView> getSuperUsers(SortOrder sortByRating, int from, Integer count);

    List<Integer> getIdUsers(int userId, IncludeType include);
}