package net.thumbtack.school.notes.endpoint.user;

import com.google.gson.Gson;
import net.thumbtack.school.notes.NotesServer;
import net.thumbtack.school.notes.dto.requests.user.DeleteUserDtoRequest;
import net.thumbtack.school.notes.dto.requests.user.EditUserProfileDtoRequest;
import net.thumbtack.school.notes.dto.requests.user.RegisterUserDtoRequest;
import net.thumbtack.school.notes.dto.responses.user.EditUserProfileDtoResponse;
import net.thumbtack.school.notes.dto.responses.user.GetInfoOfUserDtoResponse;
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
class AccountsEndPointTest {

    private final RestTemplate template = new RestTemplate();

    private final Gson gson = new Gson();

    @BeforeEach
    void setUp() {
        template.postForEntity("http://localhost:8080/api/debug/clear", null, Object.class);
    }

    @Test
    public void testRegisterUserWithInvalidData() {
        RegisterUserDtoRequest request = new RegisterUserDtoRequest(null, "Rogozhina",
                "Andreevna12", "katya2001-", "katya56");

        Set<ServerError> expectedErrors = new HashSet<>();
        expectedErrors.add(new ServerError(ServerErrorCode.INVALID_FIRSTNAME, "firstName",
                "First name cannot be null"));
        expectedErrors.add(new ServerError(ServerErrorCode.INVALID_PATRONYMIC, "patronymic",
                "The patronymic can only contain Latin and Russian letters, spaces and a minus sign"));
        expectedErrors.add(new ServerError(ServerErrorCode.INVALID_LOGIN, "login",
                "The login can only contain Latin and Russian letters and numbers"));
        expectedErrors.add(new ServerError(ServerErrorCode.INVALID_PASSWORD, "password",
                "The password of the wrong size"));

        ServerErrors expectedResponse = new ServerErrors(expectedErrors);

        try {
            template.postForEntity("http://localhost:8080/api/accounts", request, ServerErrors.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testRegisterUserWithBusyLogin() {
        RegisterUserDtoRequest request1 = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "katya2001", "katya5643654");
        RegisterUserDtoRequest request2 = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "katya2001", "katya5643654");

        ServerErrors expectedResponse = createException(ServerErrorCode.LOGIN_ALREADY_BUSY);

        template.postForEntity("http://localhost:8080/api/accounts",
                request1, RegisterUserDtoResponse.class);

        try {
            template.postForEntity("http://localhost:8080/api/accounts", request2, ServerErrors.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testRegisterUserWithoutExceptions() {
        RegisterUserDtoRequest request = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "katya2001", "katya564358");
        RegisterUserDtoResponse response = new RegisterUserDtoResponse("Ekaterina", "Rogozhina",
                "Andreevna", "katya2001");

        ResponseEntity<RegisterUserDtoResponse> responseEntity =
                template.postForEntity("http://localhost:8080/api/accounts", request, RegisterUserDtoResponse.class);

        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getHeaders().getFirst(HttpHeaders.SET_COOKIE)),
                () -> assertEquals(response, responseEntity.getBody())
        );
    }

    @Test
    public void testEditUserProfileWithInvalidData() {
        EditUserProfileDtoRequest request = new EditUserProfileDtoRequest("Ekaterina", null,
                "Andreevnaa Andreevnaa Andreevnaa Andreevnaa Andreevnaa A", // 51 symbol
                "katya20011", "katya20000");

        Set<ServerError> expectedErrors = new HashSet<>();
        expectedErrors.add(new ServerError(ServerErrorCode.INVALID_LASTNAME, "lastName",
                "Last name cannot be null"));
        expectedErrors.add(new ServerError(ServerErrorCode.INVALID_PATRONYMIC, "patronymic",
                "The patronymic of the wrong size"));
        ServerErrors expectedResponse = new ServerErrors(expectedErrors);

        try {
            template.exchange("http://localhost:8080/api/accounts", HttpMethod.PUT, new HttpEntity(request),
                    ServerErrors.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testEditUserProfileWithSessionIdNotFound() {
        EditUserProfileDtoRequest request = new EditUserProfileDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "katya20011", "katya20000");
        ServerErrors expectedResponse = createException(ServerErrorCode.THIS_SESSIONID_NOT_FOUND);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "JAVASESSIONID=" + UUID.randomUUID().toString());

        try {
            template.exchange("http://localhost:8080/api/accounts", HttpMethod.PUT, new HttpEntity(request, headers),
                    ServerErrors.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testEditUserProfileWithWrongPassword() {
        EditUserProfileDtoRequest request = new EditUserProfileDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "katya000000", "katya000012");

        RegisterUserDtoRequest requestRegister = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "katya2001", "katya564358");
        String cookie = template.postForEntity("http://localhost:8080/api/accounts", requestRegister,
                RegisterUserDtoResponse.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        ServerErrors expectedResponse = createException(ServerErrorCode.WRONG_PASSWORD);

        try {
            template.exchange("http://localhost:8080/api/accounts", HttpMethod.PUT, new HttpEntity(request, headers),
                    ServerErrors.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testEditUserProfileWithoutException() {
        EditUserProfileDtoRequest request = new EditUserProfileDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "katya000000", "katya000012");
        EditUserProfileDtoResponse response = new EditUserProfileDtoResponse(0, "Ekaterina", "Rogozhina",
                "Andreevna", "katya2001");

        RegisterUserDtoRequest requestRegister = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "katya2001", "katya000000");
        String cookie = template.postForEntity("http://localhost:8080/api/accounts", requestRegister,
                RegisterUserDtoResponse.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        ResponseEntity<EditUserProfileDtoResponse> responseEntity =
                template.exchange("http://localhost:8080/api/accounts", HttpMethod.PUT,
                        new HttpEntity(request, headers), EditUserProfileDtoResponse.class);
        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(response.getFirstName(), responseEntity.getBody().getFirstName()),
                () -> assertEquals(response.getLastName(), responseEntity.getBody().getLastName()),
                () -> assertEquals(response.getPatronymic(), responseEntity.getBody().getPatronymic()),
                () -> assertEquals(response.getLogin(), responseEntity.getBody().getLogin())
        );
    }

    @Test
    public void testTransferToSuperuserWithSessionIdNotFound() {
        ServerErrors expectedResponse = createException(ServerErrorCode.THIS_SESSIONID_NOT_FOUND);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "JAVASESSIONID=" + UUID.randomUUID().toString());

        try {
            template.exchange("http://localhost:8080/api/accounts/{id}/super", HttpMethod.PUT, new HttpEntity(headers),
                    ServerErrors.class, 0);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testTransferToSuperuserWithNotAdmin() {
        RegisterUserDtoRequest requestRegister = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "katya2001", "katya564358");
        String cookie = template.postForEntity("http://localhost:8080/api/accounts", requestRegister,
                RegisterUserDtoResponse.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        Set<ServerError> expectedErrors = new HashSet<>();
        expectedErrors.add(new ServerError(ServerErrorCode.YOU_ARE_NOT_SUPERUSER, "userType",
                ServerErrorCode.YOU_ARE_NOT_SUPERUSER.getErrorMessage()));
        ServerErrors expectedResponse = new ServerErrors(expectedErrors);

        try {
            template.exchange("http://localhost:8080/api/accounts/{id}/super", HttpMethod.PUT, new HttpEntity(headers),
                    ServerErrors.class, 0);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testDeleteUserWithSessionIdNotFound() {
        DeleteUserDtoRequest request = new DeleteUserDtoRequest( "katya20000");
        ServerErrors expectedResponse = createException(ServerErrorCode.THIS_SESSIONID_NOT_FOUND);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "JAVASESSIONID=" + UUID.randomUUID().toString());

        try {
            template.exchange("http://localhost:8080/api/accounts", HttpMethod.DELETE, new HttpEntity(request, headers),
                    ServerErrors.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testDeleteUserWithWrongPassword() {
        DeleteUserDtoRequest request = new DeleteUserDtoRequest("katya000012");

        RegisterUserDtoRequest requestRegister = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "katya2001", "katya564358");
        String cookie = template.postForEntity("http://localhost:8080/api/accounts", requestRegister,
                RegisterUserDtoResponse.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        ServerErrors expectedResponse = createException(ServerErrorCode.WRONG_PASSWORD);

        try {
            template.exchange("http://localhost:8080/api/accounts", HttpMethod.DELETE, new HttpEntity(request, headers),
                    ServerErrors.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testDeleteUserWithoutException() {
        DeleteUserDtoRequest request = new DeleteUserDtoRequest("katya564358");

        RegisterUserDtoRequest requestRegister = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "katya2001", "katya564358");
        String cookie = template.postForEntity("http://localhost:8080/api/accounts", requestRegister,
                RegisterUserDtoResponse.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);


        assertDoesNotThrow(() -> template.exchange("http://localhost:8080/api/accounts",
                HttpMethod.DELETE, new HttpEntity(request, headers), ServerErrors.class));
    }

    @Test
    public void testGetInfoOfUserWithSessionIdNotFound() {
        ServerErrors expectedResponse = createException(ServerErrorCode.THIS_SESSIONID_NOT_FOUND);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "JAVASESSIONID=" + UUID.randomUUID().toString());

        try {
            template.exchange("http://localhost:8080/api/account", HttpMethod.GET,
                    new HttpEntity<String>(headers), ServerErrors.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testGetInfoOfUserWithoutException() {
        GetInfoOfUserDtoResponse response = new GetInfoOfUserDtoResponse("Ekaterina", "Rogozhina",
                "Andreevna", "katya2001");

        RegisterUserDtoRequest requestRegister = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "katya2001", "katya000000");
        String cookie = template.postForEntity("http://localhost:8080/api/accounts", requestRegister,
                RegisterUserDtoResponse.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        ResponseEntity<GetInfoOfUserDtoResponse> responseEntity =
                template.exchange("http://localhost:8080/api/account", HttpMethod.GET,
                        new HttpEntity<String>(headers), GetInfoOfUserDtoResponse.class);
        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(response, responseEntity.getBody())
        );
    }

    private ServerErrors createException(ServerErrorCode errorCode) {
        Set<ServerError> expectedErrors = new HashSet<>();
        expectedErrors.add(new ServerError(errorCode, errorCode.getField(),
                errorCode.getErrorMessage()));
        return new ServerErrors(expectedErrors);
    }

}
