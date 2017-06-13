package com.projectkorra.projectkorra.firebending.combo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

public class FireSpin extends FireCombo {

	private int movementCounter;
	private long cooldown;
	private double damage;
	private double speed;
	private double range;
	private double knockback;
	private Location origin;
	private Location location;
	private Location destination;
	private Vector direction;
	private ArrayList<LivingEntity> affectedEntities;
	private Map<Vector, Location> streams;
	
	public FireSpin(Player player) {
		super(player);
		
		this.damage = getConfig().getDouble("Abilities.Fire.FireCombo.FireSpin.Damage");
		this.range = getConfig().getDouble("Abilities.Fire.FireCombo.FireSpin.Range");
		this.cooldown = getConfig().getLong("Abilities.Fire.FireCombo.FireSpin.Cooldown");
		this.knockback = getConfig().getDouble("Abilities.Fire.FireCombo.FireSpin.Knockback");
		this.speed = 0.3;
		
		this.streams = new HashMap<Vector, Location>();
		this.affectedEntities = new ArrayList<LivingEntity>();
		
		if (bPlayer.isOnCooldown("FireSpin") && !bPlayer.isAvatarState()) {
			remove();
			return;
		}
		bPlayer.addCooldown(this);
		destination = player.getEyeLocation().add(range, 0, range);
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 0.5f, 0.5f);

		for (int i = 0; i <= 360; i += 5) {
			Vector vec = GeneralMethods.getDirection(player.getLocation(), destination.clone());
			vec = GeneralMethods.rotateXZ(vec, i - 180);
			vec.setY(0);
			
			streams.put(vec, player.getLocation());
		}
		
		start();
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return new FireSpin(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> fireSpin = new ArrayList<AbilityInformation>();
		fireSpin.add(new AbilityInformation("FireBlast", ClickType.LEFT_CLICK));
		fireSpin.add(new AbilityInformation("FireBlast", ClickType.LEFT_CLICK));
		fireSpin.add(new AbilityInformation("FireShield", ClickType.LEFT_CLICK));
		fireSpin.add(new AbilityInformation("FireShield", ClickType.SHIFT_DOWN));
		fireSpin.add(new AbilityInformation("FireShield", ClickType.SHIFT_UP));
		return fireSpin;
	}

	@Override
	public void progress() {
		for (Vector vec : streams.keySet()) {
			Block block = location.getBlock();
			if (block.getRelative(BlockFace.UP).getType() != Material.AIR && !ElementalAbility.isPlant(block)) {
				streams.remove(vec);
				continue;
			}
			ParticleEffect.FLAME.display(location, 0.0F, 0.0F, 0.0F, 0, 1);
			

			location.add(direction.normalize().multiply(speed));
			if (origin.distanceSquared(location) > range * range) {
				streams.remove(vec);
				continue;
			} else if (movementCounter % 10 != 0) {
				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2)) {
					if (entity instanceof LivingEntity && !entity.equals(this.getPlayer())) {
						if (!affectedEntities.contains(entity)) {
							affectedEntities.add((LivingEntity) entity);
							double newKnockback = bPlayer.isAvatarState() ? knockback + 0.5 : knockback;
							DamageHandler.damageEntity(entity, damage, this);
							entity.setVelocity(direction.normalize().multiply(newKnockback));
							streams.remove(vec);
						}
					}
				}
			}

			movementCounter++;
		}
		
		if (streams.size() == 0) {
			remove();
			return;
		}
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
	public List<Location> getLocations() {
		List<Location> locs = new ArrayList<Location>();
		locs.addAll(streams.values());
		return locs;
	}

}
