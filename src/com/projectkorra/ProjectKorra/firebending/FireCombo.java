package com.projectkorra.ProjectKorra.firebending;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.Commands;
import com.projectkorra.ProjectKorra.Element;
import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.Utilities.ClickType;
import com.projectkorra.ProjectKorra.Utilities.ParticleEffect;
import com.projectkorra.ProjectKorra.airbending.AirMethods;
import com.projectkorra.ProjectKorra.chiblocking.ChiMethods;
import com.projectkorra.ProjectKorra.chiblocking.Paralyze;
import com.projectkorra.ProjectKorra.waterbending.Bloodbending;
import com.projectkorra.ProjectKorra.waterbending.WaterMethods;

public class FireCombo {
	public static final List<String> abilitiesToBlock = new ArrayList<String>() {
		private static final long serialVersionUID = 5395690551860441647L;
		{
			add("AirShield");
			add("FireShield");
			add("AirSwipe");
			add("FireBlast");
			add("EarthBlast");
			add("WaterManipulation");
			add("Combustion");
			add("FireKick");
			add("FireSpin");
			add("AirSweep");
		}
	};
	private static boolean enabled = ProjectKorra.plugin.getConfig()
			.getBoolean("Abilities.Fire.FireCombo.Enabled");
	private static final double FIRE_WHEEL_STARTING_HEIGHT = 2;
	private static final double FIRE_WHEEL_RADIUS = 1;
	public static double fireticksFireWheel = ProjectKorra.plugin.getConfig()
			.getDouble("Abilities.Fire.FireCombo.FireWheel.FireTicks");
	public static double fireticksJetBlaze = ProjectKorra.plugin.getConfig()
			.getDouble("Abilities.Fire.FireCombo.JetBlaze.FireTicks");
	public static double FIRE_KICK_RANGE = ProjectKorra.plugin.getConfig()
			.getDouble("Abilities.Fire.FireCombo.FireKick.Range");
	public static double FIRE_KICK_DAMAGE = ProjectKorra.plugin.getConfig()
			.getDouble("Abilities.Fire.FireCombo.FireKick.Damage");
	public static double FIRE_SPIN_RANGE = ProjectKorra.plugin.getConfig()
			.getDouble("Abilities.Fire.FireCombo.FireSpin.Range");
	public static double FIRE_SPIN_DAMAGE = ProjectKorra.plugin.getConfig()
			.getDouble("Abilities.Fire.FireCombo.FireSpin.Damage");
	public static double FIRE_SPIN_KNOCKBACK = ProjectKorra.plugin.getConfig()
			.getDouble("Abilities.Fire.FireCombo.FireSpin.Knockback");
	public static double FIRE_WHEEL_DAMAGE = ProjectKorra.plugin.getConfig()
			.getDouble("Abilities.Fire.FireCombo.FireWheel.Damage");
	public static double FIRE_WHEEL_RANGE = ProjectKorra.plugin.getConfig()
			.getDouble("Abilities.Fire.FireCombo.FireWheel.Range");
	public static double FIRE_WHEEL_SPEED = ProjectKorra.plugin.getConfig()
			.getDouble("Abilities.Fire.FireCombo.FireWheel.Speed");
	public static double JET_BLAST_SPEED = ProjectKorra.plugin.getConfig()
			.getDouble("Abilities.Fire.FireCombo.JetBlast.Speed");
	public static double JET_BLAZE_SPEED = ProjectKorra.plugin.getConfig()
			.getDouble("Abilities.Fire.FireCombo.JetBlaze.Speed");
	public static double JET_BLAZE_DAMAGE = ProjectKorra.plugin.getConfig()
			.getDouble("Abilities.Fire.FireCombo.JetBlaze.Damage");

