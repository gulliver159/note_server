package net.thumbtack.school.notes.endpoint.note;

import lombok.extern.slf4j.Slf4j;
import net.thumbtack.school.notes.dto.requests.note.CreateNoteDtoRequest;
import net.thumbtack.school.notes.dto.requests.note.EditOrTransferNoteDtoRequest;
import net.thumbtack.school.notes.dto.requests.note.RateNoteDtoRequest;
import net.thumbtack.school.notes.dto.responses.note.GetCommentInfoDtoResponse;
import net.thumbtack.school.notes.dto.responses.note.GetNoteInfoDtoResponse;
import net.thumbtack.school.notes.dto.responses.note.GetNoteListDtoResponse;
import net.thumbtack.school.notes.endpoint.request_param.IncludeType;
import net.thumbtack.school.notes.endpoint.request_param.SortOrder;
import net.thumbtack.school.notes.exception.ServerErrorCode;
import net.thumbtack.school.notes.exception.ServerException;
import net.thumbtack.school.notes.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@SpringBootApplication
@RequestMapping("/api/notes")
public class NotesEndPoint {

    private final NoteService noteService;

    @Autowired
    public NotesEndPoint(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public GetNoteInfoDtoResponse createNote(@RequestBody @Valid CreateNoteDtoRequest request,
                                             @CookieValue(value = "JAVASESSIONID") String sessionId) throws ServerException {
        log.debug("Accepted the post request createNote");
        GetNoteInfoDtoResponse response = noteService.createNote(sessionId, request);
        log.debug("Executed the post request createNote");
        return response;
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public GetNoteInfoDtoResponse getNoteInfo(@PathVariable("id") int id,
                                                @CookieValue(value = "JAVASESSIONID") String sessionId) throws ServerException {
        log.debug("Accepted the get request getNoteInfo");
        GetNoteInfoDtoResponse response = noteService.getNoteInfo(sessionId, id);
        log.debug("Executed the get request getNoteInfo");
        return response;
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public GetNoteInfoDtoResponse editOrTransferNote(@RequestBody @Valid EditOrTransferNoteDtoRequest request, @PathVariable("id") int id,
                                                     @CookieValue(value = "JAVASESSIONID") String sessionId) throws ServerException {
        log.debug("Accepted the put request editOrTransferNote");
        GetNoteInfoDtoResponse response = noteService.editOrTransferNote(sessionId, id, request);
        log.debug("Executed the put request editOrTransferNote");
        return response;
    }

    @DeleteMapping(value = "/{id}")
    public void deleteNote(@PathVariable("id") int id, @CookieValue(value = "JAVASESSIONID") String sessionId) throws ServerException {
        log.debug("Accepted the delete request deleteNote");
        noteService.deleteNote(sessionId, id);
        log.debug("Executed the delete request deleteNote");
    }

    @GetMapping(value = "/{id}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<GetCommentInfoDtoResponse> getCommentsNotes(@PathVariable("id") int id,
                                                            @CookieValue(value = "JAVASESSIONID") String sessionId) throws ServerException {
        log.debug("Accepted the get request getCommentsNotes");
        List<GetCommentInfoDtoResponse> response = noteService.getCommentsNotes(sessionId, id);
        log.debug("Executed the get request getCommentsNotes");
        return response;
    }

    @DeleteMapping(value = "/{id}/comments")
    public void deleteCommentsNote(@PathVariable("id") int id,
                                    @CookieValue(value = "JAVASESSIONID") String sessionId) throws ServerException {
        log.debug("Accepted the delete request deleteCommentsNotes");
        noteService.deleteCommentsNote(sessionId, id);
        log.debug("Executed the delete request deleteCommentsNotes");
    }

    @PostMapping(value = "/{id}/rating", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void rateNote(@RequestBody @Valid RateNoteDtoRequest request,
                                           @PathVariable("id") int id,
                                           @CookieValue(value = "JAVASESSIONID") String sessionId) throws ServerException {
        log.debug("Accepted the post request rateNote");
        noteService.rateNote(sessionId, id, request);
        log.debug("Executed the post request rateNote");
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<GetNoteListDtoResponse> getNoteList(@RequestParam Map<String, String> allParams,
                                                    @CookieValue(value = "JAVASESSIONID") String sessionId) throws ServerException {
        log.debug("Accepted the get request getNoteList");
        Map<String, IncludeType> includeTypeMap = createIncludeTypeMap();

        try {
            Integer sectionId = (allParams.get("sectionId") == null) ? null : Integer.parseInt(allParams.remove("sectionId"));
            SortOrder sortByRating = (allParams.get("sortByRating") == null) ? SortOrder.NONE :
                    SortOrder.valueOf(allParams.remove("sortByRating").toUpperCase());
            List<String> tags = (allParams.get("tags") == null) ? null :
                    new ArrayList<>(Arrays.asList(allParams.remove("tags").split(",")));
            boolean alltags = (allParams.get("alltags") == null) ? false : Boolean.parseBoolean(allParams.remove("alltags"));

            LocalDateTime timeFrom = (allParams.get("timeFrom") == null) ? null :
                    LocalDateTime.parse(allParams.remove("timeFrom"));
            LocalDateTime timeTo = (allParams.get("timeTo") == null) ? null :
                    LocalDateTime.parse(allParams.remove("timeTo"));

            Integer userId = (allParams.get("user") == null) ? null : Integer.parseInt(allParams.remove("user"));
            IncludeType include = (allParams.get("include") == null) ? IncludeType.NONE :
                    includeTypeMap.get(allParams.remove("include"));

            boolean comments = (allParams.get("comments") == null) ? false :
                    Boolean.parseBoolean(allParams.remove("comments"));
            boolean allVersions = (allParams.get("allVersions") == null) ? false :
                    Boolean.parseBoolean(allParams.remove("allVersions"));
            boolean commentVersion = (allParams.get("commentVersion") == null) ? false :
                    Boolean.parseBoolean(allParams.remove("commentVersion"));
            int from = (allParams.get("from") == null) ? 0 : Integer.parseInt(allParams.remove("from"));
            Integer count = (allParams.get("count") == null) ? null : Integer.parseInt(allParams.remove("count"));

            if (allParams.keySet().size() != 0) {
                log.info("Cannot execute the get request getNoteList due to invalid search parameters");
                throw new ServerException(ServerErrorCode.WRONG_SEARCH_PARAM);
            }
            List<GetNoteListDtoResponse> response = noteService.getNoteList(sessionId, sectionId, sortByRating, tags, alltags,
                    timeFrom, timeTo, userId, include, comments, allVersions, commentVersion, from, count);
            log.debug("Executed the get request getNoteList");
            return response;
        } catch (IllegalArgumentException ex) {
            log.info("Cannot execute the get request getNoteList due to invalid search parameter values");
            throw new ServerException(ServerErrorCode.INVALID_PARAM_VALUE);
        }
    }


    private Map<String, IncludeType> createIncludeTypeMap() {
        Map<String, IncludeType> searchParamsMap = new HashMap<>();
        searchParamsMap.put("notIgnore", IncludeType.NOT_IGNORE);
        searchParamsMap.put("onlyFollowings", IncludeType.ONLY_FOLLOWINGS);
        searchParamsMap.put("onlyIgnore", IncludeType.ONLY_IGNORE);
        return searchParamsMap;
    }
}
