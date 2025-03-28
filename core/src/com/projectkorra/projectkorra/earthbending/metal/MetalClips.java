package com.projectkorra.projectkorra.earthbending.metal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.region.RegionProtection;
import com.projectkorra.projectkorra.util.ChatUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.MetalAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempArmor;

public class MetalClips extends MetalAbility {

	private static final Map<Entity, Integer> ENTITY_CLIPS_COUNT = new ConcurrentHashMap<>();
	private static final Map<Entity, MetalClips> TARGET_TO_ABILITY = new ConcurrentHashMap<>();
	private static final List<Material> METAL_ITEMS = List.of(Material.IRON_INGOT, Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS, Material.IRON_BLOCK, Material.IRON_AXE, Material.IRON_PICKAXE, Material.IRON_SWORD, Material.IRON_HOE, Material.IRON_SHOVEL, Material.IRON_DOOR);

	private boolean isBeingWorn;
	private boolean isControlling;
	@Attribute("CanThrow")
	private boolean canThrow;
	private boolean isMagnetized;
	@Attribute("CanUse4Clips")
	private boolean canUse4Clips;
	@Attribute("CanLoot")
	private boolean canLoot;
	private boolean hasSnuck;
	private int metalClipsCount;
	private int abilityType;
	private int armorTime;
	@Attribute("Magnet" + Attribute.RANGE)
	private int magnetRange;
	private long armorStartTime;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute("Shoot" + Attribute.COOLDOWN)
	private long shootCooldown;
	@Attribute("Shoot" + Attribute.SPEED)
	private double shootSpeed;
	@Attribute("Crush" + Attribute.COOLDOWN)
	private long crushCooldown;
	@Attribute("Magnet" + Attribute.COOLDOWN)
	private long magnetCooldown;
	@Attribute("Magnet" + Attribute.SPEED)
	private double magnetSpeed;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute("Crush" + Attribute.DAMAGE)
	private double crushDamage;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	private LivingEntity targetEntity;
	private List<Item> trackedIngots;
	private String actionBarMessage;

	public MetalClips(final Player player, final int abilityType) {
		super(player);
		if (hasAbility(player, MetalClips.class)) {
			return;
		}

		this.abilityType = abilityType;
		this.canLoot = player.hasPermission("bending.ability.MetalClips.loot");
		this.canUse4Clips = player.hasPermission("bending.ability.MetalClips.4clips");
		this.armorTime = getConfig().getInt("Abilities.Earth.MetalClips.Duration");
		this.range = getConfig().getDouble("Abilities.Earth.MetalClips.Range");
		this.cooldown = getConfig().getLong("Abilities.Earth.MetalClips.Cooldown");
		this.shootCooldown = getConfig().getLong("Abilities.Earth.MetalClips.Shoot.Cooldown");
		this.shootSpeed = getConfig().getDouble("Abilities.Earth.MetalClips.Shoot.Speed");
		this.crushCooldown = getConfig().getLong("Abilities.Earth.MetalClips.Crush.Cooldown");
		this.magnetCooldown = getConfig().getLong("Abilities.Earth.MetalClips.Magnet.Cooldown");
		this.magnetRange = getConfig().getInt("Abilities.Earth.MetalClips.Magnet.Range");
		this.magnetSpeed = getConfig().getDouble("Abilities.Earth.MetalClips.Magnet.Speed");
		this.crushDamage = getConfig().getDouble("Abilities.Earth.MetalClips.Crush.Damage");
		this.damage = getConfig().getDouble("Abilities.Earth.MetalClips.Damage");
		this.canThrow = (getConfig().getBoolean("Abilities.Earth.MetalClips.ThrowEnabled") && player.hasPermission("bending.ability.metalclips.throw"));
		this.actionBarMessage = ChatUtil.color(ConfigManager.languageConfig.get().getString("Abilities.Earth.MetalClips.ActionBarMessage", "* MetalClipped *"));
		this.trackedIngots = new ArrayList<>();

		if (abilityType == 0 && this.bPlayer.canBend(this) && player.getInventory().containsAtLeast(new ItemStack(Material.IRON_INGOT), 1)) {
			this.shootMetal();
		} else if (abilityType == 1 && !this.bPlayer.isOnCooldown("MetalClips Magnet")) {
			this.isMagnetized = true;
		}

		this.start();
	}

