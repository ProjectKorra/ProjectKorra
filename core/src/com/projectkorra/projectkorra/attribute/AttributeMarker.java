package com.projectkorra.projectkorra.attribute;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark other @Attribute annotations. Things such as @DayNightFactor
 * and other factors that modify values in events
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
public @interface AttributeMarker {

}
