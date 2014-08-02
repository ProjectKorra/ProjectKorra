package com.projectkorra.ProjectKorra;

import java.util.HashMap;

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
import com.projectkorra.ProjectKorra.chiblocking.RapidPunch;
import com.projectkorra.ProjectKorra.earthbending.Catapult;
import com.projectkorra.ProjectKorra.earthbending.CompactColumn;
import com.projectkorra.ProjectKorra.earthbending.EarthArmor;
import com.projectkorra.ProjectKorra.earthbending.EarthBlast;
import com.projectkorra.ProjectKorra.earthbending.EarthColumn;
import com.projectkorra.ProjectKorra.earthbending.EarthPassive;
import com.projectkorra.ProjectKorra.earthbending.EarthTunnel;
import com.projectkorra.ProjectKorra.earthbending.Shockwave;
import com.projectkorra.ProjectKorra.earthbending.Tremorsense;
import com.projectkorra.ProjectKorra.firebending.Cook;
import com.projectkorra.ProjectKorra.firebending.FireBlast;
import com.projectkorra.ProjectKorra.firebending.FireBurst;
import com.projectkorra.ProjectKorra.firebending.FireJet;
import com.projectkorra.ProjectKorra.firebending.FirePassive;
import com.projectkorra.ProjectKorra.firebending.FireShield;
import com.projectkorra.ProjectKorra.firebending.FireStream;
import com.projectkorra.ProjectKorra.firebending.Fireball;
import com.projectkorra.ProjectKorra.firebending.Illumination;
import com.projectkorra.ProjectKorra.firebending.Lightning;
import com.projectkorra.ProjectKorra.firebending.WallOfFire;
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
import com.projectkorra.ProjectKorra.waterbending.WaterReturn;
import com.projectkorra.ProjectKorra.waterbending.WaterSpout;
import com.projectkorra.ProjectKorra.waterbending.WaterWall;
import com.projectkorra.ProjectKorra.waterbending.Wave;

public class BendingManager implements Runnable {

	public ProjectKorra plugin;

	long time;
	long interval;

	//private final HashMap<String, Time> dayNight = new HashMap<>();
	private final HashMap<World, AtomicBoolean> times = new HashMap<World, AtomicBoolean>(); // true if day time

	static final String defaultsunrisemessage = "You feel the strength of the rising sun empowering your firebending.";
	static final String defaultsunsetmessage = "You feel the empowering of your firebending subside as the sun sets.";
	static final String defaultmoonrisemessage = "You feel the strength of the rising moon empowering your waterbending.";
	static final String defaultfullmoonrisemessage = "A full moon is rising, empowering your waterbending like never before.";
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
			EarthPassive.handleMetalPassives();
			TempPotionEffect.progressAll();
			Plantbending.regrow();
			AirBurst.progressAll();
			handleDayNight();
			Bloodbending.progressAll();
			Flight.handle();
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
			FireBurst.progressAll();
			FireShield.progressAll();
			Lightning.progressAll();
			WallOfFire.manage();
			WaterReturn.progressAll();
			for (Player p : RapidPunch.instance.keySet())
				RapidPunch.instance.get(p).startPunch(p);

			for (Block block : RevertChecker.revertQueue.keySet()) {
				// Tools.removeEarthbendedBlockByIndex(block);
				// if (Tools.revertBlock(block))
				Methods.revertBlock(block);
				RevertChecker.revertQueue.remove(block);
			}

			for (int i : RevertChecker.airRevertQueue.keySet()) {
				Methods.revertAirBlock(i);
				RevertChecker.airRevertQueue.remove(i);
			}

			for (Player player : EarthTunnel.instances.keySet()) {
				EarthTunnel.progress(player);
			}
			for (Player player : EarthArmor.instances.keySet()) {
				EarthArmor.moveArmor(player);
			}
			for (int ID : AirSwipe.instances.keySet()) {
				AirSwipe.progress(ID);
			}
			for (int ID : Tornado.instances.keySet()) {
				Tornado.progress(ID);
			}

			Tremorsense.manage(Bukkit.getServer());
			for (int id : FireStream.instances.keySet()) {
				FireStream.progress(id);
			}

			for (int ID : EarthBlast.instances.keySet()) {
				EarthBlast.progress(ID);
			}

			for (Block block : FireStream.ignitedblocks.keySet()) {
				if (block.getType() != Material.FIRE) {
					FireStream.ignitedblocks.remove(block);
				}
			}

			for (int ID : Catapult.instances.keySet()) {
				Catapult.progress(ID);
			}

			for (int ID : EarthColumn.instances.keySet()) {
				EarthColumn.progress(ID);
			}

			for (int ID : CompactColumn.instances.keySet()) {
				CompactColumn.progress(ID);
			}

