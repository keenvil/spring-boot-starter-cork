package com.keenvil.cork.validation;

import java.util.ArrayList;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Enum Validation implementation.
 * 
 * <p>Validates if a given value belongs to the list of defined enum Class.</p>
 */
public class EnumValidatorImpl implements ConstraintValidator<EnumValidator, String> {

  List<String> valueList = null;

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (!valueList.contains(value.toUpperCase())) {
      return false;
    }
    return true;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public void initialize(EnumValidator constraintAnnotation) {
    valueList = new ArrayList<String>();
    Class<? extends Enum<?>> enumClass = constraintAnnotation.source();

    Enum[] enumValArr = enumClass.getEnumConstants();

    
    for (Enum enumVal : enumValArr) {
      valueList.add(enumVal.toString().toUpperCase());
    }
  }
}