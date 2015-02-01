package com.projectkorra.ProjectKorra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.SlimeSplitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.ComboManager.ClickType;
import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.CustomEvents.PlayerGrappleEvent;
import com.projectkorra.ProjectKorra.Objects.Preset;
import com.projectkorra.ProjectKorra.Utilities.GrapplingHookAPI;
import com.projectkorra.ProjectKorra.airbending.AirBlast;
import com.projectkorra.ProjectKorra.airbending.AirBubble;
import com.projectkorra.ProjectKorra.airbending.AirBurst;
import com.projectkorra.ProjectKorra.airbending.AirScooter;
import com.projectkorra.ProjectKorra.airbending.AirShield;
import com.projectkorra.ProjectKorra.airbending.AirSpout;
import com.projectkorra.ProjectKorra.airbending.AirSuction;
import com.projectkorra.ProjectKorra.airbending.AirSwipe;
import com.projectkorra.ProjectKorra.airbending.FlightAbility;
import com.projectkorra.ProjectKorra.airbending.Suffocate;
import com.projectkorra.ProjectKorra.airbending.Tornado;
import com.projectkorra.ProjectKorra.chiblocking.AcrobatStance;
import com.projectkorra.ProjectKorra.chiblocking.ChiComboManager;
import com.projectkorra.ProjectKorra.chiblocking.ChiPassive;
import com.projectkorra.ProjectKorra.chiblocking.HighJump;
import com.projectkorra.ProjectKorra.chiblocking.Paralyze;
import com.projectkorra.ProjectKorra.chiblocking.QuickStrike;
import com.projectkorra.ProjectKorra.chiblocking.RapidPunch;
import com.projectkorra.ProjectKorra.chiblocking.Smokescreen;
import com.projectkorra.ProjectKorra.chiblocking.SwiftKick;
import com.projectkorra.ProjectKorra.chiblocking.WarriorStance;
import com.projectkorra.ProjectKorra.earthbending.Catapult;
import com.projectkorra.ProjectKorra.earthbending.Collapse;
import com.projectkorra.ProjectKorra.earthbending.CompactColumn;
import com.projectkorra.ProjectKorra.earthbending.EarthArmor;
import com.projectkorra.ProjectKorra.earthbending.EarthBlast;
import com.projectkorra.ProjectKorra.earthbending.EarthColumn;
import com.projectkorra.ProjectKorra.earthbending.EarthGrab;
import com.projectkorra.ProjectKorra.earthbending.EarthPassive;
import com.projectkorra.ProjectKorra.earthbending.EarthSmash;
import com.projectkorra.ProjectKorra.earthbending.EarthTunnel;
import com.projectkorra.ProjectKorra.earthbending.EarthWall;
import com.projectkorra.ProjectKorra.earthbending.Extraction;
import com.projectkorra.ProjectKorra.earthbending.LavaFlow;
import com.projectkorra.ProjectKorra.earthbending.LavaFlow.AbilityType;
import com.projectkorra.ProjectKorra.earthbending.LavaSurge;
import com.projectkorra.ProjectKorra.earthbending.LavaWave;
import com.projectkorra.ProjectKorra.earthbending.MetalClips;
import com.projectkorra.ProjectKorra.earthbending.Shockwave;
import com.projectkorra.ProjectKorra.earthbending.Tremorsense;
import com.projectkorra.ProjectKorra.firebending.ArcOfFire;
import com.projectkorra.ProjectKorra.firebending.Combustion;
import com.projectkorra.ProjectKorra.firebending.Cook;
import com.projectkorra.ProjectKorra.firebending.Enflamed;
import com.projectkorra.ProjectKorra.firebending.Extinguish;
import com.projectkorra.ProjectKorra.firebending.FireBlast;
import com.projectkorra.ProjectKorra.firebending.FireBurst;
import com.projectkorra.ProjectKorra.firebending.FireJet;
import com.projectkorra.ProjectKorra.firebending.FireShield;
import com.projectkorra.ProjectKorra.firebending.FireStream;
import com.projectkorra.ProjectKorra.firebending.Fireball;
import com.projectkorra.ProjectKorra.firebending.Illumination;
import com.projectkorra.ProjectKorra.firebending.Lightning;
import com.projectkorra.ProjectKorra.firebending.RingOfFire;
import com.projectkorra.ProjectKorra.firebending.WallOfFire;
import com.projectkorra.ProjectKorra.waterbending.Bloodbending;
import com.projectkorra.ProjectKorra.waterbending.FreezeMelt;
import com.projectkorra.ProjectKorra.waterbending.IceBlast;
import com.projectkorra.ProjectKorra.waterbending.IceSpike2;
import com.projectkorra.ProjectKorra.waterbending.Melt;
import com.projectkorra.ProjectKorra.waterbending.OctopusForm;
import com.projectkorra.ProjectKorra.waterbending.Torrent;
import com.projectkorra.ProjectKorra.waterbending.WaterManipulation;
import com.projectkorra.ProjectKorra.waterbending.WaterPassive;
import com.projectkorra.ProjectKorra.waterbending.WaterSpout;
import com.projectkorra.ProjectKorra.waterbending.WaterWall;
import com.projectkorra.ProjectKorra.waterbending.WaterWave;
import com.projectkorra.ProjectKorra.waterbending.Wave;

public class PKListener implements Listener {

	ProjectKorra plugin;

	public PKListener(ProjectKorra plugin) {
		this.plugin = plugin;
	}

