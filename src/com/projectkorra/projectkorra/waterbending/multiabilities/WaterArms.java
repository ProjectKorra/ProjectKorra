package com.projectkorra.projectkorra.waterbending.multiabilities;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.firebending.lightning.Lightning;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.multiabilities.WaterArmsWhip.Whip;
import com.projectkorra.projectkorra.waterbending.plant.PlantRegrowth;
import com.projectkorra.projectkorra.waterbending.util.WaterReturn;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WaterArms extends WaterAbility {

	/**
	 * Arm Enum value for deciding which arm is being used.
	 */
	public static enum Arm {
		RIGHT, LEFT;
	}

	private static final Map<Block, Long> BLOCK_REVERT_TIMES = new ConcurrentHashMap<Block, Long>();
	private static final Integer[] UNBREAKABLES = { 7, 10, 11, 49, 54, 90, 119, 120, 130, 146 };

	private boolean cooldownLeft;
	private boolean cooldownRight;
	private boolean fullSource; // used to determine whip length in WhaterArmsWhip
	private boolean leftArmConsumed;
	private boolean rightArmConsumed;
	private boolean canUsePlantSource;
	private boolean lightningEnabled;
	private boolean lightningKill;
	private int lengthReduction;
	private int initLength;
	private int sourceGrabRange;
	private int maxPunches;
	private int maxIceBlasts;
	private int maxUses;
	private int selectedSlot;
	private int freezeSlot;
	private long cooldown;
	private long lastClickTime;
	private double lightningDamage;
	private World world;
	private String sneakMsg;
	private Arm activeArm;

	public WaterArms(Player player) {
		super(player);

		this.fullSource = true;
		this.leftArmConsumed = false;
		this.rightArmConsumed = false;
		this.canUsePlantSource = getConfig().getBoolean("Abilities.Water.WaterArms.Arms.AllowPlantSource");
		this.lightningEnabled = getConfig().getBoolean("Abilities.Water.WaterArms.Arms.Lightning.Enabled");
		this.lightningKill = getConfig().getBoolean("Abilities.Water.WaterArms.Arms.Lightning.KillUser");
		this.initLength = getConfig().getInt("Abilities.Water.WaterArms.Arms.InitialLength");
		this.sourceGrabRange = getConfig().getInt("Abilities.Water.WaterArms.Arms.SourceGrabRange");
		this.maxPunches = getConfig().getInt("Abilities.Water.WaterArms.Arms.MaxAttacks");
		this.maxIceBlasts = getConfig().getInt("Abilities.Water.WaterArms.Arms.MaxIceShots");
		this.maxUses = getConfig().getInt("Abilities.Water.WaterArms.Arms.MaxAlternateUsage");
		this.cooldown = getConfig().getLong("Abilities.Water.WaterArms.Arms.Cooldown");
		this.lightningDamage = getConfig().getDouble("Abilities.Water.WaterArms.Arms.Lightning.Damage");
		this.sneakMsg = ConfigManager.languageConfig.get().getString("Abilities.Water.WaterArms.SneakMessage");
		this.lengthReduction = 0;
		this.selectedSlot = 0;
		this.freezeSlot = 4;
		this.lastClickTime = 0;
		this.world = player.getWorld();
		this.activeArm = Arm.RIGHT;

		WaterArms oldArms = getAbility(player, WaterArms.class);

		if (oldArms != null) {
			if (player.isSneaking()) {
				oldArms.prepareCancel();
			} else {
				
				switch (player.getInventory().getHeldItemSlot()) {
					case 0:
						if (player.hasPermission("bending.ability.WaterArms.Pull")) {
							new WaterArmsWhip(player, Whip.PULL);
						}
						break;
					case 1:
						if (player.hasPermission("bending.ability.WaterArms.Punch")) {
							new WaterArmsWhip(player, Whip.PUNCH);
						}
						break;
					case 2:
						if (player.hasPermission("bending.ability.WaterArms.Grapple")) {
							new WaterArmsWhip(player, Whip.GRAPPLE);
						}
						break;
					case 3:
						if (player.hasPermission("bending.ability.WaterArms.Grab")) {
							new WaterArmsWhip(player, Whip.GRAB);
						}
						break;
					case 4:
						if (player.hasPermission("bending.ability.WaterArms.Freeze") && bPlayer.canIcebend()) {
							new WaterArmsFreeze(player);
						}
						break;
					case 5:
						if (player.hasPermission("bending.ability.WaterArms.Spear")) {
							if (bPlayer.canIcebend()) {
								new WaterArmsSpear(player, true);
							} else {
								new WaterArmsSpear(player, false);
							}
						}
						break;
					default:
						break;
				}
			}
			return;
		}

		if (bPlayer.canBend(this) && prepare()) {
			start();
			MultiAbilityManager.bindMultiAbility(player, "WaterArms");

			if (ChatColor.stripColor(bPlayer.getBoundAbilityName()) == null) {
				remove();
				return;
			}
		}
	}

	private boolean prepare() {
		Block sourceBlock = getWaterSourceBlock(player, sourceGrabRange, canUsePlantSource);
		if (sourceBlock != null) {
			
			if (isPlant(sourceBlock) || isSnow(sourceBlock)) {
				new PlantRegrowth(player, sourceBlock);
				sourceBlock.setType(Material.AIR);
				fullSource = false;
			} 
			
			ParticleEffect.LARGE_SMOKE.display(getWaterSourceBlock(player, sourceGrabRange, canUsePlantSource).getLocation().clone().add(0.5, 0.5, 0.5), 0, 0, 0, 0F, 4);
			return true;
		} else if (WaterReturn.hasWaterBottle(player)) {
			WaterReturn.emptyWaterBottle(player);
			fullSource = false;
			return true;
		}
		return false;
	}

	@Override
	public void progress() {
		if (!world.equals(player.getWorld()) || !bPlayer.canBendIgnoreBindsCooldowns(this) || !bPlayer.hasElement(Element.WATER)) {
			remove();
			return;
		} else if (!bPlayer.isToggled()) {
			remove();
			return;
		} else if (!MultiAbilityManager.hasMultiAbilityBound(player, "WaterArms")) {
			remove();
			return;
		} else if (maxPunches == 0 || maxUses == 0 || maxIceBlasts == 0 || (leftArmConsumed && rightArmConsumed)) {
			remove();
			return;
		}

		selectedSlot = player.getInventory().getHeldItemSlot();
		displayRightArm();
		displayLeftArm();

		if (lightningEnabled) {
			checkIfZapped();
		}
	}

	private boolean canPlaceBlock(Block block) {
		if (!isTransparent(player, block) && !(isWater(block) && TempBlock.isTempBlock(block))) {
			return false;
		}
		return true;
	}

	/**
	 * Displays the right arm. Returns false if the arm cannot be fully
	 * displayed.
	 * 
	 * @return false If arm cannot be fully displayed
	 */
	public boolean displayRightArm() {
		if (rightArmConsumed) {
			return false;
		}

		Location r1 = GeneralMethods.getRightSide(player.getLocation(), 1).add(0, 1.5, 0);
		if (!canPlaceBlock(r1.getBlock())) {
			return false;
		}

		if (!(getRightHandPos().getBlock().getLocation().equals(r1.getBlock().getLocation()))) {
			new TempBlock(r1.getBlock(), Material.STATIONARY_WATER, (byte) 5);
			BLOCK_REVERT_TIMES.put(r1.getBlock(), System.currentTimeMillis() + 1);
		}

		Location r2 = GeneralMethods.getRightSide(player.getLocation(), 2).add(0, 1.5, 0);
		if (!canPlaceBlock(r2.getBlock())) {
			return false;
		}

		new TempBlock(r2.getBlock(), Material.STATIONARY_WATER, (byte) 8);
		BLOCK_REVERT_TIMES.put(r2.getBlock(), 0L);

		for (int j = 0; j <= initLength; j++) {
			Location r3 = r2.clone().toVector().add(player.getLocation().clone().getDirection().multiply(j)).toLocation(player.getWorld());
			if (!canPlaceBlock(r3.getBlock())) {
				if (selectedSlot == freezeSlot && r3.getBlock().getType().equals(Material.ICE)) {
					continue;
				}
				return false;
			}

			if (j >= 1 && selectedSlot == freezeSlot && bPlayer.canIcebend()) {
				new TempBlock(r3.getBlock(), Material.ICE, (byte) 0);
				BLOCK_REVERT_TIMES.put(r3.getBlock(), 0L);
			} else {
				new TempBlock(r3.getBlock(), Material.STATIONARY_WATER, (byte) 8);
				BLOCK_REVERT_TIMES.put(r3.getBlock(), 0L);
			}
		}

		return true;
	}

	/**
	 * Displays the left arm. Returns false if the arm cannot be fully
	 * displayed.
	 * 
	 * @return false If the arm cannot be fully displayed.
	 */
	public boolean displayLeftArm() {
		if (leftArmConsumed) {
			return false;
		}

		Location l1 = GeneralMethods.getLeftSide(player.getLocation(), 1).add(0, 1.5, 0);
		if (!canPlaceBlock(l1.getBlock())) {
			return false;
		}

		if (!(getLeftHandPos().getBlock().getLocation().equals(l1.getBlock().getLocation()))) {
			new TempBlock(l1.getBlock(), Material.STATIONARY_WATER, (byte) 5);
			BLOCK_REVERT_TIMES.put(l1.getBlock(), 0L);
		}

		Location l2 = GeneralMethods.getLeftSide(player.getLocation(), 2).add(0, 1.5, 0);
		if (!canPlaceBlock(l2.getBlock())) {
			return false;
		}

		new TempBlock(l2.getBlock(), Material.STATIONARY_WATER, (byte) 8);
		BLOCK_REVERT_TIMES.put(l2.getBlock(), System.currentTimeMillis() + 1);

		for (int j = 0; j <= initLength; j++) {
			Location l3 = l2.clone().toVector().add(player.getLocation().clone().getDirection().multiply(j)).toLocation(player.getWorld());
			if (!canPlaceBlock(l3.getBlock())) {
				if (selectedSlot == freezeSlot && l3.getBlock().getType().equals(Material.ICE)) {
					continue;
				}
				return false;
			}

			if (j >= 1 && selectedSlot == freezeSlot && bPlayer.canIcebend()) {
				new TempBlock(l3.getBlock(), Material.ICE, (byte) 0);
				BLOCK_REVERT_TIMES.put(l3.getBlock(), System.currentTimeMillis() + 1);
			} else {
				new TempBlock(l3.getBlock(), Material.STATIONARY_WATER, (byte) 8);
				BLOCK_REVERT_TIMES.put(l3.getBlock(), System.currentTimeMillis() + 1);
			}
		}

		return true;
	}

	/**
	 * Calculate roughly where the player's right hand is.
	 * 
	 * @return location of right hand
	 */
	private Location getRightHandPos() {
		return GeneralMethods.getRightSide(player.getLocation(), .34).add(0, 1.5, 0);
	}

	/**
	 * Calculate roughly where the player's left hand is.
	 * 
	 * @return location of left hand
	 */
	private Location getLeftHandPos() {
		return GeneralMethods.getLeftSide(player.getLocation(), .34).add(0, 1.5, 0);
	}

	/**
	 * Returns the location of the tip of the right arm, assuming it is fully
	 * extended. Use the displayRightArm() check to see if it is fully extended.
	 * 
	 * @return location of the tip of the right arm
	 */
	public Location getRightArmEnd() {
		Location r1 = GeneralMethods.getRightSide(player.getLocation(), 2).add(0, 1.5, 0);
		return r1.clone().add(player.getLocation().getDirection().normalize().multiply(initLength));
	}

	/**
	 * Returns the location of the tip of the left arm assuming it is fully
	 * extended. Use the displayLeftArm() check to see if it is fully extended.
	 * 
	 * @return location of the tip of the left arm
	 */
	public Location getLeftArmEnd() {
		Location l1 = GeneralMethods.getLeftSide(player.getLocation(), 2).add(0, 1.5, 0);
		return l1.clone().add(player.getLocation().getDirection().normalize().multiply(initLength));
	}

	private static void progressRevert(boolean ignoreTime) {
		for (Block block : BLOCK_REVERT_TIMES.keySet()) {
			long time = BLOCK_REVERT_TIMES.get(block);
			if (System.currentTimeMillis() > time || ignoreTime) {
				if (TempBlock.isTempBlock(block)) {
					TempBlock.revertBlock(block, Material.AIR);
				}
				BLOCK_REVERT_TIMES.remove(block);
			}
		}

		for (Block block : WaterArmsSpear.getIceBlocks().keySet()) {
			long time = WaterArmsSpear.getIceBlocks().get(block);
			if (System.currentTimeMillis() > time || ignoreTime) {
				if (TempBlock.isTempBlock(block)) {
					TempBlock.revertBlock(block, Material.AIR);
				}
				WaterArmsSpear.getIceBlocks().remove(block);
			}
		}
	}

	private void checkIfZapped() {
		for (Lightning lightning : getAbilities(Lightning.class)) {
			for (Lightning.Arc arc : lightning.getArcs()) {
				for (Block arm : BLOCK_REVERT_TIMES.keySet()) {
					for (Location loc : arc.getPoints()) {
						if (arm.getLocation().getWorld().equals(loc.getWorld()) && loc.distance(arm.getLocation()) <= 2.5) {
							for (Location l1 : getOffsetLocations(4, arm.getLocation(), 1.25)) {
								FireAbility.playLightningbendingParticle(l1);
							}
							if (lightningKill) {
								DamageHandler.damageEntity(player, 60D, lightning);
							} else {
								DamageHandler.damageEntity(player, lightningDamage, lightning);
							}
						}
					}
				}
			}
		}
	}

	private static List<Location> getOffsetLocations(int amount, Location location, double offset) {
		List<Location> locations = new ArrayList<Location>();
		for (int i = 0; i < amount; i++) {
			locations.add(location.clone().add((float) (Math.random() * offset), (float) (Math.random() * offset), (float) (Math.random() * offset)));
		}
		return locations;
	}

	@Override
	public void remove() {
		super.remove();
		MultiAbilityManager.unbindMultiAbility(player);
		if (player.isOnline()) {
			bPlayer.addCooldown("WaterArms", cooldown);
		}
	}

	public void prepareCancel() {
		if (System.currentTimeMillis() < lastClickTime + 500L) {
			remove();
		} else {
			lastClickTime = System.currentTimeMillis();
		}
	}

	public static void progressAllCleanup() {
		progressRevert(false);
		/*
		 * There is currently a bug where waterArms will display the arms and
		 * then progressRevert will revert the same blocks in the same tick
		 * before the user is able to see them, thus causing invisible arms.
		 * Simple fix is just to display the arms again.
		 */
		for (WaterArms waterArms : getAbilities(WaterArms.class)) {
			waterArms.displayLeftArm();
			waterArms.displayRightArm();
		}
		WaterArmsWhip.progressAllCleanup();
	}

	public static void removeAllCleanup() {
		progressRevert(true);
		BLOCK_REVERT_TIMES.clear();
		WaterArmsSpear.getIceBlocks().clear();
		WaterArmsWhip.removeAllCleanup();
	}

	@SuppressWarnings("deprecation")
	public static boolean isUnbreakable(Block block) {
		if (Arrays.asList(UNBREAKABLES).contains(block.getTypeId())) {
			return true;
		}
		return false;
	}

	public void displayBoundMsg(int slot) {
		String name = bPlayer.getAbilities().get(slot);
		if (name != null) {
			player.sendMessage(getElement().getColor() + sneakMsg + " " + name);
		}
	}

	/**
	 * Returns the active arm of the player.
	 * 
	 * @return {@link Arm} of the player
	 */
	public Arm getActiveArm() {
		return activeArm;
	}

	/**
	 * Switches the active arm of a player.
	 */
	public void switchActiveArm() {
		if (activeArm.equals(Arm.RIGHT)) {
			activeArm = Arm.LEFT;
		} else {
			activeArm = Arm.RIGHT;
		}
	}

	/**
	 * Switches to the most suitable arm for the player.
	 * 
	 * @return the {@link Arm} that was swapped to
	 */
	public Arm switchPreferredArm() {
		switchActiveArm();
		if (activeArm.equals(Arm.LEFT)) {
			if (!displayLeftArm()) {
				switchActiveArm();
			}
		}
		if (activeArm.equals(Arm.RIGHT)) {
			if (!displayRightArm()) {
				switchActiveArm();
			}
		}
		return getActiveArm();
	}

	public boolean canDisplayActiveArm() {
		switch (activeArm) {
			case LEFT:
				return displayLeftArm();
			case RIGHT:
				return displayRightArm();
			default:
				return false;
		}
	}

	public Location getActiveArmEnd() {
		switch (activeArm) {
			case LEFT:
				return getLeftArmEnd();
			case RIGHT:
				return getRightArmEnd();
			default:
				return null;
		}
	}

	public Boolean isFullSource() {
		return fullSource;
	}

	public boolean getLeftArmConsumed() {
		return leftArmConsumed;
	}

	public void setLeftArmConsumed(boolean consumed) {
		this.leftArmConsumed = consumed;
	}

	public boolean getRightArmConsumed() {
		return rightArmConsumed;
	}

	public void setRightArmConsumed(boolean consumed) {
		this.rightArmConsumed = consumed;
	}

	public Integer getLengthReduction() {
		return lengthReduction;
	}

	public void setLengthReduction(int lengthReduction) {
		this.lengthReduction = lengthReduction;
	}

	public Integer getMaxPunches() {
		return maxPunches;
	}

	public void setMaxPunches(int maxPunches) {
		this.maxPunches = maxPunches;
	}

	public Integer getMaxUses() {
		return maxUses;
	}

	public void setMaxUses(int maxUses) {
		this.maxUses = maxUses;
	}

	public Integer getMaxIceBlasts() {
		return maxIceBlasts;
	}

	public void setMaxIceBlasts(int maxIceBlasts) {
		this.maxIceBlasts = maxIceBlasts;
	}

	public boolean canLightningDamage() {
		return lightningEnabled;
	}

	public void setCanLightningDamage(boolean lightningEnabled) {
		this.lightningEnabled = lightningEnabled;
	}

	public double getLightningDamage() {
		return lightningDamage;
	}

	public void setLightningDamage(double lightningDamage) {
		this.lightningDamage = lightningDamage;
	}

	public boolean isLeftArmCooldown() {
		return cooldownLeft;
	}

	public void setLeftArmCooldown(boolean cooldown) {
		this.cooldownLeft = cooldown;
	}

	public boolean isRightArmCooldown() {
		return cooldownRight;
	}

	public void setRightArmCooldown(boolean cooldown) {
		this.cooldownRight = cooldown;
	}

	public void setActiveArmCooldown(boolean cooldown) {
		switch (activeArm) {
			case LEFT:
				setLeftArmCooldown(cooldown);
				return;
			case RIGHT:
				setRightArmCooldown(cooldown);
				return;
			default:
				break;
		}
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	@Override
	public String getName() {
		return "WaterArms";
	}

	@Override
	public Location getLocation() {
		return player != null ? player.getLocation() : null;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	public boolean isCooldownLeft() {
		return cooldownLeft;
	}

	public void setCooldownLeft(boolean cooldownLeft) {
		this.cooldownLeft = cooldownLeft;
	}

	public boolean isCooldownRight() {
		return cooldownRight;
	}

	public void setCooldownRight(boolean cooldownRight) {
		this.cooldownRight = cooldownRight;
	}

	public boolean isCanUsePlantSource() {
		return canUsePlantSource;
	}

	public void setCanUsePlantSource(boolean canUsePlantSource) {
		this.canUsePlantSource = canUsePlantSource;
	}

	public boolean isLightningEnabled() {
		return lightningEnabled;
	}

	public void setLightningEnabled(boolean lightningEnabled) {
		this.lightningEnabled = lightningEnabled;
	}

	public boolean isLightningKill() {
		return lightningKill;
	}

	public void setLightningKill(boolean lightningKill) {
		this.lightningKill = lightningKill;
	}

	public int getInitLength() {
		return initLength;
	}

	public void setInitLength(int initLength) {
		this.initLength = initLength;
	}

	public int getSourceGrabRange() {
		return sourceGrabRange;
	}

	public void setSourceGrabRange(int sourceGrabRange) {
		this.sourceGrabRange = sourceGrabRange;
	}

	public int getSelectedSlot() {
		return selectedSlot;
	}

	public void setSelectedSlot(int selectedSlot) {
		this.selectedSlot = selectedSlot;
	}

	public int getFreezeSlot() {
		return freezeSlot;
	}

	public void setFreezeSlot(int freezeSlot) {
		this.freezeSlot = freezeSlot;
	}

	public long getLastClickTime() {
		return lastClickTime;
	}

	public void setLastClickTime(long lastClickTime) {
		this.lastClickTime = lastClickTime;
	}

	public World getWorld() {
		return world;
	}

	public void setWorld(World world) {
		this.world = world;
	}

	public String getSneakMsg() {
		return sneakMsg;
	}

	public void setSneakMsg(String sneakMsg) {
		this.sneakMsg = sneakMsg;
	}

	public static Map<Block, Long> getBlockRevertTimes() {
		return BLOCK_REVERT_TIMES;
	}

	public static Integer[] getUnbreakables() {
		return UNBREAKABLES;
	}

	public void setFullSource(boolean fullSource) {
		this.fullSource = fullSource;
	}

	public void setActiveArm(Arm activeArm) {
		this.activeArm = activeArm;
	}

}
