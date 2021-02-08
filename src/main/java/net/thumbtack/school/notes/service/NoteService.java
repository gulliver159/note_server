package net.thumbtack.school.notes.service;

import lombok.extern.slf4j.Slf4j;
import net.thumbtack.school.notes.dao.NoteDao;
import net.thumbtack.school.notes.dao.UserDao;
import net.thumbtack.school.notes.dto.requests.note.*;
import net.thumbtack.school.notes.dto.responses.note.GetCommentInfoDtoResponse;
import net.thumbtack.school.notes.dto.responses.note.GetNoteInfoDtoResponse;
import net.thumbtack.school.notes.dto.responses.note.GetCommentDtoResponse;
import net.thumbtack.school.notes.dto.responses.note.GetNoteListDtoResponse;
import net.thumbtack.school.notes.dto.responses.note.GetRevisionDtoResponse;
import net.thumbtack.school.notes.dto.responses.note.SectionDataDtoResponse;
import net.thumbtack.school.notes.endpoint.request_param.IncludeType;
import net.thumbtack.school.notes.endpoint.request_param.SortOrder;
import net.thumbtack.school.notes.exception.ServerErrorCode;
import net.thumbtack.school.notes.exception.ServerException;
import net.thumbtack.school.notes.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Transactional(rollbackFor = Exception.class)
public class NoteService {

    private final UserDao userDao;
    private final NoteDao noteDao;

    @Value("${user_idle_timeout}")
    private int userIdleTimeout;

    @Autowired
    public NoteService(UserDao userDao, NoteDao noteDao) throws IOException {
        this.userDao = userDao;
        this.noteDao = noteDao;
    }

    public SectionDataDtoResponse createSection(String sessionId, SectionNameDtoRequest request) throws ServerException {
        log.debug("Execute createSection with name {} by user with sessionId {}", request.getName(), sessionId);
        User user = getUserBySessionId(sessionId);
        Section section = new Section(request.getName());
        noteDao.createSection(section, user.getId());
        return new SectionDataDtoResponse(section.getId(), section.getName());
    }

    public SectionDataDtoResponse renameSection(String sessionId, int sectionId, SectionNameDtoRequest request) throws ServerException {
        log.debug("Execute renameSection with id {} by user with sessionId {}", sectionId, sessionId);
        User user = getUserBySessionId(sessionId);
        Section section = getSection(sectionId);
        checkIsOwner(user, section);
        noteDao.renameSection(section, request.getName());
        return new SectionDataDtoResponse(sectionId, request.getName());
    }

    public void deleteSection(String sessionId, int sectionId) throws ServerException {
        log.debug("Execute deleteSection with id {} by user with sessionId {}", sectionId, sessionId);
        User user = getUserBySessionId(sessionId);
        Section section = getSection(sectionId);
        checkIsAdminOrOwner(user, section);
        noteDao.deleteSection(sectionId);
    }

    public SectionDataDtoResponse getSectionInfo(String sessionId, int sectionId) throws ServerException {
        log.debug("Execute getSectionInfo with id {} by user with sessionId {}", sectionId, sessionId);
        getUserBySessionId(sessionId);
        Section section = getSection(sectionId);
        return new SectionDataDtoResponse(section.getId(), section.getName());
    }

    public List<SectionDataDtoResponse> getSectionList(String sessionId) throws ServerException {
        log.debug("Execute getSectionList by user with sessionId {}", sessionId);
        getUserBySessionId(sessionId);
        List<Section> sections = noteDao.getSectionList();

        List<SectionDataDtoResponse> response = new ArrayList<>();
        for (Section section : sections) {
            response.add(new SectionDataDtoResponse(section.getId(), section.getName()));
        }
        return response;
    }

    public GetNoteInfoDtoResponse createNote(String sessionId, CreateNoteDtoRequest request) throws ServerException {
        log.debug("Execute createNote by user with sessionId {}", sessionId);
        User user = getUserBySessionId(sessionId);

        LocalDateTime timeCreated = LocalDateTime.now();
        Note note = new Note(request.getSubject(), timeCreated);
        Revision revision = new Revision(request.getBody());
        noteDao.createNote(note, user.getId(), request.getSectionId());
        noteDao.createRevision(revision, note.getId());
        return new GetNoteInfoDtoResponse(note.getId(), request.getSubject(), request.getBody(), request.getSectionId(),
                user.getId(), timeCreated.toString(), 1);
    }

