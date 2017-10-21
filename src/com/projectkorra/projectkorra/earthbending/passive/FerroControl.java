package com.projectkorra.projectkorra.earthbending.passive;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.MetalAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;

public class FerroControl extends MetalAbility implements PassiveAbility {
	
	private Block block;

	public FerroControl(Player player) {
		super(player);
		
		if (!player.isSneaking()) {
			return;
		}
		
		if (!bPlayer.canUsePassive(this)) {
			return;
		}
		
		if (!bPlayer.canBendPassive(this)) {
			return;
		}
		start();
	}

	@Override
	public void progress() {
		boolean used = false, tDoor = false, open = false;
		block = player.getTargetBlock((HashSet<Material>) null, 5);
		
		if (block != null) {
			if (block.getType() == Material.IRON_DOOR_BLOCK && !GeneralMethods.isRegionProtectedFromBuild(player, block.getLocation())) {
				if (block.getData() >= 8) {
					block = block.getRelative(BlockFace.DOWN);
				}
				
				block.setData((byte) ((block.getData() & 0x4) == 0x4 ? (block.getData() & ~0x4) : (block.getData() | 0x4)));
				open = (block.getData() & 0x4) == 0x4;
				used = true;
			} else if (block.getType() == Material.IRON_TRAPDOOR && !GeneralMethods.isRegionProtectedFromBuild(player, block.getLocation())) {
				block.setData((byte) ((block.getData() & 0x4) == 0x4 ? (block.getData() & ~0x4) : (block.getData() | 0x4)));
				open = (block.getData() & 0x4) == 0x4;
				used = true;
				tDoor = true;
			}
			
		}
		
		if (used) {
			String sound = "BLOCK_IRON_" + (tDoor ? "TRAP" : "") + "DOOR_" + (open ? "OPEN" : "CLOSE");
			block.getWorld().playSound(block.getLocation(), Sound.valueOf(sound), 0.5f, 0);
			bPlayer.addCooldown(this, 200);
		}
		remove();
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
		return block != null ? block.getLocation() : null;
	}

	@Override
	public boolean isInstantiable() {
		return true;
	}

	@Override
	public boolean isProgressable() {
		return true;
	}
}