	public static long FIRE_KICK_COOLDOWN = ProjectKorra.plugin.getConfig()
			.getLong("Abilities.Fire.FireCombo.FireKick.Cooldown");
	public static long FIRE_SPIN_COOLDOWN = ProjectKorra.plugin.getConfig()
			.getLong("Abilities.Fire.FireCombo.FireSpin.Cooldown");
	public static long FIRE_WHEEL_COOLDOWN = ProjectKorra.plugin.getConfig()
			.getLong("Abilities.Fire.FireCombo.FireWheel.Cooldown");
	public static long JET_BLAST_COOLDOWN = ProjectKorra.plugin.getConfig()
			.getLong("Abilities.Fire.FireCombo.JetBlast.Cooldown");
	public static long JET_BLAZE_COOLDOWN = ProjectKorra.plugin.getConfig()
			.getLong("Abilities.Fire.FireCombo.JetBlaze.Cooldown");

	public static ArrayList<FireCombo> instances = new ArrayList<FireCombo>();

	private Player player;
	private BendingPlayer bplayer;
	private ClickType type;
	private String ability;

	private long time;
	private Location origin;
	private Location currentLoc;
	private Location destination;
	private Vector direction;
	private boolean firstTime = true;
	private ArrayList<LivingEntity> affectedEntities = new ArrayList<LivingEntity>();
	private ArrayList<FireComboStream> tasks = new ArrayList<FireComboStream>();
	private int progressCounter = 0;
	private double damage = 0, speed = 0, range = 0;
	private long cooldown = 0;

	public FireCombo(Player player, String ability) {
		// Dont' call Methods.canBind directly, it doesn't let you combo as fast
		if (!enabled)
			return;
		if(!GeneralMethods.getBendingPlayer(player.getName()).hasElement(Element.Fire))
			return;
		if (Commands.isToggledForAll) 
			return;
		if (GeneralMethods.isRegionProtectedFromBuild(player, "Blaze",
				player.getLocation()))
			return;
		if (!GeneralMethods.getBendingPlayer(player.getName()).isToggled()) 
			return;
		time = System.currentTimeMillis();
		this.player = player;
		this.ability = ability;
		this.bplayer = GeneralMethods.getBendingPlayer(player.getName());

		if (ChiMethods.isChiBlocked(player.getName())
				|| Bloodbending.isBloodbended(player)
				|| Paralyze.isParalyzed(player)) {
			return;
		}

		if (ability.equalsIgnoreCase("FireKick")) {
			damage = FIRE_KICK_DAMAGE;
			range = FIRE_KICK_RANGE;
			speed = 1;
			cooldown = FIRE_KICK_COOLDOWN;
		} else if (ability.equalsIgnoreCase("FireSpin")) {
			damage = FIRE_SPIN_DAMAGE;
			range = FIRE_SPIN_RANGE;
			speed = 0.3;
			cooldown = FIRE_SPIN_COOLDOWN;
		} else if (ability.equalsIgnoreCase("FireWheel")) {
			damage = FIRE_WHEEL_DAMAGE;
			range = FIRE_WHEEL_RANGE;
			speed = FIRE_WHEEL_SPEED;
			cooldown = FIRE_WHEEL_COOLDOWN;
		} else if (ability.equalsIgnoreCase("JetBlast")) {
			speed = JET_BLAST_SPEED;
			cooldown = JET_BLAST_COOLDOWN;
		} else if (ability.equalsIgnoreCase("JetBlaze")) {
			damage = JET_BLAZE_DAMAGE;
			speed = JET_BLAZE_SPEED;
			cooldown = JET_BLAZE_COOLDOWN;
		}
		if (AvatarState.isAvatarState(player)) {
			cooldown = 0;
			damage = AvatarState.getValue(damage);
			range = AvatarState.getValue(range);
		}
		instances.add(this);
	}

	public void progress() {
		progressCounter++;
		for (int i = 0; i < tasks.size(); i++) {
			BukkitRunnable br = tasks.get(i);
			if (br instanceof FireComboStream) {
				FireComboStream fs = (FireComboStream) br;
				if (fs.isCancelled())
					tasks.remove(fs);
			}
		}
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}

