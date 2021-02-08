package net.thumbtack.school.notes.endpoint.user;

import com.google.gson.Gson;
import net.thumbtack.school.notes.NotesServer;
import net.thumbtack.school.notes.dto.requests.user.AddToListDtoRequest;
import net.thumbtack.school.notes.dto.requests.user.RegisterUserDtoRequest;
import net.thumbtack.school.notes.dto.responses.user.RegisterUserDtoResponse;
import net.thumbtack.school.notes.exception.ServerError;
import net.thumbtack.school.notes.exception.ServerErrorCode;
import net.thumbtack.school.notes.exception.ServerErrors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes= NotesServer.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class FollowingIgnoreEndPointTest {

    private final RestTemplate template = new RestTemplate();

    private final Gson gson = new Gson();

    @BeforeEach
    void setUp() {
        template.postForEntity("http://localhost:8080/api/debug/clear", null, Object.class);
    }

    @Test
    public void testAddToFollowingsWithSessionIdNotFound() {
        AddToListDtoRequest request = new AddToListDtoRequest("login0000");
        ServerErrors expectedResponse = createException(ServerErrorCode.THIS_SESSIONID_NOT_FOUND);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "JAVASESSIONID=" + UUID.randomUUID().toString());

        try {
            template.postForEntity("http://localhost:8080/api/followings", new HttpEntity(request, headers),
                    ServerErrors.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testAddToFollowingsWithLoginNotFound() {
        AddToListDtoRequest request = new AddToListDtoRequest("login0000");
        ServerErrors expectedResponse = createException(ServerErrorCode.THIS_LOGIN_NOT_FOUND);

        RegisterUserDtoRequest requestRegister = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "login0001", "password0000");
        String cookie = template.postForEntity("http://localhost:8080/api/accounts", requestRegister,
                RegisterUserDtoResponse.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        try {
            template.postForEntity("http://localhost:8080/api/followings", new HttpEntity(request, headers),
                    ServerErrors.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testAddToFollowingsWithoutException() {
        AddToListDtoRequest request = new AddToListDtoRequest("login0000");

        RegisterUserDtoRequest requestRegister1 = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "login0000", "password0000");
        template.postForEntity("http://localhost:8080/api/accounts", requestRegister1, RegisterUserDtoResponse.class);

        RegisterUserDtoRequest requestRegister2 = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "login0001", "password0000");
        String cookie = template.postForEntity("http://localhost:8080/api/accounts", requestRegister2,
                RegisterUserDtoResponse.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        assertDoesNotThrow(() -> template.postForEntity("http://localhost:8080/api/followings",
                new HttpEntity(request, headers), ServerErrors.class));
    }

    @Test
    public void testAddToIgnoreWithSessionIdNotFound() {
        AddToListDtoRequest request = new AddToListDtoRequest("login0000");
        ServerErrors expectedResponse = createException(ServerErrorCode.THIS_SESSIONID_NOT_FOUND);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "JAVASESSIONID=" + UUID.randomUUID().toString());

        try {
            template.postForEntity("http://localhost:8080/api/ignore", new HttpEntity(request, headers),
                    ServerErrors.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testAddToIgnoreWithLoginNotFound() {
        AddToListDtoRequest request = new AddToListDtoRequest("login0000");
        ServerErrors expectedResponse = createException(ServerErrorCode.THIS_LOGIN_NOT_FOUND);

        RegisterUserDtoRequest requestRegister = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "login0001", "password0000");
        String cookie = template.postForEntity("http://localhost:8080/api/accounts", requestRegister,
                RegisterUserDtoResponse.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        try {
            template.postForEntity("http://localhost:8080/api/ignore", new HttpEntity(request, headers),
                    ServerErrors.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testAddToIgnoreWithoutException() {
        AddToListDtoRequest request = new AddToListDtoRequest("login0000");

        RegisterUserDtoRequest requestRegister1 = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "login0000", "password0000");
        template.postForEntity("http://localhost:8080/api/accounts", requestRegister1, RegisterUserDtoResponse.class);

        RegisterUserDtoRequest requestRegister2 = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "login0001", "password0000");
        String cookie = template.postForEntity("http://localhost:8080/api/accounts", requestRegister2,
                RegisterUserDtoResponse.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        assertDoesNotThrow(() -> template.postForEntity("http://localhost:8080/api/ignore",
                new HttpEntity(request, headers), ServerErrors.class));
    }

    @Test
    public void testDeleteFromFollowingsWithSessionIdNotFound() {
        ServerErrors expectedResponse = createException(ServerErrorCode.THIS_SESSIONID_NOT_FOUND);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "JAVASESSIONID=" + UUID.randomUUID().toString());

        try {
            template.exchange("http://localhost:8080/api/followings/login0000", HttpMethod.DELETE,
                    new HttpEntity(headers), ServerErrors.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testDeleteFromFollowingsWithLoginNotFound() {
        ServerErrors expectedResponse = createException(ServerErrorCode.THIS_LOGIN_NOT_FOUND);

        RegisterUserDtoRequest requestRegister = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "login0001", "password0000");
        String cookie = template.postForEntity("http://localhost:8080/api/accounts", requestRegister,
                RegisterUserDtoResponse.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        try {
            template.exchange("http://localhost:8080/api/followings/login0000", HttpMethod.DELETE,
                    new HttpEntity(headers), ServerErrors.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testDeleteFromFollowingsWithoutException() {
        RegisterUserDtoRequest requestRegister1 = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "login0000", "password0000");
        template.postForEntity("http://localhost:8080/api/accounts", requestRegister1, RegisterUserDtoResponse.class);

        RegisterUserDtoRequest requestRegister2 = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "login0001", "password0000");
        String cookie = template.postForEntity("http://localhost:8080/api/accounts", requestRegister2,
                RegisterUserDtoResponse.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        assertDoesNotThrow(() -> template.exchange("http://localhost:8080/api/followings/login0000", HttpMethod.DELETE,
                new HttpEntity(headers), ServerErrors.class));
    }

    @Test
    public void testDeleteFromIgnoreWithSessionIdNotFound() {
        ServerErrors expectedResponse = createException(ServerErrorCode.THIS_SESSIONID_NOT_FOUND);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "JAVASESSIONID=" + UUID.randomUUID().toString());

        try {
            template.exchange("http://localhost:8080/api/ignore/login0000", HttpMethod.DELETE,
                    new HttpEntity(headers), ServerErrors.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testDeleteFromIgnoreWithLoginNotFound() {
        ServerErrors expectedResponse = createException(ServerErrorCode.THIS_LOGIN_NOT_FOUND);

        RegisterUserDtoRequest requestRegister = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "login0001", "password0000");
        String cookie = template.postForEntity("http://localhost:8080/api/accounts", requestRegister,
                RegisterUserDtoResponse.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        try {
            template.exchange("http://localhost:8080/api/ignore/login0000", HttpMethod.DELETE,
                    new HttpEntity(headers), ServerErrors.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testDeleteFromIgnoreWithoutException() {
        RegisterUserDtoRequest requestRegister1 = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "login0000", "password0000");
        template.postForEntity("http://localhost:8080/api/accounts", requestRegister1, RegisterUserDtoResponse.class);

        RegisterUserDtoRequest requestRegister2 = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "login0001", "password0000");
        String cookie = template.postForEntity("http://localhost:8080/api/accounts", requestRegister2,
                RegisterUserDtoResponse.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        assertDoesNotThrow(() -> template.exchange("http://localhost:8080/api/ignore/login0000", HttpMethod.DELETE,
                new HttpEntity(headers), ServerErrors.class));
    }

    private ServerErrors createException(ServerErrorCode errorCode) {
        Set<ServerError> expectedErrors = new HashSet<>();
        expectedErrors.add(new ServerError(errorCode, errorCode.getField(),
                errorCode.getErrorMessage()));
        return new ServerErrors(expectedErrors);
    }
}