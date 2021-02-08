package net.thumbtack.school.notes.dto.requests.validator;

import org.springframework.beans.BeanWrapperImpl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NotAllNullValidator implements ConstraintValidator<NotAllNull, Object> {

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext constraintValidatorContext) {
        String body = (String) new BeanWrapperImpl(obj)
                .getPropertyValue("body");

        Integer sectionId = (Integer) new BeanWrapperImpl(obj)
                .getPropertyValue("sectionId");

        return body != null || sectionId != null;
    }

}
