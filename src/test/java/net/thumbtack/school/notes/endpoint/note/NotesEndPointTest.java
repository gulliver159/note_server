package net.thumbtack.school.notes.endpoint.note;

import com.google.gson.Gson;
import net.thumbtack.school.notes.NotesServer;
import net.thumbtack.school.notes.dto.requests.note.*;
import net.thumbtack.school.notes.dto.requests.user.RegisterUserDtoRequest;
import net.thumbtack.school.notes.dto.responses.note.GetCommentInfoDtoResponse;
import net.thumbtack.school.notes.dto.responses.note.GetNoteInfoDtoResponse;
import net.thumbtack.school.notes.dto.responses.note.SectionDataDtoResponse;
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
class NotesEndPointTest {

    private final RestTemplate template = new RestTemplate();

    private final Gson gson = new Gson();

    private HttpHeaders headers;
    private int sectionId;

    @BeforeEach
    void setUp() {
        template.postForEntity("http://localhost:8080/api/debug/clear", null, Void.class);
        RegisterUserDtoRequest requestRegister1 = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "login0000", "password0000");
        String cookie = template.postForEntity("http://localhost:8080/api/accounts", requestRegister1,
                RegisterUserDtoResponse.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        headers = new HttpHeaders();
        headers.add("Cookie", cookie);
        SectionNameDtoRequest request = new SectionNameDtoRequest("section");
        sectionId = template.exchange("http://localhost:8080/api/sections", HttpMethod.POST, new HttpEntity<>(request, headers),
                SectionDataDtoResponse.class).getBody().getId();
    }

    @Test
    public void testCreateNoteWithSessionIdNotFound() {
        CreateNoteDtoRequest request = new CreateNoteDtoRequest("subject", "body", 0);
        ServerErrors expectedResponse = createException(ServerErrorCode.THIS_SESSIONID_NOT_FOUND);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "JAVASESSIONID=" + UUID.randomUUID().toString());

