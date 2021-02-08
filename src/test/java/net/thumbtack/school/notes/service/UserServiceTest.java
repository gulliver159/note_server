package net.thumbtack.school.notes.service;

import net.thumbtack.school.notes.dao.UserDao;
import net.thumbtack.school.notes.dto.requests.user.*;
import net.thumbtack.school.notes.dto.responses.user.EditUserProfileDtoResponse;
import net.thumbtack.school.notes.dto.responses.user.GetInfoOfUserDtoResponse;
import net.thumbtack.school.notes.dto.responses.user.RegisterUserDtoResponse;
import net.thumbtack.school.notes.exception.ServerErrorCode;
import net.thumbtack.school.notes.exception.ServerException;
import net.thumbtack.school.notes.model.Session;
import net.thumbtack.school.notes.model.User;
import net.thumbtack.school.notes.model.UserType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@SpringBootTest
class UserServiceTest {

    @MockBean
    private UserDao userDao;

    @Autowired
    private UserService userService;

    @Test
    public void testRegisterUserWithBusyLogin() throws ServerException {
        doThrow(new ServerException(ServerErrorCode.LOGIN_ALREADY_BUSY)).
                when(userDao).registerUser(any(User.class), anyString());
        Throwable thrown = assertThrows(ServerException.class,
                () -> userService.registerUser(new RegisterUserDtoRequest()));
        assertEquals(new ServerException(ServerErrorCode.LOGIN_ALREADY_BUSY), thrown);
    }

    @Test
    public void testRegisterValidUser() throws ServerException {
        RegisterUserDtoRequest request = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "katya2001", "katya5643");
        RegisterUserDtoResponse response = new RegisterUserDtoResponse("Ekaterina", "Rogozhina",
                "Andreevna", "katya2001");

