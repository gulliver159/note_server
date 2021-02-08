package net.thumbtack.school.notes.endpoint.note;

import com.google.gson.Gson;
import net.thumbtack.school.notes.NotesServer;
import net.thumbtack.school.notes.dto.requests.note.*;
import net.thumbtack.school.notes.dto.requests.user.LoginDtoRequest;
import net.thumbtack.school.notes.dto.requests.user.RegisterUserDtoRequest;
import net.thumbtack.school.notes.dto.responses.note.*;
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
class CommentsEndPointTest {

    private final RestTemplate template = new RestTemplate();

    private final Gson gson = new Gson();

    private HttpHeaders headers;
    private int sectionId;
    private int noteId;
    private int authorId;

    @BeforeEach
    void setUp() {
        template.postForEntity("http://localhost:8080/api/debug/clear", null, Object.class);
        RegisterUserDtoRequest requestRegister1 = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "login0000", "password0000");
        String cookie = template.postForEntity("http://localhost:8080/api/accounts", requestRegister1,
                RegisterUserDtoResponse.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        headers = new HttpHeaders();
        headers.add("Cookie", cookie);
        SectionNameDtoRequest request1 = new SectionNameDtoRequest("section");
        sectionId = template.exchange("http://localhost:8080/api/sections", HttpMethod.POST, new HttpEntity<>(request1, headers),
                        SectionDataDtoResponse.class).getBody().getId();
        CreateNoteDtoRequest request2 = new CreateNoteDtoRequest("subject", "revision1", sectionId);
        GetNoteInfoDtoResponse response = template.exchange("http://localhost:8080/api/notes",
                HttpMethod.POST, new HttpEntity<>(request2, headers), GetNoteInfoDtoResponse.class).getBody();
        noteId = response.getId(); authorId = response.getAuthorId();
    }

    @Test
    public void testCreateCommentWithSessionIdNotFound() {
        CreateCommentDtoRequest request = new CreateCommentDtoRequest("body", noteId);

        ServerErrors expectedResponse = createException(ServerErrorCode.THIS_SESSIONID_NOT_FOUND);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "JAVASESSIONID=" + UUID.randomUUID().toString());

