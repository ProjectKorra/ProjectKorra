package com.projectkorra.projectkorra.configuration.configs.abilities.air;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class AirSwipeConfig extends AbilityConfig {
	
	public final long Cooldown = 1250;
	public final int AnimationParticleAmount = 2;
	public final int Arc = 16;
	public final int StepSize = 4;
	public final long MaxChargeTime = 2500;
	public final double Damage = 2;
	public final double PushFactor = .5;
	public final double Speed = 25;
	public final double Range = 15;
	public final double Radius = 1.5;
	public final double MaxChargeFactor = 3;
	
	public final long AvatarState_Cooldown = 700;
	public final double AvatarState_Damage = 3;
	public final double AvatarState_PushFactor = 1.0;
	public final double AvatarState_Range = 20;
	public final double AvatarState_Radius = 2;
	
	public AirSwipeConfig() {
		super(true, "AirSwipe is the most commonly used damage ability in an airbender's arsenal. An arc of air will flow from you towards the direction you're facing, cutting and pushing back anything in its path. This ability will extinguish fires, cool lava, and cut things like grass, mushrooms, and flowers.", "\"\\n\" + \"(Uncharged) Simply left click to send an air swipe out that will damage targets that it comes into contact with.\" + \"\\n\" + \"(Charged) Hold sneak until particles appear, then release sneak to send a more powerful air swipe out that damages entity's that it comes into contact with.\"");
	}

	@Override
	public String getName() {
		return "AirSwipe";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Air" };
	}

}