    public GetNoteInfoDtoResponse getNoteInfo(String sessionId, int noteId) throws ServerException {
        log.debug("Execute getNoteInfo with id {} by user with sessionId {}", noteId, sessionId);
        getUserBySessionId(sessionId);
        Note note = getNote(noteId);
        int numberCurrentRevision = note.getRevisions().size() - 1;
        Revision revision = note.getRevisions().get(numberCurrentRevision);
        return new GetNoteInfoDtoResponse(note.getId(), note.getSubject(), revision.getBody(), note.getSection().getId(),
                note.getOwner().getId(), note.getTimeCreated().toString(), numberCurrentRevision + 1);
    }

    public GetNoteInfoDtoResponse editOrTransferNote(String sessionId, int noteId, EditOrTransferNoteDtoRequest request) throws ServerException {
        log.debug("Execute editOrTransferNote with id {} by user with sessionId {}", noteId, sessionId);
        User user = getUserBySessionId(sessionId);
        Note note = getNote(noteId);
        int currentRevisionNumber = note.getRevisions().size() - 1;
        Revision revision = note.getRevisions().get(currentRevisionNumber);
        if (request.getBody() != null) {
            checkIsOwner(user, note);
            revision.setBody(request.getBody());
            currentRevisionNumber++;
            noteDao.createRevision(revision, noteId);
        }

        if (request.getSectionId() != null) {
            checkIsAdminOrOwner(user, note);
            noteDao.transferNote(noteId, request.getSectionId());
            note.getSection().setId(request.getSectionId());
        }
        return new GetNoteInfoDtoResponse(noteId, note.getSubject(), revision.getBody(), note.getSection().getId(),
                note.getOwner().getId(), note.getTimeCreated().toString(), currentRevisionNumber + 1);
    }

    public void deleteNote(String sessionId, int noteId) throws ServerException {
        log.debug("Execute deleteNote with id {} by user with sessionId {}", noteId, sessionId);
        User user = getUserBySessionId(sessionId);
        Note note = getNote(noteId);
        checkIsAdminOrOwner(user, note);
        noteDao.deleteNote(noteId);
    }

    public GetCommentInfoDtoResponse createComment(String sessionId, CreateCommentDtoRequest request) throws ServerException {
        log.debug("Execute createComment in note with id {} by user with sessionId {}", request.getNoteId(), sessionId);
        User user = getUserBySessionId(sessionId);
        Note note = getNote(request.getNoteId());
        int numberCurrentRevision = note.getRevisions().size() - 1;
        Revision revision = note.getRevisions().get(numberCurrentRevision);

        LocalDateTime timeCreated = LocalDateTime.now();
        Comment comment = new Comment(request.getBody(), timeCreated);
        noteDao.createComment(comment, user.getId(), revision.getId());
        return new GetCommentInfoDtoResponse(comment.getId(), comment.getBody(), note.getId(), user.getId(),
                numberCurrentRevision + 1, timeCreated.toString());
    }

    public List<GetCommentInfoDtoResponse> getCommentsNotes(String sessionId, int noteId) throws ServerException {
        log.debug("Execute getCommentsNotes with id {} by user with sessionId {}", noteId, sessionId);
        getUserBySessionId(sessionId);
        Note note = getNote(noteId);

        List<GetCommentInfoDtoResponse> response = new ArrayList<>();
        for (Revision revision : note.getRevisions()) {
            for (Comment comment : revision.getComments())
                response.add(new GetCommentInfoDtoResponse(comment.getId(), comment.getBody(), noteId, comment.getOwner().getId(),
                    revision.getRevisionIdForNote(), comment.getTimeCreated().toString()));
        }
        return response;
    }

    public GetCommentInfoDtoResponse editComment(String sessionId, int commentId, EditCommentDtoRequest request) throws ServerException {
        log.debug("Execute editComment with id {} by user with sessionId {}", commentId, sessionId);
        User user = getUserBySessionId(sessionId);
        Comment comment = getComment(commentId);
        checkIsOwner(user, comment);
        noteDao.editComment(comment, request.getBody());
        comment.setBody(request.getBody());
        return new GetCommentInfoDtoResponse(comment.getId(), comment.getBody(), comment.getRevision().getNote().getId(),
                comment.getOwner().getId(), comment.getRevision().getRevisionIdForNote(), comment.getTimeCreated().toString());
    }

