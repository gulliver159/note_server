package net.thumbtack.school.notes.daoimpl;

import lombok.extern.slf4j.Slf4j;
import net.thumbtack.school.notes.dao.UserDao;
import net.thumbtack.school.notes.endpoint.request_param.IncludeType;
import net.thumbtack.school.notes.endpoint.request_param.SearchParams;
import net.thumbtack.school.notes.endpoint.request_param.SortOrder;
import net.thumbtack.school.notes.exception.ServerErrorCode;
import net.thumbtack.school.notes.exception.ServerException;
import net.thumbtack.school.notes.mapper.UserMapper;
import net.thumbtack.school.notes.model.Session;
import net.thumbtack.school.notes.model.User;
import net.thumbtack.school.notes.views.SessionView;
import net.thumbtack.school.notes.views.UserView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class UserDaoImpl implements UserDao {

    private final UserMapper userMapper;

    @Autowired
    public UserDaoImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public void registerUser(User user, String sessionId) throws ServerException {
        log.debug("Dao insert User {}", user);
        try {
            userMapper.insertUser(user);
            userMapper.insertSession(new Session(sessionId, user.getTimeRegistered(), user));
        } catch (DuplicateKeyException ex) {
            log.info("Cannot insert user, because supplied login already busy");
            throw new ServerException(ServerErrorCode.LOGIN_ALREADY_BUSY);
        }
    }

    public void login(Session session) {
        log.debug("Dao insert session {}", session);
        userMapper.deleteSession(session.getUser().getId());
        userMapper.insertSession(session);
    }

    public void logout(int id) {
        log.debug("Dao delete Session by userId {}", id);
        userMapper.deleteSession(id);
    }

    public void updateSession(int userId, LocalDateTime currentTime) {
        log.debug("Dao update Session by userId {}", userId);
        userMapper.updateSession(userId, currentTime);
    }

    public Session getSession(String sessionId) {
        log.debug("Dao get Session by sessionId {}", sessionId);
        return userMapper.getSession(sessionId);
    }

    public SessionView getSessionView(String sessionId, SortOrder sortByRating, int from, Integer count) {
        log.debug("Dao get SessionView by sessionId {}", sessionId);
        return userMapper.getSessionView(sessionId, sortByRating, from, count);
    }

    public User getUserByLoginAndPassword(String login, String password) {
        log.debug("Dao get User by login {} and password {}", login, password);
        return userMapper.getUserByLoginAndPassword(login, password);
    }

    public User getUserByLogin(String login) {
        log.debug("Dao get User by login {}", login);
        return userMapper.getUserByLogin(login);
    }

    public User getUserById(int id) {
        log.debug("Dao get User by id {}", id);
        return userMapper.getUserById(id);
    }

    public void updateProfile(int id, String firstName, String lastName, String patronymic, String newPassword) {
        log.debug("Dao update User by id {}", id);
        userMapper.updateProfile(id, firstName, lastName, patronymic, newPassword);
    }

    public void deleteUser(int id) {
        log.debug("Dao delete User by id {}", id);
        userMapper.deleteSession(id);
        userMapper.deleteUser(id);
    }

    public void transferToSuperuser(int id) {
        log.debug("Dao transfer to superuser User by id {}", id);
        userMapper.transferToSuperuser(id);
    }

    public void addToFollowing(int userId, int followingId) {
        log.debug("Dao add to following User by id {} to User by id {}", followingId, userId);
        userMapper.insertToFollowing(userId, followingId);
    }

    public void addToIgnore(int userId, int ignoredId) {
        log.debug("Dao add to ignore User by id {} to User by id {}", ignoredId, userId);
        userMapper.insertToIgnore(userId, ignoredId);
    }

    public void deleteFromFollowing(int userId, int followingId) {
        log.debug("Dao delete from following User by id {} to User by id {}", followingId, userId);
        userMapper.deleteFromFollowing(userId, followingId);
    }

    public void deleteFromIgnore(int userId, int ignoredId) {
        log.debug("Dao delete from ignore User by id {} to User by id {}", ignoredId, userId);
        userMapper.deleteFromIgnore(userId, ignoredId);
    }


    public List<UserView> getAllUsers(SortOrder sortByRating, int from, Integer count) {
        log.debug("Dao get all users");
        return userMapper.getAllUsers(sortByRating, from, count);
    }

    public List<UserView> getUsersWithHighOrLowRating(SearchParams type, SortOrder sortByRating, int from, Integer count) {
        log.debug("Dao get users with high rating");
        return userMapper.getUsersWithHighOrLowRating(type, sortByRating, from, count);
    }

    public List<UserView> getDeletedUsers(SortOrder sortByRating, int from, Integer count) {
        log.debug("Dao get deleted users");
        return userMapper.getDeletedUsers(sortByRating, from, count);
    }

    public List<UserView> getSuperUsers(SortOrder sortByRating, int from, Integer count) {
        log.debug("Dao get superusers");
        return userMapper.getSuperUsers(sortByRating, from, count);
    }

    public List<Integer> getIdUsers(int userId, IncludeType include) {
        log.debug("Dao get id users");
        return userMapper.getIdUsers(userId, include);
    }
}