package com.projectkorra.projectkorra.waterbending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.PlantAbility;
import com.projectkorra.projectkorra.earthbending.EarthArmor;
import com.projectkorra.projectkorra.util.PassiveHandler;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.util.Vector;

import java.util.Random;

public class PlantArmor extends PlantAbility {
	
	private boolean formed;
	private int resistance;
	private long duration;
	private long cooldown;
	private double range;
	private Material blockType;
	private Block block;
	private Location location;
	private PlantRegrowth plantbending;
	private ItemStack[] oldArmor;	
	
	public PlantArmor(Player player) {
		super(player);
		
		this.resistance = getConfig().getInt("Abilities.Water.PlantArmor.Resistance");
		this.range = getConfig().getInt("Abilities.Water.PlantArmor.Range");
		this.duration = getConfig().getLong("Abilities.Water.PlantArmor.Duration");
		this.cooldown = getConfig().getLong("Abilities.Water.PlantArmor.Cooldown");
		
		this.range = getNightFactor(range);
		this.duration = (long) getNightFactor(duration);  
		
		if (hasAbility(player, PlantArmor.class)) {
			return;
		} else if (!bPlayer.canBend(this)) {
			return;
		}
		
		if (hasAbility(player, EarthArmor.class)) {
			EarthArmor abil = getAbility(player, EarthArmor.class);
			abil.remove();
		}
		
		block = getPlantSourceBlock(player, range, true);
		if (block == null) {
			return;
		}
		
		location = block.getLocation();
		if (!canUse()) {
			return;
		}
		
		plantbending = new PlantRegrowth(player, block);
		blockType = block.getType();
		block.setType(Material.AIR);
		
		start();
	}

	private boolean canUse() {
		if (!bPlayer.canPlantbend() 
				|| !bPlayer.canBend(this) 
				|| GeneralMethods.isRegionProtectedFromBuild(this, location)) {
			remove();
			return false;
		} else if (location.distanceSquared(player.getEyeLocation()) > range * range) {
			remove();
			return false;
		}
		return true;
	}

	private void formArmor() {
		oldArmor = player.getInventory().getArmorContents();
		ItemStack helmet = new ItemStack(blockType);
		ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
		
		LeatherArmorMeta itemMeta = (LeatherArmorMeta) chestplate.getItemMeta();
		itemMeta.setColor(Color.GREEN);
		chestplate.setItemMeta(itemMeta);
		
		ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
		ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
		leggings.setItemMeta(itemMeta);
		boots.setItemMeta(itemMeta);
		
		player.getInventory().setHelmet(helmet);
		player.getInventory().setChestplate(chestplate);
		player.getInventory().setLeggings(leggings);
		player.getInventory().setBoots(boots);

		formed = true;
		startTime = System.currentTimeMillis();
	}

	private boolean inPosition() {
		return location.distanceSquared(player.getEyeLocation()) <= 1.5 * 1.5;
	}

	private void playEffect() {
		if (!formed) {
			if ((new Random()).nextInt(4) == 0) {
				playPlantbendingSound(location);
			}
			
			GeneralMethods.displayColoredParticle(location, "009933");
			Vector vector = player.getEyeLocation().toVector().subtract(location.toVector());
			location = location.add(vector.normalize());
		}
	}

	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}

		if (formed) {
			PassiveHandler.checkArmorPassives(player);
			if (System.currentTimeMillis() > startTime + duration) {
				remove();
				bPlayer.addCooldown(this);
				return;
			}
		} else if (!canUse()) {
			return;
		}
		
		playEffect();
		if (inPosition() && !formed) {
			formArmor();
		}
	}

	@Override
	public void remove() {
		super.remove();
		
		if (oldArmor != null) {
			player.getInventory().setArmorContents(oldArmor);
		}
		
		if (plantbending != null) {
			plantbending.remove();
		}
	}
	
	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	public static boolean canRemoveArmor(Player player) {
		PlantArmor plantArmor = getAbility(player, PlantArmor.class);
		if (plantArmor != null) {
			if (System.currentTimeMillis() < plantArmor.startTime + plantArmor.duration) {
				return false;
			}
		}
		return true;
	}
	
	public void setResistance(int resistance) {
		this.resistance = resistance;
	}
	
	public int getResistance() {
		return resistance;
	}

	@Override
	public String getName() {
		return "PlantArmor";
	}

	@Override
	public Location getLocation() {
		if (location != null) {
			return location;
		} else if (block != null) {
			return block.getLocation();
		}
		return player != null ? player.getLocation() : null;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	public boolean isFormed() {
		return formed;
	}

	public void setFormed(boolean formed) {
		this.formed = formed;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public Material getBlockType() {
		return blockType;
	}

	public void setBlockType(Material blockType) {
		this.blockType = blockType;
	}

	public Block getBlock() {
		return block;
	}

	public void setBlock(Block block) {
		this.block = block;
	}

	public PlantRegrowth getPlantbending() {
		return plantbending;
	}

	public void setPlantbending(PlantRegrowth plantbending) {
		this.plantbending = plantbending;
	}

	public ItemStack[] getOldArmor() {
		return oldArmor;
	}

	public void setOldArmor(ItemStack[] oldArmor) {
		this.oldArmor = oldArmor;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
		
}
