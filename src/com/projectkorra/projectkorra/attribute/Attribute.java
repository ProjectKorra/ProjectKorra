package com.projectkorra.projectkorra.attribute;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Attribute {

	String value();

	String SPEED = "Speed";
	String RANGE = "Range";
	String SELECT_RANGE = "SelectRange";
	String DAMAGE = "Damage";
	String COOLDOWN = "Cooldown";
	String DURATION = "Duration";
	String RADIUS = "Radius";
	String CHARGE_DURATION = "ChargeTime";
	String WIDTH = "Width";
	String HEIGHT = "Height";
	String KNOCKBACK = "Knockback";
	String KNOCKUP = "Knockup";
	String FIRE_TICK = "FireTicks";
}
