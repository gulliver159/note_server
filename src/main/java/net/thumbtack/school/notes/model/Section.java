package net.thumbtack.school.notes.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Section {
    private int id;
    private String name;
    private User owner;
    private List<Note> notes;

    public Section(String name) {
        this.name = name;
    }

    public Section(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
