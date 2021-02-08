package net.thumbtack.school.notes.dto.requests.validator;

import org.springframework.beans.factory.annotation.Value;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NameSizeValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD } )
@Retention(RetentionPolicy.RUNTIME)
public @interface NameSize {
    String message() default "The field of the wrong size";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
