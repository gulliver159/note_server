package net.thumbtack.school.notes.debugging;

import lombok.extern.slf4j.Slf4j;
import net.thumbtack.school.notes.dto.requests.user.RegisterUserDtoRequest;
import net.thumbtack.school.notes.dto.responses.note.GetCommentInfoDtoResponse;
import net.thumbtack.school.notes.exception.ServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@SpringBootApplication
@RequestMapping("/api/debug")
public class DebugEndPoint {

    private final DebugService debugService;

    @Autowired
    public DebugEndPoint(DebugService debugService) {
        this.debugService = debugService;
    }

    @PostMapping(value = "/clear")
    public void clearStateServer() {
        log.debug("Accepted the post request clearStateServer");
        debugService.clearStateServer();
        log.debug("Executed the post request clearStateServer");
    }

    @PostMapping(value = "/registerAdmin")
    public void registerAdmin(@RequestBody @Valid RegisterUserDtoRequest registerUserDtoRequest,
                              HttpServletResponse responseCookie) throws ServerException {
        log.debug("Accepted the post request registerAdmin");
        String cookieString = debugService.registerAdmin(registerUserDtoRequest);
        Cookie cookie = new Cookie("JAVASESSIONID", cookieString);
        responseCookie.addCookie(cookie);
        log.debug("Executed the post request registerAdmin");
    }

    @GetMapping(value = "/settings", produces = MediaType.APPLICATION_JSON_VALUE)
    public GetServerSettingsDtoResponse getServerSettings() throws IOException {
        log.debug("Accepted the get request getServerSettings");
        GetServerSettingsDtoResponse response = debugService.getServerSettings();
        log.debug("Executed the get request getServerSettings");
        return response;
    }
}