		if (ability.equalsIgnoreCase("FireKick")) {
			if (destination == null) {
				if (bplayer.isOnCooldown("FireKick")
						&& !AvatarState.isAvatarState(player)) {
					remove();
					return;
				}
				bplayer.addCooldown("FireKick", cooldown);
				Vector eyeDir = player.getEyeLocation().getDirection()
						.normalize().multiply(range);
				destination = player.getEyeLocation().add(eyeDir);

				player.getWorld().playSound(player.getLocation(),
						Sound.HORSE_JUMP, 0.5f, 0f);
				player.getWorld().playSound(player.getLocation(), Sound.FIZZ,
						0.5f, 1f);
				for (int i = -30; i <= 30; i += 5) {
					Vector vec = GeneralMethods.getDirection(player.getLocation(),
							destination.clone());
					vec = GeneralMethods.rotateXZ(vec, i);

					FireComboStream fs = new FireComboStream(this, vec,
							player.getLocation(), range, speed);
					fs.setSpread(0.2F);
					fs.setDensity(5);
					fs.setUseNewParticles(true);
					if (tasks.size() % 3 != 0)
						fs.setCollides(false);
					fs.runTaskTimer(ProjectKorra.plugin, 0, 1L);
					tasks.add(fs);
					player.getWorld().playSound(player.getLocation(),
							Sound.FIRE_IGNITE, 0.5f, 1f);
				}
				currentLoc = ((FireComboStream) tasks.get(0)).getLocation();
				for (FireComboStream stream : tasks)
					if (GeneralMethods.blockAbilities(player, abilitiesToBlock,
							stream.currentLoc, 2))
						stream.remove();
			} else if (tasks.size() == 0) {
				remove();
				return;
			}
		} else if (ability.equalsIgnoreCase("FireSpin")) {
			if (destination == null) {
				if (bplayer.isOnCooldown("FireSpin")
						&& !AvatarState.isAvatarState(player)) {
					remove();
					return;
				}
				bplayer.addCooldown("FireSpin", cooldown);
				destination = player.getEyeLocation().add(range, 0, range);
				player.getWorld().playSound(player.getLocation(), Sound.FIZZ,
						0.5f, 0.5f);

				for (int i = 0; i <= 360; i += 5) {
					Vector vec = GeneralMethods.getDirection(player.getLocation(),
							destination.clone());
					vec = GeneralMethods.rotateXZ(vec, i - 180);
					vec.setY(0);

					FireComboStream fs = new FireComboStream(this, vec, player
							.getLocation().clone().add(0, 1, 0), range, speed);
					fs.setSpread(0.0F);
					fs.setDensity(1);
					fs.setUseNewParticles(true);
					if (tasks.size() % 10 != 0)
						fs.setCollides(false);
					fs.runTaskTimer(ProjectKorra.plugin, 0, 1L);
					tasks.add(fs);
				}
			}
			if (tasks.size() == 0) {
				remove();
				return;
			}
			for (FireComboStream stream : tasks) {
				if (FireMethods.isWithinFireShield(stream.getLocation()))
					stream.remove();
				if (AirMethods.isWithinAirShield(stream.getLocation()))
					stream.remove();
			}
		} else if (ability.equalsIgnoreCase("JetBlast")) {
			if (System.currentTimeMillis() - time > 5000) {
				remove();
				return;
			} else if (FireJet.instances.containsKey(player)) {
				if (firstTime) {
					if (bplayer.isOnCooldown("JetBlast")
							&& !AvatarState.isAvatarState(player)) {
						remove();
						return;
					}
					bplayer.addCooldown("JetBlast", cooldown);
					firstTime = false;
					float spread = 0F;
					ParticleEffect.LARGE_EXPLODE.display(player.getLocation(),
							spread, spread, spread, 0, 1);
					player.getWorld().playSound(player.getLocation(),
							Sound.EXPLODE, 15, 0F);
				}
				player.setVelocity(player.getVelocity().normalize()
						.multiply(speed));

				FireComboStream fs = new FireComboStream(this, player
						.getVelocity().clone().multiply(-1),
						player.getLocation(), 3, 0.5);
				fs.setDensity(1);
				fs.setSpread(0.9F);
				fs.setUseNewParticles(true);
				fs.setCollides(false);
				fs.runTaskTimer(ProjectKorra.plugin, 0, 1L);
				tasks.add(fs);
			}
		}

