package com.projectkorra.projectkorra.earthbending;

import java.util.ArrayList;
import java.util.Arrays;
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

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.MetalAbility;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.util.DamageHandler;

public class MetalClips extends MetalAbility {
	
	private static final Map<Entity, Integer> ENTITY_CLIPS_COUNT = new ConcurrentHashMap<>();
	private static final Map<Entity, MetalClips> TARGET_TO_ABILITY = new ConcurrentHashMap<>();
	private static final Material[] METAL_ITEMS = { 
		Material.IRON_INGOT, Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, 
		Material.IRON_BOOTS, Material.IRON_BLOCK, Material.IRON_AXE, Material.IRON_PICKAXE, 
		Material.IRON_SWORD, Material.IRON_HOE, Material.IRON_SPADE, Material.IRON_DOOR 
	};
	
	private boolean isBeingWorn;
	private boolean isControlling;
	private boolean canThrow;
	private boolean isMagnetized;
	private boolean canUse4Clips;
	private boolean canLoot;
	private boolean hasSnuck;
	private int metalClipsCount;
	private int abilityType;
	private int armorTime;
	private int magnetRange;
	private long armorStartTime;
	private long cooldown;
	private long shootCooldown;
	private long crushCooldown;
	private double magnetPower;
	private double range;
	private double crushDamage;
	private double damage;
	private LivingEntity targetEntity;
	private ItemStack[] oldArmor;
	private List<Item> trackedIngots;
	
	public MetalClips(Player player, int abilityType) {
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
		this.shootCooldown = getConfig().getLong("Abilities.Earth.MetalClips.ShootCooldown");
		this.crushCooldown = getConfig().getLong("Abilities.Earth.MetalClips.CrushCooldown");
		this.magnetRange = getConfig().getInt("Abilities.Earth.MetalClips.MagnetRange");
		this.magnetPower = getConfig().getDouble("Abilities.Earth.MetalClips.MagnetPower");
		this.crushDamage = getConfig().getDouble("Abilities.Earth.MetalClips.CrushDamage");
		this.damage = getConfig().getDouble("Abilities.Earth.MetalClips.Damage");
		this.canThrow = (getConfig().getBoolean("Abilities.Earth.MetalClips.ThrowEnabled") && player.hasPermission("bending.ability.metalclips.throw"));
		this.trackedIngots = new ArrayList<>();		
		
		if (!bPlayer.canBend(this)) {
			return;
		}
		
		if (bPlayer.isAvatarState()) {
			cooldown = 0;
			range = AvatarState.getValue(range);
			crushDamage = AvatarState.getValue(crushDamage);
			magnetRange = AvatarState.getValue(magnetRange);
			magnetPower = AvatarState.getValue(magnetPower);
		}

		if (abilityType == 0) {
			shootMetal();
		} else if (abilityType == 1) {
			isMagnetized = true;
		}

		start();
	}
	
	public static ItemStack getOriginalHelmet(LivingEntity ent) {
		MetalClips clips = TARGET_TO_ABILITY.get(ent);
		if (clips != null) {
			return clips.oldArmor[3];
		}
		return null;
	}

	public static ItemStack getOriginalChestplate(LivingEntity ent) {
		MetalClips clips = TARGET_TO_ABILITY.get(ent);
		if (clips != null) {
			return clips.oldArmor[2];
		}
		return null;
	}
	
	public static ItemStack getOriginalLeggings(LivingEntity ent) {
		MetalClips clips = TARGET_TO_ABILITY.get(ent);
		if (clips != null) {
			return clips.oldArmor[1];
		}
		return null;
	}
	
	public static ItemStack getOriginalBoots(LivingEntity ent) {
		MetalClips clips = TARGET_TO_ABILITY.get(ent);
		if (clips != null) {
			return clips.oldArmor[0];
		}
		return null;
	}
	