        try {
            template.exchange("http://localhost:8080/api/comments", HttpMethod.POST, new HttpEntity<>(request, headers),
                    GetCommentInfoDtoResponse.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testCreateCommentWithInvalidData() {
        CreateCommentDtoRequest request = new CreateCommentDtoRequest("", null);

        Set<ServerError> expectedErrors = new HashSet<>();
        expectedErrors.add(new ServerError(ServerErrorCode.INVALID_NOTEID, "noteId",
                "The note id cannot be null"));
        expectedErrors.add(new ServerError(ServerErrorCode.INVALID_BODY, "body",
                "The note body cannot be empty"));
        ServerErrors expectedResponse = new ServerErrors(expectedErrors);

        try {
            template.exchange("http://localhost:8080/api/comments", HttpMethod.POST, new HttpEntity<>(request, headers),
                    GetCommentInfoDtoResponse.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testCreateCommentWithNoteIdNotFound() {
        CreateCommentDtoRequest request = new CreateCommentDtoRequest("body", 0);

        ServerErrors expectedResponse = createException(ServerErrorCode.THIS_NOTE_ID_NOT_FOUND);

        try {
            template.exchange("http://localhost:8080/api/comments", HttpMethod.POST, new HttpEntity<>(request, headers),
                    GetCommentInfoDtoResponse.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testCreateCommentWithoutException() {
        GetCommentInfoDtoResponse response = new GetCommentInfoDtoResponse(0, "body", noteId,
                authorId, 1, "");
        CreateCommentDtoRequest request = new CreateCommentDtoRequest("body", noteId);

        ResponseEntity<GetCommentInfoDtoResponse> responseEntity =
                template.exchange("http://localhost:8080/api/comments", HttpMethod.POST, new HttpEntity<>(request, headers),
                        GetCommentInfoDtoResponse.class);

        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(response.getBody(), responseEntity.getBody().getBody()),
                () -> assertEquals(response.getNoteId(), responseEntity.getBody().getNoteId()),
                () -> assertEquals(response.getAuthorId(), responseEntity.getBody().getAuthorId()),
                () -> assertEquals(response.getRevisionId(), responseEntity.getBody().getRevisionId()),
                () -> assertNotNull(responseEntity.getBody().getCreated())
        );
    }

    @Test
    public void testGetCommentsListWithNoteIdNotFound() {
        ServerErrors expectedResponse = createException(ServerErrorCode.THIS_NOTE_ID_NOT_FOUND);

        try {
            template.exchange("http://localhost:8080/api/notes/{id}/comments", HttpMethod.GET, new HttpEntity<>(headers),
                    ServerErrors.class, 0);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testGetCommentsListWithoutException() {
        CreateCommentDtoRequest request1 = new CreateCommentDtoRequest("body1", noteId);
        CreateCommentDtoRequest request2 = new CreateCommentDtoRequest("body2", noteId);

        List<GetCommentInfoDtoResponse> expectedResponse = new ArrayList<>();
        expectedResponse.add(new GetCommentInfoDtoResponse(0, "body1", noteId, authorId, 1, ""));
        expectedResponse.add(new GetCommentInfoDtoResponse(0, "body2", noteId, authorId, 2, ""));

        template.exchange("http://localhost:8080/api/comments", HttpMethod.POST, new HttpEntity<>(request1, headers),
                        GetCommentInfoDtoResponse.class);

        EditOrTransferNoteDtoRequest request = new EditOrTransferNoteDtoRequest("revision2", null);
        template.exchange("http://localhost:8080/api/notes/{id}", HttpMethod.PUT, new HttpEntity<>(request, headers),
                GetNoteInfoDtoResponse.class, noteId);

        template.exchange("http://localhost:8080/api/comments", HttpMethod.POST, new HttpEntity<>(request2, headers),
                GetCommentInfoDtoResponse.class);


        ResponseEntity<List<GetCommentInfoDtoResponse>> responseEntity =
                template.exchange("http://localhost:8080/api/notes/{id}/comments", HttpMethod.GET, new HttpEntity<>(headers),
                        new ParameterizedTypeReference<List<GetCommentInfoDtoResponse>>(){}, noteId);

        List<GetCommentInfoDtoResponse> actualList = responseEntity.getBody();
        actualList.sort(Comparator.comparingInt(GetCommentInfoDtoResponse::getId));
        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(expectedResponse, actualList)
        );
    }

    @Test
    public void testEditCommentWithInvalidData() {
        Set<ServerError> expectedErrors = new HashSet<>();
        expectedErrors.add(new ServerError(ServerErrorCode.INVALID_BODY, "body",
                "The comment body cannot be empty"));
        ServerErrors expectedResponse = new ServerErrors(expectedErrors);

        EditCommentDtoRequest request = new EditCommentDtoRequest("");

        try {
            template.exchange("http://localhost:8080/api/comments/{id}", HttpMethod.PUT,
                    new HttpEntity<>(request, headers), GetCommentInfoDtoResponse.class, 1);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testEditCommentWithCommentIdNotFound() {
        ServerErrors expectedResponse = createException(ServerErrorCode.THIS_COMMENT_ID_NOT_FOUND);

        EditCommentDtoRequest request = new EditCommentDtoRequest("newBody");

        try {
            template.exchange("http://localhost:8080/api/comments/{id}", HttpMethod.PUT,
                    new HttpEntity<>(request, headers), GetCommentInfoDtoResponse.class, 1);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testEditCommentWithNotOwner() {
        ServerErrors expectedResponse = createException(ServerErrorCode.YOU_ARE_NOT_OWNER_OF_COMMENT);

        CreateCommentDtoRequest requestCreate = new CreateCommentDtoRequest("body", noteId);
        int commentId = template.exchange("http://localhost:8080/api/comments", HttpMethod.POST,
                new HttpEntity<>(requestCreate, headers), GetCommentInfoDtoResponse.class).getBody().getId();

        registerNewUser();

        EditCommentDtoRequest request = new EditCommentDtoRequest("newBody");

        try {
            template.exchange("http://localhost:8080/api/comments/{id}", HttpMethod.PUT,
                    new HttpEntity<>(request, headers), GetCommentInfoDtoResponse.class, commentId);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testEditCommentWithNotOwnerWhenAdmin() {
        ServerErrors expectedResponse = createException(ServerErrorCode.YOU_ARE_NOT_OWNER_OF_COMMENT);

        CreateCommentDtoRequest requestCreate = new CreateCommentDtoRequest("body", noteId);
        int commentId = template.exchange("http://localhost:8080/api/comments", HttpMethod.POST,
                new HttpEntity<>(requestCreate, headers), GetCommentInfoDtoResponse.class).getBody().getId();

        registerAdmin();

        EditCommentDtoRequest request = new EditCommentDtoRequest("newBody");

        try {
            template.exchange("http://localhost:8080/api/comments/{id}", HttpMethod.PUT,
                    new HttpEntity<>(request, headers), GetCommentInfoDtoResponse.class, commentId);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testEditCommentWithoutException() {
        GetCommentInfoDtoResponse expectedResponse = new GetCommentInfoDtoResponse(0, "newBody", noteId, authorId, 1, "");

        CreateCommentDtoRequest requestCreate = new CreateCommentDtoRequest("body", noteId);
        int commentId = template.exchange("http://localhost:8080/api/comments", HttpMethod.POST,
                new HttpEntity<>(requestCreate, headers), GetCommentInfoDtoResponse.class).getBody().getId();

        EditCommentDtoRequest request = new EditCommentDtoRequest("newBody");
        ResponseEntity<GetCommentInfoDtoResponse> responseEntity =
                template.exchange("http://localhost:8080/api/comments/{id}", HttpMethod.PUT,
                        new HttpEntity<>(request, headers), GetCommentInfoDtoResponse.class, commentId);
        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(expectedResponse, responseEntity.getBody())
        );
    }

    @Test
    public void testDeleteCommentWithCommentIdNotFound() {
        ServerErrors expectedResponse = createException(ServerErrorCode.THIS_COMMENT_ID_NOT_FOUND);

        try {
            template.exchange("http://localhost:8080/api/comments/{id}", HttpMethod.DELETE, new HttpEntity<>(headers),
                    Void.class, 1);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testDeleteCommentWithNotOwner() {
        CreateCommentDtoRequest request1 = new CreateCommentDtoRequest("body", noteId);
        int commentId = template.exchange("http://localhost:8080/api/comments", HttpMethod.POST, new HttpEntity<>(request1, headers),
                GetCommentInfoDtoResponse.class).getBody().getId();

        ServerErrors expectedResponse = createException(ServerErrorCode.YOU_ARE_NOT_OWNER_OF_COMMENT_OR_NOTE);

        registerNewUser();

        try {
            template.exchange("http://localhost:8080/api/comments/{id}", HttpMethod.DELETE, new HttpEntity<>(headers),
                    Void.class, commentId);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testDeleteCommentWithoutExceptionWhenOwnerNote() {
        registerNewUser();

        CreateCommentDtoRequest request1 = new CreateCommentDtoRequest("body", noteId);
        int commentId = template.exchange("http://localhost:8080/api/comments", HttpMethod.POST, new HttpEntity<>(request1, headers),
                GetCommentInfoDtoResponse.class).getBody().getId();

        LoginDtoRequest loginRequest = new LoginDtoRequest("login0000", "password0000");
        String cookie = template.postForEntity("http://localhost:8080/api/sessions", loginRequest,
                Void.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        headers.add("Cookie", cookie);

        assertDoesNotThrow(
                () -> template.exchange("http://localhost:8080/api/comments/{id}", HttpMethod.DELETE, new HttpEntity<>(headers),
                        Void.class, commentId));
    }

    @Test
    public void testDeleteCommentWithoutExceptionWhenAdmin() {
        CreateCommentDtoRequest request1 = new CreateCommentDtoRequest("body", noteId);
        int commentId = template.exchange("http://localhost:8080/api/comments", HttpMethod.POST, new HttpEntity<>(request1, headers),
                GetCommentInfoDtoResponse.class).getBody().getId();

        registerAdmin();

        assertDoesNotThrow(
                () -> template.exchange("http://localhost:8080/api/comments/{id}", HttpMethod.DELETE, new HttpEntity<>(headers),
                        Void.class, commentId));
    }

    @Test
    public void testDeleteCommentWithoutExceptionWhenOwnerComment() {
        CreateCommentDtoRequest request1 = new CreateCommentDtoRequest("body", noteId);
        int commentId = template.exchange("http://localhost:8080/api/comments", HttpMethod.POST, new HttpEntity<>(request1, headers),
                GetCommentInfoDtoResponse.class).getBody().getId();

        assertDoesNotThrow(
                () -> template.exchange("http://localhost:8080/api/comments/{id}", HttpMethod.DELETE, new HttpEntity<>(headers),
                        Void.class, commentId));

        ServerErrors expectedResponse = createException(ServerErrorCode.THIS_COMMENT_ID_NOT_FOUND);

        try {
            template.exchange("http://localhost:8080/api/comments/{id}", HttpMethod.DELETE, new HttpEntity<>(headers),
                    Void.class, commentId);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testDeleteCommentsNoteWithNoteIdNotFound() {
        ServerErrors expectedResponse = createException(ServerErrorCode.THIS_NOTE_ID_NOT_FOUND);

        try {
            template.exchange("http://localhost:8080/api/notes/{id}/comments", HttpMethod.DELETE,
                    new HttpEntity<>(headers), Void.class, 0);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testDeleteCommentsNoteWithNotOwner() {
        ServerErrors expectedResponse = createException(ServerErrorCode.YOU_ARE_NOT_OWNER_OF_NOTE);

        registerNewUser();

        try {
            template.exchange("http://localhost:8080/api/notes/{id}/comments", HttpMethod.DELETE,
                    new HttpEntity<>(headers), Void.class, noteId);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testDeleteCommentsNoteWithNotOwnerWhenAdmin() {
        ServerErrors expectedResponse = createException(ServerErrorCode.YOU_ARE_NOT_OWNER_OF_NOTE);

        registerAdmin();

        try {
            template.exchange("http://localhost:8080/api/notes/{id}/comments", HttpMethod.DELETE,
                    new HttpEntity<>(headers), Void.class, noteId);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testDeleteCommentsNote() {
        CreateCommentDtoRequest request1 = new CreateCommentDtoRequest("body1", noteId);
        CreateCommentDtoRequest request2 = new CreateCommentDtoRequest("body2", noteId);
        CreateCommentDtoRequest request3 = new CreateCommentDtoRequest("body3", noteId);

        template.exchange("http://localhost:8080/api/comments", HttpMethod.POST, new HttpEntity<>(request1, headers),
                GetCommentInfoDtoResponse.class);

        EditOrTransferNoteDtoRequest request = new EditOrTransferNoteDtoRequest("revision2", null);
        template.exchange("http://localhost:8080/api/notes/{id}", HttpMethod.PUT, new HttpEntity<>(request, headers),
                GetNoteInfoDtoResponse.class, noteId);

        template.exchange("http://localhost:8080/api/comments", HttpMethod.POST, new HttpEntity<>(request2, headers),
                GetCommentInfoDtoResponse.class);
        template.exchange("http://localhost:8080/api/comments", HttpMethod.POST, new HttpEntity<>(request3, headers),
                GetCommentInfoDtoResponse.class);

        assertDoesNotThrow(
                () -> template.exchange("http://localhost:8080/api/notes/{id}/comments", HttpMethod.DELETE,
                        new HttpEntity<>(headers), Void.class, noteId));

        List<GetCommentDtoResponse> comments = new ArrayList<>();
        comments.add(new GetCommentDtoResponse(0, "body1", authorId, 1, ""));
        List<GetRevisionDtoResponse> revisions = new ArrayList<>();
        revisions.add(new GetRevisionDtoResponse(1, "revision1", "", comments));
        revisions.add(new GetRevisionDtoResponse(2, "revision2", "", new ArrayList<>()));
        List<GetNoteListDtoResponse> expectedResponse = new ArrayList<>();
        expectedResponse.add(new GetNoteListDtoResponse(0, "subject", "revision2", sectionId, authorId, "", revisions));

        ResponseEntity<List<GetNoteListDtoResponse>> responseEntity =
                template.exchange("http://localhost:8080/api/notes?comments=true&allVersions=true&commentVersion=true",
                        HttpMethod.GET, new HttpEntity<>(headers),
                        new ParameterizedTypeReference<List<GetNoteListDtoResponse>>(){});
        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(expectedResponse, responseEntity.getBody())
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
