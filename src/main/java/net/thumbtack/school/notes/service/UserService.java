package net.thumbtack.school.notes.service;

import lombok.extern.slf4j.Slf4j;
import net.thumbtack.school.notes.dao.UserDao;
import net.thumbtack.school.notes.dto.requests.user.*;
import net.thumbtack.school.notes.dto.responses.user.EditUserProfileDtoResponse;
import net.thumbtack.school.notes.dto.responses.user.GetInfoOfUserDtoResponse;
import net.thumbtack.school.notes.dto.responses.user.GetUsersDtoResponse;
import net.thumbtack.school.notes.dto.responses.user.RegisterUserDtoResponse;
import net.thumbtack.school.notes.endpoint.request_param.SearchParams;
import net.thumbtack.school.notes.endpoint.request_param.SortOrder;
import net.thumbtack.school.notes.exception.ServerErrorCode;
import net.thumbtack.school.notes.exception.ServerException;
import net.thumbtack.school.notes.model.Session;
import net.thumbtack.school.notes.model.User;
import net.thumbtack.school.notes.model.UserType;
import net.thumbtack.school.notes.views.SessionView;
import net.thumbtack.school.notes.views.UserView;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
@Transactional(rollbackFor = Exception.class)
public class UserService {

    private final UserDao userDao;

    @Value("${user_idle_timeout}")
    private int userIdleTimeout;

    @Autowired
    public UserService(UserDao userDao) throws IOException {
        this.userDao = userDao;
    }

    public Pair<String, RegisterUserDtoResponse> registerUser(RegisterUserDtoRequest request) throws ServerException {
        User user = createUser(request);
        log.debug("Execute registerUser (User {})", user);

        String sessionId = UUID.randomUUID().toString();
        user.setTimeRegistered(LocalDateTime.now());
        userDao.registerUser(user, sessionId);

        RegisterUserDtoResponse response = new RegisterUserDtoResponse(request.getFirstName(), request.getLastName(),
                request.getPatronymic(), request.getLogin());
        return new ImmutablePair<>(sessionId, response);
    }

    public String login(LoginDtoRequest request) throws ServerException {
        log.debug("Execute login user with login {} and password {}", request.getLogin(), request.getPassword());
        String sessionId = UUID.randomUUID().toString();
        User user = getUserByLoginAndPassword(request.getLogin(), request.getPassword());
        Session session = new Session(sessionId, LocalDateTime.now(), user);
        userDao.login(session);
        return sessionId;
    }

    public void logout(String sessionId) throws ServerException {
        log.debug("Execute logout user with sessionId {}", sessionId);
        User user = getUserBySessionId(sessionId);
        userDao.logout(user.getId());
    }

    public EditUserProfileDtoResponse editUserProfile(String sessionId, EditUserProfileDtoRequest request) throws ServerException {
        log.debug("Execute editUserProfile user with sessionId {}", sessionId);
    	User user = getUserBySessionId(sessionId);
        checkPassword(user.getPassword(), request.getOldPassword());

        userDao.updateProfile(user.getId(), request.getFirstName(), request.getLastName(),
                request.getPatronymic(), request.getNewPassword());
        return new EditUserProfileDtoResponse(user.getId(), request.getFirstName(),
                request.getLastName(), request.getPatronymic(), user.getLogin());
    }

    public void transferToSuperuser(String sessionId, int id) throws ServerException {
        log.debug("Execute transferToSuperuser user with sessionId {}", sessionId);
        User user = getUserBySessionId(sessionId);
        checkIsAdmin(user);
        checkUserIdExists(id);
        userDao.transferToSuperuser(id);
    }

    public void deleteUser(String sessionId, DeleteUserDtoRequest request) throws ServerException {
        log.debug("Execute deleteUser user with sessionId {}", sessionId);
        User user = getUserBySessionId(sessionId);
        checkPassword(user.getPassword(), request.getPassword());
        userDao.logout(user.getId());
        userDao.deleteUser(user.getId());
    }

    public GetInfoOfUserDtoResponse getUserInfo(String sessionId) throws ServerException {
        log.debug("Execute deleteUser user with sessionId {}", sessionId);
        User user = getUserBySessionId(sessionId);
        return new GetInfoOfUserDtoResponse(user.getFirstName(), user.getLastName(), user.getPatronymic(),
                user.getLogin());
    }

    public List<GetUsersDtoResponse> getUserList(String sessionId, SortOrder sortByRating, SearchParams type,
                                                 int from, Integer count) throws ServerException {
        log.debug("Execute getUserList by user with sessionId {}", sessionId);
    	List<UserView> userList = new ArrayList<>();
        UserView user = getUserViewBySessionId(sessionId, sortByRating, from, count);
        switch (type) {
            case HIGH_RATING:
            case LOW_RATING:
                userList = userDao.getUsersWithHighOrLowRating(type, sortByRating, from, count);
                break;
            case FOLLOWINGS:
                userList = user.getFollowings();
                break;
            case FOLLOWERS:
                userList = user.getFollowers();
                break;
            case IGNORE:
                userList = user.getIgnore();
                break;
            case IGNORED_BY:
                userList = user.getIgnoreBy();
                break;
            case DELETED:
                userList = userDao.getDeletedUsers(sortByRating, from, count);
                break;
            case SUPER:
                if (user.getUserType() == UserType.ADMIN) {
                    userList = userDao.getSuperUsers(sortByRating, from, count);
                }
                break;
            case NONE:
                userList = userDao.getAllUsers(sortByRating, from, count); break;
        }

        List<GetUsersDtoResponse> response = new ArrayList<>();
        for (UserView u : userList) {
            response.add(new GetUsersDtoResponse(u.getId(), u.getFirstName(), u.getLastName(),
                    u.getPatronymic(), u.getLogin(), u.getTimeRegistered().toString(), u.isOnline(), u.isDeleted(),
                    user.getUserType() == UserType.ADMIN ? u.getUserType() == UserType.ADMIN : null ,
                    Math.round(u.getRating() * 10.0) / 10.0));
        }

        return response;
    }



