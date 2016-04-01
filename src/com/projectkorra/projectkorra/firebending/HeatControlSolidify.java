package com.projectkorra.projectkorra.firebending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HeatControlSolidify extends FireAbility {
	
	private int radius;
	private long delay;
	private long lastBlockTime;
	private long lastParticleTime;
	private long revertTime;
	private double maxRadius;
	private double range;
	private Location location;
	private Random random;
	private ArrayList<TempBlock> tempBlocks;
	
	public HeatControlSolidify(Player player) {
		super(player);
		
		this.radius = 1;
		this.delay = 50;
		this.lastBlockTime = 0;
		this.lastParticleTime = 0;
		this.revertTime = getConfig().getLong("Abilities.Fire.HeatControl.Solidify.RevertTime");
		this.maxRadius = getConfig().getDouble("Abilities.Fire.HeatControl.Solidify.Radius");
		this.range = getConfig().getDouble("Abilities.Fire.HeatControl.Solidify.Range");
		this.random = new Random();
		this.tempBlocks = new ArrayList<>();
		
		if (!bPlayer.canBend(this)) {
			return;
		} else if (EarthAbility.getLavaSourceBlock(player, range) == null) {
			new HeatControlCook(player);
			return;
		}
		
		lastBlockTime = System.currentTimeMillis();
		start();
	}

	@SuppressWarnings("deprecation")
	public void freeze(List<Location> area) {
		if (System.currentTimeMillis() < lastBlockTime + delay) {
			return;
		}

		List<Block> lava = new ArrayList<Block>();
		for (Location l : area) {
			if (isLava(l.getBlock())) {
				lava.add(l.getBlock());
			}
		}

		lastBlockTime = System.currentTimeMillis();
		if (lava.size() == 0) {
			radius++;
			return;
		}

		Block b = lava.get(random.nextInt(lava.size()));
		TempBlock tb;

		if (TempBlock.isTempBlock(b)) {
			tb = TempBlock.get(b);
			tb.setType(Material.STONE);
		} else {
			tb = new TempBlock(b, Material.STONE, b.getData());
		}

		if (!tempBlocks.contains(tb)) {
			tempBlocks.add(tb);
		}
	}

	public void particles(List<Location> area) {
		if (System.currentTimeMillis() < lastParticleTime + 300) {
			return;
		}

		lastParticleTime = System.currentTimeMillis();
		for (Location l : area) {
			if (isLava(l.getBlock())) {
				ParticleEffect.SMOKE.display(l, 0, 0, 0, 0.1f, 2);
			}
		}
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreCooldowns(this)) {
			remove();
			return;
		} else if (radius >= maxRadius) {
			remove();
			return;
		}

		Location targetlocation = GeneralMethods.getTargetedLocation(player, range);
		resetLocation(targetlocation);
		List<Location> area = GeneralMethods.getCircle(location, radius, 3, true, true, 0);
		particles(area);
		freeze(area);
	}

	@Override
	public void remove() {
		ProjectKorra.plugin.getServer().getScheduler().scheduleSyncDelayedTask(ProjectKorra.plugin, new Runnable() {
			@Override
			public void run() {
				revertAll();
				HeatControlSolidify.super.remove();
			}
		}, revertTime);
	}

	public void resetLocation(Location loc) {
		if (location == null) {
			location = loc;
			return;
		}

		if (!loc.equals(location)) {
			radius = 1;
			location = loc;
		}
	}

	public void revertAll() {
		for (TempBlock tb : tempBlocks) {
			tb.revertBlock();
		}
		tempBlocks.clear();
	}

	@Override
	public String getName() {
		return "HeatControl";
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public long getCooldown() {
		return 0;
	}
	
	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	public long getLastBlockTime() {
		return lastBlockTime;
	}

	public void setLastBlockTime(long lastBlockTime) {
		this.lastBlockTime = lastBlockTime;
	}

	public long getLastParticleTime() {
		return lastParticleTime;
	}

	public void setLastParticleTime(long lastParticleTime) {
		this.lastParticleTime = lastParticleTime;
	}

	public long getRevertTime() {
		return revertTime;
	}

	public void setRevertTime(long revertTime) {
		this.revertTime = revertTime;
	}

	public double getMaxRadius() {
		return maxRadius;
	}

	public void setMaxRadius(double maxRadius) {
		this.maxRadius = maxRadius;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public ArrayList<TempBlock> getTempBlocks() {
		return tempBlocks;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
	
}
