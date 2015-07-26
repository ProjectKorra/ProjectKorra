package com.projectkorra.ProjectKorra.Utilities;

import com.projectkorra.ProjectKorra.PKListener;
import com.projectkorra.ProjectKorra.ProjectKorra;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class GrapplingHookAPI {

	public static ItemStack createHook(int uses) {
		ItemStack is = new ItemStack(Material.FISHING_ROD);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(ChatColor.GOLD + "Grappling Hook");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Uses Left: " + ChatColor.GREEN + uses);
		im.setLore(lore);
		is.setItemMeta(im);
		return is;
	}

	public static boolean isGrapplingHook(ItemStack is) {
		ItemMeta im = is.getItemMeta();
        return is.getType() == Material.FISHING_ROD
                && im.getDisplayName() != null
                && im.getDisplayName().equals(ChatColor.GOLD + "Grappling Hook");
    }

	public static int getUses(ItemStack is) {
		ItemMeta im = is.getItemMeta();
		String usesLine = im.getLore().get(0);
		String uses = usesLine.substring(usesLine.indexOf("a") + 1, usesLine.length());

		if (isInteger(uses)) {
			return Integer.parseInt(uses);
		}
		else
			return 0;
	}

	public static boolean playerOnCooldown(Player player) {
        return PKListener.noGrapplePlayers.containsKey(player.getName());
    }

	public static void removePlayerCooldown(Player player) {
		if (PKListener.noGrapplePlayers.containsKey(player.getName())) {
			PKListener.noGrapplePlayers.remove(player.getName());
		}
	}
	public static void addPlayerCooldown(final Player player, int seconds) {
		if (PKListener.noGrapplePlayers.containsKey(player.getName())) {
			Bukkit.getServer().getScheduler().cancelTask(PKListener.noGrapplePlayers.get(player.getName()));
		}

        int taskId = ProjectKorra.plugin.getServer().getScheduler()
                .scheduleSyncDelayedTask(ProjectKorra.plugin, () -> removePlayerCooldown(player), (100));

		PKListener.noGrapplePlayers.put(player.getName(), taskId);
	}

	public static void setUses(ItemStack is, int uses) {
		ItemMeta im = is.getItemMeta();
        List<String> lore = new ArrayList<>();

		lore.add(ChatColor.GRAY+"Uses Left: " + ChatColor.GREEN + uses);
		im.setLore(lore);
		is.setItemMeta(im);
	}

	public static boolean addUse(Player player, ItemStack hook) {
		if (player.getGameMode() == GameMode.CREATIVE) return true;

		ItemMeta im = hook.getItemMeta();
		String usesLine = im.getLore().get(0);
		String uses = usesLine.substring(usesLine.indexOf("a")+1, usesLine.length());

        if (!isInteger(uses)) {
            player.setItemInHand(new ItemStack(Material.AIR));
			player.getWorld().playSound(player.getLocation(), Sound.ITEM_BREAK, 10f, 1f);
			return false;
		} else {
			int currentUses = Integer.parseInt(uses);
			currentUses--;

			if (currentUses == 0) {
				player.setItemInHand(new ItemStack(Material.AIR));
				player.getWorld().playSound(player.getLocation(), Sound.ITEM_BREAK, 10f, 1f);
				return false;
			} else {
                ArrayList<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Uses Left: " + ChatColor.GREEN + currentUses);
				im.setLore(lore);
				hook.setItemMeta(im);
			}
		}
		return true;
	}

	public static void playGrappleSound(Location loc) {
		loc.getWorld().playSound(loc, Sound.MAGMACUBE_JUMP, 10f, 1f);
	}

	private static boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	public static void pullPlayerSlightly(Player p, Location loc) {
		if (loc.getY() > p.getLocation().getY()) {
			p.setVelocity(new Vector(0, 0.25, 0));
			return;
		}

		Location playerLoc = p.getLocation();

		Vector vector = loc.toVector().subtract(playerLoc.toVector());
		p.setVelocity(vector);
	}

	public static void pullEntityToLocation(final Entity e, Location loc) {
		Location entityLoc = e.getLocation();

		entityLoc.setY(entityLoc.getY() + 0.5);
		e.teleport(entityLoc);

		double g = -0.08;
        double t = loc.distance(entityLoc);
        double v_x = (1.0+0.07*t) * (loc.getX()-entityLoc.getX())/t;
		double v_y = (1.0+0.03*t) * (loc.getY()-entityLoc.getY())/t -0.5*g*t;
		double v_z = (1.0+0.07*t) * (loc.getZ()-entityLoc.getZ())/t;

		Vector v = e.getVelocity();
		v.setX(v_x);
		v.setY(v_y);
		v.setZ(v_z);
		e.setVelocity(v);
	}


}