package com.projectkorra.projectkorra.airbending.flight;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.FlightAbility;
import com.projectkorra.projectkorra.ability.MultiAbility;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager.MultiAbilityInfoSub;
import com.projectkorra.projectkorra.util.ActionBar;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

public class FlightMultiAbility extends FlightAbility implements MultiAbility{
	
	private static Set<UUID> flying = new HashSet<>();
	private boolean canFly, hadFly, hadGlide;
	
	private static enum FlightMode {
		SOAR, GLIDE, LEVITATE, ENDING;
	}
	
	public double speed = 1, baseSpeed, slowSpeed, fastSpeed, multiplier;
	public FlightMode mode = FlightMode.SOAR;
	public long prevCheck = 0;
	public Vector prevDir;

	public FlightMultiAbility(Player player) {
		super(player);
		
		FlightMultiAbility f = getAbility(player, FlightMultiAbility.class);
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
		
		MultiAbilityManager.bindMultiAbility(player, "Flight");
		hadFly = player.isFlying();
		canFly = player.getAllowFlight();
		hadGlide = player.isGliding();
		flying.add(player.getUniqueId());
		prevDir = player.getEyeLocation().getDirection().clone();
		baseSpeed = getConfig().getDouble("Abilities.Air.Flight.BaseSpeed");
		slowSpeed = baseSpeed/2;
		fastSpeed = baseSpeed*2;
		multiplier = baseSpeed;
		start();
	}

	@Override
	public long getCooldown() {
		return getConfig().getLong("Abilities.Air.Flight.Cooldown");
	}

	@Override
	public Location getLocation() {
		return player == null ? null : player.getLocation();
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
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		}
		
		switch (player.getInventory().getHeldItemSlot()) {
		case 0:
			mode = FlightMode.SOAR;
			ActionBar.sendActionBar(ChatColor.AQUA + "Flight speed: " + speed(), player);
			break;
		case 1:
			mode = FlightMode.GLIDE;
			ActionBar.sendActionBar(ChatColor.AQUA + "Flight speed: " + speed(), player);
			checkMultiplier();
			break;
		case 2:
			mode = FlightMode.LEVITATE;
			break;
		case 3:
			mode = FlightMode.ENDING;
			break;
		}
		
		speed = player.getVelocity().length();
		
		if (mode == FlightMode.SOAR) {
			player.setGliding(true);
			player.setAllowFlight(false);
			player.setFlying(false);
			
			if (speed > baseSpeed) {
				if (prevDir.angle(player.getEyeLocation().getDirection()) > 45 || prevDir.angle(player.getEyeLocation().getDirection()) < -45) {
					multiplier = 1;
					ActionBar.sendActionBar(ChatColor.AQUA + "Flight Speed: " + ChatColor.GREEN + "NORMAL", player);
				}
				
				prevDir = player.getEyeLocation().getDirection().clone();
			}
			
			particles();
			
			if (speed >= baseSpeed) {
				for (Entity e : GeneralMethods.getEntitiesAroundPoint(player.getLocation(), speed)) {
					if (e instanceof LivingEntity && e.getEntityId() != player.getEntityId() && !player.getPassengers().contains(e)) {
						LivingEntity le = (LivingEntity) e;
						DamageHandler.damageEntity(le, speed/2, this);
						le.setVelocity(player.getVelocity().clone().multiply(2/3));
					}
				}
			}
			
			player.setVelocity(player.getEyeLocation().getDirection().clone().multiply(multiplier));
		} else if (mode == FlightMode.GLIDE) {
			player.setAllowFlight(false);
			player.setFlying(false);
			player.setGliding(true);
			particles();
		} else if (mode == FlightMode.LEVITATE) {
			player.setGliding(false);
			player.setAllowFlight(true);
			player.setFlying(true);
		} else if (mode == FlightMode.ENDING) {
			player.setGliding(hadGlide);
			player.setAllowFlight(canFly);
			player.setFlying(hadFly);
		}
		
		if (isWater(player.getEyeLocation().clone().getBlock().getType())) {
			remove();
		}
		
		if (player.isOnGround()) {
			remove();
			return;
		}
	}
	
	private void particles() {
		ParticleEffect.CLOUD.display(GeneralMethods.getRightSide(player.getLocation(), 0.55).add(player.getVelocity().clone()), 0f, 0f, 0f, 0f, 1);
		ParticleEffect.CLOUD.display(GeneralMethods.getLeftSide(player.getLocation(), 0.55).add(player.getVelocity().clone()), 0f, 0f, 0f, 0f, 1);
	}
	
	private String speed() {
		if (speed >= fastSpeed-0.3) {
			return ChatColor.RED + "FAST";
		} else if (speed >= baseSpeed-0.3) {
			return ChatColor.GREEN + "NORMAL";
		} else if (speed >= 0) {
			return ChatColor.YELLOW + "SLOW";
		} else {
			return ChatColor.WHITE + "ERROR";
		}
	}
	
	private void checkMultiplier() {
		if (speed >= fastSpeed-0.1) {
			multiplier = fastSpeed;
		} else if (speed >= baseSpeed-0.1) {
			multiplier = baseSpeed;
		} else if (speed >= 0) {
			multiplier = slowSpeed;
		}
	}
	
	@Override
	public void remove() {
		super.remove();
		bPlayer.addCooldown(this);
		MultiAbilityManager.unbindMultiAbility(player);
		flying.remove(player.getUniqueId());
		if (player.isOnline() && !player.isDead()) {
			player.eject();
		}
		player.setAllowFlight(canFly);
		player.setFlying(hadFly);
		player.setGliding(hadGlide);
	}

	@Override
	public ArrayList<MultiAbilityInfoSub> getMultiAbilities() {
		ArrayList<MultiAbilityInfoSub> abils = new ArrayList<>();
		abils.add(new MultiAbilityInfoSub("Soar", Element.FLIGHT));
		abils.add(new MultiAbilityInfoSub("Glide", Element.FLIGHT));
		abils.add(new MultiAbilityInfoSub("Levitate", Element.FLIGHT));
		abils.add(new MultiAbilityInfoSub("Ending", Element.FLIGHT));
		return abils;
	}

	public void manageSoarSpeed() {
		if (speed >= fastSpeed-0.3) {
			multiplier = slowSpeed;
		} else if (speed >= baseSpeed-0.3) {
			multiplier = fastSpeed;
		} else if (speed >= 0) {
			multiplier = baseSpeed;
		}
	}
	
	public void cancel(String reason) {
		ActionBar.sendActionBar(ChatColor.RED + "FLIGHT CANCELLED BY: " + reason, player);
		remove();
	}
	
	public static Set<UUID> getFlyingPlayers() {
		return flying;
	}
}