        try {
            template.exchange("http://localhost:8080/api/notes", HttpMethod.POST, new HttpEntity<>(request, headers),
                    GetNoteInfoDtoResponse.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testCreateNoteWithInvalidData() {
        CreateNoteDtoRequest request = new CreateNoteDtoRequest(null, "", null);

        Set<ServerError> expectedErrors = new HashSet<>();
        expectedErrors.add(new ServerError(ServerErrorCode.INVALID_SUBJECT, "subject",
                "The note subject cannot be null"));
        expectedErrors.add(new ServerError(ServerErrorCode.INVALID_BODY, "body",
                "The note body cannot be empty"));
        expectedErrors.add(new ServerError(ServerErrorCode.INVALID_SECTIONID, "sectionId",
                "The note sectionId cannot be null"));
        ServerErrors expectedResponse = new ServerErrors(expectedErrors);

        try {
            template.exchange("http://localhost:8080/api/notes", HttpMethod.POST, new HttpEntity<>(request, headers),
                    GetNoteInfoDtoResponse.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testCreateNoteWithSectionIdNotFound() {
        CreateNoteDtoRequest request = new CreateNoteDtoRequest("subject", "body", 0);
        ServerErrors expectedResponse = createException(ServerErrorCode.THIS_SECTION_ID_NOT_FOUND);

        try {
            template.exchange("http://localhost:8080/api/notes", HttpMethod.POST, new HttpEntity<>(request, headers),
                    GetNoteInfoDtoResponse.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testCreateNoteWithoutException() {
        GetNoteInfoDtoResponse response = new GetNoteInfoDtoResponse(0, "subject", "body", sectionId,
                0, "", 1);
        CreateNoteDtoRequest request = new CreateNoteDtoRequest("subject", "body", sectionId);

        ResponseEntity<GetNoteInfoDtoResponse> responseEntity =
                template.exchange("http://localhost:8080/api/notes", HttpMethod.POST, new HttpEntity<>(request, headers),
                        GetNoteInfoDtoResponse.class);

        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(response.getSubject(), responseEntity.getBody().getSubject()),
                () -> assertEquals(response.getBody(), responseEntity.getBody().getBody()),
                () -> assertEquals(response.getSectionId(), responseEntity.getBody().getSectionId()),
                () -> assertEquals(response.getRevisionId(), responseEntity.getBody().getRevisionId())
        );
    }

    @Test
    public void testGetNoteInfoWithNoteIdNotFound() {
        ServerErrors expectedResponse = createException(ServerErrorCode.THIS_NOTE_ID_NOT_FOUND);

        try {
            template.exchange("http://localhost:8080/api/notes/{id}", HttpMethod.GET, new HttpEntity<>(headers),
                    GetNoteInfoDtoResponse.class, 1);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testGetNoteInfoWithoutException() {
        CreateNoteDtoRequest request = new CreateNoteDtoRequest("subject", "body", sectionId);

        int noteId =
                template.exchange("http://localhost:8080/api/notes", HttpMethod.POST, new HttpEntity<>(request, headers),
                        GetNoteInfoDtoResponse.class).getBody().getId();

        GetNoteInfoDtoResponse response = new GetNoteInfoDtoResponse(noteId, "subject", "body", sectionId,
                0, "", 1);

        ResponseEntity<GetNoteInfoDtoResponse> responseEntity =
                template.exchange("http://localhost:8080/api/notes/{id}", HttpMethod.GET, new HttpEntity<>(headers),
                        GetNoteInfoDtoResponse.class, noteId);

        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(response.getId(), responseEntity.getBody().getId()),
                () -> assertEquals(response.getSubject(), responseEntity.getBody().getSubject()),
                () -> assertEquals(response.getBody(), responseEntity.getBody().getBody()),
                () -> assertEquals(response.getSectionId(), responseEntity.getBody().getSectionId()),
                () -> assertEquals(response.getRevisionId(), responseEntity.getBody().getRevisionId())
        );
    }

    @Test
    public void testEditOrTransferNoteWithBothAreNull() {
        Set<ServerError> expectedErrors = new HashSet<>();
        expectedErrors.add(new ServerError(ServerErrorCode.All_PARAMETERS_CANNOT_BE_NULL, "parameters",
                "At least one field in the request must be non null"));
        ServerErrors expectedResponse = new ServerErrors(expectedErrors);

        EditOrTransferNoteDtoRequest request = new EditOrTransferNoteDtoRequest(null, null);

        try {
            template.exchange("http://localhost:8080/api/notes/{id}", HttpMethod.PUT, new HttpEntity<>(request, headers),
                    GetNoteInfoDtoResponse.class, 1);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testEditOrTransferNoteWithNoteIdNotFound() {
        ServerErrors expectedResponse = createException(ServerErrorCode.THIS_NOTE_ID_NOT_FOUND);

        EditOrTransferNoteDtoRequest request = new EditOrTransferNoteDtoRequest("body2", null);

        try {
            template.exchange("http://localhost:8080/api/notes/{id}", HttpMethod.PUT, new HttpEntity<>(request, headers),
                    GetNoteInfoDtoResponse.class, 1);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testTransferNoteWithSectionIdNotFound() {
        ServerErrors expectedResponse = createException(ServerErrorCode.THIS_SECTION_ID_NOT_FOUND);

        CreateNoteDtoRequest requestCreateNote = new CreateNoteDtoRequest("subject", "body", sectionId);
        int noteId =
                template.exchange("http://localhost:8080/api/notes", HttpMethod.POST,
                        new HttpEntity<>(requestCreateNote, headers), GetNoteInfoDtoResponse.class).getBody().getId();

        EditOrTransferNoteDtoRequest request = new EditOrTransferNoteDtoRequest(null, 0);

        try {
            template.exchange("http://localhost:8080/api/notes/{id}", HttpMethod.PUT, new HttpEntity<>(request, headers),
                    GetNoteInfoDtoResponse.class, noteId);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testEditNoteWithNotOwner() {
        ServerErrors expectedResponse = createException(ServerErrorCode.YOU_ARE_NOT_OWNER_OF_NOTE);

        CreateNoteDtoRequest requestCreateNote = new CreateNoteDtoRequest("subject", "body", sectionId);
        int noteId =
                template.exchange("http://localhost:8080/api/notes", HttpMethod.POST,
                        new HttpEntity<>(requestCreateNote, headers), GetNoteInfoDtoResponse.class).getBody().getId();

        registerNewUser();

        EditOrTransferNoteDtoRequest request = new EditOrTransferNoteDtoRequest("body2", null);
        try {
            template.exchange("http://localhost:8080/api/notes/{id}", HttpMethod.PUT, new HttpEntity<>(request, headers),
                    GetNoteInfoDtoResponse.class, noteId);
        }  catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testTransferNoteWithNotOwnerOrAdmin() {
        ServerErrors expectedResponse = createException(ServerErrorCode.YOU_ARE_NOT_OWNER_OF_NOTE);

        CreateNoteDtoRequest requestCreateNote = new CreateNoteDtoRequest("subject", "body", sectionId);
        int noteId =
                template.exchange("http://localhost:8080/api/notes", HttpMethod.POST,
                        new HttpEntity<>(requestCreateNote, headers), GetNoteInfoDtoResponse.class).getBody().getId();

        registerNewUser();

        EditOrTransferNoteDtoRequest request = new EditOrTransferNoteDtoRequest(null, 1);
        try {
            template.exchange("http://localhost:8080/api/notes/{id}", HttpMethod.PUT, new HttpEntity<>(request, headers),
                    GetNoteInfoDtoResponse.class, noteId);
        }  catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testEditNoteWithNotOwnerWhenAdmin() {
        ServerErrors expectedResponse = createException(ServerErrorCode.YOU_ARE_NOT_OWNER_OF_NOTE);

        CreateNoteDtoRequest requestCreateNote = new CreateNoteDtoRequest("subject", "body", sectionId);
        int noteId =
                template.exchange("http://localhost:8080/api/notes", HttpMethod.POST,
                        new HttpEntity<>(requestCreateNote, headers), GetNoteInfoDtoResponse.class).getBody().getId();

        registerAdmin();

        EditOrTransferNoteDtoRequest request = new EditOrTransferNoteDtoRequest("body2", null);
        try {
            template.exchange("http://localhost:8080/api/notes/{id}", HttpMethod.PUT, new HttpEntity<>(request, headers),
                    GetNoteInfoDtoResponse.class, noteId);
        }  catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testTransferNoteWithoutExceptionWhenAdmin() {
        CreateNoteDtoRequest requestCreateNote = new CreateNoteDtoRequest("subject", "body", sectionId);
        int noteId =
                template.exchange("http://localhost:8080/api/notes", HttpMethod.POST,
                        new HttpEntity<>(requestCreateNote, headers), GetNoteInfoDtoResponse.class).getBody().getId();
        SectionNameDtoRequest requestCreateSection = new SectionNameDtoRequest("section2");
        int sectionId2 = template.exchange("http://localhost:8080/api/sections", HttpMethod.POST,
                new HttpEntity<>(requestCreateSection, headers), SectionDataDtoResponse.class).getBody().getId();

        GetNoteInfoDtoResponse response = new GetNoteInfoDtoResponse(noteId, "subject", "body", sectionId2,
                0, "", 1);

        registerAdmin();

        EditOrTransferNoteDtoRequest request = new EditOrTransferNoteDtoRequest(null, sectionId2);

        ResponseEntity<GetNoteInfoDtoResponse> responseEntity =
                template.exchange("http://localhost:8080/api/notes/{id}", HttpMethod.PUT, new HttpEntity<>(request, headers),
                        GetNoteInfoDtoResponse.class, noteId);

        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(response.getId(), responseEntity.getBody().getId()),
                () -> assertEquals(response.getSubject(), responseEntity.getBody().getSubject()),
                () -> assertEquals(response.getBody(), responseEntity.getBody().getBody()),
                () -> assertEquals(response.getSectionId(), responseEntity.getBody().getSectionId()),
                () -> assertEquals(response.getRevisionId(), responseEntity.getBody().getRevisionId())
        );
    }

    @Test
    public void testEditOrTransferNoteWithoutException() {
        CreateNoteDtoRequest requestCreateNote = new CreateNoteDtoRequest("subject", "body", sectionId);
        int noteId =
                template.exchange("http://localhost:8080/api/notes", HttpMethod.POST,
                        new HttpEntity<>(requestCreateNote, headers), GetNoteInfoDtoResponse.class).getBody().getId();
        SectionNameDtoRequest requestCreateSection = new SectionNameDtoRequest("section2");
        int sectionId2 = template.exchange("http://localhost:8080/api/sections", HttpMethod.POST,
                new HttpEntity<>(requestCreateSection, headers), SectionDataDtoResponse.class).getBody().getId();

        GetNoteInfoDtoResponse response = new GetNoteInfoDtoResponse(noteId, "subject", "body2", sectionId2,
                0, "", 2);

        EditOrTransferNoteDtoRequest request = new EditOrTransferNoteDtoRequest("body2", sectionId2);

        ResponseEntity<GetNoteInfoDtoResponse> responseEntity =
                template.exchange("http://localhost:8080/api/notes/{id}", HttpMethod.PUT, new HttpEntity<>(request, headers),
                        GetNoteInfoDtoResponse.class, noteId);

        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(response.getId(), responseEntity.getBody().getId()),
                () -> assertEquals(response.getSubject(), responseEntity.getBody().getSubject()),
                () -> assertEquals(response.getBody(), responseEntity.getBody().getBody()),
                () -> assertEquals(response.getSectionId(), responseEntity.getBody().getSectionId()),
                () -> assertEquals(response.getRevisionId(), responseEntity.getBody().getRevisionId())
        );
    }

    @Test
    public void testDeleteNoteWithNoteIdNotFound() {
        ServerErrors expectedResponse = createException(ServerErrorCode.THIS_NOTE_ID_NOT_FOUND);

        try {
            template.exchange("http://localhost:8080/api/notes/{id}", HttpMethod.DELETE, new HttpEntity(headers),
                    ServerErrors.class, 1);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testDeleteNoteWithNotOwner() {
        ServerErrors expectedResponse = createException(ServerErrorCode.YOU_ARE_NOT_OWNER_OF_NOTE);

        CreateNoteDtoRequest requestCreateNote = new CreateNoteDtoRequest("subject", "body", sectionId);
        int noteId =
                template.exchange("http://localhost:8080/api/notes", HttpMethod.POST,
                        new HttpEntity<>(requestCreateNote, headers), GetNoteInfoDtoResponse.class).getBody().getId();

        registerNewUser();

        try {
            template.exchange("http://localhost:8080/api/notes/{id}", HttpMethod.DELETE, new HttpEntity(headers),
                    ServerErrors.class, noteId);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testDeleteNoteWithoutExceptionWhenAdmin() {
        CreateNoteDtoRequest requestCreateNote = new CreateNoteDtoRequest("subject", "body", sectionId);
        int noteId =
                template.exchange("http://localhost:8080/api/notes", HttpMethod.POST,
                        new HttpEntity<>(requestCreateNote, headers), GetNoteInfoDtoResponse.class).getBody().getId();
        registerAdmin();

        assertDoesNotThrow(
                () -> template.exchange("http://localhost:8080/api/notes/{id}", HttpMethod.DELETE,
                        new HttpEntity<>(headers), GetNoteInfoDtoResponse.class, noteId));
    }

    @Test
    public void testDeleteNoteWithoutException() {
        CreateNoteDtoRequest requestCreateNote = new CreateNoteDtoRequest("subject", "body", sectionId);
        int noteId =
                template.exchange("http://localhost:8080/api/notes", HttpMethod.POST,
                        new HttpEntity<>(requestCreateNote, headers), GetNoteInfoDtoResponse.class).getBody().getId();
        assertDoesNotThrow(
                () -> template.exchange("http://localhost:8080/api/notes/{id}", HttpMethod.DELETE,
                            new HttpEntity<>(headers), GetNoteInfoDtoResponse.class, noteId));

        ServerErrors expectedResponse = createException(ServerErrorCode.THIS_NOTE_ID_NOT_FOUND);

        try {
            template.exchange("http://localhost:8080/api/notes/{id}", HttpMethod.DELETE, new HttpEntity(headers),
                    ServerErrors.class, noteId);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testRateNoteWithInvalidData() {
        Set<ServerError> expectedErrors = new HashSet<>();
        expectedErrors.add(new ServerError(ServerErrorCode.INVALID_RATING, "rating",
                "The rating should not be more than 5"));
        ServerErrors expectedResponse = new ServerErrors(expectedErrors);

        RateNoteDtoRequest request = new RateNoteDtoRequest(7);

        try {
            template.exchange("http://localhost:8080/api/notes/{id}/rating", HttpMethod.POST,
                    new HttpEntity<>(request, headers), ServerErrors.class, 1);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testRateNoteWithNoteIdNotFound() {
        ServerErrors expectedResponse = createException(ServerErrorCode.THIS_NOTE_ID_NOT_FOUND);

        RateNoteDtoRequest request = new RateNoteDtoRequest(4);

        try {
            template.exchange("http://localhost:8080/api/notes/{id}/rating", HttpMethod.POST,
                    new HttpEntity<>(request, headers), ServerErrors.class, 1);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testRateNoteWithOwner() {
        ServerErrors expectedResponse = createException(ServerErrorCode.YOU_CANT_RATE_YOUR_NOTE);

        CreateNoteDtoRequest requestCreateNote = new CreateNoteDtoRequest("subject", "body", sectionId);
        int noteId =
                template.exchange("http://localhost:8080/api/notes", HttpMethod.POST,
                        new HttpEntity<>(requestCreateNote, headers), GetNoteInfoDtoResponse.class).getBody().getId();

        RateNoteDtoRequest request = new RateNoteDtoRequest(4);

        try {
            template.exchange("http://localhost:8080/api/notes/{id}/rating", HttpMethod.POST,
                    new HttpEntity<>(request, headers), ServerErrors.class, noteId);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }


    @Test
    public void testRateNoteWithoutException() {
        CreateNoteDtoRequest requestCreateNote = new CreateNoteDtoRequest("subject", "body", sectionId);
        int noteId =
                template.exchange("http://localhost:8080/api/notes", HttpMethod.POST,
                        new HttpEntity<>(requestCreateNote, headers), GetNoteInfoDtoResponse.class).getBody().getId();

        registerNewUser();

        RateNoteDtoRequest request = new RateNoteDtoRequest(4);
        assertDoesNotThrow(
                () -> template.exchange("http://localhost:8080/api/notes/{id}/rating", HttpMethod.POST,
                        new HttpEntity<>(request, headers), Void.class, noteId));

        List<GetUsersDtoResponse> expectedList = new ArrayList<>();
        expectedList.add(new GetUsersDtoResponse(0, "Ekaterina", "Rogozhina",
                "Andreevna", "login0000", "", true, false, null, 4));
        expectedList.add(new GetUsersDtoResponse(0, "Ekaterina", "Rogozhina",
                "Andreevna", "login0001", "", true, false, null, 0));

        ResponseEntity<List<GetUsersDtoResponse>> responseEntity =
                template.exchange("http://localhost:8080/api/accounts", HttpMethod.GET,
                        new HttpEntity<String>(headers), new ParameterizedTypeReference<List<GetUsersDtoResponse>>(){});
        List<GetUsersDtoResponse> actualList = responseEntity.getBody();
        actualList.sort(Comparator.comparingInt(GetUsersDtoResponse::getId));
        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(expectedList, actualList)
        );
    }



    private ServerErrors createException(ServerErrorCode errorCode) {
        Set<ServerError> expectedErrors = new HashSet<>();
        expectedErrors.add(new ServerError(errorCode, errorCode.getField(),
                errorCode.getErrorMessage()));
        return new ServerErrors(expectedErrors);
    }

    private void registerNewUser() {
        RegisterUserDtoRequest requestRegister = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "login0001", "password0000");
        String cookie = template.postForEntity("http://localhost:8080/api/accounts", requestRegister,
                RegisterUserDtoResponse.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        headers = new HttpHeaders();
        headers.add("Cookie", cookie);
    }

    private void registerAdmin() {
        RegisterUserDtoRequest requestRegister = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "login0001", "password0000");
        String cookie = template.postForEntity("http://localhost:8080/api/debug/registerAdmin", requestRegister,
                RegisterUserDtoResponse.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        headers = new HttpHeaders();
        headers.add("Cookie", cookie);
    }

}