package com.projectkorra.projectkorra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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
import org.bukkit.event.entity.EntityDeathEvent;
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
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.AvatarAbility;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.ability.util.PassiveManager;
import com.projectkorra.projectkorra.airbending.AirBlast;
import com.projectkorra.projectkorra.airbending.AirBurst;
import com.projectkorra.projectkorra.airbending.AirScooter;
import com.projectkorra.projectkorra.airbending.AirShield;
import com.projectkorra.projectkorra.airbending.AirSpout;
import com.projectkorra.projectkorra.airbending.AirSuction;
import com.projectkorra.projectkorra.airbending.AirSwipe;
import com.projectkorra.projectkorra.airbending.Suffocate;
import com.projectkorra.projectkorra.airbending.Tornado;
import com.projectkorra.projectkorra.airbending.flight.AirFlight;
import com.projectkorra.projectkorra.airbending.passive.GracefulDescent;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.chiblocking.AcrobatStance;
import com.projectkorra.projectkorra.chiblocking.HighJump;
import com.projectkorra.projectkorra.chiblocking.Paralyze;
import com.projectkorra.projectkorra.chiblocking.QuickStrike;
import com.projectkorra.projectkorra.chiblocking.RapidPunch;
import com.projectkorra.projectkorra.chiblocking.Smokescreen;
import com.projectkorra.projectkorra.chiblocking.SwiftKick;
import com.projectkorra.projectkorra.chiblocking.WarriorStance;
import com.projectkorra.projectkorra.chiblocking.combo.Immobilize;
import com.projectkorra.projectkorra.chiblocking.passive.Acrobatics;
import com.projectkorra.projectkorra.chiblocking.passive.ChiPassive;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.earthbending.Catapult;
import com.projectkorra.projectkorra.earthbending.Collapse;
import com.projectkorra.projectkorra.earthbending.CollapseWall;
import com.projectkorra.projectkorra.earthbending.EarthArmor;
import com.projectkorra.projectkorra.earthbending.EarthBlast;
import com.projectkorra.projectkorra.earthbending.EarthGrab;
import com.projectkorra.projectkorra.earthbending.EarthSmash;
import com.projectkorra.projectkorra.earthbending.EarthTunnel;
import com.projectkorra.projectkorra.earthbending.RaiseEarth;
import com.projectkorra.projectkorra.earthbending.RaiseEarthWall;
import com.projectkorra.projectkorra.earthbending.Shockwave;
import com.projectkorra.projectkorra.earthbending.Tremorsense;
import com.projectkorra.projectkorra.earthbending.lava.LavaFlow;
import com.projectkorra.projectkorra.earthbending.lava.LavaFlow.AbilityType;
import com.projectkorra.projectkorra.earthbending.lava.LavaSurge;
import com.projectkorra.projectkorra.earthbending.metal.Extraction;
import com.projectkorra.projectkorra.earthbending.metal.MetalClips;
import com.projectkorra.projectkorra.earthbending.passive.DensityShift;
import com.projectkorra.projectkorra.earthbending.passive.EarthPassive;
import com.projectkorra.projectkorra.earthbending.passive.FerroControl;
import com.projectkorra.projectkorra.earthbending.sand.SandSpout;
import com.projectkorra.projectkorra.event.EntityBendingDeathEvent;
import com.projectkorra.projectkorra.event.HorizontalVelocityChangeEvent;
import com.projectkorra.projectkorra.event.PlayerChangeElementEvent;
import com.projectkorra.projectkorra.event.PlayerChangeElementEvent.Result;
import com.projectkorra.projectkorra.event.PlayerJumpEvent;
import com.projectkorra.projectkorra.firebending.Blaze;
import com.projectkorra.projectkorra.firebending.BlazeArc;
import com.projectkorra.projectkorra.firebending.BlazeRing;
import com.projectkorra.projectkorra.firebending.FireBlast;
import com.projectkorra.projectkorra.firebending.FireBlastCharged;
import com.projectkorra.projectkorra.firebending.FireBurst;
import com.projectkorra.projectkorra.firebending.FireJet;
import com.projectkorra.projectkorra.firebending.FireManipulation;
import com.projectkorra.projectkorra.firebending.FireManipulation.FireManipulationType;
import com.projectkorra.projectkorra.firebending.FireShield;
import com.projectkorra.projectkorra.firebending.HeatControl;
import com.projectkorra.projectkorra.firebending.HeatControl.HeatControlType;
import com.projectkorra.projectkorra.firebending.Illumination;
import com.projectkorra.projectkorra.firebending.WallOfFire;
import com.projectkorra.projectkorra.firebending.combo.FireCombo;
import com.projectkorra.projectkorra.firebending.combustion.Combustion;
import com.projectkorra.projectkorra.firebending.lightning.Lightning;
import com.projectkorra.projectkorra.firebending.util.FireDamageTimer;
import com.projectkorra.projectkorra.object.HorizontalVelocityTracker;
import com.projectkorra.projectkorra.object.Preset;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.Flight;
import com.projectkorra.projectkorra.util.PassiveHandler;
import com.projectkorra.projectkorra.util.TempArmor;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.OctopusForm;
import com.projectkorra.projectkorra.waterbending.SurgeWall;
import com.projectkorra.projectkorra.waterbending.SurgeWave;
import com.projectkorra.projectkorra.waterbending.Torrent;
import com.projectkorra.projectkorra.waterbending.WaterBubble;
import com.projectkorra.projectkorra.waterbending.WaterManipulation;
import com.projectkorra.projectkorra.waterbending.WaterSpout;
import com.projectkorra.projectkorra.waterbending.blood.Bloodbending;
import com.projectkorra.projectkorra.waterbending.healing.HealingWaters;
import com.projectkorra.projectkorra.waterbending.ice.IceBlast;
import com.projectkorra.projectkorra.waterbending.ice.IceSpikeBlast;
import com.projectkorra.projectkorra.waterbending.ice.PhaseChange;
import com.projectkorra.projectkorra.waterbending.ice.PhaseChange.PhaseChangeType;
import com.projectkorra.projectkorra.waterbending.multiabilities.WaterArms;
import com.projectkorra.projectkorra.waterbending.passive.Hydrosink;
import com.projectkorra.projectkorra.waterbending.passive.WaterPassive;
import com.projectkorra.rpg.RPGMethods;

public class PKListener implements Listener {

	ProjectKorra plugin;

	private static final HashMap<Player, String> BENDING_PLAYER_DEATH = new HashMap<>(); // Player killed by Bending.
	private static final List<UUID> RIGHT_CLICK_INTERACT = new ArrayList<UUID>(); // Player right click block.
	private static final ArrayList<UUID> TOGGLED_OUT = new ArrayList<>(); // Stands for toggled = false while logging out.
	private static final Map<Player, Integer> JUMPS = new HashMap<>();

	public PKListener(ProjectKorra plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) {
			return;
		}

		Block block = event.getBlock();
		Player player = event.getPlayer();
		String abil = BendingPlayer.getBendingPlayer(player).getBoundAbilityName();
		CoreAbility ability = null;
		if (abil != null && abil.equalsIgnoreCase("Surge")) {
			ability = CoreAbility.getAbility(SurgeWall.class);
		} else if (abil != null && abil.equalsIgnoreCase("Torrent")) {
			ability = CoreAbility.getAbility(Torrent.class);
		} else {
			ability = CoreAbility.getAbility(abil);
		}
		if (ability != null && ability instanceof WaterAbility && !((WaterAbility) ability).allowBreakPlants() && WaterAbility.isPlantbendable(player, block.getType(), false)) {
			event.setCancelled(true);
			return;
		}

