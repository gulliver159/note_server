package net.thumbtack.school.notes.endpoint.note;

import com.google.gson.Gson;
import net.thumbtack.school.notes.NotesServer;
import net.thumbtack.school.notes.dto.requests.note.SectionNameDtoRequest;
import net.thumbtack.school.notes.dto.requests.user.AddToListDtoRequest;
import net.thumbtack.school.notes.dto.requests.user.DeleteUserDtoRequest;
import net.thumbtack.school.notes.dto.requests.user.EditUserProfileDtoRequest;
import net.thumbtack.school.notes.dto.requests.user.RegisterUserDtoRequest;
import net.thumbtack.school.notes.dto.responses.note.SectionDataDtoResponse;
import net.thumbtack.school.notes.dto.responses.user.EditUserProfileDtoResponse;
import net.thumbtack.school.notes.dto.responses.user.GetInfoOfUserDtoResponse;
import net.thumbtack.school.notes.dto.responses.user.GetUsersDtoResponse;
import net.thumbtack.school.notes.dto.responses.user.RegisterUserDtoResponse;
import net.thumbtack.school.notes.exception.ServerError;
import net.thumbtack.school.notes.exception.ServerErrorCode;
import net.thumbtack.school.notes.exception.ServerErrors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes= NotesServer.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class SectionsEndPointTest {

    private final RestTemplate template = new RestTemplate();

    private final Gson gson = new Gson();

    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        template.postForEntity("http://localhost:8080/api/debug/clear", null, Object.class);
        RegisterUserDtoRequest requestRegister1 = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "login0000", "password0000");
        String cookie = template.postForEntity("http://localhost:8080/api/accounts", requestRegister1,
                RegisterUserDtoResponse.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        headers = new HttpHeaders();
        headers.add("Cookie", cookie);
    }

    @Test
    public void testAddToFollowingsWithSessionIdNotFound() {
        SectionNameDtoRequest request = new SectionNameDtoRequest("section");
        ServerErrors expectedResponse = createException(ServerErrorCode.THIS_SESSIONID_NOT_FOUND);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "JAVASESSIONID=" + UUID.randomUUID().toString());

        try {
            template.postForEntity("http://localhost:8080/api/sections", new HttpEntity(request, headers),
                    ServerErrors.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testCreateSectionWithInvalidName() {
        SectionNameDtoRequest request = new SectionNameDtoRequest("section.");

        Set<ServerError> expectedErrors = new HashSet<>();
        expectedErrors.add(new ServerError(ServerErrorCode.INVALID_NAME, "name",
                "The section name can only contain Latin and Russian letters, numbers, spaces, and underscores"));
        ServerErrors expectedResponse = new ServerErrors(expectedErrors);

        try {
            template.exchange("http://localhost:8080/api/sections", HttpMethod.POST, new HttpEntity<>(request, headers),
                    SectionDataDtoResponse.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testCreateSectionWithBusyName() {
        SectionNameDtoRequest request = new SectionNameDtoRequest("section");

        ServerErrors expectedResponse = createException(ServerErrorCode.SECTION_NAME_ALREADY_BUSY);

        template.exchange("http://localhost:8080/api/sections", HttpMethod.POST, new HttpEntity<>(request, headers),
                SectionDataDtoResponse.class);

        try {
            template.exchange("http://localhost:8080/api/sections", HttpMethod.POST, new HttpEntity<>(request, headers),
                    SectionDataDtoResponse.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testCreateSectionWithoutException() {
        SectionDataDtoResponse response = new SectionDataDtoResponse(0, "section");
        SectionNameDtoRequest request = new SectionNameDtoRequest("section");

        ResponseEntity<SectionDataDtoResponse> responseEntity =
                template.exchange("http://localhost:8080/api/sections", HttpMethod.POST, new HttpEntity<>(request, headers),
                        SectionDataDtoResponse.class);

        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(response.getName(), responseEntity.getBody().getName())
        );
    }

    @Test
    public void testRenameSectionWithSectionIdNotFound() {
        SectionNameDtoRequest request = new SectionNameDtoRequest("section");

        ServerErrors expectedResponse = createException(ServerErrorCode.THIS_SECTION_ID_NOT_FOUND);

        try {
            template.exchange("http://localhost:8080/api/sections/{id}", HttpMethod.PUT, new HttpEntity<>(request, headers),
                        SectionDataDtoResponse.class, 1);
        }  catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testRenameSectionWithBusyName() {
        SectionNameDtoRequest request1 = new SectionNameDtoRequest("section1");
        template.exchange("http://localhost:8080/api/sections", HttpMethod.POST, new HttpEntity<>(request1, headers),
                        SectionDataDtoResponse.class);
        SectionNameDtoRequest request2 = new SectionNameDtoRequest("section2");
        int sectionId =
                template.exchange("http://localhost:8080/api/sections", HttpMethod.POST, new HttpEntity<>(request2, headers),
                    SectionDataDtoResponse.class).getBody().getId();

        ServerErrors expectedResponse = createException(ServerErrorCode.SECTION_NAME_ALREADY_BUSY);

        try {
            template.exchange("http://localhost:8080/api/sections/{id}", HttpMethod.PUT, new HttpEntity<>(request1, headers),
                    SectionDataDtoResponse.class, sectionId);
        }  catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testRenameSectionWithNotOwner() {
        SectionNameDtoRequest request = new SectionNameDtoRequest("section2");

        ServerErrors expectedResponse = createException(ServerErrorCode.YOU_ARE_NOT_OWNER_OF_SECTION);

        int sectionId =
                template.exchange("http://localhost:8080/api/sections", HttpMethod.POST, new HttpEntity<>(request, headers),
                        SectionDataDtoResponse.class).getBody().getId();

        RegisterUserDtoRequest requestRegister = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "login0001", "password0000");
        String cookie = template.postForEntity("http://localhost:8080/api/accounts", requestRegister,
                RegisterUserDtoResponse.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        try {
            template.exchange("http://localhost:8080/api/sections/{id}", HttpMethod.PUT, new HttpEntity<>(request, headers),
                    SectionDataDtoResponse.class, sectionId);
        }  catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testRenameSectionWithoutException() {
        SectionDataDtoResponse response = new SectionDataDtoResponse(0, "section2");
        SectionNameDtoRequest request = new SectionNameDtoRequest("section2");

        int sectionId =
                template.exchange("http://localhost:8080/api/sections", HttpMethod.POST, new HttpEntity<>(request, headers),
                        SectionDataDtoResponse.class).getBody().getId();

        ResponseEntity<SectionDataDtoResponse> responseEntity =
                template.exchange("http://localhost:8080/api/sections/{id}", HttpMethod.PUT, new HttpEntity<>(request, headers),
                        SectionDataDtoResponse.class, sectionId);

        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(response.getName(), responseEntity.getBody().getName())
        );
    }

    @Test
    public void testDeleteSectionWithSectionIdNotFound() {
        ServerErrors expectedResponse = createException(ServerErrorCode.THIS_SECTION_ID_NOT_FOUND);

        try {
            template.exchange("http://localhost:8080/api/sections/{id}", HttpMethod.DELETE, new HttpEntity<>(headers),
                    Void.class, 1);
        }  catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testDeleteSectionWithNotOwner() {
        ServerErrors expectedResponse = createException(ServerErrorCode.YOU_ARE_NOT_OWNER_OF_SECTION);
        SectionNameDtoRequest request = new SectionNameDtoRequest("section2");
        int sectionId =
                template.exchange("http://localhost:8080/api/sections", HttpMethod.POST, new HttpEntity<>(request, headers),
                        SectionDataDtoResponse.class).getBody().getId();

        RegisterUserDtoRequest requestRegister = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "login0001", "password0000");
        String cookie = template.postForEntity("http://localhost:8080/api/accounts", requestRegister,
                RegisterUserDtoResponse.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        try {
            template.exchange("http://localhost:8080/api/sections/{id}", HttpMethod.DELETE, new HttpEntity<>(headers),
                    SectionDataDtoResponse.class, sectionId);
        }  catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testDeleteSectionWithoutExceptionWhenAdmin() {
        SectionNameDtoRequest request = new SectionNameDtoRequest("section2");

        int sectionId =
                template.exchange("http://localhost:8080/api/sections", HttpMethod.POST, new HttpEntity<>(request, headers),
                        SectionDataDtoResponse.class).getBody().getId();

        RegisterUserDtoRequest requestRegister = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "login0001", "password0000");
        String cookie = template.postForEntity("http://localhost:8080/api/debug/registerAdmin", requestRegister,
                RegisterUserDtoResponse.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        assertDoesNotThrow(
                () -> template.exchange("http://localhost:8080/api/sections/{id}", HttpMethod.DELETE,
                new HttpEntity(headers), Void.class, sectionId));
    }

    @Test
    public void testDeleteSectionWithoutException() {
        SectionNameDtoRequest request = new SectionNameDtoRequest("section2");

        int sectionId =
                template.exchange("http://localhost:8080/api/sections", HttpMethod.POST, new HttpEntity<>(request, headers),
                        SectionDataDtoResponse.class).getBody().getId();

        assertDoesNotThrow(
                () -> template.exchange("http://localhost:8080/api/sections/{id}", HttpMethod.DELETE,
                        new HttpEntity(headers), Void.class, sectionId));
    }

    @Test
    public void testGetSectionInfoWithSectionIdNotFound() {
        ServerErrors expectedResponse = createException(ServerErrorCode.THIS_SECTION_ID_NOT_FOUND);

        try {
            template.exchange("http://localhost:8080/api/sections/{id}", HttpMethod.GET, new HttpEntity<>(headers),
                    Void.class, 1);
        }  catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testGetSectionInfoWithoutException() {
        SectionDataDtoResponse response = new SectionDataDtoResponse(0, "section2");
        SectionNameDtoRequest request = new SectionNameDtoRequest("section2");

        int sectionId =
                template.exchange("http://localhost:8080/api/sections", HttpMethod.POST, new HttpEntity<>(request, headers),
                        SectionDataDtoResponse.class).getBody().getId();

        ResponseEntity<SectionDataDtoResponse> responseEntity =
                template.exchange("http://localhost:8080/api/sections/{id}", HttpMethod.GET, new HttpEntity<>(headers),
                        SectionDataDtoResponse.class, sectionId);

        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(response.getName(), responseEntity.getBody().getName())
        );
    }

    @Test
    public void testGetSectionListWithSessionIdNotFound() {
        ServerErrors expectedResponse = createException(ServerErrorCode.THIS_SESSIONID_NOT_FOUND);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "JAVASESSIONID=" + UUID.randomUUID().toString());

        try {
            template.exchange("http://localhost:8080/api/sections", HttpMethod.GET, new HttpEntity(headers),
                    ServerErrors.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testGetSectionList() {
        List<SectionDataDtoResponse> response = new ArrayList<>();
        response.add(new SectionDataDtoResponse(0, "section1"));
        response.add(new SectionDataDtoResponse(0, "section2"));
        SectionNameDtoRequest request1 = new SectionNameDtoRequest("section1");
        SectionNameDtoRequest request2 = new SectionNameDtoRequest("section2");

        template.exchange("http://localhost:8080/api/sections", HttpMethod.POST, new HttpEntity<>(request1, headers),
                        SectionDataDtoResponse.class);
        template.exchange("http://localhost:8080/api/sections", HttpMethod.POST, new HttpEntity<>(request2, headers),
                SectionDataDtoResponse.class);

        ResponseEntity<List<SectionDataDtoResponse>> responseEntity =
                template.exchange("http://localhost:8080/api/sections", HttpMethod.GET, new HttpEntity<>(headers),
                        new ParameterizedTypeReference<List<SectionDataDtoResponse>>(){});

        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(response.get(0).getName(), responseEntity.getBody().get(0).getName()),
                () -> assertEquals(response.get(1).getName(), responseEntity.getBody().get(1).getName()),
                () -> assertEquals(2, responseEntity.getBody().size())
        );
    }

    private ServerErrors createException(ServerErrorCode errorCode) {
        Set<ServerError> expectedErrors = new HashSet<>();
        expectedErrors.add(new ServerError(errorCode, errorCode.getField(),
                errorCode.getErrorMessage()));
        return new ServerErrors(expectedErrors);
    }

}