	public static ItemStack[] getOriginalArmor(LivingEntity ent) {
		MetalClips clips = TARGET_TO_ABILITY.get(ent);
		if (clips != null) {
			return clips.oldArmor;
		}
		return null;
	}

	public void shootMetal() {
		if (bPlayer.isOnCooldown("MetalClips Shoot")) {
			return;
		}
		bPlayer.addCooldown("MetalClips Shoot", shootCooldown);
		ItemStack is = new ItemStack(Material.IRON_INGOT, 1);

		if (!player.getInventory().containsAtLeast(is, 1)) {
			remove();
			return;
		}

		Item item = player.getWorld().dropItemNaturally(player.getLocation().add(0, 1, 0), is);
		Vector vector;

		Entity targetedEntity = GeneralMethods.getTargetedEntity(player, range, new ArrayList<Entity>());
		if (targetedEntity != null) {
			vector = GeneralMethods.getDirection(player.getLocation(), targetedEntity.getLocation());
		} else {
			vector = GeneralMethods.getDirection(player.getLocation(), GeneralMethods.getTargetedLocation(player, range));
		}

		item.setVelocity(vector.normalize().add(new Vector(0, 0.1, 0).multiply(1.2)));
		trackedIngots.add(item);
		player.getInventory().removeItem(is);
	}

	public void formArmor() {
		if (metalClipsCount >= 4) {
			return;
		} else if (metalClipsCount == 3 && !canUse4Clips) {
			return;
		} else if (targetEntity != null && GeneralMethods.isRegionProtectedFromBuild(this, targetEntity.getLocation())) {
			return;
		}

		metalClipsCount = (metalClipsCount < 4) ? metalClipsCount + 1 : 4;

		if (targetEntity instanceof Player) {
			Player target = (Player) targetEntity;
			if (oldArmor == null) {
				oldArmor = target.getInventory().getArmorContents();
			}

			ItemStack[] metalArmor = new ItemStack[4];

			metalArmor[2] = (metalClipsCount >= 1) ? new ItemStack(Material.IRON_CHESTPLATE, 1) : oldArmor[2];
			metalArmor[0] = (metalClipsCount >= 2) ? new ItemStack(Material.IRON_BOOTS, 1) : oldArmor[0];
			metalArmor[1] = (metalClipsCount >= 3) ? new ItemStack(Material.IRON_LEGGINGS, 1) : oldArmor[1];
			metalArmor[3] = (metalClipsCount >= 4) ? new ItemStack(Material.IRON_HELMET, 1) : oldArmor[3];
			ENTITY_CLIPS_COUNT.put(target, metalClipsCount);
			target.getInventory().setArmorContents(metalArmor);
		} else {
			if (oldArmor == null) {
				oldArmor = targetEntity.getEquipment().getArmorContents();
			}

			ItemStack[] metalarmor = new ItemStack[4];

			metalarmor[2] = (metalClipsCount >= 1) ? new ItemStack(Material.IRON_CHESTPLATE, 1) : oldArmor[2];
			metalarmor[0] = (metalClipsCount >= 2) ? new ItemStack(Material.IRON_BOOTS, 1) : oldArmor[0];
			metalarmor[1] = (metalClipsCount >= 3) ? new ItemStack(Material.IRON_LEGGINGS, 1) : oldArmor[1];
			metalarmor[3] = (metalClipsCount >= 4) ? new ItemStack(Material.IRON_HELMET, 1) : oldArmor[3];
			ENTITY_CLIPS_COUNT.put(targetEntity, metalClipsCount);
			targetEntity.getEquipment().setArmorContents(metalarmor);
		}
		armorStartTime = System.currentTimeMillis();
		isBeingWorn = true;
	}

	public void resetArmor() {
		if (targetEntity == null || oldArmor == null || targetEntity.isDead()) {
			return;
		}

		if (targetEntity instanceof Player) {
			((Player) targetEntity).getInventory().setArmorContents(oldArmor);
		} else {
			targetEntity.getEquipment().setArmorContents(oldArmor);
		}

		player.getWorld().dropItem(targetEntity.getLocation(), new ItemStack(Material.IRON_INGOT, metalClipsCount));
		isBeingWorn = false;
		bPlayer.addCooldown(this);
	}