		else if (ability.equalsIgnoreCase("JetBlaze")) {
			if (firstTime) {
				if (bplayer.isOnCooldown("JetBlaze")
						&& !AvatarState.isAvatarState(player)) {
					remove();
					return;
				}
				bplayer.addCooldown("JetBlaze", cooldown);
				firstTime = false;
			} else if (System.currentTimeMillis() - time > 5000) {
				remove();
				return;
			} else if (FireJet.instances.containsKey(player)) {
				direction = player.getVelocity().clone().multiply(-1);
				player.setVelocity(player.getVelocity().normalize()
						.multiply(speed));

				FireComboStream fs = new FireComboStream(this, direction,
						player.getLocation(), 5, 1);
				fs.setDensity(8);
				fs.setSpread(1.0F);
				fs.setUseNewParticles(true);
				fs.setCollisionRadius(3);
				fs.setParticleEffect(ParticleEffect.LARGE_SMOKE);
				if (progressCounter % 5 != 0)
					fs.setCollides(false);
				fs.runTaskTimer(ProjectKorra.plugin, 0, 1L);
				tasks.add(fs);
				if (progressCounter % 4 == 0)
					player.getWorld().playSound(player.getLocation(),
							Sound.FIZZ, 1, 0F);
			}
		} else if (ability.equalsIgnoreCase("FireWheel")) {
			if (currentLoc == null) {
				if (bplayer.isOnCooldown("FireWheel")
						&& !AvatarState.isAvatarState(player)) {
					remove();
					return;
				}
				bplayer.addCooldown("FireWheel", cooldown);
				origin = player.getLocation();
				
				if (GeneralMethods.getTopBlock(player.getLocation(), 3, 3) == null) {
					remove();
					return;
				}
				
				currentLoc = player.getLocation();
				direction = player.getEyeLocation().getDirection().clone()
						.normalize();
				direction.setY(0);
			} else if (currentLoc.distance(origin) > range) {
				remove();
				return;
			}

			Block topBlock = GeneralMethods.getTopBlock(currentLoc, 2, -4);
			if (topBlock == null
					|| (WaterMethods.isWaterbendable(topBlock, player) && !WaterMethods
							.isPlant(topBlock))) {
				remove();
				return;
			}
			if (topBlock.getType() == Material.FIRE
					|| WaterMethods.isPlant(topBlock))
				topBlock = topBlock.getLocation().add(0, -1, 0).getBlock();
			currentLoc.setY(topBlock.getY() + FIRE_WHEEL_STARTING_HEIGHT);

			FireComboStream fs = new FireComboStream(this, direction,
					currentLoc.clone().add(0, -1, 0), 5, 1);
			fs.setDensity(0);
			fs.setSinglePoint(true);
			fs.setCollisionRadius(1.5);
			fs.setCollides(true);
			fs.runTaskTimer(ProjectKorra.plugin, 0, 1L);
			tasks.add(fs);

			for (double i = -180; i <= 180; i += 3) {
				Location tempLoc = currentLoc.clone();
				Vector newDir = direction.clone().multiply(
						FIRE_WHEEL_RADIUS * Math.cos(Math.toRadians(i)));
				tempLoc.add(newDir);
				tempLoc.setY(tempLoc.getY()
						+ (FIRE_WHEEL_RADIUS * Math.sin(Math.toRadians(i))));
				ParticleEffect.FLAME.display(tempLoc, 0, 0, 0, 0, 1);
			}

			currentLoc = currentLoc.add(direction.clone().multiply(speed));
			currentLoc.getWorld().playSound(currentLoc, Sound.FIRE, 1, 1);
			if (GeneralMethods.blockAbilities(player, abilitiesToBlock, currentLoc, 2)) {
				remove();
				return;
			}
			;
		}

