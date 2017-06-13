package com.projectkorra.projectkorra.attribute;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.FIELD})
public @interface Attribute {

	/**This is the attribute name that is added to the CoreAbility name. E.g. 
	 * Returning "Damage" on a FireBlast ability would make the attribute 
	 * "FireBlastDamage"*/
	String value() default ""; 
	
	
	/**This is for overriding the attribute name if the name you want should
	 * not come from the CoreAbility name. E.g. Returning "FastSwimSpeed" would
	 * make the Attribute name "FastSwimSpeed", instead of ability + "FastSwimSpeed"*/
	String attribute() default "";
	
	public static final String SPEED = "Speed";
	public static final String RANGE = "Range";
	public static final String SELECT_RANGE = "SelectRange";
	public static final String DAMAGE = "Damage";
	public static final String COOLDOWN = "Cooldown";
	public static final String DURATION = "Duration";
	public static final String RADIUS = "Radius";
	public static final String CHARGE_DURATION = "ChargeTime";
	public static final String POWER = "Power";
	public static final String WIDTH = "Width";
	public static final String HEIGHT = "Height";
}


