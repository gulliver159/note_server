package net.thumbtack.school.notes.mapper;

import net.thumbtack.school.notes.endpoint.request_param.IncludeType;
import net.thumbtack.school.notes.endpoint.request_param.SearchParams;
import net.thumbtack.school.notes.endpoint.request_param.SortOrder;
import net.thumbtack.school.notes.model.Session;
import net.thumbtack.school.notes.model.User;
import net.thumbtack.school.notes.views.SessionView;
import net.thumbtack.school.notes.views.UserView;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserMapper {
    @Insert("INSERT INTO user(firstName, lastName, patronymic, login, password, userType, deleted, timeRegistered)" +
            "VALUES (#{user.firstName}, #{user.lastName}, #{user.patronymic}, #{user.login}," +
            "#{user.password}, #{user.userType}, #{user.deleted}, #{user.timeRegistered})")
    @Options(useGeneratedKeys = true, keyProperty = "user.id")
    void insertUser(@Param("user") User user) throws DuplicateKeyException;

    @Insert("INSERT INTO session(sessionId, timeLogin, userId) VALUES (#{session.sessionId}, #{session.timeLogin}, #{session.user.id})")
    void insertSession(@Param("session") Session session);

    @Delete("DELETE FROM session WHERE userId = #{userId}")
    void deleteSession(int userId);

    @Select("SELECT * FROM user WHERE id = #{id}")
    User getUserById(int id);

    @Select("SELECT sessionId, timeLogin, userId FROM session WHERE sessionId = #{sessionId}")
    @Result(property = "user", column = "userId", one = @One(select = "getUserById"))
    Session getSession(@Param("sessionId") String sessionId);

    @Update("UPDATE session SET timeLogin = #{currentTime} WHERE userId = #{userId}")
    void updateSession(@Param("userId") int userId, @Param("currentTime") LocalDateTime currentTime);

    @Select("SELECT id, userType, #{sortByRating} as sortByRating, #{from} as `from`, #{count} as count FROM user WHERE id = #{userId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "followings", column = "{id=id,sortByRating=sortByRating,from=from,count=count}", javaType = List.class,
                    many = @Many(select = "getFollowings", fetchType = FetchType.LAZY)),
            @Result(property = "followers", column = "{id=id,sortByRating=sortByRating,from=from,count=count}", javaType = List.class,
                    many = @Many(select = "getFollowers", fetchType = FetchType.LAZY)),
            @Result(property = "ignore", column = "{id=id,sortByRating=sortByRating,from=from,count=count}", javaType = List.class,
                    many = @Many(select = "getIgnore", fetchType = FetchType.LAZY)),
            @Result(property = "ignoreBy", column = "{id=id,sortByRating=sortByRating,from=from,count=count}", javaType = List.class,
                    many = @Many(select = "getIgnoreBy", fetchType = FetchType.LAZY))
    })
    UserView getUserViewById(@Param("userId") int userId, @Param("sortByRating") SortOrder sortByRating, @Param("from") int from,
                             @Param("count") Integer count);

    @Select("SELECT sessionId, timeLogin, userId, #{sortByRating} as sortByRating, #{from} as `from`, #{count} as count " +
            "FROM session WHERE sessionId = #{sessionId}")
    @Result(property = "user", column = "{userId=userId,sortByRating=sortByRating,from=from,count=count}", one = @One(select = "getUserViewById"))
    SessionView getSessionView(@Param("sessionId") String sessionId, @Param("sortByRating") SortOrder sortByRating,
                               @Param("from") int from, @Param("count") Integer count);

    @Select("SELECT * FROM user WHERE login = #{login} AND password = #{password} AND deleted = false")
    User getUserByLoginAndPassword(@Param("login") String login, @Param("password") String password);

    @Select("SELECT * FROM user WHERE login = #{login}")
    User getUserByLogin(String login);


    @Update("UPDATE user SET firstName = #{firstName}, lastName = #{lastName}," +
            "patronymic = #{patronymic}, password = #{newPassword} WHERE id = #{id}")
    void updateProfile(@Param("id") Integer id, @Param("firstName") String firstName,
                       @Param("lastName") String lastName, @Param("patronymic") String patronymic,
                       @Param("newPassword") String newPassword);

    @Update("UPDATE user SET deleted = true WHERE id = #{id}")
    void deleteUser(int id);

    @Update("UPDATE user SET userType = 'ADMIN' WHERE id = #{id}")
    void transferToSuperuser(int id);



    @Insert("INSERT INTO following(userId, followingId) VALUES (#{userId}, #{followingId})")
    void insertToFollowing(@Param("userId") int userId, @Param("followingId") int followingId);

    @Insert("INSERT INTO `ignore`(userId, ignoredId) VALUES (#{userId}, #{ignoredId})")
    void insertToIgnore(@Param("userId") int userId, @Param("ignoredId") int ignoredId);

    @Delete("DELETE FROM following WHERE userId = #{userId} AND followingId = #{followingId}")
    void deleteFromFollowing(@Param("userId") int userId, @Param("followingId") int followingId);

    @Delete("DELETE FROM `ignore` WHERE userId = #{userId} AND ignoredId = #{ignoredId}")
    void deleteFromIgnore(@Param("userId") int userId, @Param("ignoredId") int ignoredId);

    String parametersString = " <choose>" +
                        "<when test='sortByRating.toString() == \"ASC\"'> ORDER BY rating ASC" +
                        "</when>" +
                        "<when test='sortByRating.toString()  == \"DESC\"'> ORDER BY rating DESC" +
                        "</when>" +
                    "</choose>" +
                    "<choose>" +
                        "<when test='count == null'> LIMIT #{from}, 18446744073709551615" +
                        "</when>" +
                        "<when test='count != null'> LIMIT #{from}, #{count}" +
                        "</when>" +
                    "</choose>";

    String selectString = "SELECT id, firstName, lastName, patronymic, login, password, " +
            "(SELECT COALESCE(SUM(rating) / NULLIF(COUNT(id), 0), 0) FROM note WHERE ownerId = user.id) as rating, " +
            "userType, deleted, timeRegistered FROM user ";

    @Select({"<script>",
            selectString,
            parametersString,
            "</script>"})
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "online", column = "id", one = @One(select = "isOnline"))
    })
    List<UserView> getAllUsers(@Param("sortByRating") SortOrder sortByRating, @Param("from") int from,
                               @Param("count")Integer count);

    @Select({"<script>",
            selectString,
            "WHERE id in (SELECT followingId FROM following WHERE userId = #{id})",
            parametersString,
            "</script>"})
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "online", column = "id", one = @One(select = "isOnline"))
    })
    List<UserView> getFollowings(@Param("id") int id, @Param("sortByRating") SortOrder sortByRating, @Param("from") int from,
                                 @Param("count")Integer count);

    @Select({"<script>",
            selectString,
            "WHERE id in (SELECT userId FROM following WHERE followingId = #{id})",
            parametersString,
            "</script>"})
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "online", column = "id", one = @One(select = "isOnline"))
    })
    List<UserView> getFollowers(@Param("id") int id, @Param("sortByRating") SortOrder sortByRating, @Param("from") int from,
                                @Param("count")Integer count);

    @Select({"<script>",
            selectString,
            "WHERE id in (SELECT ignoredId FROM `ignore` WHERE userId = #{id})",
            parametersString,
            "</script>"})
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "online", column = "id", one = @One(select = "isOnline"))
    })
    List<UserView> getIgnore(@Param("id") int id, @Param("sortByRating") SortOrder sortByRating, @Param("from") int from,
                             @Param("count")Integer count);

    @Select({"<script>",
            selectString,
            "WHERE id in (SELECT userId FROM `ignore` WHERE ignoredId = #{id})",
            parametersString,
            "</script>"})
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "online", column = "id", one = @One(select = "isOnline"))
    })
    List<UserView> getIgnoreBy(@Param("id") int id, @Param("sortByRating") SortOrder sortByRating, @Param("from") int from,
                               @Param("count")Integer count);

    @Select({"<script>",
            selectString,
            "<where>" +
                "<if test='type.toString() == \"HIGH_RATING\"'> rating = (SELECT MAX(rating) FROM user WHERE rating != 0)",
                "</if>",
                "<if test='type.toString() == \"LOW_RATING\"'> rating = (SELECT MIN(rating) FROM user WHERE rating != 0)",
                "</if>",
            "</where>" +
            parametersString,
            "</script>"})
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "online", column = "id", one = @One(select = "isOnline"))
    })
    List<UserView> getUsersWithHighOrLowRating(@Param("type") SearchParams type, @Param("sortByRating") SortOrder sortByRating,
                                               @Param("from") int from, @Param("count") Integer count);

    @Select({"<script>",
            selectString + "WHERE deleted = true",
            parametersString,
            "</script>"})
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "online", column = "id", one = @One(select = "isOnline"))
    })
    List<UserView> getDeletedUsers(@Param("sortByRating") SortOrder sortByRating, @Param("from") int from,
                                   @Param("count") Integer count);

    @Select({"<script>",
            selectString + "WHERE userType = 'ADMIN'",
            parametersString,
            "</script>"})
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "online", column = "id", one = @One(select = "isOnline"))
    })
    List<UserView> getSuperUsers(@Param("sortByRating") SortOrder sortByRating, @Param("from") int from,
                                 @Param("count") Integer count);

    @Select("SELECT EXISTS(SELECT id FROM session WHERE userId = #{userId})")
    Boolean isOnline(int userId);

    @Select({"<script>",
            "<choose>",
                "<when test='include.toString() == \"ONLY_FOLLOWINGS\"'> SELECT followingId FROM following WHERE userId = #{userId}",
                "</when>",
                "<when test='include.toString() == \"ONLY_IGNORE\"'> SELECT ignoredId FROM `ignore` WHERE userId = #{userId}",
                "</when>",
                "<when test='include.toString() == \"NOT_IGNORE\"'> SELECT id FROM user ",
                        "WHERE id NOT IN (SELECT ignoredId FROM `ignore` WHERE userId = #{userId})",
                "</when>",
                "<when test='include.toString() == \"NONE\"'> SELECT id FROM user ",
                "</when>",
            "</choose>",
            "</script>"})
    List<Integer> getIdUsers(@Param("userId") int userId, @Param("include") IncludeType include);
}