    public void deleteComment(String sessionId, int commentId) throws ServerException {
        log.debug("Execute deleteComment with id {} by user with sessionId {}", commentId, sessionId);
        User user = getUserBySessionId(sessionId);
        Comment comment = getComment(commentId);
        Note note = comment.getRevision().getNote();
        checkIsOwnerNoteOrCommentOrAdmin(user, comment, note);
        noteDao.deleteComment(commentId);
    }

    public void deleteCommentsNote(String sessionId, int noteId) throws ServerException {
        log.debug("Execute deleteCommentsNotes with id {} by user with sessionId {}", noteId, sessionId);
        User user = getUserBySessionId(sessionId);
        Note note = getNote(noteId);
        checkIsOwner(user, note);
        noteDao.deleteCommentsNote(note.getRevisions().get(note.getRevisions().size() - 1).getId());
    }

    public void rateNote(String sessionId, int noteId, RateNoteDtoRequest request) throws ServerException {
        log.debug("Execute rateNote with id {} by user with sessionId {}", noteId, sessionId);
        User user = getUserBySessionId(sessionId);
        Note note = getNote(noteId);
        checkUserNotOwner(user, note);
        noteDao.rateNote(noteId, request.getRating());
    }

    public List<GetNoteListDtoResponse> getNoteList(String sessionId, Integer sectionId, SortOrder sortByRating,
                            List<String> tags, boolean alltags, LocalDateTime timeFrom, LocalDateTime timeTo, Integer userId,
                            IncludeType include, boolean comments, boolean allVersions, boolean commentVersion,
                            int from, Integer count) throws ServerException {
        User user = getUserBySessionId(sessionId);
        List<Integer> idUsers = new ArrayList<>();
        if (userId == null) {
            idUsers = userDao.getIdUsers(user.getId(), include);
        }
        List<Note> notes = noteDao.getNoteList(sectionId, sortByRating, userId, idUsers, timeFrom, timeTo, tags, alltags,
                from, count);
        List<GetNoteListDtoResponse> response = createNoteResponseList(notes, comments, allVersions, commentVersion);
        return response;
    }

    private List<GetNoteListDtoResponse> createNoteResponseList(List<Note> notes, boolean comments, boolean allVersions,
                                                                boolean commentVersion) {
        List<GetNoteListDtoResponse> response = new ArrayList<>();
        for (Note note : notes) {
            String timeCreated = note.getTimeCreated().toString();

            List<GetRevisionDtoResponse> revisionsResponse = null;
            if (allVersions || comments) {
                revisionsResponse = new ArrayList<>();
                for (Revision revision : note.getRevisions()) {

                    List<GetCommentDtoResponse> commentsResponse = null;
                    if (comments) {
                        commentsResponse = new ArrayList<>();
                        for (Comment comment : revision.getComments()) {
                            commentsResponse.add(new GetCommentDtoResponse(comment.getId(), comment.getBody(), comment.getOwner().getId(),
                                    commentVersion ? revision.getRevisionIdForNote() : null,
                                    comment.getTimeCreated().toString()));
                        }
                    }

                    GetRevisionDtoResponse revisionResponse;
                    if (allVersions)
                        revisionResponse = new GetRevisionDtoResponse(revision.getRevisionIdForNote(),
                                revision.getBody(), timeCreated, commentsResponse);
                    else
                        revisionResponse = new GetRevisionDtoResponse(null,
                                null, null, commentsResponse);
                    revisionsResponse.add(revisionResponse);
                }
            }

            Revision revision = note.getRevisions().get(note.getRevisions().size() - 1);
            GetNoteListDtoResponse noteResponse = new GetNoteListDtoResponse(note.getId(), note.getSubject(), revision.getBody(),
                    note.getSection().getId(), note.getOwner().getId(), timeCreated, revisionsResponse);
            response.add(noteResponse);
        }
        return response;
    }



