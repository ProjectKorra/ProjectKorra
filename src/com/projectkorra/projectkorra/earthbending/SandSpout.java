package com.projectkorra.projectkorra.earthbending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.SandAbility;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.Flight;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.Random;

public class SandSpout extends SandAbility {

	private boolean canSpiral;
	private int angle;
	private int blindnessTime;
	private long time;
	private long interval;
	private double damage;
	private double height;
	private double currentHeight;
	private Flight flight;
	
	public SandSpout(Player player) {
		super(player);
		
		this.currentHeight = 0;
		this.angle = 0;
		this.interval = getConfig().getLong("Abilities.Earth.SandSpout.Interval");
		this.canSpiral = getConfig().getBoolean("Abilities.Earth.SandSpout.Spiral");
		this.height = getConfig().getDouble("Abilities.Earth.SandSpout.Height");
		this.blindnessTime = getConfig().getInt("Abilities.Earth.SandSpout.BlindnessTime");
		this.damage = getConfig().getInt("Abilities.Earth.SandSpout.SpoutDamage");
		
		SandSpout oldSandSpout = getAbility(player, SandSpout.class);
		if (oldSandSpout != null) {
			oldSandSpout.remove();
			return;
		}
		
		if (!bPlayer.canBend(this)) {
			return;
		}

		time = System.currentTimeMillis();
		Block topBlock = GeneralMethods.getTopBlock(player.getLocation(), 0, -50);
		if (topBlock == null) {
			topBlock = player.getLocation().getBlock();
		}
		
		Material mat = topBlock.getType();
		if (mat != Material.SAND && mat != Material.SANDSTONE && mat != Material.RED_SANDSTONE) {
			return;
		}
		
		if (EarthPassive.isPassiveSand(topBlock)) {
			return;
		}
		
		flight = new Flight(player);
		start();
		bPlayer.addCooldown(this);
	}

	@Override
	public void progress() {
		Block eyeBlock = player.getEyeLocation().getBlock();
		if (!bPlayer.canBendIgnoreBindsCooldowns(this) || eyeBlock.isLiquid() || GeneralMethods.isSolid(eyeBlock)) {
			remove();
			return;
		}

		player.setFallDistance(0);
		player.setSprinting(false);
		if ((new Random()).nextInt(2) == 0) {
			playSandBendingSound(player.getLocation());
		}
		
		Block block = getGround();
		
		if (EarthPassive.isPassiveSand(block)) {
			remove();
			return;
		}
		
		if (block != null && (block.getType() == Material.SAND || block.getType() == Material.SANDSTONE || block.getType() == Material.RED_SANDSTONE)) {
			double dy = player.getLocation().getY() - block.getY();
			if (dy > height) {
				removeFlight();
			} else {
				allowFlight();
			}
			rotateSandColumn(block);
		} else {
			remove();
		}
	}

	private void allowFlight() {
		player.setAllowFlight(true);
		player.setFlying(true);
		player.setFlySpeed(.05f);
	}

	private void removeFlight() {
		player.setAllowFlight(false);
		player.setFlying(false);
	}

