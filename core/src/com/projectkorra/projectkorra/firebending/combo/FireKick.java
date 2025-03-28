package com.projectkorra.projectkorra.firebending.combo;

import java.util.ArrayList;
import java.util.List;

import com.projectkorra.projectkorra.ability.util.ComboUtil;
import com.projectkorra.projectkorra.attribute.markers.DayNightFactor;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.ClickType;

public class FireKick extends FireAbility implements ComboAbility {

	@Attribute(Attribute.COOLDOWN) @DayNightFactor(invert = true)
	private long cooldown;
	@Attribute(Attribute.DAMAGE) @DayNightFactor
	private double damage;
	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute(Attribute.RANGE) @DayNightFactor
	private double range;
	private Location location;
	private Location destination;
	private ArrayList<LivingEntity> affectedEntities;
	private ArrayList<BukkitRunnable> tasks;

	public FireKick(final Player player) {
		super(player);

		if (!this.bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		}

		this.affectedEntities = new ArrayList<>();
		this.tasks = new ArrayList<>();

		this.damage = getConfig().getDouble("Abilities.Fire.FireKick.Damage");
		this.range = getConfig().getDouble("Abilities.Fire.FireKick.Range");
		this.cooldown = getConfig().getLong("Abilities.Fire.FireKick.Cooldown");
		this.speed = getConfig().getLong("Abilities.Fire.FireKick.Speed");

		this.start();
	}

	@Override
	public String getName() {
		return "FireKick";
	}

	@Override
	public boolean isCollidable() {
		return true;
	}

	@Override
	public void progress() {
		for (int i = 0; i < this.tasks.size(); i++) {
			final BukkitRunnable br = this.tasks.get(i);
			if (br instanceof FireComboStream) {
				final FireComboStream fs = (FireComboStream) br;
				if (fs.isCancelled()) {
					this.tasks.remove(fs);
				}
			}
		}

		if (!this.bPlayer.canBendIgnoreBindsCooldowns(this)) {
			this.remove();
			return;
		}

		if (this.destination == null) {
			if (this.bPlayer.isOnCooldown("FireKick") && !this.bPlayer.isAvatarState()) {
				this.remove();
				return;
			}

			this.bPlayer.addCooldown("FireKick", this.cooldown);
			final Vector eyeDir = this.player.getEyeLocation().getDirection().normalize().multiply(this.range);
			this.destination = this.player.getEyeLocation().add(eyeDir);

			this.player.getWorld().playSound(this.player.getLocation(), Sound.ENTITY_HORSE_JUMP, 0.5f, 0f);
			this.player.getWorld().playSound(this.player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 0.5f, 1f);
			for (int i = -30; i <= 30; i += 5) {
				double angle = Math.toRadians(i);
				final Vector direction = this.player.getEyeLocation().getDirection().clone();
				Vector xz = GeneralMethods.rotateVectorAroundVector(direction, new Vector(-direction.getZ(), 0, direction.getX()).normalize(), 0);
				Vector vec = direction.clone().multiply(Math.cos(angle))
						.add(xz.clone().multiply(Math.sin(angle))).normalize();

				final FireComboStream fs = new FireComboStream(this.player, this, vec, this.player.getLocation(), this.range, this.speed);
				fs.setSpread(0.2F);
				fs.setDensity(5);
				fs.setUseNewParticles(true);
				fs.setDamage(this.damage);
				if (this.tasks.size() % 3 != 0) {
					fs.setCollides(false);
				}
				fs.runTaskTimer(ProjectKorra.plugin, 0, 1L);
				this.tasks.add(fs);
				this.player.getWorld().playSound(this.player.getLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 0.5f, 1f);
			}
		} else if (this.tasks.size() == 0) {
			this.remove();
			return;
		}

	}

	@Override
	public void remove() {
		super.remove();
		for (final BukkitRunnable task : this.tasks) {
			task.cancel();
		}
	}

	@Override
	public void handleCollision(final Collision collision) {
		if (collision.isRemovingFirst()) {
			final ArrayList<BukkitRunnable> newTasks = new ArrayList<>();
			final double collisionDistanceSquared = Math.pow(this.getCollisionRadius() + collision.getAbilitySecond().getCollisionRadius(), 2);
			// Remove all of the streams that are by this specific ourLocation.
			// Don't just do a single stream at a time or this algorithm becomes O(n^2) with Collision's detection algorithm.
			for (final BukkitRunnable task : this.getTasks()) {
				if (task instanceof FireComboStream) {
					final FireComboStream stream = (FireComboStream) task;
					if (stream.getLocation().distanceSquared(collision.getLocationSecond()) > collisionDistanceSquared) {
						newTasks.add(stream);
					} else {
						stream.cancel();
					}
				} else {
					newTasks.add(task);
				}
			}
			this.setTasks(newTasks);
		}
	}

	@Override
	public List<Location> getLocations() {
		final ArrayList<Location> locations = new ArrayList<>();
		for (final BukkitRunnable task : this.getTasks()) {
			if (task instanceof FireComboStream) {
				final FireComboStream stream = (FireComboStream) task;
				locations.add(stream.getLocation());
			}
		}
		return locations;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public long getCooldown() {
		return this.cooldown;
	}

	@Override
	public Location getLocation() {
		return this.location;
	}

	@Override
	public Object createNewComboInstance(final Player player) {
		return new FireKick(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		return ComboUtil.generateCombinationFromList(this, ConfigManager.defaultConfig.get().getStringList("Abilities.Fire.FireKick.Combination"));
	}

	public ArrayList<LivingEntity> getAffectedEntities() {
		return this.affectedEntities;
	}

	public ArrayList<BukkitRunnable> getTasks() {
		return this.tasks;
	}

	public void setTasks(final ArrayList<BukkitRunnable> tasks) {
		this.tasks = tasks;
	}
}
