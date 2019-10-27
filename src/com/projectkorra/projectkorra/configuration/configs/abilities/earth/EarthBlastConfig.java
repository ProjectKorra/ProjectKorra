package com.projectkorra.projectkorra.configuration.configs.abilities.earth;

import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;

public class EarthBlastConfig extends AbilityConfig {

	public final long Cooldown = 500;
	public final double DeflectRange = 25;
	public final double CollisionRadius = 1.5;
	public final double Range = 30;
	public final double Damage = 2;
	public final double Speed = 25;
	public final boolean CanHitSelf = true;
	public final double PushFactor = .5;
	public final double SelectRange = 20;
	
	public final long AvatarState_Cooldown = 0;
	public final double AvatarState_Damage = 5;
	
	public EarthBlastConfig() {
		super(true, "EarthBlast is a basic yet fundamental earthbending ability. It allows you to deal rapid fire damage to your target to finish low health targets off or deal burst damage to them. Although it can be used at long range, it's potential is greater in close ranged combat.", "Tap sneak at an earthbendable block and then left click in a direction to send an earthblast. Additionally, you can left click again to change the direction of the earthblast. You can also redirect other earthbender's earth blast by left clicking. If the earth blast hits an entity it will deal damage and knockback.");
	}

	@Override
	public String getName() {
		return "EarthBlast";
	}

	@Override
	public String[] getParents() {
		return new String[] { "Abilities", "Earth" };
	}

}