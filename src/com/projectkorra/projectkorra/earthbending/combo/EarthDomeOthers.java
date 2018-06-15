package com.projectkorra.projectkorra.earthbending.combo;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.earthbending.EarthDome;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.ParticleEffect.BlockData;

public class EarthDomeOthers extends EarthAbility {

	public Vector direction;
	public double range = 0, maxRange;
	public Location loc;
	
	public EarthDomeOthers(Player player) {
		super(player);

		if (bPlayer.isOnCooldown("EarthDome")) return;
		this.loc = player.getLocation().clone();
		
		if (GeneralMethods.isRegionProtectedFromBuild(player, loc)) return;
		if (!isEarthbendable(loc.getBlock().getRelative(BlockFace.DOWN).getType(), true, true, true)) return;
		this.direction = loc.getDirection().setY(0);
		this.maxRange = getConfig().getDouble("Abilities.Earth.EarthDome.Range");
		start();
	}

	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead()) {
			remove(true);
			return;
		}
		if (range >= maxRange) {
			remove(true);
			return;
		}
		if (GeneralMethods.isRegionProtectedFromBuild(player, loc)) {
			remove(true);
			return;
		}
		
		range++;
		loc.add(direction.normalize());
		Block top = GeneralMethods.getTopBlock(loc, 2);
		
		while (!isEarthbendable(top)) {
			if (isTransparent(top)) {
				top = top.getRelative(BlockFace.DOWN);
			} else {
				remove(true);
				return;
			}
		}	
		
		if (!isTransparent(top.getRelative(BlockFace.UP))) {
			remove(true);
			return;
		}
		
		loc.setY(top.getY() + 1);
		
		ParticleEffect.CRIT.display(loc, 0.4f, 0, 0.4f, 0.001f, 9);
		ParticleEffect.BLOCK_DUST.display(new BlockData(loc.getBlock().getRelative(BlockFace.DOWN).getType(), (byte)0), 0.2f, 0.1f, 0.2f, 0.001f, 7, loc, 255);
		
		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(loc, 2)) {
			if (!(entity instanceof LivingEntity) || entity.getEntityId() == player.getEntityId()) {
				continue;
			}
			
			new EarthDome(player, entity.getLocation().clone().subtract(0, 1, 0));
			remove(false);
			return;
		}
	}
	
	public void remove(boolean cooldown) {
		super.remove();
		if (cooldown)
			bPlayer.addCooldown("EarthDome", getConfig().getLong("Abilities.Earth.EarthDome.Cooldown"));
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
		return loc != null ? loc : null;
	}
	
	@Override
	public boolean isHiddenAbility() {
		return true;
	}
}