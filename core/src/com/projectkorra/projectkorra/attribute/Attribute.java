package com.projectkorra.projectkorra.attribute;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Attribute {

	public String value();

	public static final String SPEED = "Speed";
	public static final String RANGE = "Range";
	public static final String SELECT_RANGE = "SelectRange";
	public static final String DAMAGE = "Damage";
	public static final String COOLDOWN = "Cooldown";
	public static final String DURATION = "Duration";
	public static final String RADIUS = "Radius";
	public static final String CHARGE_DURATION = "ChargeTime";
	public static final String WIDTH = "Width";
	public static final String HEIGHT = "Height";
	public static final String KNOCKBACK = "Knockback";
	public static final String KNOCKUP = "Knockup";
	public static final String SELF_PUSH = "SelfPush";
	public static final String FIRE_TICK = "FireTicks";
	public static final String AVATAR_STATE_TOGGLE = "AvatarStateToggle";
}
