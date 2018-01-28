package com.projectkorra.projectkorra.earthbending;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.util.MovementHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.ParticleEffect.BlockData;
import com.projectkorra.projectkorra.util.TempArmorStand;

public class EarthGrab extends EarthAbility {
	
	public LivingEntity target;
	public long cooldown, lastHit = 0, interval;
	public double range, targetRange, dragSpeed, trapHP, trappedHP, damageThreshold;
	public GrabMode mode;
	public boolean initiated = false;
	public MovementHandler mHandler;
	public Material m;
	public ArmorStand trap;
	private Material[] crops = new Material[] {Material.BEETROOT_BLOCK, Material.CARROT, Material.POTATO, Material.SUGAR_CANE_BLOCK, Material.CROPS, Material.MELON_BLOCK, Material.PUMPKIN};
	
	public static enum GrabMode {
		TRAP, DRAG;
	}

	public EarthGrab(Player player) {
		super(player);
		
		if (hasAbility(player, EarthGrab.class)) {
			((EarthGrab) getAbility(player, EarthGrab.class)).remove();
			return;
		}
		
		if (bPlayer.isOnCooldown(this)) {
			return;
		}
		
		setFields();
		Entity e = GeneralMethods.getTargetedEntity(player, targetRange);
		if (!(e instanceof LivingEntity)) {
			target = null;
			mode = GrabMode.DRAG;
		} else {
			target = (LivingEntity) e;
			trappedHP = target.getHealth();
			mode = GrabMode.TRAP;
			m = target.getLocation().getBlock().getRelative(BlockFace.DOWN).getType();
			if (!isEarthbendable(m, true, true, false)) {
				return;
			}
			
			if (GeneralMethods.isRegionProtectedFromBuild(player, target.getLocation())) {
				return;
			}
		}
		
		start();
	}
	
	private void setFields() {
		range = getConfig().getDouble("Abilities.Earth.EarthGrab.Range");
		targetRange = getConfig().getDouble("Abilities.Earth.EarthGrab.SelectRange");
		cooldown = getConfig().getLong("Abilities.Earth.EarthGrab.Cooldown");
		dragSpeed = getConfig().getDouble("Abilities.Earth.EarthGrab.DragSpeed");
		interval = getConfig().getLong("Abilities.Earth.EarthGrab.TrapHitInterval");
		trapHP = getConfig().getDouble("Abilities.Earth.EarthGrab.TrapHP");
		damageThreshold = getConfig().getDouble("Abilities.Earth.EarthGrab.DamageThreshold");
	}

	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		}
		
		if (target != null) {
			if (target instanceof Player) {
				Player pt = (Player) target;
				if (!pt.isOnline()) {
					remove();
					return;
				}
			}
			
			if (target.isDead()) {
				remove();
				return;
			}
		}
		
		switch (mode) {
		case TRAP:
			trap();
			break;
		case DRAG:
			drag();
			break;
		}
	}
	
	public void trap() {
		if (!initiated) {
			TempArmorStand tas = new TempArmorStand(target.getLocation());
			trap = tas.getArmorStand();
			trap.setVisible(false);
			trap.setInvulnerable(false);
			trap.setSmall(true);
			trap.setHelmet(new ItemStack(m));
			trap.setHealth(trapHP);
			trap.setMetadata("earthgrab:trap", new FixedMetadataValue(ProjectKorra.plugin, this));
			mHandler = new MovementHandler(target, this);
			mHandler.stop(Element.EARTH.getColor() + "* Trapped *");
			playEarthbendingSound(target.getLocation());
			initiated = true;
		}
		
		ParticleEffect.BLOCK_DUST.display(new BlockData(m, (byte)0), 0.2f, 0.5f, 0.2f, 0, 27, target.getLocation(), 256);
		
		if (trap.getLocation().clone().subtract(0, 0.1, 0).getBlock().getType() != Material.AIR) {
			trap.setGravity(false);
		} else {
			trap.setGravity(true);
		}
		
		if (trap.getLocation().distance(target.getLocation()) > 2) {
			remove();
			return;
		}
		
		if (trappedHP - target.getHealth() >= damageThreshold) {
			remove();
			return;
		}
		
		if (trapHP <= 0) {
			remove();
			return;
		}
		
		if (trap.isDead()) {
			remove();
			return;
		}
		
		if (player.getLocation().distance(target.getLocation()) > range) {
			remove();
			return;
		}
		
		if (GeneralMethods.isSolid(target.getLocation().getBlock())) {
			remove();
			return;
		}
	}
	
	public void drag() {
		if (!player.isOnGround()) {
			return;
		}
		
		if (!player.isSneaking()) {
			remove();
			return;
		}
		
		if (GeneralMethods.isRegionProtectedFromBuild(player, player.getLocation())) {
			remove();
			return;
		}
		
		for (Location l : GeneralMethods.getCircle(player.getLocation(), (int) Math.floor(range), 2, false, false, 0)) {
			if (!Arrays.asList(crops).contains(l.getBlock().getType())) {
				continue;
			}
			
			Block b = l.getBlock();
			if (b.getData() == (byte)7 || b.getType() == Material.MELON_BLOCK || b.getType() == Material.PUMPKIN) {
				b.breakNaturally();
			}
		}
		
		List<Entity> ents = GeneralMethods.getEntitiesAroundPoint(player.getLocation(), range);
		if (ents.isEmpty()) {
			remove();
			return;
		}
		
		for (Entity entity : ents) {
			if (!isEarth(entity.getLocation().clone().subtract(0, 1, 0).getBlock()) && (bPlayer.canSandbend() && !isSand(entity.getLocation().clone().subtract(0, 1, 0).getBlock()))
					&& entity.getLocation().clone().subtract(0, 1, 0).getBlock().getType() != Material.SOIL) {
				continue;
			}
			
			if (entity instanceof Arrow) {
				Location l = entity.getLocation();
				entity.remove();
				entity = l.getWorld().dropItem(l, new ItemStack(Material.ARROW, 1));
			} else if (!(entity instanceof Item)) {
				continue;
			}
			Block b = entity.getLocation().getBlock().getRelative(BlockFace.DOWN);
			entity.setVelocity(GeneralMethods.getDirection(entity.getLocation(), player.getLocation()).multiply(dragSpeed));
			ParticleEffect.BLOCK_CRACK.display(new BlockData(b.getType(), b.getData()), 0, 0, 0, 0, 1, entity.getLocation(), 256);
			playEarthbendingSound(entity.getLocation());
		}
	}
	
	public void damageTrap() {
		if (System.currentTimeMillis() >= lastHit + interval) {
			trapHP -= 1;
			lastHit = System.currentTimeMillis();
			ParticleEffect.BLOCK_CRACK.display(new BlockData(m, (byte)0), 0.1f, 0.5f, 0.1f, 0, 17, target.getLocation().clone().add(0, 1, 0), 256);
			playEarthbendingSound(target.getLocation());
		}
	}
	
	@Override
	public void remove() {
		super.remove();
		if (mode == GrabMode.TRAP) {
			bPlayer.addCooldown(this);
			mHandler.reset();
			trap.remove();
		}
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
	public String getName() {
		return "EarthGrab";
	}

	@Override
	public Location getLocation() {
		return target == null ? null : target.getLocation();
	}
	
	public GrabMode getMode() {
		return mode;
	}
	
	public double getRange() {
		return range;
	}
	
	public LivingEntity getTarget() {
		return target;
	}
}