	public void launch() {
		if (!canThrow) {
			return;
		}
		
		if (targetEntity == null) {
			return;
		}

		Location location = player.getLocation();
		double dx, dy, dz;
		Location target = targetEntity.getLocation().clone();
		dx = target.getX() - location.getX();
		dy = target.getY() - location.getY();
		dz = target.getZ() - location.getZ();
		Vector vector = new Vector(dx, dy, dz);
		vector.normalize();
		targetEntity.setVelocity(vector.multiply(metalClipsCount/2).normalize());
		remove();
	}
	
	public void crush() {
		if (bPlayer.isOnCooldown("MetalClips Crush")) {
			return;
		}
		bPlayer.addCooldown("MetalClips Crush", crushCooldown);
		DamageHandler.damageEntity(targetEntity, player, crushDamage, this);
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreCooldowns(this)) {
			remove();
			return;
		}

		if (targetEntity != null) {
			if ((targetEntity instanceof Player && !((Player) targetEntity).isOnline()) || targetEntity.isDead()) {
				remove();
				return;
			}
		}
		
		if (player.isSneaking()) {
			hasSnuck = true;
		}

		if (!player.isSneaking()) {
			isControlling = false;
			isMagnetized = false;
			if (metalClipsCount < 4 && hasSnuck && abilityType == 0) {
				launch();
			}
		}

		if (isMagnetized) {
			if (GeneralMethods.getEntitiesAroundPoint(player.getLocation(), magnetRange).size() == 0) {
				remove();
				return;
			}
			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(player.getLocation(), magnetRange)) {
				Vector vector = GeneralMethods.getDirection(entity.getLocation(), player.getLocation());
				ItemStack itemInHand = player.getInventory().getItemInHand();
				
				if (entity instanceof Player && canLoot && itemInHand.getType() == Material.IRON_INGOT && itemInHand.getItemMeta().getDisplayName().equalsIgnoreCase("Magnet")) {
					Player targetPlayer = (Player) entity;

					if (targetPlayer.getEntityId() == player.getEntityId()) {
						continue;
					}

					ItemStack[] inventory = targetPlayer.getInventory().getContents();

					for (ItemStack is : inventory) {
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
					ItemStack[] armor = targetPlayer.getInventory().getArmorContents();

					for (ItemStack is : armor) {
						if (Arrays.asList(METAL_ITEMS).contains(is.getType())) {
							targetPlayer.getWorld().dropItem(targetPlayer.getLocation(), is);
							is.setType(Material.AIR);
						}
					}

					targetPlayer.getInventory().setArmorContents(armor);
					if (Arrays.asList(METAL_ITEMS).contains(targetPlayer.getInventory().getItemInHand().getType())) {
						targetPlayer.getWorld().dropItem(targetPlayer.getLocation(), targetPlayer.getEquipment().getItemInHand());
						targetPlayer.getEquipment().setItemInHand(new ItemStack(Material.AIR, 1));
					}
				}

				if ((entity instanceof Zombie || entity instanceof Skeleton) 
						&& canLoot
						&& itemInHand.getType() == Material.IRON_INGOT 
						&& itemInHand.getItemMeta().getDisplayName().equalsIgnoreCase("Magnet")) {
					LivingEntity livingEntity = (LivingEntity) entity;

					ItemStack[] armor = livingEntity.getEquipment().getArmorContents();

					for (ItemStack istack : armor) {
						if (Arrays.asList(METAL_ITEMS).contains(istack.getType())) {
							livingEntity.getWorld().dropItem(livingEntity.getLocation(), istack);
							istack.setType(Material.AIR);
						}
					}

					livingEntity.getEquipment().setArmorContents(armor);

					if (Arrays.asList(METAL_ITEMS).contains(livingEntity.getEquipment().getItemInHand().getType())) {
						livingEntity.getWorld().dropItem(livingEntity.getLocation(), livingEntity.getEquipment().getItemInHand());
						livingEntity.getEquipment().setItemInHand(new ItemStack(Material.AIR, 1));
					}
				}

				if (entity instanceof Item) {
					Item iron = (Item) entity;

					if (Arrays.asList(METAL_ITEMS).contains(iron.getItemStack().getType())) {
						iron.setVelocity(vector.normalize().multiply(magnetPower));
					}
				}
			}
		}