	public void shootMetal() {
		if (this.bPlayer.isOnCooldown("MetalClips Shoot")) {
			return;
		}
		this.bPlayer.addCooldown("MetalClips Shoot", this.shootCooldown);

		final PlayerInventory inventory = player.getInventory();
		final ItemStack itemStack = new ItemStack(Material.IRON_INGOT);
		if (!inventory.containsAtLeast(itemStack, 1)) {
			return;
		}


		final Location location = player.getLocation();
		final Item item = this.player.getWorld().dropItemNaturally(location.clone().add(0, 1, 0), itemStack);
		final Entity targetedEntity = GeneralMethods.getTargetedEntity(this.player, this.range);
		final Vector direction = GeneralMethods.getDirection(location, targetedEntity != null
				? targetedEntity.getLocation()
				: GeneralMethods.getTargetedLocation(this.player, this.range));
		GeneralMethods.setVelocity(this, item, direction.normalize().multiply(this.shootSpeed));

		this.trackedIngots.add(item);
		this.player.getInventory().removeItem(itemStack);
	}

	public void formArmor() {
		if (this.metalClipsCount >= 4 || (this.metalClipsCount == 3 && !this.canUse4Clips)) {
			return;
		} else if (this.targetEntity != null && (RegionProtection.isRegionProtected(this, this.targetEntity.getLocation()) || ((targetEntity instanceof Player) && Commands.invincible.contains(targetEntity.getName())))) {
			return;
		}

		this.metalClipsCount = Math.min(this.metalClipsCount + 1, 4);
		final ItemStack[] armor = new ItemStack[] {
				(this.metalClipsCount >= 2) ? new ItemStack(Material.IRON_BOOTS) : null,
				(this.metalClipsCount >= 3) ? new ItemStack(Material.IRON_LEGGINGS) : null,
				(this.metalClipsCount >= 1) ? new ItemStack(Material.IRON_CHESTPLATE) : null,
				(this.metalClipsCount >= 4) ? new ItemStack(Material.IRON_HELMET) : null
		};
		new TempArmor(this.targetEntity, this, armor);
		this.armorStartTime = System.currentTimeMillis();
		this.isBeingWorn = true;
		ENTITY_CLIPS_COUNT.put(this.targetEntity, this.metalClipsCount);
	}

	public void resetArmor() {
		if (this.targetEntity == null || !TempArmor.hasTempArmor(this.targetEntity) || this.targetEntity.isDead()) {
			return;
		}

		for (final TempArmor tempArmor : TempArmor.getTempArmorList(targetEntity)) {
			tempArmor.revert();
		}
		this.dropIngots(this.targetEntity.getLocation());
		this.isBeingWorn = false;
	}

	public void launch() {
		if (!this.canThrow || this.targetEntity == null) {
			return;
		}
		Vector direction = this.targetEntity.getLocation().toVector().subtract(this.player.getLocation().toVector()).normalize();
		GeneralMethods.setVelocity(this, this.targetEntity, direction.multiply(this.metalClipsCount / 2D));
		this.remove();
	}

	public void crush() {
		if (!this.bPlayer.isOnCooldown("MetalClips Crush")) {
			this.bPlayer.addCooldown("MetalClips Crush", this.crushCooldown);
			DamageHandler.damageEntity(this.targetEntity, this.player, this.crushDamage, this);
		}
	}

