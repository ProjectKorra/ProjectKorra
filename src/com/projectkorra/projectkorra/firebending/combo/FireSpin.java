package com.projectkorra.projectkorra.firebending.combo;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
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

public class FireSpin extends FireAbility implements ComboAbility {

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute(Attribute.KNOCKBACK)
	private double knockback;
	private Location destination;
	private ArrayList<LivingEntity> affectedEntities;
	private ArrayList<BukkitRunnable> tasks;

	public FireSpin(final Player player) {
		super(player);

		if (!this.bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		}

		if (player.getLocation().getBlock().getType() == Material.WATER || player.getLocation().getBlock().getType() == Material.STATIONARY_WATER) {
			return;
		}

		this.affectedEntities = new ArrayList<>();
		this.tasks = new ArrayList<>();

		this.damage = getConfig().getDouble("Abilities.Fire.FireSpin.Damage");
		this.range = getConfig().getDouble("Abilities.Fire.FireSpin.Range");
		this.cooldown = getConfig().getLong("Abilities.Fire.FireSpin.Cooldown");
		this.knockback = getConfig().getDouble("Abilities.Fire.FireSpin.Knockback");
		this.speed = getConfig().getDouble("Abilities.Fire.FireSpin.Speed");

		if (this.bPlayer.isAvatarState()) {
			this.cooldown = 0;
			this.damage = getConfig().getDouble("Abilities.Avatar.AvatarState.Fire.FireSpin.Damage");
			this.range = getConfig().getDouble("Abilities.Avatar.AvatarState.Fire.FireSpin.Range");
			this.knockback = getConfig().getDouble("Abilities.Avatar.AvatarState.Fire.FireSpin.Knockback");
		}

		this.start();
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

				final FireComboStream fs = new FireComboStream(this.player, this, vec, this.player.getLocation().clone().add(0, 1, 0), this.range, this.speed);
				fs.setSpread(0.0F);
				fs.setDensity(1);
				fs.setUseNewParticles(true);
				fs.setDamage(this.damage);
				fs.setKnockback(this.knockback);
				if (this.tasks.size() % 10 != 0) {
					fs.setCollides(false);
				}
				fs.runTaskTimer(ProjectKorra.plugin, 0, 1L);
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
	public Object createNewComboInstance(final Player player) {
		return new FireSpin(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		final ArrayList<AbilityInformation> fireSpin = new ArrayList<>();
		fireSpin.add(new AbilityInformation("FireBlast", ClickType.LEFT_CLICK));
		fireSpin.add(new AbilityInformation("FireBlast", ClickType.LEFT_CLICK));
		fireSpin.add(new AbilityInformation("FireShield", ClickType.LEFT_CLICK));
		fireSpin.add(new AbilityInformation("FireShield", ClickType.SHIFT_DOWN));
		fireSpin.add(new AbilityInformation("FireShield", ClickType.SHIFT_UP));
		return fireSpin;
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

	public ArrayList<BukkitRunnable> getTasks() {
		return this.tasks;
	}

	public void setTasks(final ArrayList<BukkitRunnable> tasks) {
		this.tasks = tasks;
	}
}
