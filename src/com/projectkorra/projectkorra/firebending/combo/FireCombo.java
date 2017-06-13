package com.projectkorra.projectkorra.firebending.combo;

import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.FireAbility;

public abstract class FireCombo extends FireAbility implements ComboAbility {

	public FireCombo(Player player) {
		super(player);
	}

	@Override
	public boolean isHarmlessAbility() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean isCollidable() {
		return false;
	}
	
	@Override
	public boolean isSneakAbility() {
		return false;
	}

}
