package net.thumbtack.school.notes.dto.requests.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordSizeValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD } )
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordSize {
    String message() default "The password of the wrong size";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
