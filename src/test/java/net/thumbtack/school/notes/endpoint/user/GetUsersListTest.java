
package net.thumbtack.school.notes.endpoint.user;

import com.google.gson.Gson;
import net.thumbtack.school.notes.NotesServer;
import net.thumbtack.school.notes.dto.requests.note.CreateNoteDtoRequest;
import net.thumbtack.school.notes.dto.requests.note.RateNoteDtoRequest;
import net.thumbtack.school.notes.dto.requests.note.SectionNameDtoRequest;
import net.thumbtack.school.notes.dto.requests.user.AddToListDtoRequest;
import net.thumbtack.school.notes.dto.requests.user.DeleteUserDtoRequest;
import net.thumbtack.school.notes.dto.requests.user.LoginDtoRequest;
import net.thumbtack.school.notes.dto.requests.user.RegisterUserDtoRequest;
import net.thumbtack.school.notes.dto.responses.note.GetNoteInfoDtoResponse;
import net.thumbtack.school.notes.dto.responses.note.SectionDataDtoResponse;
import net.thumbtack.school.notes.dto.responses.user.GetUsersDtoResponse;
import net.thumbtack.school.notes.dto.responses.user.RegisterUserDtoResponse;
import net.thumbtack.school.notes.exception.ServerError;
import net.thumbtack.school.notes.exception.ServerErrorCode;
import net.thumbtack.school.notes.exception.ServerErrors;
import net.thumbtack.school.notes.model.User;
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
class GetUsersListTest {

    private final RestTemplate template = new RestTemplate();

    private final Gson gson = new Gson();

    private User user1;
    private User user2;
    private User user3;
    private User user4;
    private User user5;
    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        template.postForEntity("http://localhost:8080/api/debug/clear", null, Object.class);
        user1 = new User("Ekaterina", "Rogozhina", "Andreevna",
                "login0001", "password0001");
        user2 = new User("Ekaterina", "Rogozhina", "Andreevna",
                "login0002", "password0002");
        user3 = new User("Ekaterina", "Rogozhina", "Andreevna",
                "login0003", "password0003");
        user4 = new User("Ekaterina", "Rogozhina", "Andreevna",
                "login0004", "password0004");
        user5 = new User("Ekaterina", "Rogozhina", "Andreevna",
                "login0005", "password0005");
        RegisterUserDtoRequest request1 = new RegisterUserDtoRequest(user1.getFirstName(), user1.getLastName(),
                user1.getPatronymic(), user1.getLogin(), user1.getPassword());
        RegisterUserDtoRequest request2 = new RegisterUserDtoRequest(user2.getFirstName(), user2.getLastName(),
                user2.getPatronymic(), user2.getLogin(), user2.getPassword());
        RegisterUserDtoRequest request3 = new RegisterUserDtoRequest(user3.getFirstName(), user3.getLastName(),
                user3.getPatronymic(), user3.getLogin(), user3.getPassword());
        RegisterUserDtoRequest request4 = new RegisterUserDtoRequest(user4.getFirstName(), user4.getLastName(),
                user4.getPatronymic(), user4.getLogin(), user4.getPassword());
        RegisterUserDtoRequest request5 = new RegisterUserDtoRequest(user5.getFirstName(), user5.getLastName(),
                user5.getPatronymic(), user5.getLogin(), user5.getPassword());
        template.postForEntity("http://localhost:8080/api/accounts", request1, RegisterUserDtoResponse.class);
        template.postForEntity("http://localhost:8080/api/accounts", request2, RegisterUserDtoResponse.class);
        template.postForEntity("http://localhost:8080/api/accounts", request3, RegisterUserDtoResponse.class);
        template.postForEntity("http://localhost:8080/api/accounts", request4, RegisterUserDtoResponse.class);
        template.postForEntity("http://localhost:8080/api/accounts", request5, RegisterUserDtoResponse.class);

