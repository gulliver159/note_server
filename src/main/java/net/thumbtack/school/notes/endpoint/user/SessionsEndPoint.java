package net.thumbtack.school.notes.endpoint.user;

import lombok.extern.slf4j.Slf4j;
import net.thumbtack.school.notes.dto.requests.user.LoginDtoRequest;
import net.thumbtack.school.notes.exception.ServerException;
import net.thumbtack.school.notes.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Slf4j
@RestController
@SpringBootApplication
@RequestMapping("/api/sessions")
public class SessionsEndPoint {

    private final UserService userService;

    @Autowired
    public SessionsEndPoint(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void login(@RequestBody @Valid LoginDtoRequest loginDtoRequest,
                                                HttpServletResponse responseCookie) throws ServerException {
        log.debug("Accepted the post request login");
        String cookieString = userService.login(loginDtoRequest);
        Cookie cookie = new Cookie("JAVASESSIONID", cookieString);
        responseCookie.addCookie(cookie);
        log.debug("Executed the post request login");
    }

    @DeleteMapping()
    public void logout(@CookieValue(value = "JAVASESSIONID") String sessionId) throws ServerException {
        log.debug("Accepted the delete request logout");
        userService.logout(sessionId);
        log.debug("Executed the delete request logout");
    }
}
