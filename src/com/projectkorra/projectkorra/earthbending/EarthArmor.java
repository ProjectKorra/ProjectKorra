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
	private TempArmor armor;

	public EarthArmor(final Player player) {
		super(player);
		if (hasAbility(player, EarthArmor.class) || !this.canBend()) {
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

		if (this.bPlayer.isAvatarState()) {
			this.cooldown = getConfig().getLong("Abilities.Avatar.AvatarState.Earth.EarthArmor.Cooldown");
			this.maxGoldHearts = getConfig().getInt("Abilities.Avatar.AvatarState.Earth.EarthArmor.GoldHearts");
		}

		this.headBlock = this.getTargetEarthBlock((int) this.selectRange);
		if (!GeneralMethods.isRegionProtectedFromBuild(this, this.headBlock.getLocation()) && this.getEarthbendableBlocksLength(this.headBlock, new Vector(0, -1, 0), 2) >= 2) {
			this.legsBlock = this.headBlock.getRelative(BlockFace.DOWN);
			this.headData = this.headBlock.getState().getData();
			this.legsData = this.legsBlock.getState().getData();
			this.headBlockLocation = this.headBlock.getLocation();
			this.legsBlockLocation = this.legsBlock.getLocation();

			final Block oldHeadBlock = this.headBlock;
			final Block oldLegsBlock = this.legsBlock;

			if (!this.moveBlocks()) {
				return;
			}
			if (isEarthRevertOn()) {
				addTempAirBlock(oldHeadBlock);
				addTempAirBlock(oldLegsBlock);
			} else {
				GeneralMethods.removeBlock(oldHeadBlock);
				GeneralMethods.removeBlock(oldLegsBlock);
			}

			playEarthbendingSound(this.headBlock.getLocation());
			this.start();
		}
	}

	private void formArmor() {
		if (TempBlock.isTempBlock(this.headBlock)) {
			TempBlock.revertBlock(this.headBlock, Material.AIR);
		}
		if (TempBlock.isTempBlock(this.legsBlock)) {
			TempBlock.revertBlock(this.legsBlock, Material.AIR);
		}

		final ItemStack head = new ItemStack(Material.LEATHER_HELMET, 1);
		final ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
		final ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS, 1);
		final ItemStack boots = new ItemStack(Material.LEATHER_BOOTS, 1);

		final LeatherArmorMeta metaHead = (LeatherArmorMeta) head.getItemMeta();
		final LeatherArmorMeta metaChest = (LeatherArmorMeta) chestplate.getItemMeta();
		final LeatherArmorMeta metaLegs = (LeatherArmorMeta) leggings.getItemMeta();
		final LeatherArmorMeta metaBottom = (LeatherArmorMeta) boots.getItemMeta();

		metaHead.setColor(Color.fromRGB(getColor(this.headData.getItemType(), this.headData.getData())));
		metaChest.setColor(Color.fromRGB(getColor(this.headData.getItemType(), this.headData.getData())));
		metaLegs.setColor(Color.fromRGB(getColor(this.legsData.getItemType(), this.legsData.getData())));
		metaBottom.setColor(Color.fromRGB(getColor(this.legsData.getItemType(), this.legsData.getData())));

		head.setItemMeta(metaHead);
		chestplate.setItemMeta(metaChest);
		leggings.setItemMeta(metaLegs);
		boots.setItemMeta(metaBottom);

		final ItemStack armors[] = { boots, leggings, chestplate, head };
		this.armor = new TempArmor(this.player, 72000000L, this, armors); // Duration of 2 hours.
		this.armor.setRemovesAbilityOnForceRevert(true);
		this.formed = true;

		for (final PotionEffect effect : this.player.getActivePotionEffects()) {
			if (effect.getType() == PotionEffectType.ABSORPTION) {
				this.oldAbsorbtion = effect;
				this.player.removePotionEffect(PotionEffectType.ABSORPTION);
				break;
			}
		}
		final int level = this.maxGoldHearts / 2 - 1 + (this.maxGoldHearts % 2);
		this.player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, Integer.MAX_VALUE, level, true, false));

		this.goldHearts = this.maxGoldHearts * 2;
		GeneralMethods.setAbsorbationHealth(this.player, this.goldHearts);
	}

	private boolean inPosition() {
		return this.headBlock.equals(this.player.getEyeLocation().getBlock()) && this.legsBlock.equals(this.player.getLocation().getBlock());
	}

	private boolean moveBlocks() {
		if (!this.player.getWorld().equals(this.headBlock.getWorld())) {
			this.remove();
			return false;
		}

		final Location headLocation = this.player.getEyeLocation();
		final Location legsLocation = this.player.getLocation();
		Vector headDirection = headLocation.toVector().subtract(this.headBlockLocation.toVector()).normalize().multiply(.5);

		Block newHeadBlock = this.headBlock;
		Block newLegsBlock = this.legsBlock;

		final int yDiff = this.player.getEyeLocation().getBlockY() - this.headBlock.getY();

		if (yDiff != 0) {
			final Block checkBlock = yDiff > 0 ? this.headBlock.getRelative(BlockFace.UP) : this.legsBlock.getRelative(BlockFace.DOWN);

			if (this.isTransparent(checkBlock) && !checkBlock.isLiquid()) {
				GeneralMethods.breakBlock(checkBlock); // Destroy any minor blocks that are in the way.

				headDirection = new Vector(0, yDiff > 0 ? 0.5 : -0.5, 0);
			}
		}

		if (!headLocation.getBlock().equals(this.headBlock)) {
			this.headBlockLocation = this.headBlockLocation.clone().add(headDirection);
			newHeadBlock = this.headBlockLocation.getBlock();
		}
		if (!legsLocation.getBlock().equals(this.legsBlock)) {
			this.legsBlockLocation = this.headBlockLocation.clone().add(0, -1, 0);
			newLegsBlock = newHeadBlock.getRelative(BlockFace.DOWN);
		}

		if (this.isTransparent(newHeadBlock) && !newHeadBlock.isLiquid()) {
			GeneralMethods.breakBlock(newHeadBlock);
		} else if (!this.isEarthbendable(newHeadBlock) && !newHeadBlock.isLiquid() && newHeadBlock.getType() != Material.AIR) {
			ParticleEffect.BLOCK_CRACK.display(new ParticleEffect.BlockData(this.headData.getItemType(), this.headData.getData()), 0.5F, 0.5F, 0.5F, 1, 32, newLegsBlock.getLocation(), 128);
			this.remove();
			return false;
		}

		if (this.isTransparent(newLegsBlock) && !newLegsBlock.isLiquid()) {
			GeneralMethods.breakBlock(newLegsBlock);
		} else if (!this.isEarthbendable(newLegsBlock) && !newLegsBlock.isLiquid() && newLegsBlock.getType() != Material.AIR) {
			newLegsBlock.getLocation().getWorld().playSound(newLegsBlock.getLocation(), Sound.BLOCK_GRASS_BREAK, 1, 1);
			ParticleEffect.BLOCK_CRACK.display(new ParticleEffect.BlockData(this.legsData.getItemType(), this.legsData.getData()), 0.5F, 0.5F, 0.5F, 1, 32, newLegsBlock.getLocation(), 128);
			this.remove();
			return false;
		}

		if (this.headBlock.getLocation().distanceSquared(this.player.getEyeLocation()) > this.selectRange * this.selectRange) {
			this.remove();
			return false;
		}

		if (!newHeadBlock.equals(this.headBlock)) {
			new TempBlock(newHeadBlock, this.headData.getItemType(), this.headData.getData());
			if (TempBlock.isTempBlock(this.headBlock)) {
				TempBlock.revertBlock(this.headBlock, Material.AIR);
			}
		}

		if (!newLegsBlock.equals(this.legsBlock)) {
			new TempBlock(newLegsBlock, this.legsData.getItemType(), this.legsData.getData());
			if (TempBlock.isTempBlock(this.legsBlock)) {
				TempBlock.revertBlock(this.legsBlock, Material.AIR);
			}
		}
		this.headBlock = newHeadBlock;
		this.legsBlock = newLegsBlock;
		return true;
	}

	@Override
	public void progress() {
		if (!this.canBend()) {
			this.remove();
			return;
		}

		if (System.currentTimeMillis() - this.getStartTime() > this.maxDuration) {
			this.player.getLocation().getWorld().playSound(this.player.getLocation(), Sound.BLOCK_STONE_BREAK, 2, 1);
			this.player.getLocation().getWorld().playSound(this.player.getLocation(), Sound.BLOCK_STONE_BREAK, 2, 1);
			this.player.getLocation().getWorld().playSound(this.player.getLocation(), Sound.BLOCK_STONE_BREAK, 2, 1);

			ParticleEffect.BLOCK_CRACK.display(new ParticleEffect.BlockData(this.headData.getItemType(), this.headData.getData()), 0.1F, 0.1F, 0.1F, 1, 32, this.player.getEyeLocation(), 128);
			ParticleEffect.BLOCK_CRACK.display(new ParticleEffect.BlockData(this.legsData.getItemType(), this.legsData.getData()), 0.1F, 0.1F, 0.1F, 1, 32, this.player.getLocation(), 128);

			this.bPlayer.addCooldown(this);
			this.remove();
			return;
		}

		if (this.formed) {
			if (!this.player.hasPotionEffect(PotionEffectType.ABSORPTION)) {
				this.player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, Integer.MAX_VALUE, 1, true, false));
				GeneralMethods.setAbsorbationHealth(this.player, this.goldHearts);
			}

			if (!this.active) {
				this.bPlayer.addCooldown(this);
				this.remove();
				return;
			}

			this.player.setFireTicks(0);
		} else {
			if (!this.moveBlocks()) {
				return;
			}
			if (this.inPosition()) {
				this.formArmor();
			}
		}
	}

	@Override
	public void remove() {
		super.remove();
		if (isEarthRevertOn()) {
			if (TempBlock.isTempBlock(this.headBlock)) {
				TempBlock.revertBlock(this.headBlock, Material.AIR);
			}
			if (TempBlock.isTempBlock(this.legsBlock)) {
				TempBlock.revertBlock(this.legsBlock, Material.AIR);
			}
		} else {
			this.headBlock.breakNaturally();
			this.legsBlock.breakNaturally();
		}

		if (TempArmor.getTempArmorList(this.player).contains(this.armor)) {
			this.armor.revert();
		}

		this.player.removePotionEffect(PotionEffectType.ABSORPTION);

		if (this.oldAbsorbtion != null) {
			this.player.addPotionEffect(this.oldAbsorbtion);
		}

	}

	public void updateAbsorbtion() {
		final EarthArmor abil = this;
		new BukkitRunnable() {
			@Override
			public void run() {
				EarthArmor.this.goldHearts = GeneralMethods.getAbsorbationHealth(EarthArmor.this.player);
				if (EarthArmor.this.formed && EarthArmor.this.goldHearts < 0.9F) {
					EarthArmor.this.bPlayer.addCooldown(abil);

					EarthArmor.this.player.getLocation().getWorld().playSound(EarthArmor.this.player.getLocation(), Sound.BLOCK_STONE_BREAK, 2, 1);
					EarthArmor.this.player.getLocation().getWorld().playSound(EarthArmor.this.player.getLocation(), Sound.BLOCK_STONE_BREAK, 2, 1);
					EarthArmor.this.player.getLocation().getWorld().playSound(EarthArmor.this.player.getLocation(), Sound.BLOCK_STONE_BREAK, 2, 1);

					ParticleEffect.BLOCK_CRACK.display(new ParticleEffect.BlockData(EarthArmor.this.headData.getItemType(), EarthArmor.this.headData.getData()), 0.1F, 0.1F, 0.1F, 1, 32, EarthArmor.this.player.getEyeLocation(), 128);
					ParticleEffect.BLOCK_CRACK.display(new ParticleEffect.BlockData(EarthArmor.this.legsData.getItemType(), EarthArmor.this.legsData.getData()), 0.1F, 0.1F, 0.1F, 1, 32, EarthArmor.this.player.getLocation(), 128);

					EarthArmor.this.remove();
				}
			}
		}.runTaskLater(ProjectKorra.plugin, 1L);

	}

	public static int getColor(final Material material) {
		return getColor(material, (byte) 0x0);
	}

	/** Returns the color for the specified material. */
	public static int getColor(final Material material, final byte damage) {
		if (material == Material.DIRT) {
			return 0xa86e45; // Default dirt brown.
		}
		if (material == Material.GRASS) {
			return 0xa86e45; // Default dirt brown.
		}
		if (material == Material.MYCEL) {
			return 0xa86e45; // Default dirt brown.
		}
		if (material == Material.CLAY) {
			return 0xBAC2D1; // Dull gray-brown.
		}
		if (material == Material.STONE && damage == 0x0) {
			return 0x9e9e9e; // Gray.
		}
		if (material == Material.STONE && (damage == 0x1 || damage == 0x2)) {
			return 0xc69489; // Pink.
		}
		if (material == Material.STONE && (damage == 0x3 || damage == 0x4)) {
			return 0xe3e3e5; // White.
		}
		if (material == Material.STONE && (damage == 0x5 || damage == 0x6)) {
			return 0xa3a3a3; // Gray.
		}
		if (material == Material.COBBLESTONE) {
			return 0x6B6B6B; // Dark Gray.
		}
		if (material == Material.SAND && damage == 0x0) {
			return 0xffffaf; // Sand yellow.
		}
		if (material == Material.SAND && damage == 0x1) {
			return 0xb85f25; // Sand orange.
		}
		if (material == Material.SANDSTONE) {
			return 0xffffaf; // Sand.
		}
		if (material == Material.RED_SANDSTONE) {
			return 0xbc5a1a; // Red sandstone.
		}
		if (material == Material.GRAVEL) {
			return 0xaaa49e; // Dark Gray.
		}
		if (material == Material.GOLD_ORE) {
			return 0xa2a38f; // Gray-yellow.
		}
		if (material == Material.GOLD_BLOCK) {
			return 0xF2F204; // Gold - Could be a tiny bit darker.
		}
		if (material == Material.IRON_ORE) {
			return 0xa39d91; // Gray-brown.
		}
		if (material == Material.IRON_BLOCK) {
			return 0xf4f4f4; // Silver/Gray.
		}
		if (material == Material.COAL_ORE) {
			return 0x7c7c7c; // Stone gray.
		}
		if (material == Material.LAPIS_ORE) {
			return 0x9198a3; // Gray-azure.
		}
		if (material == Material.LAPIS_BLOCK) {
			return 0x0060BA; // Dark blue.
		}
		if (material == Material.DIAMOND_ORE) {
			return 0xa8bebf; // Gray-cyan.
		}
		if (material == Material.NETHERRACK) {
			return 0x9b3131; // Pinkish-red.
		}
		if (material == Material.QUARTZ_ORE) {
			return 0xb75656; // Pinkish-red.
		}
		if (material == Material.QUARTZ_BLOCK) {
			return 0xfff4f4; // White.
		}
		if (material == Material.STAINED_CLAY && damage == 0x0) {
			return 0xCFAFA0; // White Stained Clay.
		}
		if (material == Material.STAINED_CLAY && damage == 0x1) {
			return 0xA75329; // Orange.
		}
		if (material == Material.STAINED_CLAY && damage == 0x2) {
			return 0x95596E; // Magenta.
		}
		if (material == Material.STAINED_CLAY && damage == 0x3) {
			return 0x736E8A; // Light blue.
		}
		if (material == Material.STAINED_CLAY && damage == 0x4) {
			return 0xBA8825; // Yellow.
		}
		if (material == Material.STAINED_CLAY && damage == 0x5) {
			return 0x6B7736; // Lime.
		}
		if (material == Material.STAINED_CLAY && damage == 0x6) {
			return 0xA24D4F; // Pink.
		}
		if (material == Material.STAINED_CLAY && damage == 0x7) {
			return 0x3A2923; // Gray.
		}
		if (material == Material.STAINED_CLAY && damage == 0x8) {
			return 0x876A61; // Light Gray.
		}
		if (material == Material.STAINED_CLAY && damage == 0x9) {
			return 0x575B5B; // Cyan.
		}
		if (material == Material.STAINED_CLAY && damage == 0xA) {
			return 0x734453; // Purple.
		}
		if (material == Material.STAINED_CLAY && damage == 0xB) {
			return 0x493A5A; // Blue.
		}
		if (material == Material.STAINED_CLAY && damage == 0xC) {
			return 0x4C3223; // Brown.
		}
		if (material == Material.STAINED_CLAY && damage == 0xD) {
			return 0x4B522A; // Green.
		}
		if (material == Material.STAINED_CLAY && damage == 0xE) {
			return 0x8D3B2E; // Red.
		}
		if (material == Material.STAINED_CLAY && damage == 0xF) {
			return 0x251610; // Black.
		}

		return 0x9e9e9e; // Stone.
	}

	public void click() {
		if (!this.player.isSneaking()) {
			return;
		}

		this.player.getLocation().getWorld().playSound(this.player.getLocation(), Sound.BLOCK_STONE_BREAK, 2, 1);
		this.player.getLocation().getWorld().playSound(this.player.getLocation(), Sound.BLOCK_STONE_BREAK, 2, 1);
		this.player.getLocation().getWorld().playSound(this.player.getLocation(), Sound.BLOCK_STONE_BREAK, 2, 1);

		ParticleEffect.BLOCK_CRACK.display(new ParticleEffect.BlockData(this.headData.getItemType(), this.headData.getData()), 0.1F, 0.1F, 0.1F, 1, 32, this.player.getEyeLocation(), 128);
		ParticleEffect.BLOCK_CRACK.display(new ParticleEffect.BlockData(this.legsData.getItemType(), this.legsData.getData()), 0.1F, 0.1F, 0.1F, 1, 32, this.player.getLocation(), 128);

		this.bPlayer.addCooldown(this);
		this.remove();
	}

	private boolean canBend() {

		final List<String> disabledWorlds = getConfig().getStringList("Properties.DisabledWorlds");
		final Location playerLoc = this.player.getLocation();

		if (!this.player.isOnline() || this.player.isDead()) {
			return false;

		} else if (this.bPlayer.isOnCooldown("EarthArmor")) {
			return false;
		} else if (!this.bPlayer.canBind(this)) {
			return false;
		} else if (this.getPlayer() != null && this.getLocation() != null && !this.getLocation().getWorld().equals(this.player.getWorld())) {
			return false;
		} else if (disabledWorlds != null && disabledWorlds.contains(this.player.getWorld().getName())) {
			return false;
		} else if (Commands.isToggledForAll || !this.bPlayer.isToggled() || !this.bPlayer.isElementToggled(this.getElement())) {
			return false;
		} else if (this.player.getGameMode() == GameMode.SPECTATOR) {
			return false;
		}

		if (GeneralMethods.isRegionProtectedFromBuild(this.player, this.getName(), playerLoc)) {
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
		return this.headBlockLocation;
	}

	@Override
	public long getCooldown() {
		return this.cooldown;
	}

	@Override
	public boolean isSneakAbility() {
		return this.player != null;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	public boolean isFormed() {
		return this.formed;
	}

	public void setFormed(final boolean formed) {
		this.formed = formed;
	}

	public MaterialData getHeadData() {
		return this.headData;
	}

	public void setHeadData(final MaterialData materialdata) {
		this.headData = materialdata;
	}

	public MaterialData getLegsData() {
		return this.legsData;
	}

	public void setLegsData(final MaterialData materialdata) {
		this.legsData = materialdata;
	}

	public double getSelectRange() {
		return this.selectRange;
	}

	public void setSelectRange(final double selectRange) {
		this.selectRange = selectRange;
	}

	public long getInterval() {
		return this.interval;
	}

	public void setInterval(final long interval) {
		this.interval = interval;
	}

	public Block getHeadBlock() {
		return this.headBlock;
	}

	public void setHeadBlock(final Block headBlock) {
		this.headBlock = headBlock;
	}

	public Block getLegsBlock() {
		return this.legsBlock;
	}

	public void setLegsBlock(final Block legsBlock) {
		this.legsBlock = legsBlock;
	}

	public Location getHeadBlockLocation() {
		return this.headBlockLocation;
	}

	public void setHeadBlockLocation(final Location headBlockLocation) {
		this.headBlockLocation = headBlockLocation;
	}

	public Location getLegsBlockLocation() {
		return this.legsBlockLocation;
	}

	public void setLegsBlockLocation(final Location legsBlockLocation) {
		this.legsBlockLocation = legsBlockLocation;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

	public float getGoldHearts() {
		return this.goldHearts;
	}

	public int getMaxGoldHearts() {
		return this.maxGoldHearts;
	}

	public void setGoldHearts(final float goldHearts) {
		this.goldHearts = goldHearts;
	}

	public void setMaxGoldHearts(final int maxGoldHearts) {
		this.maxGoldHearts = maxGoldHearts;
	}

}
