package net.thumbtack.school.notes.dto.requests.validator;

import org.springframework.beans.factory.annotation.Value;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PasswordSizeValidator implements ConstraintValidator<PasswordSize, String> {

    @Value("${max_name_length}")
    private int maxNameLength;

    @Value("${min_password_length}")
    private int minPasswordLength;

    @Override
    public boolean isValid(String password, ConstraintValidatorContext constraintValidatorContext) {
        if (password != null)
            return password.length() <= maxNameLength && password.length() >= minPasswordLength;
        return true;
    }

}