		if (progressCounter % 3 == 0)
			checkSafeZone();
	}

	public void checkSafeZone() {
		if (currentLoc != null
				&& GeneralMethods.isRegionProtectedFromBuild(player, "Blaze",
						currentLoc))
			remove();
	}

	public void collision(LivingEntity entity, Vector direction,
			FireComboStream fstream) {
		if (GeneralMethods.isRegionProtectedFromBuild(player, "Blaze",
				entity.getLocation()))
			return;
		entity.getLocation()
				.getWorld()
				.playSound(entity.getLocation(), Sound.VILLAGER_HIT, 0.3f, 0.3f);

		if (ability.equalsIgnoreCase("FireKick")) {
			GeneralMethods.damageEntity(player, entity, damage);
			fstream.remove();
		} else if (ability.equalsIgnoreCase("FireSpin")) {
			double knockback = AvatarState.isAvatarState(player) ? FIRE_SPIN_KNOCKBACK + 0.5
					: FIRE_SPIN_KNOCKBACK;
			GeneralMethods.damageEntity(player, entity, damage);
			entity.setVelocity(direction.normalize().multiply(knockback));
			fstream.remove();
		} else if (ability.equalsIgnoreCase("JetBlaze")) {
			if (!affectedEntities.contains(entity)) {
				affectedEntities.add(entity);
				GeneralMethods.damageEntity(player, entity, damage);
				entity.setFireTicks((int) (fireticksJetBlaze * 20));
			}
		} else if (ability.equalsIgnoreCase("FireWheel")) {
			if (!affectedEntities.contains(entity)) {
				affectedEntities.add(entity);
				GeneralMethods.damageEntity(player, entity, damage);
				entity.setFireTicks((int) (fireticksFireWheel * 20));
				this.remove();
			}
		}
	}

	public void remove() {
		/**
		 * Removes this instance of FireCombo, cleans up any blocks that are
		 * remaining in totalBlocks, and cancels any remaining tasks.
		 */
		instances.remove(this);
		for (BukkitRunnable task : tasks)
			task.cancel();
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

	public static ArrayList<FireCombo> getFireCombo(Player player) {
		/**
		 * Returns all the FireCombos created by a specific player.
		 */
		ArrayList<FireCombo> list = new ArrayList<FireCombo>();
		for (FireCombo lf : instances)
			if (lf.player != null && lf.player == player)
				list.add(lf);
		return list;
	}

	public static ArrayList<FireCombo> getFireCombo(Player player,
			ClickType type) {
		/**
		 * Returns all of the FireCombos created by a specific player but
		 * filters the abilities based on shift or click.
		 */
		ArrayList<FireCombo> list = new ArrayList<FireCombo>();
		for (FireCombo lf : instances)
			if (lf.player != null && lf.player == player && lf.type != null
					&& lf.type == type)
				list.add(lf);
		return list;
	}

	public static boolean removeAroundPoint(Player player, String ability,
			Location loc, double radius) {
		boolean removed = false;
		for (int i = 0; i < instances.size(); i++) {
			FireCombo combo = instances.get(i);
			if (combo.getPlayer().equals(player))
				continue;

			if (ability.equalsIgnoreCase("FireKick")
					&& combo.ability.equalsIgnoreCase("FireKick")) {
				for (FireComboStream fs : combo.tasks) {
					if (fs.getLocation() != null && fs.getLocation().getWorld() == loc.getWorld()
							&& Math.abs(fs.getLocation().distance(loc)) <= radius) {
						fs.remove();
						removed = true;
					}
				}
			}

			else if (ability.equalsIgnoreCase("FireSpin")
					&& combo.ability.equalsIgnoreCase("FireSpin")) {
				for (FireComboStream fs : combo.tasks) {
					if (fs.getLocation() != null
							&& Math.abs(fs.getLocation().distance(loc)) <= radius) {
						fs.remove();
						removed = true;
					}
				}
			}

			else if (ability.equalsIgnoreCase("FireWheel")
					&& combo.ability.equalsIgnoreCase("FireWheel")) {
				if (combo.currentLoc != null
						&& Math.abs(combo.currentLoc.distance(loc)) <= radius) {
					instances.remove(combo);
					removed = true;
				}
			}
		}
		return removed;
	}

	public static class FireComboStream extends BukkitRunnable {
		private Vector direction;
		private double speed;
		private Location initialLoc, currentLoc;
		private double distance;

		ParticleEffect particleEffect = ParticleEffect.FLAME;
		private FireCombo fireCombo;
		private float spread = 0;
		private int density = 1;
		private boolean useNewParticles = false;
		private boolean cancelled = false;
		private boolean collides = true;
		private boolean singlePoint = false;
		private double collisionRadius = 2;
		private int checkCollisionDelay = 1;
		private int checkCollisionCounter = 0;

		public FireComboStream(FireCombo fireCombo, Vector direction,
				Location loc, double distance, double speed) {
			this.fireCombo = fireCombo;
			this.direction = direction;
			this.speed = speed;
			this.initialLoc = loc.clone();
			this.currentLoc = loc.clone();
			this.distance = distance;
		}

		public void run() {
			Block block = currentLoc.getBlock();
			if (block.getRelative(BlockFace.UP).getType() != Material.AIR
					&& !WaterMethods.isPlant(block)) {
				remove();
				return;
			}
			for (int i = 0; i < density; i++) {
				if (useNewParticles)
					particleEffect.display(currentLoc, spread, spread, spread,
							0, 1);
				else
					currentLoc.getWorld().playEffect(currentLoc,
							Effect.MOBSPAWNER_FLAMES, 0, 15);
			}

			currentLoc.add(direction.normalize().multiply(speed));
			if (initialLoc.distance(currentLoc) > distance) {
				remove();
				return;
			} else if (collides
					&& checkCollisionCounter % checkCollisionDelay == 0) {
				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(currentLoc,
						collisionRadius)) {
					if (entity instanceof LivingEntity
							&& !entity.equals(fireCombo.getPlayer()))
						fireCombo.collision((LivingEntity) entity, direction,
								this);
				}
			}
			checkCollisionCounter++;
			if (singlePoint)
				remove();
		}

		public void cancel() {
			remove();
		}

		public void remove() {
			super.cancel();
			this.cancelled = true;
		}

		public Location getLocation() {
			return this.currentLoc;
		}

		public Vector getDirection() {
			return this.direction.clone();
		}

		public void setSpread(float spread) {
			this.spread = spread;
		}

		public void setUseNewParticles(boolean b) {
			useNewParticles = b;
		}

		public void setDensity(int density) {
			this.density = density;
		}

		public boolean isCancelled() {
			return cancelled;
		}

		public void setParticleEffect(ParticleEffect effect) {
			this.particleEffect = effect;
		}

		public void setCollides(boolean b) {
			this.collides = b;
		}

		public void setCollisionRadius(double radius) {
			this.collisionRadius = radius;
		}

		public void setSinglePoint(boolean b) {
			this.singlePoint = b;
		}

		public void setCheckCollisionDelay(int delay) {
			this.checkCollisionDelay = delay;
		}
	}

}
