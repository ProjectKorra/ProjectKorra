package com.projectkorra.projectkorra.earthbending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempArmor;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.PlantArmor;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class EarthArmor extends EarthAbility {

	private boolean formed;
	private MaterialData headData;
	private MaterialData legsData;
	private long cooldown;
	private long interval;
	private double selectRange;
	private Block headBlock;
	private Block legsBlock;
	private Location headBlockLocation;
	private Location legsBlockLocation;
	private boolean active;
	private PotionEffect oldAbsorbtion = null;
	private float goldHearts;
	private int maxGoldHearts;
	
	public EarthArmor(Player player) {
		super(player);
		if (hasAbility(player, EarthArmor.class) || !bPlayer.canBend(this)) {
			return;
		}
		
		if (hasAbility(player, PlantArmor.class)) {
			PlantArmor abil = getAbility(player, PlantArmor.class);
			abil.remove();
		}
		
		this.formed = false;
		this.active = true;
		this.interval = 2000;
		this.goldHearts = 0;
		this.cooldown = getConfig().getLong("Abilities.Earth.EarthArmor.Cooldown");
		this.selectRange = getConfig().getDouble("Abilities.Earth.EarthArmor.SelectRange");
		this.maxGoldHearts = getConfig().getInt("Abilities.Earth.EarthArmor.GoldHearts");
		
		headBlock = getTargetEarthBlock((int) selectRange);
		if (!GeneralMethods.isRegionProtectedFromBuild(this, headBlock.getLocation()) 
				&& getEarthbendableBlocksLength(headBlock, new Vector(0, -1, 0), 2) >= 2) {			
			this.legsBlock = headBlock.getRelative(BlockFace.DOWN);
			this.headData = headBlock.getState().getData();
			this.legsData = legsBlock.getState().getData();
			this.headBlockLocation = headBlock.getLocation();
			this.legsBlockLocation = legsBlock.getLocation();

			Block oldHeadBlock = headBlock;
			Block oldLegsBlock = legsBlock;

			if (!moveBlocks()) {
				return;
			}
			if (isEarthRevertOn()) {
				addTempAirBlock(oldHeadBlock);
				addTempAirBlock(oldLegsBlock);
			} else {
				GeneralMethods.removeBlock(oldHeadBlock);
				GeneralMethods.removeBlock(oldLegsBlock);
			}
			
			playEarthbendingSound(headBlock.getLocation());
			bPlayer.addCooldown(this, getCooldown() / 2); //Prevents spamming of the move to remove blocks
			
			start();
		}
	}

	@SuppressWarnings("deprecation")
	private void formArmor() {
		if (TempBlock.isTempBlock(headBlock)) {
			TempBlock.revertBlock(headBlock, Material.AIR);
		}
		if (TempBlock.isTempBlock(legsBlock)) {
			TempBlock.revertBlock(legsBlock, Material.AIR);
		}
		
		ItemStack head = new ItemStack(Material.LEATHER_HELMET, 1);
		ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
		ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS, 1);
		ItemStack boots = new ItemStack(Material.LEATHER_BOOTS, 1);
		
		LeatherArmorMeta metaHead = (LeatherArmorMeta) head.getItemMeta();
		LeatherArmorMeta metaChest = (LeatherArmorMeta) chestplate.getItemMeta();
		LeatherArmorMeta metaLegs = (LeatherArmorMeta) leggings.getItemMeta();
		LeatherArmorMeta metaBottom = (LeatherArmorMeta) boots.getItemMeta();
		
		metaHead.setColor(Color.fromRGB(getColor(headData.getItemType(), headData.getData())));
		metaChest.setColor(Color.fromRGB(getColor(headData.getItemType(), headData.getData())));
		metaLegs.setColor(Color.fromRGB(getColor(legsData.getItemType(), legsData.getData())));
		metaBottom.setColor(Color.fromRGB(getColor(legsData.getItemType(), legsData.getData())));
		
		head.setItemMeta(metaHead);
		chestplate.setItemMeta(metaChest);
		leggings.setItemMeta(metaLegs);
		boots.setItemMeta(metaBottom);
		
		ItemStack armors[] = { boots, leggings, chestplate, head };
		TempArmor armor = new TempArmor(player, 72000000L, this, armors); //Duration of 2 hours
		armor.setRemovesAbilityOnForceRevert(true);
		formed = true;
		
		for (PotionEffect effect : player.getActivePotionEffects()) {
			if (effect.getType() == PotionEffectType.ABSORPTION) {
				this.oldAbsorbtion = effect;
				player.removePotionEffect(PotionEffectType.ABSORPTION);
				break;
			}
		}
		int level = (int) (maxGoldHearts / 2 - 1 + (maxGoldHearts % 2));
		player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, Integer.MAX_VALUE, level, true, false));
		
		this.goldHearts = maxGoldHearts * 2;
		GeneralMethods.setAbsorbationHealth(player, goldHearts);
	}
	
	private boolean inPosition() {
		return headBlock.equals(player.getEyeLocation().getBlock()) && legsBlock.equals(player.getLocation().getBlock());
	}

	@SuppressWarnings("deprecation")
	private boolean moveBlocks() {
		if (!player.getWorld().equals(headBlock.getWorld())) {
			remove();
			return false;
		}
		
		Location headLocation = player.getEyeLocation();
		Location legsLocation = player.getLocation();
		Vector headDirection = headLocation.toVector().subtract(headBlockLocation.toVector()).normalize().multiply(.5);
		//Vector legsDirection = legsLocation.toVector().subtract(legsBlockLocation.toVector()).normalize().multiply(.5);
		Block newHeadBlock = headBlock;
		Block newLegsBlock = legsBlock;
		
		int yDiff = player.getEyeLocation().getBlockY() - headBlock.getY();
		
		if (yDiff != 0) {
			Block checkBlock = yDiff > 0 ? headBlock.getRelative(BlockFace.UP) : legsBlock.getRelative(BlockFace.DOWN);
			
			if (isTransparent(checkBlock) && !checkBlock.isLiquid()) {
				GeneralMethods.breakBlock(checkBlock); //Destroy any minor blocks that are in the way
				
				headDirection = new Vector(0, yDiff > 0 ? 0.5 : -0.5, 0);
			}
		}

		if (!headLocation.getBlock().equals(headBlock)) {
			headBlockLocation = headBlockLocation.clone().add(headDirection);
			newHeadBlock = headBlockLocation.getBlock();
		}
		if (!legsLocation.getBlock().equals(legsBlock)) {
			legsBlockLocation = headBlockLocation.clone().add(0, -1, 0);
			newLegsBlock = newHeadBlock.getRelative(BlockFace.DOWN);
		}

		if (isTransparent(newHeadBlock) && !newHeadBlock.isLiquid()) {
			GeneralMethods.breakBlock(newHeadBlock);
		} else if (!isEarthbendable(newHeadBlock) && !newHeadBlock.isLiquid() && newHeadBlock.getType() != Material.AIR) {
			ParticleEffect.BLOCK_CRACK.display(new ParticleEffect.BlockData(headData.getItemType(), headData.getData()), 0.5F, 0.5F, 0.5F, 1, 32, newLegsBlock.getLocation(), 128);
			remove();
			return false;
		}

		if (isTransparent(newLegsBlock) && !newLegsBlock.isLiquid()) {
			GeneralMethods.breakBlock(newLegsBlock);
		} else if (!isEarthbendable(newLegsBlock) && !newLegsBlock.isLiquid() && newLegsBlock.getType() != Material.AIR) {
			newLegsBlock.getLocation().getWorld().playSound(newLegsBlock.getLocation(), Sound.BLOCK_GRASS_BREAK, 1, 1);
			ParticleEffect.BLOCK_CRACK.display(new ParticleEffect.BlockData(legsData.getItemType(), legsData.getData()), 0.5F, 0.5F, 0.5F, 1, 32, newLegsBlock.getLocation(), 128);
			remove();
			return false;
		}

		if (headBlock.getLocation().distanceSquared(player.getEyeLocation()) > selectRange * selectRange) {
			remove();
			return false;
		}

		if (!newHeadBlock.equals(headBlock)) {
			new TempBlock(newHeadBlock, headData.getItemType(), headData.getData());
			if (TempBlock.isTempBlock(headBlock)) {
				TempBlock.revertBlock(headBlock, Material.AIR);
			}
		}

		if (!newLegsBlock.equals(legsBlock)) {
			new TempBlock(newLegsBlock, legsData.getItemType(), legsData.getData());
			if (TempBlock.isTempBlock(legsBlock)) {
				TempBlock.revertBlock(legsBlock, Material.AIR);
			}
		}
		headBlock = newHeadBlock;
		legsBlock = newLegsBlock;
		return true;
	}

	@Override
	public void progress() {		
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			remove();
			return;
		}

		if (formed) {
			//PassiveHandler.checkArmorPassives(player);
			if (!player.hasPotionEffect(PotionEffectType.ABSORPTION)) {
				player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, Integer.MAX_VALUE, 1, true, false));
				GeneralMethods.setAbsorbationHealth(player, goldHearts);
			}
			
			if (!active) {
				bPlayer.addCooldown(this);
				remove();
				return;
			}
			
			player.setFireTicks(0);
		} else {
			if (!moveBlocks()) {
				return;
			}
			if (inPosition()) {
				formArmor();
			}
		}
	}

	@Override
	public void remove() {
		super.remove();
		if (isEarthRevertOn()) {
			if (TempBlock.isTempBlock(headBlock)) {
				TempBlock.revertBlock(headBlock, Material.AIR);
			}
			if (TempBlock.isTempBlock(legsBlock)) {
				TempBlock.revertBlock(legsBlock, Material.AIR);
			}
		} else {
			headBlock.breakNaturally();
			legsBlock.breakNaturally();
		}
		
		if (TempArmor.hasTempArmor(player) && TempArmor.getTempArmor(player).getAbility().equals(this)) {
			TempArmor.getTempArmor(player).revert();
		}
		
		player.removePotionEffect(PotionEffectType.ABSORPTION);
		
		if (oldAbsorbtion != null) {
			player.addPotionEffect(oldAbsorbtion);
		}
		
	}

	public void updateAbsorbtion() {
		final EarthArmor abil = this;
		new BukkitRunnable() {
			@SuppressWarnings("deprecation")
			public void run() {
				goldHearts = GeneralMethods.getAbsorbationHealth(player);
				if (formed && goldHearts < 0.9F) {
					bPlayer.addCooldown(abil);
					
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 2, 1);
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 2, 1);
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 2, 1);
					
					ParticleEffect.BLOCK_CRACK.display(new ParticleEffect.BlockData(headData.getItemType(), headData.getData()), 0.1F, 0.1F, 0.1F, 1, 32, player.getEyeLocation(), 128);
					ParticleEffect.BLOCK_CRACK.display(new ParticleEffect.BlockData(legsData.getItemType(), legsData.getData()), 0.1F, 0.1F, 0.1F, 1, 32, player.getLocation(), 128);
				
					remove();
				}
			}
		}.runTaskLater(ProjectKorra.plugin, 1L);
		
	}
	public static int getColor(Material material) {
		return getColor(material, (byte) 0x0);
	}
	
	/**Returns the color for the specified material.*/
	public static int getColor(Material material, byte damage) {
		if (material == Material.GRASS) return 0x29932C; //Dark dull green - Needs to be darker
		if (material == Material.CLAY) return 0xBAC2D1; //Dull gray-brown - PERFECT
		if (material == Material.STONE && damage == 0x0) return 0xCCCCCC; //Gray
		if (material == Material.STONE && (damage == 0x1 || damage == 0x2)) return 0xC9705C; //Pink - Needs to be richer
		if (material == Material.STONE && (damage == 0x3 || damage == 0x4)) return 0xF8F7FC; //White
		if (material == Material.STONE && (damage == 0x5 || damage == 0x6)) return 0xBFBFBF; //Gray  - fine for now
		if (material == Material.COBBLESTONE) return 0x6B6B6B; //Dark Gray
		if (material == Material.SAND && damage == 0x0) return 0xFFFFCC; //Sand yellow - PERFECT
		if (material == Material.SAND && damage == 0x1) return 0xB85F25; //Sand orange - Needs more red --------------
		if (material == Material.SANDSTONE) return 0xFFF372; //Sand - Could be darker/more vibrant
		if (material == Material.RED_SANDSTONE) return 0xB85F25; //Red sandstone - PERFECT
		if (material == Material.GRAVEL) return 0xEDE4DC; //Dark Gray 
		if (material == Material.GOLD_ORE) return 0xF2F204;
		if (material == Material.GOLD_BLOCK) return 0xF2F204; //Gold - Could be a tiny bit darker
		if (material == Material.IRON_ORE) 	return 0xf4f4f4;
		if (material == Material.IRON_BLOCK) return 0xf4f4f4; //Silver/Gray
		if (material == Material.COAL_ORE) return 0x999999; //Stone gray
		if (material == Material.DIRT) return 0x843700; //Default dirt brown - NEEDS SERIOUS CHANGING
		if (material == Material.LAPIS_ORE) return 0x0060BA;
		if (material == Material.LAPIS_BLOCK) return 0x0060BA; //Dark blue
		if (material == Material.NETHERRACK) return 0x13139A; //Pinkish-red - PERFECT
		if (material == Material.QUARTZ_ORE) return 0x13139A; //Pinkish-red
		if (material == Material.QUARTZ_BLOCK) return 0xFDFDFD; //White
		
		return 0xCCCCCC; //Stone
	}
	
	@SuppressWarnings("deprecation")
	public void click() {
		if (!this.player.isSneaking()) return;
		
		player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 2, 1);
		player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 2, 1);
		player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 2, 1);
		
		ParticleEffect.BLOCK_CRACK.display(new ParticleEffect.BlockData(headData.getItemType(), headData.getData()), 0.1F, 0.1F, 0.1F, 1, 32, player.getEyeLocation(), 128);
		ParticleEffect.BLOCK_CRACK.display(new ParticleEffect.BlockData(legsData.getItemType(), legsData.getData()), 0.1F, 0.1F, 0.1F, 1, 32, player.getLocation(), 128);
		
		bPlayer.addCooldown(this);
		remove();
	}
	
	@Override
	public String getName() {
		return "EarthArmor";
	}

	@Override
	public Location getLocation() {
		return headBlockLocation;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}
	
	public boolean isFormed() {
		return formed;
	}

	public void setFormed(boolean formed) {
		this.formed = formed;
	}

	public MaterialData getHeadData() {
		return headData;
	}

	public void setHeadData(MaterialData materialdata) {
		this.headData = materialdata;
	}

	public MaterialData getLegsData() {
		return legsData;
	}

	public void setLegsData(MaterialData materialdata) {
		this.legsData = materialdata;
	}
	
	public double getSelectRange() {
		return selectRange;
	}

	public void setSelectRange(double selectRange) {
		this.selectRange = selectRange;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public Block getHeadBlock() {
		return headBlock;
	}

	public void setHeadBlock(Block headBlock) {
		this.headBlock = headBlock;
	}

	public Block getLegsBlock() {
		return legsBlock;
	}

	public void setLegsBlock(Block legsBlock) {
		this.legsBlock = legsBlock;
	}

	public Location getHeadBlockLocation() {
		return headBlockLocation;
	}

	public void setHeadBlockLocation(Location headBlockLocation) {
		this.headBlockLocation = headBlockLocation;
	}

	public Location getLegsBlockLocation() {
		return legsBlockLocation;
	}

	public void setLegsBlockLocation(Location legsBlockLocation) {
		this.legsBlockLocation = legsBlockLocation;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}
	
	public float getGoldHearts() {
		return goldHearts;
	}
	
	public int getMaxGoldHearts() {
		return maxGoldHearts;
	}
	
	public void setGoldHearts(float goldHearts) {
		this.goldHearts = goldHearts;
	}
	
	public void setMaxGoldHearts(int maxGoldHearts) {
		this.maxGoldHearts = maxGoldHearts;
	}
	
}
