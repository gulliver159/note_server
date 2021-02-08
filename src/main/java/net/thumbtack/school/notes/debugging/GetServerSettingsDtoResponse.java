package net.thumbtack.school.notes.debugging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetServerSettingsDtoResponse {
   private int maxNameLength;
   private int minPasswordLength;
   private int userIdleTimeout;
}
