package com.projectkorra.projectkorra.waterbending.multiabilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.api.WaterAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.abilities.water.WaterArmsConfig;
import com.projectkorra.projectkorra.configuration.configs.properties.WaterPropertiesConfig;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.multiabilities.WaterArms.Arm;

@SuppressWarnings({ "deprecation", "unused" })
public class WaterArmsSpear extends WaterAbility<WaterArmsConfig> {

	private static final Map<Block, Long> ICE_BLOCKS = new ConcurrentHashMap<Block, Long>();

	private boolean hitEntity;
	private boolean canFreeze;
	private boolean usageCooldownEnabled;
	@Attribute("DamageEnabled")
	private boolean spearDamageEnabled;
	@Attribute("Length")
	private int spearLength;
	@Attribute(Attribute.RANGE)
	private int spearRange;
	private int spearRangeNight;
	private int spearRangeFullMoon;
	@Attribute("SphereRadius")
	private int spearSphereRadius;
	private int spearSphereNight;
	private int spearSphereFullMoon;
	private int distanceTravelled;
	@Attribute(Attribute.DURATION)
	private long spearDuration;
	private long spearDurationNight;
	private long spearDurationFullMoon;
	@Attribute(Attribute.COOLDOWN)
	private long usageCooldown;
	@Attribute(Attribute.DAMAGE)
	private double spearDamage;
	private Arm arm;
	private Location location;
	private Location initLocation;
	private WaterArms waterArms;
	private final List<Location> spearLocations;

	public WaterArmsSpear(final WaterArmsConfig config, final Player player, final boolean freeze) {
		super(config, player);
		this.canFreeze = freeze;

		this.usageCooldownEnabled = config.SpearConfig.UsageCooldownEnabled;
		this.spearDamageEnabled = config.SpearConfig.DamageEnabled;
		this.spearLength = config.SpearConfig.Length;
		this.spearRange = config.SpearConfig.RangeDay;
		this.spearRangeNight = config.SpearConfig.RangeNight;
		this.spearRangeFullMoon = config.SpearConfig.RangeFullMoon;
		this.spearSphereRadius = config.SpearConfig.SphereRadiusDay;
		this.spearSphereNight = config.SpearConfig.SphereRadiusNight;
		this.spearSphereFullMoon = config.SpearConfig.SphereRadiusFullMoon;
		this.spearDuration = config.SpearConfig.DurationDay;
		this.spearDurationNight = config.SpearConfig.DurationNight;
		this.spearDurationFullMoon = config.SpearConfig.DurationFullMoon;
		this.usageCooldown = config.SpearConfig.UsageCooldown;
		this.spearDamage = config.SpearConfig.Damage;
		this.spearLocations = new ArrayList<>();

		this.getNightAugments();
		this.createInstance();
	}

	private void getNightAugments() {
		final World world = this.player.getWorld();
		if (isNight(world)) {
			if (isFullMoon(world) && !GeneralMethods.hasRPG()) {
				this.spearRange = this.spearRangeFullMoon;
				this.spearSphereRadius = this.spearSphereFullMoon;
				this.spearDuration = this.spearDurationFullMoon;
			} else {
				this.spearRange = this.spearRangeNight;
				this.spearSphereRadius = this.spearSphereNight;
				this.spearDuration = this.spearDurationNight;
			}
		}
	}

	private void createInstance() {
		this.waterArms = getAbility(this.player, WaterArms.class);
		if (this.waterArms != null) {
			this.waterArms.switchPreferredArm();
			this.arm = this.waterArms.getActiveArm();

			if (this.arm.equals(Arm.LEFT)) {
				if (this.waterArms.isLeftArmCooldown() || this.bPlayer.isOnCooldown("WaterArms_LEFT") || !this.waterArms.displayLeftArm()) {
					return;
				} else {
					if (this.usageCooldownEnabled) {
						this.bPlayer.addCooldown("WaterArms_LEFT", this.usageCooldown);
					}
					this.waterArms.setLeftArmConsumed(true);
					this.waterArms.setLeftArmCooldown(true);
				}
			}
			if (this.arm.equals(Arm.RIGHT)) {
				if (this.waterArms.isRightArmCooldown() || this.bPlayer.isOnCooldown("WaterArms_RIGHT") || !this.waterArms.displayRightArm()) {
					return;
				} else {
					if (this.usageCooldownEnabled) {
						this.bPlayer.addCooldown("WaterArms_RIGHT", this.usageCooldown);
					}
					this.waterArms.setRightArmConsumed(true);
					this.waterArms.setRightArmCooldown(true);
				}
			}
			final Vector dir = this.player.getLocation().getDirection();
			this.location = this.waterArms.getActiveArmEnd().add(dir.normalize().multiply(1));
			this.initLocation = this.location.clone();
		} else {
			return;
		}
		this.start();
	}

