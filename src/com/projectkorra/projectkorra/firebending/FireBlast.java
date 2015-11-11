package com.projectkorra.projectkorra.firebending;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AvatarState;
import com.projectkorra.projectkorra.airbending.AirMethods;
import com.projectkorra.projectkorra.configuration.ConfigLoadable;
import com.projectkorra.projectkorra.earthbending.EarthBlast;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.waterbending.Plantbending;
import com.projectkorra.projectkorra.waterbending.WaterManipulation;
import com.projectkorra.projectkorra.waterbending.WaterMethods;

public class FireBlast implements ConfigLoadable {
	
	public static ConcurrentHashMap<Integer, FireBlast> instances = new ConcurrentHashMap<>();

	private static double SPEED = config.get().getDouble("Abilities.Fire.FireBlast.Speed");
	private static double PUSH_FACTOR = config.get().getDouble("Abilities.Fire.FireBlast.Push");
	private static double RANGE = config.get().getDouble("Abilities.Fire.FireBlast.Range");
	private static int DAMAGE = config.get().getInt("Abilities.Fire.FireBlast.Damage");
	private static double fireticks = config.get().getDouble("Abilities.Fire.FireBlast.FireTicks");
	private static int idCounter = 0;
	
	/* Package visible variables */
	static boolean dissipate = config.get().getBoolean("Abilities.Fire.FireBlast.Dissipate");
	/* End Package visible variables */

	public static double AFFECTING_RADIUS = 2;

	// public static long interval = 2000;
	public static byte full = 0x0;
	private static boolean canPowerFurnace = true;
	private static final int maxticks = 10000;
	private long cooldown = config.get().getLong("Abilities.Fire.FireBlast.Cooldown");

	private Location location;
	private List<Block> safe = new ArrayList<Block>();
	private Location origin;
	private Vector direction;
	private Player player;
	private double speedfactor;
	private int ticks = 0;
	private int id = 0;
	private double range = RANGE;
	private double damage = DAMAGE;
	private double speed = SPEED;
	private double pushfactor = PUSH_FACTOR;
	private double affectingradius = AFFECTING_RADIUS;
	private boolean showParticles = true;
	private Random rand = new Random();

	public FireBlast(Location location, Vector direction, Player player, int damage, List<Block> safeblocks) {
		/* Initial Checks */
		if (location.getBlock().isLiquid()) {
			return;
		}
		/* End Initial Checks */
		//reloadVariables();
		safe = safeblocks;
		range = FireMethods.getFirebendingDayAugment(range, player.getWorld());
		// timers.put(player, System.currentTimeMillis());
		this.player = player;
		this.location = location.clone();
		origin = location.clone();
		this.direction = direction.clone().normalize();
		this.damage *= 1.5;
		instances.put(idCounter, this);
		this.id = idCounter;
		idCounter = (idCounter + 1) % Integer.MAX_VALUE;
	}

	public FireBlast(Player player) {
		/* Initial Checks */
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
		if (bPlayer.isOnCooldown("FireBlast"))
			return;
		if (player.getEyeLocation().getBlock().isLiquid() || Fireball.isCharging(player)) {
			return;
		}
		/* End Initial Checks */
		//reloadVariables();
		range = FireMethods.getFirebendingDayAugment(range, player.getWorld());
		this.player = player;
		location = player.getEyeLocation();
		origin = player.getEyeLocation();
		direction = player.getEyeLocation().getDirection().normalize();
		location = location.add(direction.clone());
		instances.put(idCounter, this);
		this.id = idCounter;
		idCounter = (idCounter + 1) % Integer.MAX_VALUE;
		bPlayer.addCooldown("FireBlast", cooldown);
		// time = System.currentTimeMillis();
		// timers.put(player, System.currentTimeMillis());
	}

	public static boolean annihilateBlasts(Location location, double radius, Player source) {
		boolean broke = false;
		for (FireBlast blast : instances.values()) {
			Location fireblastlocation = blast.location;
			if (location.getWorld() == fireblastlocation.getWorld() && !blast.player.equals(source)) {
				if (location.distance(fireblastlocation) <= radius) {
					blast.remove();
					broke = true;
				}
			}
		}
		if (Fireball.annihilateBlasts(location, radius, source))
			broke = true;
		return broke;
	}

	public static ArrayList<FireBlast> getAroundPoint(Location location, double radius) {
		ArrayList<FireBlast> list = new ArrayList<FireBlast>();
		for (FireBlast fireBlast : instances.values()) {
			Location fireblastlocation = fireBlast.location;
			if (location.getWorld() == fireblastlocation.getWorld()) {
				if (location.distance(fireblastlocation) <= radius)
					list.add(fireBlast);
			}
		}
		return list;
	}

	public static String getDescription() {
		return "FireBlast is the most fundamental bending technique of a firebender. " + "To use, simply left-click in a direction. A blast of fire will be created at your fingertips. " + "If this blast contacts an enemy, it will dissipate and engulf them in flames, " + "doing additional damage and knocking them back slightly. " + "If the blast hits terrain, it will ignite the nearby area. " + "Additionally, if you hold sneak, you will charge up the fireblast. " + "If you release it when it's charged, it will instead launch a powerful " + "fireball that explodes on contact.";
	}

	public static void removeFireBlastsAroundPoint(Location location, double radius) {
		for (FireBlast fireBlast : instances.values()) {
			Location fireblastlocation = fireBlast.location;
			if (location.getWorld() == fireblastlocation.getWorld()) {
				if (location.distance(fireblastlocation) <= radius)
					fireBlast.remove();
			}
		}
		Fireball.removeFireballsAroundPoint(location, radius);
	}

