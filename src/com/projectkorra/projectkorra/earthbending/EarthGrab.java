package com.projectkorra.projectkorra.earthbending;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Color;
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
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.util.MovementHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.ParticleEffect.BlockData;
import com.projectkorra.projectkorra.util.TempArmor;
import com.projectkorra.projectkorra.util.TempArmorStand;
import com.projectkorra.projectkorra.util.TempBlock;

public class EarthGrab extends EarthAbility {
	
	private LivingEntity target;
	private long cooldown, lastHit = 0, interval;
	private double range, dragSpeed, trapHP, trappedHP, damageThreshold;
	private GrabMode mode;
	private boolean initiated = false;
	private MovementHandler mHandler;
	private ArmorStand trap;
	private Location origin;
	private Vector direction;
	private TempArmor armor;
	private Material[] crops = new Material[] {Material.BEETROOT_BLOCK, Material.CARROT, Material.POTATO, Material.SUGAR_CANE_BLOCK, Material.CROPS, Material.MELON_BLOCK, Material.PUMPKIN};
	
	public static enum GrabMode {
		TRAP, DRAG, PROJECTING;
	}

	public EarthGrab(Player player, GrabMode mode) {
		super(player);
		
		if (hasAbility(player, EarthGrab.class)) {
			((EarthGrab) getAbility(player, EarthGrab.class)).remove();
			return;
		}
		
		if (bPlayer.isOnCooldown(this)) {
			return;
		}
		
		if (!isEarthbendable(player.getLocation().getBlock().getRelative(BlockFace.DOWN))) {
			return;
		}
		
		this.mode = mode;
		setFields();
		start();
	}
	
