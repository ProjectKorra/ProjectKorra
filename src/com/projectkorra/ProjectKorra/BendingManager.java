package com.projectkorra.ProjectKorra;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.airbending.AirBlast;
import com.projectkorra.ProjectKorra.airbending.AirBubble;
import com.projectkorra.ProjectKorra.airbending.AirBurst;
import com.projectkorra.ProjectKorra.airbending.AirPassive;
import com.projectkorra.ProjectKorra.airbending.AirScooter;
import com.projectkorra.ProjectKorra.airbending.AirShield;
import com.projectkorra.ProjectKorra.airbending.AirSpout;
import com.projectkorra.ProjectKorra.airbending.AirSuction;
import com.projectkorra.ProjectKorra.airbending.AirSwipe;
import com.projectkorra.ProjectKorra.airbending.Tornado;
import com.projectkorra.ProjectKorra.chiblocking.ChiPassive;
import com.projectkorra.ProjectKorra.earthbending.Catapult;
import com.projectkorra.ProjectKorra.earthbending.CompactColumn;
import com.projectkorra.ProjectKorra.earthbending.EarthArmor;
import com.projectkorra.ProjectKorra.earthbending.EarthBlast;
import com.projectkorra.ProjectKorra.earthbending.EarthColumn;
import com.projectkorra.ProjectKorra.earthbending.EarthPassive;
import com.projectkorra.ProjectKorra.earthbending.Shockwave;
import com.projectkorra.ProjectKorra.firebending.Cook;
import com.projectkorra.ProjectKorra.firebending.FireBlast;
import com.projectkorra.ProjectKorra.firebending.FireJet;
import com.projectkorra.ProjectKorra.firebending.FirePassive;
import com.projectkorra.ProjectKorra.firebending.FireStream;
import com.projectkorra.ProjectKorra.firebending.Fireball;
import com.projectkorra.ProjectKorra.firebending.Illumination;
import com.projectkorra.ProjectKorra.waterbending.Bloodbending;
import com.projectkorra.ProjectKorra.waterbending.FreezeMelt;
import com.projectkorra.ProjectKorra.waterbending.HealingWaters;
import com.projectkorra.ProjectKorra.waterbending.IceSpike;
import com.projectkorra.ProjectKorra.waterbending.IceSpike2;
import com.projectkorra.ProjectKorra.waterbending.OctopusForm;
import com.projectkorra.ProjectKorra.waterbending.Plantbending;
import com.projectkorra.ProjectKorra.waterbending.Torrent;
import com.projectkorra.ProjectKorra.waterbending.TorrentBurst;
import com.projectkorra.ProjectKorra.waterbending.WaterManipulation;
import com.projectkorra.ProjectKorra.waterbending.WaterPassive;
import com.projectkorra.ProjectKorra.waterbending.WaterSpout;
import com.projectkorra.ProjectKorra.waterbending.WaterWall;
import com.projectkorra.ProjectKorra.waterbending.Wave;

public class BendingManager implements Runnable {

	public ProjectKorra plugin;

	long time;
	long interval;

	ArrayList<World> worlds = new ArrayList<World>();
	ConcurrentHashMap<World, Boolean> nights = new ConcurrentHashMap<World, Boolean>();
	ConcurrentHashMap<World, Boolean> days = new ConcurrentHashMap<World, Boolean>();

	static final String defaultsunrisemessage = "You feel the strength of the rising sun empowering your firebending.";
	static final String defaultsunsetmessage = "You feel the empowering of your firebending subside as the sun sets.";
	static final String defaultmoonrisemessage = "You feel the strength of the rising moon empowering your waterbending.";
	static final String defaultmoonsetmessage = "You feel the empowering of your waterbending subside as the moon sets.";

	public BendingManager(ProjectKorra plugin) {
		this.plugin = plugin;
		time = System.currentTimeMillis();
	}

