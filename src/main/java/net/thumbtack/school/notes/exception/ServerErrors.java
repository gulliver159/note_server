package net.thumbtack.school.notes.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerErrors {
    private Set<ServerError> errors;
}
