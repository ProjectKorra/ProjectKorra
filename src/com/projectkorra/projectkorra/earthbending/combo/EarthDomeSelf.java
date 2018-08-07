package com.projectkorra.projectkorra.earthbending.combo;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.earthbending.EarthDome;
import com.projectkorra.projectkorra.util.ClickType;

public class EarthDomeSelf extends EarthAbility implements ComboAbility {

	public EarthDomeSelf(final Player player) {
		super(player);

		new EarthDome(player);
	}

	@Override
	public void progress() {
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public long getCooldown() {
		return 0;
	}

	@Override
	public String getName() {
		return "EarthDome";
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public Object createNewComboInstance(final Player player) {
		return new EarthDomeSelf(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		final ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("RaiseEarth", ClickType.RIGHT_CLICK_BLOCK));
		combo.add(new AbilityInformation("Shockwave", ClickType.RIGHT_CLICK_BLOCK));
		return combo;
	}

	@Override
	public String getInstructions() {
		return "\n(Self) RaiseEarth (Right click) > Shockwave (Right click)\n(Projection) RaiseEarth(Right click) > Shockwave (Left click)";
	}
}