	public void run() {
		try {
			interval = System.currentTimeMillis() - time;
			time = System.currentTimeMillis();
			ProjectKorra.time_step = interval;

			AvatarState.manageAvatarStates();
			AirBlast.progressAll();
			AirPassive.handlePassive(Bukkit.getServer());
			ChiPassive.handlePassive();
			WaterPassive.handlePassive();
			FirePassive.handlePassive();
			EarthPassive.revertSands();
			TempPotionEffect.progressAll();
			Plantbending.regrow();
			AirBurst.progressAll();
			handleDayNight();
			Bloodbending.progressAll();
			FireJet.progressAll();
			AirScooter.progressAll();
			AirSpout.spoutAll();
			WaterSpout.handleSpouts(Bukkit.getServer());
			Cook.progressAll();
			FreezeMelt.handleFrozenBlocks();
			OctopusForm.progressAll();
			AirBubble.handleBubbles(Bukkit.getServer());
			Illumination.manage(Bukkit.getServer());
			Torrent.progressAll();
			TorrentBurst.progressAll();
			FireBlast.progressAll();
			AirSuction.progressAll();
			Fireball.progressAll();
			HealingWaters.heal(Bukkit.getServer());
			for (Player player : EarthArmor.instances.keySet()) {
				EarthArmor.moveArmor(player);
			}
			for (int ID: AirSwipe.instances.keySet()) {
				AirSwipe.progress(ID);
			}
			for (int ID: Tornado.instances.keySet()) {
				Tornado.progress(ID);
			}

			for (int id: FireStream.instances.keySet()) {
				FireStream.progress(id);
			}

			for (int ID : EarthBlast.instances.keySet()) {
				EarthBlast.progress(ID);
			}

			for (Block block: FireStream.ignitedblocks.keySet()) {
				if (block.getType() != Material.FIRE) {
					FireStream.ignitedblocks.remove(block);
				}
			}

			for (int ID: Catapult.instances.keySet()) {
				Catapult.progress(ID);
			}

			for (int ID: EarthColumn.instances.keySet()) {
				EarthColumn.progress(ID);
			}

			for (int ID: CompactColumn.instances.keySet()) {
				CompactColumn.progress(ID);
			}

			for (int ID: WaterManipulation.instances.keySet()) {
				WaterManipulation.progress(ID);
			}

			for (int ID: WaterWall.instances.keySet()) {
				WaterWall.progress(ID);
			}

			for (int ID : Wave.instances.keySet()) {
				Wave.progress(ID);
			}

			for (int ID : IceSpike.instances.keySet()) {
				IceSpike.instances.get(ID).progress();
			}

			for (int ID : AirShield.instances.keySet()) {
				AirShield.progress(ID);
			}

			Shockwave.progressAll();

			IceSpike2.progressAll();

			FireStream.dissipateAll();
		} catch (Exception e) {
			Methods.stopBending();
			e.printStackTrace();
		}
	}

	public void handleDayNight() {
		for (World world: plugin.getServer().getWorlds()) {
			if (world.getWorldType() == WorldType.NORMAL && !worlds.contains(world)) {
				worlds.add(world);
				nights.put(world, false);
				days.put(world, false);
			}
		}
		ArrayList<World> removeworlds = new ArrayList<World>();
		for (World world: worlds) {
			if (!plugin.getServer().getWorlds().contains(world)) {
				removeworlds.add(world);
				continue;
			}
			boolean night = nights.get(world);
			boolean day = days.get(world);
			if (Methods.isDay(world) && !day) {
				for (Player player: world.getPlayers()) {
					if (Methods.isBender(player.getName(), Element.Fire) && player.hasPermission("bending.message.daymessage")) {
						player.sendMessage(ChatColor.RED + defaultsunrisemessage);
					}
				}
				days.replace(world, true);
			}

			if (!Methods.isDay(world) && day) {
				for (Player player: world.getPlayers()) {
					if (Methods.isBender(player.getName(), Element.Fire) && player.hasPermission("bending.message.daymessage")) {
						player.sendMessage(ChatColor.RED + defaultsunsetmessage);
					}
				}
				days.replace(world, false);
			}

			if (Methods.isNight(world) && !night) {
				for (Player player: world.getPlayers()) {
					if (Methods.isBender(player.getName(), Element.Water) && player.hasPermission("bending.message.nightmessage")) {
						player.sendMessage(ChatColor.AQUA + defaultmoonrisemessage);
					}
				}
				nights.replace(world, true);
			}

			if (!Methods.isNight(world) && night) {
				for (Player player: world.getPlayers()) {
					if (Methods.isBender(player.getName(), Element.Water) && player.hasPermission("bending.message.nightmessage")) {
						player.sendMessage(ChatColor.AQUA + defaultmoonsetmessage);
					}
				}
				nights.replace(world, false);
			}
		}

		for (World world: removeworlds) {
			worlds.remove(world);
		}

	}
}
