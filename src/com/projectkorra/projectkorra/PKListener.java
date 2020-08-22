package com.projectkorra.projectkorra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.FluidLevelChangeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.SlimeSplitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
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
import com.projectkorra.projectkorra.airbending.flight.FlightMultiAbility;
import com.projectkorra.projectkorra.airbending.passive.GracefulDescent;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.board.BendingBoardManager;
import com.projectkorra.projectkorra.chiblocking.AcrobatStance;
import com.projectkorra.projectkorra.chiblocking.HighJump;
import com.projectkorra.projectkorra.chiblocking.Paralyze;
import com.projectkorra.projectkorra.chiblocking.QuickStrike;
import com.projectkorra.projectkorra.chiblocking.RapidPunch;
import com.projectkorra.projectkorra.chiblocking.Smokescreen;
import com.projectkorra.projectkorra.chiblocking.SwiftKick;
import com.projectkorra.projectkorra.chiblocking.WarriorStance;
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
import com.projectkorra.projectkorra.earthbending.EarthGrab.GrabMode;
import com.projectkorra.projectkorra.earthbending.EarthSmash;
import com.projectkorra.projectkorra.earthbending.EarthTunnel;
import com.projectkorra.projectkorra.earthbending.RaiseEarth;
import com.projectkorra.projectkorra.earthbending.RaiseEarthWall;
import com.projectkorra.projectkorra.earthbending.Shockwave;
import com.projectkorra.projectkorra.earthbending.Tremorsense;
import com.projectkorra.projectkorra.earthbending.combo.EarthPillars;
import com.projectkorra.projectkorra.earthbending.lava.LavaFlow;
import com.projectkorra.projectkorra.earthbending.lava.LavaFlow.AbilityType;
import com.projectkorra.projectkorra.earthbending.lava.LavaSurge;
import com.projectkorra.projectkorra.earthbending.metal.Extraction;
import com.projectkorra.projectkorra.earthbending.metal.MetalClips;
import com.projectkorra.projectkorra.earthbending.passive.DensityShift;
import com.projectkorra.projectkorra.earthbending.passive.EarthPassive;
import com.projectkorra.projectkorra.earthbending.passive.FerroControl;
import com.projectkorra.projectkorra.event.BendingPlayerCreationEvent;
import com.projectkorra.projectkorra.event.EntityBendingDeathEvent;
import com.projectkorra.projectkorra.event.HorizontalVelocityChangeEvent;
import com.projectkorra.projectkorra.event.PlayerBindChangeEvent;
import com.projectkorra.projectkorra.event.PlayerChangeElementEvent;
import com.projectkorra.projectkorra.event.PlayerChangeSubElementEvent;
import com.projectkorra.projectkorra.event.PlayerCooldownChangeEvent;
import com.projectkorra.projectkorra.event.PlayerJumpEvent;
import com.projectkorra.projectkorra.event.PlayerStanceChangeEvent;
import com.projectkorra.projectkorra.firebending.Blaze;
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
import com.projectkorra.projectkorra.firebending.combustion.Combustion;
import com.projectkorra.projectkorra.firebending.lightning.Lightning;
import com.projectkorra.projectkorra.firebending.passive.FirePassive;
import com.projectkorra.projectkorra.firebending.util.FireDamageTimer;
import com.projectkorra.projectkorra.object.HorizontalVelocityTracker;
import com.projectkorra.projectkorra.object.Preset;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.FlightHandler;
import com.projectkorra.projectkorra.util.FlightHandler.Flight;
import com.projectkorra.projectkorra.util.MovementHandler;
import com.projectkorra.projectkorra.util.PassiveHandler;
import com.projectkorra.projectkorra.util.StatisticsManager;
import com.projectkorra.projectkorra.util.StatisticsMethods;
import com.projectkorra.projectkorra.util.TempArmor;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.OctopusForm;
import com.projectkorra.projectkorra.waterbending.SurgeWall;
import com.projectkorra.projectkorra.waterbending.SurgeWave;
import com.projectkorra.projectkorra.waterbending.Torrent;
import com.projectkorra.projectkorra.waterbending.WaterBubble;
import com.projectkorra.projectkorra.waterbending.WaterManipulation;
import com.projectkorra.projectkorra.waterbending.WaterSpout;
import com.projectkorra.projectkorra.waterbending.WaterSpoutWave;
import com.projectkorra.projectkorra.waterbending.blood.Bloodbending;
import com.projectkorra.projectkorra.waterbending.combo.IceBullet;
import com.projectkorra.projectkorra.waterbending.healing.HealingWaters;
import com.projectkorra.projectkorra.waterbending.ice.IceBlast;
import com.projectkorra.projectkorra.waterbending.ice.IceSpikeBlast;
import com.projectkorra.projectkorra.waterbending.ice.PhaseChange;
import com.projectkorra.projectkorra.waterbending.ice.PhaseChange.PhaseChangeType;
import com.projectkorra.projectkorra.waterbending.multiabilities.WaterArms;
import com.projectkorra.projectkorra.waterbending.passive.FastSwim;
import com.projectkorra.projectkorra.waterbending.passive.HydroSink;

import co.aikar.timings.lib.MCTiming;

public class PKListener implements Listener {
	ProjectKorra plugin;

	private static final HashMap<Entity, Ability> BENDING_ENTITY_DEATH = new HashMap<>(); // Entities killed by Bending.
	private static final HashMap<Player, String> BENDING_PLAYER_DEATH = new HashMap<>(); // Player killed by Bending.
	private static final List<UUID> RIGHT_CLICK_INTERACT = new ArrayList<UUID>(); // Player right click block.
	private static final ArrayList<UUID> TOGGLED_OUT = new ArrayList<>(); // Stands for toggled = false while logging out.
	private static final List<Player> PLAYER_DROPPED_ITEM = new ArrayList<>(); // Player dropped an item.
	private static final Map<Player, Integer> JUMPS = new HashMap<>();

	private static MCTiming TimingPhysicsWaterManipulationCheck, TimingPhysicsEarthPassiveCheck, TimingPhysicsIlluminationTorchCheck, TimingPhysicsEarthAbilityCheck, TimingPhysicsAirTempBlockBelowFallingBlockCheck;
	private static MCTiming TimingPlayerMoveMovementHandlerCheck, TimingPlayerMoveSpoutCheck, TimingPlayerMoveBloodbentCheck, TimingPlayerMoveAirChiPassiveCheck, TimingPlayerMoveFirePassiveCheck, TimingPlayerMoveJumpCheck;