	private void setFields() {
		range = getConfig().getDouble("Abilities.Earth.EarthGrab.Range");
		cooldown = getConfig().getLong("Abilities.Earth.EarthGrab.Cooldown");
		dragSpeed = getConfig().getDouble("Abilities.Earth.EarthGrab.DragSpeed");
		interval = getConfig().getLong("Abilities.Earth.EarthGrab.TrapHitInterval");
		trapHP = getConfig().getDouble("Abilities.Earth.EarthGrab.TrapHP");
		damageThreshold = getConfig().getDouble("Abilities.Earth.EarthGrab.DamageThreshold");
		origin = player.getLocation().clone();
		direction = player.getLocation().getDirection().setY(0).normalize();
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
			case PROJECTING:
				project();
				break;
			case TRAP:
				trap();
				break;
			case DRAG:
				drag();
				break;
		}
	}
	
	public void project() {
		origin = origin.add(direction);
		Block top = GeneralMethods.getTopBlock(origin, 2);
		if (origin.distance(player.getLocation()) > range) {
			remove();
			return;
		}
		
		if (!isTransparent(top.getRelative(BlockFace.UP))) {
			remove();
			return;
		}
		
		if (top.getType() == Material.FIRE) {
			top.setType(Material.AIR);
		}
		
		while (!isEarthbendable(top)) {
			if (isTransparent(top)) {
				top = top.getRelative(BlockFace.DOWN);
			} else {
				remove();
				return;
			}
		}
		
		if (GeneralMethods.isRegionProtectedFromBuild(player, origin)) {
			remove();
			return;
		}
		
		origin.setY(top.getY() + 1);
		
		ParticleEffect.BLOCK_DUST.display(new BlockData(origin.getBlock().getRelative(BlockFace.DOWN).getType(), (byte)0), 0.2f, 0.5f, 0.2f, 0, 27, origin, 256);
		playEarthbendingSound(origin);
		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(origin, 1)) {
			if (entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId() && isEarthbendable(entity.getLocation().getBlock().getRelative(BlockFace.DOWN))) {
				if (entity instanceof Player && BendingPlayer.getBendingPlayer((Player)entity) != null) {
					if (CoreAbility.hasAbility((Player) entity, AvatarState.class)) {
						continue;
					}
				}
				target = (LivingEntity) entity;
				trappedHP = target.getHealth();
				mode = GrabMode.TRAP;
				origin = target.getLocation().clone();
			}
		}
	}
	
	public void trap() {
		if (!initiated) {
			Material m = target.getLocation().getBlock().getRelative(BlockFace.DOWN).getType();
			TempArmorStand tas = new TempArmorStand(target.getLocation());
			trap = tas.getArmorStand();
			trap.setVisible(false);
			trap.setInvulnerable(false);
			trap.setSmall(true);
			trap.setHelmet(new ItemStack(m));
			trap.setHealth(trapHP);
			trap.setMetadata("earthgrab:trap", new FixedMetadataValue(ProjectKorra.plugin, this));
			
			new TempBlock(target.getLocation().clone().subtract(0, 1, 0).getBlock(), target.getLocation().clone().subtract(0, 1, 0).getBlock().getType(), (byte)0);
			
			mHandler = new MovementHandler(target, this);
			mHandler.stop(Element.EARTH.getColor() + "* Trapped *");
			
			if (target instanceof Player || target instanceof Zombie || target instanceof Skeleton) {
				ItemStack legs = new ItemStack(Material.LEATHER_LEGGINGS);
				LeatherArmorMeta legmeta = (LeatherArmorMeta) legs.getItemMeta();
				legmeta.setColor(Color.fromRGB(EarthArmor.getColor(m)));
				legs.setItemMeta(legmeta);
				
				ItemStack feet = new ItemStack(Material.LEATHER_BOOTS);
				LeatherArmorMeta footmeta = (LeatherArmorMeta) feet.getItemMeta();
				footmeta.setColor(Color.fromRGB(EarthArmor.getColor(m)));
				feet.setItemMeta(footmeta);
				
				ItemStack[] pieces = {(target.getEquipment().getArmorContents()[0] == null || target.getEquipment().getArmorContents()[0].getType() == Material.AIR) ? feet : null, (target.getEquipment().getArmorContents()[1] == null || target.getEquipment().getArmorContents()[1].getType() == Material.AIR) ? legs : null, null, null};
				armor = new TempArmor(target, 36000000L, this, pieces);
			}
			
			playEarthbendingSound(target.getLocation());
			initiated = true;
		}
		
		ParticleEffect.BLOCK_DUST.display(new BlockData(target.getLocation().getBlock().getRelative(BlockFace.DOWN).getType(), (byte)0), 0.3f, 0.6f, 0.3f, 0, 36, target.getLocation(), 256);
		
		if (trap.getLocation().clone().subtract(0, 0.1, 0).getBlock().getType() != Material.AIR) {
			trap.setGravity(false);
		} else {
			trap.setGravity(true);
		}
		
		if (!isEarthbendable(target.getLocation().getBlock().getRelative(BlockFace.DOWN))) {
			remove();
			return;
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
		
		if (!GeneralMethods.isSolid(target.getLocation().getBlock().getRelative(BlockFace.DOWN))) {
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
			entity.setVelocity(GeneralMethods.getDirection(entity.getLocation(), player.getLocation()).normalize().multiply(dragSpeed));
			ParticleEffect.BLOCK_CRACK.display(new BlockData(b.getType(), b.getData()), 0, 0, 0, 0, 1, entity.getLocation(), 256);
			playEarthbendingSound(entity.getLocation());
		}
	}
	
	public void damageTrap() {
		if (System.currentTimeMillis() >= lastHit + interval) {
			trapHP -= 1;
			lastHit = System.currentTimeMillis();
			ParticleEffect.BLOCK_CRACK.display(new BlockData(target.getLocation().getBlock().getRelative(BlockFace.DOWN).getType(), (byte)0), 0.1f, 0.5f, 0.1f, 0, 17, target.getLocation().clone().add(0, 1, 0), 256);
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
			if (TempArmor.getTempArmorList(target).contains(armor)) {
				armor.revert();
			}
		}
		bPlayer.addCooldown(this);
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