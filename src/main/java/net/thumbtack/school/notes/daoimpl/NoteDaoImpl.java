package net.thumbtack.school.notes.daoimpl;

import lombok.extern.slf4j.Slf4j;
import net.thumbtack.school.notes.dao.NoteDao;
import net.thumbtack.school.notes.endpoint.request_param.SortOrder;
import net.thumbtack.school.notes.exception.ServerErrorCode;
import net.thumbtack.school.notes.exception.ServerException;
import net.thumbtack.school.notes.mapper.NoteMapper;
import net.thumbtack.school.notes.model.Comment;
import net.thumbtack.school.notes.model.Note;
import net.thumbtack.school.notes.model.Revision;
import net.thumbtack.school.notes.model.Section;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class NoteDaoImpl implements NoteDao {

    private final NoteMapper noteMapper;

    @Autowired
    public NoteDaoImpl(NoteMapper noteMapper) {
        this.noteMapper = noteMapper;
    }

    public void createSection(Section section, int ownerId) throws ServerException {
        log.debug("Dao insert Section {} with ownerId {}", section, ownerId);
        try {
            noteMapper.insertSection(section, ownerId);
        } catch (DuplicateKeyException ex) {
            log.info("Cannot insert section, because supplied name already busy");
            throw new ServerException(ServerErrorCode.SECTION_NAME_ALREADY_BUSY);
        }
    }

    public Section getSection(int sectionId) {
        log.debug("Dao get section with id {}", sectionId);
        return noteMapper.getSection(sectionId);
    }

    public List<Section> getSectionList() {
        log.debug("Dao get all sections");
        return noteMapper.getAllSection();
    }

    public void renameSection(Section section, String newName) throws ServerException {
        log.debug("Dao rename Section with id {} on newName {}", section.getId(), newName);
        try {
            noteMapper.renameSection(section, newName);
        } catch (DuplicateKeyException ex) {
            log.info("Cannot rename section, because supplied name already busy");
            throw new ServerException(ServerErrorCode.SECTION_NAME_ALREADY_BUSY);
        }
    }

    public void deleteSection(int sectionId) {
        log.debug("Dao delete Section with sectionId {}", sectionId);
        noteMapper.deleteSection(sectionId);
    }

    public void createNote(Note note, int ownerId, int sectionId) throws ServerException {
        log.debug("Dao insert Note {} with ownerId {} and sectionId {}", note, ownerId, sectionId);
        try {
            noteMapper.insertNote(note, ownerId, sectionId);
        } catch (DataIntegrityViolationException ex) {
            log.info("Cannot create note, because this sectionId eas not fount in DB");
            throw new ServerException(ServerErrorCode.THIS_SECTION_ID_NOT_FOUND);
        }
    }

    public void createRevision(Revision revision, int noteId) {
        log.debug("Dao insert Revision {} with noteId {}", revision, noteId);
        noteMapper.insertRevision(revision, noteId);
    }

    public Note getNote(int noteId) {
        log.debug("Dao get note with id {}", noteId);
        return noteMapper.getNote(noteId);
    }

    public void transferNote(int noteId, int sectionId) throws ServerException {
        log.debug("Dao transfer Note with noteId {} in section with sectionId {}", noteId, sectionId);
        try {
            noteMapper.transferNote(noteId, sectionId);
        } catch (DataIntegrityViolationException ex) {
            log.info("Cannot transfer note, because this sectionId eas not fount in DB");
            throw new ServerException(ServerErrorCode.THIS_SECTION_ID_NOT_FOUND);
        }
    }

    public void deleteNote(int noteId) {
        log.debug("Dao delete Note with noteId {}", noteId);
        noteMapper.deleteNote(noteId);
    }

    public void createComment(Comment comment, int ownerId, int revisionId) {
        log.debug("Dao insert Comment {} with revisionId {}", comment, revisionId);
        noteMapper.insertComment(comment, ownerId, revisionId);
    }

    public Comment getComment(int commentId) {
        log.debug("Dao get comment with id {}", commentId);
        return noteMapper.getComment(commentId);
    }

    public void editComment(Comment comment, String body) {
        log.debug("Dao edit comment with commentId {}", comment.getId());
        noteMapper.editComment(comment, body);
    }

    public void deleteComment(int commentId) {
        log.debug("Dao delete comment with commentId {}", commentId);
        noteMapper.deleteComment(commentId);
    }

    public void deleteCommentsNote(int revisionId) {
        log.debug("Dao delete comments note with revisionId {}", revisionId);
        noteMapper.deleteCommentsNote(revisionId);
    }

    public void rateNote(int noteId, int rating) {
        log.debug("Dao rate note with id {}", noteId);
        noteMapper.rateNote(noteId, rating);
    }

    public List<Note> getNoteList(Integer sectionId, SortOrder sortByRating, Integer userId, List<Integer> idUsers,
                                  LocalDateTime timeFrom, LocalDateTime timeTo, List<String> tags, boolean alltags,
                                  int from, Integer count) {
        log.debug("Dao get Note List");
        return noteMapper.getNoteList(sectionId, sortByRating, userId, idUsers, timeFrom, timeTo, tags, alltags, from, count);
    }
}
