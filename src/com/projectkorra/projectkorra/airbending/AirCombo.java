package com.projectkorra.projectkorra.airbending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AvatarState;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.configuration.ConfigLoadable;
import com.projectkorra.projectkorra.earthbending.EarthMethods;
import com.projectkorra.projectkorra.firebending.FireCombo;
import com.projectkorra.projectkorra.firebending.FireMethods;
import com.projectkorra.projectkorra.firebending.FireCombo.FireComboStream;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.Flight;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class AirCombo implements ConfigLoadable {

	public static enum AbilityState {
		TWISTER_MOVING, TWISTER_STATIONARY
	}

	public static ArrayList<AirCombo> instances = new ArrayList<AirCombo>();

	public static double twisterSpeed = config.get().getDouble("Abilities.Air.AirCombo.Twister.Speed");
	public static double twisterRange = config.get().getDouble("Abilities.Air.AirCombo.Twister.Range");
	public static double twisterHeight = config.get().getDouble("Abilities.Air.AirCombo.Twister.Height");
	public static double twisterRadius = config.get().getDouble("Abilities.Air.AirCombo.Twister.Radius");
	public static double twisterDegreePerParticle = config.get().getDouble("Abilities.Air.AirCombo.Twister.DegreesPerParticle");
	public static double twisterHeightPerParticle = config.get().getDouble("Abilities.Air.AirCombo.Twister.HeightPerParticle");
	public static long twisterRemoveDelay = config.get().getLong("Abilities.Air.AirCombo.Twister.RemoveDelay");
	public static long twisterCooldown = config.get().getLong("Abilities.Air.AirCombo.Twister.Cooldown");

	public static double airStreamSpeed = config.get().getDouble("Abilities.Air.AirCombo.AirStream.Speed");
	public static double airStreamRange = config.get().getDouble("Abilities.Air.AirCombo.AirStream.Range");
	public static double airStreamEntityHeight = config.get().getDouble("Abilities.Air.AirCombo.AirStream.EntityHeight");
	public static long airStreamEntityDuration = config.get().getLong("Abilities.Air.AirCombo.AirStream.EntityDuration");
	public static long airStreamCooldown = config.get().getLong("Abilities.Air.AirCombo.AirStream.Cooldown");

	public static double airSweepSpeed = config.get().getDouble("Abilities.Air.AirCombo.AirSweep.Speed");
	public static double airSweepRange = config.get().getDouble("Abilities.Air.AirCombo.AirSweep.Range");
	public static double airSweepDamage = config.get().getDouble("Abilities.Air.AirCombo.AirSweep.Damage");
	public static double airSweepKnockback = config.get().getDouble("Abilities.Air.AirCombo.AirSweep.Knockback");
	public static long airSweepCooldown = config.get().getLong("Abilities.Air.AirCombo.AirSweep.Cooldown");

	private static boolean enabled = config.get().getBoolean("Abilities.Air.AirCombo.Enabled");

	public double airSliceSpeed = 0.7;
	public double airSliceRange = 10;
	public double airSliceDamage = 3;
	public long airSliceCooldown = 500;

	private Player player;
	private BendingPlayer bPlayer;
	private ClickType type;
	private String ability;

	private long time;
	private Location origin;
	private Location currentLoc;
	private Location destination;
	private Vector direction;
	private int progressCounter = 0;
	private double damage = 0, speed = 0, range = 0, knockback = 0;
	private long cooldown = 0;
	private AbilityState state;
	private ArrayList<Entity> affectedEntities = new ArrayList<Entity>();
	private ArrayList<BukkitRunnable> tasks = new ArrayList<BukkitRunnable>();
	private ArrayList<Flight> flights = new ArrayList<Flight>();

	public AirCombo(Player player, String ability) {
		/* Initial Checks */

		if (!enabled)
			return;
		if (Commands.isToggledForAll)
			return;
		this.bPlayer = GeneralMethods.getBendingPlayer(player.getName());
		if (!bPlayer.isToggled())
			return;
		if (!bPlayer.hasElement(Element.Air))
			return;
		if (!GeneralMethods.canBend(player.getName(), ability)) {
			return;
		}
		if (GeneralMethods.isRegionProtectedFromBuild(player, "AirBlast", player.getLocation()))
			return;
		/* End Initial Checks */
		// reloadVariables();
		time = System.currentTimeMillis();
		this.player = player;
		this.ability = ability;

		if (ability.equalsIgnoreCase("Twister")) {
			damage = 0;
			range = twisterRange;
			speed = twisterSpeed;
			cooldown = twisterCooldown;
		} else if (ability.equalsIgnoreCase("AirStream")) {
			damage = 0;
			range = airStreamRange;
			speed = airStreamSpeed;
			cooldown = airStreamCooldown;
		} else if (ability.equalsIgnoreCase("AirSlice")) {
			damage = airSliceDamage;
			range = airSliceRange;
			speed = airSliceSpeed;
			cooldown = airSliceCooldown;
		} else if (ability.equalsIgnoreCase("AirSweep")) {
			damage = airSweepDamage;
			range = airSweepRange;
			speed = airSweepSpeed;
			knockback = airSweepKnockback;
			cooldown = airSweepCooldown;
		}
		if (AvatarState.isAvatarState(player)) {
			cooldown = 0;
			damage = AvatarState.getValue(damage);
			range = AvatarState.getValue(range);
			knockback = knockback * 1.4;
		}
		instances.add(this);
	}

	public void progress() {
		progressCounter++;
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}

		if (ability.equalsIgnoreCase("Twister")) {
			if (destination == null) {
				if (bPlayer.isOnCooldown("Twister") && !AvatarState.isAvatarState(player)) {
					remove();
					return;
				}
				bPlayer.addCooldown("Twister", cooldown);
				state = AbilityState.TWISTER_MOVING;
				direction = player.getEyeLocation().getDirection().clone().normalize();
				direction.setY(0);
				origin = player.getLocation().add(direction.clone().multiply(2));
				destination = player.getLocation().add(direction.clone().multiply(range));
				currentLoc = origin.clone();
			}
			if (origin.distance(currentLoc) < origin.distance(destination) && state == AbilityState.TWISTER_MOVING)
				currentLoc.add(direction.clone().multiply(speed));
			else if (state == AbilityState.TWISTER_MOVING) {
				state = AbilityState.TWISTER_STATIONARY;
				time = System.currentTimeMillis();
			} else if (System.currentTimeMillis() - time >= twisterRemoveDelay) {
				remove();
				return;
			} else if (GeneralMethods.isRegionProtectedFromBuild(player, "AirBlast", currentLoc)) {
				remove();
				return;
			}

			Block topBlock = GeneralMethods.getTopBlock(currentLoc, 3, -3);
			if (topBlock == null) {
				remove();
				return;
			}
			currentLoc.setY(topBlock.getLocation().getY());

			double height = twisterHeight;
			double radius = twisterRadius;
			for (double y = 0; y < height; y += twisterHeightPerParticle) {
				double animRadius = ((radius / height) * y);
				for (double i = -180; i <= 180; i += twisterDegreePerParticle) {
					Vector animDir = GeneralMethods.rotateXZ(new Vector(1, 0, 1), i);
					Location animLoc = currentLoc.clone().add(animDir.multiply(animRadius));
					animLoc.add(0, y, 0);
					AirMethods.playAirbendingParticles(animLoc, 1, 0, 0, 0);
				}
			}
			AirMethods.playAirbendingSound(currentLoc);

			for (int i = 0; i < height; i += 3)
				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(currentLoc.clone().add(0, i, 0), radius * 0.75))
					if (!affectedEntities.contains(entity) && !entity.equals(player))
						affectedEntities.add(entity);

			for (Entity entity : affectedEntities) {
				Vector forceDir = GeneralMethods.getDirection(entity.getLocation(), currentLoc.clone().add(0, height, 0));

				if (entity instanceof Player) {
					if (Commands.invincible.contains(((Player) entity).getName()))
						break;
				}

				entity.setVelocity(forceDir.clone().normalize().multiply(0.3));
			}
		}

		else if (ability.equalsIgnoreCase("AirStream")) {
			if (destination == null) {
				if (bPlayer.isOnCooldown("AirStream") && !AvatarState.isAvatarState(player)) {
					remove();
					return;
				}
				bPlayer.addCooldown("AirStream", cooldown);
				origin = player.getEyeLocation();
				currentLoc = origin.clone();
			}
			Entity target = GeneralMethods.getTargetedEntity(player, range, new ArrayList<Entity>());

			if (target instanceof Player) {
				if (Commands.invincible.contains(((Player) target).getName()))
					return;
			}

			if (target != null && target.getLocation().distance(currentLoc) > 7)
				destination = target.getLocation();
			else
				destination = GeneralMethods.getTargetedLocation(player, range, EarthMethods.transparentToEarthbending);

			direction = GeneralMethods.getDirection(currentLoc, destination).normalize();
			currentLoc.add(direction.clone().multiply(speed));
			if (!EarthMethods.isTransparentToEarthbending(player, currentLoc.getBlock()))
				currentLoc.subtract(direction.clone().multiply(speed));
			
			if (player.getWorld() != currentLoc.getWorld()) {
				remove();
				return;
			}

			if (Math.abs(player.getLocation().distance(currentLoc)) > range) {
				remove();
				return;
			} else if (affectedEntities.size() > 0 && System.currentTimeMillis() - time >= airStreamEntityDuration) {
				remove();
				return;
			} else if (!player.isSneaking()) {
				remove();
				return;
			} else if (!EarthMethods.isTransparentToEarthbending(player, currentLoc.getBlock())) {
				remove();
				return;
			} else if (currentLoc.getY() - origin.getY() > airStreamEntityHeight) {
				remove();
				return;
			} else if (GeneralMethods.isRegionProtectedFromBuild(player, "AirBlast", currentLoc)) {
				remove();
				return;
			} else if (FireMethods.isWithinFireShield(currentLoc)) {
				remove();
				return;
			} else if (AirMethods.isWithinAirShield(currentLoc)) {
				remove();
				return;
			}

			for (int i = 0; i < 10; i++) {
				BukkitRunnable br = new BukkitRunnable() {
					final Location loc = currentLoc.clone();
					final Vector dir = direction.clone();

					@Override
					public void run() {
						for (int angle = -180; angle <= 180; angle += 45) {
							Vector orthog = GeneralMethods.getOrthogonalVector(dir.clone(), angle, 0.5);
							AirMethods.playAirbendingParticles(loc.clone().add(orthog), 1, 0F, 0F, 0F);
						}
					}
				};
				br.runTaskLater(ProjectKorra.plugin, i * 2);
				tasks.add(br);
			}

			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(currentLoc, 2.8)) {
				if (affectedEntities.size() == 0) {
					// Set the timer to remove the ability
					time = System.currentTimeMillis();
				}
				if (!entity.equals(player) && !affectedEntities.contains(entity)) {
					affectedEntities.add(entity);
					if (entity instanceof Player) {
						flights.add(new Flight((Player) entity, player));
					}
				}
			}

			for (Entity entity : affectedEntities) {
				Vector force = GeneralMethods.getDirection(entity.getLocation(), currentLoc);
				entity.setVelocity(force.clone().normalize().multiply(speed));
				entity.setFallDistance(0F);
			}
		}

		else if (ability.equalsIgnoreCase("AirSlice")) {
			if (origin == null) {
				if (bPlayer.isOnCooldown("AirSlice") && !AvatarState.isAvatarState(player)) {
					remove();
					return;
				}
				bPlayer.addCooldown("AirSlice", cooldown);
				origin = player.getLocation();
				currentLoc = origin.clone();
				direction = player.getEyeLocation().getDirection();

				for (double i = -5; i < 10; i += 1) {
					FireComboStream fs = new FireComboStream(null, direction.clone().add(new Vector(0, 0.03 * i, 0)),
							player.getLocation(), range, speed, "AirSlice");
					fs.setDensity(1);
					fs.setSpread(0F);
					fs.setUseNewParticles(true);
					fs.setParticleEffect(AirMethods.getAirbendingParticles());
					fs.setCollides(false);
					fs.runTaskTimer(ProjectKorra.plugin, 0, 1L);
					tasks.add(fs);
				}
			}
			manageAirVectors();
			for (Entity entity : affectedEntities)
				if (entity instanceof LivingEntity) {
					remove();
					return;
				}
		}

		else if (ability.equalsIgnoreCase("AirSweep")) {
			if (origin == null) {
				if (bPlayer.isOnCooldown("AirSweep") && !AvatarState.isAvatarState(player)) {
					remove();
					return;
				}
				bPlayer.addCooldown("AirSweep", cooldown);
				direction = player.getEyeLocation().getDirection().normalize();
				origin = player.getLocation().add(direction.clone().multiply(10));

			}
			if (progressCounter < 8)
				return;

			if (destination == null) {
				destination = player.getLocation().add(player.getEyeLocation().getDirection().normalize().multiply(10));

				// if (Math.abs(origin.distance(destination)) < 7) {
				// remove();
				// return;
				// }

				Vector origToDest = GeneralMethods.getDirection(origin, destination);
				for (double i = 0; i < 30; i++) {
					Vector vec = GeneralMethods.getDirection(player.getLocation(),
							origin.clone().add(origToDest.clone().multiply(i / 30)));

					FireComboStream fs = new FireComboStream(null, vec, player.getLocation(), range, speed, "AirSweep");
					fs.setDensity(1);
					fs.setSpread(0F);
					fs.setUseNewParticles(true);
					fs.setParticleEffect(AirMethods.getAirbendingParticles());
					fs.setCollides(false);
					fs.runTaskTimer(ProjectKorra.plugin, (long) (i / 2.5), 1L);
					tasks.add(fs);
				}
			}
			manageAirVectors();
		}
	}

	public void manageAirVectors() {
		for (int i = 0; i < tasks.size(); i++) {
			if (((FireComboStream) tasks.get(i)).isCancelled()) {
				tasks.remove(i);
				i--;
			}
		}
		if (tasks.size() == 0) {
			remove();
			return;
		}
		for (int i = 0; i < tasks.size(); i++) {
			FireComboStream fstream = (FireComboStream) tasks.get(i);
			Location loc = fstream.getLocation();

			if (!EarthMethods.isTransparentToEarthbending(player, loc.getBlock())) {
				if (!EarthMethods.isTransparentToEarthbending(player, loc.clone().add(0, 0.2, 0).getBlock())) {
					fstream.remove();
					return;
				}
			}
			if (i % 3 == 0) {
				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(loc, 2.5)) {
					if (GeneralMethods.isRegionProtectedFromBuild(player, "AirBlast", entity.getLocation())) {
						remove();
						return;
					}
					if (!entity.equals(player) && !affectedEntities.contains(entity)) {
						affectedEntities.add(entity);
						if (knockback != 0) {
							Vector force = fstream.getDirection();
							entity.setVelocity(force.multiply(knockback));
						}
						if (damage != 0)
							if (entity instanceof LivingEntity)
								if (fstream.getAbility().equalsIgnoreCase("AirSweep")) {
									GeneralMethods.damageEntity(player, entity, damage, Element.Air, "AirSweep");
								} else {
									GeneralMethods.damageEntity(player, entity, damage, Element.Air, "AirCombo");
								}
					}
				}

				if (GeneralMethods.blockAbilities(player, FireCombo.abilitiesToBlock, loc, 1)) {
					fstream.remove();
				}
			}
		}
	}

	public void remove() {
		instances.remove(this);
		for (BukkitRunnable task : tasks)
			task.cancel();
		for (int i = 0; i < flights.size(); i++) {
			Flight flight = flights.get(i);
			flight.revert();
			flight.remove();
			flights.remove(i);
			i--;
		}
	}

	public static void progressAll() {
		for (int i = instances.size() - 1; i >= 0; i--)
			instances.get(i).progress();
	}

	public static void removeAll() {
		for (int i = instances.size() - 1; i >= 0; i--) {
			instances.get(i).remove();
		}
	}

	public Player getPlayer() {
		return player;
	}

	public static ArrayList<AirCombo> getAirCombo(Player player) {
		ArrayList<AirCombo> list = new ArrayList<AirCombo>();
		for (AirCombo combo : instances)
			if (combo.player != null && combo.player == player)
				list.add(combo);
		return list;
	}

	public static ArrayList<AirCombo> getAirCombo(Player player, ClickType type) {
		ArrayList<AirCombo> list = new ArrayList<AirCombo>();
		for (AirCombo combo : instances)
			if (combo.player != null && combo.player == player && combo.type != null && combo.type == type)
				list.add(combo);
		return list;
	}

	public static boolean removeAroundPoint(Player player, String ability, Location loc, double radius) {
		boolean removed = false;
		for (int i = 0; i < instances.size(); i++) {
			AirCombo combo = instances.get(i);
			if (combo.getPlayer().equals(player))
				continue;

			if (ability.equalsIgnoreCase("Twister") && combo.ability.equalsIgnoreCase("Twister")) {
				if (combo.currentLoc != null && Math.abs(combo.currentLoc.distance(loc)) <= radius) {
					instances.remove(combo);
					removed = true;
				}
			}

			else if (ability.equalsIgnoreCase("AirStream") && combo.ability.equalsIgnoreCase("AirStream")) {
				if (combo.currentLoc != null && Math.abs(combo.currentLoc.distance(loc)) <= radius) {
					instances.remove(combo);
					removed = true;
				}
			}

			else if (ability.equalsIgnoreCase("AirSweep") && combo.ability.equalsIgnoreCase("AirSweep")) {
				for (int j = 0; j < combo.tasks.size(); j++) {
					FireComboStream fs = (FireComboStream) combo.tasks.get(j);
					if (fs.getLocation() != null && fs.getLocation().getWorld().equals(loc.getWorld())
							&& Math.abs(fs.getLocation().distance(loc)) <= radius) {
						fs.remove();
						removed = true;
					}
				}
			}
		}
		return removed;
	}

	@Override
	public void reloadVariables() {
		twisterSpeed = config.get().getDouble("Abilities.Air.AirCombo.Twister.Speed");
		twisterRange = config.get().getDouble("Abilities.Air.AirCombo.Twister.Range");
		twisterHeight = config.get().getDouble("Abilities.Air.AirCombo.Twister.Height");
		twisterRadius = config.get().getDouble("Abilities.Air.AirCombo.Twister.Radius");
		twisterDegreePerParticle = config.get().getDouble("Abilities.Air.AirCombo.Twister.DegreesPerParticle");
		twisterHeightPerParticle = config.get().getDouble("Abilities.Air.AirCombo.Twister.HeightPerParticle");
		twisterRemoveDelay = config.get().getLong("Abilities.Air.AirCombo.Twister.RemoveDelay");
		twisterCooldown = config.get().getLong("Abilities.Air.AirCombo.Twister.Cooldown");

		airStreamSpeed = config.get().getDouble("Abilities.Air.AirCombo.AirStream.Speed");
		airStreamRange = config.get().getDouble("Abilities.Air.AirCombo.AirStream.Range");
		airStreamEntityHeight = config.get().getDouble("Abilities.Air.AirCombo.AirStream.EntityHeight");
		airStreamEntityDuration = config.get().getLong("Abilities.Air.AirCombo.AirStream.EntityDuration");
		airStreamCooldown = config.get().getLong("Abilities.Air.AirCombo.AirStream.Cooldown");

		airSweepSpeed = config.get().getDouble("Abilities.Air.AirCombo.AirSweep.Speed");
		airSweepRange = config.get().getDouble("Abilities.Air.AirCombo.AirSweep.Range");
		airSweepDamage = config.get().getDouble("Abilities.Air.AirCombo.AirSweep.Damage");
		airSweepKnockback = config.get().getDouble("Abilities.Air.AirCombo.AirSweep.Knockback");
		airSweepCooldown = config.get().getLong("Abilities.Air.AirCombo.AirSweep.Cooldown");

		enabled = config.get().getBoolean("Abilities.Air.AirCombo.Enabled");
	}
}
