package net.thumbtack.school.notes.debugging;

import net.thumbtack.school.notes.model.User;
import org.apache.ibatis.annotations.*;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;

@Mapper
public interface DebugMapper {

    @Delete("DELETE FROM user;")
    void clearUser();

    @Delete("DELETE FROM section;")
    void clearSection();

    @Insert("INSERT INTO user(firstName, lastName, patronymic, login, password, userType, deleted, timeRegistered)" +
            "VALUES (#{user.firstName}, #{user.lastName}, #{user.patronymic}, #{user.login}," +
            "#{user.password}, 'ADMIN', #{user.deleted}, #{user.timeRegistered})")
    @Options(useGeneratedKeys = true, keyProperty = "user.id")
    void insertAdmin(@Param("user") User user) throws DuplicateKeyException;

    @Insert("INSERT INTO session(sessionId, timeLogin, userId) VALUES (#{sessionId}, #{timeLogin}, #{userId})")
    void insertSession(@Param("sessionId") String sessionId, @Param("timeLogin") LocalDateTime timeLogin,
                       @Param("userId") int userId);
}
