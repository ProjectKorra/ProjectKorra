package com.projectkorra.projectkorra.waterbending.combo;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.IceAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.WaterSpoutWave;

public class IceWave extends IceAbility implements ComboAbility {

	private static final Map<Block, TempBlock> FROZEN_BLOCKS = new ConcurrentHashMap<>();
	
	private long cooldown;
	private Location origin;
	
	public IceWave(Player player) {
		super(player);

		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		}

		if (bPlayer.isOnCooldown("IceWave") && !bPlayer.isAvatarState()) {
			remove();
			return;
		}

		this.cooldown = getConfig().getLong("Abilities.Water.WaterCombo.IceWave.Cooldown");

		if (bPlayer.isAvatarState()) {
			this.cooldown = 0;
		}

		start();
	}

	@Override
	public String getName() {
		return "IceWave";
	}

	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		
		if (origin == null && WaterSpoutWave.containsType(player, WaterSpoutWave.AbilityType.RELEASE)) {
			bPlayer.addCooldown("IceWave", cooldown);
			origin = player.getLocation();

			WaterSpoutWave wave = WaterSpoutWave.getType(player, WaterSpoutWave.AbilityType.RELEASE).get(0);
			wave.setIceWave(true);
		} else if (!WaterSpoutWave.containsType(player, WaterSpoutWave.AbilityType.RELEASE)) {
			remove();
			return;
		}	
	}
	
	public static boolean canThaw(Block block) {
		return FROZEN_BLOCKS.containsKey(block);
	}

	public static void thaw(Block block) {
		if (FROZEN_BLOCKS.containsKey(block)) {
			FROZEN_BLOCKS.get(block).revertBlock();
			FROZEN_BLOCKS.remove(block);
		}
	}
	
	@Override
	public void remove() {
		bPlayer.addCooldown("WaterWave", cooldown);
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
		return cooldown;
	}
	
	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	@Override
	public Location getLocation() {
		return origin;
	}
	
	public void setLocation(Location location) {
		this.origin = location;
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return null;
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		return null;
	}
}
