package com.projectkorra.projectkorra.waterbending.combo;

import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.IceAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.ability.util.ComboUtil;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.WaterSpoutWave;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IceWave extends IceAbility implements ComboAbility {

	private static final Map<Block, TempBlock> FROZEN_BLOCKS = new ConcurrentHashMap<>();

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	private Location origin;

	public IceWave(final Player player) {
		super(player);

		if (!hasAbility(player, WaterSpoutWave.class)) {
			return;
		}

		if (!this.bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		}

		if (this.bPlayer.isOnCooldown("IceWave") && !this.bPlayer.isAvatarState()) {
			this.remove();
			return;
		}

		this.cooldown = applyInverseModifiers(getConfig().getLong("Abilities.Water.IceWave.Cooldown"));

		if (this.bPlayer.isAvatarState()) {
			this.cooldown = 0;
		}

		this.start();
	}

	@Override
	public String getName() {
		return "IceWave";
	}

	@Override
	public void progress() {
		if (this.player.isDead() || !this.player.isOnline()) {
			this.remove();
			return;
		}

		if (this.origin == null && WaterSpoutWave.containsType(this.player, WaterSpoutWave.AbilityType.RELEASE)) {
			this.bPlayer.addCooldown("IceWave", this.cooldown);
			this.origin = this.player.getLocation();

			final WaterSpoutWave wave = WaterSpoutWave.getType(this.player, WaterSpoutWave.AbilityType.RELEASE).get(0);
			wave.setIceWave(true);
		} else if (!WaterSpoutWave.containsType(this.player, WaterSpoutWave.AbilityType.RELEASE)) {
			this.remove();
			return;
		}
	}

	public static boolean canThaw(final Block block) {
		return FROZEN_BLOCKS.containsKey(block);
	}

	public static void thaw(final Block block) {
		if (FROZEN_BLOCKS.containsKey(block)) {
			FROZEN_BLOCKS.get(block).revertBlock();
			FROZEN_BLOCKS.remove(block);
		}
	}

	@Override
	public void remove() {
		super.remove();
		this.bPlayer.addCooldown("WaterWave", this.cooldown);
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public long getCooldown() {
		return this.cooldown;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

	@Override
	public Location getLocation() {
		return this.origin;
	}

	public void setLocation(final Location location) {
		this.origin = location;
	}

	@Override
	public Object createNewComboInstance(final Player player) {
		return new IceWave(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		return ComboUtil.generateCombinationFromList(this, ConfigManager.defaultConfig.get().getStringList("Abilities.Water.IceWave.Combination"));
	}
}