		EarthBlast blast = EarthBlast.getBlastFromSource(block);
		if (blast != null) {
			blast.remove();
		}

		if (PhaseChange.getFrozenBlocksAsBlock().contains(block)) {
			if (PhaseChange.thaw(block)) {
				event.setCancelled(true);
			}
		} else if (SurgeWall.getWallBlocks().containsKey(block)) {
			SurgeWall.thaw(block);
			event.setCancelled(true);
		} else if (Illumination.getBlocks().containsKey(block)) {
			event.setCancelled(true);
		} else if (!SurgeWave.canThaw(block)) {
			SurgeWave.thaw(block);
			event.setCancelled(true);
		} else if (EarthAbility.getMovedEarth().containsKey(block)) {
			EarthAbility.removeRevertIndex(block);
		} else if (TempBlock.isTempBlock(block)) {
			TempBlock.revertBlock(block, Material.AIR);
		} else if (EarthPassive.isPassiveSand(block)) {
			EarthPassive.revertSand(block);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockFlowTo(BlockFromToEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Block toblock = event.getToBlock();
		Block fromblock = event.getBlock();
		if (ElementalAbility.isLava(fromblock)) {
			event.setCancelled(!EarthPassive.canFlowFromTo(fromblock, toblock));
		}
		if (ElementalAbility.isWater(fromblock)) {
			event.setCancelled(!WaterBubble.isAir(toblock));
			if (!event.isCancelled()) {
				event.setCancelled(!WaterManipulation.canFlowFromTo(fromblock, toblock));
			}
			if (!event.isCancelled()) {
				if (Illumination.getBlocks().containsKey(toblock)) {
					toblock.setType(Material.AIR);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockForm(BlockFormEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (TempBlock.isTempBlock(event.getBlock())) {
			event.setCancelled(true);
		}
		if (!WaterManipulation.canPhysicsChange(event.getBlock())) {
			event.setCancelled(true);
		}
		if (!EarthPassive.canPhysicsChange(event.getBlock())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockIgnite(BlockIgniteEvent event) {
		if (event.isCancelled()) {
			return;
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockMeltEvent(BlockFadeEvent event) {
		if (event.isCancelled()) {
			return;
		}

		Block block = event.getBlock();
		if (block.getType() == Material.FIRE) {
			return;
		}
		event.setCancelled(Illumination.getBlocks().containsKey(block));
		if (!event.isCancelled()) {
			event.setCancelled(!WaterManipulation.canPhysicsChange(block));
		}
		if (!event.isCancelled()) {
			event.setCancelled(!EarthPassive.canPhysicsChange(block));
		}
		if (!event.isCancelled()) {
			event.setCancelled(PhaseChange.getFrozenBlocksAsBlock().contains(block));
		}
		if (!event.isCancelled()) {
			event.setCancelled(!SurgeWave.canThaw(block));
		}
		if (!event.isCancelled()) {
			event.setCancelled(!Torrent.canThaw(block));
		}
		if (BlazeArc.getIgnitedBlocks().containsKey(block)) {
			BlazeArc.removeBlock(block);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockPhysics(BlockPhysicsEvent event) {
		if (event.isCancelled()) {
			return;
		}

		Block block = event.getBlock();

		if (!WaterManipulation.canPhysicsChange(block) || !EarthPassive.canPhysicsChange(block) || Illumination.getBlocks().containsKey(block) || EarthAbility.getPreventPhysicsBlocks().contains(block)) {
			event.setCancelled(true);
		}

		// If there is a TempBlock of Air bellow FallingSand blocks, prevent it from updating.
		if (!event.isCancelled() && (block.getType() == Material.SAND || block.getType() == Material.GRAVEL || block.getType() == Material.ANVIL || block.getType() == Material.DRAGON_EGG) && TempBlock.isTempBlock(block.getRelative(BlockFace.DOWN)) && block.getRelative(BlockFace.DOWN).getType() == Material.AIR) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Player player = event.getPlayer();
		if (Paralyze.isParalyzed(player) || Immobilize.isParalyzed(player) || Bloodbending.isBloodbent(player) || Suffocate.isBreathbent(player)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onElementChange(PlayerChangeElementEvent event) {
		Player player = event.getTarget();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		PassiveManager.registerPassives(player);
		boolean chatEnabled = ConfigManager.languageConfig.get().getBoolean("Chat.Enable");
		if (chatEnabled) {
			Element element = event.getElement();
			String prefix = "";

			if (bPlayer == null) {
				return;
			}

			if (bPlayer.getElements().size() > 1) {
				prefix = Element.AVATAR.getPrefix();
			} else if (element != null) {
				prefix = element.getPrefix();
			} else {
				prefix = ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', ConfigManager.languageConfig.get().getString("Chat.Prefixes.Nonbender")) + " ";
			}
			player.setDisplayName(player.getName());
			player.setDisplayName(prefix + ChatColor.RESET + player.getDisplayName());
		}

		if (event.getResult() == Result.REMOVE) {
			if (GeneralMethods.hasRPG()) {
				RPGMethods.revokeAvatar(player.getUniqueId());
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityChangeBlockEvent(EntityChangeBlockEvent event) {
		if (event.isCancelled()) {
			return;
		}

		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || Immobilize.isParalyzed(entity) || Bloodbending.isBloodbent(entity) || Suffocate.isBreathbent(entity)) {
			event.setCancelled(true);
		}

		if (event.getEntityType() == EntityType.FALLING_BLOCK) {
			if (LavaSurge.getAllFallingBlocks().contains(entity)) {
				LavaSurge.getAllFallingBlocks().remove(entity);
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityCombust(EntityCombustEvent event) {
		if (event.isCancelled()) {
			return;
		}

		Entity entity = event.getEntity();
		Block block = entity.getLocation().getBlock();
		if (BlazeArc.getIgnitedBlocks().containsKey(block) && entity instanceof LivingEntity) {
			new FireDamageTimer(entity, BlazeArc.getIgnitedBlocks().get(block));
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityDamageBlock(EntityDamageByBlockEvent event) {

	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityDamageByBlock(EntityDamageByBlockEvent event) {
		Block block = event.getDamager();
		if (block == null)
			return;

		if (TempBlock.isTempBlock(block)) {
			if (EarthAbility.isEarthbendable(block.getType()) && GeneralMethods.isSolid(block)) {
				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityDamageEvent(EntityDamageEvent event) {
		if (event.isCancelled()) {
			return;
		}

		Entity entity = event.getEntity();

		if (event.getCause() == DamageCause.FIRE && BlazeArc.getIgnitedBlocks().containsKey(entity.getLocation().getBlock())) {
			new FireDamageTimer(entity, BlazeArc.getIgnitedBlocks().get(entity.getLocation().getBlock()));
		}

		if (FireDamageTimer.isEnflamed(entity) && event.getCause() == DamageCause.FIRE_TICK) {
			event.setCancelled(true);
			FireDamageTimer.dealFlameDamage(entity);
		}

		if (entity instanceof Player) {
			Player player = (Player) entity;
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
			if (bPlayer == null) {
				return;
			}

			if (bPlayer.isElementToggled(Element.FIRE)) {
				return;
			}
			if (bPlayer.getBoundAbilityName().equalsIgnoreCase("HeatControl")) {
				if (event.getCause() == DamageCause.FIRE || event.getCause() == DamageCause.FIRE_TICK) {
					player.setFireTicks(0);
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityDeath(EntityDeathEvent event) {
		if (TempArmor.hasTempArmor(event.getEntity())) {
			TempArmor armor = TempArmor.getTempArmor(event.getEntity());

			List<ItemStack> newDrops = armor.filterArmor(event.getDrops());
			event.getDrops().clear();
			event.getDrops().addAll(newDrops);

			armor.revert();
		}

		for (FireCombo fc : CoreAbility.getAbilities(event.getEntity().getKiller(), FireCombo.class)) {
			if (!fc.getAffectedEntities().contains(event.getEntity()))
				continue;
			List<ItemStack> drops = event.getDrops();
			List<ItemStack> newDrops = new ArrayList<>();
			for (int i = 0; i < drops.size(); i++) {
				ItemStack cooked = drops.get(i);
				Material material = drops.get(i).getType();
				switch (material) {
					case RAW_BEEF:
						cooked = new ItemStack(Material.COOKED_BEEF, 1);
						break;
					case RAW_FISH:
						ItemStack salmon = new ItemStack(Material.RAW_FISH, 1, (short) 1);
						if (drops.get(i).getDurability() == salmon.getDurability()) {
							cooked = new ItemStack(Material.COOKED_FISH, 1, (short) 1);
						} else {
							cooked = new ItemStack(Material.COOKED_FISH, 1);
						}
						break;
					case RAW_CHICKEN:
						cooked = new ItemStack(Material.COOKED_CHICKEN, 1);
						break;
					case PORK:
						cooked = new ItemStack(Material.GRILLED_PORK, 1);
						break;
					case MUTTON:
						cooked = new ItemStack(Material.COOKED_MUTTON);
						break;
					case RABBIT:
						cooked = new ItemStack(Material.COOKED_RABBIT);
						break;
					default:
						break;
				}

				newDrops.add(cooked);
			}
			event.getDrops().clear();
			event.getDrops().addAll(newDrops);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent event) {
		if (event.isCancelled()) {
			return;
		}

		for (Block block : event.blockList()) {
			EarthBlast blast = EarthBlast.getBlastFromSource(block);

			if (blast != null) {
				blast.remove();
			}
			if (PhaseChange.getFrozenBlocksAsBlock().contains(block)) {
				PhaseChange.thaw(block);
			}
			if (SurgeWall.getWallBlocks().containsKey(block)) {
				block.setType(Material.AIR);
			}
			if (!SurgeWave.canThaw(block)) {
				SurgeWave.thaw(block);
			}
			if (EarthAbility.getMovedEarth().containsKey(block)) {
				EarthAbility.removeRevertIndex(block);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityExplodeEvent(EntityExplodeEvent event) {
		if (event.isCancelled()) {
			return;
		}

		Entity entity = event.getEntity();
		if (entity != null) {
			if (Paralyze.isParalyzed(entity) || Immobilize.isParalyzed(entity) || Bloodbending.isBloodbent(entity) || Suffocate.isBreathbent(entity)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityInteractEvent(EntityInteractEvent event) {
		if (event.isCancelled()) {
			return;
		}

		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || Immobilize.isParalyzed(entity) || Bloodbending.isBloodbent(entity) || Suffocate.isBreathbent(entity)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityProjectileLaunchEvent(ProjectileLaunchEvent event) {
		if (event.isCancelled()) {
			return;
		}

		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || Immobilize.isParalyzed(entity) || Bloodbending.isBloodbent(entity) || Suffocate.isBreathbent(entity)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityShootBowEvent(EntityShootBowEvent event) {
		if (event.isCancelled()) {
			return;
		}

		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || Immobilize.isParalyzed(entity) || Bloodbending.isBloodbent(entity) || Suffocate.isBreathbent(entity)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntitySlimeSplitEvent(SlimeSplitEvent event) {
		if (event.isCancelled()) {
			return;
		}

		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || Immobilize.isParalyzed(entity) || Bloodbending.isBloodbent(entity) || Suffocate.isBreathbent(entity)) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntitySuffocatedByTempBlocks(EntityDamageEvent event) {
		if (event.isCancelled()) {
			return;
		}

		if (event.getCause() == DamageCause.SUFFOCATION) {
			if (TempBlock.isTempBlock(event.getEntity().getLocation().add(0, 1, 0).getBlock())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityTarget(EntityTargetEvent event) {
		if (event.isCancelled()) {
			return;
		}

		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || Immobilize.isParalyzed(entity) || Bloodbending.isBloodbent(entity)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityTargetLiving(EntityTargetLivingEntityEvent event) {
		if (event.isCancelled()) {
			return;
		}

		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || Immobilize.isParalyzed(entity) || Bloodbending.isBloodbent(entity)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityTeleportEvent(EntityTeleportEvent event) {
		if (event.isCancelled()) {
			return;
		}

		Entity entity = event.getEntity();
		if (Paralyze.isParalyzed(entity) || Immobilize.isParalyzed(entity) || Bloodbending.isBloodbent(entity) || Suffocate.isBreathbent(entity)) {
			event.setCancelled(true);
		}
		
		if (entity instanceof LivingEntity && TempArmor.hasTempArmor((LivingEntity) entity)) {
			TempArmor.getTempArmor((LivingEntity) entity).revert();
		}
	}

	@EventHandler
	public void onHorizontalCollision(HorizontalVelocityChangeEvent e) {
		if (e.getEntity() instanceof LivingEntity) {
			if (e.getEntity().getEntityId() != e.getInstigator().getEntityId()) {
				double minimumDistance = plugin.getConfig().getDouble("Properties.HorizontalCollisionPhysics.WallDamageMinimumDistance");
				double maxDamage = plugin.getConfig().getDouble("Properties.HorizontalCollisionPhysics.WallDamageCap");
				double damage = ((e.getDistanceTraveled() - minimumDistance) < 0 ? 0 : e.getDistanceTraveled() - minimumDistance) / (e.getDifference().length());
				if (damage > 0) {
					if (damage <= maxDamage) {
						DamageHandler.damageEntity((LivingEntity) e.getEntity(), damage, e.getAbility());
					} else {
						DamageHandler.damageEntity((LivingEntity) e.getEntity(), maxDamage, e.getAbility());
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.isCancelled()) {
			return;
		}

		for (MetalClips clips : CoreAbility.getAbilities(MetalClips.class)) {
			if (clips.getTargetEntity() != null && clips.getTargetEntity().getEntityId() == event.getWhoClicked().getEntityId()) {
				event.setCancelled(true);
				break;
			}
		}

		if (event.getSlotType() == SlotType.ARMOR && CoreAbility.hasAbility((Player) event.getWhoClicked(), EarthArmor.class)) {
			event.setCancelled(true);
		}

		if (event.getSlotType() == SlotType.ARMOR && TempArmor.hasTempArmor((Player) event.getWhoClicked())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerBendingDeath(EntityBendingDeathEvent event) {
		if (ConfigManager.languageConfig.get().getBoolean("DeathMessages.Enabled") && event.getEntity() instanceof Player) {
			Ability ability = event.getAbility();
			if (ability == null) {
				return;
			}

			BENDING_PLAYER_DEATH.put((Player) event.getEntity(), ability.getElement().getColor() + ability.getName());
			final Player player = (Player) event.getEntity();

			new BukkitRunnable() {
				@Override
				public void run() {
					BENDING_PLAYER_DEATH.remove(player);
				}
			}.runTaskLater(ProjectKorra.plugin, 20);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (event.isCancelled()) {
			return;
		}

		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		String e = bPlayer == null || bPlayer.getElements().size() == 0 ? "Nonbender" : (bPlayer.getElements().size() > 1 ? "Avatar" : bPlayer.getElements().get(0).getName());
		String element = ConfigManager.languageConfig.get().getString("Chat.Prefixes." + e);
		ChatColor c = bPlayer == null || bPlayer.getElements().size() == 0 ? ChatColor.WHITE : (bPlayer.getElements().size() > 1 ? Element.AVATAR.getColor() : bPlayer.getElements().get(0).getColor());
		event.setFormat(event.getFormat().replace("{element}", c + element + ChatColor.RESET).replace("{ELEMENT}", c + element + ChatColor.RESET).replace("{elementcolor}", c + "").replace("{ELEMENTCOLOR}", c + ""));

		if (!ConfigManager.languageConfig.get().getBoolean("Chat.Enable")) {
			return;
		}

		ChatColor color = ChatColor.WHITE;

		if (bPlayer == null) {
			return;
		}

		if (player.hasPermission("bending.avatar") || bPlayer.getElements().size() > 1) {
			color = ChatColor.valueOf(ConfigManager.languageConfig.get().getString("Chat.Colors.Avatar"));
		} else {
			for (Element element_ : Element.getMainElements()) {
				if (bPlayer.hasElement(element_)) {
					color = element_.getColor();
					break;
				}
			}
		}

		String format = ConfigManager.languageConfig.get().getString("Chat.Format");
		format = format.replace("<message>", "%2$s");
		format = format.replace("<name>", color + player.getDisplayName() + ChatColor.RESET);
		event.setFormat(format);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerDamage(EntityDamageEvent event) {

		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

			if (bPlayer == null) {
				return;
			}

			if (bPlayer.hasElement(Element.EARTH) && event.getCause() == DamageCause.FALL) {
				new Shockwave(player, true);
			}

			if (!event.isCancelled() && bPlayer.hasElement(Element.AIR) && event.getCause() == DamageCause.FALL && bPlayer.canBendPassive(Element.AIR) && bPlayer.canUsePassive(Element.AIR)) {
				new AirBurst(player, true);
				if (CoreAbility.getAbility(GracefulDescent.class).isEnabled() && PassiveManager.hasPassive(player, CoreAbility.getAbility(GracefulDescent.class))) {
					event.setDamage(0D);
					event.setCancelled(true);
				}
			}

			if (!event.isCancelled() && bPlayer.hasElement(Element.WATER) && event.getCause() == DamageCause.FALL && bPlayer.canBendPassive(Element.WATER) && bPlayer.canUsePassive(Element.WATER) && CoreAbility.getAbility(Hydrosink.class).isEnabled() && PassiveManager.hasPassive(player, CoreAbility.getAbility(Hydrosink.class))) {
				if (WaterPassive.applyNoFall(player)) {
					event.setDamage(0D);
					event.setCancelled(true);
				}
			}

			if (!event.isCancelled() && bPlayer.hasElement(Element.EARTH) && event.getCause() == DamageCause.FALL && bPlayer.canBendPassive(Element.EARTH) && bPlayer.canUsePassive(Element.EARTH) && CoreAbility.getAbility(DensityShift.class).isEnabled() && PassiveManager.hasPassive(player, CoreAbility.getAbility(DensityShift.class))) {
				if (EarthPassive.softenLanding(player)) {
					event.setDamage(0D);
					event.setCancelled(true);
				}
			}

			if (!event.isCancelled() && bPlayer.hasElement(Element.CHI) && event.getCause() == DamageCause.FALL && bPlayer.canBendPassive(Element.CHI) && bPlayer.canUsePassive(Element.CHI) && CoreAbility.getAbility(Acrobatics.class).isEnabled() && PassiveManager.hasPassive(player, CoreAbility.getAbility(Acrobatics.class))) {
				double initdamage = event.getDamage();
				double newdamage = event.getDamage() * ChiPassive.getFallReductionFactor();
				double finaldamage = initdamage - newdamage;
				event.setDamage(finaldamage);
				if (finaldamage <= 0.4)
					event.setCancelled(true);
			}

			if (!event.isCancelled() && event.getCause() == DamageCause.FALL) {
				Player source = Flight.getLaunchedBy(player);
				if (source == player) {
					event.setCancelled(true);
				}
			}

			if (bPlayer.canBendPassive(Element.FIRE) && bPlayer.hasElement(Element.FIRE) && bPlayer.canUsePassive(Element.FIRE) && (event.getCause() == DamageCause.FIRE || event.getCause() == DamageCause.FIRE_TICK)) {
				event.setCancelled(!HeatControl.canBurn(player));
			}

			if (bPlayer.hasElement(Element.EARTH) && event.getCause() == DamageCause.SUFFOCATION && TempBlock.isTempBlock(player.getEyeLocation().getBlock())) {
				event.setDamage(0D);
				event.setCancelled(true);
			}

			if (!event.isCancelled() && CoreAbility.getAbility(player, EarthArmor.class) != null) {
				EarthArmor eartharmor = CoreAbility.getAbility(player, EarthArmor.class);
				eartharmor.updateAbsorbtion();
			}
		}
	}

	@EventHandler
	public void onPlayerDamageByPlayer(EntityDamageByEntityEvent e) {
		if (e.isCancelled()) {
			return;
		}

		Entity source = e.getDamager();
		Entity entity = e.getEntity();
		FireBlastCharged fireball = FireBlastCharged.getFireball(source);

		if (fireball != null) {
			e.setCancelled(true);
			fireball.dealDamage(entity);
			return;
		}

		if (Paralyze.isParalyzed(e.getDamager()) || Immobilize.isParalyzed(e.getDamager())) {
			e.setCancelled(true);
			return;
		}

		if (entity instanceof Player) {
			Suffocate.remove((Player) entity);
		}

		Entity en = e.getEntity();
		if (en instanceof Player) {
			if (e.getDamager() instanceof Player) { // This is the player hitting someone.
				Player sourcePlayer = (Player) e.getDamager();
				Player targetPlayer = (Player) e.getEntity();
				BendingPlayer sourceBPlayer = BendingPlayer.getBendingPlayer(sourcePlayer);
				if (sourceBPlayer == null) {
					return;
				}

				String boundAbil = sourceBPlayer.getBoundAbilityName();

				if (sourceBPlayer.canBendPassive(Element.CHI)) {
					if (e.getCause() == DamageCause.ENTITY_ATTACK && e.getDamage() == 1) {
						if (sourceBPlayer.getBoundAbility() instanceof ChiAbility) {
							if (sourceBPlayer.canCurrentlyBendWithWeapons()) {
								if (sourceBPlayer.isElementToggled(Element.CHI) == true) {
									if (boundAbil.equalsIgnoreCase("Paralyze")) {
										new Paralyze(sourcePlayer, targetPlayer);
									} else if (boundAbil.equalsIgnoreCase("QuickStrike")) {
										new QuickStrike(sourcePlayer, targetPlayer);
										e.setCancelled(true);
									} else if (boundAbil.equalsIgnoreCase("SwiftKick")) {
										new SwiftKick(sourcePlayer, targetPlayer);
										e.setCancelled(true);
									} else if (boundAbil.equalsIgnoreCase("RapidPunch")) {
										new RapidPunch(sourcePlayer, targetPlayer);
										e.setCancelled(true);
									} else {
										if (ChiPassive.willChiBlock(sourcePlayer, targetPlayer)) {
											ChiPassive.blockChi(targetPlayer);
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}

		if (event.getKeepInventory()) {
			if (TempArmor.hasTempArmor(event.getEntity())) {
				TempArmor.getTempArmor(event.getEntity()).revert();
			}
		} else {
			// Do nothing. TempArmor drops are handled by the EntityDeath event and not PlayerDeath.
		}

		if (event.getEntity().getKiller() != null) {
			if (BENDING_PLAYER_DEATH.containsKey(event.getEntity())) {
				String message = ConfigManager.languageConfig.get().getString("DeathMessages.Default");
				String ability = BENDING_PLAYER_DEATH.get(event.getEntity());
				String tempAbility = ChatColor.stripColor(ability).replaceAll(" ", "");
				CoreAbility coreAbil = CoreAbility.getAbility(tempAbility);
				Element element = null;
				boolean isAvatarAbility = false;

				if (coreAbil != null) {
					element = coreAbil.getElement();
				}
				if (HorizontalVelocityTracker.hasBeenDamagedByHorizontalVelocity(event.getEntity()) && Arrays.asList(HorizontalVelocityTracker.abils).contains(tempAbility)) {
					if (ConfigManager.languageConfig.get().contains("Abilities." + element.getName() + "." + tempAbility + ".HorizontalVelocityDeath")) {
						message = ConfigManager.languageConfig.get().getString("Abilities." + element.getName() + "." + tempAbility + ".HorizontalVelocityDeath");
					}
				} else if (element != null) {
					if (element instanceof SubElement) {
						element = ((SubElement) element).getParentElement();
					}
					if (ConfigManager.languageConfig.get().contains("Abilities." + element.getName() + "." + tempAbility + ".DeathMessage")) {
						message = ConfigManager.languageConfig.get().getString("Abilities." + element.getName() + "." + tempAbility + ".DeathMessage");
					} else if (ConfigManager.languageConfig.get().contains("Abilities." + element.getName() + ".Combo." + tempAbility + ".DeathMessage")) {
						message = ConfigManager.languageConfig.get().getString("Abilities." + element.getName() + ".Combo." + tempAbility + ".DeathMessage");
					}
				} else {
					if (isAvatarAbility) {
						if (ConfigManager.languageConfig.get().contains("Abilities.Avatar." + tempAbility + ".DeathMessage")) {
							message = ConfigManager.languageConfig.get().getString("Abilities.Avatar." + tempAbility + ".DeathMessage");
						}
					} else if (ConfigManager.languageConfig.get().contains("Abilities.Avatar.Combo." + tempAbility + ".DeathMessage")) {
						message = ConfigManager.languageConfig.get().getString("Abilities.Avatar.Combo." + tempAbility + ".DeathMessage");
					}
				}
				message = message.replace("{victim}", event.getEntity().getName()).replace("{attacker}", event.getEntity().getKiller().getName()).replace("{ability}", ability);
				event.setDeathMessage(message);
				BENDING_PLAYER_DEATH.remove(event.getEntity());
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerInteraction(PlayerInteractEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			final UUID uuid = player.getUniqueId();
			RIGHT_CLICK_INTERACT.add(uuid);

			new BukkitRunnable() {
				@Override
				public void run() {
					RIGHT_CLICK_INTERACT.remove(uuid);
				}
			}.runTaskLater(plugin, 5);

			if (event.getHand() == EquipmentSlot.HAND) {
				if (bPlayer.canCurrentlyBendWithWeapons()) {
					if (event.getClickedBlock() != null) {
						ComboManager.addComboAbility(player, ClickType.RIGHT_CLICK_BLOCK);
					} else {
						ComboManager.addComboAbility(player, ClickType.RIGHT_CLICK);
					}
				}
			}

			if (bPlayer.getBoundAbilityName().equalsIgnoreCase("EarthSmash")) {
				new EarthSmash(player, ClickType.RIGHT_CLICK);
			}
		}
		if (Paralyze.isParalyzed(player) || Immobilize.isParalyzed(player) || Bloodbending.isBloodbent(player) || Suffocate.isBreathbent(player)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if (event.isCancelled()) {
			return;
		}

		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer.canCurrentlyBendWithWeapons()) {
			ComboManager.addComboAbility(player, ClickType.RIGHT_CLICK_ENTITY);
		}

		if (Paralyze.isParalyzed(player) || Immobilize.isParalyzed(player) || Bloodbending.isBloodbent(player) || Suffocate.isBreathbent(player)) {
			event.setCancelled(true);
		}

		if (bPlayer.getBoundAbilityName().equalsIgnoreCase("HealingWaters") && event.getHand().equals(EquipmentSlot.HAND)) {
			HealingWaters instance = CoreAbility.getAbility(player, HealingWaters.class);
			if (instance != null && instance.charged) {
				instance.click();
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onPlayerItemDamage(PlayerItemDamageEvent event) {
		if (event.isCancelled())
			return;

		if (TempArmor.hasTempArmor(event.getPlayer())) {
			TempArmor armor = TempArmor.getTempArmor(event.getPlayer());
			for (ItemStack i : armor.getNewArmor()) {
				if (i != null && event.getItem().isSimilar(i)) {
					event.setCancelled(true);
					break;
				}
			}
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		JUMPS.put(player, player.getStatistic(Statistic.JUMP));

		GeneralMethods.createBendingPlayer(player.getUniqueId(), player.getName());
		Bukkit.getScheduler().runTaskLater(ProjectKorra.plugin, new Runnable() {

			@Override
			public void run() {
				PassiveManager.registerPassives(player);
				GeneralMethods.removeUnusableAbilities(player.getName());
			}
		}, 5);

		if (ConfigManager.languageConfig.get().getBoolean("Chat.Branding.JoinMessage.Enabled")) {
			Bukkit.getScheduler().runTaskLater(ProjectKorra.plugin, new Runnable() {
				public void run() {
					ChatColor color = ChatColor.valueOf(ConfigManager.languageConfig.get().getString("Chat.Branding.Color").toUpperCase());
					color = color == null ? ChatColor.GOLD : color;
					String topBorder = ConfigManager.languageConfig.get().getString("Chat.Branding.Borders.TopBorder");
					String bottomBorder = ConfigManager.languageConfig.get().getString("Chat.Branding.Borders.BottomBorder");
					if (!topBorder.isEmpty()) {
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', topBorder));
					}
					player.sendMessage(color + "This server is running ProjectKorra version " + ProjectKorra.plugin.getDescription().getVersion() + " for bending! Find out more at http://www.projectkorra.com!");
					if (!bottomBorder.isEmpty()) {
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', bottomBorder));
					}
				}
			}, 20 * 4);
		}
	}

	@EventHandler
	public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
		PassiveManager.registerPassives(event.getPlayer());
	}

	@EventHandler
	public void onPlayerKick(PlayerKickEvent event) {
		if (event.isCancelled()) {
			return;
		}

		AirFlight.remove(event.getPlayer());
		JUMPS.remove(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		if (event.isCancelled()) {
			return;
		}

		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (Paralyze.isParalyzed(player)) {
			event.setCancelled(true);
			return;
		}

		else if (Immobilize.isParalyzed(player)) {
			event.setCancelled(true);
			return;
		}

		else if (CoreAbility.hasAbility(player, WaterSpout.class) || CoreAbility.hasAbility(player, AirSpout.class) || CoreAbility.hasAbility(player, SandSpout.class)) {
			Vector vel = new Vector();
			vel.setX(event.getTo().getX() - event.getFrom().getX());
			vel.setZ(event.getTo().getZ() - event.getFrom().getZ());

			double currspeed = vel.length();
			double maxspeed = .2;
			if (currspeed > maxspeed) {
				// apply only if moving set a factor
				vel = vel.normalize().multiply(maxspeed);
				// apply the new velocity
				event.getPlayer().setVelocity(vel);
			}
		}

		else if (Bloodbending.isBloodbent(player)) {
			double distance1 = 0;
			double distance2 = 0;
			Location loc = Bloodbending.getBloodbendingLocation(player);
			if (event.getPlayer().getWorld().equals(loc.getWorld())) {
				distance1 = event.getFrom().distance(loc);
				distance2 = event.getTo().distance(loc);
			}
			if (distance2 > distance1) {
				player.setVelocity(new Vector(0, 0, 0));
			}
		}

		else if (AirFlight.isFlying(event.getPlayer())) {
			if (AirFlight.isHovering(event.getPlayer())) {
				Location loc = event.getFrom();
				Location toLoc = event.getTo();

				if (loc.getX() != toLoc.getX() || loc.getY() != toLoc.getY() || loc.getZ() != toLoc.getZ()) {
					event.setCancelled(true);
					return;
				}
			}
		}

		else {
			if (bPlayer != null) {
				if (bPlayer.hasElement(Element.AIR) || bPlayer.hasElement(Element.CHI)) {
					PassiveHandler.checkExhaustionPassives(player);
				}
			}
		}

		if (event.getTo().getY() > event.getFrom().getY()) {
			if (!(player.getLocation().getBlock().getType() == Material.VINE) && !(player.getLocation().getBlock().getType() == Material.LADDER)) {
				int current = player.getStatistic(Statistic.JUMP);
				int last = JUMPS.get(player);

				if (last != current) {
					JUMPS.put(player, current);

					double yDif = event.getTo().getY() - event.getFrom().getY();

					if ((yDif < 0.035 || yDif > 0.037) && (yDif < 0.116 || yDif > 0.118)) {
						Bukkit.getServer().getPluginManager().callEvent(new PlayerJumpEvent(player, yDif));
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerGamemodeChange(PlayerGameModeChangeEvent event) {
		Player player = event.getPlayer();
		if (event.getNewGameMode() == GameMode.SPECTATOR) {
			if (!Commands.invincible.contains(player.getName())) {
				Commands.invincible.add(player.getName());
			}
		} else if (!(event.getNewGameMode() == GameMode.SPECTATOR) && Commands.invincible.contains(player.getName())) {
			Commands.invincible.remove(player.getName());
		}

	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer != null) {
			if (TOGGLED_OUT.contains(player.getUniqueId()) && bPlayer.isToggled()) {
				TOGGLED_OUT.remove(player.getUniqueId());
			}
			if (!bPlayer.isToggled()) {
				TOGGLED_OUT.add(player.getUniqueId());
			}
		}

		if (Commands.invincible.contains(player.getName())) {
			Commands.invincible.remove(player.getName());
		}
		Preset.unloadPreset(player);

		if (TempArmor.hasTempArmor(player)) {
			TempArmor.getTempArmor(player).revert();
		}

		if (MetalClips.isControlled(event.getPlayer())) {
			MetalClips.removeControlledEnitity(event.getPlayer());
		}

		MultiAbilityManager.remove(player);
		AirFlight.remove(player);
		JUMPS.remove(player);
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerSneak(PlayerToggleSneakEvent event) {
		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (event.isCancelled() || bPlayer == null) {
			return;
		}

		if (CoreAbility.hasAbility(event.getPlayer(), AirScooter.class)) {
			event.setCancelled(true);
			return;
		}

		if (bPlayer.canCurrentlyBendWithWeapons()) {
			if (player.isSneaking()) {
				ComboManager.addComboAbility(player, ClickType.SHIFT_UP);
			} else {
				ComboManager.addComboAbility(player, ClickType.SHIFT_DOWN);
			}
		}

		String abilName = bPlayer.getBoundAbilityName();
		if (Suffocate.isBreathbent(player)) {
			if (!abilName.equalsIgnoreCase("AirSwipe") || !abilName.equalsIgnoreCase("FireBlast") || !abilName.equalsIgnoreCase("EarthBlast") || !abilName.equalsIgnoreCase("WaterManipulation")) {
				if (!player.isSneaking()) {
					event.setCancelled(true);
				}
			}
		}

		if (Paralyze.isParalyzed(player) || Immobilize.isParalyzed(player) || Bloodbending.isBloodbent(player)) {
			event.setCancelled(true);
			return;
		}

		if (!player.isSneaking()) {
			BlockSource.update(player, ClickType.SHIFT_DOWN);
		}

		if (CoreAbility.getAbility(FerroControl.class).isEnabled() && PassiveManager.hasPassive(player, CoreAbility.getAbility(FerroControl.class)) && !bPlayer.isOnCooldown("Hodor")) {
			if (event.isSneaking()) {
				Block block = player.getTargetBlock((HashSet<Material>) null, 5);
				if (block != null) {
					if (block.getType() == Material.IRON_DOOR_BLOCK && !GeneralMethods.isRegionProtectedFromBuild(player, block.getLocation())) {
						if (block.getData() >= 8) {
							block = block.getRelative(BlockFace.DOWN);
						}
						if (block.getData() < 4) {
							block.setData((byte) (block.getData() + 4));
							block.getWorld().playSound(block.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 10, 1);
						} else {
							block.setData((byte) (block.getData() - 4));
							block.getWorld().playSound(block.getLocation(), Sound.BLOCK_IRON_DOOR_OPEN, 10, 1);
						}
						bPlayer.addCooldown("Hodor", 200);
					}
				}

			}
		}

		AirScooter.check(player);

		CoreAbility coreAbil = bPlayer.getBoundAbility();
		String abil = bPlayer.getBoundAbilityName();
		if (coreAbil == null) {
			return;
		}

		if (bPlayer.isChiBlocked()) {
			event.setCancelled(true);
			return;
		}

		if (!player.isSneaking() && bPlayer.canBendIgnoreCooldowns(coreAbil)) {
			if (coreAbil instanceof AddonAbility) {
				return;
			}
			if (coreAbil instanceof AirAbility && bPlayer.isElementToggled(Element.AIR) == true) {
				if (bPlayer.canCurrentlyBendWithWeapons()) {
					if (abil.equalsIgnoreCase("Tornado")) {
						new Tornado(player);
					} else if (abil.equalsIgnoreCase("AirBlast")) {
						AirBlast.setOrigin(player);
					} else if (abil.equalsIgnoreCase("AirBurst")) {
						new AirBurst(player, false);
					} else if (abil.equalsIgnoreCase("AirSuction")) {
						AirSuction.setOrigin(player);
					} else if (abil.equalsIgnoreCase("AirSwipe")) {
						new AirSwipe(player, true);
					} else if (abil.equalsIgnoreCase("AirShield")) {
						new AirShield(player);
					} else if (abil.equalsIgnoreCase("Suffocate")) {
						new Suffocate(player);
					} else if (abil.equalsIgnoreCase("Flight")) {
						if (player.isSneaking() || !bPlayer.canUseFlight()) {
							return;
						}
						new AirFlight(player);
					}
				}
			}

			if (coreAbil instanceof WaterAbility && bPlayer.isElementToggled(Element.WATER) == true) {
				if (bPlayer.canCurrentlyBendWithWeapons()) {
					if (abil.equalsIgnoreCase("Bloodbending")) {
						new Bloodbending(player);
					} else if (abil.equalsIgnoreCase("IceBlast")) {
						new IceBlast(player);
					} else if (abil.equalsIgnoreCase("IceSpike")) {
						new IceSpikeBlast(player);
					} else if (abil.equalsIgnoreCase("OctopusForm")) {
						OctopusForm.form(player);
					} else if (abil.equalsIgnoreCase("PhaseChange")) {
						if (!CoreAbility.hasAbility(player, PhaseChange.class)) {
							new PhaseChange(player, PhaseChangeType.MELT);
						} else {
							PhaseChange pc = CoreAbility.getAbility(player, PhaseChange.class);
							pc.startNewType(PhaseChangeType.MELT);
						}
					} else if (abil.equalsIgnoreCase("WaterManipulation")) {
						new WaterManipulation(player);
					} else if (abil.equalsIgnoreCase("WaterBubble")) {
						new WaterBubble(player, true);
					} else if (abil.equalsIgnoreCase("Surge")) {
						SurgeWall.form(player);
					} else if (abil.equalsIgnoreCase("Torrent")) {
						Torrent.create(player);
					} else if (abil.equalsIgnoreCase("WaterArms")) {
						new WaterArms(player);
					}
					if (abil.equalsIgnoreCase("HealingWaters")) {
						new HealingWaters(player);
					}
				}
			}

			if (coreAbil instanceof EarthAbility && bPlayer.isElementToggled(Element.EARTH) == true) {
				if (bPlayer.canCurrentlyBendWithWeapons()) {
					if (abil.equalsIgnoreCase("Catapult")) {
						new Catapult(player, true);
					} else if (abil.equalsIgnoreCase("EarthBlast")) {
						new EarthBlast(player);
					} else if (abil.equalsIgnoreCase("EarthArmor")) {
						new EarthArmor(player);
					} else if (abil.equalsIgnoreCase("RaiseEarth")) {
						new RaiseEarthWall(player);
					} else if (abil.equalsIgnoreCase("Collapse")) {
						new CollapseWall(player);
					} else if (abil.equalsIgnoreCase("Shockwave")) {
						new Shockwave(player, false);
					} else if (abil.equalsIgnoreCase("EarthTunnel")) {
						new EarthTunnel(player);
					} else if (abil.equalsIgnoreCase("Tremorsense")) {
						bPlayer.toggleTremorSense();
					} else if (abil.equalsIgnoreCase("Extraction")) {
						new Extraction(player);
					} else if (abil.equalsIgnoreCase("LavaFlow")) {
						new LavaFlow(player, LavaFlow.AbilityType.SHIFT);
					} else if (abil.equalsIgnoreCase("EarthSmash")) {
						new EarthSmash(player, ClickType.SHIFT_DOWN);
					} else if (abil.equalsIgnoreCase("MetalClips")) {
						MetalClips clips = CoreAbility.getAbility(player, MetalClips.class);
						if (clips != null) {
							if (clips.getTargetEntity() == null) {
								clips.setMagnetized(true);
							} else {
								clips.setControlling(true);
							}
						}
					} else {
						new MetalClips(player, 1);
					}
				}

			}

			if (coreAbil instanceof FireAbility && bPlayer.isElementToggled(Element.FIRE) == true) {
				if (bPlayer.canCurrentlyBendWithWeapons()) {
					if (abil.equalsIgnoreCase("Blaze")) {
						new BlazeRing(player);
					} else if (abil.equalsIgnoreCase("FireBlast")) {
						new FireBlastCharged(player);
					} else if (abil.equalsIgnoreCase("HeatControl")) {
						new HeatControl(player, HeatControlType.COOK);
					} else if (abil.equalsIgnoreCase("FireBurst")) {
						new FireBurst(player);
					} else if (abil.equalsIgnoreCase("FireShield")) {
						new FireShield(player, true);
					} else if (abil.equalsIgnoreCase("Lightning")) {
						new Lightning(player);
					} else if (abil.equalsIgnoreCase("Combustion")) {
						new Combustion(player);
					} else if (abil.equalsIgnoreCase("FireManipulation")) {
						new FireManipulation(player, FireManipulationType.SHIFT);
					} 
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerSlotChange(PlayerItemHeldEvent event) {
		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		int slot = event.getNewSlot() + 1;

		if (ConfigManager.defaultConfig.get().getBoolean("Properties.BendingPreview")) {
			if (bPlayer != null && bPlayer.getAbilities() != null) {
				GeneralMethods.displayMovePreview(player, slot);
			}
		} else {
			WaterArms waterArms = CoreAbility.getAbility(player, WaterArms.class);
			if (waterArms != null) {
				waterArms.displayBoundMsg(event.getNewSlot() + 1);
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerSwing(PlayerAnimationEvent event) {
		if (event.isCancelled()) {
			return;
		}

		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return;
		} else if (RIGHT_CLICK_INTERACT.contains(player.getUniqueId())) {
			return;
		}

		Entity target = GeneralMethods.getTargetedEntity(player, 3);

		if (bPlayer.canCurrentlyBendWithWeapons()) {
			if (target != null && !(target.equals(player)) && target instanceof LivingEntity) {
				ComboManager.addComboAbility(player, ClickType.LEFT_CLICK_ENTITY);

			} else {
				ComboManager.addComboAbility(player, ClickType.LEFT_CLICK);
			}
		}

		if (Suffocate.isBreathbent(player)) {
			event.setCancelled(true);
			return;
		} else if (Bloodbending.isBloodbent(player) || Paralyze.isParalyzed(player) || Immobilize.isParalyzed(player)) {
			event.setCancelled(true);
			return;
		} else if (bPlayer.isChiBlocked()) {
			event.setCancelled(true);
			return;
		} else if (GeneralMethods.isInteractable(player.getTargetBlock(new HashSet<Material>(), 5))) {
			event.setCancelled(true);
			return;
		} else if (player.getInventory().getItemInMainHand().getType() == Material.FISHING_ROD) {
			return;
		}

		BlockSource.update(player, ClickType.LEFT_CLICK);
		AirScooter.check(player);

		String abil = bPlayer.getBoundAbilityName();
		CoreAbility coreAbil = bPlayer.getBoundAbility();

		if (coreAbil == null && !MultiAbilityManager.hasMultiAbilityBound(player)) {
			return;
		} else if (bPlayer.canBendIgnoreCooldowns(coreAbil)) {
			if (coreAbil instanceof AddonAbility) {
				return;
			}

			if (coreAbil instanceof AirAbility && bPlayer.isElementToggled(Element.AIR) == true) {
				if (bPlayer.canCurrentlyBendWithWeapons()) {
					if (abil.equalsIgnoreCase("AirBlast")) {
						new AirBlast(player);
					} else if (abil.equalsIgnoreCase("AirSuction")) {
						new AirSuction(player);
					} else if (abil.equalsIgnoreCase("AirBurst")) {
						AirBurst.coneBurst(player);
					} else if (abil.equalsIgnoreCase("AirScooter")) {
						new AirScooter(player);
					} else if (abil.equalsIgnoreCase("AirSpout")) {
						new AirSpout(player);
					} else if (abil.equalsIgnoreCase("AirSwipe")) {
						new AirSwipe(player);
					} else if (abil.equalsIgnoreCase("Flight")) {
						if (!ProjectKorra.plugin.getConfig().getBoolean("Abilities.Air.Flight.HoverEnabled") || !bPlayer.canUseFlight()) {
							return;
						}

						if (AirFlight.isFlying(event.getPlayer())) {
							if (AirFlight.isHovering(event.getPlayer())) {
								AirFlight.setHovering(event.getPlayer(), false);
							} else {
								AirFlight.setHovering(event.getPlayer(), true);
							}
						}
					}
				}
			}

			if (coreAbil instanceof WaterAbility && bPlayer.isElementToggled(Element.WATER) == true) {
				if (bPlayer.canCurrentlyBendWithWeapons()) {
					if (abil.equalsIgnoreCase("Bloodbending")) {
						Bloodbending.launch(player);
					} else if (abil.equalsIgnoreCase("IceBlast")) {
						IceBlast.activate(player);
					} else if (abil.equalsIgnoreCase("IceSpike")) {
						IceSpikeBlast.activate(player);
					} else if (abil.equalsIgnoreCase("OctopusForm")) {
						new OctopusForm(player);
					} else if (abil.equalsIgnoreCase("PhaseChange")) {
						if (!CoreAbility.hasAbility(player, PhaseChange.class)) {
							new PhaseChange(player, PhaseChangeType.FREEZE);
						} else {
							PhaseChange pc = CoreAbility.getAbility(player, PhaseChange.class);
							pc.startNewType(PhaseChangeType.FREEZE);
						}
					} else if (abil.equalsIgnoreCase("WaterBubble")) {
						new WaterBubble(player, false);
					} else if (abil.equalsIgnoreCase("WaterSpout")) {
						new WaterSpout(player);
					} else if (abil.equalsIgnoreCase("WaterManipulation")) {
						WaterManipulation.moveWater(player);
					} else if (abil.equalsIgnoreCase("Surge")) {
						new SurgeWall(player);
					} else if (abil.equalsIgnoreCase("Torrent")) {
						new Torrent(player);
					}
				}
			}

			if (coreAbil instanceof EarthAbility && bPlayer.isElementToggled(Element.EARTH) == true) {
				if (bPlayer.canCurrentlyBendWithWeapons()) {
					if (abil.equalsIgnoreCase("Catapult")) {
						new Catapult(player, false);
					} else if (abil.equalsIgnoreCase("EarthBlast")) {
						EarthBlast.throwEarth(player);
					} else if (abil.equalsIgnoreCase("RaiseEarth")) {
						new RaiseEarth(player);
					} else if (abil.equalsIgnoreCase("Collapse")) {
						new Collapse(player);
					} else if (abil.equalsIgnoreCase("Shockwave")) {
						Shockwave.coneShockwave(player);
					} else if (abil.equalsIgnoreCase("EarthArmor")) {
						EarthArmor armor = CoreAbility.getAbility(player, EarthArmor.class);
						if (armor != null && armor.isFormed()) {
							armor.click();
						}
					} else if (abil.equalsIgnoreCase("EarthGrab")) {
						new EarthGrab(player);
					} else if (abil.equalsIgnoreCase("Tremorsense")) {
						new Tremorsense(player, true);
					} else if (abil.equalsIgnoreCase("MetalClips")) {
						MetalClips clips = CoreAbility.getAbility(player, MetalClips.class);
						if (clips == null) {
							new MetalClips(player, 0);
						} else if (clips.getMetalClipsCount() < (player.hasPermission("bending.ability.MetalClips.4clips") ? 4 : 3)) {
							clips.shootMetal();
						} else if (clips.getMetalClipsCount() == 4 && clips.isCanUse4Clips()) {
							clips.crush();
						}
					} else if (abil.equalsIgnoreCase("LavaSurge")) {
						LavaSurge surge = CoreAbility.getAbility(player, LavaSurge.class);
						if (surge != null) {
							surge.launch();
						}
					} else if (abil.equalsIgnoreCase("LavaFlow")) {
						new LavaFlow(player, AbilityType.CLICK);
					} else if (abil.equalsIgnoreCase("EarthSmash")) {
						new EarthSmash(player, ClickType.LEFT_CLICK);
					} else if (abil.equalsIgnoreCase("SandSpout")) {
						new SandSpout(player);
					}
				}
			}

			if (coreAbil instanceof FireAbility && bPlayer.isElementToggled(Element.FIRE) == true) {
				if (bPlayer.canCurrentlyBendWithWeapons()) {
					if (abil.equalsIgnoreCase("Blaze")) {
						new Blaze(player);
					} else if (abil.equalsIgnoreCase("FireBlast")) {
						new FireBlast(player);
					} else if (abil.equalsIgnoreCase("FireJet")) {
						new FireJet(player);
					} else if (abil.equalsIgnoreCase("HeatControl")) {
						new HeatControl(player, HeatControlType.MELT);
					} else if (abil.equalsIgnoreCase("Illumination")) {
						if (ConfigManager.defaultConfig.get().getBoolean("Abilities.Fire.Illumination.Passive")) {
							bPlayer.toggleIllumination();
						} else {
							new Illumination(player);
						}
					} else if (abil.equalsIgnoreCase("FireBurst")) {
						FireBurst.coneBurst(player);
					} else if (abil.equalsIgnoreCase("FireShield")) {
						new FireShield(player);
					} else if (abil.equalsIgnoreCase("WallOfFire")) {
						new WallOfFire(player);
					} else if (abil.equalsIgnoreCase("Combustion")) {
						Combustion.explode(player);
					} else if (abil.equalsIgnoreCase("FireManipulation")) {
						if (CoreAbility.hasAbility(player, FireManipulation.class)) {
							FireManipulation fireManip = CoreAbility.getAbility(player, FireManipulation.class);
							if (fireManip.getFireManipulationType() == FireManipulationType.SHIFT) {
								fireManip.click();
							}
						} else {
							new FireManipulation(player, FireManipulationType.CLICK);
						}
					}
				}
			}

			if (coreAbil instanceof ChiAbility && bPlayer.isElementToggled(Element.CHI) == true) {
				if (bPlayer.canCurrentlyBendWithWeapons()) {
					if (abil.equalsIgnoreCase("HighJump")) {
						new HighJump(player);
					} else if (abil.equalsIgnoreCase("Smokescreen")) {
						new Smokescreen(player);
					} else if (abil.equalsIgnoreCase("WarriorStance")) {
						new WarriorStance(player);
					} else if (abil.equalsIgnoreCase("AcrobatStance")) {
						new AcrobatStance(player);
					}
				}
			}

			if (coreAbil instanceof AvatarAbility) {
				if (abil.equalsIgnoreCase("AvatarState")) {
					new AvatarState(player);
				}
			}
		}
		if (MultiAbilityManager.hasMultiAbilityBound(player)) {
			abil = MultiAbilityManager.getBoundMultiAbility(player);
			if (abil.equalsIgnoreCase("WaterArms")) {
				new WaterArms(player);

			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
		if (event.isCancelled()) {
			return;
		}

		Player player = event.getPlayer();
		if (CoreAbility.hasAbility(player, Tornado.class) || Bloodbending.isBloodbent(player) || Suffocate.isBreathbent(player) || CoreAbility.hasAbility(player, FireJet.class) || CoreAbility.hasAbility(player, AvatarState.class)) {
			event.setCancelled(player.getGameMode() != GameMode.CREATIVE);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onProjectileHit(ProjectileHitEvent event) {
		Integer id = event.getEntity().getEntityId();
		Smokescreen smokescreen = Smokescreen.getSnowballs().get(id);
		if (smokescreen != null) {
			Location loc = event.getEntity().getLocation();
			Smokescreen.playEffect(loc);
			for (Entity en : GeneralMethods.getEntitiesAroundPoint(loc, smokescreen.getRadius())) {
				smokescreen.applyBlindness(en);
			}
			Smokescreen.getSnowballs().remove(id);
		}
	}

	public static HashMap<Player, String> getBendingPlayerDeath() {
		return BENDING_PLAYER_DEATH;
	}

	public static List<UUID> getRightClickInteract() {
		return RIGHT_CLICK_INTERACT;
	}

	public static ArrayList<UUID> getToggledOut() {
		return TOGGLED_OUT;
	}

	public static Map<Player, Integer> getJumpStatistics() {
		return JUMPS;
	}
}
