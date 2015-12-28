package com.projectkorra.projectkorra.waterbending;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.multiability.MultiAbilityManager;
import com.projectkorra.projectkorra.earthbending.EarthMethods;
import com.projectkorra.projectkorra.firebending.FireMethods;
import com.projectkorra.projectkorra.firebending.Lightning;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.WaterArmsWhip.Whip;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class WaterArms {

	/**
	 * Arm Enum value for deciding which arm is being used.
	 */
	public enum Arm {
		Right, Left;
	}

	private static FileConfiguration config = ProjectKorra.plugin.getConfig();

	public static ConcurrentHashMap<Player, WaterArms> instances = new ConcurrentHashMap<Player, WaterArms>();
	public static ConcurrentHashMap<Block, Long> revert = new ConcurrentHashMap<Block, Long>();

	private static Integer[] unbreakable = { 7, 8, 9, 10, 11, 49, 54, 90, 119, 120, 130, 146 };

	private Player player;
	private World world;

	private Arm activeArm = Arm.Right;

	private boolean cooldownLeft;
	private boolean cooldownRight;
	private boolean fullSource = true;

	private boolean leftArmConsumed = false;
	private boolean rightArmConsumed = false;

	private int lengthReduction = 0;

	private int initLength = config.getInt("Abilities.Water.WaterArms.Arms.InitialLength");
	private int sourceGrabRange = config.getInt("Abilities.Water.WaterArms.Arms.SourceGrabRange");
	private int maxPunches = config.getInt("Abilities.Water.WaterArms.Arms.MaxAttacks");
	private int maxIceBlasts = config.getInt("Abilities.Water.WaterArms.Arms.MaxIceShots");
	private int maxUses = config.getInt("Abilities.Water.WaterArms.Arms.MaxAlternateUsage");
	private long cooldown = config.getLong("Abilities.Water.WaterArms.Arms.Cooldown");
	private boolean canUsePlantSource = config.getBoolean("Abilities.Water.WaterArms.Arms.AllowPlantSource");

	private boolean lightningEnabled = config.getBoolean("Abilities.Water.WaterArms.Arms.Lightning.Enabled");
	private double lightningDamage = config.getDouble("Abilities.Water.WaterArms.Arms.Lightning.Damage");
	private boolean lightningKill = config.getBoolean("Abilities.Water.WaterArms.Arms.Lightning.KillUser");

	private static String sneakMsg = config.getString("Abilities.Water.WaterArms.SneakMessage");

	private int selectedSlot = 0;
	private int freezeSlot = 4;

	private long lastClickTime;

	public WaterArms(Player player) {
		if (instances.containsKey(player)) {
			if (player.isSneaking()) {
				instances.get(player).prepareCancel();
			} else {
				switch (player.getInventory().getHeldItemSlot()) {
					case 0:
						if (player.hasPermission("bending.ability.WaterArms.Pull")) {
							new WaterArmsWhip(player, Whip.Pull);
						}
						break;
					case 1:
						if (player.hasPermission("bending.ability.WaterArms.Punch")) {
							new WaterArmsWhip(player, Whip.Punch);
						}
						break;
					case 2:
						if (player.hasPermission("bending.ability.WaterArms.Grapple")) {
							new WaterArmsWhip(player, Whip.Grapple);
						}
						break;
					case 3:
						if (player.hasPermission("bending.ability.WaterArms.Grab")) {
							new WaterArmsWhip(player, Whip.Grab);
						}
						break;
					case 4:
						if (player.hasPermission("bending.ability.WaterArms.Freeze") && WaterMethods.canIcebend(player)) {
							new WaterArmsFreeze(player);
						}
						break;
					case 5:
						if (player.hasPermission("bending.ability.WaterArms.Spear")) {
							if (WaterMethods.canIcebend(player)) {
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
		this.player = player;
		if (canUse(player) && prepare()) {
			world = player.getWorld();
			instances.put(player, this);
			MultiAbilityManager.bindMultiAbility(player, "WaterArms");
			if (ChatColor.stripColor(GeneralMethods.getBoundAbility(player)) == null) {
				remove();
				return;
			}
			player.sendMessage(WaterMethods.getWaterColor() + sneakMsg + " " + GeneralMethods.getBoundAbility(player));
		}
	}

	private boolean canUse(Player player) {
		if (GeneralMethods.getBoundAbility(player) == null)
			return false;
		if (!GeneralMethods.canBend(player.getName(), "WaterArms"))
			return false;
		if (GeneralMethods.isRegionProtectedFromBuild(player, "WaterArms", player.getLocation()))
			return false;
		if (GeneralMethods.getBendingPlayer(player.getName()).isOnCooldown("WaterArms"))
			return false;
		if (GeneralMethods.getBoundAbility(player).equalsIgnoreCase("WaterArms"))
			return true;
		return false;
	}

	private boolean prepare() {
		Block sourceBlock = WaterMethods.getWaterSourceBlock(player, sourceGrabRange, true, WaterMethods.canPlantbend(player), canUsePlantSource && WaterMethods.canPlantbend(player));
		if (sourceBlock != null) {
			if (WaterMethods.isPlant(sourceBlock)) {
				fullSource = false;
			}
			ParticleEffect.LARGE_SMOKE.display(WaterMethods.getWaterSourceBlock(player, sourceGrabRange, true, WaterMethods.canPlantbend(player), canUsePlantSource && WaterMethods.canPlantbend(player)).getLocation().clone().add(0.5, 0.5, 0.5), 0, 0, 0, 0F, 4);
			return true;
		} else if (WaterReturn.hasWaterBottle(player)) {
			WaterReturn.emptyWaterBottle(player);
			fullSource = false;
			return true;
		}
		return false;
	}

	private void progress() {
		if (!instances.containsKey(player)) {
			return;
		}
		if (player.isDead() || !player.isOnline() || !world.equals(player.getWorld())) {
			remove();
			return;
		}
		if (!GeneralMethods.getBendingPlayer(player.getName()).isToggled()) {
			remove();
			return;
		}
		if (!MultiAbilityManager.hasMultiAbilityBound(player, "WaterArms")) {
			remove();
			return;
		}
		if (maxPunches == 0 || maxUses == 0 || maxIceBlasts == 0 || (leftArmConsumed && rightArmConsumed)) {
			remove();
			return;
		}

		selectedSlot = player.getInventory().getHeldItemSlot();
		displayRightArm();
		displayLeftArm();

		if (lightningEnabled)
			checkIfZapped();
	}

	private boolean canPlaceBlock(Block block) {
		if (!EarthMethods.isTransparentToEarthbending(player, block) && !(WaterMethods.isWater(block) && TempBlock.isTempBlock(block)))
			return false;
		return true;
	}

	/**
	 * Displays the right arm. Returns false if the arm cannot be fully
	 * displayed.
	 * 
	 * @return false If arm cannot be fully displayed
	 */
	public boolean displayRightArm() {
		if (rightArmConsumed)
			return false;

		Location r1 = GeneralMethods.getRightSide(player.getLocation(), 1).add(0, 1.5, 0);
		if (!canPlaceBlock(r1.getBlock()))
			return false;

		if (!(getRightHandPos().getBlock().getLocation().equals(r1.getBlock().getLocation()))) {
			new TempBlock(r1.getBlock(), Material.STATIONARY_WATER, (byte) 5);
			revert.put(r1.getBlock(), 0L);
		}

		Location r2 = GeneralMethods.getRightSide(player.getLocation(), 2).add(0, 1.5, 0);
		if (!canPlaceBlock(r2.getBlock()))
			return false;

		new TempBlock(r2.getBlock(), Material.STATIONARY_WATER, (byte) 8);
		revert.put(r2.getBlock(), 0L);

		for (int j = 0; j <= initLength; j++) {
			Location r3 = r2.clone().toVector().add(player.getLocation().clone().getDirection().multiply(j)).toLocation(player.getWorld());
			if (!canPlaceBlock(r3.getBlock())) {
				if (selectedSlot == freezeSlot && r3.getBlock().getType().equals(Material.ICE))
					continue;
				return false;
			}

			if (j >= 1 && selectedSlot == freezeSlot && WaterMethods.canIcebend(player)) {
				new TempBlock(r3.getBlock(), Material.ICE, (byte) 0);
				revert.put(r3.getBlock(), 0L);
			} else {
				new TempBlock(r3.getBlock(), Material.STATIONARY_WATER, (byte) 8);
				revert.put(r3.getBlock(), 0L);
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
		if (leftArmConsumed)
			return false;

		Location l1 = GeneralMethods.getLeftSide(player.getLocation(), 1).add(0, 1.5, 0);
		if (!canPlaceBlock(l1.getBlock()))
			return false;

		if (!(getLeftHandPos().getBlock().getLocation().equals(l1.getBlock().getLocation()))) {
			new TempBlock(l1.getBlock(), Material.STATIONARY_WATER, (byte) 5);
			revert.put(l1.getBlock(), 0L);
		}

		Location l2 = GeneralMethods.getLeftSide(player.getLocation(), 2).add(0, 1.5, 0);
		if (!canPlaceBlock(l2.getBlock()))
			return false;

		new TempBlock(l2.getBlock(), Material.STATIONARY_WATER, (byte) 8);
		revert.put(l2.getBlock(), 0L);

		for (int j = 0; j <= initLength; j++) {
			Location l3 = l2.clone().toVector().add(player.getLocation().clone().getDirection().multiply(j)).toLocation(player.getWorld());
			if (!canPlaceBlock(l3.getBlock())) {
				if (selectedSlot == freezeSlot && l3.getBlock().getType().equals(Material.ICE))
					continue;
				return false;
			}

			if (j >= 1 && selectedSlot == freezeSlot && WaterMethods.canIcebend(player)) {
				new TempBlock(l3.getBlock(), Material.ICE, (byte) 0);
				revert.put(l3.getBlock(), 0L);
			} else {
				new TempBlock(l3.getBlock(), Material.STATIONARY_WATER, (byte) 8);
				revert.put(l3.getBlock(), 0L);
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
		for (Block block : revert.keySet()) {
			long time = revert.get(block);
			if (System.currentTimeMillis() > time || ignoreTime) {
				if (TempBlock.isTempBlock(block))
					TempBlock.revertBlock(block, Material.AIR);
				revert.remove(block);
			}
		}
	}

	private void checkIfZapped() {
		for (Lightning l : Lightning.instances.values()) {
			for (Lightning.Arc arc : l.getArcs()) {
				for (Block arm : revert.keySet()) {
					for (Location loc : arc.getPoints()) {
						if (arm.getLocation().getWorld() == loc.getWorld() && loc.distance(arm.getLocation()) <= 2.5) {
							for (Location l1 : getOffsetLocations(4, arm.getLocation(), 1.25))
								FireMethods.playLightningbendingParticle(l1);
							if (lightningKill)
								GeneralMethods.damageEntity(l.getPlayer(), player, 60D, Element.Water, "Electrocution");
							else
								GeneralMethods.damageEntity(l.getPlayer(), player, lightningDamage, Element.Water, "Electrocution");
						}
					}
				}
			}
		}
	}

	private static List<Location> getOffsetLocations(int amount, Location location, double offset) {
		List<Location> locations = new ArrayList<Location>();
		for (int i = 0; i < amount; i++)
			locations.add(location.clone().add((float) (Math.random() * offset), (float) (Math.random() * offset), (float) (Math.random() * offset)));
		return locations;
	}

	public static void remove(Player player) {
		if (instances.containsKey(player))
			instances.get(player).remove();
	}

	public void remove() {
		MultiAbilityManager.unbindMultiAbility(player);
		if (player.isOnline())
			GeneralMethods.getBendingPlayer(player.getName()).addCooldown("WaterArms", cooldown);
		instances.remove(player);
	}

	public void prepareCancel() {
		if (System.currentTimeMillis() < lastClickTime + 500L) {
			remove();
		} else {
			lastClickTime = System.currentTimeMillis();
		}
	}

	public static void progressAll() {
		progressRevert(false);
		for (Player p : instances.keySet())
			instances.get(p).progress();
		WaterArmsWhip.progressAll();
		WaterArmsFreeze.progressAll();
		WaterArmsSpear.progressAll();
	}

	public static void removeAll() {
		progressRevert(true);
		revert.clear();
		instances.clear();
		WaterArmsWhip.removeAll();
		WaterArmsFreeze.removeAll();
		WaterArmsSpear.removeAll();
	}

	@SuppressWarnings("deprecation")
	public static boolean isUnbreakable(Block block) {
		if (Arrays.asList(unbreakable).contains(block.getTypeId()))
			return true;
		return false;
	}

	public static void displayBoundMsg(Player player) {
		player.sendMessage(WaterMethods.getWaterColor() + sneakMsg + " " + GeneralMethods.getBoundAbility(player));
	}

	public void displayBoundMsg() {
		player.sendMessage(WaterMethods.getWaterColor() + sneakMsg + " " + GeneralMethods.getBoundAbility(player));
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
		if (activeArm.equals(Arm.Right))
			activeArm = Arm.Left;
		else
			activeArm = Arm.Right;
	}

	/**
	 * Switches to the most suitable arm for the player.
	 * 
	 * @return the {@link Arm} that was swapped to
	 */
	public Arm switchPreferredArm() {
		switchActiveArm();
		if (activeArm.equals(Arm.Left)) {
			if (!displayLeftArm()) {
				switchActiveArm();
			}
		}
		if (activeArm.equals(Arm.Right)) {
			if (!displayRightArm()) {
				switchActiveArm();
			}
		}
		return getActiveArm();
	}

	public boolean canDisplayActiveArm() {
		switch (activeArm) {
			case Left:
				return displayLeftArm();
			case Right:
				return displayRightArm();
			default:
				return false;
		}
	}

	public Location getActiveArmEnd() {
		switch (activeArm) {
			case Left:
				return getLeftArmEnd();
			case Right:
				return getRightArmEnd();
			default:
				return null;
		}
	}

	public static boolean hasPlayer(Player player) {
		if (instances.containsKey(player)) {
			return true;
		}
		return false;
	}

	public Player getPlayer() {
		return player;
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
			case Left:
				setLeftArmCooldown(cooldown);
				return;
			case Right:
				setRightArmCooldown(cooldown);
				return;
			default:
				break;
		}
	}

	public long getCooldown() {
		return cooldown;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}
}