	public PKListener(final ProjectKorra plugin) {

		this.plugin = plugin;

		TimingPhysicsWaterManipulationCheck = ProjectKorra.timing("PhysicsWaterManipulationCheck");
		TimingPhysicsEarthPassiveCheck = ProjectKorra.timing("PhysicsEarthPassiveCheck");
		TimingPhysicsIlluminationTorchCheck = ProjectKorra.timing("PhysicsIlluminationTorchCheck");
		TimingPhysicsEarthAbilityCheck = ProjectKorra.timing("PhysicsEarthAbilityCheck");
		TimingPhysicsAirTempBlockBelowFallingBlockCheck = ProjectKorra.timing("PhysicsAirTempBlockBelowFallingBlockCheck");

		TimingPlayerMoveMovementHandlerCheck = ProjectKorra.timing("PlayerMoveMovementHandlerCheck");
		TimingPlayerMoveSpoutCheck = ProjectKorra.timing("PlayerMoveSpoutCheck");
		TimingPlayerMoveBloodbentCheck = ProjectKorra.timing("PlayerMoveBloodbentCheck");
		TimingPlayerMoveAirChiPassiveCheck = ProjectKorra.timing("PlayerMoveAirChiPassiveCheck");
		TimingPlayerMoveFirePassiveCheck = ProjectKorra.timing("PlayerMoveFirePassiveCheck");
		TimingPlayerMoveJumpCheck = ProjectKorra.timing("PlayerMoveJumpCheck");
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockBreak(final BlockBreakEvent event) {
		final Block block = event.getBlock();
		final Player player = event.getPlayer();
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		final String abil = bPlayer.getBoundAbilityName();
		CoreAbility ability = null;

		if (Illumination.isIlluminationTorch(block.getRelative(BlockFace.UP))) {
			TempBlock torch = TempBlock.get(block.getRelative(BlockFace.UP));
			Player user = Illumination.getBlocks().get(torch);
			Illumination illumination = CoreAbility.getAbility(user, Illumination.class);
			if (illumination != null) {
				illumination.remove();
			}
		}

		if (bPlayer.isElementToggled(Element.WATER) && bPlayer.isToggled()) {
			if (abil != null && abil.equalsIgnoreCase("Surge")) {
				ability = CoreAbility.getAbility(SurgeWall.class);
			} else if (abil != null && abil.equalsIgnoreCase("Torrent")) {
				ability = CoreAbility.getAbility(Torrent.class);
			} else if (abil != null && abil.equalsIgnoreCase("WaterSpout")) {
				ability = CoreAbility.getAbility(WaterSpoutWave.class);
			} else {
				ability = CoreAbility.getAbility(abil);
			}

			if (ability != null && ability instanceof WaterAbility && !((WaterAbility) ability).allowBreakPlants() && WaterAbility.isPlantbendable(player, block.getType(), false)) {
				event.setCancelled(true);
				return;
			}
		}

		final EarthBlast blast = EarthBlast.getBlastFromSource(block);
		if (blast != null) {
			blast.remove();
		}

		if (PhaseChange.getFrozenBlocksAsBlock().contains(block)) {
			if (PhaseChange.thaw(block)) {
				event.setCancelled(true);
			}
		} else if (SurgeWall.getWallBlocks().containsKey(block)) {
			event.setCancelled(true);
		} else if (Illumination.isIlluminationTorch(block)) {
			event.setCancelled(true);
		} else if (!SurgeWave.canThaw(block)) {
			SurgeWave.thaw(block);
			event.setCancelled(true);
		} else if (LavaFlow.isLavaFlowBlock(block)) {
			LavaFlow.removeBlock(block);
		} else if (EarthAbility.getMovedEarth().containsKey(block)) {
			EarthAbility.removeRevertIndex(block);
		} else if (TempBlock.isTempBlock(block)) {
			event.setCancelled(true);
			TempBlock.revertBlock(block, Material.AIR);
		} else if (DensityShift.isPassiveSand(block)) {
			DensityShift.revertSand(block);
		} else if (WaterBubble.isAir(block)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockFlowTo(final BlockFromToEvent event) {
		final Block toblock = event.getToBlock();
		final Block fromblock = event.getBlock();

		if (TempBlock.isTempBlock(fromblock) || TempBlock.isTempBlock(toblock)) {
			event.setCancelled(true);
		} else {
			if (ElementalAbility.isLava(fromblock)) {
				event.setCancelled(!EarthPassive.canFlowFromTo(fromblock, toblock));
			} else if (ElementalAbility.isWater(fromblock)) {
				event.setCancelled(WaterBubble.isAir(toblock));
				if (!event.isCancelled()) {
					event.setCancelled(!WaterManipulation.canFlowFromTo(fromblock, toblock));
				}

				if (!event.isCancelled()) {
					if (Illumination.isIlluminationTorch(toblock)) {
						toblock.setType(Material.AIR);
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onFluidLevelChange(final FluidLevelChangeEvent event) {
		if (TempBlock.isTempBlock(event.getBlock())) {
			event.setCancelled(true);
		} else if (TempBlock.isTouchingTempBlock(event.getBlock())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockForm(final BlockFormEvent event) {
		if (TempBlock.isTempBlock(event.getBlock())) {
			event.setCancelled(true);
		}

		if (!WaterManipulation.canPhysicsChange(event.getBlock())) {
			event.setCancelled(true);
		}

		if (!EarthPassive.canPhysicsChange(event.getBlock())) {
			event.setCancelled(true);
		}

		if (event.getBlock().getType().toString().equals("CONCRETE_POWDER")) {
			final BlockFace[] faces = new BlockFace[] { BlockFace.UP, BlockFace.DOWN, BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH };

			boolean marked = true;
			for (final BlockFace face : faces) {
				final Block b = event.getBlock().getRelative(face);
				if (b.getType() == Material.WATER) {
					if (!TempBlock.isTempBlock(b)) {
						marked = false; // if there is any normal water around it, prevent it.
						break;
					}
				}
			}

			if (marked) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockIgnite(final BlockIgniteEvent event) {}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockMeltEvent(final BlockFadeEvent event) {
		final Block block = event.getBlock();
		if (block.getType() == Material.FIRE) {
			return;
		}

		event.setCancelled(Illumination.isIlluminationTorch(block));
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
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockPhysics(final BlockPhysicsEvent event) {
		final Block block = event.getBlock();

		try (MCTiming timing = TimingPhysicsWaterManipulationCheck.startTiming()) {
			if (!WaterManipulation.canPhysicsChange(block)) {
				event.setCancelled(true);
				return;
			}
		}

		try (MCTiming timing = TimingPhysicsEarthPassiveCheck.startTiming()) {
			if (!EarthPassive.canPhysicsChange(block)) {
				event.setCancelled(true);
				return;
			}
		}

		try (MCTiming timing = TimingPhysicsIlluminationTorchCheck.startTiming()) {
			if (Illumination.isIlluminationTorch(block) || Illumination.isIlluminationTorch(block.getRelative(BlockFace.UP))) {
				event.setCancelled(true);
				return;
			}
		}

		try (MCTiming timing = TimingPhysicsEarthAbilityCheck.startTiming()) {
			if (EarthAbility.getPreventPhysicsBlocks().contains(block)) {
				event.setCancelled(true);
				return;
			}
		}

		// If there is a TempBlock of Air bellow FallingSand blocks, prevent it from updating.
		try (MCTiming timing = TimingPhysicsAirTempBlockBelowFallingBlockCheck.startTiming()) {
			if ((block.getType() == Material.SAND || block.getType() == Material.RED_SAND || block.getType() == Material.GRAVEL || block.getType() == Material.ANVIL || block.getType() == Material.DRAGON_EGG) && ElementalAbility.isAir(block.getRelative(BlockFace.DOWN).getType()) && TempBlock.isTempBlock(block.getRelative(BlockFace.DOWN))) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockPlace(final BlockPlaceEvent event) {
		final Player player = event.getPlayer();
		if (MovementHandler.isStopped(player) || Bloodbending.isBloodbent(player) || Suffocate.isBreathbent(player)) {
			event.setCancelled(true);
			return;
		}

		if (TempBlock.isTempBlock(event.getBlock()) && event.getItemInHand().getType() != Material.FLINT_AND_STEEL) {
			TempBlock.removeBlock(event.getBlock());
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onElementChange(final PlayerChangeElementEvent event) {
		final Player player = event.getTarget();
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		PassiveManager.registerPassives(player);
		final boolean chatEnabled = ConfigManager.languageConfig.get().getBoolean("Chat.Enable");
		if (chatEnabled) {
			final Element element = event.getElement();
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
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityChangeBlockEvent(final EntityChangeBlockEvent event) {
		final Entity entity = event.getEntity();
		if (MovementHandler.isStopped(entity) || Bloodbending.isBloodbent(entity) || Suffocate.isBreathbent(entity)) {
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
	public void onEntityCombust(final EntityCombustEvent event) {
		final Entity entity = event.getEntity();
		final Block block = entity.getLocation().getBlock();
		if (FireAbility.getSourcePlayers().containsKey(block) && entity instanceof LivingEntity) {
			new FireDamageTimer(entity, FireAbility.getSourcePlayers().get(block));
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityDamageBlock(final EntityDamageByBlockEvent event) {}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityDamageByBlock(final EntityDamageByBlockEvent event) {
		final Block block = event.getDamager();
		if (block == null) {
			return;
		}

		if (TempBlock.isTempBlock(block)) {
			if (EarthAbility.isEarthbendable(block.getType(), true, true, true) && GeneralMethods.isSolid(block)) {
				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityDamageEvent(final EntityDamageEvent event) {
		final Entity entity = event.getEntity();

		if (event.getCause() == DamageCause.FIRE && FireAbility.getSourcePlayers().containsKey(entity.getLocation().getBlock())) {
			new FireDamageTimer(entity, FireAbility.getSourcePlayers().get(entity.getLocation().getBlock()));
		}

		if (FireDamageTimer.isEnflamed(entity) && event.getCause() == DamageCause.FIRE_TICK) {
			event.setCancelled(true);
			FireDamageTimer.dealFlameDamage(entity);
		}

		if (entity instanceof LivingEntity && TempArmor.hasTempArmor((LivingEntity) entity)) {
			if (event.isApplicable(DamageModifier.ARMOR)) {
				event.setDamage(DamageModifier.ARMOR, 0);
			}
		}

		if (entity instanceof Player) {
			final Player player = (Player) entity;
			final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
			if (bPlayer == null) {
				return;
			}

			if (CoreAbility.hasAbility(player, EarthGrab.class)) {
				final EarthGrab abil = CoreAbility.getAbility(player, EarthGrab.class);
				abil.remove();
			}

			if (CoreAbility.getAbility(player, FireJet.class) != null && event.getCause() == DamageCause.FLY_INTO_WALL) {
				event.setCancelled(true);
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
	public void onEntityDeath(final EntityDeathEvent event) {
		if (TempArmor.hasTempArmor(event.getEntity())) {
			for (final TempArmor tarmor : TempArmor.getTempArmorList(event.getEntity())) {
				tarmor.revert(event.getDrops());
			}

			if (MetalClips.isControlled(event.getEntity())) {
				event.getDrops().add(new ItemStack(Material.IRON_INGOT, MetalClips.getTargetToAbility().get(event.getEntity()).getMetalClipsCount()));
			}
		}

		final CoreAbility[] cookingFireCombos = { CoreAbility.getAbility("JetBlast"), CoreAbility.getAbility("FireWheel"), CoreAbility.getAbility("FireSpin"), CoreAbility.getAbility("FireKick") };

		if (BENDING_ENTITY_DEATH.containsKey(event.getEntity())) {
			final CoreAbility coreAbility = (CoreAbility) BENDING_ENTITY_DEATH.get(event.getEntity());
			for (final CoreAbility fireCombo : cookingFireCombos) {
				if (coreAbility.getName().equalsIgnoreCase(fireCombo.getName())) {
					final List<ItemStack> drops = event.getDrops();
					final List<ItemStack> newDrops = new ArrayList<>();
					for (int i = 0; i < drops.size(); i++) {
						ItemStack cooked = drops.get(i);
						final Material material = drops.get(i).getType();
						switch (material) {
							case BEEF:
								cooked = new ItemStack(Material.COOKED_BEEF);
								break;
							case SALMON:
								cooked = new ItemStack(Material.COOKED_SALMON);
								break;
							case CHICKEN:
								cooked = new ItemStack(Material.COOKED_CHICKEN);
								break;
							case PORKCHOP:
								cooked = new ItemStack(Material.COOKED_PORKCHOP);
								break;
							case MUTTON:
								cooked = new ItemStack(Material.COOKED_MUTTON);
								break;
							case RABBIT:
								cooked = new ItemStack(Material.COOKED_RABBIT);
								break;
							case COD:
								cooked = new ItemStack(Material.COOKED_COD);
								break;
							default:
								break;
						}

						newDrops.add(cooked);
					}
					event.getDrops().clear();
					event.getDrops().addAll(newDrops);

					break;
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onEntityExplode(final EntityExplodeEvent event) {
		for (final Block block : event.blockList()) {
			final EarthBlast blast = EarthBlast.getBlastFromSource(block);

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
	public void onEntityExplodeEvent(final EntityExplodeEvent event) {
		final Entity entity = event.getEntity();
		if (entity != null) {
			if (MovementHandler.isStopped(entity) || Bloodbending.isBloodbent(entity) || Suffocate.isBreathbent(entity)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityInteractEvent(final EntityInteractEvent event) {
		final Entity entity = event.getEntity();
		if (MovementHandler.isStopped(entity) || Bloodbending.isBloodbent(entity) || Suffocate.isBreathbent(entity)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityProjectileLaunchEvent(final ProjectileLaunchEvent event) {
		final Entity entity = event.getEntity();
		if (MovementHandler.isStopped(entity) || Bloodbending.isBloodbent(entity) || Suffocate.isBreathbent(entity)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityShootBowEvent(final EntityShootBowEvent event) {
		final Entity entity = event.getEntity();
		if (MovementHandler.isStopped(entity) || Bloodbending.isBloodbent(entity) || Suffocate.isBreathbent(entity)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntitySlimeSplitEvent(final SlimeSplitEvent event) {
		final Entity entity = event.getEntity();
		if (MovementHandler.isStopped(entity) || Bloodbending.isBloodbent(entity) || Suffocate.isBreathbent(entity)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntitySuffocatedByTempBlocks(final EntityDamageEvent event) {
		if (event.getCause() == DamageCause.SUFFOCATION) {
			if (TempBlock.isTempBlock(event.getEntity().getLocation().add(0, 1, 0).getBlock())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityTarget(final EntityTargetEvent event) {
		final Entity entity = event.getEntity();
		if (MovementHandler.isStopped(entity) || Bloodbending.isBloodbent(entity)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityTargetLiving(final EntityTargetLivingEntityEvent event) {
		final Entity entity = event.getEntity();
		if (MovementHandler.isStopped(entity) || Bloodbending.isBloodbent(entity)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityTeleportEvent(final EntityTeleportEvent event) {
		final Entity entity = event.getEntity();
		if (MovementHandler.isStopped(entity) || Bloodbending.isBloodbent(entity) || Suffocate.isBreathbent(entity) || (entity instanceof LivingEntity && MetalClips.isControlled((LivingEntity) entity))) {
			event.setCancelled(true);
		}

		if (entity instanceof LivingEntity && TempArmor.hasTempArmor((LivingEntity) entity)) {
			for (final TempArmor armor : TempArmor.getTempArmorList((LivingEntity) entity)) {
				armor.revert(null);
			}
		}

		if (entity instanceof Player) {
			final Player player = (Player) entity;
			if (CoreAbility.hasAbility(player, EarthArmor.class)) {
				final EarthArmor abil = CoreAbility.getAbility(player, EarthArmor.class);
				abil.remove();
			}
		}
	}

	@EventHandler
	public void onHorizontalCollision(final HorizontalVelocityChangeEvent e) {
		if (e.getEntity() instanceof LivingEntity) {
			if (e.getEntity().getEntityId() != e.getInstigator().getEntityId()) {
				final double minimumDistance = this.plugin.getConfig().getDouble("Properties.HorizontalCollisionPhysics.WallDamageMinimumDistance");
				final double maxDamage = this.plugin.getConfig().getDouble("Properties.HorizontalCollisionPhysics.WallDamageCap");
				final double damage = ((e.getDistanceTraveled() - minimumDistance) < 0 ? 0 : e.getDistanceTraveled() - minimumDistance) / (e.getDifference().length());
				if (damage > 0) {
					if (damage <= maxDamage) {
						DamageHandler.damageEntity(e.getEntity(), damage, e.getAbility());
					} else {
						DamageHandler.damageEntity(e.getEntity(), maxDamage, e.getAbility());
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onInventoryClick(final InventoryClickEvent event) {
		for (final MetalClips clips : CoreAbility.getAbilities(MetalClips.class)) {
			if (clips.getTargetEntity() != null && clips.getTargetEntity().getEntityId() == event.getWhoClicked().getEntityId()) {
				event.setCancelled(true);
				break;
			}
		}

		for (int i = 0; i < 4; i++) {
			if (event.getSlot() == 36 + i && TempArmor.hasTempArmor(event.getWhoClicked())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityBendingDeath(final EntityBendingDeathEvent event) {
		BENDING_ENTITY_DEATH.put(event.getEntity(), event.getAbility());
		if (event.getEntity() instanceof Player) {
			if (ConfigManager.languageConfig.get().getBoolean("DeathMessages.Enabled")) {
				final Ability ability = event.getAbility();
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
			if (event.getAttacker() != null && ProjectKorra.isStatisticsEnabled()) {
				StatisticsMethods.addStatisticAbility(event.getAttacker().getUniqueId(), CoreAbility.getAbility(event.getAbility().getName()), com.projectkorra.projectkorra.util.Statistic.PLAYER_KILLS, 1);
			}
		}
		if (event.getAttacker() != null && ProjectKorra.isStatisticsEnabled()) {
			StatisticsMethods.addStatisticAbility(event.getAttacker().getUniqueId(), CoreAbility.getAbility(event.getAbility().getName()), com.projectkorra.projectkorra.util.Statistic.TOTAL_KILLS, 1);
		}
	}

	@EventHandler
	public void onPlayerBucketEmpty(final PlayerBucketEmptyEvent event) {
		final Block block = event.getBlockClicked().getRelative(event.getBlockFace());

		if (Illumination.isIlluminationTorch(block)) {
			final Player player = Illumination.getBlocks().get(TempBlock.get(block));
			CoreAbility.getAbility(player, Illumination.class).remove();
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerChat(final AsyncPlayerChatEvent event) {
		final Player player = event.getPlayer();
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		String e = "Nonbender";
		ChatColor c = ChatColor.WHITE;
		if (bPlayer != null) {
			if (player.hasPermission("bending.avatar") || bPlayer.getElements().size() > 1) {
				c = Element.AVATAR.getColor();
				e = Element.AVATAR.getName();
			} else if (bPlayer.getElements().size() > 0) {
				c = bPlayer.getElements().get(0).getColor();
				e = bPlayer.getElements().get(0).getName();
			}
		}
		final String element = ConfigManager.languageConfig.get().getString("Chat.Prefixes." + e);
		event.setFormat(event.getFormat().replace("{element}", c + element + ChatColor.RESET).replace("{ELEMENT}", c + element + ChatColor.RESET).replace("{elementcolor}", c + "").replace("{ELEMENTCOLOR}", c + ""));

		if (!ConfigManager.languageConfig.get().getBoolean("Chat.Enable")) {
			return;
		}

		ChatColor color = ChatColor.WHITE;

		if (bPlayer == null) {
			return;
		}

		if (player.hasPermission("bending.avatar") || (bPlayer.hasElement(Element.AIR) && bPlayer.hasElement(Element.EARTH) && bPlayer.hasElement(Element.FIRE) && bPlayer.hasElement(Element.WATER))) {
			color = ChatColor.valueOf(ConfigManager.languageConfig.get().getString("Chat.Colors.Avatar"));
		} else if (bPlayer.getElements().size() > 0) {
			color = bPlayer.getElements().get(0).getColor();
		}

		String format = ConfigManager.languageConfig.get().getString("Chat.Format");
		format = format.replace("<message>", "%2$s");
		format = format.replace("<name>", color + player.getDisplayName() + ChatColor.RESET);
		event.setFormat(format);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerDamage(final EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			final Player player = (Player) event.getEntity();
			final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

			if (bPlayer == null) {
				return;
			} else if (bPlayer.isChiBlocked()) {
				return;
			}

			if (FlightMultiAbility.getFlyingPlayers().contains(player.getUniqueId())) {
				final FlightMultiAbility fma = CoreAbility.getAbility(player, FlightMultiAbility.class);
				fma.cancel("taking damage");
			}

			if (bPlayer.hasElement(Element.EARTH) && event.getCause() == DamageCause.FALL) {
				if (bPlayer.getBoundAbilityName().equalsIgnoreCase("Shockwave")) {
					new Shockwave(player, true);
				} else if (bPlayer.getBoundAbilityName().equalsIgnoreCase("Catapult")) {
					new EarthPillars(player, true);
				}
			}

			if (bPlayer.hasElement(Element.AIR) && event.getCause() == DamageCause.FALL) {
				if (bPlayer.getBoundAbilityName().equalsIgnoreCase("AirBurst")) {
					new AirBurst(player, true);
				}
			}

			CoreAbility gd = CoreAbility.getAbility(GracefulDescent.class);
			CoreAbility ds = CoreAbility.getAbility(DensityShift.class);
			CoreAbility hs = CoreAbility.getAbility(HydroSink.class);
			CoreAbility ab = CoreAbility.getAbility(Acrobatics.class);

			if (gd != null && bPlayer.hasElement(Element.AIR) && event.getCause() == DamageCause.FALL && bPlayer.canBendPassive(gd) && bPlayer.canUsePassive(gd) && gd.isEnabled() && PassiveManager.hasPassive(player, gd)) {
				event.setDamage(0D);
				event.setCancelled(true);
			} else if (ds != null && bPlayer.hasElement(Element.EARTH) && event.getCause() == DamageCause.FALL && bPlayer.canBendPassive(ds) && bPlayer.canUsePassive(ds) && ds.isEnabled() && PassiveManager.hasPassive(player, ds)) {
				if (DensityShift.softenLanding(player)) {
					event.setDamage(0D);
					event.setCancelled(true);
				}
			} else if (hs != null && bPlayer.hasElement(Element.WATER) && event.getCause() == DamageCause.FALL && bPlayer.canBendPassive(hs) && bPlayer.canUsePassive(hs) && hs.isEnabled() && PassiveManager.hasPassive(player, hs)) {
				if (HydroSink.applyNoFall(player)) {
					event.setDamage(0D);
					event.setCancelled(true);
				}
			}

			if (ab != null && bPlayer.hasElement(Element.CHI) && event.getCause() == DamageCause.FALL && bPlayer.canBendPassive(ab) && bPlayer.canUsePassive(ab) && ab.isEnabled() && PassiveManager.hasPassive(player, ab)) {
				final double initdamage = event.getDamage();
				final double newdamage = event.getDamage() * Acrobatics.getFallReductionFactor();
				final double finaldamage = initdamage - newdamage;
				event.setDamage(finaldamage);
				if (finaldamage <= 0.4) {
					event.setCancelled(true);
				}
			}

			if (event.getCause() == DamageCause.FALL) {
				final Flight flight = Manager.getManager(FlightHandler.class).getInstance(player);
				if (flight != null) {
					if (flight.getPlayer() == flight.getSource()) {
						event.setCancelled(true);
					}
				}
			}

			CoreAbility hc = CoreAbility.getAbility(HeatControl.class);

			if (hc != null && bPlayer.hasElement(Element.FIRE) && bPlayer.canBendPassive(hc) && bPlayer.canUsePassive(hc) && (event.getCause() == DamageCause.FIRE || event.getCause() == DamageCause.FIRE_TICK)) {
				event.setCancelled(!HeatControl.canBurn(player));
			}

			if (bPlayer.hasElement(Element.EARTH) && event.getCause() == DamageCause.SUFFOCATION && TempBlock.isTempBlock(player.getEyeLocation().getBlock())) {
				event.setDamage(0D);
				event.setCancelled(true);
			}

			if (CoreAbility.getAbility(player, EarthArmor.class) != null) {
				final EarthArmor eartharmor = CoreAbility.getAbility(player, EarthArmor.class);
				eartharmor.updateAbsorbtion();
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerDamageByPlayer(final EntityDamageByEntityEvent e) {
		final Entity source = e.getDamager();
		final Entity entity = e.getEntity();
		final FireBlastCharged fireball = FireBlastCharged.getFireball(source);

		if (fireball != null) {
			e.setCancelled(true);
			fireball.dealDamage(entity);
			return;
		}

		if (MovementHandler.isStopped(e.getDamager())) {
			final CoreAbility ability = (CoreAbility) e.getDamager().getMetadata("movement:stop").get(0).value();
			if (!(ability instanceof EarthGrab)) {
				e.setCancelled(true);
				return;
			}
		}

		if (entity instanceof Player) {
			Suffocate.remove((Player) entity);
		}

		if (source instanceof Player) { // This is the player hitting someone.
			final Player sourcePlayer = (Player) source;
			final BendingPlayer sourceBPlayer = BendingPlayer.getBendingPlayer(sourcePlayer);
			if (sourceBPlayer == null) {
				return;
			}

			final Ability boundAbil = sourceBPlayer.getBoundAbility();

			if (sourceBPlayer.getBoundAbility() != null) {
				if (!sourceBPlayer.isOnCooldown(boundAbil)) {
					if (sourceBPlayer.canBendPassive(sourceBPlayer.getBoundAbility())) {
						if (e.getCause() == DamageCause.ENTITY_ATTACK) {
							if (sourceBPlayer.getBoundAbility() instanceof ChiAbility) {
								if (sourceBPlayer.canCurrentlyBendWithWeapons()) {
									if (sourceBPlayer.isElementToggled(Element.CHI)) {
										if (boundAbil.equals(CoreAbility.getAbility(Paralyze.class))) {
											new Paralyze(sourcePlayer, entity);
										} else if (boundAbil.equals(CoreAbility.getAbility(QuickStrike.class))) {
											new QuickStrike(sourcePlayer, entity);
											e.setCancelled(true);
										} else if (boundAbil.equals(CoreAbility.getAbility(SwiftKick.class))) {
											new SwiftKick(sourcePlayer, entity);
											e.setCancelled(true);
										} else if (boundAbil.equals(CoreAbility.getAbility(RapidPunch.class))) {
											new RapidPunch(sourcePlayer, entity);
											e.setCancelled(true);
										}
									}
								}
							}
						}
					}
				}
			} else {
				if (e.getCause() == DamageCause.ENTITY_ATTACK) {
					if (sourceBPlayer.canCurrentlyBendWithWeapons()) {
						if (sourceBPlayer.isElementToggled(Element.CHI)) {
							if (entity instanceof Player) {
								final Player targetPlayer = (Player) entity;
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

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerDeath(final PlayerDeathEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}

		if (event.getKeepInventory()) {
			if (TempArmor.hasTempArmor(event.getEntity())) {
				for (final TempArmor armor : TempArmor.getTempArmorList(event.getEntity())) {
					armor.revert(event.getDrops());
				}
			}
		} else {
			// Do nothing. TempArmor drops are handled by the EntityDeath event and not PlayerDeath.
		}

		if (event.getEntity().getKiller() != null) {
			if (BENDING_PLAYER_DEATH.containsKey(event.getEntity())) {
				String message = ConfigManager.languageConfig.get().getString("DeathMessages.Default");
				final String ability = BENDING_PLAYER_DEATH.get(event.getEntity());
				final String tempAbility = ChatColor.stripColor(ability).replaceAll(" ", "");
				final CoreAbility coreAbil = CoreAbility.getAbility(tempAbility);
				Element element = null;
				final boolean isAvatarAbility = false;

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
	public void onPlayerItemDrop(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (event.isCancelled())
			return;

		if (bPlayer == null)
			return;

		if (bPlayer.getBoundAbility() == null)
			return;

		if (!PLAYER_DROPPED_ITEM.contains(player))
			PLAYER_DROPPED_ITEM.add(player);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerInteraction(final PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (!RIGHT_CLICK_INTERACT.contains(player.getUniqueId())) {
				final UUID uuid = player.getUniqueId();
				RIGHT_CLICK_INTERACT.add(uuid);

				new BukkitRunnable() {
					@Override
					public void run() {
						RIGHT_CLICK_INTERACT.remove(uuid);
					}
				}.runTaskLater(this.plugin, 5);
			}

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

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
			if (bPlayer.getBoundAbilityName().equalsIgnoreCase("IceBlast")) {
				if (CoreAbility.hasAbility(player, IceBullet.class)) {
					CoreAbility.getAbility(player, IceBullet.class).doRightClick();
				}
			}
		}

		if (MovementHandler.isStopped(player) || Bloodbending.isBloodbent(player) || Suffocate.isBreathbent(player)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerInteractEntity(final PlayerInteractAtEntityEvent event) {
		final Player player = event.getPlayer();
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer.canCurrentlyBendWithWeapons()) {
			ComboManager.addComboAbility(player, ClickType.RIGHT_CLICK_ENTITY);
		}

		if (event.getRightClicked().hasMetadata("earthgrab:trap")) {
			final EarthGrab eg = (EarthGrab) event.getRightClicked().getMetadata("earthgrab:trap").get(0).value();
			eg.damageTrap();
			event.setCancelled(true);
			return;
		}

		if (event.getRightClicked().hasMetadata("temparmorstand")) {
			event.setCancelled(true);
			return;
		}

		if (MovementHandler.isStopped(player) || Bloodbending.isBloodbent(player) || Suffocate.isBreathbent(player)) {
			event.setCancelled(true);
			return;
		}

		if (bPlayer.getBoundAbilityName().equalsIgnoreCase("HealingWaters") && event.getHand().equals(EquipmentSlot.HAND)) {
			final HealingWaters instance = CoreAbility.getAbility(player, HealingWaters.class);
			if (instance != null && instance.charged) {
				instance.click();
				event.setCancelled(true);
				return;
			}
		}

		if (!RIGHT_CLICK_INTERACT.contains(player.getUniqueId())) {
			if (event.getRightClicked() instanceof Player) {
				final Player target = (Player) event.getRightClicked();
				if (FlightMultiAbility.getFlyingPlayers().contains(player.getUniqueId())) {
					final FlightMultiAbility fma = CoreAbility.getAbility(player, FlightMultiAbility.class);
					fma.requestCarry(target);
					final UUID uuid = player.getUniqueId();
					RIGHT_CLICK_INTERACT.add(uuid);

					new BukkitRunnable() {
						@Override
						public void run() {
							RIGHT_CLICK_INTERACT.remove(uuid);
						}
					}.runTaskLater(this.plugin, 5);
				} else if (FlightMultiAbility.getFlyingPlayers().contains(target.getUniqueId())) {
					FlightMultiAbility.acceptCarryRequest(player, target);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerItemDamage(final PlayerItemDamageEvent event) {
		if (TempArmor.hasTempArmor(event.getPlayer())) {
			final TempArmor armor = TempArmor.getVisibleTempArmor(event.getPlayer());
			for (final ItemStack i : armor.getNewArmor()) {
				if (i != null && event.getItem().isSimilar(i)) {
					event.setCancelled(true);
					break;
				}
			}
		}
	}

	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		JUMPS.put(player, player.getStatistic(Statistic.JUMP));

		GeneralMethods.createBendingPlayer(player.getUniqueId(), player.getName());
		if (ProjectKorra.isStatisticsEnabled()) {
			Manager.getManager(StatisticsManager.class).load(player.getUniqueId());
		}
		Bukkit.getScheduler().runTaskLater(ProjectKorra.plugin, (Runnable) () -> {
			PassiveManager.registerPassives(player);
			GeneralMethods.removeUnusableAbilities(player.getName());
		}, 5);

		if (ConfigManager.languageConfig.get().getBoolean("Chat.Branding.JoinMessage.Enabled")) {
			Bukkit.getScheduler().runTaskLater(ProjectKorra.plugin, (Runnable) () -> {
				ChatColor color = ChatColor.valueOf(ConfigManager.languageConfig.get().getString("Chat.Branding.Color").toUpperCase());
				color = color == null ? ChatColor.GOLD : color;
				final String topBorder = ConfigManager.languageConfig.get().getString("Chat.Branding.Borders.TopBorder");
				final String bottomBorder = ConfigManager.languageConfig.get().getString("Chat.Branding.Borders.BottomBorder");
				if (!topBorder.isEmpty()) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', topBorder));
				}
				player.sendMessage(color + "This server is running ProjectKorra version " + ProjectKorra.plugin.getDescription().getVersion() + " for bending! Find out more at http://www.projectkorra.com!");
				if (!bottomBorder.isEmpty()) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', bottomBorder));
				}
			}, 20 * 4);
		}
	}

	@EventHandler
	public void onPlayerChangeWorld(final PlayerChangedWorldEvent event) {
		PassiveManager.registerPassives(event.getPlayer());
		BendingBoardManager.forceToggleScoreboard(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerKick(final PlayerKickEvent event) {
		JUMPS.remove(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerMove(final PlayerMoveEvent event) {
		if (event.getTo().getX() == event.getFrom().getX() && event.getTo().getY() == event.getFrom().getY() && event.getTo().getZ() == event.getFrom().getZ()) {
			return;
		}

		final Player player = event.getPlayer();
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		try (MCTiming timing = TimingPlayerMoveMovementHandlerCheck.startTiming()) {
			if (MovementHandler.isStopped(player)) {
				if (event.getTo().getX() != event.getFrom().getX() || event.getTo().getZ() != event.getFrom().getZ() || event.getTo().getY() > event.getFrom().getY()) {
					event.setCancelled(true);
				}
				return;
			}
		}

		try (MCTiming timing = TimingPlayerMoveSpoutCheck.startTiming()) {
			if (CoreAbility.hasAbility(player, WaterSpout.class) || CoreAbility.hasAbility(player, AirSpout.class)) {
				Vector vel = new Vector();
				vel.setX(event.getTo().getX() - event.getFrom().getX());
				vel.setZ(event.getTo().getZ() - event.getFrom().getZ());

				final double currspeed = vel.length();
				final double maxspeed = .2;
				if (currspeed > maxspeed) {
					// apply only if moving set a factor
					vel = vel.normalize().multiply(maxspeed);
					// apply the new velocity
					event.getPlayer().setVelocity(vel);
				}
				return;
			}
		}

		try (MCTiming timing = TimingPlayerMoveBloodbentCheck.startTiming()) {
			if (Bloodbending.isBloodbent(player)) {
				final BendingPlayer bender = Bloodbending.getBloodbender(player);
				if (bender.isAvatarState()) {
					event.setCancelled(true);
					return;
				}

				final Location loc = Bloodbending.getBloodbendingLocation(player);
				if (player.getWorld().equals(loc.getWorld())) {
					if (!player.getVelocity().equals(Bloodbending.getBloodbendingVector(player))) {
						player.setVelocity(Bloodbending.getBloodbendingVector(player));
					}
				}
				return;
			}
		}

		if (bPlayer != null) {
			try (MCTiming timing = TimingPlayerMoveAirChiPassiveCheck) {
				if (bPlayer.hasElement(Element.AIR) || bPlayer.hasElement(Element.CHI)) {
					PassiveHandler.checkExhaustionPassives(player);
				}
			}

			try (MCTiming timing = TimingPlayerMoveFirePassiveCheck.startTiming()) {
				if (event.getTo().getBlock() != event.getFrom().getBlock()) {
					FirePassive.handle(player);
				}
			}
		}

		try (MCTiming timing = TimingPlayerMoveJumpCheck.startTiming()) {
			if (event.getTo().getY() > event.getFrom().getY()) {
				if (!(player.getLocation().getBlock().getType() == Material.VINE) && !(player.getLocation().getBlock().getType() == Material.LADDER)) {
					final int current = player.getStatistic(Statistic.JUMP);
					final int last = JUMPS.get(player);

					if (last != current) {
						JUMPS.put(player, current);

						final double yDif = event.getTo().getY() - event.getFrom().getY();

						if ((yDif < 0.035 || yDif > 0.037) && (yDif < 0.116 || yDif > 0.118)) {
							Bukkit.getServer().getPluginManager().callEvent(new PlayerJumpEvent(player, yDif));
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerGamemodeChange(final PlayerGameModeChangeEvent event) {
		final Player player = event.getPlayer();
		if (event.getNewGameMode() == GameMode.SPECTATOR) {
			if (!Commands.invincible.contains(player.getName())) {
				Commands.invincible.add(player.getName());
			}
		} else if (!(event.getNewGameMode() == GameMode.SPECTATOR) && Commands.invincible.contains(player.getName())) {
			Commands.invincible.remove(player.getName());
		}

	}

	@EventHandler
	public void onPlayerQuit(final PlayerQuitEvent event) {
		final Player player = event.getPlayer();
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		BendingBoardManager.clean(player);

		if (ProjectKorra.isStatisticsEnabled()) {
			Manager.getManager(StatisticsManager.class).store(player.getUniqueId());
		}
		if (bPlayer != null) {
			if (ProjectKorra.isDatabaseCooldownsEnabled()) {
				bPlayer.saveCooldowns();
			}

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
			for (final TempArmor armor : TempArmor.getTempArmorList(player)) {
				armor.revert(null);
			}
		}

		if (MetalClips.isControlled(event.getPlayer())) {
			MetalClips.removeControlledEnitity(event.getPlayer());
		}

		MultiAbilityManager.remove(player);
		JUMPS.remove(player);

		for (final CoreAbility ca : CoreAbility.getAbilities()) {
			if (CoreAbility.getAbility(event.getPlayer(), ca.getClass()) != null) {
				CoreAbility.getAbility(event.getPlayer(), ca.getClass()).remove();
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerSneak(final PlayerToggleSneakEvent event) {
		final Player player = event.getPlayer();
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer == null) {
			return;
		}

		if (bPlayer.canCurrentlyBendWithWeapons()) {
			if (player.isSneaking()) {
				ComboManager.addComboAbility(player, ClickType.SHIFT_UP);
			} else {
				ComboManager.addComboAbility(player, ClickType.SHIFT_DOWN);
			}
		}

		final String abilName = bPlayer.getBoundAbilityName();
		if (Suffocate.isBreathbent(player)) {
			if (!abilName.equalsIgnoreCase("AirSwipe") || !abilName.equalsIgnoreCase("FireBlast") || !abilName.equalsIgnoreCase("EarthBlast") || !abilName.equalsIgnoreCase("WaterManipulation")) {
				if (!player.isSneaking()) {
					event.setCancelled(true);
				}
			}
		}

		if (MovementHandler.isStopped(player) || Bloodbending.isBloodbent(player)) {
			if (!player.isSneaking()) {
				event.setCancelled(true);
				return;
			}
		}

		if (bPlayer.isChiBlocked()) {
			event.setCancelled(true);
			return;
		}

		if (!player.isSneaking()) {
			BlockSource.update(player, ClickType.SHIFT_DOWN);
		}

		AirScooter.check(player);

		final CoreAbility coreAbil = bPlayer.getBoundAbility();
		final String abil = bPlayer.getBoundAbilityName();

		if (coreAbil == null || !coreAbil.isSneakAbility()) {
			if (PassiveManager.hasPassive(player, CoreAbility.getAbility(FerroControl.class))) {
				new FerroControl(player);
			}

			if (PassiveManager.hasPassive(player, CoreAbility.getAbility(FastSwim.class))) {
				new FastSwim(player);
			}
		}

		if (coreAbil == null) {
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
						new AirSuction(player);
					} else if (abil.equalsIgnoreCase("AirSwipe")) {
						new AirSwipe(player, true);
					} else if (abil.equalsIgnoreCase("AirShield")) {
						new AirShield(player);
					} else if (abil.equalsIgnoreCase("Suffocate")) {
						new Suffocate(player);
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
							final PhaseChange pc = CoreAbility.getAbility(player, PhaseChange.class);
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
						final MetalClips clips = CoreAbility.getAbility(player, MetalClips.class);
						if (clips != null) {
							if (clips.getTargetEntity() == null) {
								clips.setMagnetized(true);
							} else {
								clips.setControlling(true);
							}
						} else {
							new MetalClips(player, 1);
						}
					} else if (abil.equalsIgnoreCase("EarthGrab")) {
						new EarthGrab(player, GrabMode.DRAG);
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

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerSlotChange(final PlayerItemHeldEvent event) {
		final Player player = event.getPlayer();
		if (!MultiAbilityManager.canChangeSlot(player, event.getNewSlot())) {
			event.setCancelled(true);
			return;
		}

		final int slot = event.getNewSlot() + 1;
		GeneralMethods.displayMovePreview(player, slot);
		BendingBoardManager.changeActiveSlot(player, event.getPreviousSlot(), event.getNewSlot());

		if (ConfigManager.defaultConfig.get().getBoolean("Abilities.Water.WaterArms.DisplayBoundMsg")) {
			final WaterArms waterArms = CoreAbility.getAbility(player, WaterArms.class);
			if (waterArms != null) {
				waterArms.displayBoundMsg(event.getNewSlot() + 1);
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerSwapItems(final PlayerSwapHandItemsEvent event) {
		final Player player = event.getPlayer();
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return;
		}

		final ItemStack main = event.getMainHandItem();
		final ItemStack off = event.getOffHandItem();
		if (main.getType() == Material.AIR && (off == null || off.getType() == Material.AIR)) {
			ComboManager.addComboAbility(player, ClickType.OFFHAND_TRIGGER);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerSwing(final PlayerInteractEvent event) {
		final Player player = event.getPlayer();

		if (PLAYER_DROPPED_ITEM.contains(player)) {
			PLAYER_DROPPED_ITEM.remove(player);
			return;
		}
		if (event.getHand() != EquipmentSlot.HAND) {
			return;
		}
		if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_AIR) {
			return;
		}
		if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.isCancelled()) {
			return;
		}
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return;
		} else if (RIGHT_CLICK_INTERACT.contains(player.getUniqueId())) {
			return;
		}

		final Entity target = GeneralMethods.getTargetedEntity(player, 3);

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
		} else if ((Bloodbending.isBloodbent(player) && !bPlayer.getBoundAbilityName().equalsIgnoreCase("AvatarState"))) {
			event.setCancelled(true);
			return;
		} else if (MovementHandler.isStopped(player)) {
			if (player.hasMetadata("movement:stop")) {
				final CoreAbility abil = (CoreAbility) player.getMetadata("movement:stop").get(0).value();
				if (!(abil instanceof EarthGrab)) {
					event.setCancelled(true);
					return;
				}
			}
		} else if (bPlayer.isChiBlocked()) {
			event.setCancelled(true);
			return;
		}

		BlockSource.update(player, ClickType.LEFT_CLICK);
		AirScooter.check(player);

		String abil = bPlayer.getBoundAbilityName();
		final CoreAbility coreAbil = bPlayer.getBoundAbility();

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
						AirSuction.shoot(player);
					} else if (abil.equalsIgnoreCase("AirBurst")) {
						AirBurst.coneBurst(player);
					} else if (abil.equalsIgnoreCase("AirScooter")) {
						new AirScooter(player);
					} else if (abil.equalsIgnoreCase("AirSpout")) {
						new AirSpout(player);
					} else if (abil.equalsIgnoreCase("AirSwipe")) {
						new AirSwipe(player);
					} else if (abil.equalsIgnoreCase("Flight")) {
						new FlightMultiAbility(player);
						return;
					}
				}
			}

			if (coreAbil instanceof WaterAbility && bPlayer.isElementToggled(Element.WATER) == true) {
				if (bPlayer.canCurrentlyBendWithWeapons()) {
					if (abil.equalsIgnoreCase("Bloodbending")) {
						Bloodbending.launch(player);
					} else if (abil.equalsIgnoreCase("IceBlast")) {
						if (CoreAbility.hasAbility(player, IceBullet.class)) {
							CoreAbility.getAbility(player, IceBullet.class).doLeftClick();
						} else {
							IceBlast.activate(player);
						}
					} else if (abil.equalsIgnoreCase("IceSpike")) {
						IceSpikeBlast.activate(player);
					} else if (abil.equalsIgnoreCase("OctopusForm")) {
						new OctopusForm(player);
					} else if (abil.equalsIgnoreCase("PhaseChange")) {
						if (!CoreAbility.hasAbility(player, PhaseChange.class)) {
							new PhaseChange(player, PhaseChangeType.FREEZE);
						} else {
							final PhaseChange pc = CoreAbility.getAbility(player, PhaseChange.class);
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
						final EarthArmor armor = CoreAbility.getAbility(player, EarthArmor.class);
						if (armor != null && armor.isFormed()) {
							armor.click();
						}
					} else if (abil.equalsIgnoreCase("Tremorsense")) {
						new Tremorsense(player, true);
					} else if (abil.equalsIgnoreCase("MetalClips")) {
						final MetalClips clips = CoreAbility.getAbility(player, MetalClips.class);
						if (clips == null) {
							new MetalClips(player, 0);
						} else if (clips.getMetalClipsCount() < (player.hasPermission("bending.ability.MetalClips.4clips") ? 4 : 3)) {
							clips.shootMetal();
						} else if (clips.getMetalClipsCount() == 4 && clips.isCanUse4Clips()) {
							clips.crush();
						}
					} else if (abil.equalsIgnoreCase("LavaSurge")) {
						final LavaSurge surge = CoreAbility.getAbility(player, LavaSurge.class);
						if (surge != null) {
							surge.launch();
						}
					} else if (abil.equalsIgnoreCase("LavaFlow")) {
						new LavaFlow(player, AbilityType.CLICK);
					} else if (abil.equalsIgnoreCase("EarthSmash")) {
						new EarthSmash(player, ClickType.LEFT_CLICK);
					} else if (abil.equalsIgnoreCase("EarthGrab")) {
						new EarthGrab(player, GrabMode.PROJECTING);
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
							final FireManipulation fireManip = CoreAbility.getAbility(player, FireManipulation.class);
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
			} else if (abil.equalsIgnoreCase("Flight")) {
				new FlightMultiAbility(player);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerToggleFlight(final PlayerToggleFlightEvent event) {
		final Player player = event.getPlayer();
		if (CoreAbility.hasAbility(player, Tornado.class) || Bloodbending.isBloodbent(player) || Suffocate.isBreathbent(player) || CoreAbility.hasAbility(player, FireJet.class) || CoreAbility.hasAbility(player, AvatarState.class)) {
			event.setCancelled(player.getGameMode() != GameMode.CREATIVE);
			return;
		}

		if (FlightMultiAbility.getFlyingPlayers().contains(player.getUniqueId())) {
			if (player.isFlying()) {
				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerToggleGlide(final EntityToggleGlideEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}
		final Player player = (Player) event.getEntity();

		if (FlightMultiAbility.getFlyingPlayers().contains(player.getUniqueId())) {
			if (player.isGliding()) {
				event.setCancelled(true);
				return;
			}
		}
		if (ConfigManager.getConfig().getBoolean("Abilities.Fire.FireJet.ShowGliding")) {
			if (CoreAbility.getAbility(player, FireJet.class) != null) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onProjectileHit(final ProjectileHitEvent event) {
		final Integer id = event.getEntity().getEntityId();
		final Smokescreen smokescreen = Smokescreen.getSnowballs().get(id);
		if (smokescreen != null) {
			final Location loc = event.getEntity().getLocation();
			Smokescreen.playEffect(loc);
			for (final Entity en : GeneralMethods.getEntitiesAroundPoint(loc, smokescreen.getRadius())) {
				smokescreen.applyBlindness(en);
			}

			Smokescreen.getSnowballs().remove(id);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPickupItem(final EntityPickupItemEvent event) {
		for (final MetalClips metalClips : CoreAbility.getAbilities(MetalClips.class)) {
			if (metalClips.getTrackedIngots().contains(event.getItem())) {
				event.setCancelled(true);
			}
		}

		if (event.isCancelled()) {
			return;
		}

		if (event.getEntity() instanceof LivingEntity) {
			LivingEntity lent = (LivingEntity) event.getEntity();

			if (TempArmor.hasTempArmor(lent)) {
				TempArmor armor = TempArmor.getVisibleTempArmor(lent);
				ItemStack is = event.getItem().getItemStack();
				int index = GeneralMethods.getArmorIndex(is.getType());

				if (index == -1) {
					return;
				}

				event.setCancelled(true);
				ItemStack prev = armor.getOldArmor()[index];

				if (GeneralMethods.compareArmor(is.getType(), prev.getType()) > 0) {
					event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), prev);
					armor.getOldArmor()[index] = is;
					event.getItem().remove();
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onInventoryPickupItem(final InventoryPickupItemEvent event) {
		for (final MetalClips metalClips : CoreAbility.getAbilities(MetalClips.class)) {
			if (metalClips.getTrackedIngots().contains(event.getItem())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onItemMerge(final ItemMergeEvent event) {
		for (final MetalClips metalClips : CoreAbility.getAbilities(MetalClips.class)) {
			if (metalClips.getTrackedIngots().contains(event.getEntity()) || metalClips.getTrackedIngots().contains(event.getTarget())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockPistonExtendEvent(final BlockPistonExtendEvent event) {
		for (final Block b : event.getBlocks()) {
			if (TempBlock.isTempBlock(b)) {
				event.setCancelled(true);
				break;
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockPistonRetractEvent(final BlockPistonRetractEvent event) {
		for (final Block b : event.getBlocks()) {
			if (TempBlock.isTempBlock(b)) {
				event.setCancelled(true);
				break;
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBendingElementChange(final PlayerChangeElementEvent event) {
		final Player player = event.getTarget();
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) return;

		if (event.getResult() == PlayerChangeElementEvent.Result.CHOOSE || event.getResult() == PlayerChangeElementEvent.Result.REMOVE || event.getResult() == PlayerChangeElementEvent.Result.PERMAREMOVE) {
			BendingBoardManager.updateAllSlots(player);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBendingSubElementChange(final PlayerChangeSubElementEvent event) {
		final Player player = event.getTarget();
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) return;

		if (event.getResult() == PlayerChangeSubElementEvent.Result.CHOOSE || event.getResult() == PlayerChangeSubElementEvent.Result.REMOVE || event.getResult() == PlayerChangeSubElementEvent.Result.PERMAREMOVE) {
			BendingBoardManager.updateAllSlots(player);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBindChange(final PlayerBindChangeEvent event) {
		final Player player = event.getPlayer();
		if (player == null) return;
		BendingBoardManager.updateBoard(player, event.getAbility(), false, event.getSlot());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onCooldownChange(final PlayerCooldownChangeEvent event) {
		final Player player = event.getPlayer();
		if (player == null) return;
		BendingBoardManager.updateBoard(player, event.getAbility(), event.getResult().equals(PlayerCooldownChangeEvent.Result.ADDED), 0);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerStanceChange(final PlayerStanceChangeEvent event) {
		final Player player = event.getPlayer();
		if (player == null) return;
		if (!event.getOldStance().isEmpty()) {
			BendingBoardManager.updateBoard(player, event.getOldStance(), false, 0);
		}
		if (!event.getNewStance().isEmpty()) {
			BendingBoardManager.updateBoard(player, event.getNewStance(), false, 0);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBendingPlayerCreation(final BendingPlayerCreationEvent event) {
		final Player player = event.getBendingPlayer().getPlayer();
		BendingBoardManager.canUseScoreboard(player);
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