		if (isBeingWorn && System.currentTimeMillis() > armorStartTime + armorTime) {
			remove();
			return;
		}

		if (isControlling && player.isSneaking()) {
			if (metalClipsCount == 1) {
				Location oldLocation = targetEntity.getLocation();
				Location loc = GeneralMethods.getTargetedLocation(player, (int) player.getLocation().distance(oldLocation));
				double distance = loc.distance(oldLocation);
				Vector vector = GeneralMethods.getDirection(targetEntity.getLocation(), player.getLocation());

				if (distance > 0.5) {
					targetEntity.setVelocity(vector.normalize().multiply(0.2));
				}
			}

			if (metalClipsCount == 2) {
				Location oldLocation = targetEntity.getLocation();
				Location loc = GeneralMethods.getTargetedLocation(player, (int) player.getLocation().distance(oldLocation));
				double distance = loc.distance(oldLocation);

				Vector vector = GeneralMethods.getDirection(targetEntity.getLocation(), GeneralMethods.getTargetedLocation(player, 10));

				if (distance > 1.2) {
					targetEntity.setVelocity(vector.normalize().multiply(0.2));
				}
			}

			if (metalClipsCount >= 3) {
				Location oldLocation = targetEntity.getLocation();
				Location loc = GeneralMethods.getTargetedLocation(player, (int) player.getLocation().distance(oldLocation));
				double distance = loc.distance(oldLocation);
				Vector vector = GeneralMethods.getDirection(oldLocation, GeneralMethods.getTargetedLocation(player, 10));
				
				if (distance > 1.2) {
					targetEntity.setVelocity(vector.normalize().multiply(.5));
				} else {
					targetEntity.setVelocity(new Vector(0, 0, 0));
				}

				targetEntity.setFallDistance(0);
			}
		}

		for (int i = 0; i < trackedIngots.size(); i++) {
			Item ii = trackedIngots.get(i);
			if (ii.isOnGround()) {
				trackedIngots.remove(i);
				continue;
			}

			if (ii.getItemStack().getType() == Material.IRON_INGOT) {
				if (GeneralMethods.getEntitiesAroundPoint(ii.getLocation(), 2).size() == 0) {
					remove();
					return;
				}

				for (Entity e : GeneralMethods.getEntitiesAroundPoint(ii.getLocation(), 2)) {
					if (e instanceof LivingEntity && e.getEntityId() != player.getEntityId()) {
						if ((e instanceof Player || e instanceof Zombie || e instanceof Skeleton)) {
							if (targetEntity == null) {
								targetEntity = (LivingEntity) e;
								TARGET_TO_ABILITY.put(targetEntity, this);
								formArmor();
							} else if (targetEntity == e) {
								formArmor();
							} else {
								TARGET_TO_ABILITY.get(targetEntity).remove();
								player.getWorld().dropItemNaturally(e.getLocation(), new ItemStack(Material.IRON_INGOT, 1));
							}
						} else {
							DamageHandler.damageEntity(e, player, damage, this);
							ii.getWorld().dropItem(ii.getLocation(), ii.getItemStack());
							remove();
						}

						ii.remove();
						break;
					}
				}
			}
		}

