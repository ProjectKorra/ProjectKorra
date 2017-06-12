package com.projectkorra.projectkorra.firebending.combo;

import java.util.ArrayList;
import java.util.List;

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
import com.projectkorra.projectkorra.avatar.AvatarState;

public class FireSpin extends FireAbility implements ComboAbility {

	private long cooldown;
	private double damage;
	private double speed;
	private double range;
	private double knockback;
	private Location destination;
	private ArrayList<LivingEntity> affectedEntities;
	private ArrayList<BukkitRunnable> tasks;
	
	public FireSpin(Player player) {
		super(player);

		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		}

		this.affectedEntities = new ArrayList<>();
		this.tasks = new ArrayList<>();

		this.damage = getConfig().getDouble("Abilities.Fire.FireCombo.FireSpin.Damage");
		this.range = getConfig().getDouble("Abilities.Fire.FireCombo.FireSpin.Range");
		this.cooldown = getConfig().getLong("Abilities.Fire.FireCombo.FireSpin.Cooldown");
		this.knockback = getConfig().getDouble("Abilities.Fire.FireCombo.FireSpin.Knockback");
		this.speed = getConfig().getDouble("Abilities.Fire.FireCombo.FireSpin.Speed");

		if (bPlayer.isAvatarState()) {
			this.cooldown = 0;
			this.damage = AvatarState.getValue(damage);
			this.range = AvatarState.getValue(range);
		}
		
		start();
	}

	@Override
	public void progress() {
		for (int i = 0; i < tasks.size(); i++) {
			BukkitRunnable br = tasks.get(i);
			if (br instanceof FireComboStream) {
				FireComboStream fs = (FireComboStream) br;
				if (fs.isCancelled()) {
					tasks.remove(fs);
				}
			}
		}

		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			remove();
			return;
		}
		
		if (destination == null) {
			if (bPlayer.isOnCooldown("FireSpin") && !bPlayer.isAvatarState()) {
				remove();
				return;
			}
			bPlayer.addCooldown("FireSpin", cooldown);
			destination = player.getEyeLocation().add(range, 0, range);
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 0.5f, 0.5f);

			for (int i = 0; i <= 360; i += 5) {
				Vector vec = GeneralMethods.getDirection(player.getLocation(), destination.clone());
				vec = GeneralMethods.rotateXZ(vec, i - 180);
				vec.setY(0);

				FireComboStream fs = new FireComboStream(player, this, vec, player.getLocation().clone().add(0, 1, 0), range, speed);
				fs.setSpread(0.0F);
				fs.setDensity(1);
				fs.setUseNewParticles(true);
				fs.setDamage(damage);
				fs.setKnockback(knockback);
				if (tasks.size() % 10 != 0) {
					fs.setCollides(false);
				}
				fs.runTaskTimer(ProjectKorra.plugin, 0, 1L);
				tasks.add(fs);
			}
		}

		if (tasks.size() == 0) {
			remove();
			return;
		}
	}
	
	@Override
	public void remove() {
		super.remove();
		for (BukkitRunnable task : tasks) {
			task.cancel();
		}
	}
	
	@Override
	public void handleCollision(Collision collision) {
		if (collision.isRemovingFirst()) {
			ArrayList<BukkitRunnable> newTasks = new ArrayList<>();
			double collisionDistanceSquared = Math.pow(getCollisionRadius() + collision.getAbilitySecond().getCollisionRadius(), 2);
			// Remove all of the streams that are by this specific ourLocation.
			// Don't just do a single stream at a time or this algorithm becomes O(n^2) with
			// Collision's detection algorithm.
			for (BukkitRunnable task : getTasks()) {
				if (task instanceof FireComboStream) {
					FireComboStream stream = (FireComboStream) task;
					if (stream.getLocation().distanceSquared(collision.getLocationSecond()) > collisionDistanceSquared) {
						newTasks.add(stream);
					} else {
						stream.cancel();
					}
				} else {
					newTasks.add(task);
				}
			}
			setTasks(newTasks);
		}
	}

	@Override
	public List<Location> getLocations() {
		ArrayList<Location> locations = new ArrayList<>();
		for (BukkitRunnable task : getTasks()) {
			if (task instanceof FireComboStream) {
				FireComboStream stream = (FireComboStream) task;
				locations.add(stream.getLocation());
			}
		}
		return locations;
	}
	
	@Override
	public Object createNewComboInstance(Player player) {
		return null;
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		return null;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public String getName() {
		return "FireSpin";
	}

	@Override
	public Location getLocation() {
		return player.getLocation();
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	public ArrayList<LivingEntity> getAffectedEntities() {
		return affectedEntities;
	}
	
	public ArrayList<BukkitRunnable> getTasks() {
		return tasks;
	}

	public void setTasks(ArrayList<BukkitRunnable> tasks) {
		this.tasks = tasks;
	}
}