	private void advanceLocation() {
		if (showParticles) {
			//ParticleEffect.RED_DUST.display((float) 16, (float) 111, (float) 227, 0.01F, 0, location, 256D);
			ParticleEffect.FLAME.display(location, 0.275F, 0.275F, 0.275F, 0, 6);
			ParticleEffect.SMOKE.display(location, 0.3F, 0.3F, 0.3F, 0, 3);
		}
		location = location.add(direction.clone().multiply(speedfactor));
		if (rand.nextInt(4) == 0) {
			FireMethods.playFirebendingSound(location);
		}
	}

	private void affect(Entity entity) {
		if (entity.getUniqueId() != player.getUniqueId()) {
			if (AvatarState.isAvatarState(player)) {
				GeneralMethods.setVelocity(entity, direction.clone().multiply(AvatarState.getValue(pushfactor)));
			} else {
				GeneralMethods.setVelocity(entity, direction.clone().multiply(pushfactor));
			}
			if (entity instanceof LivingEntity) {
				entity.setFireTicks((int) (fireticks * 20));
				GeneralMethods.damageEntity(player, entity, (int) FireMethods.getFirebendingDayAugment((double) damage, entity.getWorld()), "FireBlast");
				AirMethods.breakBreathbendingHold(entity);
				new Enflamed(entity, player);
				remove();
			}
		}
	}

	public double getAffectingradius() {
		return affectingradius;
	}

	public long getCooldown() {
		return cooldown;
	}

	public double getDamage() {
		return damage;
	}

	public Player getPlayer() {
		return player;
	}

	public double getPushfactor() {
		return pushfactor;
	}

	public double getRange() {
		return range;
	}

	public double getSpeed() {
		return speed;
	}

	private void ignite(Location location) {
		for (Block block : GeneralMethods.getBlocksAroundPoint(location, affectingradius)) {
			if (FireStream.isIgnitable(player, block) && !safe.contains(block)) {
				/*if (WaterMethods.isPlantbendable(block)) {
					new Plantbending(block);
				}*/
				if (FireMethods.canFireGrief()) {
					if (WaterMethods.isPlantbendable(block)) new Plantbending(block);
					block.setType(Material.FIRE);
				}
				else FireMethods.createTempFire(block.getLocation());
				//block.setType(Material.FIRE);
				if (dissipate) {
					FireStream.ignitedblocks.put(block, player);
					FireStream.ignitedtimes.put(block, System.currentTimeMillis());
				}
			}
		}
	}

	public boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return false;
		}

		if (GeneralMethods.isRegionProtectedFromBuild(player, "Blaze", location)) {
			remove();
			return false;
		}

		speedfactor = speed * (ProjectKorra.time_step / 1000.);

		ticks++;

		if (ticks > maxticks) {
			remove();
			return false;
		}

		Block block = location.getBlock();

		if (GeneralMethods.isSolid(block) || block.isLiquid()) {
			if (block.getType() == Material.FURNACE && canPowerFurnace) {
				Furnace furnace = (Furnace) block.getState();
				furnace.setBurnTime((short) 800);
				furnace.setCookTime((short) 800);
				furnace.update();
			} else if (FireStream.isIgnitable(player, block.getRelative(BlockFace.UP))) {
				ignite(location);
			}
			remove();
			return false;
		}

		if (location.distance(origin) > range) {
			remove();
			return false;
		}

		WaterMethods.removeWaterSpouts(location, player);
		AirMethods.removeAirSpouts(location, player);

		double radius = affectingradius;
		Player source = player;
		if (EarthBlast.annihilateBlasts(location, radius, source) || WaterManipulation.annihilateBlasts(location, radius, source) || FireBlast.annihilateBlasts(location, radius, source)) {
			remove();
			return false;
		}

		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, affectingradius)) {
			// Block bblock = location.getBlock();
			// Block block1 = entity.getLocation().getBlock();
			// if (bblock.equals(block1))
			affect(entity);
			if (entity instanceof LivingEntity) {
				// Block block2 = ((LivingEntity) entity).getEyeLocation()
				// .getBlock();
				// if (bblock.equals(block1))
				// break;
				// if (bblock.equals(block2)) {
				// affect(entity);
				break;
				// }
			}
		}

		advanceLocation();

		return true;
	}
	
	public void remove() {
		instances.remove(id);
	}

	@Override
	public void reloadVariables() {
		SPEED = config.get().getDouble("Abilities.Fire.FireBlast.Speed");
		PUSH_FACTOR = config.get().getDouble("Abilities.Fire.FireBlast.Push");
		RANGE = config.get().getDouble("Abilities.Fire.FireBlast.Range");
		DAMAGE = config.get().getInt("Abilities.Fire.FireBlast.Damage");
		fireticks = config.get().getDouble("Abilities.Fire.FireBlast.FireTicks");
		dissipate = config.get().getBoolean("Abilities.Fire.FireBlast.Dissipate");
		cooldown = config.get().getLong("Abilities.Fire.FireBlast.Cooldown");
		range = RANGE;
		damage = DAMAGE;
		speed = SPEED;
		pushfactor = PUSH_FACTOR;
		affectingradius = AFFECTING_RADIUS;
	}

	public void setAffectingradius(double affectingradius) {
		this.affectingradius = affectingradius;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
		if (player != null)
			GeneralMethods.getBendingPlayer(player.getName()).addCooldown("FireBlast", cooldown);
	}

	public void setDamage(double dmg) {
		this.damage = dmg;
	}

	public void setPushfactor(double pushfactor) {
		this.pushfactor = pushfactor;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public void setShowParticles(boolean show) {
		this.showParticles = show;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

}
