package net.thumbtack.school.notes.debugging;

import net.thumbtack.school.notes.exception.ServerErrorCode;
import net.thumbtack.school.notes.exception.ServerException;
import net.thumbtack.school.notes.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
public class DebugDaoImpl {

    private final DebugMapper debugMapper;

    @Autowired
    public DebugDaoImpl(DebugMapper debugMapper) {
        this.debugMapper = debugMapper;
    }

    public void clearStateServer() {
        debugMapper.clearSection();
        debugMapper.clearUser();
    }

    public void registerAdmin(String sessionId, User user) throws ServerException {
        try {
            debugMapper.insertAdmin(user);
            debugMapper.insertSession(sessionId, user.getTimeRegistered(), user.getId());
        } catch (DuplicateKeyException ex) {
            throw new ServerException(ServerErrorCode.LOGIN_ALREADY_BUSY);
        }
    }
}
