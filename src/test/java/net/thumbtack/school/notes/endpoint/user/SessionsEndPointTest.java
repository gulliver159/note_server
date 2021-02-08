package net.thumbtack.school.notes.endpoint.user;

import com.google.gson.Gson;
import net.thumbtack.school.notes.NotesServer;
import net.thumbtack.school.notes.dto.requests.user.LoginDtoRequest;
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
class SessionsEndPointTest {

    private final RestTemplate template = new RestTemplate();

    private final Gson gson = new Gson();

    @BeforeEach
    void setUp() {
        template.postForEntity("http://localhost:8080/api/debug/clear", null, Object.class);
    }

    @Test
    public void testLoginWithInvalidData() {
        LoginDtoRequest request = new LoginDtoRequest(null, "katya20");

        Set<ServerError> expectedErrors = new HashSet<>();
        expectedErrors.add(new ServerError(ServerErrorCode.INVALID_LOGIN, "login",
                "Login cannot be null"));
        expectedErrors.add(new ServerError(ServerErrorCode.INVALID_PASSWORD, "password",
                "The password of the wrong size"));
        ServerErrors expectedResponse = new ServerErrors(expectedErrors);

        try {
            template.postForEntity("http://localhost:8080/api/sessions", new HttpEntity(request),
                    ServerErrors.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testLoginWithLoginAndPasswordNotFound() {
        LoginDtoRequest request = new LoginDtoRequest("katya2001", "katya2020202");

        Set<ServerError> expectedErrors = new HashSet<>();
        expectedErrors.add(new ServerError(ServerErrorCode.THIS_LOGIN_AND_PASSWORD_NOT_FOUND, "login and password",
                ServerErrorCode.THIS_LOGIN_AND_PASSWORD_NOT_FOUND.getErrorMessage()));
        ServerErrors expectedResponse = new ServerErrors(expectedErrors);

        try {
            template.postForEntity("http://localhost:8080/api/sessions", new HttpEntity(request),
                    ServerErrors.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testLoginWithoutExceptions() {
        LoginDtoRequest request = new LoginDtoRequest("katya2001", "katya000000");

        RegisterUserDtoRequest requestRegister = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "katya2001", "katya000000");
        template.postForEntity("http://localhost:8080/api/accounts",
                new HttpEntity(requestRegister), RegisterUserDtoResponse.class);

        assertDoesNotThrow(() -> {
            ResponseEntity<Void> responseEntity =
                    template.postForEntity("http://localhost:8080/api/sessions", request, Void.class);

            assertAll(
                    () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                    () -> assertNotNull(responseEntity.getHeaders().getFirst(HttpHeaders.SET_COOKIE))
            );
        });
    }

    @Test
    public void testLogoutWithSessionIdNotFound() {
        Set<ServerError> expectedErrors = new HashSet<>();
        expectedErrors.add(new ServerError(ServerErrorCode.THIS_SESSIONID_NOT_FOUND, "sessionId",
                ServerErrorCode.THIS_SESSIONID_NOT_FOUND.getErrorMessage()));
        ServerErrors expectedResponse = new ServerErrors(expectedErrors);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "JAVASESSIONID=" + UUID.randomUUID().toString());

        try {
            template.exchange("http://localhost:8080/api/sessions", HttpMethod.DELETE, new HttpEntity(headers),
                    ServerErrors.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testLogoutWithoutExceptions() {
        RegisterUserDtoRequest requestRegister = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "katya2001", "katya000000");
        String cookie = template.postForEntity("http://localhost:8080/api/accounts", requestRegister,
                RegisterUserDtoResponse.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        assertDoesNotThrow(() ->
                    template.exchange("http://localhost:8080/api/sessions", HttpMethod.DELETE,
                            new HttpEntity(headers), Void.class));
    }
}