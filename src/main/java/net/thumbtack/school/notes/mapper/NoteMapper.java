package net.thumbtack.school.notes.mapper;

import net.thumbtack.school.notes.endpoint.request_param.SortOrder;
import net.thumbtack.school.notes.model.Comment;
import net.thumbtack.school.notes.model.Note;
import net.thumbtack.school.notes.model.Revision;
import net.thumbtack.school.notes.model.Section;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface NoteMapper {
    @Insert("INSERT INTO section(name, ownerId) VALUES (#{section.name}, #{ownerId})")
    @Options(useGeneratedKeys = true, keyProperty = "section.id")
    void insertSection(@Param("section") Section section, @Param("ownerId") int ownerId) throws DuplicateKeyException;

    @Select("SELECT id, name, ownerId FROM section WHERE id = #{id}")
    @Result(property = "owner", column = "ownerId", one = @One(select = "net.thumbtack.school.notes.mapper.UserMapper.getUserById"))
    Section getSection(int id);

    @Select("SELECT id, name FROM section")
    List<Section> getAllSection();

    @Update("UPDATE section SET name = #{newName} WHERE id = #{section.id}")
    void renameSection(@Param("section") Section section, @Param("newName") String newName);

    @Delete("DELETE FROM section WHERE id = #{sectionId}")
    void deleteSection(int sectionId);

    @Insert("INSERT INTO note(subject, timeCreated, ownerId, sectionId) VALUES (#{note.subject}, #{note.timeCreated}," +
            " #{ownerId}, #{sectionId})")
    @Options(useGeneratedKeys = true, keyProperty = "note.id")
    void insertNote(@Param("note") Note note, @Param("ownerId") int ownerId, @Param("sectionId") int sectionId) throws DataIntegrityViolationException;

    @Insert("INSERT INTO revision(body, noteId) VALUES (#{revision.body}, #{noteId})")
    void insertRevision(@Param("revision") Revision revision, @Param("noteId") int noteId);

    @Select("SELECT id, subject, rating, timeCreated, sectionId, ownerId FROM note WHERE id = #{noteId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "revisions", column = "id", javaType = List.class,
                    many = @Many(select = "getRevisions", fetchType = FetchType.LAZY)),
            @Result(property = "section", column = "sectionId", one = @One(select = "getSection")),
            @Result(property = "owner", column = "ownerId",
                    one = @One(select = "net.thumbtack.school.notes.mapper.UserMapper.getUserById"))
    })
    Note getNote(int noteId);

    @Select("SELECT id, body, id as idForSelect, " +
            "(SELECT COUNT(id) FROM revision WHERE noteId = #{noteId} AND id < idForSelect) + 1 as revisionIdForNote " +
            "FROM revision WHERE noteId = #{noteId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "comments", column = "id", javaType = List.class,
                    many = @Many(select = "getComments", fetchType = FetchType.LAZY))
    })
    List<Revision> getRevisions(int noteId);

    @Select("SELECT id, body, timeCreated, ownerId FROM comment WHERE revisionId = #{revisionId}")
    @Result(property = "owner", column = "ownerId",
            one = @One(select = "net.thumbtack.school.notes.mapper.UserMapper.getUserById"))
    List<Comment> getComments(int revisionId);

    @Update("UPDATE note SET sectionId = #{sectionId} WHERE id = #{noteId}")
    void transferNote(@Param("noteId") int noteId, @Param("sectionId") int sectionId) throws DataIntegrityViolationException;

    @Delete("DELETE FROM note WHERE id = #{noteId}")
    void deleteNote(int noteId);

    @Insert("INSERT INTO comment(body, timeCreated, ownerId, revisionId) VALUES (#{comment.body}, #{comment.timeCreated}," +
            " #{ownerId}, #{revisionId})")
    @Options(useGeneratedKeys = true, keyProperty = "comment.id")
    void insertComment(@Param("comment") Comment comment, @Param("ownerId") int ownerId, @Param("revisionId") int revisionId) throws DataIntegrityViolationException;

    @Select("SELECT id, body, timeCreated, revisionId, ownerId FROM comment WHERE id = #{commentId}")
    @Results({
            @Result(property = "revision", column = "revisionId", one = @One(select = "getRevision")),
            @Result(property = "owner", column = "ownerId",
                    one = @One(select = "net.thumbtack.school.notes.mapper.UserMapper.getUserById"))
    })
    Comment getComment(int commentId);

    @Select("SELECT id, body, (SELECT COUNT(id) FROM revision WHERE noteId = (SELECT noteId FROM revision WHERE id = #{revisionId}) " +
            "AND id <= #{revisionId}) AS revisionIdForNote, noteId FROM revision WHERE id = #{revisionId}")
    @Result(property = "note", column = "noteId", one = @One(select = "getNote"))
    Revision getRevision(int revisionId);

    @Update("UPDATE comment SET body = #{body}, " +
            "revisionId = (SELECT MAX(id) FROM revision WHERE noteId = (SELECT noteId FROM revision WHERE id = revisionId)) " +
            "WHERE id = #{comment.id}")
    void editComment(@Param("comment") Comment comment, @Param("body") String body);

    @Delete("DELETE FROM comment WHERE id = #{commentId}")
    void deleteComment(int commentId);

    @Delete("DELETE FROM comment WHERE revisionId = #{revisionId}")
    void deleteCommentsNote(int revisionId);

    @Update("UPDATE note SET rating = ((rating * numberOfRatings + #{newRating})/(numberOfRatings + 1)), " +
            "numberOfRatings = numberOfRatings + 1 WHERE id = #{noteId}")
    void rateNote(@Param("noteId") int noteId, @Param("newRating") int newRating);


    @Select({"<script>",
            "SELECT id, subject, rating, timeCreated, sectionId, ownerId ",
                    " FROM note ",
            "<where>",
                "<if test='sectionId != null'> sectionId = #{sectionId}",
                "</if>",
                "<if test='userId != null'> AND ownerId = #{userId}",
                "</if>",
                "<if test='userId == null'> AND ownerId IN ",
                    "<if test='idUsers.size() == 0'> (0)",
                    "</if>",
                    "<if test='idUsers.size() != 0'>",
                        "<foreach item='item' index='index' collection='idUsers' ",
                        "open='(' separator=',' close=')'>",
                            "#{item}",
                        "</foreach>",
                    "</if>",
                "</if>",
                "<if test='timeFrom != null'> AND timeCreated &gt;= #{timeFrom} ",
                "</if>",
                "<if test='timeTo != null'> AND timeCreated &lt;= #{timeTo} ",
                "</if>",
                "<if test='alltags == true and tags != null'> AND ",
                    "<foreach item='item' index='index' collection='tags' ",
                    "separator=' AND '>",
                        "(SELECT body FROM revision WHERE id = (SELECT MAX(id) FROM revision WHERE noteId = note.id)) ",
                                "like (CONCAT('%', #{item}, '%'))",
                    "</foreach>",
                "</if>",
                "<if test='alltags == false and tags != null'> AND ",
                    "<foreach item='item' index='index' collection='tags' ",
                    "separator=' OR '>",
                        "(SELECT body FROM revision WHERE id = (SELECT MAX(id) FROM revision WHERE noteId = note.id)) ",
                                "like (CONCAT('%', #{item}, '%'))",
                    "</foreach>",
                "</if>",
            "</where>",

            "<choose>",
                "<when test='sortByRating.toString() == \"ASC\"'> ORDER BY rating ASC",
                "</when>",
                "<when test='sortByRating.toString()  == \"DESC\"'> ORDER BY rating DESC",
                "</when>",
            "</choose>",
            "<choose>",
                "<when test='count == null'> LIMIT #{from}, 18446744073709551615",
                "</when>",
                "<when test='count != null'> LIMIT #{from}, #{count}",
                "</when>",
            "</choose>",
            "</script>"})
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "revisions", column = "id", javaType = List.class,
                    many = @Many(select = "getRevisions", fetchType = FetchType.LAZY)),
            @Result(property = "section", column = "sectionId", one = @One(select = "getSection")),
            @Result(property = "owner", column = "ownerId",
                    one = @One(select = "net.thumbtack.school.notes.mapper.UserMapper.getUserById"))
    })
    List<Note> getNoteList(@Param("sectionId") Integer sectionId, @Param("sortByRating") SortOrder sortByRating,
                           @Param("userId") Integer userId, @Param("idUsers") List<Integer> idUsers,
                           @Param("timeFrom") LocalDateTime timeFrom, @Param("timeTo") LocalDateTime timeTo,
                           @Param("tags") List<String> tags, @Param("alltags") boolean alltags,
                           @Param("from") int from, @Param("count") Integer count);
}