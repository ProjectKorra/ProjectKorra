package com.projectkorra.projectkorra.earthbending;

import java.util.List;

import org.bukkit.Color;
import org.bukkit.GameMode;
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

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempArmor;
import com.projectkorra.projectkorra.util.TempBlock;

public class EarthArmor extends EarthAbility {

	private boolean formed;
	private MaterialData headData;
	private MaterialData legsData;
	private long cooldown;
	private long interval;
	private long maxDuration;
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
		if (hasAbility(player, EarthArmor.class) || !canBend()) {
			return;
		}

		this.formed = false;
		this.active = true;
		this.interval = 2000;
		this.goldHearts = 0;
		this.cooldown = getConfig().getLong("Abilities.Earth.EarthArmor.Cooldown");
		this.maxDuration = getConfig().getLong("Abilities.Earth.EarthArmor.MaxDuration");
		this.selectRange = getConfig().getDouble("Abilities.Earth.EarthArmor.SelectRange");
		this.maxGoldHearts = getConfig().getInt("Abilities.Earth.EarthArmor.GoldHearts");

		if (bPlayer.isAvatarState()) {
			this.cooldown = getConfig().getLong("Abilities.Avatar.AvatarState.Earth.EarthArmor.Cooldown");
			this.maxGoldHearts = getConfig().getInt("Abilities.Avatar.AvatarState.Earth.EarthArmor.GoldHearts");
		}

		headBlock = getTargetEarthBlock((int) selectRange);
		if (!GeneralMethods.isRegionProtectedFromBuild(this, headBlock.getLocation()) && getEarthbendableBlocksLength(headBlock, new Vector(0, -1, 0), 2) >= 2) {
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

	@SuppressWarnings("deprecation")
	@Override
	public void progress() {
		if (!canBend()) {
			remove();
			return;
		}

		if (System.currentTimeMillis() - getStartTime() > maxDuration) {
			player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 2, 1);
			player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 2, 1);
			player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 2, 1);

			ParticleEffect.BLOCK_CRACK.display(new ParticleEffect.BlockData(headData.getItemType(), headData.getData()), 0.1F, 0.1F, 0.1F, 1, 32, player.getEyeLocation(), 128);
			ParticleEffect.BLOCK_CRACK.display(new ParticleEffect.BlockData(legsData.getItemType(), legsData.getData()), 0.1F, 0.1F, 0.1F, 1, 32, player.getLocation(), 128);

			bPlayer.addCooldown(this);
			remove();
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

