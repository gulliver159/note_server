package net.thumbtack.school.notes.dto.requests.validator;

import org.springframework.beans.factory.annotation.Value;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NameSizeValidator implements ConstraintValidator<NameSize, String> {

    @Value("${max_name_length}")
    private int maxNameLength;

    @Override
    public boolean isValid(String name, ConstraintValidatorContext constraintValidatorContext) {
        if (name != null)
            return name.length() <= maxNameLength;
        return true;
    }

}