		removeDeadIngots();
	}

	public void removeDeadIngots() {
		for (int i = 0; i < trackedIngots.size(); i++) {
			Item ii = trackedIngots.get(i);
			if (ii.isDead()) {
				trackedIngots.remove(ii);
			}
		}
	}

	@Override
	public void remove() {
		super.remove();
		
		resetArmor();
		trackedIngots.clear();
		metalClipsCount = 0;
		
		if (targetEntity != null) {
			ENTITY_CLIPS_COUNT.remove(targetEntity);
			TARGET_TO_ABILITY.remove(targetEntity);
		}
	}

	public static boolean isControlled(LivingEntity player) {
		return TARGET_TO_ABILITY.containsKey(player);
	}
	
	public static boolean isControllingEntity(Player player) {
		MetalClips clips = getAbility(player, MetalClips.class);
		return clips != null && player.isSneaking() && clips.targetEntity != null;
	}
	
	public static Map<Entity, Integer> getEntityClipsCount() {
		return ENTITY_CLIPS_COUNT;
	}
	
	public static Map<Entity, MetalClips> getTargetToAbility() {
		return TARGET_TO_ABILITY;
	}
	
	public static boolean removeControlledEnitity(LivingEntity entity) {
		if (entity == null) return false;
		for (MetalClips metalclips : CoreAbility.getAbilities(MetalClips.class)) {
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
		if (targetEntity != null) {
			return targetEntity.getLocation();
		} else if (player != null) {
			return player.getLocation();
		}
		return null;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}
	
	public long getShootCooldown() {
		return shootCooldown;
	}
	
	public long getCrushCooldown() {
		return crushCooldown;
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
		return isBeingWorn;
	}

	public void setBeingWorn(boolean isBeingWorn) {
		this.isBeingWorn = isBeingWorn;
	}

	public boolean isControlling() {
		return isControlling;
	}

	public void setControlling(boolean isControlling) {
		this.isControlling = isControlling;
	}

	public boolean isCanThrow() {
		return canThrow;
	}

	public void setCanThrow(boolean canThrow) {
		this.canThrow = canThrow;
	}

	public boolean isMagnetized() {
		return isMagnetized;
	}

	public void setMagnetized(boolean isMagnetized) {
		this.isMagnetized = isMagnetized;
	}

	public boolean isCanUse4Clips() {
		return canUse4Clips;
	}

	public void setCanUse4Clips(boolean canUse4Clips) {
		this.canUse4Clips = canUse4Clips;
	}

	public boolean isCanLoot() {
		return canLoot;
	}

	public void setCanLoot(boolean canLoot) {
		this.canLoot = canLoot;
	}

	public int getMetalClipsCount() {
		return metalClipsCount;
	}

	public void setMetalClipsCount(int metalClipsCount) {
		this.metalClipsCount = metalClipsCount;
	}

	public int getAbilityType() {
		return abilityType;
	}

	public void setAbilityType(int abilityType) {
		this.abilityType = abilityType;
	}

	public int getArmorTime() {
		return armorTime;
	}

	public void setArmorTime(int armorTime) {
		this.armorTime = armorTime;
	}

	public double getCrushDamage() {
		return crushDamage;
	}

	public void setCrushDamage(double crushDamage) {
		this.crushDamage = crushDamage;
	}

	public int getMagnetRange() {
		return magnetRange;
	}

	public void setMagnetRange(int magnetRange) {
		this.magnetRange = magnetRange;
	}

	public long getArmorStartTime() {
		return armorStartTime;
	}

	public void setArmorStartTime(long armorStartTime) {
		this.armorStartTime = armorStartTime;
	}

	public double getMagnetPower() {
		return magnetPower;
	}

	public void setMagnetPower(double magnetPower) {
		this.magnetPower = magnetPower;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public LivingEntity getTargetEntity() {
		return targetEntity;
	}

	public void setTargetEntity(LivingEntity targetEntity) {
		this.targetEntity = targetEntity;
	}

	public ItemStack[] getOldArmor() {
		return oldArmor;
	}

	public List<Item> getTrackedIngots() {
		return trackedIngots;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

}
