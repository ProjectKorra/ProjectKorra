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
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

public class FireKick extends FireCombo {

	private int moveCounter;
	private long cooldown;
	private double damage;
	private double speed;
	private double range;
	private Location location;
	private Location destination;
	private Vector direction;
	private ArrayList<LivingEntity> affectedEntities;
	private Map<Vector, Location> streams;
	private Location origin;
	
	public FireKick(Player player) {
		super(player);
		
		this.damage = getConfig().getDouble("Abilities.Fire.FireCombo.FireKick.Damage");
		this.range = getConfig().getDouble("Abilities.Fire.FireCombo.FireKick.Range");
		this.cooldown = getConfig().getLong("Abilities.Fire.FireCombo.FireKick.Cooldown");
		
		if (bPlayer.isAvatarState()) {
			this.cooldown = 0;
			this.damage = AvatarState.getValue(damage);
			this.range = AvatarState.getValue(range);
		}
		
		this.streams = new HashMap<Vector, Location>();
		this.affectedEntities = new ArrayList<LivingEntity>();
		
		if (bPlayer.isOnCooldown("FireKick") && !bPlayer.isAvatarState()) {
			remove();
			return;
		}

		bPlayer.addCooldown(this);
		this.origin = player.getLocation();
		Vector eyeDir = player.getEyeLocation().getDirection().normalize().multiply(range);
		destination = player.getEyeLocation().add(eyeDir);

		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_HORSE_JUMP, 0.5f, 0f);
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 0.5f, 1f);
		for (int i = -30; i <= 30; i += 5) {
			Vector vec = GeneralMethods.getDirection(player.getLocation(), destination.clone());
			vec = GeneralMethods.rotateXZ(vec, i);
			streams.put(vec, player.getLocation());
			player.getWorld().playSound(player.getLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 0.5f, 1f);
		}
		
		start();
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return new FireKick(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> fireKick = new ArrayList<>();
		fireKick.add(new AbilityInformation("FireBlast", ClickType.LEFT_CLICK));
		fireKick.add(new AbilityInformation("FireBlast", ClickType.LEFT_CLICK));
		fireKick.add(new AbilityInformation("FireBlast", ClickType.SHIFT_DOWN));
		fireKick.add(new AbilityInformation("FireBlast", ClickType.LEFT_CLICK));
		return fireKick;
	}
	
	@Override
	public String getInstructions() {
		return "FireBlast > FireBlast > (Hold Shift) > FireBlast.";
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBinds(this)) {
			remove();
			return;
		}
		
		for (Vector vec : streams.keySet()) {
			Location loc = streams.get(vec);
			Block block = loc.getBlock();
			if (block.getRelative(BlockFace.UP).getType() != Material.AIR && !ElementalAbility.isPlant(block)) {
				streams.remove(vec);
				continue;
			}
			
			ParticleEffect.FLAME.display(loc, 0.2F, 0.2F, 0.2F, 0, 5);

			loc.add(direction.normalize().multiply(speed));
			if (origin.distanceSquared(loc) > range * range) {
				streams.remove(vec);
				continue;
			} else if (moveCounter % 3 != 0) {
				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2)) {
					if (entity instanceof LivingEntity && !entity.equals(this.getPlayer())) {
						entity.getLocation().getWorld().playSound(entity.getLocation(), Sound.ENTITY_VILLAGER_HURT, 0.3f, 0.3f);
						if (!affectedEntities.contains(entity)) {
							affectedEntities.add((LivingEntity) entity);
							DamageHandler.damageEntity(entity, damage, this);
							streams.remove(vec);
						}
					}
				}
			}

			moveCounter++;
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
		return "FireKick";
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
