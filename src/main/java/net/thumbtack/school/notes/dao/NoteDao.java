package net.thumbtack.school.notes.dao;

import net.thumbtack.school.notes.endpoint.request_param.SortOrder;
import net.thumbtack.school.notes.exception.ServerException;
import net.thumbtack.school.notes.model.Comment;
import net.thumbtack.school.notes.model.Note;
import net.thumbtack.school.notes.model.Revision;
import net.thumbtack.school.notes.model.Section;

import java.time.LocalDateTime;
import java.util.List;

public interface NoteDao {

    void createSection(Section section, int userId) throws ServerException;

    Section getSection(int sectionId);

    List<Section> getSectionList();

    void renameSection(Section section, String newName) throws ServerException;

    void deleteSection(int sectionId);

    void createNote(Note note, int ownerId, int sectionId) throws ServerException;

    void createRevision(Revision revision, int noteId);

    Note getNote(int noteId);

    void transferNote(int noteId, int sectionId) throws ServerException;

    void deleteNote(int noteId);

    void createComment(Comment comment, int ownerId, int revisionId);

    Comment getComment(int commentId);

    void editComment(Comment comment, String body);

    void deleteComment(int commentId);

    void deleteCommentsNote(int revisionId);

    void rateNote(int noteId, int rating);

    List<Note> getNoteList(Integer sectionId, SortOrder sortByRating, Integer userId, List<Integer> idUsers,
                           LocalDateTime timeFrom, LocalDateTime timeTo, List<String> tags, boolean alltags, int from, Integer count);
}