    public void addToFollowing(String sessionId, AddToListDtoRequest request) throws ServerException {
        log.debug("Execute addToFollowing User with login {} to User with sessionId {}", request.getLogin(), sessionId);
        User user = getUserBySessionId(sessionId);
        User userFollowing = getUserByLogin(request.getLogin());

        userDao.deleteFromFollowing(user.getId(), userFollowing.getId());
        userDao.deleteFromIgnore(user.getId(), userFollowing.getId());
        userDao.addToFollowing(user.getId(), userFollowing.getId());
    }

    public void addToIgnore(String sessionId, AddToListDtoRequest request) throws ServerException {
        log.debug("Execute addToIgnore User with login {} to User with sessionId {}", request.getLogin(), sessionId);
        User user = getUserBySessionId(sessionId);
        User userIgnored = getUserByLogin(request.getLogin());

        userDao.deleteFromIgnore(user.getId(), userIgnored.getId());
        userDao.deleteFromFollowing(user.getId(), userIgnored.getId());
        userDao.addToIgnore(user.getId(), userIgnored.getId());
    }

    public void deleteFromFollowing(String sessionId, String loginFollowing) throws ServerException {
        log.debug("Execute deleteFromFollowing User with login {} from User with sessionId {}", loginFollowing, sessionId);
        User user = getUserBySessionId(sessionId);
        User userFollowing = getUserByLogin(loginFollowing);

        userDao.deleteFromFollowing(user.getId(), userFollowing.getId());
    }

    public void deleteFromIgnore(String sessionId, String loginIgnored) throws ServerException {
        log.debug("Execute deleteFromIgnore User with login {} from User with sessionId {}", loginIgnored, sessionId);
        User user = getUserBySessionId(sessionId);
        User userIgnored = getUserByLogin(loginIgnored);

        userDao.deleteFromIgnore(user.getId(), userIgnored.getId());
    }



    private static User createUser(RegisterUserDtoRequest data) {
        return new User(data.getFirstName(), data.getLastName(), data.getPatronymic(),
                data.getLogin(), data.getPassword());
    }

    private void checkIsAdmin(User user) throws ServerException {
        if (user.getUserType() != UserType.ADMIN) {
            log.info("Cannot execute transferToSuperuser, because user isn't superuser");
            throw new ServerException(ServerErrorCode.YOU_ARE_NOT_SUPERUSER);
        }
    }

    private void checkPassword(String passwordFromDB, String passwordFromRequest) throws ServerException {
        if(!passwordFromRequest.equals(passwordFromDB)) {
            log.info("Cannot execute deleteUser, because supplied password isn't correct");
            throw new ServerException(ServerErrorCode.WRONG_PASSWORD);
        }
    }

    private User getUserBySessionId(String sessionId) throws ServerException {
        Session session = userDao.getSession(sessionId);
        log.debug("Execute getUserBySessionId (Session {})", session);
        if(session == null) {
            log.info("Cannot execute getUserBySessionId, because sessionId wasn't found in DB");
            throw new ServerException(ServerErrorCode.THIS_SESSIONID_NOT_FOUND);
        }
        checkTimeSession(session.getTimeLogin(), session.getUser().getId());
        return session.getUser();
    }

    private UserView getUserViewBySessionId(String sessionId, SortOrder sortByRating,
                                            int from, Integer count) throws ServerException {
        SessionView session = userDao.getSessionView(sessionId, sortByRating, from, count);
        log.debug("Execute getUserViewBySessionId (Session {})", session);
        if(session == null) {
            log.info("Cannot execute getUserViewBySessionId, because sessionId wasn't found in DB");
            throw new ServerException(ServerErrorCode.THIS_SESSIONID_NOT_FOUND);
        }
        checkTimeSession(session.getTimeLogin(), session.getUser().getId());
        return session.getUser();
    }

    private void checkTimeSession(LocalDateTime timeLogin, int userId) throws ServerException {
        LocalDateTime timeLogout = timeLogin.plusSeconds(userIdleTimeout);
        LocalDateTime currentTime = LocalDateTime.now();
        if (currentTime.isAfter(timeLogout)) {
            log.info("Cannot execute getUserBySessionId, because session time is over");
            throw new ServerException(ServerErrorCode.SESSION_TIME_IS_OVER);
        }
        userDao.updateSession(userId, currentTime);
    }

    private User getUserByLogin(String login) throws ServerException {
        User user = userDao.getUserByLogin(login);
        if (user == null) {
            log.info("Cannot execute getUserByLogin, because user with login {} wasn't found in DB", login);
            throw new ServerException(ServerErrorCode.THIS_LOGIN_NOT_FOUND);
        }
        return user;
    }

    private User getUserByLoginAndPassword(String login, String password) throws ServerException {
        User user = userDao.getUserByLoginAndPassword(login, password);
        if(user == null) {
            log.debug("Cannot execute getUserByLoginAndPassword, because user with login {} and password {} wasn't found in DB",
                    login, password);
            throw new ServerException(ServerErrorCode.THIS_LOGIN_AND_PASSWORD_NOT_FOUND);
        }
        return user;
    }

    private User checkUserIdExists(int id) throws ServerException {
        User user = userDao.getUserById(id);
        if (user == null) {
            log.info("Cannot execute checkUserIdExists, because user with id {} wasn't found in DB", id);
            throw new ServerException(ServerErrorCode.THIS_ID_NOT_FOUND);
        }
        return user;
    }
}
