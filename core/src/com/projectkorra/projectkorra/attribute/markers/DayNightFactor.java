package com.projectkorra.projectkorra.attribute.markers;

import com.projectkorra.projectkorra.attribute.AttributeMarker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This marker is used to indicate that the attribute is affected by the time of day.
 * At night, water abilities will be multiplied by the factor.
 * At day, fire abilities will be multiplied by the factor
 */
@AttributeMarker
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DayNightFactor {

    /**
     * The factor to affect this ability. Default: -1
     * When the factor is -1, the ability will use the default value from the config
     * @return The day/night factor to multiply the attribute by
     */
    float factor() default -1;

    /**
     * Whether the factor should be inverted. Inverted factors will be divided by instead.
     * This is useful for things like cooldowns, where a lower value is stronger
     * @return Whether the factor should be inverted
     */
    boolean invert() default false;

}
