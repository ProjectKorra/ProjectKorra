package com.projectkorra.projectkorra.earthbending.metal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
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
	private static final Material[] METAL_ITEMS = { Material.IRON_INGOT, Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS, Material.IRON_BLOCK, Material.IRON_AXE, Material.IRON_PICKAXE, Material.IRON_SWORD, Material.IRON_HOE, Material.IRON_SHOVEL, Material.IRON_DOOR };

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
		this.shootCooldown = 600;
		this.crushCooldown = getConfig().getLong("Abilities.Earth.MetalClips.Crush.Cooldown");
		this.magnetCooldown = getConfig().getLong("Abilities.Earth.MetalClips.Magnet.Cooldown");
		this.magnetRange = getConfig().getInt("Abilities.Earth.MetalClips.Magnet.Range");
		this.magnetSpeed = getConfig().getDouble("Abilities.Earth.MetalClips.Magnet.Speed");
		this.crushDamage = getConfig().getDouble("Abilities.Earth.MetalClips.Crush.Damage");
		this.damage = getConfig().getDouble("Abilities.Earth.MetalClips.Damage");
		this.canThrow = (getConfig().getBoolean("Abilities.Earth.MetalClips.ThrowEnabled") && player.hasPermission("bending.ability.metalclips.throw"));
		this.trackedIngots = new ArrayList<>();

		if (this.bPlayer.isAvatarState()) {
			this.cooldown = getConfig().getLong("Abilities.Avatar.AvatarState.Earth.MetalClips.Cooldown");
			this.range = getConfig().getDouble("Abilities.Avatar.AvatarState.Earth.MetalClips.Range");
			this.crushDamage = getConfig().getLong("Abilities.Avatar.AvatarState.Earth.MetalClips.CrushDamage");
		}

		if (abilityType == 0) {
			if (!this.bPlayer.canBend(this)) {
				return;
			}
			if (!player.getInventory().containsAtLeast(new ItemStack(Material.IRON_INGOT), 1)) {
				return;
			}
			this.shootMetal();
		} else if (abilityType == 1) {
			if (this.bPlayer.isOnCooldown("MetalClips Magnet")) {
				return;
			}
			this.isMagnetized = true;
		}

		this.start();
	}

	/*
	 * public static ItemStack getOriginalHelmet(LivingEntity ent) { MetalClips
	 * clips = TARGET_TO_ABILITY.get(ent); if (clips != null) { return
	 * clips.oldArmor[3]; } return null; }
	 *
	 * public static ItemStack getOriginalChestplate(LivingEntity ent) {
	 * MetalClips clips = TARGET_TO_ABILITY.get(ent); if (clips != null) {
	 * return clips.oldArmor[2]; } return null; }
	 *
	 * public static ItemStack getOriginalLeggings(LivingEntity ent) {
	 * MetalClips clips = TARGET_TO_ABILITY.get(ent); if (clips != null) {
	 * return clips.oldArmor[1]; } return null; }
	 *
	 * public static ItemStack getOriginalBoots(LivingEntity ent) { MetalClips
	 * clips = TARGET_TO_ABILITY.get(ent); if (clips != null) { return
	 * clips.oldArmor[0]; } return null; }
	 *
	 * public static ItemStack[] getOriginalArmor(LivingEntity ent) { MetalClips
	 * clips = TARGET_TO_ABILITY.get(ent); if (clips != null) { return
	 * clips.oldArmor; } return null; }
	 */

	public void shootMetal() {
		if (this.bPlayer.isOnCooldown("MetalClips Shoot")) {
			return;
		}
		this.bPlayer.addCooldown("MetalClips Shoot", this.shootCooldown);
		final ItemStack is = new ItemStack(Material.IRON_INGOT, 1);

		if (!this.player.getInventory().containsAtLeast(is, 1)) {
			return;
		}

		final Item item = this.player.getWorld().dropItemNaturally(this.player.getLocation().add(0, 1, 0), is);
		Vector vector;

		final Entity targetedEntity = GeneralMethods.getTargetedEntity(this.player, this.range, new ArrayList<Entity>());
		if (targetedEntity != null) {
			vector = GeneralMethods.getDirection(this.player.getLocation(), targetedEntity.getLocation());
		} else {
			vector = GeneralMethods.getDirection(this.player.getLocation(), GeneralMethods.getTargetedLocation(this.player, this.range));
		}

		item.setVelocity(vector.normalize().add(new Vector(0, 0.1, 0).multiply(1.2)));
		this.trackedIngots.add(item);
		this.player.getInventory().removeItem(is);
	}

	public void formArmor() {
		if (this.metalClipsCount >= 4) {
			return;
		} else if (this.metalClipsCount == 3 && !this.canUse4Clips) {
			return;
		} else if (this.targetEntity != null && (GeneralMethods.isRegionProtectedFromBuild(this, this.targetEntity.getLocation()) || ((targetEntity instanceof Player) && Commands.invincible.contains(((Player) targetEntity).getName())))) {
			return;
		}

		this.metalClipsCount = (this.metalClipsCount < 4) ? this.metalClipsCount + 1 : 4;

		if (this.targetEntity instanceof Player) {
			final Player target = (Player) this.targetEntity;

			final ItemStack[] metalArmor = new ItemStack[4];

			metalArmor[2] = (this.metalClipsCount >= 1) ? new ItemStack(Material.IRON_CHESTPLATE) : null;
			metalArmor[0] = (this.metalClipsCount >= 2) ? new ItemStack(Material.IRON_BOOTS) : null;
			metalArmor[1] = (this.metalClipsCount >= 3) ? new ItemStack(Material.IRON_LEGGINGS) : null;
			metalArmor[3] = (this.metalClipsCount >= 4) ? new ItemStack(Material.IRON_HELMET) : null;
			ENTITY_CLIPS_COUNT.put(target, this.metalClipsCount);

			new TempArmor(target, this, metalArmor);
		} else {
			final ItemStack[] metalarmor = new ItemStack[4];

			metalarmor[2] = (this.metalClipsCount >= 1) ? new ItemStack(Material.IRON_CHESTPLATE) : null;
			metalarmor[0] = (this.metalClipsCount >= 2) ? new ItemStack(Material.IRON_BOOTS) : null;
			metalarmor[1] = (this.metalClipsCount >= 3) ? new ItemStack(Material.IRON_LEGGINGS) : null;
			metalarmor[3] = (this.metalClipsCount >= 4) ? new ItemStack(Material.IRON_HELMET) : null;
			ENTITY_CLIPS_COUNT.put(this.targetEntity, this.metalClipsCount);

			new TempArmor(this.targetEntity, this, metalarmor);
		}
		this.armorStartTime = System.currentTimeMillis();
		this.isBeingWorn = true;
	}

	public void resetArmor() {
		if (this.targetEntity == null || !TempArmor.hasTempArmor(this.targetEntity) || this.targetEntity.isDead()) {
			return;
		}

		for (final TempArmor tarmor : TempArmor.getTempArmorList(targetEntity)) {
			tarmor.revert();
		}
		this.dropIngots(this.targetEntity.getLocation());
		this.isBeingWorn = false;
	}

	public void launch() {
		if (!this.canThrow) {
			return;
		}

		if (this.targetEntity == null) {
			return;
		}

		final Location location = this.player.getLocation();
		double dx, dy, dz;
		final Location target = this.targetEntity.getLocation().clone();
		dx = target.getX() - location.getX();
		dy = target.getY() - location.getY();
		dz = target.getZ() - location.getZ();
		final Vector vector = new Vector(dx, dy, dz);
		vector.normalize();
		this.targetEntity.setVelocity(vector.multiply(this.metalClipsCount / 2D));
		this.remove();
	}

	public void crush() {
		if (this.bPlayer.isOnCooldown("MetalClips Crush")) {
			return;
		}
		this.bPlayer.addCooldown("MetalClips Crush", this.crushCooldown);
		DamageHandler.damageEntity(this.targetEntity, this.player, this.crushDamage, this);
	}

	@Override
	public void progress() {
		if (!this.bPlayer.canBendIgnoreCooldowns(this)) {
			this.remove();
			return;
		}

		if (this.targetEntity != null) {
			if ((this.targetEntity instanceof Player && !((Player) this.targetEntity).isOnline()) || this.targetEntity.isDead()) {
				this.remove();
				return;
			}
		}

		if (this.player.isSneaking()) {
			this.hasSnuck = true;
		}

		if (!this.player.isSneaking()) {
			if (this.isMagnetized) {
				this.bPlayer.addCooldown("MetalClips Magnet", this.magnetCooldown);
				this.remove();
				return;
			}
			this.isControlling = false;
			this.isMagnetized = false;
			if (this.metalClipsCount > 0 && this.hasSnuck && this.abilityType == 0) {
				this.launch();
			}
		}

		if (this.isMagnetized) {
			if (GeneralMethods.getEntitiesAroundPoint(this.player.getLocation(), this.magnetRange).size() == 0) {
				this.remove();
				return;
			}
			for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(this.player.getLocation(), this.magnetRange)) {
				final Vector vector = GeneralMethods.getDirection(entity.getLocation(), this.player.getLocation());
				final ItemStack itemInHand = this.player.getInventory().getItemInMainHand();

				if (entity instanceof Player && this.canLoot && itemInHand.getType() == Material.IRON_INGOT && (itemInHand.hasItemMeta() && itemInHand.getItemMeta().getDisplayName().equalsIgnoreCase("Magnet"))) {
					final Player targetPlayer = (Player) entity;

					if (targetPlayer.getEntityId() == this.player.getEntityId()) {
						continue;
					}

					final ItemStack[] inventory = targetPlayer.getInventory().getContents();

					for (final ItemStack is : inventory) {
						if (is == null) {
							continue;
						}
						if (Arrays.asList(METAL_ITEMS).contains(is.getType())) {
							targetPlayer.getWorld().dropItem(targetPlayer.getLocation(), is);

							is.setType(Material.AIR);
							is.setAmount(0);
						}
					}

					targetPlayer.getInventory().setContents(inventory);
					final ItemStack[] armor = targetPlayer.getInventory().getArmorContents();

					for (final ItemStack is : armor) {
						if (Arrays.asList(METAL_ITEMS).contains(is.getType())) {
							targetPlayer.getWorld().dropItem(targetPlayer.getLocation(), is);
							is.setType(Material.AIR);
						}
					}

					targetPlayer.getInventory().setArmorContents(armor);
					if (Arrays.asList(METAL_ITEMS).contains(targetPlayer.getInventory().getItemInMainHand().getType())) {
						targetPlayer.getWorld().dropItem(targetPlayer.getLocation(), targetPlayer.getEquipment().getItemInMainHand());
						targetPlayer.getEquipment().setItemInMainHand(new ItemStack(Material.AIR, 1));
					}
				}

				if ((entity instanceof Zombie || entity instanceof Skeleton) && this.canLoot && itemInHand.getType() == Material.IRON_INGOT && itemInHand.getItemMeta().getDisplayName().equalsIgnoreCase("Magnet")) {
					final LivingEntity livingEntity = (LivingEntity) entity;

					final ItemStack[] armor = livingEntity.getEquipment().getArmorContents();

					for (final ItemStack istack : armor) {
						if (Arrays.asList(METAL_ITEMS).contains(istack.getType())) {
							livingEntity.getWorld().dropItem(livingEntity.getLocation(), istack);
							istack.setType(Material.AIR);
						}
					}

					livingEntity.getEquipment().setArmorContents(armor);

					if (Arrays.asList(METAL_ITEMS).contains(livingEntity.getEquipment().getItemInMainHand().getType())) {
						livingEntity.getWorld().dropItem(livingEntity.getLocation(), livingEntity.getEquipment().getItemInMainHand());
						livingEntity.getEquipment().setItemInMainHand(new ItemStack(Material.AIR, 1));
					}
				}

				if (entity instanceof Item) {
					final Item iron = (Item) entity;

					if (Arrays.asList(METAL_ITEMS).contains(iron.getItemStack().getType())) {
						iron.setVelocity(vector.normalize().multiply(this.magnetSpeed).add(new Vector(0, 0.2, 0)));
					}
				}
			}
		}

		if (this.isBeingWorn && System.currentTimeMillis() > this.armorStartTime + this.armorTime) {
			this.remove();
			return;
		}

		if (this.isControlling && this.player.isSneaking()) {
			if (this.metalClipsCount == 1) {
				final Location oldLocation = this.targetEntity.getLocation();
				Location loc = oldLocation;
				if (this.player.getWorld().equals(oldLocation.getWorld())) {
					loc = GeneralMethods.getTargetedLocation(this.player, (int) this.player.getLocation().distance(oldLocation));
				}
				double distance = 0;
				if (loc.getWorld().equals(oldLocation.getWorld())) {
					distance = loc.distance(oldLocation);
				}
				final Vector vector = GeneralMethods.getDirection(this.targetEntity.getLocation(), this.player.getLocation());

				if (distance > 0.5) {
					this.targetEntity.setVelocity(vector.normalize().multiply(0.2));
				}
			}

			if (this.metalClipsCount == 2) {
				final Location oldLocation = this.targetEntity.getLocation();
				Location loc = oldLocation;
				if (this.player.getWorld().equals(oldLocation.getWorld())) {
					loc = GeneralMethods.getTargetedLocation(this.player, (int) this.player.getLocation().distance(oldLocation));
				}
				double distance = 0;
				if (loc.getWorld().equals(oldLocation.getWorld())) {
					distance = loc.distance(oldLocation);
				}
				final Vector vector = GeneralMethods.getDirection(this.targetEntity.getLocation(), GeneralMethods.getTargetedLocation(this.player, 10));

				if (distance > 1.2) {
					this.targetEntity.setVelocity(vector.normalize().multiply(0.2));
				}
			}

			if (this.metalClipsCount >= 3) {
				final Location oldLocation = this.targetEntity.getLocation();
				Location loc = oldLocation;
				if (this.player.getWorld().equals(oldLocation.getWorld())) {
					loc = GeneralMethods.getTargetedLocation(this.player, (int) this.player.getLocation().distance(oldLocation));
				}
				double distance = 0;
				if (loc.getWorld().equals(oldLocation.getWorld())) {
					distance = loc.distance(oldLocation);
				}
				final Vector vector = GeneralMethods.getDirection(oldLocation, GeneralMethods.getTargetedLocation(this.player, 10));

				if (distance > 1.2) {
					this.targetEntity.setVelocity(vector.normalize().multiply(.5));
				} else {
					this.targetEntity.setVelocity(new Vector(0, 0, 0));
				}

				this.targetEntity.setFallDistance(0);
			}
		}

		final Iterator<Item> it = this.trackedIngots.iterator();
		while (it.hasNext()) {
			final Item ii = it.next();
			if (ii.isOnGround()) {
				it.remove();
				continue;
			}

			for (final Entity e : GeneralMethods.getEntitiesAroundPoint(ii.getLocation(), 1.8)) {
				if (e instanceof LivingEntity && e.getEntityId() != this.player.getEntityId()) {
					if ((e instanceof Player || e instanceof Zombie || e instanceof Skeleton)) {
						if (this.targetEntity == null) {
							this.targetEntity = (LivingEntity) e;
							TARGET_TO_ABILITY.put(this.targetEntity, this);
							this.formArmor();
						} else if (this.targetEntity == e) {
							this.formArmor();
						} else {
							if (TARGET_TO_ABILITY.get(this.targetEntity) == this) {
								this.resetArmor();
								this.metalClipsCount = 0;
								ENTITY_CLIPS_COUNT.remove(this.targetEntity);
								TARGET_TO_ABILITY.remove(this.targetEntity);

								this.targetEntity = (LivingEntity) e;
								TARGET_TO_ABILITY.put(this.targetEntity, this);
								this.formArmor();
							} else {
								TARGET_TO_ABILITY.get(this.targetEntity).remove();
							}
						}
					} else {
						DamageHandler.damageEntity(e, this.player, this.damage, this);
						this.dropIngots(e.getLocation(), ii.getItemStack().getAmount());
					}
					it.remove();
					ii.remove();
					break;
				}
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
