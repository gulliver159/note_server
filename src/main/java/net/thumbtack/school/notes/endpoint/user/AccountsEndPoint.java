package net.thumbtack.school.notes.endpoint.user;

import lombok.extern.slf4j.Slf4j;
import net.thumbtack.school.notes.dto.requests.user.DeleteUserDtoRequest;
import net.thumbtack.school.notes.dto.requests.user.EditUserProfileDtoRequest;
import net.thumbtack.school.notes.dto.requests.user.RegisterUserDtoRequest;
import net.thumbtack.school.notes.dto.responses.user.EditUserProfileDtoResponse;
import net.thumbtack.school.notes.dto.responses.user.GetInfoOfUserDtoResponse;
import net.thumbtack.school.notes.dto.responses.user.GetUsersDtoResponse;
import net.thumbtack.school.notes.dto.responses.user.RegisterUserDtoResponse;
import net.thumbtack.school.notes.endpoint.request_param.SearchParams;
import net.thumbtack.school.notes.endpoint.request_param.SortOrder;
import net.thumbtack.school.notes.exception.ServerErrorCode;
import net.thumbtack.school.notes.exception.ServerException;
import net.thumbtack.school.notes.service.UserService;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@SpringBootApplication
@RequestMapping("/api")
public class AccountsEndPoint {

    private final UserService userService;

    @Autowired
    public AccountsEndPoint(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/accounts", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public RegisterUserDtoResponse registerUser(@RequestBody @Valid RegisterUserDtoRequest registerUserDtoRequest,
                                                HttpServletResponse responseCookie) throws ServerException {
        log.debug("Accepted the post request registerUser");
        Pair<String, RegisterUserDtoResponse> responsePair = userService.registerUser(registerUserDtoRequest);
        Cookie cookie = new Cookie("JAVASESSIONID", responsePair.getLeft());
        responseCookie.addCookie(cookie);
        log.debug("Executed the post request registerUser");
        return responsePair.getRight();
    }

    @PutMapping(value = "/accounts", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public EditUserProfileDtoResponse editUserProfile
            (@RequestBody @Valid EditUserProfileDtoRequest editUserProfileDtoRequest,
             @CookieValue(value = "JAVASESSIONID") String sessionId) throws ServerException {
        log.debug("Accepted the put request editingUserProfile");
        EditUserProfileDtoResponse response = userService.editUserProfile(sessionId, editUserProfileDtoRequest);
        log.debug("Executed the put request editingUserProfile");
        return response;
    }

    @PutMapping(value = "/accounts/{id}/super")
    public void transferToSuperuser(@PathVariable("id") int id,
                                        @CookieValue(value = "JAVASESSIONID") String sessionId) throws ServerException {
        log.debug("Accepted the put request transferToSuperuser");
        userService.transferToSuperuser(sessionId, id);
        log.debug("Executed the put request transferToSuperuser");
    }

    @DeleteMapping(value = "/accounts", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void deleteUser(@RequestBody @Valid DeleteUserDtoRequest deleteUserDtoRequest,
                           @CookieValue(value = "JAVASESSIONID") String sessionId) throws ServerException {
        log.debug("Accepted the delete request deleteUserDtoRequest");
        userService.deleteUser(sessionId, deleteUserDtoRequest);
        log.debug("Executed the delete request deleteUserDtoRequest");
    }

    @GetMapping(value = "/account", produces = MediaType.APPLICATION_JSON_VALUE)
    public GetInfoOfUserDtoResponse getUserInfo(@CookieValue(value = "JAVASESSIONID") String sessionId) throws ServerException {
        log.debug("Accepted the get request getUserInfo");
        GetInfoOfUserDtoResponse response = userService.getUserInfo(sessionId);
        log.debug("Executed the get request getUserInfo");
        return response;
    }

    @GetMapping(value = "/accounts", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<GetUsersDtoResponse> getUserList(@RequestParam Map<String, String> allParams,
                                                    @CookieValue(value = "JAVASESSIONID") String sessionId) throws ServerException {
        log.debug("Accepted the get request getListOfUsers");
        Map<String, SearchParams> searchParamsMap = createSearchParamsMap();

        try {
            SortOrder sortByRating = (allParams.get("sortByRating") == null) ? SortOrder.NONE :
                    SortOrder.valueOf(allParams.remove("sortByRating").toUpperCase());
            SearchParams type = (allParams.get("type") == null) ? SearchParams.NONE :
                    searchParamsMap.get(allParams.remove("type"));
            int from = (allParams.get("from") == null) ? 0 : Integer.parseInt(allParams.remove("from"));
            Integer count = (allParams.get("count") == null) ? null : Integer.parseInt(allParams.remove("count"));

            if (type == null) {
                throw new IllegalArgumentException();
            }


            if (allParams.keySet().size() != 0) {
                log.info("Cannot execute the get request getListOfUsers due to invalid search parameters");
                throw new ServerException(ServerErrorCode.WRONG_SEARCH_PARAM);
            }
            List<GetUsersDtoResponse> response = userService.getUserList(sessionId, sortByRating, type, from, count);
            log.debug("Executed the get request getListOfUsers");
            return response;
        } catch (IllegalArgumentException ex) {
            log.info("Cannot execute the get request getListOfUsers due to invalid search parameter values");
            throw new ServerException(ServerErrorCode.INVALID_PARAM_VALUE);
        }
    }


    private Map<String, SearchParams> createSearchParamsMap() {
        Map<String, SearchParams> searchParamsMap = new HashMap<>();
        searchParamsMap.put("highRating", SearchParams.HIGH_RATING);
        searchParamsMap.put("lowRating", SearchParams.LOW_RATING);
        searchParamsMap.put("followings", SearchParams.FOLLOWINGS);
        searchParamsMap.put("followers", SearchParams.FOLLOWERS);
        searchParamsMap.put("ignore", SearchParams.IGNORE);
        searchParamsMap.put("ignoredBy", SearchParams.IGNORED_BY);
        searchParamsMap.put("deleted", SearchParams.DELETED);
        searchParamsMap.put("super", SearchParams.SUPER);
        return searchParamsMap;
    }
}
