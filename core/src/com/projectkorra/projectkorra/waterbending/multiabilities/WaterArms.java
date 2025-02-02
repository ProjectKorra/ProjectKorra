package com.projectkorra.projectkorra.waterbending.multiabilities;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.attribute.markers.DayNightFactor;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.firebending.lightning.Lightning;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.LightManager;
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
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WaterArms extends WaterAbility {

	/**
	 * Arm Enum value for deciding which arm is being used.
	 */
	public enum Arm {
		RIGHT, LEFT;
	}

	private boolean cooldownLeft;
	private boolean cooldownRight;
	private boolean fullSource; // used to determine whip length in WaterArmsWhip.
	private boolean leftArmConsumed;
	private boolean rightArmConsumed;
	@Attribute("CanUsePlantSource")
	private boolean canUsePlantSource;
	@Attribute("CanLightningStrikeArms")
	private boolean lightningEnabled;
	@Attribute("LightningOneHitKO")
	private boolean lightningKill;
	private int lengthReduction;
	@Attribute("InitialLength")
	private int initLength;
	@Attribute(Attribute.SELECT_RANGE)
	private int sourceGrabRange;
	@Attribute("MaxPunches")
	private int maxPunches;
	@Attribute("MaxIceBlasts")
	private int maxIceBlasts;
	@Attribute("MaxUses") @DayNightFactor
	private int maxUses;
	private int selectedSlot;
	private int freezeSlot;
	@Attribute(Attribute.COOLDOWN) @DayNightFactor(invert = true)
	private long cooldown;
	private long lastClickTime;
	@Attribute("LightningDamage")
	private double lightningDamage;
	private World world;
	private String sneakMsg;
	private Arm activeArm;
	private List<Block> right, left;
	private Set<TempBlock> external;

	public WaterArms(final Player player) {
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
		this.right = new ArrayList<>();
		this.left = new ArrayList<>();
		this.external = new HashSet<>();

		final WaterArms oldArms = getAbility(player, WaterArms.class);

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
						if (player.hasPermission("bending.ability.WaterArms.Freeze") && this.bPlayer.canIcebend()) {
							new WaterArmsFreeze(player);
						}
						break;
					case 5:
						if (player.hasPermission("bending.ability.WaterArms.Spear")) {
							if (this.bPlayer.canIcebend()) {
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

		if (this.bPlayer.canBend(this) && this.prepare()) {
			this.start();
			MultiAbilityManager.bindMultiAbility(player, "WaterArms");

			if (ChatColor.stripColor(this.bPlayer.getBoundAbilityName()) == null) {
				this.remove();
				return;
			}
		}
	}

	private boolean prepare() {
		final Block sourceBlock = getWaterSourceBlock(this.player, this.sourceGrabRange, this.canUsePlantSource);
		if (sourceBlock != null) {

			if (isPlant(sourceBlock) || isSnow(sourceBlock)) {
				new PlantRegrowth(this.player, sourceBlock);
				sourceBlock.setType(Material.AIR);
				this.fullSource = false;
			} else if (isCauldron(sourceBlock) || isTransformableBlock(sourceBlock)) {
				updateSourceBlock(sourceBlock);
			}

			ParticleEffect.SMOKE_LARGE.display(sourceBlock.getLocation().clone().add(0.5, 0.5, 0.5), 4, 0, 0, 0);
			return true;
		} else if (WaterReturn.hasWaterBottle(this.player)) {
			WaterReturn.emptyWaterBottle(this.player);
			this.fullSource = false;
			return true;
		}
		return false;
	}

	@Override
	public void progress() {
		if (!this.world.equals(this.player.getWorld()) || !this.bPlayer.canBendIgnoreBindsCooldowns(this) || !this.bPlayer.hasElement(Element.WATER)) {
			this.remove();
			return;
		} else if (!this.bPlayer.isToggled()) {
			this.remove();
			return;
		} else if (!MultiAbilityManager.hasMultiAbilityBound(this.player, "WaterArms")) {
			this.remove();
			return;
		} else if (this.maxPunches == 0 || this.maxUses == 0 || this.maxIceBlasts == 0 || (this.leftArmConsumed && this.rightArmConsumed)) {
			this.remove();
			return;
		}

		this.selectedSlot = this.player.getInventory().getHeldItemSlot();
		this.displayRightArm();
		this.displayLeftArm();

		if (this.lightningEnabled) {
			this.checkIfZapped();
		}
	}

	private boolean canPlaceBlock(final Block block) {
		return isWaterbendable(block.getType()) || isIce(block) || isWater(block) || ElementalAbility.isAir(block.getType());
	}

	/**
	 * Displays the right arm. Returns false if the arm cannot be fully
	 * displayed.
	 *
	 * @return false If arm cannot be fully displayed
	 */
	public boolean displayRightArm() {
		final List<Block> newBlocks = new ArrayList<>();
		if (this.rightArmConsumed) {
			return false;
		}

		final Location r1 = GeneralMethods.getRightSide(this.player.getLocation(), 1).add(0, 1.5, 0);
		if (!this.canPlaceBlock(r1.getBlock())) {
			this.right.clear();
			return false;
		}

		if (!(this.getRightHandPos().getBlock().getLocation().equals(r1.getBlock().getLocation()))) {
			this.addBlock(r1.getBlock(), GeneralMethods.getWaterData(3), 100);
			newBlocks.add(r1.getBlock());
		}

		final Location r2 = GeneralMethods.getRightSide(this.player.getLocation(), 2).add(0, 1.5, 0);
		if (!this.canPlaceBlock(r2.getBlock()) || !this.canPlaceBlock(r1.getBlock())) {
			this.right.clear();
			this.right.addAll(newBlocks);
			return false;
		}

		this.addBlock(r2.getBlock(), Material.WATER.createBlockData(), 100);
		newBlocks.add(r2.getBlock());

		for (int j = 1; j <= this.initLength; j++) {
			final Location r3 = r2.clone().toVector().add(this.player.getLocation().clone().getDirection().multiply(j)).toLocation(this.player.getWorld());
			if (!this.canPlaceBlock(r3.getBlock()) || !this.canPlaceBlock(r2.getBlock()) || !this.canPlaceBlock(r1.getBlock())) {
				this.right.clear();
				this.right.addAll(newBlocks);
				return false;
			}

			newBlocks.add(r3.getBlock());
			if (j >= 1 && this.selectedSlot == this.freezeSlot && this.bPlayer.canIcebend()) {
				this.addBlock(r3.getBlock(), Material.ICE.createBlockData(), 100);
			} else {
				this.addBlock(r3.getBlock(), Material.WATER.createBlockData(), 100);
			}
		}

		this.right.clear();
		this.right.addAll(newBlocks);

		return true;
	}

	/**
	 * Displays the left arm. Returns false if the arm cannot be fully
	 * displayed.
	 *
	 * @return false If the arm cannot be fully displayed.
	 */
	public boolean displayLeftArm() {
		final List<Block> newBlocks = new ArrayList<>();
		if (this.leftArmConsumed) {
			return false;
		}

		final Location l1 = GeneralMethods.getLeftSide(this.player.getLocation(), 1).add(0, 1.5, 0);
		if (!this.canPlaceBlock(l1.getBlock())) {
			this.left.clear();
			return false;
		}

		if (!(this.getLeftHandPos().getBlock().getLocation().equals(l1.getBlock().getLocation()))) {
			this.addBlock(l1.getBlock(), GeneralMethods.getWaterData(3), 100);
			newBlocks.add(l1.getBlock());
		}

		final Location l2 = GeneralMethods.getLeftSide(this.player.getLocation(), 2).add(0, 1.5, 0);
		if (!this.canPlaceBlock(l2.getBlock()) || !this.canPlaceBlock(l1.getBlock())) {
			this.left.clear();
			this.left.addAll(newBlocks);
			return false;
		}

		this.addBlock(l2.getBlock(), Material.WATER.createBlockData(), 100);
		newBlocks.add(l2.getBlock());

		for (int j = 1; j <= this.initLength; j++) {
			final Location l3 = l2.clone().toVector().add(this.player.getLocation().clone().getDirection().multiply(j)).toLocation(this.player.getWorld());
			if (!this.canPlaceBlock(l3.getBlock()) || !this.canPlaceBlock(l2.getBlock()) || !this.canPlaceBlock(l1.getBlock())) {
				this.left.clear();
				this.left.addAll(newBlocks);
				return false;
			}

			newBlocks.add(l3.getBlock());
			if (j >= 1 && this.selectedSlot == this.freezeSlot && this.bPlayer.canIcebend()) {
				this.addBlock(l3.getBlock(), Material.ICE.createBlockData(), 100);
			} else {
				this.addBlock(l3.getBlock(), Material.WATER.createBlockData(), 100);
			}
		}

		this.left.clear();
		this.left.addAll(newBlocks);

		return true;
	}

	public void addBlock(final Block b, final BlockData data, final long revertTime) {
		new TempBlock(b, data, revertTime, this);
	}

	/**
	 * Calculate roughly where the player's right hand is.
	 *
	 * @return location of right hand
	 */
	private Location getRightHandPos() {
		return GeneralMethods.getRightSide(this.player.getLocation(), .34).add(0, 1.5, 0);
	}

	/**
	 * Calculate roughly where the player's left hand is.
	 *
	 * @return location of left hand
	 */
	private Location getLeftHandPos() {
		return GeneralMethods.getLeftSide(this.player.getLocation(), .34).add(0, 1.5, 0);
	}

	/**
	 * Returns the location of the tip of the right arm, assuming it is fully
	 * extended. Use the displayRightArm() check to see if it is fully extended.
	 *
	 * @return location of the tip of the right arm
	 */
	public Location getRightArmEnd() {
		final Location r1 = GeneralMethods.getRightSide(this.player.getLocation(), 2).add(0, 1.5, 0);
		return r1.clone().add(this.player.getLocation().getDirection().normalize().multiply(this.initLength));
	}

	/**
	 * Returns the location of the tip of the left arm assuming it is fully
	 * extended. Use the displayLeftArm() check to see if it is fully extended.
	 *
	 * @return location of the tip of the left arm
	 */
	public Location getLeftArmEnd() {
		final Location l1 = GeneralMethods.getLeftSide(this.player.getLocation(), 2).add(0, 1.5, 0);
		return l1.clone().add(this.player.getLocation().getDirection().normalize().multiply(this.initLength));
	}

	private static void progressRevert(final boolean ignoreTime) {
		for (final Block block : WaterArmsSpear.getIceBlocks().keySet()) {
			final long time = WaterArmsSpear.getIceBlocks().get(block);
			if (System.currentTimeMillis() > time || ignoreTime) {
				if (TempBlock.isTempBlock(block)) {
					TempBlock.revertBlock(block, Material.AIR);
				}
				WaterArmsSpear.getIceBlocks().remove(block);
			}
		}
	}

	private void checkIfZapped() {
		final List<Block> blocks = new ArrayList<>(this.right);
		blocks.addAll(this.left);
		for (final Lightning lightning : getAbilities(Lightning.class)) {
			for (final Lightning.Arc arc : lightning.getArcs()) {
				for (final Block arm : blocks) {
					for (final Location loc : arc.getPoints()) {
						if (arm.getLocation().getWorld().equals(loc.getWorld()) && loc.distance(arm.getLocation()) <= 2.5) {
							for (final Location l1 : getOffsetLocations(4, arm.getLocation(), 1.25)) {
								FireAbility.playLightningbendingParticle(l1);
								emitLight(l1);
							}
							if (this.lightningKill) {
								DamageHandler.damageEntity(this.player, 60D, lightning);
							} else {
								DamageHandler.damageEntity(this.player, this.lightningDamage, lightning);
							}
						}
					}
				}
			}
		}
	}

	private static List<Location> getOffsetLocations(final int amount, final Location location, final double offset) {
		final List<Location> locations = new ArrayList<Location>();
		for (int i = 0; i < amount; i++) {
			locations.add(location.clone().add((float) (Math.random() * offset), (float) (Math.random() * offset), (float) (Math.random() * offset)));
		}
		return locations;
	}

	@Override
	public void remove() {
		super.remove();
		MultiAbilityManager.unbindMultiAbility(this.player);
		if (this.player.isOnline()) {
			this.bPlayer.addCooldown("WaterArms", this.cooldown);
		}
		new WaterReturn(this.player, this.player.getLocation().getBlock());
	}

	public void prepareCancel() {
		if (System.currentTimeMillis() < this.lastClickTime + 500L) {
			this.remove();
		} else {
			this.lastClickTime = System.currentTimeMillis();
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
		for (final WaterArms waterArms : getAbilities(WaterArms.class)) {
			waterArms.displayLeftArm();
			waterArms.displayRightArm();
		}
		WaterArmsWhip.progressAllCleanup();
	}

	public static void removeAllCleanup() {
		progressRevert(true);
		WaterArmsSpear.getIceBlocks().clear();
		WaterArmsWhip.removeAllCleanup();
	}

	public static boolean isUnbreakable(final Block block) {
		if (block.getType().getBlastResistance() >= 9.0F) {
			return true;
		}
		return false;
	}

	public void displayBoundMsg(final int slot) {
		final String name = this.bPlayer.getAbilities().get(slot);
		if (name != null) {
			this.player.sendMessage(this.getElement().getColor() + this.sneakMsg + " " + name);
		}
	}

	/**
	 * Returns the active arm of the player.
	 *
	 * @return {@link Arm} of the player
	 */
	public Arm getActiveArm() {
		return this.activeArm;
	}

	/**
	 * Switches the active arm of a player.
	 */
	public void switchActiveArm() {
		if (this.activeArm.equals(Arm.RIGHT)) {
			this.activeArm = Arm.LEFT;
		} else {
			this.activeArm = Arm.RIGHT;
		}
	}

	/**
	 * Switches to the most suitable arm for the player.
	 *
	 * @return the {@link Arm} that was swapped to
	 */
	public Arm switchPreferredArm() {
		this.switchActiveArm();
		if (this.activeArm.equals(Arm.LEFT)) {
			if (!this.displayLeftArm()) {
				this.switchActiveArm();
			}
		}
		if (this.activeArm.equals(Arm.RIGHT)) {
			if (!this.displayRightArm()) {
				this.switchActiveArm();
			}
		}
		return this.getActiveArm();
	}

	public boolean canDisplayActiveArm() {
		switch (this.activeArm) {
			case LEFT:
				return this.displayLeftArm();
			case RIGHT:
				return this.displayRightArm();
			default:
				return false;
		}
	}

	public Location getActiveArmEnd() {
		switch (this.activeArm) {
			case LEFT:
				return this.getLeftArmEnd();
			case RIGHT:
				return this.getRightArmEnd();
			default:
				return null;
		}
	}

	public Boolean isFullSource() {
		return this.fullSource;
	}

	public boolean getLeftArmConsumed() {
		return this.leftArmConsumed;
	}

	public void setLeftArmConsumed(final boolean consumed) {
		this.leftArmConsumed = consumed;
	}

	public boolean getRightArmConsumed() {
		return this.rightArmConsumed;
	}

	public void setRightArmConsumed(final boolean consumed) {
		this.rightArmConsumed = consumed;
	}

	public Integer getLengthReduction() {
		return this.lengthReduction;
	}

	public void setLengthReduction(final int lengthReduction) {
		this.lengthReduction = lengthReduction;
	}

	public Integer getMaxPunches() {
		return this.maxPunches;
	}

	public void setMaxPunches(final int maxPunches) {
		this.maxPunches = maxPunches;
	}

	public Integer getMaxUses() {
		return this.maxUses;
	}

	public void setMaxUses(final int maxUses) {
		this.maxUses = maxUses;
	}

	public Integer getMaxIceBlasts() {
		return this.maxIceBlasts;
	}

	public void setMaxIceBlasts(final int maxIceBlasts) {
		this.maxIceBlasts = maxIceBlasts;
	}

	public boolean canLightningDamage() {
		return this.lightningEnabled;
	}

	public void setCanLightningDamage(final boolean lightningEnabled) {
		this.lightningEnabled = lightningEnabled;
	}

	public double getLightningDamage() {
		return this.lightningDamage;
	}

	public void setLightningDamage(final double lightningDamage) {
		this.lightningDamage = lightningDamage;
	}

	public boolean isLeftArmCooldown() {
		return this.cooldownLeft;
	}

	public void setLeftArmCooldown(final boolean cooldown) {
		this.cooldownLeft = cooldown;
	}

	public boolean isRightArmCooldown() {
		return this.cooldownRight;
	}

	public void setRightArmCooldown(final boolean cooldown) {
		this.cooldownRight = cooldown;
	}

	public void setActiveArmCooldown(final boolean cooldown) {
		switch (this.activeArm) {
			case LEFT:
				this.setLeftArmCooldown(cooldown);
				return;
			case RIGHT:
				this.setRightArmCooldown(cooldown);
				return;
			default:
				break;
		}
	}

	@Override
	public long getCooldown() {
		return this.cooldown;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

	@Override
	public String getName() {
		return "WaterArms";
	}

	@Override
	public Location getLocation() {
		return this.player != null ? this.player.getLocation() : null;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	public boolean isCanUsePlantSource() {
		return this.canUsePlantSource;
	}

	public void setCanUsePlantSource(final boolean canUsePlantSource) {
		this.canUsePlantSource = canUsePlantSource;
	}

	public boolean isLightningEnabled() {
		return this.lightningEnabled;
	}

	public void setLightningEnabled(final boolean lightningEnabled) {
		this.lightningEnabled = lightningEnabled;
	}

	public boolean isLightningKill() {
		return this.lightningKill;
	}

	public void setLightningKill(final boolean lightningKill) {
		this.lightningKill = lightningKill;
	}

	public int getInitLength() {
		return this.initLength;
	}

	public void setInitLength(final int initLength) {
		this.initLength = initLength;
	}

	public int getSourceGrabRange() {
		return this.sourceGrabRange;
	}

	public void setSourceGrabRange(final int sourceGrabRange) {
		this.sourceGrabRange = sourceGrabRange;
	}

	public int getSelectedSlot() {
		return this.selectedSlot;
	}

	public void setSelectedSlot(final int selectedSlot) {
		this.selectedSlot = selectedSlot;
	}

	public int getFreezeSlot() {
		return this.freezeSlot;
	}

	public void setFreezeSlot(final int freezeSlot) {
		this.freezeSlot = freezeSlot;
	}

	public long getLastClickTime() {
		return this.lastClickTime;
	}

	public void setLastClickTime(final long lastClickTime) {
		this.lastClickTime = lastClickTime;
	}

	public World getWorld() {
		return this.world;
	}

	public void setWorld(final World world) {
		this.world = world;
	}

	public String getSneakMsg() {
		return this.sneakMsg;
	}

	public void setSneakMsg(final String sneakMsg) {
		this.sneakMsg = sneakMsg;
	}

	public void setFullSource(final boolean fullSource) {
		this.fullSource = fullSource;
	}

	public void setActiveArm(final Arm activeArm) {
		this.activeArm = activeArm;
	}

	public void addToArm(final Block block, final Arm arm) {
		if (arm.equals(Arm.LEFT)) {
			this.left.add(block);
		} else {
			this.right.add(block);
		}
	}

	public void emitLight(final Location location) {
		if (!getConfig().getBoolean("Properties.Fire.DynamicLight.Enabled")) return;

		int brightness = getConfig().getInt("Properties.Fire.DynamicLight.Brightness");
		long keepAlive = getConfig().getLong("Properties.Fire.DynamicLight.KeepAlive");

		if (brightness < 1 || brightness > 15) {
			throw new IllegalArgumentException("Properties.Fire.DynamicLight.Brightness must be between 1 and 15.");
		}

		LightManager.createLight(location).brightness(brightness).timeUntilFadeout(keepAlive).emit();
	}
}
