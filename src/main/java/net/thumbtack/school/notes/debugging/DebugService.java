package net.thumbtack.school.notes.debugging;

import lombok.extern.slf4j.Slf4j;
import net.thumbtack.school.notes.dto.requests.user.RegisterUserDtoRequest;
import net.thumbtack.school.notes.exception.ServerException;
import net.thumbtack.school.notes.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Slf4j
public class DebugService {

    private final DebugDaoImpl debugDao;

    @Value("${max_name_length}")
    private int maxNameLength;

    @Value("${min_password_length}")
    private int minPasswordLength;

    @Value("${user_idle_timeout}")
    private int userIdleTimeout;

    @Autowired
    public DebugService(DebugDaoImpl debugDao) {
        this.debugDao = debugDao;
    }


    public void clearStateServer() {
        debugDao.clearStateServer();
    }

    public String registerAdmin(RegisterUserDtoRequest request) throws ServerException {
        User user = new User(request.getFirstName(), request.getLastName(), request.getPatronymic(),
                request.getLogin(), request.getPassword());
        String sessionId = UUID.randomUUID().toString();
        user.setTimeRegistered(LocalDateTime.now());
        debugDao.registerAdmin(sessionId, user);
        return sessionId;
    }

    public GetServerSettingsDtoResponse getServerSettings() {
        return new GetServerSettingsDtoResponse(maxNameLength, minPasswordLength, userIdleTimeout);
    }
}
