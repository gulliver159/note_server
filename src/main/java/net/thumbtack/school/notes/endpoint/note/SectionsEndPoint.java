package net.thumbtack.school.notes.endpoint.note;

import lombok.extern.slf4j.Slf4j;
import net.thumbtack.school.notes.dto.requests.note.SectionNameDtoRequest;
import net.thumbtack.school.notes.dto.responses.note.SectionDataDtoResponse;
import net.thumbtack.school.notes.exception.ServerException;
import net.thumbtack.school.notes.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@SpringBootApplication
@RequestMapping("/api/sections")
public class SectionsEndPoint {

    private final NoteService noteService;

    @Autowired
    public SectionsEndPoint(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public SectionDataDtoResponse createSection(@RequestBody @Valid SectionNameDtoRequest request,
                                                @CookieValue(value = "JAVASESSIONID") String sessionId) throws ServerException {
        log.debug("Accepted the post request createSection");
        SectionDataDtoResponse response = noteService.createSection(sessionId, request);
        log.debug("Executed the post request createSection");
        return response;
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public SectionDataDtoResponse renameSection(@RequestBody @Valid SectionNameDtoRequest request,
                                                @PathVariable("id") int id,
                                                @CookieValue(value = "JAVASESSIONID") String sessionId) throws ServerException {
        log.debug("Accepted the put request renameSection");
        SectionDataDtoResponse response = noteService.renameSection(sessionId, id, request);
        log.debug("Executed the put request renameSection");
        return response;
    }

    @DeleteMapping(value = "/{id}")
    public void deleteSection(@PathVariable("id") int id, @CookieValue(value = "JAVASESSIONID") String sessionId) throws ServerException {
        log.debug("Accepted the delete request deleteSection");
        noteService.deleteSection(sessionId, id);
        log.debug("Executed the delete request deleteSection");
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public SectionDataDtoResponse getSectionInfo(@PathVariable("id") int id,
                                                 @CookieValue(value = "JAVASESSIONID") String sessionId) throws ServerException {
        log.debug("Accepted the get request getSectionInfo");
        SectionDataDtoResponse response = noteService.getSectionInfo(sessionId, id);
        log.debug("Executed the get request getSectionInfo");
        return response;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<SectionDataDtoResponse> getSectionList(@CookieValue(value = "JAVASESSIONID") String sessionId) throws ServerException {
        log.debug("Accepted the get request getSectionList");
        List<SectionDataDtoResponse> response = noteService.getSectionList(sessionId);
        log.debug("Executed the get request getSectionList");
        return response;
    }
}