        LoginDtoRequest loginRequest = new LoginDtoRequest("login0001", "password0001");
        String cookie = template.postForEntity("http://localhost:8080/api/sessions", loginRequest,
                Void.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        headers = new HttpHeaders();
        headers.add("Cookie", cookie);
    }

    @Test
    public void testGetUsersListWithIWrongSearchParam() {
        ServerErrors expectedResponse = createException(ServerErrorCode.WRONG_SEARCH_PARAM);

        try {
            template.exchange("http://localhost:8080/api/accounts?type=followings&obs=abc", HttpMethod.GET, new HttpEntity(headers), ServerErrors.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testGetUsersListWithInvalidTypeValue() {
        ServerErrors expectedResponse = createException(ServerErrorCode.INVALID_PARAM_VALUE);

        try {
            template.exchange("http://localhost:8080/api/accounts?type=follow", HttpMethod.GET, new HttpEntity(headers), ServerErrors.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testGetUsersListWithInvalidSortByRatingValue() {
        ServerErrors expectedResponse = createException(ServerErrorCode.INVALID_PARAM_VALUE);

        try {
            template.exchange("http://localhost:8080/api/accounts?sortByRating=ccc", HttpMethod.GET, new HttpEntity(headers), ServerErrors.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testGetUsersListWithInvalidFromValue() {
        ServerErrors expectedResponse = createException(ServerErrorCode.INVALID_PARAM_VALUE);

        try {
            template.exchange("http://localhost:8080/api/accounts?from=aaa", HttpMethod.GET, new HttpEntity(headers), ServerErrors.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testGetUsersListWithInvalidCountValue() {
        ServerErrors expectedResponse = createException(ServerErrorCode.INVALID_PARAM_VALUE);

        try {
            template.exchange("http://localhost:8080/api/accounts?count=qw", HttpMethod.GET, new HttpEntity(headers), ServerErrors.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testGetUsersListWithSessionIdNotFound() {
        ServerErrors expectedResponse = createException(ServerErrorCode.THIS_SESSIONID_NOT_FOUND);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "JAVASESSIONID=" + UUID.randomUUID().toString());

        try {
            template.exchange("http://localhost:8080/api/accounts?type=ignore&sortByRating=asc", HttpMethod.GET,
                    new HttpEntity(headers), ServerErrors.class);
        } catch (HttpStatusCodeException ex) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode()),
                    () -> assertEquals(expectedResponse, gson.fromJson(ex.getResponseBodyAsString(), ServerErrors.class))
            );
        }
    }

    @Test
    public void testGetAllUsers() {
        List<GetUsersDtoResponse> expectedList = createExpectedList(user1, user2, user3, user4, user5);

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

    @Test
    public void testGetAllUsersWithFrom() {
        List<GetUsersDtoResponse> expectedList = createExpectedList(null, user2, user3, user4, user5);
        expectedList.sort((o1, o2) -> (int) (o2.getRating() - o1.getRating()));

        ResponseEntity<List<GetUsersDtoResponse>> responseEntity =
                template.exchange("http://localhost:8080/api/accounts?from=1", HttpMethod.GET,
                        new HttpEntity<String>(headers), new ParameterizedTypeReference<List<GetUsersDtoResponse>>(){});
        List<GetUsersDtoResponse> actualList = responseEntity.getBody();
        actualList.sort(Comparator.comparingInt(GetUsersDtoResponse::getId));
        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(expectedList, actualList)
        );
    }

    @Test
    public void testGetAllUsersWithCount() {
        List<GetUsersDtoResponse> expectedList = createExpectedList(user1, user2, user3, null, null);

        ResponseEntity<List<GetUsersDtoResponse>> responseEntity =
                template.exchange("http://localhost:8080/api/accounts?count=3", HttpMethod.GET,
                        new HttpEntity<String>(headers), new ParameterizedTypeReference<List<GetUsersDtoResponse>>(){});
        List<GetUsersDtoResponse> actualList = responseEntity.getBody();
        actualList.sort(Comparator.comparingInt(GetUsersDtoResponse::getId));
        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(expectedList, actualList)
        );
    }

    @Test
    public void testGetAllUsersWithFromAndCount() {
        List<GetUsersDtoResponse> expectedList = createExpectedList(null, null, user3, user4, user5);

        ResponseEntity<List<GetUsersDtoResponse>> responseEntity =
                template.exchange("http://localhost:8080/api/accounts?from=2&count=3", HttpMethod.GET,
                        new HttpEntity<String>(headers), new ParameterizedTypeReference<List<GetUsersDtoResponse>>(){});
        List<GetUsersDtoResponse> actualList = responseEntity.getBody();
        actualList.sort(Comparator.comparingInt(GetUsersDtoResponse::getId));
        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(expectedList, actualList)
        );
    }

    @Test
    public void testGetFollowings() {
        LoginDtoRequest loginRequest = new LoginDtoRequest("login0001", "password0001");
        String cookie = template.postForEntity("http://localhost:8080/api/sessions", loginRequest,
                Void.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        template.exchange("http://localhost:8080/api/followings", HttpMethod.POST,
                new HttpEntity(new AddToListDtoRequest("login0002"), headers), Void.class);
        template.exchange("http://localhost:8080/api/followings", HttpMethod.POST,
                new HttpEntity(new AddToListDtoRequest("login0003"), headers), Void.class);
        template.exchange("http://localhost:8080/api/followings", HttpMethod.POST,
                new HttpEntity(new AddToListDtoRequest("login0004"), headers), Void.class);

        List<GetUsersDtoResponse> expectedList = createExpectedList(null, user2, user3, user4, null);

        ResponseEntity<List<GetUsersDtoResponse>> responseEntity =
                template.exchange("http://localhost:8080/api/accounts?type=followings", HttpMethod.GET,
                        new HttpEntity<String>(headers), new ParameterizedTypeReference<List<GetUsersDtoResponse>>(){});
        List<GetUsersDtoResponse> actualList = responseEntity.getBody();
        actualList.sort(Comparator.comparingInt(GetUsersDtoResponse::getId));
        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(expectedList, actualList)
        );
    }

    @Test
    public void testGetFollowers() {
        String cookie = template.postForEntity("http://localhost:8080/api/sessions",
                new LoginDtoRequest("login0002", "password0002"),
                Void.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);
        template.exchange("http://localhost:8080/api/followings", HttpMethod.POST,
                new HttpEntity(new AddToListDtoRequest("login0001"), headers), Void.class);

        cookie = template.postForEntity("http://localhost:8080/api/sessions",
                new LoginDtoRequest("login0003", "password0003"),
                Void.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        headers = new HttpHeaders();
        headers.add("Cookie", cookie);
        template.exchange("http://localhost:8080/api/followings", HttpMethod.POST,
                new HttpEntity(new AddToListDtoRequest("login0001"), headers), Void.class);

        cookie = template.postForEntity("http://localhost:8080/api/sessions",
                new LoginDtoRequest("login0001", "password0001"),
                Void.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        List<GetUsersDtoResponse> expectedList = createExpectedList(null, user2, user3, null, null);

        ResponseEntity<List<GetUsersDtoResponse>> responseEntity =
                template.exchange("http://localhost:8080/api/accounts?type=followers", HttpMethod.GET,
                        new HttpEntity<String>(headers), new ParameterizedTypeReference<List<GetUsersDtoResponse>>(){});
        List<GetUsersDtoResponse> actualList = responseEntity.getBody();
        actualList.sort(Comparator.comparingInt(GetUsersDtoResponse::getId));
        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(expectedList, actualList)
        );
    }

    @Test
    public void testGetIgnore() {
        LoginDtoRequest loginRequest = new LoginDtoRequest("login0001", "password0001");
        String cookie = template.postForEntity("http://localhost:8080/api/sessions", loginRequest,
                Void.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        template.exchange("http://localhost:8080/api/ignore", HttpMethod.POST,
                new HttpEntity(new AddToListDtoRequest("login0002"), headers), Void.class);
        template.exchange("http://localhost:8080/api/ignore", HttpMethod.POST,
                new HttpEntity(new AddToListDtoRequest("login0003"), headers), Void.class);
        template.exchange("http://localhost:8080/api/ignore", HttpMethod.POST,
                new HttpEntity(new AddToListDtoRequest("login0005"), headers), Void.class);

        List<GetUsersDtoResponse> expectedList = createExpectedList(null, user2, user3, null, user5);

        ResponseEntity<List<GetUsersDtoResponse>> responseEntity =
                template.exchange("http://localhost:8080/api/accounts?type=ignore", HttpMethod.GET,
                        new HttpEntity<String>(headers), new ParameterizedTypeReference<List<GetUsersDtoResponse>>(){});
        List<GetUsersDtoResponse> actualList = responseEntity.getBody();
        actualList.sort(Comparator.comparingInt(GetUsersDtoResponse::getId));
        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(expectedList, actualList)
        );
    }

    @Test
    public void testGetIgnoredBy() {
        String cookie = template.postForEntity("http://localhost:8080/api/sessions",
                new LoginDtoRequest("login0002", "password0002"),
                Void.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);
        template.exchange("http://localhost:8080/api/ignore", HttpMethod.POST,
                new HttpEntity(new AddToListDtoRequest("login0001"), headers), Void.class);

        cookie = template.postForEntity("http://localhost:8080/api/sessions",
                new LoginDtoRequest("login0004", "password0004"),
                Void.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        headers = new HttpHeaders();
        headers.add("Cookie", cookie);
        template.exchange("http://localhost:8080/api/ignore", HttpMethod.POST,
                new HttpEntity(new AddToListDtoRequest("login0001"), headers), Void.class);

        cookie = template.postForEntity("http://localhost:8080/api/sessions",
                new LoginDtoRequest("login0001", "password0001"),
                Void.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        List<GetUsersDtoResponse> expectedList = createExpectedList(null, user2, null, user4, null);

        ResponseEntity<List<GetUsersDtoResponse>> responseEntity =
                template.exchange("http://localhost:8080/api/accounts?type=ignoredBy", HttpMethod.GET,
                        new HttpEntity<String>(headers), new ParameterizedTypeReference<List<GetUsersDtoResponse>>(){});
        List<GetUsersDtoResponse> actualList = responseEntity.getBody();
        actualList.sort(Comparator.comparingInt(GetUsersDtoResponse::getId));
        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(expectedList, actualList)
        );
    }

    @Test
    public void testGetUserListWithFromAndCount() {
        LoginDtoRequest loginRequest = new LoginDtoRequest("login0001", "password0001");
        String cookie = template.postForEntity("http://localhost:8080/api/sessions", loginRequest,
                Void.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        template.exchange("http://localhost:8080/api/followings", HttpMethod.POST,
                new HttpEntity(new AddToListDtoRequest("login0002"), headers), Void.class);
        template.exchange("http://localhost:8080/api/followings", HttpMethod.POST,
                new HttpEntity(new AddToListDtoRequest("login0003"), headers), Void.class);
        template.exchange("http://localhost:8080/api/followings", HttpMethod.POST,
                new HttpEntity(new AddToListDtoRequest("login0004"), headers), Void.class);
        template.exchange("http://localhost:8080/api/followings", HttpMethod.POST,
                new HttpEntity(new AddToListDtoRequest("login0005"), headers), Void.class);

        List<GetUsersDtoResponse> expectedList = createExpectedList(null, null, user3, user4, user5);

        ResponseEntity<List<GetUsersDtoResponse>> responseEntity =
                template.exchange("http://localhost:8080/api/accounts?type=followings&from=1&count=3", HttpMethod.GET,
                        new HttpEntity<String>(headers), new ParameterizedTypeReference<List<GetUsersDtoResponse>>(){});
        List<GetUsersDtoResponse> actualList = responseEntity.getBody();
        actualList.sort(Comparator.comparingInt(GetUsersDtoResponse::getId));
        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(expectedList, actualList)
        );
    }

    @Test
    public void testGetDeletedUsers() {
        String cookie = template.postForEntity("http://localhost:8080/api/sessions",
                new LoginDtoRequest("login0001", "password0001"), Void.class)
                .getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);
        template.exchange("http://localhost:8080/api/accounts", HttpMethod.DELETE,
                new HttpEntity(new DeleteUserDtoRequest("password0001"), headers), Void.class);

        cookie = template.postForEntity("http://localhost:8080/api/sessions",
                new LoginDtoRequest("login0002", "password0002"), Void.class)
                .getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        headers = new HttpHeaders();
        headers.add("Cookie", cookie);
        template.exchange("http://localhost:8080/api/accounts", HttpMethod.DELETE,
                new HttpEntity(new DeleteUserDtoRequest("password0002"), headers), Void.class);

        List<GetUsersDtoResponse> expectedList = createExpectedList(user1, user2, null, null, null);
        expectedList.get(0).setDeleted(true); expectedList.get(1).setDeleted(true);
        expectedList.get(0).setOnline(false); expectedList.get(1).setOnline(false);

        cookie = template.postForEntity("http://localhost:8080/api/sessions",
                new LoginDtoRequest("login0003", "password0003"), Void.class)
                .getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        ResponseEntity<List<GetUsersDtoResponse>> responseEntity =
                template.exchange("http://localhost:8080/api/accounts?type=deleted", HttpMethod.GET,
                        new HttpEntity<String>(headers), new ParameterizedTypeReference<List<GetUsersDtoResponse>>(){});
        List<GetUsersDtoResponse> actualList = responseEntity.getBody();
        actualList.sort(Comparator.comparingInt(GetUsersDtoResponse::getId));
        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(expectedList, actualList)
        );
    }

    @Test
    public void testGetSuperUsers() {
        RegisterUserDtoRequest request1 = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "katya2001", "katya564358");
        template.postForEntity("http://localhost:8080/api/debug/registerAdmin", request1, Void.class);

        RegisterUserDtoRequest request2 = new RegisterUserDtoRequest("Ekaterina", "Rogozhina",
                "Andreevna", "katya2002", "katya564358");
        String cookie = template.postForEntity("http://localhost:8080/api/debug/registerAdmin", request2, Void.class)
                .getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        List<GetUsersDtoResponse> expectedList = new ArrayList<>();
        expectedList.add(new GetUsersDtoResponse(0, "Ekaterina", "Rogozhina",
                "Andreevna", "katya2001", "", true, false, true, 0));
        expectedList.add(new GetUsersDtoResponse(0, "Ekaterina", "Rogozhina",
                "Andreevna", "katya2002", "", true, false, true, 0));

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        ResponseEntity<List<GetUsersDtoResponse>> responseEntity =
                template.exchange("http://localhost:8080/api/accounts?type=super", HttpMethod.GET,
                        new HttpEntity<String>(headers), new ParameterizedTypeReference<List<GetUsersDtoResponse>>(){});
        List<GetUsersDtoResponse> actualList = responseEntity.getBody();
        actualList.sort(Comparator.comparingInt(GetUsersDtoResponse::getId));
        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(expectedList, actualList)
        );
    }

    @Test
    public void testGetAllUsersWithSortingAndCount() {
        createNotes();

        LoginDtoRequest loginRequest = new LoginDtoRequest("login0001", "password0001");
        String cookie = template.postForEntity("http://localhost:8080/api/sessions", loginRequest,
                Void.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        List<GetUsersDtoResponse> expectedList = new ArrayList<>();
        expectedList.add(new GetUsersDtoResponse(user3.getId(), user3.getFirstName(), user3.getLastName(),
                user3.getPatronymic(), user3.getLogin(), "", true, false, null, 5));
        expectedList.add(new GetUsersDtoResponse(user2.getId(), user2.getFirstName(), user2.getLastName(),
                user2.getPatronymic(), user2.getLogin(), "", true, false, null, 4));
        expectedList.add(new GetUsersDtoResponse(user1.getId(), user1.getFirstName(), user1.getLastName(),
                user1.getPatronymic(), user1.getLogin(), "", true, false, null, 3));

        ResponseEntity<List<GetUsersDtoResponse>> responseEntity =
                template.exchange("http://localhost:8080/api/accounts?sortByRating=desc&count=3", HttpMethod.GET,
                        new HttpEntity<String>(headers), new ParameterizedTypeReference<List<GetUsersDtoResponse>>(){});
        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(expectedList, responseEntity.getBody())
        );
    }

    @Test
    public void testGetAllUsersWithSortingAndFrom() {
        createNotes();

        LoginDtoRequest loginRequest = new LoginDtoRequest("login0001", "password0001");
        String cookie = template.postForEntity("http://localhost:8080/api/sessions", loginRequest,
                Void.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        List<GetUsersDtoResponse> expectedList = new ArrayList<>();
        expectedList.add(new GetUsersDtoResponse(user1.getId(), user1.getFirstName(), user1.getLastName(),
                user1.getPatronymic(), user1.getLogin(), "", true, false, null, 3));
        expectedList.add(new GetUsersDtoResponse(user2.getId(), user2.getFirstName(), user2.getLastName(),
                user2.getPatronymic(), user2.getLogin(), "", true, false, null, 4));
        expectedList.add(new GetUsersDtoResponse(user3.getId(), user3.getFirstName(), user3.getLastName(),
                user3.getPatronymic(), user3.getLogin(), "", true, false, null, 5));

        ResponseEntity<List<GetUsersDtoResponse>> responseEntity =
                template.exchange("http://localhost:8080/api/accounts?sortByRating=asc&from=2", HttpMethod.GET,
                        new HttpEntity<String>(headers), new ParameterizedTypeReference<List<GetUsersDtoResponse>>(){});
        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(expectedList, responseEntity.getBody())
        );
    }

    @Test
    public void testGetAllUsersWithSortingFromAndCount() {
        createNotes();

        LoginDtoRequest loginRequest = new LoginDtoRequest("login0001", "password0001");
        String cookie = template.postForEntity("http://localhost:8080/api/sessions", loginRequest,
                Void.class).getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        List<GetUsersDtoResponse> expectedList = new ArrayList<>();
        expectedList.add(new GetUsersDtoResponse(user1.getId(), user1.getFirstName(), user1.getLastName(),
                user1.getPatronymic(), user1.getLogin(), "", true, false, null, 3));
        expectedList.add(new GetUsersDtoResponse(user2.getId(), user2.getFirstName(), user2.getLastName(),
                user2.getPatronymic(), user2.getLogin(), "", true, false, null, 4));

        ResponseEntity<List<GetUsersDtoResponse>> responseEntity =
                template.exchange("http://localhost:8080/api/accounts?sortByRating=asc&from=2&count=2", HttpMethod.GET,
                        new HttpEntity<String>(headers), new ParameterizedTypeReference<List<GetUsersDtoResponse>>(){});
        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(expectedList, responseEntity.getBody())
        );
    }



    private List<GetUsersDtoResponse> createExpectedList(User user1, User user2, User user3, User user4, User user5) {
        List<GetUsersDtoResponse> expectedList = new ArrayList<>();
        if (user1 != null)
            expectedList.add(new GetUsersDtoResponse(user1.getId(), user1.getFirstName(), user1.getLastName(),
                    user1.getPatronymic(), user1.getLogin(), "", true, false, null, 0));
        if (user2 != null)
            expectedList.add(new GetUsersDtoResponse(user2.getId(), user2.getFirstName(), user2.getLastName(),
                user2.getPatronymic(), user2.getLogin(), "", true, false, null, 0));
        if (user3 != null)
            expectedList.add(new GetUsersDtoResponse(user3.getId(), user3.getFirstName(), user3.getLastName(),
                user3.getPatronymic(), user3.getLogin(), "", true, false, null, 0));
        if (user4 != null)
            expectedList.add(new GetUsersDtoResponse(user4.getId(), user4.getFirstName(), user4.getLastName(),
                user4.getPatronymic(), user4.getLogin(), "", true, false, null, 0));
        if (user5 != null)
            expectedList.add(new GetUsersDtoResponse(user5.getId(), user5.getFirstName(), user5.getLastName(),
                user5.getPatronymic(), user5.getLogin(), "", true, false, null, 0));
        return expectedList;
    }

    private void createNotes() {
        String cookie = template.postForEntity("http://localhost:8080/api/sessions",
                new LoginDtoRequest("login0001", "password0001"), Void.class)
                .getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        SectionNameDtoRequest requestCreateSection1 = new SectionNameDtoRequest("section1");
        int sectionId = template.exchange("http://localhost:8080/api/sections", HttpMethod.POST,
                new HttpEntity<>(requestCreateSection1, headers), SectionDataDtoResponse.class).getBody().getId();

        CreateNoteDtoRequest requestCreateNote1 = new CreateNoteDtoRequest("subject1", "revision1", sectionId);
        int noteId1 =  template.exchange("http://localhost:8080/api/notes",
                HttpMethod.POST, new HttpEntity<>(requestCreateNote1, headers), GetNoteInfoDtoResponse.class).getBody().getId();

        cookie = template.postForEntity("http://localhost:8080/api/sessions",
                new LoginDtoRequest("login0002", "password0002"), Void.class)
                .getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        CreateNoteDtoRequest requestCreateNote2 = new CreateNoteDtoRequest("subject2", "revision1", sectionId);
        int noteId2 = template.exchange("http://localhost:8080/api/notes",
                HttpMethod.POST, new HttpEntity<>(requestCreateNote2, headers), GetNoteInfoDtoResponse.class).getBody().getId();

        cookie = template.postForEntity("http://localhost:8080/api/sessions",
                new LoginDtoRequest("login0003", "password0003"), Void.class)
                .getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        CreateNoteDtoRequest requestCreateNote3 = new CreateNoteDtoRequest("subject3", "revision1", sectionId);
        int noteId3 = template.exchange("http://localhost:8080/api/notes",
                HttpMethod.POST, new HttpEntity<>(requestCreateNote3, headers), GetNoteInfoDtoResponse.class).getBody().getId();

        cookie = template.postForEntity("http://localhost:8080/api/sessions",
                new LoginDtoRequest("login0004", "password0004"), Void.class)
                .getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        headers = new HttpHeaders();
        headers.add("Cookie", cookie);

        RateNoteDtoRequest requestRate1 = new RateNoteDtoRequest(3);
        template.exchange("http://localhost:8080/api/notes/{id}/rating", HttpMethod.POST,
                new HttpEntity<>(requestRate1, headers), Void.class, noteId1);
        RateNoteDtoRequest requestRate2 = new RateNoteDtoRequest(4);
        template.exchange("http://localhost:8080/api/notes/{id}/rating", HttpMethod.POST,
                new HttpEntity<>(requestRate2, headers), Void.class, noteId2);
        RateNoteDtoRequest requestRate3 = new RateNoteDtoRequest(5);
        template.exchange("http://localhost:8080/api/notes/{id}/rating", HttpMethod.POST,
                new HttpEntity<>(requestRate3, headers), Void.class, noteId3);
    }


    private ServerErrors createException(ServerErrorCode errorCode) {
        Set<ServerError> expectedErrors = new HashSet<>();
        expectedErrors.add(new ServerError(errorCode, errorCode.getField(),
                errorCode.getErrorMessage()));
        return new ServerErrors(expectedErrors);
    }
}