	public static HashMap<Integer, Integer> noFallEntities = new HashMap<Integer, Integer>(); // Grappling Hooks
	public static HashMap<String, Integer> noGrapplePlayers = new HashMap<String, Integer>(); // Grappling Hooks

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityDamageByBlock(EntityDamageByBlockEvent event) {
		if (event.getCause().equals(DamageCause.BLOCK_EXPLOSION)) {
			if (event.getDamager() == null) {
				event.setCancelled(true);
			}
		}

		if (event.getDamager() != null) {
			if (LavaWave.isBlockInWave(event.getDamager())) {
				event.setCancelled(true);
			}
		}

	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerGrapple(PlayerGrappleEvent event) {
		if (event.isCancelled()) return;
		if (!plugin.getConfig().getBoolean("Properties.CustomItems.GrapplingHook.Enable")) return;

		Player player = event.getPlayer();
		if (!Methods.isBender(player.getName(), Element.Chi) && (!Methods.isBender(player.getName(), Element.Earth) || !Methods.canMetalbend(player))) {
			event.setCancelled(true);
			return;
		}
		if (Methods.isBender(player.getName(), Element.Chi) && !player.hasPermission("bending.chi.grapplinghook")) {
			event.setCancelled(true);
			return;
		}

		if (Methods.isBender(player.getName(), Element.Earth) && !player.hasPermission("bending.earth.grapplinghook")) {
			event.setCancelled(true);
			return;
		}
		if (Paralyze.isParalyzed(player) || ChiComboManager.isParalyzed(player) || Bloodbending.isBloodbended(player) || Suffocate.isBreathbent(player)) {
			event.setCancelled(true);
		}

		event.getHookItem().setDurability((short) - 10);
		if (noGrapplePlayers.containsKey(player.getName())) {
			return;
		}

		Entity e = event.getPulledEntity();
		Location loc = event.getPullLocation();

		if (player.equals(e)) {
			if (player.getLocation().distance(loc) < 3) { // Too close
				GrapplingHookAPI.pullPlayerSlightly(player, loc);
			} else {
				GrapplingHookAPI.pullEntityToLocation(player, loc);
			}

			if (GrapplingHookAPI.addUse(player, event.getHookItem())) {
				GrapplingHookAPI.playGrappleSound(player.getLocation());
			}
			GrapplingHookAPI.addPlayerCooldown(player, 100);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onProjectileHit(ProjectileHitEvent event) {
		Integer id = event.getEntity().getEntityId();
		if (Smokescreen.snowballs.contains(id)) {
			Location loc = event.getEntity().getLocation();
			Smokescreen.playEffect(loc);
			for (Entity en: Methods.getEntitiesAroundPoint(loc, Smokescreen.radius)) {
				Smokescreen.applyBlindness(en);
			}
			Smokescreen.snowballs.remove(id);
		}
		//		if (Combustion.fireballs.contains(id)) {
		//			Location loc = event.getEntity().getLocation();
		////			for (Entity en: Methods.getEntitiesAroundPoint(loc, 4)) {
		////				if (en instanceof LivingEntity) {
		////					LivingEntity le = (LivingEntity) en;
		////					le.damage(ProjectKorra.plugin.getConfig().getDouble("Abilities.Fire.Combustion.Damage"));
		////				}
		////			}
		//		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void fishEvent(PlayerFishEvent event) {
		if (event.isCancelled()) return;
		Player player = event.getPlayer();
		if (GrapplingHookAPI.isGrapplingHook(player.getItemInHand())) {
			if (event.getState() == PlayerFishEvent.State.IN_GROUND) {
				Location loc = event.getHook().getLocation();
				for (Entity ent: event.getHook().getNearbyEntities(1.5, 1, 1.5)) {
					if (ent instanceof Item) {
						PlayerGrappleEvent e = new PlayerGrappleEvent(player, ent, player.getLocation());
						plugin.getServer().getPluginManager().callEvent(e);
						return;
					}
				}

				PlayerGrappleEvent e = new PlayerGrappleEvent(player, player, loc);
				plugin.getServer().getPluginManager().callEvent(e);
			}
		}
	}
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerInteraction(PlayerInteractEvent event) {
		if (event.isCancelled()) return;
		Player player = event.getPlayer();

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Methods.cooldowns.put(player.getName(), System.currentTimeMillis());
			ComboManager.addComboAbility(player, ClickType.RIGHTCLICK);
			String ability = Methods.getBoundAbility(player);
			if(ability != null && ability.equalsIgnoreCase("EarthSmash"))
				new EarthSmash(player, EarthSmash.ClickType.RIGHTCLICK);
		}
		if (Paralyze.isParalyzed(player) || ChiComboManager.isParalyzed(player) || Bloodbending.isBloodbended(player) || Suffocate.isBreathbent(player)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled()) return;
		Player player = event.getPlayer();
		Methods.cooldowns.put(player.getName(), System.currentTimeMillis());
		if (Paralyze.isParalyzed(player) || ChiComboManager.isParalyzed(player) || Bloodbending.isBloodbended(player) || Suffocate.isBreathbent(player)) {
			event.setCancelled(true);
		}
	}


	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockFlowTo(BlockFromToEvent event) {
		if (event.isCancelled()) return;
		Block toblock = event.getToBlock();
		Block fromblock = event.getBlock();
		if (Methods.isLava(fromblock)) {
			event.setCancelled(!EarthPassive.canFlowFromTo(fromblock, toblock));
		}
		if (Methods.isWater(fromblock)) {
			event.setCancelled(!AirBubble.canFlowTo(toblock));
			if (!event.isCancelled()) {
				event.setCancelled(!WaterManipulation.canFlowFromTo(fromblock,
						toblock));
			}
			if (!event.isCancelled()) {
				if (Illumination.blocks.containsKey(toblock))
					toblock.setType(Material.AIR);
			}
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		Methods.createBendingPlayer(e.getPlayer().getUniqueId(), player.getName());
		Preset.loadPresets(player);
		String append = "";
		boolean chatEnabled = ProjectKorra.plugin.getConfig().getBoolean("Properties.Chat.Enable");
		if ((player.hasPermission("bending.avatar") || Methods.getBendingPlayer(player.getName()).elements.size() > 1) && chatEnabled) {
			append = plugin.getConfig().getString("Properties.Chat.Prefixes.Avatar");
		} else if (Methods.isBender(player.getName(), Element.Air) && chatEnabled) {
			append = plugin.getConfig().getString("Properties.Chat.Prefixes.Air");
		} else if (Methods.isBender(player.getName(), Element.Water) && chatEnabled) {
			append = plugin.getConfig().getString("Properties.Chat.Prefixes.Water");
		} else if (Methods.isBender(player.getName(), Element.Earth) && chatEnabled) {
			append = plugin.getConfig().getString("Properties.Chat.Prefixes.Earth");
		} else if (Methods.isBender(player.getName(), Element.Fire) && chatEnabled) {
			append = plugin.getConfig().getString("Properties.Chat.Prefixes.Fire");
		} else if (Methods.isBender(player.getName(), Element.Chi) && chatEnabled) {
			append = plugin.getConfig().getString("Properties.Chat.Prefixes.Chi");
		}

		if (chatEnabled) {
			player.setDisplayName(append + player.getName());
		}
		
		// Handle the AirSpout/WaterSpout login glitches
		if (player.getGameMode() != GameMode.CREATIVE) {
			HashMap<Integer, String> bound = Methods.getBendingPlayer(player.getName()).getAbilities();
			for(String str : bound.values())
				if(str.equalsIgnoreCase("AirSpout") || str.equalsIgnoreCase("WaterSpout")) {
					final Player fplayer = player;
					new BukkitRunnable() {
						public void run() {
							fplayer.setFlying(false);
							fplayer.setAllowFlight(false);
						}
					}.runTaskLater(ProjectKorra.plugin, 2);
					break;
				}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		
		Player player = event.getPlayer();
		
		if (Commands.invincible.contains(event.getPlayer().getName())) {
			Commands.invincible.remove(event.getPlayer().getName());
		}
		Preset.unloadPreset(player);
		BendingPlayer.players.remove(event.getPlayer().getName());
		if (EarthArmor.instances.containsKey(event.getPlayer())) {
			EarthArmor.removeEffect(event.getPlayer());
			event.getPlayer().removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
		}
		
		for(Player p : MetalClips.instances.keySet())
		{
			if(MetalClips.instances.get(p).getTarget() != null &&
					MetalClips.instances.get(p).getTarget().getEntityId() == event.getPlayer().getEntityId())
			{
				MetalClips.instances.get(p).remove();
			}
		}
		
		com.projectkorra.ProjectKorra.airbending.FlightAbility.remove(event.getPlayer());
	}
	
	@EventHandler
	public void playerIsKicked(PlayerKickEvent event) {
		if(event.isCancelled()) return;
		
		com.projectkorra.ProjectKorra.airbending.FlightAbility.remove(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerSneak(PlayerToggleSneakEvent event) {
		Player player = event.getPlayer();

		if (event.isCancelled()) return;
		
		if(player.isSneaking())
			ComboManager.addComboAbility(player, ComboManager.ClickType.SHIFTUP);
		else
			ComboManager.addComboAbility(player, ComboManager.ClickType.SHIFTDOWN);
		
		if(Suffocate.isBreathbent(player)) {
			if(!Methods.getBoundAbility(player).equalsIgnoreCase("AirSwipe") || !Methods.getBoundAbility(player).equalsIgnoreCase("FireBlast") || !Methods.getBoundAbility(player).equalsIgnoreCase("EarthBlast") || !Methods.getBoundAbility(player).equalsIgnoreCase("WaterManipulation")) {
				event.setCancelled(true);
			}
		}

		if (Paralyze.isParalyzed(player) || ChiComboManager.isParalyzed(player) || Bloodbending.isBloodbended(player)) {
			event.setCancelled(true);
			return;
		}

		AirScooter.check(player);

		String abil = Methods.getBoundAbility(player);
		if (abil == null) {
			return;
		}

		if (Methods.isChiBlocked(player.getName())) {
			event.setCancelled(true);
			return;
		}

		if (!player.isSneaking() && Methods.canBend(player.getName(), abil)) {
                        if (Methods.isDisabledStockAbility(abil))
                            return;
			if (Methods.isAirAbility(abil)) {
				if (Methods.isWeapon(player.getItemInHand().getType()) && !plugin.getConfig().getBoolean("Properties.Air.CanBendWithWeapons")) {
					return;
				}
				if (abil.equalsIgnoreCase("Tornado")) {
					new Tornado(player);
				}
				if (abil.equalsIgnoreCase("AirBlast")) {
					AirBlast.setOrigin(player);
				}
				if (abil.equalsIgnoreCase("AirBurst")) {
					new AirBurst(player);
				}
				if (abil.equalsIgnoreCase("AirSuction")) {
					AirSuction.setOrigin(player);
				}
				if (abil.equalsIgnoreCase("AirSwipe")) {
					AirSwipe.charge(player);
				}
				if (abil.equalsIgnoreCase("AirShield")) {
					new AirShield(player);
				}
				if(abil.equalsIgnoreCase("Suffocate")) {
					new Suffocate(player);
				}
				if(abil.equalsIgnoreCase("Flight")) {
					if(player.isSneaking() || !Methods.canAirFlight(player)) return;
					new com.projectkorra.ProjectKorra.airbending.FlightAbility(player);
				}

			}

			if (Methods.isWaterAbility(abil)) {
				if (Methods.isWeapon(player.getItemInHand().getType()) && !plugin.getConfig().getBoolean("Properties.Water.CanBendWithWeapons")) {
					return;
				}
				if (abil.equalsIgnoreCase("Bloodbending")) {
					new Bloodbending(player);
				}
				if (abil.equalsIgnoreCase("IceBlast")) {
					new IceBlast(player);
				}
				if (abil.equalsIgnoreCase("IceSpike")) {
					new IceSpike2(player);
				}
				if (abil.equalsIgnoreCase("OctopusForm")) {
					OctopusForm.form(player);
				}
				if (abil.equalsIgnoreCase("PhaseChange")) {
					new Melt(player);
				}
				if (abil.equalsIgnoreCase("WaterManipulation")) {
					new WaterManipulation(player);
				}
				if (abil.equalsIgnoreCase("Surge")) {
					WaterWall.form(player);
				}
				if (abil.equalsIgnoreCase("Torrent")) {
					Torrent.create(player);
				}
			}

			if (Methods.isEarthAbility(abil)) {
				if (Methods.isWeapon(player.getItemInHand().getType()) && !plugin.getConfig().getBoolean("Properties.Earth.CanBendWithWeapons")) {
					return;
				}
				if (abil.equalsIgnoreCase("EarthBlast")) {
					new EarthBlast(player);
				}
				if (abil.equalsIgnoreCase("RaiseEarth")) {
					new EarthWall(player);
				}
				if (abil.equalsIgnoreCase("Collapse")) {
					new Collapse(player);
				}
				if (abil.equalsIgnoreCase("Shockwave")) {
					new Shockwave(player);
				}
				if (abil.equalsIgnoreCase("EarthGrab")) {
					EarthGrab.EarthGrabSelf(player);
				}
				if (abil.equalsIgnoreCase("EarthTunnel")) {
					new EarthTunnel(player);
				}

				if (abil.equalsIgnoreCase("Tremorsense")) {
					Methods.getBendingPlayer(player.getName()).toggleTremorsense();
				}

				if (abil.equalsIgnoreCase("Extraction")) {
					new Extraction(player);
				}
				
				if(abil.equalsIgnoreCase("MetalClips"))
				{
					if(MetalClips.instances.containsKey(player))
					{
						if(MetalClips.instances.get(player).getTarget() == null)
							MetalClips.instances.get(player).magnet();
						else
							MetalClips.instances.get(player).control();
					}
					else
						new MetalClips(player, 1);
				}

//				if (abil.equalsIgnoreCase("LavaSurge")) {
//					new LavaSurge(player);
//				}
				
				if (abil.equalsIgnoreCase("LavaFlow")) {
					new LavaFlow(player,LavaFlow.AbilityType.SHIFT);
				}
				if (abil.equalsIgnoreCase("EarthSmash")) {
					new EarthSmash(player, EarthSmash.ClickType.SHIFT);
				}

			}

			if (Methods.isFireAbility(abil)) {
				if (Methods.isWeapon(player.getItemInHand().getType()) && !plugin.getConfig().getBoolean("Properties.Fire.CanBendWithWeapons")) {
					return;
				}
				if (abil.equalsIgnoreCase("Blaze")) {
					new RingOfFire(player);
				}
				if (abil.equalsIgnoreCase("FireBlast")) {
					new Fireball(player);
				}
				if (abil.equalsIgnoreCase("HeatControl")) {
					new Cook(player);
				}
				if (abil.equalsIgnoreCase("FireBurst")) {
					new FireBurst(player);
				}
				if (abil.equalsIgnoreCase("FireShield")) {
					FireShield.shield(player);
				}
				if (abil.equalsIgnoreCase("Lightning")) {
					new Lightning(player);
				}
				if (abil.equalsIgnoreCase("Combustion")) {
					new Combustion(player);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockIgnite(BlockIgniteEvent event) {
		if (event.isCancelled()) return;
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		if (event.isCancelled()) return;

		Player player = event.getPlayer();
		if (Paralyze.isParalyzed(player)) {
			event.setCancelled(true);
			return;
		}
		
		if(ChiComboManager.isParalyzed(player))
		{
			event.setTo(event.getFrom());
			return;
		}

		if (Suffocate.isBreathbent(player)) {
			Location loc = event.getFrom();
			Location toLoc = player.getLocation();

			if (loc.getX() != toLoc.getX() || loc.getY() != toLoc.getY() || loc.getZ() != toLoc.getZ()) {
				event.setCancelled(true);
				return;
			}
		}

		if (WaterSpout.instances.containsKey(event.getPlayer()) || AirSpout.getPlayers().contains(event.getPlayer())) {
			Vector vel = new Vector();
			vel.setX(event.getTo().getX() - event.getFrom().getX());
			vel.setY(event.getTo().getY() - event.getFrom().getY());
			vel.setZ(event.getTo().getZ() - event.getFrom().getZ());
			// You now know the old velocity. Set to match recommended velocity
			double currspeed = vel.length();
			double maxspeed = .15;
			if (currspeed > maxspeed) {
				// only if moving set a factor
				// double recspeed = 0.6;
				// vel = vel.ultiply(recspeed * currspeed);
				vel = vel.normalize().multiply(maxspeed);
				// apply the new velocity (MAY REQUIRE A SCHEDULED TASK
				// INSTEAD!)
				event.getPlayer().setVelocity(vel);
			}
		}

		if (Bloodbending.isBloodbended(player)) {
			double distance1, distance2;
			Location loc = Bloodbending.getBloodbendingLocation(player);
			distance1 = event.getFrom().distance(loc);
			distance2 = event.getTo().distance(loc);
			if (distance2 > distance1) {
				player.setVelocity(new Vector(0, 0, 0));
			}
		}
		
		if(FlightAbility.instances.containsKey(event.getPlayer().getName())) {
			if(com.projectkorra.ProjectKorra.airbending.FlightAbility.isHovering(event.getPlayer())) {
				Location loc = event.getFrom();
				Location toLoc = player.getLocation();

				if (loc.getX() != toLoc.getX() || loc.getY() != toLoc.getY() || loc.getZ() != toLoc.getZ()) {
					event.setCancelled(true);
					return;
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityTargetLiving(EntityTargetLivingEntityEvent event) {
		if (event.isCancelled()) return;

		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || ChiComboManager.isParalyzed(entity) || Bloodbending.isBloodbended(entity)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onTarget(EntityTargetEvent event) {
		if (event.isCancelled()) return;

		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || ChiComboManager.isParalyzed(entity) || Bloodbending.isBloodbended(entity)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityChangeBlockEvent(EntityChangeBlockEvent event) {
		if (event.isCancelled()) return;

		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || ChiComboManager.isParalyzed(entity) || Bloodbending.isBloodbended(entity) || Suffocate.isBreathbent(entity))
			event.setCancelled(true);
		
		if (event.getEntityType() == EntityType.FALLING_BLOCK) {
			if (LavaSurge.falling.contains(entity)) {
				LavaSurge.falling.remove(entity);
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent event) {
		if (event.isCancelled()) return;

		for (Block block : event.blockList()) {
			EarthBlast blast = EarthBlast.getBlastFromSource(block);

			if (blast != null) {
				blast.cancel();
			}
			if (FreezeMelt.frozenblocks.containsKey(block)) {
				FreezeMelt.thaw(block);
			}
			if (WaterWall.wallblocks.containsKey(block)) {
				block.setType(Material.AIR);
			}
			if (!Wave.canThaw(block)) {
				Wave.thaw(block);
			}
			if (Methods.movedearth.containsKey(block)) {
				Methods.removeRevertIndex(block);
			}
		}



	}


	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityExplodeEvent(EntityExplodeEvent event) {
		if (event.isCancelled()) return;

		Entity entity = event.getEntity();
		if (entity != null)
			if (Paralyze.isParalyzed(entity) || ChiComboManager.isParalyzed(entity)
					|| Bloodbending.isBloodbended(entity) || Suffocate.isBreathbent(entity))
				event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityInteractEvent(EntityInteractEvent event) {
		if (event.isCancelled()) return;

		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || ChiComboManager.isParalyzed(entity) || Bloodbending.isBloodbended(entity) || Suffocate.isBreathbent(entity))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityShootBowEvent(EntityShootBowEvent event) {
		if (event.isCancelled()) return;

		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || ChiComboManager.isParalyzed(entity) || Bloodbending.isBloodbended(entity) || Suffocate.isBreathbent(entity))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityTeleportEvent(EntityTeleportEvent event) {
		if (event.isCancelled()) return;

		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || ChiComboManager.isParalyzed(entity) || Bloodbending.isBloodbended(entity) || Suffocate.isBreathbent(entity))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityProjectileLaunchEvent(ProjectileLaunchEvent event) {
		if (event.isCancelled()) return;

		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || ChiComboManager.isParalyzed(entity) || Bloodbending.isBloodbended(entity) || Suffocate.isBreathbent(entity))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntitySlimeSplitEvent(SlimeSplitEvent event) {
		if (event.isCancelled()) return;

		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || ChiComboManager.isParalyzed(entity) || Bloodbending.isBloodbended(entity) || Suffocate.isBreathbent(entity))
			event.setCancelled(true);
	}


	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true) 
	public void onPlayerSwing(PlayerAnimationEvent event) {
		if (event.isCancelled()) return;

		Player player = event.getPlayer();
		ComboManager.addComboAbility(player, ComboManager.ClickType.LEFTCLICK);
		
		if(Suffocate.isBreathbent(player)) {
			if(!Methods.getBoundAbility(player).equalsIgnoreCase("AirSwipe") || !Methods.getBoundAbility(player).equalsIgnoreCase("FireBlast") || !Methods.getBoundAbility(player).equalsIgnoreCase("EarthBlast") || !Methods.getBoundAbility(player).equalsIgnoreCase("WaterManipulation")) {
				event.setCancelled(true);
			}
		}

		if (Bloodbending.isBloodbended(player) || Paralyze.isParalyzed(player) || ChiComboManager.isParalyzed(player)) {
			event.setCancelled(true);
			return;
		}

		if (Methods.isChiBlocked(player.getName())) {
			event.setCancelled(true);
			return;
		}

		AirScooter.check(player);

		String abil = Methods.getBoundAbility(player);
		if (abil == null) return;
		if (Methods.canBend(player.getName(), abil)) {
                        if (Methods.isDisabledStockAbility(abil))
                            return;

			if (Methods.isAirAbility(abil)) {
				if (Methods.isWeapon(player.getItemInHand().getType()) && !plugin.getConfig().getBoolean("Properties.Air.CanBendWithWeapons")) {
					return;
				}
				if (abil.equalsIgnoreCase("AirBlast")) {
					new AirBlast(player);
				}
				if (abil.equalsIgnoreCase("AirSuction")) {
					new AirSuction(player);
				}
				if (abil.equalsIgnoreCase("AirBurst")) {
					AirBurst.coneBurst(player);
				}
				if (abil.equalsIgnoreCase("AirScooter")) {
					new AirScooter(player);
				}
				if (abil.equalsIgnoreCase("AirSpout")) {
					new AirSpout(player);
				}
				if (abil.equalsIgnoreCase("AirSwipe")) {
					new AirSwipe(player);
				}
				if(abil.equalsIgnoreCase("Flight")) {
					if(!ProjectKorra.plugin.getConfig().getBoolean("Abilities.Air.Flight.HoverEnabled")
							|| !Methods.canAirFlight(player)) return;
					
					if(com.projectkorra.ProjectKorra.airbending.FlightAbility.instances.containsKey(event.getPlayer().getName())) {
						if(com.projectkorra.ProjectKorra.airbending.FlightAbility.isHovering(event.getPlayer())) {
							com.projectkorra.ProjectKorra.airbending.FlightAbility.setHovering(event.getPlayer(), false);
						}else{
							com.projectkorra.ProjectKorra.airbending.FlightAbility.setHovering(event.getPlayer(), true);
						}
					}
				}
			}
			if (Methods.isWaterAbility(abil)) {
				if (Methods.isWeapon(player.getItemInHand().getType()) && !plugin.getConfig().getBoolean("Properties.Water.CanBendWithWeapons")) {
					return;
				}
				if (abil.equalsIgnoreCase("Bloodbending")) {
					Bloodbending.launch(player);
				}
				if (abil.equalsIgnoreCase("IceBlast")) {
					IceBlast.activate(player);
				}
				if (abil.equalsIgnoreCase("IceSpike")) {
					IceSpike2.activate(player);
				}
				if (abil.equalsIgnoreCase("OctopusForm")) {
					new OctopusForm(player);
				}
				if (abil.equalsIgnoreCase("PhaseChange")) {
					new FreezeMelt(player);
				}
				if (abil.equalsIgnoreCase("WaterSpout")) {
					new WaterSpout(player);
				}
				if (abil.equalsIgnoreCase("WaterManipulation")) {
					WaterManipulation.moveWater(player);
				}
				if (abil.equalsIgnoreCase("Surge")) {
					new WaterWall(player);
				}
				if (abil.equalsIgnoreCase("Torrent")) {
					new Torrent(player);
				}
			}

			if (Methods.isEarthAbility(abil)) {
				if (Methods.isWeapon(player.getItemInHand().getType()) && !plugin.getConfig().getBoolean("Properties.Earth.CanBendWithWeapons")) {
					return;
				}
				if (abil.equalsIgnoreCase("Catapult")) {
					new Catapult(player);
				}

				if (abil.equalsIgnoreCase("EarthBlast")) {
					EarthBlast.throwEarth(player);
				}

				if (abil.equalsIgnoreCase("RaiseEarth")) {
					new EarthColumn(player);
				}

				if (abil.equalsIgnoreCase("Collapse")) {
					new CompactColumn(player);
				}
				if (abil.equalsIgnoreCase("Shockwave")) {
					Shockwave.coneShockwave(player);
				}
				if (abil.equalsIgnoreCase("EarthArmor")) {
					new EarthArmor(player);
				}

				if (abil.equalsIgnoreCase("EarthGrab")) {
					new EarthGrab(player);
				}

				if (abil.equalsIgnoreCase("Tremorsense")) {
					new Tremorsense(player);
				}
				
				if(abil.equalsIgnoreCase("MetalClips"))
				{
					if(!MetalClips.instances.containsKey(player))
						new MetalClips(player, 0);
					else if(MetalClips.instances.containsKey(player))
						if(MetalClips.instances.get(player).metalclips < (player.hasPermission("bending.ability.MetalClips.4clips") ? 4 : 3))
							MetalClips.instances.get(player).shootMetal();
						else
							MetalClips.instances.get(player).launch();
				}

				if (abil.equalsIgnoreCase("LavaSurge")) {
					if(LavaSurge.instances.containsKey(player))
						LavaSurge.instances.get(player).launch();
				}
				
				if (abil.equalsIgnoreCase("LavaFlow")) {
					new LavaFlow(player,AbilityType.CLICK);
				}
				
				if (abil.equalsIgnoreCase("EarthSmash")) {
					new EarthSmash(player, EarthSmash.ClickType.LEFTCLICK);
				}
			}
			if (Methods.isFireAbility(abil)) {
				if (Methods.isWeapon(player.getItemInHand().getType()) && !plugin.getConfig().getBoolean("Properties.Fire.CanBendWithWeapons")) {
					return;
				}

				if (abil.equalsIgnoreCase("Blaze")) {
					new ArcOfFire(player);
				}
				if (abil.equalsIgnoreCase("FireBlast")) {
					new FireBlast(player);
				}
				if (abil.equalsIgnoreCase("FireJet")) {
					new FireJet(player);
				}
				if (abil.equalsIgnoreCase("HeatControl")) {
					new Extinguish(player);
				}
				if (abil.equalsIgnoreCase("Illumination")) {
					new Illumination(player);
				}
				if (abil.equalsIgnoreCase("FireBurst")) {
					FireBurst.coneBurst(player);
				}
				if (abil.equalsIgnoreCase("FireShield")) {
					new FireShield(player);
				}
				if (abil.equalsIgnoreCase("WallOfFire")) {
					new WallOfFire(player);
				}
				if (abil.equalsIgnoreCase("Combustion")) {
					Combustion.explode(player);
				}
			}

			if (Methods.isChiAbility(abil)) {
				if (Methods.isWeapon(player.getItemInHand().getType()) && !plugin.getConfig().getBoolean("Properties.Chi.CanBendWithWeapons")) {
					return;
				}
				if (abil.equalsIgnoreCase("HighJump")) {
					new HighJump(player);
				}
				if (abil.equalsIgnoreCase("RapidPunch")) {
					new RapidPunch(player);
				}
				if (abil.equalsIgnoreCase("Paralyze")) {
					//
				}
				if (abil.equalsIgnoreCase("Smokescreen")) {
					new Smokescreen(player);
				}
				if (abil.equalsIgnoreCase("WarriorStance")) {
					new WarriorStance(player);
				}
				
				if (abil.equalsIgnoreCase("AcrobatStance")) {
					new AcrobatStance(player);
				}
				
				if (abil.equalsIgnoreCase("QuickStrike"))
				{
					new QuickStrike(player);
				}
				
				if (abil.equalsIgnoreCase("SwiftKick"))
				{
					new SwiftKick(player);
				}
			}

			if (abil.equalsIgnoreCase("AvatarState")) {
				new AvatarState(player);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.isCancelled()) return;

		for(Player p : MetalClips.instances.keySet())
		{
			if(MetalClips.instances.get(p).getTarget() != null)
				if(MetalClips.instances.get(p).getTarget().getEntityId() == event.getWhoClicked().getEntityId())
					event.setCancelled(true);
		}
		
		if (event.getSlotType() == SlotType.ARMOR
				&& !EarthArmor.canRemoveArmor((Player) event.getWhoClicked()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (EarthArmor.instances.containsKey(event.getEntity())) {
			List<ItemStack> drops = event.getDrops();
			List<ItemStack> newdrops = new ArrayList<ItemStack>();
			for (int i = 0; i < drops.size(); i++) {
				if (!(drops.get(i).getType() == Material.LEATHER_BOOTS
						|| drops.get(i).getType() == Material.LEATHER_CHESTPLATE
						|| drops.get(i).getType() == Material.LEATHER_HELMET
						|| drops.get(i).getType() == Material.LEATHER_LEGGINGS || drops
						.get(i).getType() == Material.AIR))
					newdrops.add((drops.get(i)));
			}
			if (EarthArmor.instances.get(event.getEntity()).oldarmor != null) {
				for (ItemStack is : EarthArmor.instances.get(event.getEntity()).oldarmor) {
					if (!(is.getType() == Material.AIR))
						newdrops.add(is);
				}
			}
			event.getDrops().clear();
			event.getDrops().addAll(newdrops);
			EarthArmor.removeEffect(event.getEntity());
		}
		if (MetalClips.instances.containsKey(event.getEntity())) {
			MetalClips.instances.get(event.getEntity()).remove();
			List<ItemStack> drops = event.getDrops();
			List<ItemStack> newdrops = new ArrayList<ItemStack>();
			for (int i = 0; i < drops.size(); i++) {
				if (!(drops.get(i).getType() == Material.IRON_HELMET
						|| drops.get(i).getType() == Material.IRON_CHESTPLATE
						|| drops.get(i).getType() == Material.IRON_LEGGINGS
						|| drops.get(i).getType() == Material.IRON_BOOTS
						|| drops.get(i).getType() == Material.AIR))
					newdrops.add((drops.get(i)));
			}
			event.getDrops().clear();
			event.getDrops().addAll(newdrops);
			
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true) 
	public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
		if (event.isCancelled()) return;

		Player p = event.getPlayer();
		if (Tornado.getPlayers().contains(p) || Bloodbending.isBloodbended(p) || Suffocate.isBreathbent(p)
				|| FireJet.getPlayers().contains(p)
				|| AvatarState.getPlayers().contains(p)) {
			event.setCancelled(p.getGameMode() != GameMode.CREATIVE);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityCombust(EntityCombustEvent event) {
		if (event.isCancelled()) return;

		Entity entity = event.getEntity();
		Block block = entity.getLocation().getBlock();
		if (FireStream.ignitedblocks.containsKey(block) && entity instanceof LivingEntity) {
			new Enflamed(entity, FireStream.ignitedblocks.get(block));
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityDamageBlock(EntityDamageByBlockEvent event) {

	}
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityDamageEvent(EntityDamageEvent event) {
		if (event.isCancelled()) return;

		Entity entity = event.getEntity();

		if (event.getCause() == DamageCause.FIRE && FireStream.ignitedblocks.containsKey(entity.getLocation().getBlock())) {
			new Enflamed(entity, FireStream.ignitedblocks.get(entity.getLocation().getBlock()));
		}

		if (Enflamed.isEnflamed(entity) && event.getCause() == DamageCause.FIRE_TICK) {
			event.setCancelled(true);
			Enflamed.dealFlameDamage(entity);
		}

		if (entity instanceof Player) {
			Player player = (Player) entity;
			if (Methods.getBoundAbility(player) != null && Methods.getBoundAbility(player).equalsIgnoreCase("HeatControl")) {
				if (event.getCause() == DamageCause.FIRE || event.getCause() == DamageCause.FIRE_TICK) {
					player.setFireTicks(0);
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockMeltEvent(BlockFadeEvent event) {
		if (event.isCancelled()) return;

		Block block = event.getBlock();
		if (block.getType() == Material.FIRE) {
			return;
		}
		event.setCancelled(Illumination.blocks.containsKey(block));
		if (!event.isCancelled()) {
			event.setCancelled(!WaterManipulation.canPhysicsChange(block));
		}
		if (!event.isCancelled()) {
			event.setCancelled(!EarthPassive.canPhysicsChange(block));
		}
		if (!event.isCancelled()) {
			event.setCancelled(FreezeMelt.frozenblocks.containsKey(block));
		}
		if (!event.isCancelled()) {
			event.setCancelled(!Wave.canThaw(block));
		}
		if (!event.isCancelled()) {
			event.setCancelled(!Torrent.canThaw(block));
		}
		if (FireStream.ignitedblocks.containsKey(block)) {
			FireStream.remove(block);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) return;

		Block block = event.getBlock();
		Player player = event.getPlayer();
		if (WaterWall.wasBrokenFor(player, block)
				|| OctopusForm.wasBrokenFor(player, block)
				|| Torrent.wasBrokenFor(player, block) 
				|| WaterWave.wasBrokenFor(player, block)){
			event.setCancelled(true);
			return;
		}
		EarthBlast blast = EarthBlast.getBlastFromSource(block);
		if (blast != null) {
			blast.cancel();
		}

		if (FreezeMelt.frozenblocks.containsKey(block)) {
			FreezeMelt.thaw(block);
			event.setCancelled(true);
			// } else if (!WalkOnWater.canThaw(block)) {
			// WalkOnWater.thaw(block);
		} else if (WaterWall.wallblocks.containsKey(block)) {
			WaterWall.thaw(block);
			event.setCancelled(true);
		} else if (Illumination.blocks.containsKey(block)) {
			event.setCancelled(true);
			// } else if (Illumination.blocks.containsKey(block
			// .getRelative(BlockFace.UP))) {
			// event.setCancelled(true);
		} else if (!Wave.canThaw(block)) {
			Wave.thaw(block);
			event.setCancelled(true);
			// event.setCancelled(true);
		} else if (Methods.movedearth.containsKey(block)) {
			// Methods.removeEarthbendedBlockIndex(block);
			Methods.removeRevertIndex(block);
		} else if (TempBlock.isTempBlock(block)) {
			TempBlock.revertBlock(block, Material.AIR);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (event.isCancelled()) return;

		if (!plugin.getConfig().getBoolean("Properties.Chat.Enable")) {
			return;
		}

		Player player = event.getPlayer();
		ChatColor color = ChatColor.WHITE;

		if (player.hasPermission("bending.avatar") || Methods.getBendingPlayer(player.getName()).elements.size() > 1) {
			color = ChatColor.valueOf(plugin.getConfig().getString("Properties.Chat.Colors.Avatar"));
		} else if (Methods.isBender(player.getName(), Element.Air)) {
			color = ChatColor.valueOf(plugin.getConfig().getString("Properties.Chat.Colors.Air"));
		} else if (Methods.isBender(player.getName(), Element.Water)) {
			color = ChatColor.valueOf(plugin.getConfig().getString("Properties.Chat.Colors.Water"));
		} else if (Methods.isBender(player.getName(), Element.Earth)) {
			color = ChatColor.valueOf(plugin.getConfig().getString("Properties.Chat.Colors.Earth"));
		} else if (Methods.isBender(player.getName(), Element.Fire)) {
			color = ChatColor.valueOf(plugin.getConfig().getString("Properties.Chat.Colors.Fire"));
		} else if (Methods.isBender(player.getName(), Element.Chi)) {
			color = ChatColor.valueOf(plugin.getConfig().getString("Properties.Chat.Colors.Chi"));
		}

		String format = plugin.getConfig().getString("Properties.Chat.Format");
		format = format.replace("<message>", "%2$s");
		format = format.replace("<name>", color + player.getDisplayName() + ChatColor.RESET);
		event.setFormat(format);

	}
	
	@EventHandler
	public void onEntitySuffocatedByTempBlocks(EntityDamageEvent event) {
		if(event.isCancelled()) return;
		
		if(event.getCause() == DamageCause.SUFFOCATION) {
			if(TempBlock.isTempBlock(event.getEntity().getLocation().add(0, 1, 0).getBlock())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onPlayerDamageByPlayer(EntityDamageByEntityEvent e) {
		if (e.isCancelled()) return;

		Entity source = e.getDamager();
		Entity entity = e.getEntity();
		Fireball fireball = Fireball.getFireball(source);

		if (fireball != null) {
			e.setCancelled(true);
			fireball.dealDamage(entity);
			return;
		}

		//		if (Combustion.fireballs.contains(source.getEntityId())) {
		//			e.setCancelled(true);
		//		}

		if (Paralyze.isParalyzed(e.getDamager())
				|| ChiComboManager.isParalyzed(e.getDamager())) {
			e.setCancelled(true);
			return;
		}
		
		if(entity instanceof Player) {
			Suffocate.remove((Player) entity);
		}

		Entity en = e.getEntity();
		if (en instanceof Player) {
			//			Player p = (Player) en; // This is the player getting hurt.
			if (e.getDamager() instanceof Player) { // This is the player hitting someone.
				Player sourceplayer = (Player) e.getDamager();
				Player targetplayer = (Player) e.getEntity();
				if (Methods.canBendPassive(sourceplayer.getName(), Element.Chi)) {
					if (Methods.isBender(sourceplayer.getName(), Element.Chi) && e.getCause() == DamageCause.ENTITY_ATTACK && e.getDamage() == 1) {
						if (Methods.isWeapon(sourceplayer.getItemInHand().getType()) && !plugin.getConfig().getBoolean("Properties.Chi.CanBendWithWeapons")) {
							return;
						}
						if (ChiPassive.willChiBlock(sourceplayer, targetplayer)) {
							if (Methods.getBoundAbility(sourceplayer) != null && Methods.getBoundAbility(sourceplayer).equalsIgnoreCase("Paralyze")) {
								new Paralyze(sourceplayer, targetplayer);
							} else {
								ChiPassive.blockChi(targetplayer);
							}
						}
						//						if (sourceplayer.getLocation().distance(targetplayer.getLocation()) <= plugin.getConfig().getDouble("Abilities.Chi.RapidPunch.Distance") && Methods.getBoundAbility(sourceplayer) == null) {
						//							if (Methods.isWeapon(sourceplayer.getItemInHand().getType()) && !plugin.getConfig().getBoolean("Properties.Chi.CanBendWithWeapons")) {
						//								return;
						//							} else {
						//								if (ChiPassive.willChiBlock(targetplayer)) {
						//									ChiPassive.blockChi(targetplayer);
						//									
						//								}
						//							}
						//						}
					}
				}
				if (Methods.canBendPassive(sourceplayer.getName(), Element.Chi)) {
					if (Methods.isWeapon(sourceplayer.getItemInHand().getType()) && !ProjectKorra.plugin.getConfig().getBoolean("Properties.Chi.CanBendWithWeapons")) {
						return;
					}
					if (e.getCause() == DamageCause.ENTITY_ATTACK) {
						if (Methods.getBoundAbility(sourceplayer) != null && Methods.getBoundAbility(sourceplayer).equalsIgnoreCase("Paralyze") && e.getDamage() == 1) {
							if (ChiPassive.willChiBlock(sourceplayer, targetplayer)) {
								new Paralyze(sourceplayer, targetplayer);
							}
						}
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerDamage(EntityDamageEvent event) {
		if (event.isCancelled()) return;

		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();

			if (Methods.isBender(player.getName(), Element.Earth) && event.getCause() == DamageCause.FALL) {
				Shockwave.fallShockwave(player);
			}

			if (Methods.isBender(player.getName(), Element.Air) && event.getCause() == DamageCause.FALL && Methods.canBendPassive(player.getName(), Element.Air)) {
				new Flight(player);
				player.setAllowFlight(true);
				AirBurst.fallBurst(player);
				player.setFallDistance(0);
				event.setDamage(0D);
				event.setCancelled(true);
			}

			if (!event.isCancelled() && Methods.isBender(player.getName(), Element.Water) && event.getCause() == DamageCause.FALL && Methods.canBendPassive(player.getName(), Element.Water)) {
				if (WaterPassive.applyNoFall(player)) {
					new Flight(player);
					player.setAllowFlight(true);
					player.setFallDistance(0);
					event.setDamage(0D);
					event.setCancelled(true);
				}
			}

			if (!event.isCancelled()
					&& Methods.isBender(player.getName(), Element.Earth)
					&& event.getCause() == DamageCause.FALL
					&& Methods.canBendPassive(player.getName(), Element.Earth)) {
				if (EarthPassive.softenLanding(player)) {
					new Flight(player);
					player.setAllowFlight(true);
					player.setFallDistance(0);
					event.setDamage(0D);
					event.setCancelled(true);
				}
			}

			if (!event.isCancelled()
					&& Methods.isBender(player.getName(), Element.Chi)
					&& event.getCause() == DamageCause.FALL
					&& Methods.canBendPassive(player.getName(), Element.Chi)) {
				if (player.isSprinting()) {
					event.setDamage(0);
					event.setCancelled(true);
				} else {
					double initdamage = event.getDamage();
					double newdamage = event.getDamage() * ChiPassive.FallReductionFactor;
					double finaldamage = initdamage - newdamage;
					event.setDamage(finaldamage);
				}
			}

			if (!event.isCancelled() && event.getCause() == DamageCause.FALL) {
				Player source = Flight.getLaunchedBy(player);
				if (source != null) {
					event.setCancelled(true);
					Methods.damageEntity(source, player, event.getDamage());
				}
			}

			if (Methods.canBendPassive(player.getName(), Element.Fire)
					&& Methods.isBender(player.getName(),  Element.Fire)
					&& (event.getCause() == DamageCause.FIRE || event.getCause() == DamageCause.FIRE_TICK)) {
				event.setCancelled(!Extinguish.canBurn(player));
			}

			if (Methods.isBender(player.getName(), Element.Earth)
					&& event.getCause() == DamageCause.SUFFOCATION && TempBlock.isTempBlock(player.getEyeLocation().getBlock())) {
				event.setDamage(0D);
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockPhysics(BlockPhysicsEvent event) {
		if (event.isCancelled()) return;

		Block block = event.getBlock();
		event.setCancelled(!WaterManipulation.canPhysicsChange(block));
		event.setCancelled(!EarthPassive.canPhysicsChange(block));
		if (!event.isCancelled())
			event.setCancelled(Illumination.blocks.containsKey(block));
		if (!event.isCancelled())
			event.setCancelled(Methods.tempnophysics.contains(block));
	}
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockForm(BlockFormEvent event) {
		if (event.isCancelled()) return;

		if (TempBlock.isTempBlock(event.getBlock()))
			event.setCancelled(true);
		if (!WaterManipulation.canPhysicsChange(event.getBlock()))
			event.setCancelled(true);
		if (!EarthPassive.canPhysicsChange(event.getBlock()))
			event.setCancelled(true);
	}



}
