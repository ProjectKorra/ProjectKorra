package com.projectkorra.projectkorra.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Attribute {

	public String type();

	public class AttributeType {

		public static final String DAMAGE = "Damage";
		public static final String COOLDOWN = "Cooldown";
		public static final String DURATION = "Duration";
		public static final String MAX_DURATION = "MaxDuration";
		public static final String RANGE = "Range";
		public static final String SPEED = "Speed";
		public static final String RADIUS = "Radius";
		public static final String PARTICLES = "Particles";
		public static final String PUSH_FACTOR = "PushFactor";
		public static final String COLLISION_RADIUS = "CollisionRadius";

	}

}
