package com.projectkorra.projectkorra.earthbending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MetalClips {
	public static ConcurrentHashMap<Player, MetalClips> instances = new ConcurrentHashMap<Player, MetalClips>();
	public static int armorTime = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.MetalClips.Duration");
	public static int crushInterval = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.MetalClips.DamageInterval");;
	public static int cooldown = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.MetalClips.Cooldown");
	public static int crushDamage = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.MetalClips.Damage");
	public static int magnetRange = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.MetalClips.MagnetRange");
	public static double magnetPower = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.MetalClips.MagnetPower");
	public static Material[] metalItems = { 
		Material.IRON_INGOT, Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, 
		Material.IRON_BOOTS, Material.IRON_BLOCK, Material.IRON_AXE, Material.IRON_PICKAXE, 
		Material.IRON_SWORD, Material.IRON_HOE, Material.IRON_SPADE, Material.IRON_DOOR 
	};

	private Player player;
	private LivingEntity target;
	private boolean isBeingWorn = false;
	private boolean isControlling = false;
	private boolean canThrow = false;
	private boolean magnetized = false;
	public int metalclips = 0;
	public int var;
	private long startTime;
	private long time;
	private double lastDistanceCheck;

	private ItemStack[] oldarmor;
	private List<Item> trackedIngots = new ArrayList<Item>();

	public MetalClips(Player player, int var) {
		if (instances.containsKey(player))
			return;

		this.player = player;
		this.var = var;

		if (!isEligible())
			return;

		if (var == 0)
			shootMetal();
		else if (var == 1)
			magnet();

		instances.put(player, this);
	}

	public boolean isEligible() {
		final BendingPlayer bplayer = GeneralMethods.getBendingPlayer(player.getName());

		if (!GeneralMethods.canBend(player.getName(), "MetalClips"))
			return false;

		if (GeneralMethods.getBoundAbility(player) == null)
			return false;

		if (!GeneralMethods.getBoundAbility(player).equalsIgnoreCase("MetalClips"))
			return false;

		if (GeneralMethods.isRegionProtectedFromBuild(player, "MetalClips", player.getLocation()))
			return false;

		if (!EarthMethods.canMetalbend(player))
			return false;

		if (bplayer.isOnCooldown("MetalClips"))
			return false;

		return true;
	}

	public void magnet() {
		magnetized = true;
	}

	public void shootMetal() {
		ItemStack is = new ItemStack(Material.IRON_INGOT, 1);

		if (GeneralMethods.getBendingPlayer(player.getName()).isOnCooldown("MetalClips"))
			return;

		if (!player.getInventory().containsAtLeast(is, 1)) {
			//ProjectKorra.log.info("Player doesn't have enough ingots!");
			remove();
			return;
		}

		Item ii = player.getWorld().dropItemNaturally(player.getLocation(), is);

		Vector v;

		if (GeneralMethods.getTargetedEntity(player, 10, new ArrayList<Entity>()) != null)
			v = GeneralMethods.getDirection(player.getLocation(), GeneralMethods.getTargetedEntity(player, 10, new ArrayList<Entity>()).getLocation());
		else
			v = GeneralMethods.getDirection(player.getLocation(), GeneralMethods.getTargetedLocation(player, 10));

		ii.setVelocity(v.normalize().add(new Vector(0, 0.2, 0).multiply(1.2)));
		trackedIngots.add(ii);
		player.getInventory().removeItem(is);

		GeneralMethods.getBendingPlayer(player.getName()).addCooldown("MetalManipulation", cooldown);
	}

	public void formArmor() {
		if (metalclips >= 4)
			return;

		if (metalclips == 3 && !player.hasPermission("bending.ability.MetalClips.4clips"))
			return;

		metalclips = (metalclips < 4) ? metalclips + 1 : 4;

		if (target instanceof Player) {
			Player target = (Player) this.target;
			if (oldarmor == null)
				oldarmor = target.getInventory().getArmorContents();

			ItemStack[] metalarmor = new ItemStack[4];

			metalarmor[2] = (metalclips >= 1) ? new ItemStack(Material.IRON_CHESTPLATE, 1) : oldarmor[2];
			metalarmor[0] = (metalclips >= 2) ? new ItemStack(Material.IRON_BOOTS, 1) : oldarmor[0];
			metalarmor[1] = (metalclips >= 3) ? new ItemStack(Material.IRON_LEGGINGS, 1) : oldarmor[1];
			metalarmor[3] = (metalclips >= 4) ? new ItemStack(Material.IRON_HELMET, 1) : oldarmor[3];

			target.getInventory().setArmorContents(metalarmor);
		}

		else {
			if (oldarmor == null)
				oldarmor = target.getEquipment().getArmorContents();

			ItemStack[] metalarmor = new ItemStack[4];

			metalarmor[2] = (metalclips >= 1) ? new ItemStack(Material.IRON_CHESTPLATE, 1) : oldarmor[2];
			metalarmor[0] = (metalclips >= 2) ? new ItemStack(Material.IRON_BOOTS, 1) : oldarmor[0];
			metalarmor[1] = (metalclips >= 3) ? new ItemStack(Material.IRON_LEGGINGS, 1) : oldarmor[1];
			metalarmor[3] = (metalclips >= 4) ? new ItemStack(Material.IRON_HELMET, 1) : oldarmor[3];

			target.getEquipment().setArmorContents(metalarmor);
		}

		if (metalclips == 4) {
			time = System.currentTimeMillis();
			lastDistanceCheck = player.getLocation().distance(target.getLocation());
		}
		startTime = System.currentTimeMillis();
		isBeingWorn = true;
	}

	public void resetArmor() {
		if (target == null || oldarmor == null)
			return;

		if (target instanceof Player)
			((Player) target).getInventory().setArmorContents(oldarmor);
		else
			target.getEquipment().setArmorContents(oldarmor);

		player.getWorld().dropItem(target.getLocation(), new ItemStack(Material.IRON_INGOT, metalclips));

		isBeingWorn = false;
	}

	public void control() {
		isControlling = true;
	}

	public void ceaseControl() {
		isControlling = false;
	}

	public boolean controlling() {
		return isControlling;
	}

	public void launch() {
		if (!canThrow)
			return;

		Location location = player.getLocation();
		double dx, dy, dz;
		Location target = this.target.getLocation().clone();
		dx = target.getX() - location.getX();
		dy = target.getY() - location.getY();
		dz = target.getZ() - location.getZ();
		Vector vector = new Vector(dx, dy, dz);
		vector.normalize();
		this.target.setVelocity(vector.multiply(2));
		remove();
	}

	public void progress() {
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		}

		if (GeneralMethods.getBoundAbility(player) == null || !GeneralMethods.getBoundAbility(player).equalsIgnoreCase("MetalClips")) {
			remove();
			return;
		}

		if (target != null) {
			if ((target instanceof Player && !((Player) target).isOnline()) || target.isDead()) {
				remove();
				return;
			}
		}

		if (!player.isSneaking()) {
			isControlling = false;
			magnetized = false;
		}

		if (magnetized) {
			if (GeneralMethods.getEntitiesAroundPoint(player.getLocation(), magnetRange).size() == 0) {
				remove();
				return;
			}
			for (Entity e : GeneralMethods.getEntitiesAroundPoint(player.getLocation(), magnetRange)) {
				Vector v = GeneralMethods.getDirection(e.getLocation(), player.getLocation());

				if (e instanceof Player && player.hasPermission("bending.ability.MetalClips.loot") && player.getInventory().getItemInHand().getType() == Material.IRON_INGOT && player.getInventory().getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase("Magnet")) {
					Player p = (Player) e;

					if (p.getEntityId() == player.getEntityId())
						continue;

					ItemStack[] inventory = p.getInventory().getContents();

					for (ItemStack is : inventory) {
						if (is == null)
							continue;

						if (Arrays.asList(metalItems).contains(is.getType())) {
							p.getWorld().dropItem(p.getLocation(), is);

							is.setType(Material.AIR);
							is.setAmount(0);
						}
					}

					p.getInventory().setContents(inventory);
					ItemStack[] armor = p.getInventory().getArmorContents();

					for (ItemStack is : armor) {
						if (Arrays.asList(metalItems).contains(is.getType())) {
							p.getWorld().dropItem(p.getLocation(), is);

							is.setType(Material.AIR);
							;
						}
					}

					p.getInventory().setArmorContents(armor);
					if (Arrays.asList(metalItems).contains(p.getInventory().getItemInHand().getType())) {
						p.getWorld().dropItem(p.getLocation(), p.getEquipment().getItemInHand());
						p.getEquipment().setItemInHand(new ItemStack(Material.AIR, 1));
					}
				}

				if ((e instanceof Zombie || e instanceof Skeleton) && player.hasPermission("bending.ability.MetalClips.loot") && player.getInventory().getItemInHand().getType() == Material.IRON_INGOT && player.getInventory().getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase("Magnet")) {
					LivingEntity le = (LivingEntity) e;

					ItemStack[] armor = le.getEquipment().getArmorContents();

					for (ItemStack is : armor) {
						if (Arrays.asList(metalItems).contains(is.getType())) {
							le.getWorld().dropItem(le.getLocation(), is);

							is.setType(Material.AIR);
						}
					}

					le.getEquipment().setArmorContents(armor);

					if (Arrays.asList(metalItems).contains(le.getEquipment().getItemInHand().getType())) {
						le.getWorld().dropItem(le.getLocation(), le.getEquipment().getItemInHand());
						le.getEquipment().setItemInHand(new ItemStack(Material.AIR, 1));
					}
				}

				if (e instanceof Item) {
					Item iron = (Item) e;

					if (Arrays.asList(metalItems).contains(iron.getItemStack().getType())) {
						iron.setVelocity(v.normalize().multiply(magnetPower));
					}
				}
			}
		}

		if (isBeingWorn && System.currentTimeMillis() > startTime + armorTime) {
			remove();
			return;
		}

		if (isControlling && player.isSneaking()) {
			if (metalclips == 1) {
				Location oldLocation = target.getLocation();
				Location loc = GeneralMethods.getTargetedLocation(player, (int) player.getLocation().distance(oldLocation));
				double distance = loc.distance(oldLocation);

				Vector v = GeneralMethods.getDirection(target.getLocation(), player.getLocation());

				if (distance > .5)
					target.setVelocity(v.normalize().multiply(0.2));

			}

			if (metalclips == 2) {
				Location oldLocation = target.getLocation();
				Location loc = GeneralMethods.getTargetedLocation(player, (int) player.getLocation().distance(oldLocation));
				double distance = loc.distance(oldLocation);

				Vector v = GeneralMethods.getDirection(target.getLocation(), GeneralMethods.getTargetedLocation(player, 10));

				if (distance > 1.2)
					target.setVelocity(v.normalize().multiply(0.2));

			}

			if (metalclips >= 3) {
				Location oldLocation = target.getLocation();
				Location loc = GeneralMethods.getTargetedLocation(player, (int) player.getLocation().distance(oldLocation));
				double distance = loc.distance(oldLocation);

				Vector v = GeneralMethods.getDirection(oldLocation, GeneralMethods.getTargetedLocation(player, 10));
				if (distance > 1.2)
					target.setVelocity(v.normalize().multiply(.5));
				else
					target.setVelocity(new Vector(0, 0, 0));

				target.setFallDistance(0);
			}

			if (metalclips == 4 && player.hasPermission("bending.ability.MetalClips.4clips")) {
				double distance = player.getLocation().distance(target.getLocation());
				if (distance < lastDistanceCheck - 0.3) {
					double height = target.getLocation().getY();
					if (height > player.getEyeLocation().getY()) {
						lastDistanceCheck = distance;

						if (System.currentTimeMillis() > time + crushInterval) {
							time = System.currentTimeMillis();
							GeneralMethods.damageEntity(player, target, (crushDamage + (crushDamage * 1.2)));
						}
					}
				}
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
						if (e instanceof Player || e instanceof Zombie || e instanceof Skeleton) {
							if (target == null)
								target = (LivingEntity) e;

							formArmor();
						}

						else {
							GeneralMethods.damageEntity(player, e, 5);
							ii.getWorld().dropItem(ii.getLocation(), ii.getItemStack());
							remove();
						}

						ii.remove();
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

	public LivingEntity getTarget() {
		return target;
	}

	public void remove() {
		for (Item i : trackedIngots) {
			i.remove();
		}
		resetArmor();
		trackedIngots.clear();
		instances.remove(player);
	}

	public static void removeAll() {
		for (Player p : instances.keySet()) {
			instances.get(p).remove();
		}
	}

	public static void progressAll() {
		for (Player p : instances.keySet()) {
			instances.get(p).progress();
		}
	}

	public static boolean isControlled(Player player) {
		for (Player p : instances.keySet()) {
			if (instances.get(p).getTarget() != null && instances.get(p).getTarget().getEntityId() == player.getEntityId()) {
				return true;
			}
		}
		return false;
	}
}
