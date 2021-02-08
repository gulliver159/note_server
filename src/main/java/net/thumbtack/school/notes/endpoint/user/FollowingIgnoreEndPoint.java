package net.thumbtack.school.notes.endpoint.user;

import lombok.extern.slf4j.Slf4j;
import net.thumbtack.school.notes.dto.requests.user.AddToListDtoRequest;
import net.thumbtack.school.notes.exception.ServerException;
import net.thumbtack.school.notes.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@SpringBootApplication
@RequestMapping("/api")
public class FollowingIgnoreEndPoint {

    private final UserService userService;

    @Autowired
    public FollowingIgnoreEndPoint(UserService userService) {
        this.userService = userService;
    }


    @PostMapping(value = "/followings", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addToFollowing(@RequestBody @Valid AddToListDtoRequest addToListDtoRequest,
             @CookieValue(value = "JAVASESSIONID") String sessionId) throws ServerException {
        log.debug("Accepted the post request addToFollowing");
        userService.addToFollowing(sessionId, addToListDtoRequest);
        log.debug("Executed the post request addToFollowing");
    }

    @PostMapping(value = "/ignore", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addToIgnore(@RequestBody @Valid AddToListDtoRequest addToListDtoRequest,
             @CookieValue(value = "JAVASESSIONID") String sessionId) throws ServerException {
        log.debug("Accepted the post request addToIgnore");
        userService.addToIgnore(sessionId, addToListDtoRequest);
        log.debug("Executed the post request addToIgnore");
    }

    @DeleteMapping(value = "/followings/{login}")
    public void deleteFromFollowing(@PathVariable("login") String login,
             @CookieValue(value = "JAVASESSIONID") String sessionId) throws ServerException {
        log.debug("Accepted the delete request deleteFromFollowing");
        userService.deleteFromFollowing(sessionId, login);
        log.debug("Executed the delete request deleteFromFollowing");
    }

    @DeleteMapping(value = "/ignore/{login}")
    public void deleteFromIgnore(@PathVariable("login") String login,
             @CookieValue(value = "JAVASESSIONID") String sessionId) throws ServerException {
        log.debug("Accepted the delete request deleteFromIgnore");
        userService.deleteFromIgnore(sessionId, login);
        log.debug("Executed the delete request deleteFromIgnore");
    }

}
