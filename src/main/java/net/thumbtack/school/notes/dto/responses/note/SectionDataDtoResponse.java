package net.thumbtack.school.notes.dto.responses.note;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SectionDataDtoResponse {
    private int id;
    private String name;
}