    private User getUserBySessionId(String sessionId) throws ServerException {
        Session session = userDao.getSession(sessionId);
        log.debug("Execute getUserBySessionId (Session {})", session);
        if(session == null) {
            log.info("Cannot execute getUserBySessionId, because sessionId wasn't found in DB");
            throw new ServerException(ServerErrorCode.THIS_SESSIONID_NOT_FOUND);
        }
        LocalDateTime timeLogout = session.getTimeLogin().plusSeconds(userIdleTimeout);
        LocalDateTime currentTime = LocalDateTime.now();
        if (currentTime.isAfter(timeLogout)) {
            log.info("Cannot execute getUserBySessionId, because session time is over");
            throw new ServerException(ServerErrorCode.SESSION_TIME_IS_OVER);
        }
        userDao.updateSession(session.getUser().getId(), currentTime);
        return session.getUser();
    }

    private Section getSection(int sectionId) throws ServerException {
        Section section = noteDao.getSection(sectionId);
        log.debug("Execute getSection (Section {})", section);
        if(section == null) {
            log.info("Cannot execute getSection, because sectionId wasn't found in DB");
            throw new ServerException(ServerErrorCode.THIS_SECTION_ID_NOT_FOUND);
        }
        return section;
    }

    private Note getNote(int noteId) throws ServerException {
        Note note = noteDao.getNote(noteId);
        log.debug("Execute getNote (Note {})", note);
        if(note == null) {
            log.info("Cannot execute getNote, because noteId wasn't found in DB");
            throw new ServerException(ServerErrorCode.THIS_NOTE_ID_NOT_FOUND);
        }
        return note;
    }

    private Comment getComment(int commentId) throws ServerException {
        Comment comment = noteDao.getComment(commentId);
        log.debug("Execute getComment (Comment {})", comment);
        if (comment == null) {
            log.info("Cannot execute getComment, because commentId wasn't found in DB");
            throw new ServerException(ServerErrorCode.THIS_COMMENT_ID_NOT_FOUND);
        }
        return comment;
    }


    private void checkIsAdminOrOwner(User user, Section section) throws ServerException {
        if (user.getId() != section.getOwner().getId() && user.getUserType() != UserType.ADMIN) {
            log.info("Cannot execute checkIsAdminOrOwner, because user are not owner of section");
            throw new ServerException(ServerErrorCode.YOU_ARE_NOT_OWNER_OF_SECTION);
        }
    }

    private void checkIsAdminOrOwner(User user, Note note) throws ServerException {
        if (user.getId() != note.getOwner().getId() && user.getUserType() != UserType.ADMIN) {
            log.info("Cannot execute checkIsAdminOrOwner, because user are not owner of note");
            throw new ServerException(ServerErrorCode.YOU_ARE_NOT_OWNER_OF_NOTE);
        }
    }

    private void checkUserNotOwner(User user, Note note) throws ServerException {
        if (user.getId() == note.getOwner().getId()) {
            log.info("Cannot execute rateNote, because user are owner of note");
            throw new ServerException(ServerErrorCode.YOU_CANT_RATE_YOUR_NOTE);
        }
    }

    private void checkIsOwner(User user, Section section) throws ServerException {
        if (user.getId() != section.getOwner().getId()) {
            log.info("Cannot execute renameSection, because user are not owner of section");
            throw new ServerException(ServerErrorCode.YOU_ARE_NOT_OWNER_OF_SECTION);
        }
    }

    private void checkIsOwner(User user, Note note) throws ServerException {
        if (user.getId() != note.getOwner().getId()) {
            log.info("Cannot execute checkIsOwner, because user are not owner of note");
            throw new ServerException(ServerErrorCode.YOU_ARE_NOT_OWNER_OF_NOTE);
        }
    }

    private void checkIsOwner(User user, Comment comment) throws ServerException {
        if (user.getId() != comment.getOwner().getId()) {
            log.info("Cannot execute editComment, because user are not owner of comment");
            throw new ServerException(ServerErrorCode.YOU_ARE_NOT_OWNER_OF_COMMENT);
        }
    }

    private void checkIsOwnerNoteOrCommentOrAdmin(User user, Comment comment, Note note) throws ServerException {
        if (user.getId() != comment.getOwner().getId() && user.getId() != note.getOwner().getId() &&
                user.getUserType() != UserType.ADMIN) {
            log.info("Cannot execute deleteComment, because user are not owner of comment or note");
            throw new ServerException(ServerErrorCode.YOU_ARE_NOT_OWNER_OF_COMMENT_OR_NOTE);
        }
    }
}
