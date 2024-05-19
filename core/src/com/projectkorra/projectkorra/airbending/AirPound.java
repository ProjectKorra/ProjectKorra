package com.projectkorra.projectkorra.airbending;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.object.HorizontalVelocityTracker;
import com.projectkorra.projectkorra.region.RegionProtection;
import com.projectkorra.projectkorra.util.ActionBar;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class AirPound extends AirAbility {

	@Attribute(Attribute.COOLDOWN)
	private long minCooldown, cooldown, maxCooldown;
	@Attribute(Attribute.RANGE)
	private double minRange, range, maxRange;
	@Attribute(Attribute.RADIUS)
	private double radius;
	@Attribute(Attribute.DAMAGE)
	private double minDamage, damage, maxDamage;
	@Attribute(Attribute.SPEED)
	private double minSpeed, speed, maxSpeed;
	@Attribute(Attribute.KNOCKBACK)
	private double minKnockback, knockback, maxKnockback;
	private boolean showChargeNumber;
	private boolean doesBlindness;
	private boolean doesSlowness;
	private int blindnessAmplifier, blindnessDuration;
	private int slownessAmplifier, slownessDuration;
	private int maxCharge;
	private int chargePerBlock;

	private enum PoundState {
		RUNNING, BLAST, MEDIUM, POUND
	}
	private PoundState state;

	private Location origin;
	private Location location;
	private Vector direction;

	private double change;
	private double chargeUp;
	private int y;
	private StringBuilder chargeBar = new StringBuilder("⬜ ⬜ ⬜ ⬜ ⬜");
	private int[] chargePartition = { 0, 0, 0, 0, 0 };

	public AirPound(Player player) {
		super(player);

		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		} else if (bPlayer.isOnCooldown(this)) {
			return;
		} else if (RegionProtection.isRegionProtected(player, this)) {
			return;
		}
		this.minCooldown = getConfig().getLong("Abilities.Air.AirPound.MinCooldown");
		this.minRange = getConfig().getDouble("Abilities.Air.AirPound.MinRange");
		this.minDamage = getConfig().getDouble("Abilities.Air.AirPound.MinDamage");
		this.minSpeed = getConfig().getDouble("Abilities.Air.AirPound.MinSpeed");
		this.minKnockback = getConfig().getDouble("Abilities.Air.AirPound.MinKnockback");

		this.cooldown = this.minCooldown;
		this.range = this.minRange;
		this.damage = this.minDamage;
		this.speed = this.minSpeed;
		this.knockback = this.minKnockback;

		this.maxCooldown = getConfig().getLong("Abilities.Air.AirPound.MaxCooldown");
		this.maxRange = getConfig().getDouble("Abilities.Air.AirPound.MaxRange");
		this.maxDamage = getConfig().getDouble("Abilities.Air.AirPound.MaxDamage");
		this.maxSpeed = getConfig().getDouble("Abilities.Air.AirPound.MaxSpeed");
		this.maxKnockback = getConfig().getDouble("Abilities.Air.AirPound.MaxKnockback");

		this.doesBlindness = getConfig().getBoolean("Abilities.Air.AirPound.PoundBlindness.Enabled");
		this.blindnessAmplifier = getConfig().getInt("Abilities.Air.AirPound.PoundBlindness.Amplifier");
		this.blindnessDuration = getConfig().getInt("Abilities.Air.AirPound.PoundBlindness.Duration");

		this.doesSlowness = getConfig().getBoolean("Abilities.Air.AirPound.PoundSlowness.Enabled");
		this.slownessAmplifier = getConfig().getInt("Abilities.Air.AirPound.PoundSlowness.Amplifier");
		this.slownessDuration = getConfig().getInt("Abilities.Air.AirPound.PoundSlowness.Duration");

		this.showChargeNumber = getConfig().getBoolean("Abilities.Air.AirPound.ShowChargeNumber");
		this.maxCharge = getConfig().getInt("Abilities.Air.AirPound.MaxCharge");
		this.chargePerBlock = getConfig().getInt("Abilities.Air.AirPound.ChargePerBlock");

		this.state = PoundState.RUNNING;

		int partition = maxCharge / 5;
		for (int i = 1; i <= 5; i++) {
			chargePartition[i - 1] = partition * i;
		}

		start();
	}

	@Override
	public void progress() {
		if (state != PoundState.RUNNING) {
			location.add(direction.clone().multiply(speed));

			playAirbendingSound(location);

			if (location.distanceSquared(origin) > range * range) {
				remove();
				return;
			}
			if (RegionProtection.isRegionProtected(player, location, this)) {
				remove();
				return;
			}
			switch (state) {
				case BLAST:
					// Rotate spiral by 20 points. Higher is faster, but looks wonkier.
					change = change > 360 ? 0 : change + 20;

					playAirbendingParticles(location, 5, 0.25, 0.25, 0.25);

					generateSpirals(location, direction, 1, 2, (int) change, true, loc -> playAirbendingParticles(loc, 5, 0, 0, 0));
					break;
				case MEDIUM:
					// Rotate spiral by 15 points.
					change = change > 360 ? 0 : change + 15;

					playAirbendingParticles(location, 8, 0.4, 0.4, 0.4);

					generateSpirals(location, direction, 1.75, 2, (int) change, true, loc -> playAirbendingParticles(loc, 5, 0, 0, 0));
					generateSpirals(location, direction, 2.5, 3, (int) change, false, loc -> playAirbendingParticles(loc, 5, 0, 0, 0));
					break;
				case POUND:
					// Incrementing "change" each tick will make the circle grow and create
					// the gust looking attack we're looking for.
					change += 0.3;
					radius = change;

					generateDirectionalCircle(location, direction, change, 8, loc -> {
						if (ThreadLocalRandom.current().nextInt(8) == 0) {
							playAirbendingParticles(loc, 5, 0.5, 0.5, 0.5);
						}
					});
					break;
			}
			affectEntities();
		} else {
			chargeUp();
		}
	}

	private void chargeUp() {
		boolean onGround = true;

		// See if player is on ground.
		if (!GeneralMethods.isSolid(player.getLocation().getBlock().getRelative(BlockFace.DOWN, 4))) {
			onGround = false;
		}
		// If they are in the air, we'll get some charges through height.
		if (!onGround) {
			boolean foundGround = false;
			y = 0;
			int i = 0;

			while (!foundGround) {
				y++;
				i++;
				if (GeneralMethods.isSolid(player.getLocation().clone().subtract(0, i, 0).getBlock().getRelative(BlockFace.DOWN))) {
					foundGround = true;
				}
				// In case the player is in the void: causes infinite loop.
				// There's also no point to keep generating charge after a certain point.
				if (i > 400) {
					break;
				}
			}
			y *= chargePerBlock;
			chargeUp = Math.max(chargeUp, y);
			if (chargeUp > maxCharge) {
				chargeUp = maxCharge;
			}
			playAirbendingSound(player.getLocation());
			chargeActionBar(true);
		} else {
			// If the player is on the ground, we'll get charges through momentum/sprinting.
			if (player.isSprinting()) {
				if (chargeUp < maxCharge) {
					chargeUp++;
				}
				playAirbendingSound(player.getLocation());
				chargeActionBar(true);
			} else {
				// If the player is standing still (even for a second) on the ground, we'll start to lose charges.
				chargeUp = chargeUp <= 0 ? 0 : chargeUp - 1;
				chargeActionBar(false);
			}
		}
	}

	private void chargeActionBar(boolean charging) {
		switch (getChargeLevel()) {
			case 0:
				chargeBar = new StringBuilder("⬜ ⬜ ⬜ ⬜ ⬜");
				break;
			case 1:
				chargeBar = new StringBuilder("⬛ ⬜ ⬜ ⬜ ⬜");
				break;
			case 2:
				chargeBar = new StringBuilder("⬛ ⬛ ⬜ ⬜ ⬜");
				break;
			case 3:
				chargeBar = new StringBuilder("⬛ ⬛ ⬛ ⬜ ⬜");
				break;
			case 4:
				chargeBar = new StringBuilder("⬛ ⬛ ⬛ ⬛ ⬜");
				break;
			case 5:
				chargeBar = new StringBuilder("⬛ ⬛ ⬛ ⬛ ⬛");
				break;
		}
		String chargeNumber = showChargeNumber ? " - " + (int) chargeUp : "";
		if (charging) {
			ActionBar.sendActionBar(Element.AIR.getColor() + "Charge: " + ChatColor.GREEN + chargeBar + chargeNumber, player);
		} else {
			ActionBar.sendActionBar(Element.AIR.getColor() + "Charge: " + ChatColor.RED + chargeBar + chargeNumber, player);
		}
	}

	private int getChargeLevel() {
		for (int i = 0; i < 5; i++) {
			if (chargeUp < chargePartition[i]) {
				return i;
			}
		}
		return 5;
	}

	public void pound() {
		if (state != PoundState.RUNNING) {
			return;
		}
		this.origin = player.getEyeLocation().clone();
		this.location = this.origin.clone();
		this.direction = player.getEyeLocation().getDirection();

		// Some calculated stuff for charges.
		cooldown = (long) ((maxCooldown - minCooldown) * (chargeUp / 100.0));
		damage = (chargeUp / 100.0) + minDamage;
		range = ((chargeUp / 100.0) * (minRange)) + minRange;
		knockback = ((chargeUp / 1000.0) * minKnockback) + minKnockback;

		if (damage > maxDamage) {
			damage = maxDamage;
		}
		if (range > maxRange) {
			range = maxRange;
		}
		if (chargeUp < (maxCharge / 3)) { // If the charge is less than 1/3 of max charge, we'll just do a blast.
			if (cooldown < minCooldown) {
				cooldown = minCooldown;
			}
			speed = (chargeUp / 100.0) * (maxSpeed - minSpeed) + minSpeed;
			radius = 1.15;

			state = PoundState.BLAST;
		} else if (chargeUp > (maxCharge / 3) && chargeUp < ((maxCharge) / 3) * 2) { // If the charge is between 1/3 and 2/3 of max charge, we'll do a medium blast.
			speed = maxSpeed;
			radius = 1.25;

			state = PoundState.MEDIUM;
		} else if (chargeUp >= ((maxCharge / 3) * 2)) { // If the charge is above 2/3 of max charge, we'll do a full pound.
			speed = maxSpeed;
			knockback = maxKnockback;

			state = PoundState.POUND;
		}
	}

	public static void pound(Player player) {
		CoreAbility.getAbility(player, AirPound.class).pound();
	}

	private void generateDirectionalCircle(Location location, Vector direction, double radius, int points, Consumer<Location> consumer) {
		for (int i = 0; i < 360; i += points) {
			Vector circle = GeneralMethods.getOrthogonalVector(direction.clone(), i, radius);
			consumer.accept(location.clone().add(circle));
		}
	}

	private void generateSpirals(Location location, Vector direction, double radius, int spirals, int change, boolean clockwise, Consumer<Location> consumer) {
		int gap = 360 / spirals;
		List<Integer> points = new ArrayList<>();

		for (int i = 0; i < spirals; i++) {
			points.add(i * gap);
		}
		for (int point : points) {
			int newPoint = clockwise ? point + change : point - change;
			if (clockwise && point + change > 360) {
				newPoint = point + change - 360;
			} else if (!clockwise && point - change < 0) {
				newPoint = point - change + 360;
			}
			Vector circle = GeneralMethods.getOrthogonalVector(direction.clone(), newPoint, radius);
			consumer.accept(location.clone().add(circle));
		}
	}

	private void affectEntities() {
		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, radius)) {
			if (entity instanceof LivingEntity && entity.getUniqueId() != player.getUniqueId()) {
				if (Commands.invincible.contains(entity.getName()) || (entity instanceof Player && RegionProtection.isRegionProtected((Player) entity, entity.getLocation(), this))) {
					continue;
				}
				DamageHandler.damageEntity(entity, damage, this);

				// Only medium blasts and pounds will do knockback. Blasts will just damage.
				if (state == PoundState.MEDIUM || state == PoundState.POUND) {
					entity.setVelocity(direction.clone().multiply(knockback));
					new HorizontalVelocityTracker(entity, player, 0, this);

					// Pounds can apply blindness and slowness; "knocking them out".
					if (state == PoundState.POUND) {
						if (doesBlindness) {
							((LivingEntity) entity).addPotionEffect(PotionEffectType.BLINDNESS.createEffect(blindnessDuration, blindnessAmplifier));
						}
						if (doesSlowness) {
							((LivingEntity) entity).addPotionEffect(PotionEffectType.SLOW.createEffect(slownessDuration, slownessAmplifier));
						}
					}
				}
			}
		}
	}

	@Override
	public void remove() {
		super.remove();
		bPlayer.addCooldown(this);
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
		return cooldown;
	}

	@Override
	public String getName() {
		return "AirPound";
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public double getCollisionRadius() {
		return radius;
	}

	public double getChargeUp() {
		return chargeUp;
	}

	public void setChargeUp(double chargeUp) {
		this.chargeUp = chargeUp;
	}

	public PoundState getState() {
		return state;
	}

	public void setState(PoundState state) {
		this.state = state;
	}

	public Location getOrigin() {
		return origin;
	}

	public void setOrigin(Location origin) {
		this.origin = origin;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Vector getDirection() {
		return direction;
	}

	public void setDirection(Vector direction) {
		this.direction = direction;
	}

	public double getChange() {
		return change;
	}

	public void setChange(double change) {
		this.change = change;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public long getMinCooldown() {
		return minCooldown;
	}

	public void setMinCooldown(long minCooldown) {
		this.minCooldown = minCooldown;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public double getMaxCooldown() {
		return maxCooldown;
	}

	public void setMaxCooldown(long maxCooldown) {
		this.maxCooldown = maxCooldown;
	}

	public double getMinRange() {
		return minRange;
	}

	public void setMinRange(double minRange) {
		this.minRange = minRange;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getMaxRange() {
		return maxRange;
	}

	public void setMaxRange(double maxRange) {
		this.maxRange = maxRange;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getMinDamage() {
		return minDamage;
	}

	public void setMinDamage(double minDamage) {
		this.minDamage = minDamage;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public double getMaxDamage() {
		return maxDamage;
	}

	public void setMaxDamage(double maxDamage) {
		this.maxDamage = maxDamage;
	}

	public double getMinSpeed() {
		return minSpeed;
	}

	public void setMinSpeed(double minSpeed) {
		this.minSpeed = minSpeed;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public double getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(double maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public double getMinKnockback() {
		return minKnockback;
	}

	public void setMinKnockback(double minKnockback) {
		this.minKnockback = minKnockback;
	}

	public double getKnockback() {
		return knockback;
	}

	public void setKnockback(double knockback) {
		this.knockback = knockback;
	}

	public double getMaxKnockback() {
		return maxKnockback;
	}

	public void setMaxKnockback(double maxKnockback) {
		this.maxKnockback = maxKnockback;
	}

	public boolean doesBlindness() {
		return doesBlindness;
	}

	public void setDoesBlindness(boolean doesBlindness) {
		this.doesBlindness = doesBlindness;
	}

	public boolean doesSlowness() {
		return doesSlowness;
	}

	public void setDoesSlowness(boolean doesSlowness) {
		this.doesSlowness = doesSlowness;
	}

	public int getBlindnessAmplifier() {
		return blindnessAmplifier;
	}

	public void setBlindnessAmplifier(int blindnessAmplifier) {
		this.blindnessAmplifier = blindnessAmplifier;
	}

	public int getBlindnessDuration() {
		return blindnessDuration;
	}

	public void setBlindnessDuration(int blindnessDuration) {
		this.blindnessDuration = blindnessDuration;
	}

	public int getSlownessAmplifier() {
		return slownessAmplifier;
	}

	public void setSlownessAmplifier(int slownessAmplifier) {
		this.slownessAmplifier = slownessAmplifier;
	}

	public int getSlownessDuration() {
		return slownessDuration;
	}

	public void setSlownessDuration(int slownessDuration) {
		this.slownessDuration = slownessDuration;
	}
}
