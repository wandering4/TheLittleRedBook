package com.haishi.framework.commons.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {
    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext constraintValidatorContext) {
        // 校验逻辑：正则表达式判断手机号是否为 11 位数字
        return phoneNumber != null && phoneNumber.matches("\\d{11}");
    }

    @Override
    public void initialize(PhoneNumber constraintAnnotation) {

    }
}
