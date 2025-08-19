package com.projectkorra.projectkorra.firebending.combo;

import java.util.ArrayList;
import java.util.List;

import com.projectkorra.projectkorra.ability.util.ComboUtil;
import com.projectkorra.projectkorra.attribute.markers.DayNightFactor;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;

public class FireSpin extends FireAbility implements ComboAbility {

	@Attribute(Attribute.COOLDOWN) @DayNightFactor(invert = true)
	private long cooldown;
	@Attribute(Attribute.DAMAGE) @DayNightFactor
	private double damage;
	@Attribute(Attribute.SPEED) @DayNightFactor
	private double speed;
	@Attribute(Attribute.RANGE) @DayNightFactor
	private double range;
	@Attribute(Attribute.KNOCKBACK) @DayNightFactor
	private double knockback;
	private Location destination;
	private ArrayList<LivingEntity> affectedEntities;
	private ArrayList<ComboStream> tasks;

	public FireSpin(final Player player) {
		super(player);

		if (!this.bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		}

		if (player.getLocation().getBlock().getType() == Material.WATER) {
			return;
		}

		this.affectedEntities = new ArrayList<>();
		this.tasks = new ArrayList<>();

		this.damage = getConfig().getDouble("Abilities.Fire.FireSpin.Damage");
		this.range = getConfig().getDouble("Abilities.Fire.FireSpin.Range");
		this.cooldown = getConfig().getLong("Abilities.Fire.FireSpin.Cooldown");
		this.knockback = getConfig().getDouble("Abilities.Fire.FireSpin.Knockback");
		this.speed = getConfig().getDouble("Abilities.Fire.FireSpin.Speed");

		this.start();
	}

	@Override
	public void progress() {
		for (int i = 0; i < this.tasks.size(); i++) {
			final ComboStream br = this.tasks.get(i);
			if (br instanceof ComboStream) {
				final ComboStream fs = (ComboStream) br;
				if (fs.isRemoved()) {
					this.tasks.remove(fs);
				}
			}
		}

		if (!this.bPlayer.canBendIgnoreBindsCooldowns(this)) {
			this.remove();
			return;
		}

		if (this.destination == null) {
			if (this.bPlayer.isOnCooldown("FireSpin") && !this.bPlayer.isAvatarState()) {
				this.remove();
				return;
			}
			this.bPlayer.addCooldown("FireSpin", this.cooldown);
			this.destination = this.player.getEyeLocation().add(this.range, 0, this.range);
			this.player.getWorld().playSound(this.player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 0.5f, 0.5f);

			for (int i = 0; i <= 360; i += 5) {
				Vector vec = GeneralMethods.getDirection(this.player.getLocation(), this.destination.clone());
				vec = GeneralMethods.rotateXZ(vec, i - 180);
				vec.setY(0);

				final ComboStream fs = new ComboStream(this.player, this, vec, this.player.getLocation().clone().add(0, 1, 0), this.range, this.speed);
				fs.setSpread(0.0F);
				fs.setDensity(1);
				fs.setUseNewParticles(true);
				fs.setDamage(this.damage);
				fs.setKnockback(this.knockback);
				if (this.tasks.size() % 10 != 0) {
					fs.setCollides(false);
				}
				fs.start();
				this.tasks.add(fs);
			}
		}

		if (this.tasks.size() == 0) {
			this.remove();
			return;
		}
	}

	@Override
	public void remove() {
		super.remove();
		for (final ComboStream task : this.tasks) {
			task.cancel();
		}
	}

	@Override
	public void handleCollision(final Collision collision) {
		if (collision.isRemovingFirst()) {
			final ArrayList<ComboStream> newTasks = new ArrayList<>();
			final double collisionDistanceSquared = Math.pow(this.getCollisionRadius() + collision.getAbilitySecond().getCollisionRadius(), 2);
			// Remove all of the streams that are by this specific ourLocation.
			// Don't just do a single stream at a time or this algorithm becomes O(n^2) with Collision's detection algorithm.
			for (final ComboStream task : this.getTasks()) {
				if (task instanceof ComboStream) {
					final ComboStream stream = (ComboStream) task;
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
		for (final ComboStream task : this.getTasks()) {
			if (task instanceof ComboStream) {
				final ComboStream stream = (ComboStream) task;
				locations.add(stream.getLocation());
			}
		}
		return locations;
	}

	@Override
	public Object createNewComboInstance(final Player player) {
		return new FireSpin(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		return ComboUtil.generateCombinationFromList(this, ConfigManager.defaultConfig.get().getStringList("Abilities.Fire.FireSpin.Combination"));
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public long getCooldown() {
		return this.cooldown;
	}

	@Override
	public String getName() {
		return "FireSpin";
	}

	@Override
	public Location getLocation() {
		return this.player.getLocation();
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	public ArrayList<LivingEntity> getAffectedEntities() {
		return this.affectedEntities;
	}

	public ArrayList<ComboStream> getTasks() {
		return this.tasks;
	}

	public void setTasks(final ArrayList<ComboStream> tasks) {
		this.tasks = tasks;
	}
}