	@Override
	public void progress() {
		if (this.player.isDead() || !this.player.isOnline()) {
			this.remove();
			return;
		} else if (this.distanceTravelled > this.spearRange) {
			this.remove();
			return;
		}

		if (!this.hitEntity) {
			this.progressSpear();
		} else {
			this.createIceBall();
			this.remove();
		}

		if (!this.canPlaceBlock(this.location.getBlock())) {
			if (this.canFreeze) {
				this.createSpear();
			}
			this.remove();
			return;
		}
	}

	private void progressSpear() {
		for (int i = 0; i < 2; i++) {
			for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(this.location, 2)) {
				if (entity instanceof LivingEntity && entity.getEntityId() != this.player.getEntityId() && !(entity instanceof ArmorStand)) {
					this.hitEntity = true;
					this.location = entity.getLocation();

					if (this.spearDamageEnabled) {
						DamageHandler.damageEntity(entity, this.spearDamage, this);
					}

					return;
				}
			}

			if (!this.canPlaceBlock(this.location.getBlock())) {
				return;
			}

			new TempBlock(this.location.getBlock(), Material.WATER, GeneralMethods.getWaterData(0));
			getIceBlocks().put(this.location.getBlock(), System.currentTimeMillis() + 600L);
			final Vector direction = GeneralMethods.getDirection(this.initLocation, GeneralMethods.getTargetedLocation(this.player, this.spearRange, getTransparentMaterials())).normalize();

			this.location = this.location.add(direction.clone().multiply(1));
			this.spearLocations.add(this.location.clone());

			this.distanceTravelled++;
		}
	}

	private void createSpear() {
		for (int i = this.spearLocations.size() - this.spearLength; i < this.spearLocations.size(); i++) {
			if (i >= 0) {
				final Block block = this.spearLocations.get(i).getBlock();
				if (this.canPlaceBlock(block)) {
					playIcebendingSound(block.getLocation());
					if (getIceBlocks().containsKey(block)) {
						getIceBlocks().remove(block);
					}

					final TempBlock tempBlock = new TempBlock(block, Material.AIR);
					tempBlock.setType(Material.ICE);

					getIceBlocks().put(block, System.currentTimeMillis() + this.spearDuration + (long) (Math.random() * 500));
				}
			}
		}
	}

	public static boolean canThaw(final Block block) {
		return getIceBlocks().containsKey(block) && block.getType() == Material.ICE;
	}

	public static void thaw(final Block block) {
		if (canThaw(block)) {
			getIceBlocks().remove(block);
			if (TempBlock.isTempBlock(block)) {
				TempBlock.get(block).revertBlock();
			} else {
				block.setType(Material.AIR);
			}
		}
	}

	private void createIceBall() {
		if (this.spearSphereRadius <= 0) {
			if (this.canFreeze) {
				this.createSpear();
			}
			return;
		}
		final List<Entity> trapped = GeneralMethods.getEntitiesAroundPoint(this.location, this.spearSphereRadius);
		ICE_SETTING: for (final Block block : GeneralMethods.getBlocksAroundPoint(this.location, this.spearSphereRadius)) {
			if (isTransparent(this.player, block) && block.getType() != Material.ICE && !WaterArms.isUnbreakable(block)) {
				for (final Entity entity : trapped) {
					if (entity instanceof Player) {
						if (Commands.invincible.contains(((Player) entity).getName())) {
							return;
						}
						if (!ConfigManager.getConfig(WaterPropertiesConfig.class).FreezePlayerHead && GeneralMethods.playerHeadIsInBlock((Player) entity, block, true)) {
							continue ICE_SETTING;
						}
						if (!ConfigManager.getConfig(WaterPropertiesConfig.class).FreezePlayerFeet && GeneralMethods.playerFeetIsInBlock((Player) entity, block, true)) {
							continue ICE_SETTING;
						}
					}
				}
				playIcebendingSound(block.getLocation());
				new TempBlock(block, Material.ICE);
				getIceBlocks().put(block, System.currentTimeMillis() + this.spearDuration + (long) (Math.random() * 500));
			}
		}
	}

	private boolean canPlaceBlock(final Block block) {
		if (!isTransparent(this.player, block) && !((isWater(block) || this.isIcebendable(block)) && (TempBlock.isTempBlock(block) && !getIceBlocks().containsKey(block)))) {
			return false;
		} else if (GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
			return false;
		} else if (WaterArms.isUnbreakable(block) && !isWater(block)) {
			return false;
		}
		return true;
	}

	@Override
	public void remove() {
		super.remove();
		if (hasAbility(this.player, WaterArms.class)) {
			if (this.arm.equals(Arm.LEFT)) {
				this.waterArms.setLeftArmCooldown(false);
			} else {
				this.waterArms.setRightArmCooldown(false);
			}
			this.waterArms.setMaxUses(this.waterArms.getMaxUses() - 1);
		}
	}

	@Override
	public String getName() {
		return "WaterArms";
	}

	@Override
	public Location getLocation() {
		if (this.location != null) {
			return this.location;
		} else {
			return this.initLocation;
		}
	}

	@Override
	public long getCooldown() {
		return this.usageCooldown;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	public boolean isHitEntity() {
		return this.hitEntity;
	}

	public void setHitEntity(final boolean hitEntity) {
		this.hitEntity = hitEntity;
	}

	public boolean isCanFreeze() {
		return this.canFreeze;
	}

	public void setCanFreeze(final boolean canFreeze) {
		this.canFreeze = canFreeze;
	}

	public boolean isUsageCooldownEnabled() {
		return this.usageCooldownEnabled;
	}

	public void setUsageCooldownEnabled(final boolean usageCooldownEnabled) {
		this.usageCooldownEnabled = usageCooldownEnabled;
	}

	public boolean isSpearDamageEnabled() {
		return this.spearDamageEnabled;
	}

	public void setSpearDamageEnabled(final boolean spearDamageEnabled) {
		this.spearDamageEnabled = spearDamageEnabled;
	}

	public int getSpearLength() {
		return this.spearLength;
	}

	public void setSpearLength(final int spearLength) {
		this.spearLength = spearLength;
	}

	public int getSpearRange() {
		return this.spearRange;
	}

	public void setSpearRange(final int spearRange) {
		this.spearRange = spearRange;
	}

	public int getSpearRangeNight() {
		return this.spearRangeNight;
	}

	public void setSpearRangeNight(final int spearRangeNight) {
		this.spearRangeNight = spearRangeNight;
	}

	public int getSpearRangeFullMoon() {
		return this.spearRangeFullMoon;
	}

	public void setSpearRangeFullMoon(final int spearRangeFullMoon) {
		this.spearRangeFullMoon = spearRangeFullMoon;
	}

	public int getSpearSphere() {
		return this.spearSphereRadius;
	}

	public void setSpearSphere(final int spearSphere) {
		this.spearSphereRadius = spearSphere;
	}

	public int getSpearSphereNight() {
		return this.spearSphereNight;
	}

	public void setSpearSphereNight(final int spearSphereNight) {
		this.spearSphereNight = spearSphereNight;
	}

	public int getSpearSphereFullMoon() {
		return this.spearSphereFullMoon;
	}

	public void setSpearSphereFullMoon(final int spearSphereFullMoon) {
		this.spearSphereFullMoon = spearSphereFullMoon;
	}

	public int getDistanceTravelled() {
		return this.distanceTravelled;
	}

	public void setDistanceTravelled(final int distanceTravelled) {
		this.distanceTravelled = distanceTravelled;
	}

	public long getSpearDuration() {
		return this.spearDuration;
	}

	public void setSpearDuration(final long spearDuration) {
		this.spearDuration = spearDuration;
	}

	public long getSpearDurationNight() {
		return this.spearDurationNight;
	}

	public void setSpearDurationNight(final long spearDurationNight) {
		this.spearDurationNight = spearDurationNight;
	}

	public long getSpearDurationFullMoon() {
		return this.spearDurationFullMoon;
	}

	public void setSpearDurationFullMoon(final long spearDurationFullMoon) {
		this.spearDurationFullMoon = spearDurationFullMoon;
	}

	public long getUsageCooldown() {
		return this.usageCooldown;
	}

	public void setUsageCooldown(final long usageCooldown) {
		this.usageCooldown = usageCooldown;
	}

	public double getSpearDamage() {
		return this.spearDamage;
	}

	public void setSpearDamage(final double spearDamage) {
		this.spearDamage = spearDamage;
	}

	public Arm getArm() {
		return this.arm;
	}

	public void setArm(final Arm arm) {
		this.arm = arm;
	}

	public Location getInitLocation() {
		return this.initLocation;
	}

	public void setInitLocation(final Location initLocation) {
		this.initLocation = initLocation;
	}

	public WaterArms getWaterArms() {
		return this.waterArms;
	}

	public void setWaterArms(final WaterArms waterArms) {
		this.waterArms = waterArms;
	}

	public List<Location> getSpearLocations() {
		return this.spearLocations;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

	public static Map<Block, Long> getIceBlocks() {
		return ICE_BLOCKS;
	}
	
	@Override
	public Class<WaterArmsConfig> getConfigType() {
		return WaterArmsConfig.class;
	}

}
