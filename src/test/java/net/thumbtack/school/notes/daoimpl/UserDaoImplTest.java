package net.thumbtack.school.notes.daoimpl;

import net.thumbtack.school.notes.dao.UserDao;
import net.thumbtack.school.notes.debugging.DebugDaoImpl;
import net.thumbtack.school.notes.endpoint.request_param.SortOrder;
import net.thumbtack.school.notes.exception.ServerErrorCode;
import net.thumbtack.school.notes.exception.ServerException;
import net.thumbtack.school.notes.model.Session;
import net.thumbtack.school.notes.model.User;
import net.thumbtack.school.notes.model.UserType;
import net.thumbtack.school.notes.views.UserView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserDaoImplTest {

    private final UserDao userDao;
    private final DebugDaoImpl debugDao;

    @Autowired
    public UserDaoImplTest(UserDao userDao, DebugDaoImpl debugDao) {
        this.userDao = userDao;
        this.debugDao = debugDao;
    }

    private User user;
    String sessionId;

    @BeforeEach
    void setUp() throws ServerException {
        debugDao.clearStateServer();
        user = new User("Ekaterina", "Rogozhina", "Andreevna",
                "katya2001", "katya5643");
        user.setTimeRegistered(LocalDateTime.now());
        sessionId = UUID.randomUUID().toString();
        userDao.registerUser(user, sessionId);
    }

    @Test
    public void testRegisterValidUser() {
        assertEquals(user, userDao.getSession(sessionId).getUser());
    }

    @Test
    public void testRegisterInvalidUser() throws ServerException {
        User user2 = new User("Anna", "Novikova", null,
                "katya2001", "annnna5643");
        user2.setTimeRegistered(LocalDateTime.now());
        String sessionId2 = UUID.randomUUID().toString();

        Throwable thrown = assertThrows(ServerException.class, () -> {
            userDao.registerUser(user2, sessionId2);
        });
        assertEquals(new ServerException(ServerErrorCode.LOGIN_ALREADY_BUSY), thrown);
    }

    @Test
    public void testUpdateProfile() throws ServerException {
        int id = userDao.getSession(sessionId).getUser().getId();

        userDao.updateProfile(id, "Ekaterina", "Pronnikova", "Andreevna", "katya0000");

        User expectedUser = new User("Ekaterina", "Pronnikova", "Andreevna",
                "katya2001", "katya0000");
        User actualUser = userDao.getSession(sessionId).getUser();

        assertEquals(expectedUser, actualUser);
    }

    @Test
    public void testDeleteUser() throws ServerException {
        User user = userDao.getSession(sessionId).getUser();
        userDao.deleteUser(user.getId());

        assertTrue(userDao.getUserByLogin(user.getLogin()).isDeleted());
    }

    @Test
    public void testTransferToSuperuser() throws ServerException {
        userDao.transferToSuperuser(user.getId());
        assertEquals(UserType.ADMIN, userDao.getSession(sessionId).getUser().getUserType());
    }

    @Test
    public void testLogin() {
        String sessionId = UUID.randomUUID().toString();
        userDao.login(new Session(sessionId, LocalDateTime.now(), user));
        assertEquals(user.getId(), userDao.getSession(sessionId).getUser().getId());
    }

    @Test
    public void testLogout() {
        userDao.logout(user.getId());
        assertNull(userDao.getSession(sessionId));
    }

    @Test
    public void testGetUserByLoginAndPassword() {
        assertEquals(user, userDao.getSession(sessionId).getUser());
    }

    @Test
    public void testAddToFollowings() throws ServerException {
        User user2 = registerUser2();
        UserView userView2 = new UserView(user2.getId(), user2.getFirstName(), user2.getLastName(), user2.getPatronymic(),
                user2.getLogin(), user2.getPassword(), 0, user2.getUserType(), user2.isDeleted(), user2.getTimeRegistered());
        userView2.setOnline(true);
        userDao.addToFollowing(user.getId(), user2.getId());

        List<UserView> expectedFollowings = new ArrayList<>();
        expectedFollowings.add(userView2);

        assertEquals(expectedFollowings.get(0), userDao.getSessionView(sessionId, SortOrder.NONE, 0, null)
                .getUser().getFollowings().get(0));
    }

    @Test
    public void testAddToIgnore() throws ServerException {
        User user2 = registerUser2();
        userDao.addToIgnore(user.getId(), user2.getId());

        UserView userView2 = new UserView(user2.getId(), user2.getFirstName(), user2.getLastName(), user2.getPatronymic(),
                user2.getLogin(), user2.getPassword(), 0, user2.getUserType(), user2.isDeleted(), user2.getTimeRegistered());
        userView2.setOnline(true);
        List<UserView> expectedIgnore = new ArrayList<>();
        expectedIgnore.add(userView2);

        assertEquals(expectedIgnore.get(0), userDao.getSessionView(sessionId, SortOrder.NONE, 0, null)
                .getUser().getIgnore().get(0));
    }

    @Test
    public void testDeleteFromFollowings() throws ServerException {
        User user2 = registerUser2();
        userDao.addToFollowing(user.getId(), user2.getId());
        userDao.deleteFromFollowing(user.getId(), user2.getId());

        List<User> expectedFollowings = new ArrayList<>();
        assertEquals(expectedFollowings, userDao.getSession(sessionId).getUser().getFollowings());
    }

    @Test
    public void testDeleteFromIgnore() throws ServerException {
        User user2 = registerUser2();
        userDao.addToIgnore(user.getId(), user2.getId());
        userDao.deleteFromIgnore(user.getId(), user2.getId());

        List<User> expectedIgnore = new ArrayList<>();
        assertEquals(expectedIgnore, userDao.getSession(sessionId).getUser().getIgnore());
    }

    private User registerUser2() throws ServerException {
        User user2 = new User("Ekaterina", "Rogozhina", "Andreevna",
                "katya2002", "katya5643");
        user2.setTimeRegistered(LocalDateTime.now());
        userDao.registerUser(user2, UUID.randomUUID().toString());
        return user2;
    }
}