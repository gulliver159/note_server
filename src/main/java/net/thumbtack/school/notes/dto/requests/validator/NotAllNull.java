package net.thumbtack.school.notes.dto.requests.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NotAllNullValidator.class)
@Target( {ElementType.TYPE} )
@Retention(RetentionPolicy.RUNTIME)
public @interface NotAllNull {
    String message() default "At least one field in the request must be non null";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
