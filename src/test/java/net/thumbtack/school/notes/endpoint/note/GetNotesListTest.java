package net.thumbtack.school.notes.endpoint.note;

import com.google.gson.Gson;
import net.thumbtack.school.notes.NotesServer;
import net.thumbtack.school.notes.dto.requests.note.*;
import net.thumbtack.school.notes.dto.requests.user.AddToListDtoRequest;
import net.thumbtack.school.notes.dto.requests.user.LoginDtoRequest;
import net.thumbtack.school.notes.dto.requests.user.RegisterUserDtoRequest;
import net.thumbtack.school.notes.dto.responses.note.*;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes= NotesServer.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class GetNotesListTest {

    private final RestTemplate template = new RestTemplate();

    private final Gson gson = new Gson();

    private HttpHeaders headers;
    private int sectionId1;
    private int sectionId2;
    private int noteId1;
    private int noteId2;
    private int noteId3;
    private int authorId1;
    private int authorId2;

    private String timeCreatedNote3;

    @BeforeEach
    void setUp() {
        template.postForEntity("http://localhost:8080/api/debug/clear", null, Object.class);
        RegisterUserDtoRequest requestRegister1 = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "login0001", "password0001");
        String cookie = template.postForEntity("http://localhost:8080/api/accounts", requestRegister1,
                RegisterUserDtoResponse.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        SectionNameDtoRequest requestCreateSection1 = new SectionNameDtoRequest("section1");
        sectionId1 = template.exchange("http://localhost:8080/api/sections", HttpMethod.POST,
                new HttpEntity<>(requestCreateSection1, headers), SectionDataDtoResponse.class).getBody().getId();

        CreateNoteDtoRequest requestCreateNote1 = new CreateNoteDtoRequest("subject1", "revision1", sectionId1);
        GetNoteInfoDtoResponse responseCreateNote1 = template.exchange("http://localhost:8080/api/notes",
                HttpMethod.POST, new HttpEntity<>(requestCreateNote1, headers), GetNoteInfoDtoResponse.class).getBody();
        noteId1 = responseCreateNote1.getId(); authorId1 = responseCreateNote1.getAuthorId();

        CreateCommentDtoRequest requestCreateComment1 = new CreateCommentDtoRequest("comment1", noteId1);
        template.exchange("http://localhost:8080/api/comments", HttpMethod.POST, new HttpEntity<>(requestCreateComment1, headers),
                GetCommentInfoDtoResponse.class);

        EditOrTransferNoteDtoRequest requestEditNote = new EditOrTransferNoteDtoRequest("revision2", null);
        template.exchange("http://localhost:8080/api/notes/{id}", HttpMethod.PUT, new HttpEntity<>(requestEditNote, headers),
                GetNoteInfoDtoResponse.class, noteId1);

        CreateCommentDtoRequest requestCreateComment2 = new CreateCommentDtoRequest("comment2", noteId1);
        template.exchange("http://localhost:8080/api/comments", HttpMethod.POST, new HttpEntity<>(requestCreateComment2, headers),
                GetCommentInfoDtoResponse.class);

        RegisterUserDtoRequest requestRegister2 = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "login0002", "password0002");
        cookie = template.postForEntity("http://localhost:8080/api/accounts", requestRegister2,
                RegisterUserDtoResponse.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        CreateCommentDtoRequest requestCreateComment3 = new CreateCommentDtoRequest("comment3", noteId1);
        template.exchange("http://localhost:8080/api/comments", HttpMethod.POST, new HttpEntity<>(requestCreateComment3, headers),
                GetCommentInfoDtoResponse.class);

        CreateNoteDtoRequest requestCreateNote2 = new CreateNoteDtoRequest("subject2", "revision1", sectionId1);
        GetNoteInfoDtoResponse responseCreateNote2 = template.exchange("http://localhost:8080/api/notes",
                HttpMethod.POST, new HttpEntity<>(requestCreateNote2, headers), GetNoteInfoDtoResponse.class).getBody();
        noteId2 = responseCreateNote2.getId(); authorId2 = responseCreateNote2.getAuthorId();

        SectionNameDtoRequest requestCreateSection2 = new SectionNameDtoRequest("section2");
        sectionId2 = template.exchange("http://localhost:8080/api/sections", HttpMethod.POST,
                new HttpEntity<>(requestCreateSection2, headers), SectionDataDtoResponse.class).getBody().getId();

        CreateNoteDtoRequest requestCreateNote3 = new CreateNoteDtoRequest("subject3", "revision1", sectionId2);
        GetNoteInfoDtoResponse responseCreateNote3 = template.exchange("http://localhost:8080/api/notes",
                HttpMethod.POST, new HttpEntity<>(requestCreateNote3, headers), GetNoteInfoDtoResponse.class).getBody();
        noteId3 = responseCreateNote3.getId(); timeCreatedNote3 = responseCreateNote3.getCreated();

        RegisterUserDtoRequest requestRegister3 = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "login0003", "password0003");
        cookie = template.postForEntity("http://localhost:8080/api/accounts", requestRegister3,
                RegisterUserDtoResponse.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        RateNoteDtoRequest requestRateNote1 = new RateNoteDtoRequest(3);
        template.exchange("http://localhost:8080/api/notes/{id}/rating", HttpMethod.POST,
                new HttpEntity<>(requestRateNote1, headers), Void.class, noteId1);

        RateNoteDtoRequest requestRateNote2 = new RateNoteDtoRequest(4);
        template.exchange("http://localhost:8080/api/notes/{id}/rating", HttpMethod.POST,
                new HttpEntity<>(requestRateNote2, headers), Void.class, noteId2);

        RateNoteDtoRequest requestRateNote3 = new RateNoteDtoRequest(5);
        template.exchange("http://localhost:8080/api/notes/{id}/rating", HttpMethod.POST,
                new HttpEntity<>(requestRateNote3, headers), Void.class, noteId3);
    }

    @Test
    public void testGetNotesListWithIWrongSearchParam() {
        ServerErrors expectedResponse = createException(ServerErrorCode.WRONG_SEARCH_PARAM);

        try {
            template.exchange("http://localhost:8080/api/notes?section=1&user=1", HttpMethod.GET, new HttpEntity(headers), ServerErrors.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testGetNotesListWithInvalidValue() {
        ServerErrors expectedResponse = createException(ServerErrorCode.INVALID_PARAM_VALUE);

        try {
            template.exchange("http://localhost:8080/api/notes?sectionId=all", HttpMethod.GET, new HttpEntity(headers), ServerErrors.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testAllNotes() {
        List<GetCommentDtoResponse> commentsFromRevision1 = new ArrayList<>();
        commentsFromRevision1.add(new GetCommentDtoResponse(0, "comment1", authorId1, 1, ""));
        List<GetCommentDtoResponse> commentsFromRevision2 = new ArrayList<>();
        commentsFromRevision2.add(new GetCommentDtoResponse(0, "comment2", authorId1, 2, ""));
        commentsFromRevision2.add(new GetCommentDtoResponse(0, "comment3", authorId2, 2, ""));

        List<GetRevisionDtoResponse> revisionsFromNote1 = new ArrayList<>();
        revisionsFromNote1.add(new GetRevisionDtoResponse(1, "revision1", "", commentsFromRevision1));
        revisionsFromNote1.add(new GetRevisionDtoResponse(2, "revision2", "", commentsFromRevision2));

        List<GetRevisionDtoResponse> revisionsFromNote2 = new ArrayList<>();
        revisionsFromNote2.add(new GetRevisionDtoResponse(1, "revision1", "", new ArrayList<>()));

        List<GetRevisionDtoResponse> revisionsFromNote3 = new ArrayList<>();
        revisionsFromNote3.add(new GetRevisionDtoResponse(1, "revision1", "", new ArrayList<>()));

        List<GetNoteListDtoResponse> expectedResponse = new ArrayList<>();
        expectedResponse.add(new GetNoteListDtoResponse(0, "subject1", "revision2", sectionId1, authorId1,
                "", revisionsFromNote1));
        expectedResponse.add(new GetNoteListDtoResponse(0, "subject2", "revision1", sectionId1, authorId2,
                "", revisionsFromNote2));
        expectedResponse.add(new GetNoteListDtoResponse(0, "subject3", "revision1", sectionId2, authorId2,
                "", revisionsFromNote3));

        ResponseEntity<List<GetNoteListDtoResponse>> responseEntity =
                template.exchange("http://localhost:8080/api/notes?comments=true&allVersions=true&commentVersion=true",
                        HttpMethod.GET, new HttpEntity<>(headers),
                        new ParameterizedTypeReference<List<GetNoteListDtoResponse>>(){});
        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(expectedResponse, responseEntity.getBody())
        );
    }

    @Test
    public void testNotesListWithSectionId() {
        List<GetCommentDtoResponse> commentsFromRevision1 = new ArrayList<>();
        commentsFromRevision1.add(new GetCommentDtoResponse(0, "comment1", authorId1, null, ""));
        List<GetCommentDtoResponse> commentsFromRevision2 = new ArrayList<>();
        commentsFromRevision2.add(new GetCommentDtoResponse(0, "comment2", authorId1, null, ""));
        commentsFromRevision2.add(new GetCommentDtoResponse(0, "comment3", authorId2, null, ""));

        List<GetRevisionDtoResponse> revisionsFromNote1 = new ArrayList<>();
        revisionsFromNote1.add(new GetRevisionDtoResponse(1, "revision1", "", commentsFromRevision1));
        revisionsFromNote1.add(new GetRevisionDtoResponse(2, "revision2", "", commentsFromRevision2));

        List<GetRevisionDtoResponse> revisionsFromNote2 = new ArrayList<>();
        revisionsFromNote2.add(new GetRevisionDtoResponse(1, "revision1", "", new ArrayList<>()));

        List<GetNoteListDtoResponse> expectedResponse = new ArrayList<>();
        expectedResponse.add(new GetNoteListDtoResponse(0, "subject1", "revision2", sectionId1, authorId1,
                "", revisionsFromNote1));
        expectedResponse.add(new GetNoteListDtoResponse(0, "subject2", "revision1", sectionId1, authorId2,
                "", revisionsFromNote2));

        ResponseEntity<List<GetNoteListDtoResponse>> responseEntity =
                template.exchange("http://localhost:8080/api/notes?comments=true&allVersions=true&sectionId={id}",
                        HttpMethod.GET, new HttpEntity<>(headers),
                        new ParameterizedTypeReference<List<GetNoteListDtoResponse>>(){}, sectionId1);
        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(expectedResponse, responseEntity.getBody())
        );
    }

    @Test
    public void testNotesListWithSortByRating() {
        List<GetRevisionDtoResponse> revisionsFromNote1 = new ArrayList<>();
        revisionsFromNote1.add(new GetRevisionDtoResponse(1, "revision1", "", null));
        revisionsFromNote1.add(new GetRevisionDtoResponse(2, "revision2", "", null));

        List<GetRevisionDtoResponse> revisionsFromNote2 = new ArrayList<>();
        revisionsFromNote2.add(new GetRevisionDtoResponse(1, "revision1", "", null));

        List<GetRevisionDtoResponse> revisionsFromNote3 = new ArrayList<>();
        revisionsFromNote3.add(new GetRevisionDtoResponse(1, "revision1", "", null));

        List<GetNoteListDtoResponse> expectedResponse = new ArrayList<>();
        expectedResponse.add(new GetNoteListDtoResponse(0, "subject3", "revision1", sectionId2, authorId2,
                "", revisionsFromNote3));
        expectedResponse.add(new GetNoteListDtoResponse(0, "subject2", "revision1", sectionId1, authorId2,
                "", revisionsFromNote2));
        expectedResponse.add(new GetNoteListDtoResponse(0, "subject1", "revision2", sectionId1, authorId1,
                "", revisionsFromNote1));

        ResponseEntity<List<GetNoteListDtoResponse>> responseEntity =
                template.exchange("http://localhost:8080/api/notes?allVersions=true&sortByRating=desc",
                        HttpMethod.GET, new HttpEntity<>(headers),
                        new ParameterizedTypeReference<List<GetNoteListDtoResponse>>(){});
        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(expectedResponse, responseEntity.getBody())
        );
    }

    @Test
    public void testNotesListWithTags() {
        LoginDtoRequest loginRequest = new LoginDtoRequest("login0002", "password0002");
        String cookie = template.postForEntity("http://localhost:8080/api/sessions", loginRequest,
                Void.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        EditOrTransferNoteDtoRequest requestEditNote2 = new EditOrTransferNoteDtoRequest("revision2 foo bar", null);
        template.exchange("http://localhost:8080/api/notes/{id}", HttpMethod.PUT, new HttpEntity<>(requestEditNote2, headers),
                GetNoteInfoDtoResponse.class, noteId2);

        EditOrTransferNoteDtoRequest requestEditNote3 = new EditOrTransferNoteDtoRequest("revision2 foo", null);
        template.exchange("http://localhost:8080/api/notes/{id}", HttpMethod.PUT, new HttpEntity<>(requestEditNote3, headers),
                GetNoteInfoDtoResponse.class, noteId3);

        List<GetNoteListDtoResponse> expectedResponse = new ArrayList<>();
        expectedResponse.add(new GetNoteListDtoResponse(0, "subject2", "revision2 foo bar", sectionId1, authorId2,
                "", null));
        expectedResponse.add(new GetNoteListDtoResponse(0, "subject3", "revision2 foo", sectionId2, authorId2,
                "", null));

        ResponseEntity<List<GetNoteListDtoResponse>> responseEntity =
                template.exchange("http://localhost:8080/api/notes?tags=foo,bar",
                        HttpMethod.GET, new HttpEntity<>(headers),
                        new ParameterizedTypeReference<List<GetNoteListDtoResponse>>(){});
        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(expectedResponse, responseEntity.getBody())
        );
    }

    @Test
    public void testNotesListWithTagsAndAllTags() {
        LoginDtoRequest loginRequest = new LoginDtoRequest("login0002", "password0002");
        String cookie = template.postForEntity("http://localhost:8080/api/sessions", loginRequest,
                Void.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        EditOrTransferNoteDtoRequest requestEditNote2 = new EditOrTransferNoteDtoRequest("revision2 foo bar", null);
        template.exchange("http://localhost:8080/api/notes/{id}", HttpMethod.PUT, new HttpEntity<>(requestEditNote2, headers),
                GetNoteInfoDtoResponse.class, noteId2);

        EditOrTransferNoteDtoRequest requestEditNote3 = new EditOrTransferNoteDtoRequest("revision2 foo", null);
        template.exchange("http://localhost:8080/api/notes/{id}", HttpMethod.PUT, new HttpEntity<>(requestEditNote3, headers),
                GetNoteInfoDtoResponse.class, noteId3);

        List<GetNoteListDtoResponse> expectedResponse = new ArrayList<>();
        expectedResponse.add(new GetNoteListDtoResponse(0, "subject2", "revision2 foo bar", sectionId1, authorId2,
                "", null));

        ResponseEntity<List<GetNoteListDtoResponse>> responseEntity =
                template.exchange("http://localhost:8080/api/notes?tags=foo,bar&alltags=true",
                        HttpMethod.GET, new HttpEntity<>(headers),
                        new ParameterizedTypeReference<List<GetNoteListDtoResponse>>(){});
        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(expectedResponse, responseEntity.getBody())
        );
    }

    @Test
    public void testNotesListWithTimeFrom() throws InterruptedException {
        List<GetNoteListDtoResponse> expectedResponse = new ArrayList<>();
        expectedResponse.add(new GetNoteListDtoResponse(0, "subject4", "revision1", sectionId1, authorId2,
                "", null));

        LocalDateTime timeFrom = LocalDateTime.parse(timeCreatedNote3).plusSeconds(1);
        TimeUnit.SECONDS.sleep(3);

        LoginDtoRequest loginRequest = new LoginDtoRequest("login0002", "password0002");
        String cookie = template.postForEntity("http://localhost:8080/api/sessions", loginRequest,
                Void.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        CreateNoteDtoRequest requestCreateNote4 = new CreateNoteDtoRequest("subject4", "revision1", sectionId1);
        template.exchange("http://localhost:8080/api/notes",
                HttpMethod.POST, new HttpEntity<>(requestCreateNote4, headers), GetNoteInfoDtoResponse.class).getBody();

        ResponseEntity<List<GetNoteListDtoResponse>> responseEntity =
                template.exchange("http://localhost:8080/api/notes?timeFrom={time}",
                        HttpMethod.GET, new HttpEntity<>(headers),
                        new ParameterizedTypeReference<List<GetNoteListDtoResponse>>(){}, timeFrom.toString());
        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(expectedResponse, responseEntity.getBody())
        );
    }

    @Test
    public void testNotesListWithTimeTo() throws InterruptedException {
        List<GetNoteListDtoResponse> expectedResponse = new ArrayList<>();
        expectedResponse.add(new GetNoteListDtoResponse(0, "subject1", "revision2", sectionId1, authorId1,
                "", null));
        expectedResponse.add(new GetNoteListDtoResponse(0, "subject2", "revision1", sectionId1, authorId2,
                "", null));
        expectedResponse.add(new GetNoteListDtoResponse(0, "subject3", "revision1", sectionId2, authorId2,
                "", null));

        LocalDateTime timeTo = LocalDateTime.parse(timeCreatedNote3).plusSeconds(1);
        TimeUnit.SECONDS.sleep(3);

        CreateNoteDtoRequest requestCreateNote4 = new CreateNoteDtoRequest("subject4", "revision1", sectionId1);
        template.exchange("http://localhost:8080/api/notes",
                HttpMethod.POST, new HttpEntity<>(requestCreateNote4, headers), GetNoteInfoDtoResponse.class).getBody();

        ResponseEntity<List<GetNoteListDtoResponse>> responseEntity =
                template.exchange("http://localhost:8080/api/notes?timeTo={time}",
                        HttpMethod.GET, new HttpEntity<>(headers),
                        new ParameterizedTypeReference<List<GetNoteListDtoResponse>>(){}, timeTo.toString());
        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(expectedResponse, responseEntity.getBody())
        );
    }

    @Test
    public void testNotesListWithUserId() {
        List<GetNoteListDtoResponse> expectedResponse = new ArrayList<>();
        expectedResponse.add(new GetNoteListDtoResponse(0, "subject2", "revision1", sectionId1, authorId2,
                "", null));
        expectedResponse.add(new GetNoteListDtoResponse(0, "subject3", "revision1", sectionId2, authorId2,
                "", null));

        ResponseEntity<List<GetNoteListDtoResponse>> responseEntity =
                template.exchange("http://localhost:8080/api/notes?user={id}",
                        HttpMethod.GET, new HttpEntity<>(headers),
                        new ParameterizedTypeReference<List<GetNoteListDtoResponse>>(){}, authorId2);
        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(expectedResponse, responseEntity.getBody())
        );
    }

    @Test
    public void testNotesListWithOnlyFollowings() {
        AddToListDtoRequest requestAddToFollowings = new AddToListDtoRequest("login0002");
        template.postForEntity("http://localhost:8080/api/followings",
                new HttpEntity(requestAddToFollowings, headers), Void.class);

        List<GetNoteListDtoResponse> expectedResponse = new ArrayList<>();
        expectedResponse.add(new GetNoteListDtoResponse(0, "subject2", "revision1", sectionId1, authorId2,
                "", null));
        expectedResponse.add(new GetNoteListDtoResponse(0, "subject3", "revision1", sectionId2, authorId2,
                "", null));

        ResponseEntity<List<GetNoteListDtoResponse>> responseEntity =
                template.exchange("http://localhost:8080/api/notes?include=onlyFollowings",
                        HttpMethod.GET, new HttpEntity<>(headers),
                        new ParameterizedTypeReference<List<GetNoteListDtoResponse>>(){}, authorId2);
        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(expectedResponse, responseEntity.getBody())
        );
    }

    @Test
    public void testNotesListWithOnlyIgnore() {
        AddToListDtoRequest requestAddToIgnore = new AddToListDtoRequest("login0002");
        template.postForEntity("http://localhost:8080/api/ignore",
                new HttpEntity(requestAddToIgnore, headers), Void.class);

        List<GetNoteListDtoResponse> expectedResponse = new ArrayList<>();
        expectedResponse.add(new GetNoteListDtoResponse(0, "subject2", "revision1", sectionId1, authorId2,
                "", null));
        expectedResponse.add(new GetNoteListDtoResponse(0, "subject3", "revision1", sectionId2, authorId2,
                "", null));

        ResponseEntity<List<GetNoteListDtoResponse>> responseEntity =
                template.exchange("http://localhost:8080/api/notes?include=onlyIgnore",
                        HttpMethod.GET, new HttpEntity<>(headers),
                        new ParameterizedTypeReference<List<GetNoteListDtoResponse>>(){}, authorId2);
        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(expectedResponse, responseEntity.getBody())
        );
    }

    @Test
    public void testNotesListWithNotIgnore() {
        AddToListDtoRequest requestAddToIgnore = new AddToListDtoRequest("login0001");
        template.postForEntity("http://localhost:8080/api/ignore",
                new HttpEntity(requestAddToIgnore, headers), Void.class);

        List<GetNoteListDtoResponse> expectedResponse = new ArrayList<>();
        expectedResponse.add(new GetNoteListDtoResponse(0, "subject2", "revision1", sectionId1, authorId2,
                "", null));
        expectedResponse.add(new GetNoteListDtoResponse(0, "subject3", "revision1", sectionId2, authorId2,
                "", null));

        ResponseEntity<List<GetNoteListDtoResponse>> responseEntity =
                template.exchange("http://localhost:8080/api/notes?include=notIgnore",
                        HttpMethod.GET, new HttpEntity<>(headers),
                        new ParameterizedTypeReference<List<GetNoteListDtoResponse>>(){}, authorId2);
        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(expectedResponse, responseEntity.getBody())
        );
    }

    @Test
    public void testNotesListWithSortingAndFrom() {
        List<GetNoteListDtoResponse> expectedResponse = new ArrayList<>();
        expectedResponse.add(new GetNoteListDtoResponse(0, "subject2", "revision1", sectionId1, authorId2,
                "", null));
        expectedResponse.add(new GetNoteListDtoResponse(0, "subject1", "revision2", sectionId1, authorId1,
                "", null));


        ResponseEntity<List<GetNoteListDtoResponse>> responseEntity =
                template.exchange("http://localhost:8080/api/notes?sortByRating=desc&from=1",
                        HttpMethod.GET, new HttpEntity<>(headers),
                        new ParameterizedTypeReference<List<GetNoteListDtoResponse>>(){});
        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(expectedResponse, responseEntity.getBody())
        );
    }

    @Test
    public void testNotesListWithSortingAndCount() {
        List<GetNoteListDtoResponse> expectedResponse = new ArrayList<>();
        expectedResponse.add(new GetNoteListDtoResponse(0, "subject1", "revision2", sectionId1, authorId1,
                "", null));
        expectedResponse.add(new GetNoteListDtoResponse(0, "subject2", "revision1", sectionId1, authorId2,
                "", null));


        ResponseEntity<List<GetNoteListDtoResponse>> responseEntity =
                template.exchange("http://localhost:8080/api/notes?sortByRating=asc&count=2",
                        HttpMethod.GET, new HttpEntity<>(headers),
                        new ParameterizedTypeReference<List<GetNoteListDtoResponse>>(){});
        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(expectedResponse, responseEntity.getBody())
        );
    }

    @Test
    public void testNotesListWithSortingFromAndCount() {
        List<GetNoteListDtoResponse> expectedResponse = new ArrayList<>();
        expectedResponse.add(new GetNoteListDtoResponse(0, "subject2", "revision1", sectionId1, authorId2,
                "", null));


        ResponseEntity<List<GetNoteListDtoResponse>> responseEntity =
                template.exchange("http://localhost:8080/api/notes?sortByRating=asc&from=1&count=1",
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
}