			for (int ID : WaterManipulation.instances.keySet()) {
				WaterManipulation.progress(ID);
			}

			for (int ID : WaterWall.instances.keySet()) {
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
		for (World world: Bukkit.getServer().getWorlds()) {
			if (!times.containsKey(world)) {
				if (Methods.isDay(world)) {
					times.put(world, new AtomicBoolean(true));
				} else {
					times.put(world, new AtomicBoolean(false));
				}
			} else {
				final AtomicBoolean isDay = times.get(world);
				if (isDay.get() && !Methods.isDay(world)) {
					// The hashmap says it is day, but it is not.
					isDay.set(false); // Sets time to night, requires no re-adding a boolean.
					sendFirebenderMessage(world, isDay.get());
					if(isNight(world)) { //isNight also checks if the world is nether or end type which nees to be done
						sendWaterbenderMessage(world, isDay.get());//for waterbenders
					}
				}

				if (!isDay.get() && Methods.isDay(world)) {
					// The hashmap says it is night, but it is day.
					isDay.set(true);
					sendFirebenderMessage(world, isDay.get());
                                        sendWaterbenderMessage(world, isDay.get());//isNight works the same as !isDay if it's day time
					}
				}
			}
		}
		
//		/**
//		 * This code is ran on startup, it adds all loaded worlds to the
//		 * hashmap.
//		 */
//		if (dayNight.size() < 1) {
//			for (World world : plugin.getServer().getWorlds()) {
//				if (world.getWorldType() == WorldType.NORMAL) {
//					String worldName = world.getName();
//					if (dayNight.containsKey(worldName))
//						return;
//					if (Methods.isDay(world)) {
//						dayNight.put(worldName, Time.DAY);
//					} else {
//						dayNight.put(worldName, Time.NIGHT);
//					}
//				}
//			}
//		}

		//		for (World world : Bukkit.getWorlds()) {
		//			final String worldName = world.getName();
		//			if (!dayNight.containsKey(worldName))
		//				return;
		//			Time time = dayNight.get(worldName);
		//			if (Methods.isDay(world) && time.equals(Time.NIGHT)) {
		//				final Time newTime = Time.DAY;
		//				sendFirebenderMessage(world, newTime);
		//				dayNight.remove(worldName);
		//				dayNight.put(worldName, newTime);
		//			}
		//
		//			if (!Methods.isDay(world) && time.equals(Time.DAY)) {
		//				final Time newTime = Time.NIGHT;
		//				sendFirebenderMessage(world, newTime);
		//				dayNight.remove(worldName);
		//				dayNight.put(worldName, newTime);
		//			}
		//
		//			if (Methods.isNight(world) && time.equals(Time.DAY)) {
		//				final Time newTime = Time.NIGHT;
		//				sendWaterbenderMessage(world, newTime);
		//				dayNight.remove(worldName);
		//				dayNight.put(worldName, newTime);
		//			}
		//
		//			if (!Methods.isNight(world) && time.equals(Time.NIGHT)) {
		//				final Time newTime = Time.DAY;
		//				sendWaterbenderMessage(world, Time.DAY);
		//				dayNight.remove(worldName);
		//				dayNight.put(worldName, newTime);
		//			}
		//		}

	}

//	private static enum Time {
//		DAY, NIGHT;
//	}

	private void sendFirebenderMessage(World world, boolean b) {
		if (b) {
			for (Player player : world.getPlayers()) {
				if (Methods.isBender(player.getName(), Element.Fire)
						&& player.hasPermission("bending.message.daymessage")) {
					player.sendMessage(ChatColor.RED + defaultsunrisemessage);
				}
			}
		} else {
			for (Player player : world.getPlayers()) {
				if (Methods.isBender(player.getName(), Element.Fire)
						&& player.hasPermission("bending.message.daymessage")) {
					player.sendMessage(ChatColor.RED + defaultsunsetmessage);
				}
			}
		}
	}

	private void sendWaterbenderMessage(World world, boolean b) {
		if (!b) {
			for (Player player : world.getPlayers()) {
				if (Methods.isBender(player.getName(), Element.Water)
						&& player.hasPermission("bending.message.nightmessage")) {
					if (Methods.isFullMoon(world)) {
						player.sendMessage(ChatColor.AQUA
								+ defaultfullmoonrisemessage);
					} else {
						player.sendMessage(ChatColor.AQUA
								+ defaultmoonrisemessage);
					}
				}
			}
		} else {
			for (Player player : world.getPlayers()) {
				if (Methods.isBender(player.getName(), Element.Water)
						&& player.hasPermission("bending.message.nightmessage")) {
					player.sendMessage(ChatColor.AQUA + defaultmoonsetmessage);
				}
			}
		}
	}
}
