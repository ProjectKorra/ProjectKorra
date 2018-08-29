package com.projectkorra.projectkorra.airbending.flight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.FlightAbility;
import com.projectkorra.projectkorra.ability.MultiAbility;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager.MultiAbilityInfoSub;
import com.projectkorra.projectkorra.airbending.AirScooter;
import com.projectkorra.projectkorra.airbending.AirSpout;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.firebending.FireJet;
import com.projectkorra.projectkorra.util.ActionBar;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.MovementHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.waterbending.WaterSpout;

public class FlightMultiAbility extends FlightAbility implements MultiAbility {

	public static final String ID = "FlightMultiAbility";
	public static Map<UUID, UUID> requestedMap = new HashMap<>();
	public static Map<UUID, Long> requestTime = new HashMap<>();

	private static Set<UUID> flying = new HashSet<>();
	private boolean hadGlide;

	private static enum FlightMode {
		SOAR, GLIDE, LEVITATE, ENDING;
	}

	private double speed = 1;
	private double slowSpeed;
	private double fastSpeed;
	private double multiplier;
	@Attribute(Attribute.SPEED)
	private double baseSpeed;
	private FlightMode mode = FlightMode.SOAR;
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	private Vector prevDir;

	public FlightMultiAbility(final Player player) {
		super(player);

		if (this.bPlayer.isOnCooldown(this)) {
			return;
		}

		final FlightMultiAbility f = getAbility(player, FlightMultiAbility.class);
		if (f != null) {
			if (player.isSneaking()) {
				player.eject();
				return;
			}

			switch (player.getInventory().getHeldItemSlot()) {
				case 0:
					f.manageSoarSpeed();
					break;
				case 3:
					f.remove();
					break;
			}
			return;
		}

		if (player.isInsideVehicle()) {
			return;
		}

		if (isWater(player.getEyeLocation().getBlock())) {
			return;
		}

		if (player.isOnGround()) {
			return;
		}

		CoreAbility abil = null;
		if (hasAbility(player, AirSpout.class)) {
			abil = getAbility(player, AirSpout.class);
		} else if (hasAbility(player, WaterSpout.class)) {
			abil = getAbility(player, WaterSpout.class);
		} else if (hasAbility(player, FireJet.class)) {
			abil = getAbility(player, FireJet.class);
		} else if (hasAbility(player, AirScooter.class)) {
			abil = getAbility(player, AirScooter.class);
		}

		if (abil != null) {
			abil.remove();
		}

		MultiAbilityManager.bindMultiAbility(player, "Flight");
		flightHandler.createInstance(player, ID);
		this.hadGlide = player.isGliding();
		flying.add(player.getUniqueId());
		this.prevDir = player.getEyeLocation().getDirection().clone();
		this.duration = getConfig().getLong("Abilities.Air.Flight.Duration");
		this.cooldown = getConfig().getLong("Abilities.Air.Flight.Cooldown");
		this.baseSpeed = getConfig().getDouble("Abilities.Air.Flight.BaseSpeed");
		
		this.speed = 1;
		this.slowSpeed = this.baseSpeed / 2;
		this.fastSpeed = this.baseSpeed * 2;
		this.multiplier = this.baseSpeed;
		this.start();
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return this.player == null ? null : this.player.getLocation();
	}

