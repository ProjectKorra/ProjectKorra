package com.projectkorra.projectkorra.firebending.bluefire;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.BlueFireAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.util.TempBlock;

public class BlueFirePassive extends BlueFireAbility implements PassiveAbility {

	public BlueFirePassive(Player player) {
		super(player);
	}

	@Override
	public void progress() {
		if(bPlayer.canBendPassive(this)) {
			for(Block b : GeneralMethods.getBlocksAroundPoint(player.getLocation(), 5)) {
				if (b.getType() == Material.TORCH ) { 
					new TempBlock(b, Material.SOUL_TORCH.createBlockData(), 5000);
				} else if (b.getType() == Material.WALL_TORCH) {
					new TempBlock(b, Material.SOUL_WALL_TORCH.createBlockData(), 5000);
				} else if (b.getType() == Material.FIRE) {
					new TempBlock(b, Material.SOUL_FIRE.createBlockData(), 5000);
				} else {
					// do nothing
				}
			}
		}
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
		return null;
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public boolean isInstantiable() {
		return false;
	}

	@Override
	public boolean isProgressable() {
		return true;
	}

}