	private Block getGround() {
		Block standingblock = player.getLocation().getBlock();
		for (int i = 0; i <= height + 5; i++) {
			Block block = standingblock.getRelative(BlockFace.DOWN, i);
			if (GeneralMethods.isSolid(block) || block.isLiquid()) {
				return block;
			}
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	private void rotateSandColumn(Block block) {
		if (System.currentTimeMillis() >= time + interval) {
			time = System.currentTimeMillis();

			Location location = block.getLocation();
			Location playerLoc = player.getLocation();
			location = new Location(location.getWorld(), playerLoc.getX(), location.getY(), playerLoc.getZ());

			double dy = playerLoc.getY() - block.getY();
			if (dy > height) {
				dy = height;
			}
			
			Integer[] directions = { 0, 1, 2, 3, 5, 6, 7, 8 };
			int index = angle;

			angle++;
			if (angle >= directions.length) {
				angle = 0;
			}
			for (int i = 1; i <= dy; i++) {
				index += 1;
				if (index >= directions.length) {
					index = 0;
				}

				Location effectloc2 = new Location(location.getWorld(), location.getX(), block.getY() + i, location.getZ());

				if (canSpiral) {
					displayHelix(block.getLocation(), this.player.getLocation(), block);
				}
				if (block != null && ((block.getType() == Material.SAND && block.getData() == (byte) 0) || block.getType() == Material.SANDSTONE)) {
					displaySandParticle(effectloc2, 1f, 3f, 1f, 20, .2f, false);
				} else if (block != null && ((block.getType() == Material.SAND && block.getData() == (byte) 1) || block.getType() == Material.RED_SANDSTONE)) {
					displaySandParticle(effectloc2, 1f, 3f, 1f, 20, .2f, true);
				}

				Collection<Player> players = GeneralMethods.getPlayersAroundPoint(effectloc2, 1.5f);
				if (!players.isEmpty()) {
					for (Player sPlayer : players) {
						if (!sPlayer.equals(player)) {
							sPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, blindnessTime * 20, 1));
							DamageHandler.damageEntity(sPlayer, damage, this);
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void displayHelix(Location location, Location player, Block block) {
		this.currentHeight += 0.1;
		if (this.currentHeight >= player.getY() - location.getY()) {
			this.currentHeight = 0D;
		}
		
		for (int points = 0; points <= 5; points++) {
			double x = Math.cos(currentHeight);
			double z = Math.sin(currentHeight);
			double nx = x * -1;
			double nz = z * -1;
			Location newLoc = new Location(player.getWorld(), location.getX() + x, location.getY() + currentHeight, location.getZ() + z);
			Location secondLoc = new Location(player.getWorld(), location.getX() + nx, location.getY() + currentHeight, location.getZ() + nz);
			
			if (block != null && ((block.getType() == Material.SAND && block.getData() == (byte) 0) || block.getType() == Material.SANDSTONE)) {
				displaySandParticle(newLoc.add(0.5, 0.5, 0.5), 0.1F, 0.1F, 0.1F, 2, 1, false);
				displaySandParticle(secondLoc.add(0.5, 0.5, 0.5), 0.1F, 0.1F, 0.1F, 2, 1, false);
			} else if (block != null && ((block.getType() == Material.SAND && block.getData() == (byte) 1) || block.getType() == Material.RED_SANDSTONE)) {
				displaySandParticle(newLoc.add(0.5, 0.5, 0.5), 0.1F, 0.1F, 0.1F, 2, 1, true);
				displaySandParticle(secondLoc.add(0.5, 0.5, 0.5), 0.1F, 0.1F, 0.1F, 2, 1, true);
			}
		}
	}

	public static boolean removeSpouts(Location location, double radius, Player sourcePlayer) {
		boolean removed = false;
		for (SandSpout spout : getAbilities(SandSpout.class)) {
			Player player = spout.player;
			if (!player.equals(sourcePlayer)) {
				Location loc1 = player.getLocation().getBlock().getLocation();
				location = location.getBlock().getLocation();
				double dx = loc1.getX() - location.getX();
				double dy = loc1.getY() - location.getY();
				double dz = loc1.getZ() - location.getZ();

				double distance = Math.sqrt(dx * dx + dz * dz);

				if (distance <= radius && dy > 0 && dy < spout.height) {
					spout.remove();
					removed = true;
				}
			}
		}
		return removed;
	}

	@Override
	public void remove() {
		super.remove();
		flight.revert();
		removeFlight();
	}

	@Override
	public String getName() {
		return "SandSpout";
	}

	@Override
	public Location getLocation() {
		return player != null ? player.getLocation() : null;
	}

	@Override
	public long getCooldown() {
		return 0;
	}
	
	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	public boolean isCanSpiral() {
		return canSpiral;
	}

	public void setCanSpiral(boolean canSpiral) {
		this.canSpiral = canSpiral;
	}

	public int getAngle() {
		return angle;
	}

	public void setAngle(int angle) {
		this.angle = angle;
	}

	public int getBlindnessTime() {
		return blindnessTime;
	}

	public void setBlindnessTime(int blindnessTime) {
		this.blindnessTime = blindnessTime;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public double getCurrentHeight() {
		return currentHeight;
	}

	public void setCurrentHeight(double currentHeight) {
		this.currentHeight = currentHeight;
	}
	
}