	/** Returns the color for the specified material. */
	public static int getColor(Material material, byte damage) {
		if (material == Material.DIRT)
			return 0xa86e45; //Default dirt brown
		if (material == Material.GRASS)
			return 0xa86e45; //Default dirt brown
		if (material == Material.MYCEL)
			return 0xa86e45; //Default dirt brown
		if (material == Material.CLAY)
			return 0xBAC2D1; //Dull gray-brown
		if (material == Material.STONE && damage == 0x0)
			return 0x9e9e9e; //Gray
		if (material == Material.STONE && (damage == 0x1 || damage == 0x2))
			return 0xc69489; //Pink
		if (material == Material.STONE && (damage == 0x3 || damage == 0x4))
			return 0xe3e3e5; //White
		if (material == Material.STONE && (damage == 0x5 || damage == 0x6))
			return 0xa3a3a3; //Gray
		if (material == Material.COBBLESTONE)
			return 0x6B6B6B; //Dark Gray
		if (material == Material.SAND && damage == 0x0)
			return 0xffffaf; //Sand yellow
		if (material == Material.SAND && damage == 0x1)
			return 0xb85f25; //Sand orange
		if (material == Material.SANDSTONE)
			return 0xffffaf; //Sand
		if (material == Material.RED_SANDSTONE)
			return 0xbc5a1a; //Red sandstone
		if (material == Material.GRAVEL)
			return 0xaaa49e; //Dark Gray 
		if (material == Material.GOLD_ORE)
			return 0xa2a38f; //Gray-yellow
		if (material == Material.GOLD_BLOCK)
			return 0xF2F204; //Gold - Could be a tiny bit darker
		if (material == Material.IRON_ORE)
			return 0xa39d91; //Gray-brown
		if (material == Material.IRON_BLOCK)
			return 0xf4f4f4; //Silver/Gray
		if (material == Material.COAL_ORE)
			return 0x7c7c7c; //Stone gray
		if (material == Material.LAPIS_ORE)
			return 0x9198a3; //Gray-azure
		if (material == Material.LAPIS_BLOCK)
			return 0x0060BA; //Dark blue
		if (material == Material.DIAMOND_ORE)
			return 0xa8bebf; //Gray-cyan
		if (material == Material.NETHERRACK)
			return 0x9b3131; //Pinkish-red
		if (material == Material.QUARTZ_ORE)
			return 0xb75656; //Pinkish-red
		if (material == Material.QUARTZ_BLOCK)
			return 0xfff4f4; //White
		if (material == Material.STAINED_CLAY && damage == 0x0)
			return 0xCFAFA0; //White Stained Clay
		if (material == Material.STAINED_CLAY && damage == 0x1)
			return 0xA75329; //Orange
		if (material == Material.STAINED_CLAY && damage == 0x2)
			return 0x95596E; //Magenta
		if (material == Material.STAINED_CLAY && damage == 0x3)
			return 0x736E8A; //Light blue
		if (material == Material.STAINED_CLAY && damage == 0x4)
			return 0xBA8825; //Yellow
		if (material == Material.STAINED_CLAY && damage == 0x5)
			return 0x6B7736; //Lime
		if (material == Material.STAINED_CLAY && damage == 0x6)
			return 0xA24D4F; //Pink
		if (material == Material.STAINED_CLAY && damage == 0x7)
			return 0x3A2923; //Gray
		if (material == Material.STAINED_CLAY && damage == 0x8)
			return 0x876A61; //Light Gray
		if (material == Material.STAINED_CLAY && damage == 0x9)
			return 0x575B5B; //Cyan
		if (material == Material.STAINED_CLAY && damage == 0xA)
			return 0x734453; //Purple
		if (material == Material.STAINED_CLAY && damage == 0xB)
			return 0x493A5A; //Blue
		if (material == Material.STAINED_CLAY && damage == 0xC)
			return 0x4C3223; //Brown
		if (material == Material.STAINED_CLAY && damage == 0xD)
			return 0x4B522A; //Green
		if (material == Material.STAINED_CLAY && damage == 0xE)
			return 0x8D3B2E; //Red
		if (material == Material.STAINED_CLAY && damage == 0xF)
			return 0x251610; //Black

		return 0x9e9e9e; //Stone
	}

	@SuppressWarnings("deprecation")
	public void click() {
		if (!this.player.isSneaking())
			return;

		player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 2, 1);
		player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 2, 1);
		player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 2, 1);

		ParticleEffect.BLOCK_CRACK.display(new ParticleEffect.BlockData(headData.getItemType(), headData.getData()), 0.1F, 0.1F, 0.1F, 1, 32, player.getEyeLocation(), 128);
		ParticleEffect.BLOCK_CRACK.display(new ParticleEffect.BlockData(legsData.getItemType(), legsData.getData()), 0.1F, 0.1F, 0.1F, 1, 32, player.getLocation(), 128);

		bPlayer.addCooldown(this);
		remove();
	}
	
	private boolean canBend() {

		List<String> disabledWorlds = getConfig().getStringList("Properties.DisabledWorlds");
		Location playerLoc = player.getLocation();

		if (!player.isOnline() || player.isDead()) {
			return false;
		} else if (!bPlayer.canBind(this)) { 
			return false; 
		} else if (this.getPlayer() != null && this.getLocation() != null && !this.getLocation().getWorld().equals(player.getWorld())) {
			return false;
		} else if (disabledWorlds != null && disabledWorlds.contains(player.getWorld().getName())) {
			return false;
		} else if (Commands.isToggledForAll || !bPlayer.isToggled() || !bPlayer.isElementToggled(this.getElement())) {
			return false;
		} else if (player.getGameMode() == GameMode.SPECTATOR) {
			return false;
		}

		if (GeneralMethods.isRegionProtectedFromBuild(player, this.getName(), playerLoc)) {
			return false;
		}
		
		return true;
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
