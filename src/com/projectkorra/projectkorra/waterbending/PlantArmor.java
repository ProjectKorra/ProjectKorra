package com.projectkorra.projectkorra.waterbending;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;

public class PlantArmor {

	public static ConcurrentHashMap<Player, PlantArmor> instances = new ConcurrentHashMap<Player, PlantArmor>();

	private static long cooldown = ProjectKorra.plugin.getConfig().getLong("Abilities.Water.PlantArmor.Cooldown");
	private static long DURATION = ProjectKorra.plugin.getConfig().getLong("Abilities.Water.PlantArmor.Duration");
	private static int RESISTANCE = ProjectKorra.plugin.getConfig().getInt("Abilities.Water.PlantArmor.Resistance");
	
	private static int selectRange = ProjectKorra.plugin.getConfig().getInt("Abilities.Water.PlantArmor.SelectRange");
	private static int autoSelectRange = ProjectKorra.plugin.getConfig().getInt("Abilities.Water.PlantArmor.AutoSourcing.SelectRange");
	private static boolean auto = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Water.PlantArmor.AutoSourcing.Enabled");
	private static long autocooldown = ProjectKorra.plugin.getConfig().getLong("Abilities.Water.PlantArmor.AutoSourcing.Cooldown");
	
	private boolean isAuto;
	
	private Player player;
	private Block block;
	private Location location;
	private Plantbending plantbending;
	private long starttime;
	private boolean formed = false;
	private int resistance = RESISTANCE;
	public ItemStack[] oldarmor;
	public boolean hadEffect;
	private long duration = DURATION;
	private Material blocktype;

	public PlantArmor(Player player) {
		if (instances.containsKey(player)) {
			return;
		}

		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());

		if (bPlayer.isOnCooldown("PlantArmor"))
			return;

		this.player = player;
		Double d = WaterMethods.getWaterbendingNightAugment(player.getWorld()) * duration;
		duration = d.longValue();
		block = BlockSource.getWaterSourceBlock(player, autoSelectRange, selectRange, ClickType.LEFT_CLICK, auto, false, false, false, false, WaterMethods.canPlantbend(player));
		if (block == null) {
			return;
		}
		if (BlockSource.isAuto(block)) {
			isAuto = true;
		} else {
			isAuto = false;
		}
		location = block.getLocation();
		hadEffect = player.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
		if (!canUse())
			return;
		plantbending = new Plantbending(block);
		blocktype = block.getType();
		block.setType(Material.AIR);
		instances.put(player, this);
	}

	private boolean canUse() {
		if (!player.getWorld().equals(block.getWorld())) {
			cancel();
			return false;
		}

		if (location.distance(player.getEyeLocation()) > selectRange) {
			cancel();
			return false;
		}

		if (!WaterMethods.canPlantbend(player)) {
			cancel();
			return false;
		}

		return true;
	}

	private void playEffect() {
		if (!formed) {
			if (GeneralMethods.rand.nextInt(4) == 0) {
				WaterMethods.playPlantbendingSound(location);
			}
			GeneralMethods.displayColoredParticle(location, "009933");
			Vector v = player.getEyeLocation().toVector().subtract(location.toVector());
			location = location.add(v.normalize());
		}
	}

	private void cancel() {
		if (plantbending != null)
			plantbending.revert();
		if (instances.containsKey(player))
			instances.remove(player);
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
		if (isAuto) {
			bPlayer.addCooldown("PlantArmor", autocooldown);
		} else {
			bPlayer.addCooldown("PlantArmor", cooldown);
		}
	}

	private boolean inPosition() {
		if (location.distance(player.getEyeLocation()) <= 1.5)
			return true;
		return false;
	}

	private void formArmor() {
		oldarmor = player.getInventory().getArmorContents();
		ItemStack helmet = new ItemStack(blocktype);
		ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
		LeatherArmorMeta im = (LeatherArmorMeta) chestplate.getItemMeta();
		im.setColor(Color.GREEN);
		chestplate.setItemMeta(im);
		ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
		leggings.setItemMeta(im);
		ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
		boots.setItemMeta(im);
		player.getInventory().setHelmet(helmet);
		player.getInventory().setChestplate(chestplate);
		player.getInventory().setLeggings(leggings);
		player.getInventory().setBoots(boots);
		if (!hadEffect)
			player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 1000000, resistance - 1));
		formed = true;
		starttime = System.currentTimeMillis();
	}

	public static void progressAll() {
		for (Player player : instances.keySet()) {
			progress(player);
		}
	}

	public static void progress(Player player) {
		if (!instances.containsKey(player))
			return;
		PlantArmor plantarmor = instances.get(player);

		if (player.isDead() || !player.isOnline()) {
			plantarmor.removeEffect();
			plantarmor.cancel();
			return;
		}

		if (plantarmor.formed) {
			if (System.currentTimeMillis() > plantarmor.starttime + plantarmor.duration) {
				plantarmor.removeEffect();
				plantarmor.cancel();
				GeneralMethods.getBendingPlayer(player.getName()).addCooldown("PlantArmor", cooldown);
				return;
			}
		} else {
			if (!plantarmor.canUse())
				return;

			plantarmor.playEffect();

			if (plantarmor.inPosition()) {
				plantarmor.formArmor();
			}
		}

	}

	private void removeEffect() {
		player.getInventory().setArmorContents(oldarmor);
		if (!hadEffect)
			player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
	}

	public static void removeEffect(Player player) {
		if (!instances.containsKey(player))
			return;
		instances.get(player).removeEffect();
	}

	public static void removeAll() {
		for (Player player : instances.keySet()) {
			PlantArmor plantarmor = instances.get(player);
			plantarmor.removeEffect();
			plantarmor.cancel();
		}
	}

	public static boolean canRemoveArmor(Player player) {
		if (instances.containsKey(player)) {
			PlantArmor plantarmor = instances.get(player);
			if (System.currentTimeMillis() < plantarmor.starttime + plantarmor.duration)
				return false;
		}
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public int getResistance() {
		return resistance;
	}

	public void setResistance(int resistance) {
		this.resistance = resistance;
		if (!hadEffect) {
			player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
			player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 1000000, resistance - 1));
		}
	}
}
