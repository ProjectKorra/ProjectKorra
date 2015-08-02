package com.projectkorra.projectkorra.firebending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AvatarState;
import com.projectkorra.projectkorra.ability.StockAbility;
import com.projectkorra.projectkorra.ability.api.CoreAbility;
import com.projectkorra.projectkorra.airbending.AirMethods;
import com.projectkorra.projectkorra.util.ParticleEffect;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class WallOfFire extends CoreAbility {

	private static double maxangle = 50;

	private static int RANGE = config.get().getInt("Abilities.Fire.WallOfFire.Range");

	private static int HEIGHT = config.get().getInt("Abilities.Fire.WallOfFire.Height");
	private static int WIDTH = config.get().getInt("Abilities.Fire.WallOfFire.Width");
	private static long DURATION = config.get().getLong("Abilities.Fire.WallOfFire.Duration");
	private static int DAMAGE = config.get().getInt("Abilities.Fire.WallOfFire.Damage");
	private static long interval = 250;
	private static long COOLDOWN = config.get().getLong("Abilities.Fire.WallOfFire.Cooldown");
	private static long DAMAGE_INTERVAL = config.get().getLong("Abilities.Fire.WallOfFire.Interval");
	private static double FIRETICKS = config.get().getDouble("Abilities.Fire.WallOfFire.FireTicks");
	private Player player;

	private Location origin;
	private long time, starttime;
	private boolean active = true;
	private int damagetick = 0, intervaltick = 0;
	private int range = RANGE;
	private int height = HEIGHT;
	private int width = WIDTH;
	private long duration = DURATION;
	private int damage = DAMAGE;
	private long cooldown = COOLDOWN;
	private long damageinterval = DAMAGE_INTERVAL;
	private List<Block> blocks = new ArrayList<Block>();

	public WallOfFire(Player player) {
		/* Initial Checks */
		if (containsPlayer(player, WallOfFire.class) && !AvatarState.isAvatarState(player)) {
			return;
		}
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
		if (bPlayer.isOnCooldown("WallOfFire"))
			return;
		/* End Initial Checks */

		this.player = player;

		origin = GeneralMethods.getTargetedLocation(player, range);

		World world = player.getWorld();

		if (FireMethods.isDay(player.getWorld())) {
			width = (int) FireMethods.getFirebendingDayAugment((double) width, world);
			height = (int) FireMethods.getFirebendingDayAugment((double) height, world);
			duration = (long) FireMethods.getFirebendingDayAugment((double) duration, world);
			damage = (int) FireMethods.getFirebendingDayAugment((double) damage, world);
		}

		time = System.currentTimeMillis();
		starttime = time;

		Block block = origin.getBlock();

		if (block.isLiquid() || GeneralMethods.isSolid(block)) {
			return;
		}

		Vector direction = player.getEyeLocation().getDirection();
		Vector compare = direction.clone();
		compare.setY(0);

		if (Math.abs(direction.angle(compare)) > Math.toRadians(maxangle)) {
			return;
		}

		initializeBlocks();

		//instances.put(player, this);
		putInstance(player, this);
		bPlayer.addCooldown("WallOfFire", cooldown);
	}

	private void affect(Entity entity) {
		entity.setFireTicks((int) (FIRETICKS * 20));
		GeneralMethods.setVelocity(entity, new Vector(0, 0, 0));
		if (entity instanceof LivingEntity) {
			GeneralMethods.damageEntity(player, entity, damage);
			new Enflamed(entity, player);
			AirMethods.breakBreathbendingHold(entity);
		}
	}

	private void damage() {
		double radius = height;
		if (radius < width)
			radius = width;
		radius = radius + 1;
		List<Entity> entities = GeneralMethods.getEntitiesAroundPoint(origin, radius);
		if (entities.contains(player))
			entities.remove(player);
		for (Entity entity : entities) {
			if (GeneralMethods.isRegionProtectedFromBuild(player, "WallOfFire", entity.getLocation()))
				continue;
			for (Block block : blocks) {
				if (entity.getLocation().distance(block.getLocation()) <= 1.5) {
					affect(entity);
					break;
				}
			}
		}
	}

	private void display() {
		for (Block block : blocks) {
			ParticleEffect.FLAME.display(block.getLocation(), 0.6F, 0.6F, 0.6F, 0, 6);
			ParticleEffect.SMOKE.display(block.getLocation(), 0.6F, 0.6F, 0.6F, 0, 6);

			if (GeneralMethods.rand.nextInt(7) == 0) {
				FireMethods.playFirebendingSound(block.getLocation());
			}
		}
	}

	public long getCooldown() {
		return cooldown;
	}

	public int getDamage() {
		return damage;
	}

	public long getDamageinterval() {
		return damageinterval;
	}

	public long getDuration() {
		return duration;
	}

	public int getHeight() {
		return height;
	}

	public Player getPlayer() {
		return player;
	}

	public int getRange() {
		return range;
	}

	@Override
	public StockAbility getStockAbility() {
		return StockAbility.WallOfFire;
	}

	public int getWidth() {
		return width;
	}

	private void initializeBlocks() {
		Vector direction = player.getEyeLocation().getDirection();
		direction = direction.normalize();

		Vector ortholr = GeneralMethods.getOrthogonalVector(direction, 0, 1);
		ortholr = ortholr.normalize();

		Vector orthoud = GeneralMethods.getOrthogonalVector(direction, 90, 1);
		orthoud = orthoud.normalize();

		double w = (double) width;
		double h = (double) height;

		for (double i = -w; i <= w; i++) {
			for (double j = -h; j <= h; j++) {
				Location location = origin.clone().add(orthoud.clone().multiply(j));
				location = location.add(ortholr.clone().multiply(i));
				if (GeneralMethods.isRegionProtectedFromBuild(player, "WallOfFire", location))
					continue;
				Block block = location.getBlock();
				if (!blocks.contains(block))
					blocks.add(block);
			}
		}

	}

	@Override
	public boolean progress() {
		time = System.currentTimeMillis();

		if (time - starttime > cooldown) {
			remove();
			return false;
		}

		if (!active)
			return false;

		if (time - starttime > duration) {
			active = false;
			return false;
		}

		if (time - starttime > intervaltick * interval) {
			intervaltick++;
			display();
		}

		if (time - starttime > damagetick * damageinterval) {
			damagetick++;
			damage();
		}
		return true;
	}

	@Override
	public void reloadVariables() {
		RANGE = config.get().getInt("Abilities.Fire.WallOfFire.Range");
		HEIGHT = config.get().getInt("Abilities.Fire.WallOfFire.Height");
		WIDTH = config.get().getInt("Abilities.Fire.WallOfFire.Width");
		DURATION = config.get().getLong("Abilities.Fire.WallOfFire.Duration");
		DAMAGE = config.get().getInt("Abilities.Fire.WallOfFire.Damage");
		COOLDOWN = config.get().getLong("Abilities.Fire.WallOfFire.Cooldown");
		DAMAGE_INTERVAL = config.get().getLong("Abilities.Fire.WallOfFire.Interval");
		FIRETICKS = config.get().getDouble("Abilities.Fire.WallOfFire.FireTicks");
		range = RANGE;
		height = HEIGHT;
		width = WIDTH;
		duration = DURATION;
		damage = DAMAGE;
		cooldown = COOLDOWN;
		damageinterval = DAMAGE_INTERVAL;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
		if (player != null)
			GeneralMethods.getBendingPlayer(player.getName()).addCooldown("WallOfFire", cooldown);
	}

	public void setDamage(int damage) {
		this.damage = damage;
	}

	public void setDamageinterval(long damageinterval) {
		this.damageinterval = damageinterval;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setRange(int range) {
		this.range = range;
	}

	public void setWidth(int width) {
		this.width = width;
	}
}
