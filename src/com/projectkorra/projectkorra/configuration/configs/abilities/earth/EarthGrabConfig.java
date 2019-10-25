package com.projectkorra.projectkorra.configuration.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class EarthGrabConfig extends AbilityConfig {

	public final long Cooldown = 4500;
	public final double Range = 30;
	public final double DragSpeed = 1.0;
	public final double DamageThreshold = 4;
	public final double TrapHP = 3;
	public final long TrapHitInterval = 250;
	
	public EarthGrabConfig() {
		super(true, "EarthGrab is one of the best defence abilities in an earthbender's arsenal. It allows you to trap someone who is running away so that you can catch up to someone. It is also of great utility use to an earthbender. It can be used to drag items, arrows, and crops that are on earthbendable blocks towards you, saving you the time of running to get them.", "(Grab) To grab an entity, left click in the direction of the target. Your power will be sent through the earth, and then it will reach up and root them in their spot upon contact. The ability can be manually be disabled by sneaking or clicking again on the EarthGrab slot.\" + \"\\n\" + \"(Drag) To drag items towards you, sneak\" + \"\\n(Escaping) To escape, the trap must be destroyed or the user damaged. The trap can be destroyed by damage or the trapped entity right-clicking it a certain number of times. Additionally, forcefully moving the entity with another earth ability destroys the trap.");
	}

	@Override
	public String getName() {
		return "EarthGrab";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Earth" };
	}

}