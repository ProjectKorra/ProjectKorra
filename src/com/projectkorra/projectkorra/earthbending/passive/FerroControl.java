package com.projectkorra.projectkorra.earthbending.passive;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.MetalAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.configuration.configs.abilities.earth.FerroControlConfig;

public class FerroControl extends MetalAbility<FerroControlConfig> implements PassiveAbility {

	private Block block;

	public FerroControl(final FerroControlConfig config, final Player player) {
		super(config, player);

		this.start();
	}

	@Override
	public void progress() {
		if (!this.player.isSneaking() || !this.bPlayer.canUsePassive(this) || !this.bPlayer.canBendPassive(this)) {
			this.remove();
			return;
		}

		boolean used = false, tDoor = false, open = false;
		this.block = this.player.getTargetBlock((HashSet<Material>) null, 5);

		if (this.block != null) {
			if (this.block.getType() == Material.IRON_DOOR && !GeneralMethods.isRegionProtectedFromBuild(this.player, this.block.getLocation())) {
				final Door door = (Door) this.block.getBlockData();

				door.setOpen(!door.isOpen());
				this.block.setBlockData(door);

				open = door.isOpen();
				used = true;
			} else if (this.block.getType() == Material.IRON_TRAPDOOR && !GeneralMethods.isRegionProtectedFromBuild(this.player, this.block.getLocation())) {
				final TrapDoor trap = (TrapDoor) this.block.getBlockData();

				trap.setOpen(!trap.isOpen());
				this.block.setBlockData(trap);

				open = trap.isOpen();
				used = true;
				tDoor = true;
			}

		}

		if (used) {
			final String sound = "BLOCK_IRON_" + (tDoor ? "TRAP" : "") + "DOOR_" + (open ? "OPEN" : "CLOSE");
			this.block.getWorld().playSound(this.block.getLocation(), Sound.valueOf(sound), 0.5f, 0);
			this.bPlayer.addCooldown(this, 200);
		}
		this.remove();
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	@Override
	public long getCooldown() {
		return 0;
	}

	@Override
	public String getName() {
		return "FerroControl";
	}

	@Override
	public Location getLocation() {
		return this.block != null ? this.block.getLocation() : null;
	}

	@Override
	public boolean isInstantiable() {
		return false;
	}

	@Override
	public boolean isProgressable() {
		return true;
	}
	
	@Override
	public Class<FerroControlConfig> getConfigType() {
		return FerroControlConfig.class;
	}
}
