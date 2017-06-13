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

public class FireKick extends FireAbility implements ComboAbility {

	private long cooldown;
	private double damage;
	private double speed;
	private double range;
	private Location location;
	private Location destination;
	private ArrayList<LivingEntity> affectedEntities;
	private ArrayList<BukkitRunnable> tasks;
	
	public FireKick(Player player) {
		super(player);

		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		}

		this.affectedEntities = new ArrayList<>();
		this.tasks = new ArrayList<>();
			
		this.damage = getConfig().getDouble("Abilities.Fire.FireCombo.FireKick.Damage");
		this.range = getConfig().getDouble("Abilities.Fire.FireCombo.FireKick.Range");
		this.cooldown = getConfig().getLong("Abilities.Fire.FireCombo.FireKick.Cooldown");
		this.speed = getConfig().getLong("Abilities.Fire.FireCombo.FireKick.Speed");;
			
		if (bPlayer.isAvatarState()) {
			this.cooldown = 0;
			this.damage = AvatarState.getValue(damage);
			this.range = AvatarState.getValue(range);
		}

		start();
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
			if (bPlayer.isOnCooldown("FireKick") && !bPlayer.isAvatarState()) {
				remove();
				return;
			}

			bPlayer.addCooldown("FireKick", cooldown);
			Vector eyeDir = player.getEyeLocation().getDirection().normalize().multiply(range);
			destination = player.getEyeLocation().add(eyeDir);

			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_HORSE_JUMP, 0.5f, 0f);
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 0.5f, 1f);
			for (int i = -30; i <= 30; i += 5) {
				Vector vec = GeneralMethods.getDirection(player.getLocation(), destination.clone());
				vec = GeneralMethods.rotateXZ(vec, i);

				FireComboStream fs = new FireComboStream(player, this, vec, player.getLocation(), range, speed);
				fs.setSpread(0.2F);
				fs.setDensity(5);
				fs.setUseNewParticles(true);
				fs.setDamage(damage);
				if (tasks.size() % 3 != 0) {
					fs.setCollides(false);
				}
				fs.runTaskTimer(ProjectKorra.plugin, 0, 1L);
				tasks.add(fs);
				player.getWorld().playSound(player.getLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 0.5f, 1f);
			}
		} else if (tasks.size() == 0) {
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
	public boolean isSneakAbility() {
		return true;
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
	public Location getLocation() {
		return location;
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return null;
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		return null;
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