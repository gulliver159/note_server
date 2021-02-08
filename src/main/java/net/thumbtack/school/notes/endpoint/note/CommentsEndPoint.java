package net.thumbtack.school.notes.endpoint.note;

import lombok.extern.slf4j.Slf4j;
import net.thumbtack.school.notes.dto.requests.note.CreateCommentDtoRequest;
import net.thumbtack.school.notes.dto.requests.note.EditCommentDtoRequest;
import net.thumbtack.school.notes.dto.responses.note.GetCommentInfoDtoResponse;
import net.thumbtack.school.notes.exception.ServerException;
import net.thumbtack.school.notes.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@SpringBootApplication
@RequestMapping("/api/comments")
public class CommentsEndPoint {

    private final NoteService noteService;

    @Autowired
    public CommentsEndPoint(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public GetCommentInfoDtoResponse createComment(@RequestBody @Valid CreateCommentDtoRequest request,
                                                   @CookieValue(value = "JAVASESSIONID") String sessionId) throws ServerException {
        log.debug("Accepted the post request createComment");
        GetCommentInfoDtoResponse response = noteService.createComment(sessionId, request);
        log.debug("Executed the post request createComment");
        return response;
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public GetCommentInfoDtoResponse editComment(@RequestBody @Valid EditCommentDtoRequest request,
                                                 @PathVariable("id") int id,
                                                 @CookieValue(value = "JAVASESSIONID") String sessionId) throws ServerException {
        log.debug("Accepted the put request editComment");
        GetCommentInfoDtoResponse response = noteService.editComment(sessionId, id, request);
        log.debug("Executed the put request editComment");
        return response;
    }

    @DeleteMapping(value = "/{id}")
    public void deleteComment(@PathVariable("id") int id,
                              @CookieValue(value = "JAVASESSIONID") String sessionId) throws ServerException {
        log.debug("Accepted the delete request deleteComment");
        noteService.deleteComment(sessionId, id);
        log.debug("Executed the delete request deleteComment");
    }

}