	@Override
	public String getName() {
		return "Flight";
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public void progress() {
		if (!this.player.isOnline() || this.player.isDead()) {
			this.remove();
			return;
		}

		if (this.duration > 0) {
			if (System.currentTimeMillis() >= this.duration + this.getStartTime()) {
				this.remove();
				return;
			}
		}

		if (requestedMap.containsKey(this.player.getUniqueId())) {
			final Player p2 = Bukkit.getPlayer(requestedMap.get(this.player.getUniqueId()));
			if (p2 == null) {
				requestedMap.remove(this.player.getUniqueId());
				requestTime.remove(this.player.getUniqueId());
				this.player.sendMessage(ChatColor.RED + "Requested player no longer found, cancelling request!");
			} else {
				if (requestTime.get(this.player.getUniqueId()) + 15000 > System.currentTimeMillis()) {
					final long start = System.currentTimeMillis();
					new BukkitRunnable() {
						@Override
						public void run() {
							ActionBar.sendActionBar(ChatColor.WHITE + FlightMultiAbility.this.player.getName() + ChatColor.GREEN + " has requested to carry you, right-click them to accept!", p2);
							if (System.currentTimeMillis() >= start + 300) {
								this.cancel();
							}
						}
					}.runTaskTimer(ProjectKorra.plugin, 0, 1);
				} else {
					requestedMap.remove(this.player.getUniqueId());
					requestTime.remove(this.player.getUniqueId());
				}
			}
		}

		switch (this.player.getInventory().getHeldItemSlot()) {
			case 0:
				this.mode = FlightMode.SOAR;
				break;
			case 1:
				this.mode = FlightMode.GLIDE;
				this.checkMultiplier();
				break;
			case 2:
				this.mode = FlightMode.LEVITATE;
				break;
			case 3:
				this.mode = FlightMode.ENDING;
				break;
		}

		this.speed = this.player.getVelocity().length();

		if (this.mode == FlightMode.SOAR) {
			this.player.setGliding(true);
			this.player.setAllowFlight(false);
			this.player.setFlying(false);

			if (this.speed > this.baseSpeed) {
				if (this.prevDir.angle(this.player.getEyeLocation().getDirection()) > 45 || this.prevDir.angle(this.player.getEyeLocation().getDirection()) < -45) {
					this.multiplier = 1;
				}

				this.prevDir = this.player.getEyeLocation().getDirection().clone();
				
				for (final Entity e : GeneralMethods.getEntitiesAroundPoint(this.player.getLocation(), this.speed)) {
					if (e instanceof LivingEntity && e.getEntityId() != this.player.getEntityId() && !this.player.getPassengers().contains(e)) {
						if (!GeneralMethods.isRegionProtectedFromBuild(this.player, e.getLocation())) {
							final LivingEntity le = (LivingEntity) e;
							DamageHandler.damageEntity(le, this.speed / 2, this);
							le.setVelocity(this.player.getVelocity().clone().multiply(2 / 3));
						}
					}
				}
			}

			this.particles();
			this.player.setVelocity(this.player.getEyeLocation().getDirection().clone().multiply(this.multiplier));
		} else if (this.mode == FlightMode.GLIDE) {
			this.player.setAllowFlight(false);
			this.player.setFlying(false);
			this.player.setGliding(true);
			this.particles();
		} else if (this.mode == FlightMode.LEVITATE) {
			this.player.setGliding(false);
			this.player.setAllowFlight(true);
			this.player.setFlying(true);
		} else if (this.mode == FlightMode.ENDING) {
			this.player.setGliding(false);
			this.player.setAllowFlight(false);
			this.player.setFlying(false);
		}

		if (isWater(this.player.getEyeLocation().clone().getBlock().getType())) {
			this.remove();
		}

		if (this.player.isOnGround()) {
			this.remove();
			return;
		}
	}

	private void particles() {
		ParticleEffect.CLOUD.display(GeneralMethods.getRightSide(this.player.getLocation(), 0.55).add(this.player.getVelocity().clone()), 0f, 0f, 0f, 0f, 1);
		ParticleEffect.CLOUD.display(GeneralMethods.getLeftSide(this.player.getLocation(), 0.55).add(this.player.getVelocity().clone()), 0f, 0f, 0f, 0f, 1);
	}

	private String speed() {
		if (this.speed >= this.fastSpeed - 0.3) {
			return ChatColor.RED + "FAST";
		} else if (this.speed >= this.baseSpeed - 0.3) {
			return ChatColor.GREEN + "NORMAL";
		} else if (this.speed >= 0) {
			return ChatColor.YELLOW + "SLOW";
		} else {
			return ChatColor.WHITE + "ERROR";
		}
	}

	private void checkMultiplier() {
		if (this.speed >= this.fastSpeed - 0.1) {
			this.multiplier = this.fastSpeed;
		} else if (this.speed >= this.baseSpeed - 0.1) {
			this.multiplier = this.baseSpeed;
		} else if (this.speed >= 0) {
			this.multiplier = this.slowSpeed;
		}
	}

	public void requestCarry(final Player p2) {
		if (this.mode != FlightMode.LEVITATE) {
			this.player.sendMessage(ChatColor.RED + "Can only request to carry when levitating!");
			return;
		}
		if (!this.player.getPassengers().isEmpty()) {
			this.player.sendMessage(ChatColor.RED + "You already have a passenger!");
			return;
		}
		if (flying.contains(p2.getUniqueId())) {
			this.player.sendMessage(ChatColor.RED + "Cannot request to carry an already flying player!");
			return;
		}
		if (requestedMap.containsKey(this.player.getUniqueId())) {
			if (requestedMap.get(this.player.getUniqueId()).equals(p2.getUniqueId())) {
				this.player.sendMessage(ChatColor.RED + "Already requested to carry that player!");
				return;
			}
		}
		requestedMap.put(this.player.getUniqueId(), p2.getUniqueId());
		requestTime.put(this.player.getUniqueId(), System.currentTimeMillis());
		this.player.sendMessage(ChatColor.GREEN + "Requested to carry " + ChatColor.WHITE + p2.getName());
	}

	@Override
	public void remove() {
		super.remove();
		this.bPlayer.addCooldown(this);
		MultiAbilityManager.unbindMultiAbility(this.player);
		flying.remove(this.player.getUniqueId());
		if (this.player.isOnline() && !this.player.isDead()) {
			this.player.eject();
		}
		flightHandler.removeInstance(this.player, ID);
		this.player.setGliding(this.hadGlide);
	}

	@Override
	public ArrayList<MultiAbilityInfoSub> getMultiAbilities() {
		final ArrayList<MultiAbilityInfoSub> abils = new ArrayList<>();
		abils.add(new MultiAbilityInfoSub("Soar", Element.FLIGHT));
		abils.add(new MultiAbilityInfoSub("Glide", Element.FLIGHT));
		abils.add(new MultiAbilityInfoSub("Levitate", Element.FLIGHT));
		abils.add(new MultiAbilityInfoSub("Ending", Element.FLIGHT));
		return abils;
	}

	public void manageSoarSpeed() {
		if (this.speed >= this.fastSpeed - 0.3) {
			this.multiplier = this.slowSpeed;
		} else if (this.speed >= this.baseSpeed - 0.3) {
			this.multiplier = this.fastSpeed;
		} else if (this.speed >= 0) {
			this.multiplier = this.baseSpeed;
		}

		final long start = System.currentTimeMillis();
		new BukkitRunnable() {
			@Override
			public void run() {
				ActionBar.sendActionBar(ChatColor.AQUA + "Flight speed: " + FlightMultiAbility.this.speed(), FlightMultiAbility.this.player);
				if (System.currentTimeMillis() >= start + 1000) {
					this.cancel();
				}
			}
		}.runTaskTimer(ProjectKorra.plugin, 0, 1);
	}

	public void cancel(final String reason) {
		if (!MovementHandler.isStopped(this.player) && !this.bPlayer.isChiBlocked()) {
			final long start = System.currentTimeMillis();
			new BukkitRunnable() {
				@Override
				public void run() {
					ActionBar.sendActionBar(ChatColor.RED + "* Flight cancelled due to " + reason + " *", FlightMultiAbility.this.player);
					if (System.currentTimeMillis() >= start + 1000) {
						this.cancel();
					}
				}
			}.runTaskTimer(ProjectKorra.plugin, 0, 1);
		}

		this.remove();
	}

	public static Set<UUID> getFlyingPlayers() {
		return flying;
	}

	public static void acceptCarryRequest(final Player requested, final Player requester) {
		if (!requestedCarry(requested, requester)) {
			return;
		}
		requester.sendMessage(ChatColor.WHITE + requested.getName() + ChatColor.GREEN + " has accepted your carry request!");
		requestedMap.remove(requester.getUniqueId());
		requestTime.remove(requester.getUniqueId());
		requester.addPassenger(requested);
	}

	public static boolean requestedCarry(final Player requested, final Player requester) {
		if (requestedMap.containsKey(requester.getUniqueId())) {
			return requestedMap.get(requester.getUniqueId()).equals(requested.getUniqueId());
		}
		return false;
	}
}