        assertEquals(response, userService.registerUser(request).getRight());
    }

    @Test
    public void testEditUserProfileWithSessionIdNotFound() {
        when(userDao
                .getSession(anyString())).thenReturn(null);

        EditUserProfileDtoRequest request = new EditUserProfileDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "katya5643", "katya0000");

        Throwable thrown = assertThrows(ServerException.class,
                () -> userService.editUserProfile(UUID.randomUUID().toString(), request));

        assertEquals(new ServerException(ServerErrorCode.THIS_SESSIONID_NOT_FOUND), thrown);
    }

    @Test
    public void testEditUserProfileWithWrongPassword() {
        EditUserProfileDtoRequest request = new EditUserProfileDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "katya5643", "katya0000");

        Session sessionWithWrongPassword = new Session("as", LocalDateTime.now());
        sessionWithWrongPassword.setUser(new User("Ekaterina", "Rogozhina",
                "Andreevna", "katya2001","annna1234"));

        when(userDao.getSession(anyString())).thenReturn(sessionWithWrongPassword);

        Throwable thrown = assertThrows(ServerException.class,
                () -> userService.editUserProfile(UUID.randomUUID().toString(), request));

        assertEquals(new ServerException(ServerErrorCode.WRONG_PASSWORD), thrown);
    }

    @Test
    public void testEditUserProfileWithoutExceptions() throws ServerException {
        EditUserProfileDtoRequest request = new EditUserProfileDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "katya5643", "katya0000");

        Session sessionWithCorrectPassword = new Session("as", LocalDateTime.now());
        sessionWithCorrectPassword.setUser(new User("Ekaterina", "Rogozhina",
                "Andreevna", "katya2001","katya5643"));

        when(userDao.getSession(anyString())).thenReturn(sessionWithCorrectPassword);

        EditUserProfileDtoResponse response = new EditUserProfileDtoResponse(0, "Ekaterina", "Rogozhina",
                "Andreevna", "katya2001");

        assertEquals(response, userService.editUserProfile(UUID.randomUUID().toString(), request));
    }

    @Test
    public void testTransferToSuperuserWithUserNotAdmin() {
        Session session = new Session("as", LocalDateTime.now());
        session.setUser(new User("Ekaterina", "Rogozhina",
                "Andreevna", "katya2001","katya5643"));

        when(userDao.getSession(anyString())).thenReturn(session);

        Throwable thrown = assertThrows(ServerException.class,
                () -> userService.transferToSuperuser(UUID.randomUUID().toString(), 0));

        assertEquals(new ServerException(ServerErrorCode.YOU_ARE_NOT_SUPERUSER), thrown);
    }

    @Test
    public void testTransferToSuperuserWithUserIdNotFound() {
        Session session = new Session("as", LocalDateTime.now());
        session.setUser(new User(0, "Ekaterina", "Rogozhina",
                "Andreevna", "katya2001","katya5643", UserType.ADMIN,
                true, null));

        when(userDao.getSession(anyString())).thenReturn(session);

        Throwable thrown = assertThrows(ServerException.class,
                () -> userService.transferToSuperuser(UUID.randomUUID().toString(), 0));

        assertEquals(new ServerException(ServerErrorCode.THIS_ID_NOT_FOUND), thrown);
    }

    @Test
    public void testDeleteUserWithSessionIdNotFound() {
        when(userDao.getSession(anyString())).thenReturn(null);

        DeleteUserDtoRequest request = new DeleteUserDtoRequest("katya5643");

        Throwable thrown = assertThrows(ServerException.class,
                () -> userService.deleteUser(UUID.randomUUID().toString(), request));

        assertEquals(new ServerException(ServerErrorCode.THIS_SESSIONID_NOT_FOUND), thrown);
    }

    @Test
    public void testDeleteUserWithWrongPassword() {
        DeleteUserDtoRequest request = new DeleteUserDtoRequest("katya5643");

        Session sessionWithWrongPassword = new Session("as", LocalDateTime.now());
        sessionWithWrongPassword.setUser(new User("Ekaterina", "Rogozhina",
                "Andreevna", "katya2001","annna1234"));

        when(userDao.getSession(anyString())).thenReturn(sessionWithWrongPassword);

        Throwable thrown = assertThrows(ServerException.class,
                () -> userService.deleteUser(UUID.randomUUID().toString(), request));

        assertEquals(new ServerException(ServerErrorCode.WRONG_PASSWORD), thrown);
    }

    @Test
    public void testDeleteUserWithoutExceptions() {
        Session session = new Session("as", LocalDateTime.now());
        session.setUser(new User("Ekaterina", "Rogozhina",
                "Andreevna", "katya2001","katya5643"));

        DeleteUserDtoRequest request = new DeleteUserDtoRequest("katya5643");

        when(userDao.getSession(anyString())).thenReturn(session);

        assertDoesNotThrow(() -> userService.deleteUser(UUID.randomUUID().toString(), request));
    }

    @Test
    public void testGetInfoOfUserWithSessionIdNotFound() {
        when(userDao.getSession(anyString())).thenReturn(null);

        Throwable thrown = assertThrows(ServerException.class,
                () -> userService.getUserInfo(UUID.randomUUID().toString()));

        assertEquals(new ServerException(ServerErrorCode.THIS_SESSIONID_NOT_FOUND), thrown);
    }

    @Test
    public void testGetInfoOfUserWithoutExceptions() throws ServerException {
        Session session = new Session("as", LocalDateTime.now());
        session.setUser(new User("Ekaterina", "Rogozhina",
                "Andreevna", "katya2001","katya5643"));

        when(userDao.getSession(anyString())).thenReturn(session);

        GetInfoOfUserDtoResponse response = new GetInfoOfUserDtoResponse(
                "Ekaterina", "Rogozhina", "Andreevna", "katya2001"
        );

        assertEquals(response, userService.getUserInfo(UUID.randomUUID().toString()));
    }

    @Test
    public void testLoginWithLoginAndPasswordNotFound() {
        when(userDao.getUserByLoginAndPassword(anyString(), anyString())).thenReturn(null);

        Throwable thrown = assertThrows(ServerException.class,
                () -> userService.login(new LoginDtoRequest()));

        assertEquals(new ServerException(ServerErrorCode.THIS_LOGIN_AND_PASSWORD_NOT_FOUND), thrown);
    }

    @Test
    public void testLoginWithoutExceptions() {
        User user = new User("Ekaterina", "Rogozhina",
                "Andreevna", "katya2001","katya5643");

        LoginDtoRequest request = new LoginDtoRequest("katya2001", "katya5643");

        when(userDao.getUserByLoginAndPassword(anyString(), anyString())).thenReturn(user);

        assertDoesNotThrow(() -> userService.login(request));
    }

    @Test
    public void testLogoutWithSessionIdNotFound() {
        when(userDao.getSession(anyString())).thenReturn(null);

        Throwable thrown = assertThrows(ServerException.class,
                () -> userService.logout(UUID.randomUUID().toString()));

        assertEquals(new ServerException(ServerErrorCode.THIS_SESSIONID_NOT_FOUND), thrown);
    }

    @Test
    public void testLogoutWithoutExceptions() {
        Session session = new Session("as", LocalDateTime.now());
        session.setUser(new User("Ekaterina", "Rogozhina",
                "Andreevna", "katya2001","katya5643"));

        when(userDao.getSession(anyString())).thenReturn(session);

        assertDoesNotThrow(() -> userService.logout(UUID.randomUUID().toString()));
    }

    @Test
    public void testAddToFollowingsWithSessionIdNotFound() {
        when(userDao.getSession(anyString())).thenReturn(null);

        Throwable thrown = assertThrows(ServerException.class,
                () -> userService.addToFollowing(UUID.randomUUID().toString(), new AddToListDtoRequest()));

        assertEquals(new ServerException(ServerErrorCode.THIS_SESSIONID_NOT_FOUND), thrown);
    }

    @Test
    public void testAddToFollowingsWithLoginNotFound() {
        Session session = new Session("as", LocalDateTime.now());
        session.setUser(new User("Ekaterina", "Rogozhina",
                "Andreevna", "katya2001","katya5643"));

        when(userDao.getSession(anyString())).thenReturn(session);
        when(userDao.getUserByLogin(anyString())).thenReturn(null);

        Throwable thrown = assertThrows(ServerException.class,
                () -> userService.addToFollowing(UUID.randomUUID().toString(), new AddToListDtoRequest()));

        assertEquals(new ServerException(ServerErrorCode.THIS_LOGIN_NOT_FOUND), thrown);
    }

    @Test
    public void testAddToFollowingsWithoutExceptions() {
        Session session = new Session("as", LocalDateTime.now());
        session.setUser(new User("Ekaterina", "Rogozhina",
                "Andreevna", "katya2001","katya5643"));
        User user2 = new User("Ekaterina", "Rogozhina",
                "Andreevna", "katya2002","katya5643");
        when(userDao.getSession(anyString())).thenReturn(session);
        when(userDao.getUserByLogin(anyString())).thenReturn(user2);

        assertDoesNotThrow(() -> userService.addToFollowing(UUID.randomUUID().toString(),
                new AddToListDtoRequest("login")));
    }

    @Test
    public void testAddToIgnoreWithSessionIdNotFound() {
        when(userDao.getSession(anyString())).thenReturn(null);

        Throwable thrown = assertThrows(ServerException.class,
                () -> userService.addToIgnore(UUID.randomUUID().toString(), new AddToListDtoRequest()));

        assertEquals(new ServerException(ServerErrorCode.THIS_SESSIONID_NOT_FOUND), thrown);
    }

    @Test
    public void testAddToIgnoreWithLoginNotFound() {
        Session session = new Session("as", LocalDateTime.now());
        session.setUser(new User("Ekaterina", "Rogozhina",
                "Andreevna", "katya2001","katya5643"));
        when(userDao.getSession(anyString())).thenReturn(session);
        when(userDao.getUserByLogin(anyString())).thenReturn(null);

        Throwable thrown = assertThrows(ServerException.class,
                () -> userService.addToIgnore(UUID.randomUUID().toString(), new AddToListDtoRequest()));

        assertEquals(new ServerException(ServerErrorCode.THIS_LOGIN_NOT_FOUND), thrown);
    }

    @Test
    public void testAddToIgnoreWithoutExceptions() {
        Session session = new Session("as", LocalDateTime.now());
        session.setUser(new User("Ekaterina", "Rogozhina",
                "Andreevna", "katya2001","katya5643"));
        User user2 = new User("Ekaterina", "Rogozhina",
                "Andreevna", "katya2002","katya5643");
        when(userDao.getSession(anyString())).thenReturn(session);
        when(userDao.getUserByLogin(anyString())).thenReturn(user2);

        assertDoesNotThrow(() -> userService.addToIgnore(UUID.randomUUID().toString(),
                new AddToListDtoRequest("login")));
    }

    @Test
    public void testDeleteFromFollowingsWithSessionIdNotFound() {
        when(userDao.getSession(anyString())).thenReturn(null);

        Throwable thrown = assertThrows(ServerException.class,
                () -> userService.deleteFromFollowing(UUID.randomUUID().toString(), "loginFollowing"));

        assertEquals(new ServerException(ServerErrorCode.THIS_SESSIONID_NOT_FOUND), thrown);
    }

    @Test
    public void testDeleteFromFollowingsWithLoginNotFound() {
        Session session = new Session("as", LocalDateTime.now());
        session.setUser(new User("Ekaterina", "Rogozhina",
                "Andreevna", "katya2001","katya5643"));
        when(userDao.getSession(anyString())).thenReturn(session);
        when(userDao.getUserByLogin(anyString())).thenReturn(null);

        Throwable thrown = assertThrows(ServerException.class,
                () -> userService.deleteFromFollowing(UUID.randomUUID().toString(), "loginFollowing"));

        assertEquals(new ServerException(ServerErrorCode.THIS_LOGIN_NOT_FOUND), thrown);
    }

    @Test
    public void testDeleteFromFollowingsWithoutExceptions() {
        Session session = new Session("as", LocalDateTime.now());
        session.setUser(new User("Ekaterina", "Rogozhina",
                "Andreevna", "katya2001","katya5643"));
        User user2 = new User("Ekaterina", "Rogozhina",
                "Andreevna", "katya2002","katya5643");
        when(userDao.getSession(anyString())).thenReturn(session);
        when(userDao.getUserByLogin(anyString())).thenReturn(user2);

        assertDoesNotThrow(() -> userService.deleteFromFollowing(UUID.randomUUID().toString(), "loginFollowing"));
    }

    @Test
    public void testDeleteFromIgnoreWithSessionIdNotFound() {
        when(userDao.getSession(anyString())).thenReturn(null);

        Throwable thrown = assertThrows(ServerException.class,
                () -> userService.deleteFromIgnore(UUID.randomUUID().toString(), "loginFollowing"));

        assertEquals(new ServerException(ServerErrorCode.THIS_SESSIONID_NOT_FOUND), thrown);
    }

    @Test
    public void testDeleteFromIgnoreWithLoginNotFound() {
        Session session = new Session("as", LocalDateTime.now());
        session.setUser(new User("Ekaterina", "Rogozhina",
                "Andreevna", "katya2001","katya5643"));
        when(userDao.getSession(anyString())).thenReturn(session);
        when(userDao.getUserByLogin(anyString())).thenReturn(null);

        Throwable thrown = assertThrows(ServerException.class,
                () -> userService.deleteFromIgnore(UUID.randomUUID().toString(), "loginFollowing"));

        assertEquals(new ServerException(ServerErrorCode.THIS_LOGIN_NOT_FOUND), thrown);
    }

    @Test
    public void testDeleteFromIgnoreWithoutExceptions() {
        Session session = new Session("as", LocalDateTime.now());
        session.setUser(new User("Ekaterina", "Rogozhina",
                "Andreevna", "katya2001","katya5643"));
        User user2 = new User("Ekaterina", "Rogozhina",
                "Andreevna", "katya2002","katya5643");
        when(userDao.getSession(anyString())).thenReturn(session);
        when(userDao.getUserByLogin(anyString())).thenReturn(user2);

        assertDoesNotThrow(() -> userService.deleteFromIgnore(UUID.randomUUID().toString(), "loginFollowing"));
    }
}