package com.projectkorra.projectkorra.earthbending.combo;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.configuration.better.ConfigManager;
import com.projectkorra.projectkorra.configuration.better.configs.abilities.earth.EarthDomeConfig;
import com.projectkorra.projectkorra.earthbending.EarthDome;
import com.projectkorra.projectkorra.util.ClickType;

public class EarthDomeSelf extends EarthAbility<EarthDomeConfig> implements ComboAbility {

	public EarthDomeSelf(final EarthDomeConfig config, final Player player) {
		super(config, player);

		new EarthDome(config, player);
	}

	@Override
	public void progress() {}

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
		return new EarthDomeSelf(ConfigManager.getConfig(EarthDomeConfig.class), player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		final ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("RaiseEarth", ClickType.RIGHT_CLICK_BLOCK));
		combo.add(new AbilityInformation("Shockwave", ClickType.RIGHT_CLICK_BLOCK));
		return combo;
	}
}