	@Override
	public void progress() {
		if (!this.bPlayer.canBendIgnoreCooldowns(this)) {
			this.remove();
			return;
		}

		if (this.targetEntity != null) {
			if ((this.targetEntity instanceof Player target && !target.isOnline()) || this.targetEntity.isDead()) {
				this.remove();
				return;
			}
		}

		if (this.player.isSneaking()) {
			this.hasSnuck = true;
		} else {
			if (this.isMagnetized) {
				this.bPlayer.addCooldown("MetalClips Magnet", this.magnetCooldown);
				this.remove();
				return;
			}
			this.isControlling = false;
			if (this.metalClipsCount > 0 && this.hasSnuck && this.abilityType == 0) {
				this.launch();
			}
		}

		Location location = player.getLocation();
		if (this.isMagnetized) {
			List<Entity> entities = GeneralMethods.getEntitiesAroundPoint(location, this.magnetRange);
			if (entities.isEmpty()) {
				this.remove();
				return;
			}

			final ItemStack itemInHand = this.player.getInventory().getItemInMainHand();
			final boolean holdingMagnet = itemInHand.getType() == Material.IRON_INGOT && itemInHand.hasItemMeta()
					&& itemInHand.getItemMeta().getDisplayName().equalsIgnoreCase("Magnet");

			for (final Entity entity : entities) {
				Location entityLocation = entity.getLocation();
				final Vector vector = GeneralMethods.getDirection(entityLocation, location);
				if (holdingMagnet && (entity instanceof Player || entity instanceof Zombie || entity instanceof Skeleton)) {
					if (entity instanceof Player target) {
						for (final ItemStack itemStack : target.getInventory().getContents()) {
							if (itemStack != null && !METAL_ITEMS.contains(itemStack.getType())) {
								player.getWorld().dropItem(location, itemStack.clone());
								itemStack.setType(Material.AIR);
								itemStack.setAmount(0);
							}
						}
					}

					final LivingEntity livingEntity = (LivingEntity) entity;
					final EntityEquipment equipment = livingEntity.getEquipment();
					if (equipment == null) {
						continue;
					}

					final ItemStack[] armor = equipment.getArmorContents();
					for (final ItemStack itemStack : armor) {
						if (METAL_ITEMS.contains(itemStack.getType())) {
							livingEntity.getWorld().dropItem(entityLocation, itemStack.clone());
							itemStack.setType(Material.AIR);
							itemStack.setAmount(0);
						}
					}
					equipment.setArmorContents(armor);

					ItemStack entityMainHand = equipment.getItemInMainHand();
					if (METAL_ITEMS.contains(entityMainHand.getType())) {
						livingEntity.getWorld().dropItem(entityLocation, entityMainHand);
						equipment.setItemInMainHand(new ItemStack(Material.AIR, 1));
					}
				}

				if (entity instanceof Item item && METAL_ITEMS.contains(item.getItemStack().getType())) {
					GeneralMethods.setVelocity(this, item, vector.normalize().multiply(this.magnetSpeed).add(new Vector(0, 0.2, 0)));
				}
			}
		}

		if (this.isBeingWorn && System.currentTimeMillis() > this.armorStartTime + this.armorTime) {
			this.remove();
			return;
		}

		if (this.isControlling && this.player.isSneaking()) {
			final Location targetLocation = this.targetEntity.getLocation();
			final Location loc = this.player.getWorld().equals(this.targetEntity.getWorld())
					? GeneralMethods.getTargetedLocation(this.player, (int) location.distance(targetLocation))
					: targetLocation;
			final Vector direction = GeneralMethods.getDirection(targetLocation, GeneralMethods.getTargetedLocation(this.player, 10));
			double distance = loc.getWorld().equals(targetLocation.getWorld()) ? loc.distance(targetLocation) : 0;

			if (this.metalClipsCount == 1 && distance > 0.5) {
				GeneralMethods.setVelocity(this, this.targetEntity, direction.normalize().multiply(0.2));
			} else if (this.metalClipsCount == 2 && distance > 1.2) {
				GeneralMethods.setVelocity(this, this.targetEntity, direction.normalize().multiply(0.2));
			} else if (this.metalClipsCount >= 3) {
				GeneralMethods.setVelocity(this, this.targetEntity, distance > 1.2 ? direction.normalize().multiply(.5) : new Vector());
				this.targetEntity.setFallDistance(0);
			}

			if (this.targetEntity instanceof Player && !actionBarMessage.isEmpty()) {
				ChatUtil.sendActionBar(Element.METAL.getColor() + actionBarMessage, (Player) this.targetEntity);
			}
		}

		final Iterator<Item> ingotIterator = this.trackedIngots.iterator();
		while (ingotIterator.hasNext()) {
			final Item item = ingotIterator.next();
			if (item.isOnGround()) {
				ingotIterator.remove();
				continue;
			}

			for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(item.getLocation(), 1.8)) {
                if (entity == player || !(entity instanceof LivingEntity)) {
					continue;
                }

				if (entity instanceof Player || entity instanceof Zombie || entity instanceof Skeleton) {
					if (this.targetEntity == null) {
						this.targetEntity = (LivingEntity) entity;
						TARGET_TO_ABILITY.put(this.targetEntity, this);
						this.formArmor();
					} else if (this.targetEntity == entity) {
						this.formArmor();
					} else {
						if (TARGET_TO_ABILITY.get(this.targetEntity) == this) {
							this.resetArmor();
							this.metalClipsCount = 0;
							ENTITY_CLIPS_COUNT.remove(this.targetEntity);
							TARGET_TO_ABILITY.remove(this.targetEntity);

							this.targetEntity = (LivingEntity) entity;
							TARGET_TO_ABILITY.put(this.targetEntity, this);
							this.formArmor();
						} else {
							TARGET_TO_ABILITY.get(this.targetEntity).remove();
						}
					}
				} else {
					DamageHandler.damageEntity(entity, this.player, this.damage, this);
					this.dropIngots(entity.getLocation(), item.getItemStack().getAmount());
				}
				ingotIterator.remove();
				item.remove();
				break;
            }
		}
	}

	public void dropIngots(final Location loc) {
		this.dropIngots(loc, this.metalClipsCount == 0 ? 1 : this.metalClipsCount);
	}

	public void dropIngots(final Location loc, final int amount) {
		final Item i = this.player.getWorld().dropItem(loc, new ItemStack(Material.IRON_INGOT, amount));
		i.setPickupDelay(61);
	}

	@Override
	public void remove() {
		super.remove();

		this.resetArmor();
		if (!this.isMagnetized) {
			this.bPlayer.addCooldown(this);
		}
		this.trackedIngots.clear();
		this.metalClipsCount = 0;

		if (this.targetEntity != null) {
			ENTITY_CLIPS_COUNT.remove(this.targetEntity);
			TARGET_TO_ABILITY.remove(this.targetEntity);
		}
	}

	public static boolean isControlled(final LivingEntity player) {
		return TARGET_TO_ABILITY.containsKey(player);
	}

	public static boolean isControllingEntity(final Player player) {
		final MetalClips clips = getAbility(player, MetalClips.class);
		return clips != null && player.isSneaking() && clips.targetEntity != null;
	}

	public static Map<Entity, Integer> getEntityClipsCount() {
		return ENTITY_CLIPS_COUNT;
	}

	public static Map<Entity, MetalClips> getTargetToAbility() {
		return TARGET_TO_ABILITY;
	}

	public static boolean removeControlledEnitity(final LivingEntity entity) {
		if (entity == null) {
			return false;
		}
		for (final MetalClips metalclips : CoreAbility.getAbilities(MetalClips.class)) {
			if (metalclips.targetEntity == entity) {
				metalclips.remove();
				return true;
			}
		}
		return false;
	}

	@Override
	public String getName() {
		return "MetalClips";
	}

	@Override
	public Location getLocation() {
		if (this.targetEntity != null) {
			return this.targetEntity.getLocation();
		} else if (this.player != null) {
			return this.player.getLocation();
		}
		return null;
	}

	@Override
	public long getCooldown() {
		return this.cooldown;
	}

	public long getShootCooldown() {
		return this.shootCooldown;
	}

	public long getCrushCooldown() {
		return this.crushCooldown;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	public boolean isBeingWorn() {
		return this.isBeingWorn;
	}

	public void setBeingWorn(final boolean isBeingWorn) {
		this.isBeingWorn = isBeingWorn;
	}

	public boolean isControlling() {
		return this.isControlling;
	}

	public void setControlling(final boolean isControlling) {
		this.isControlling = isControlling;
	}

	public boolean isCanThrow() {
		return this.canThrow;
	}

	public void setCanThrow(final boolean canThrow) {
		this.canThrow = canThrow;
	}

	public boolean isMagnetized() {
		return this.isMagnetized;
	}

	public void setMagnetized(final boolean isMagnetized) {
		this.isMagnetized = isMagnetized;
	}

	public boolean isCanUse4Clips() {
		return this.canUse4Clips;
	}

	public void setCanUse4Clips(final boolean canUse4Clips) {
		this.canUse4Clips = canUse4Clips;
	}

	public boolean isCanLoot() {
		return this.canLoot;
	}

	public void setCanLoot(final boolean canLoot) {
		this.canLoot = canLoot;
	}

	public int getMetalClipsCount() {
		return this.metalClipsCount;
	}

	public void setMetalClipsCount(final int metalClipsCount) {
		this.metalClipsCount = metalClipsCount;
	}

	public int getAbilityType() {
		return this.abilityType;
	}

	public void setAbilityType(final int abilityType) {
		this.abilityType = abilityType;
	}

	public int getArmorTime() {
		return this.armorTime;
	}

	public void setArmorTime(final int armorTime) {
		this.armorTime = armorTime;
	}

	public double getCrushDamage() {
		return this.crushDamage;
	}

	public void setCrushDamage(final double crushDamage) {
		this.crushDamage = crushDamage;
	}

	public int getMagnetRange() {
		return this.magnetRange;
	}

	public void setMagnetRange(final int magnetRange) {
		this.magnetRange = magnetRange;
	}

	public long getArmorStartTime() {
		return this.armorStartTime;
	}

	public void setArmorStartTime(final long armorStartTime) {
		this.armorStartTime = armorStartTime;
	}

	public double getMagnetSpeed() {
		return this.magnetSpeed;
	}

	public void setMagnetSpeed(final double magnetSpeed) {
		this.magnetSpeed = magnetSpeed;
	}

	public double getRange() {
		return this.range;
	}

	public void setRange(final double range) {
		this.range = range;
	}

	public LivingEntity getTargetEntity() {
		return this.targetEntity;
	}

	public void setTargetEntity(final LivingEntity targetEntity) {
		this.targetEntity = targetEntity;
	}

	public List<Item> getTrackedIngots() {
		return this.trackedIngots;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

}
