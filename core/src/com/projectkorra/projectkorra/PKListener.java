package com.projectkorra.projectkorra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.BlueFireAbility;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.FlightAbility;
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
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.attribute.AttributeCache;
import com.projectkorra.projectkorra.attribute.AttributeModification;
import com.projectkorra.projectkorra.attribute.AttributeModifier;
import com.projectkorra.projectkorra.attribute.markers.DayNightFactor;
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
import com.projectkorra.projectkorra.earthbending.Ripple;
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
import com.projectkorra.projectkorra.event.AbilityRecalculateAttributeEvent;
import com.projectkorra.projectkorra.event.EntityBendingDeathEvent;
import com.projectkorra.projectkorra.event.HorizontalVelocityChangeEvent;
import com.projectkorra.projectkorra.event.PlayerBindChangeEvent;
import com.projectkorra.projectkorra.event.PlayerChangeElementEvent;
import com.projectkorra.projectkorra.event.PlayerChangeSubElementEvent;
import com.projectkorra.projectkorra.event.PlayerJumpEvent;
import com.projectkorra.projectkorra.event.PlayerStanceChangeEvent;
import com.projectkorra.projectkorra.event.PlayerSwingEvent;
import com.projectkorra.projectkorra.event.WorldTimeEvent;
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
import com.projectkorra.projectkorra.firebending.combustion.Combustion;
import com.projectkorra.projectkorra.firebending.lightning.Lightning;
import com.projectkorra.projectkorra.firebending.passive.FirePassive;
import com.projectkorra.projectkorra.firebending.util.FireDamageTimer;
import com.projectkorra.projectkorra.region.RegionProtection;
import com.projectkorra.projectkorra.object.HorizontalVelocityTracker;
import com.projectkorra.projectkorra.object.Preset;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ChatUtil;
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
import com.projectkorra.projectkorra.util.TempFallingBlock;
import com.projectkorra.projectkorra.waterbending.OctopusForm;
import com.projectkorra.projectkorra.waterbending.SurgeWall;
import com.projectkorra.projectkorra.waterbending.SurgeWave;
import com.projectkorra.projectkorra.waterbending.Torrent;
import com.projectkorra.projectkorra.waterbending.TorrentWave;
import com.projectkorra.projectkorra.waterbending.WaterBubble;
import com.projectkorra.projectkorra.waterbending.WaterManipulation;
import com.projectkorra.projectkorra.waterbending.WaterSpout;
import com.projectkorra.projectkorra.waterbending.WaterSpoutWave;
import com.projectkorra.projectkorra.waterbending.blood.Bloodbending;
import com.projectkorra.projectkorra.waterbending.combo.IceBullet;
import com.projectkorra.projectkorra.waterbending.healing.HealingWaters;
import com.projectkorra.projectkorra.waterbending.ice.IceBlast;
import com.projectkorra.projectkorra.waterbending.ice.IceSpikeBlast;
import com.projectkorra.projectkorra.waterbending.ice.IceSpikePillar;
import com.projectkorra.projectkorra.waterbending.ice.IceSpikePillarField;
import com.projectkorra.projectkorra.waterbending.ice.PhaseChange;
import com.projectkorra.projectkorra.waterbending.ice.PhaseChange.PhaseChangeType;
import com.projectkorra.projectkorra.waterbending.multiabilities.WaterArms;
import com.projectkorra.projectkorra.waterbending.passive.FastSwim;
import com.projectkorra.projectkorra.waterbending.passive.HydroSink;

import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
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
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityEvent;
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
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import static com.projectkorra.projectkorra.ProjectKorra.plugin;

public class PKListener implements Listener {
	private static final HashMap<Entity, Ability> BENDING_ENTITY_DEATH = new HashMap<>(); // Entities killed by Bending.
	private static final HashMap<Player, Pair<String, Player>> BENDING_PLAYER_DEATH = new HashMap<>(); // Player killed by Bending. Stores the victim (k), and a pair of the ability and killer (v)
	private static final Set<UUID> RIGHT_CLICK_INTERACT = new HashSet<>(); // Player right click block.
	private static final Set<Player> PLAYER_DROPPED_ITEM = new HashSet<>(); // Player dropped an item.
	private static final Map<Player, Integer> JUMPS = new HashMap<>();

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockBreak(final BlockBreakEvent event) {
		if (BendingPlayer.isWorldDisabled(event.getBlock().getWorld())) {
			return;
		}

		final Block block = event.getBlock();
		final Player player = event.getPlayer();
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) return;
		final String abil = bPlayer.getBoundAbilityName();
		CoreAbility ability;

		if (bPlayer.isElementToggled(Element.EARTH) && bPlayer.isPassiveToggled(Element.EARTH)) {
			Tremorsense tremorsense = CoreAbility.getAbility(player, Tremorsense.class);
			if (tremorsense != null) {
				if (block.equals(tremorsense.getBlock())) {
					if (!tremorsense.canBreak()) {
						tremorsense.setUpBreaking();
						event.setCancelled(true);
					}
				}
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

			if (ability instanceof WaterAbility waterAbility && !waterAbility.allowBreakPlants() && WaterAbility.isPlantbendable(player, block.getType(), false)) {
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
		if (BendingPlayer.isWorldDisabled(fromblock.getWorld())) {
			return;
		} else if (TempBlock.isTempBlock(fromblock) || TempBlock.isTempBlock(toblock)) {
			event.setCancelled(true);
			return;
		}

		if (ElementalAbility.isLava(fromblock)) {
			event.setCancelled(!EarthPassive.canFlowFromTo(fromblock, toblock));
		} else if (ElementalAbility.isWater(fromblock)) {
			event.setCancelled(WaterBubble.isAir(toblock) || !WaterManipulation.canFlowFromTo(fromblock, toblock));
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onFluidLevelChange(final FluidLevelChangeEvent event) {
		Block block = event.getBlock();
		if (!BendingPlayer.isWorldDisabled(block.getWorld()) && (TempBlock.isTempBlock(block) || TempBlock.isTouchingTempBlock(block))) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockForm(final BlockFormEvent event) {
		Block block = event.getBlock();
		if (BendingPlayer.isWorldDisabled(block.getWorld())) {
			return;
		} else if (TempBlock.isTempBlock(block) || !WaterManipulation.canPhysicsChange(block) || !EarthPassive.canPhysicsChange(block)) {
			event.setCancelled(true);
			return;
		}

		if (block.getType().name().endsWith("_CONCRETE_POWDER")) {
			boolean marked = true;
			for (final BlockFace face : GeneralMethods.ADJACENT_FACES) {
				final Block relative = block.getRelative(face);
				if (relative.getType() == Material.WATER && !TempBlock.isTempBlock(relative)) {
					marked = false; // if there is any normal water around it, prevent it.
					break;
				}
			}

			if (marked) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockMeltEvent(final BlockFadeEvent event) {
		final Block block = event.getBlock();
		if (block.getType() != Material.FIRE) {
			event.setCancelled(!WaterManipulation.canPhysicsChange(block)
				|| !EarthPassive.canPhysicsChange(block)
				|| PhaseChange.getFrozenBlocksAsBlock().contains(block)
				|| !SurgeWave.canThaw(block)
				|| !Torrent.canThaw(block));
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockPhysics(final BlockPhysicsEvent event) {
		final Block block = event.getBlock();
		if (!WaterManipulation.canPhysicsChange(block) || !EarthPassive.canPhysicsChange(block) || EarthAbility.getPreventPhysicsBlocks().contains(block)) {
			event.setCancelled(true);
			return;
		}

		// If there is a TempBlock of Air below gravity affected blocks, prevent it from updating.
		Material type = block.getType();
		if (!(type == Material.SAND || type == Material.RED_SAND || type == Material.GRAVEL || type == Material.ANVIL || type == Material.DRAGON_EGG)) {
			return;
		}

		Block below = block.getRelative(BlockFace.DOWN);
		if (ElementalAbility.isAir(below.getType()) && TempBlock.isTempBlock(below)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockPlace(final BlockPlaceEvent event) {
		final Player player = event.getPlayer();
		if (MovementHandler.isStopped(player) || Bloodbending.isBloodbent(player) || Suffocate.isBreathbent(player)) {
			event.setCancelled(true);
			return;
		}

		//Stop combos from triggering from placing blocks.
		//The block place method triggers AFTER interactions, so we have to remove
		//triggers that have already been added.
		ComboManager.removeRecentType(player, ClickType.RIGHT_CLICK_BLOCK);

		//When a player places a block that isn't fire, remove the temp block that was there
		Block block = event.getBlock();
		Material heldType = event.getItemInHand().getType();
		if (heldType != Material.FLINT_AND_STEEL && heldType != Material.FIRE_CHARGE) {
			TempBlock.removeBlock(block);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onElementChange(final PlayerChangeElementEvent event) {
		Player player = event.getTarget().getPlayer();
        if (player == null) {
            return;
        }

        if (ConfigManager.languageConfig.get().getBoolean("Chat.Enable")) {
			// TODO: Abstract this as I've definitely seen something like this elsewhere and also the logic is different elsewhere (specifically for avatar that I noticed)
			final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
			final Element element = event.getElement();
            String prefix;

            if (bPlayer != null && bPlayer.getElements().size() > 1) {
                prefix = Element.AVATAR.getPrefix();
            } else if (element != null) {
                prefix = element.getPrefix();
            } else {
                prefix = ChatColor.WHITE + ChatUtil.color(ConfigManager.languageConfig.get().getString("Chat.Prefixes.Nonbender")) + " ";
            }
            player.setDisplayName(prefix + ChatColor.RESET + player.getName());
        }

		BendingBoardManager.updateAllSlots(player);
		PassiveManager.registerPassives(player);
        FirePassive.handle(player);
    }

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityChangeBlockEvent(final EntityChangeBlockEvent event) {
		if (BendingPlayer.isWorldDisabled(event.getBlock().getWorld())) {
			return;
		}

		final Entity entity = event.getEntity();
		if (MovementHandler.isStopped(entity) || Bloodbending.isBloodbent(entity) || Suffocate.isBreathbent(entity)) {
			event.setCancelled(true);
		}

		if (entity instanceof FallingBlock fallingBlock) {
			if (LavaSurge.getAllFallingBlocks().remove(fallingBlock)) {
				event.setCancelled(true);
			}

			TempFallingBlock tempFallingBlock = TempFallingBlock.get(fallingBlock);
			if (tempFallingBlock != null) {
				tempFallingBlock.tryPlace();
				tempFallingBlock.remove();
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityCombust(final EntityCombustEvent event) {
		final Entity entity = event.getEntity();
		if (!(entity instanceof LivingEntity)) {
			return;
		}
		final Block block = entity.getLocation().getBlock();
		final Player source = FireAbility.getSourcePlayers().get(block);
		if (source != null) {
			new FireDamageTimer(entity, source, null, false);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityDamageByBlock(final EntityDamageByBlockEvent event) {
		final Block block = event.getDamager();

		//Fix for MythicLib firing false EntityDamageEvents to test its own stuff
		if (block == null || BendingPlayer.isWorldDisabled(block.getWorld()) || GeneralMethods.isFakeEvent(event)) {
			return;
		}

		TempBlock tempBlock = TempBlock.get(block);
		if (tempBlock != null) {
			if (EarthAbility.isEarthbendable(block.getType(), true, true, true) && GeneralMethods.isSolid(block)) {
				event.setCancelled(true);
			} else if (event.getCause() == DamageCause.LAVA && EarthAbility.isLava(block)) {
				tempBlock.getAbility().ifPresent(ability -> {
					new FireDamageTimer(event.getEntity(), ability.getPlayer(), ability, true);
					event.setCancelled(true);
					FireDamageTimer.dealFlameDamage(event.getEntity(), event.getDamage());
				});
			} else if (!tempBlock.canSuffocate()) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDamageEvent(final EntityDamageEvent event) {
		final Entity entity = event.getEntity();
		double damage = event.getDamage();

		//Fix for MythicLib firing false EntityDamageEvents to test its own stuff
		if (GeneralMethods.isFakeEvent(event)) return;

		if (entity instanceof Player && event.getCause() == DamageCause.ENTITY_ATTACK
				&& event.getDamage(EntityDamageEvent.DamageModifier.BASE) == 0
				&& event.getFinalDamage() == 0) {
			return;
		}

		if (BendingPlayer.isWorldDisabled(entity.getWorld())) {
			return;
		}

		if (event.getCause() == DamageCause.FIRE && FireAbility.getSourcePlayers().containsKey(entity.getLocation().getBlock())) {
			new FireDamageTimer(entity, FireAbility.getSourcePlayers().get(entity.getLocation().getBlock()), null, true);
		}
		
		if (FireDamageTimer.isEnflamed(entity) && event.getCause() == DamageCause.FIRE_TICK) {
			event.setCancelled(true);
			FireDamageTimer.dealFlameDamage(entity, damage);
		}

		if (event.getCause() == DamageCause.SUFFOCATION) {
			Block block = event.getEntity().getLocation().getBlock();
			if (event.getEntity() instanceof LivingEntity living) {
				block = living.getEyeLocation().getBlock();
			}

			TempBlock tempBlock = TempBlock.get(block);
			if (tempBlock != null) {
				if (EarthAbility.isEarthbendable(block.getType(), true, true, true) && GeneralMethods.isSolid(block)) {
					event.setCancelled(true);
				} else if (event.getCause() == DamageCause.LAVA && EarthAbility.isLava(block)) {
					tempBlock.getAbility().ifPresent(ability -> {
						new FireDamageTimer(event.getEntity(), ability.getPlayer(), ability, true);
						event.setCancelled(true);
						FireDamageTimer.dealFlameDamage(event.getEntity(), event.getDamage());
					});
				} else if (!tempBlock.canSuffocate()) {
					event.setCancelled(true);
				}
			}
		}


		if (entity instanceof Player player) {
            final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
			if (bPlayer == null || !bPlayer.canBendInWorld()) {
				return;
			}

			if (CoreAbility.hasAbility(player, EarthGrab.class)) {
				CoreAbility.getAbility(player, EarthGrab.class).remove();
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

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityDamageEventArmorIgnore(final EntityDamageEvent event) {
		if (DamageHandler.ignoreArmor(event.getEntity()) && !GeneralMethods.isFakeEvent(event)) {
			DamageHandler.entityDamageCallback(event);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityDeath(final EntityDeathEvent event) {
		LivingEntity entity = event.getEntity();
		List<ItemStack> drops = event.getDrops();
		if (TempArmor.hasTempArmor(entity)) {
			for (final TempArmor tempArmor : TempArmor.getTempArmorList(entity)) {
				tempArmor.revert(drops, false);
			}

			if (MetalClips.isControlled(entity)) {
				drops.add(new ItemStack(Material.IRON_INGOT, MetalClips.getTargetToAbility().get(entity).getMetalClipsCount()));
			}
		}

		if (BENDING_ENTITY_DEATH.get(entity) instanceof CoreAbility ability && (ability instanceof FireAbility || FireDamageTimer.isEnflamed(entity))) {
			final List<ItemStack> newDrops = new ArrayList<>();
			for (ItemStack drop : drops) {
				newDrops.add(switch(drop.getType()) {
					case BEEF -> new ItemStack(Material.COOKED_BEEF);
					case SALMON -> new ItemStack(Material.COOKED_SALMON);
					case CHICKEN -> new ItemStack(Material.COOKED_CHICKEN);
					case PORKCHOP -> new ItemStack(Material.COOKED_PORKCHOP);
					case MUTTON -> new ItemStack(Material.COOKED_MUTTON);
					case RABBIT -> new ItemStack(Material.COOKED_RABBIT);
					case COD -> new ItemStack(Material.COOKED_COD);
					default -> drop;
				});
			}
			drops.clear();
			drops.addAll(newDrops);
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
		handleMovementRestriction(event);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityInteractEvent(final EntityInteractEvent event) {
		handleMovementRestriction(event);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityProjectileLaunchEvent(final ProjectileLaunchEvent event) {
		handleMovementRestriction(event);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityShootBowEvent(final EntityShootBowEvent event) {
		handleMovementRestriction(event);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntitySlimeSplitEvent(final SlimeSplitEvent event) {
		handleMovementRestriction(event);
	}

	/*@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntitySuffocatedByTempBlocks(final EntityDamageEvent event) {
		if (event.getCause() == DamageCause.SUFFOCATION) {
			if (TempBlock.isTempBlock(event.getEntity().getLocation().add(0, 1, 0).getBlock())) {
				event.setCancelled(true);
			}
		}
	}*/

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityTarget(final EntityTargetEvent event) {
		handleMovementRestriction(event);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityTargetLiving(final EntityTargetLivingEntityEvent event) {
		handleMovementRestriction(event);
	}

	private <E extends EntityEvent & Cancellable> void handleMovementRestriction(E event) {
		final Entity entity = event.getEntity();
		if (!BendingPlayer.isWorldDisabled(entity.getWorld())) {
			event.setCancelled(MovementHandler.isStopped(entity) || Bloodbending.isBloodbent(entity) || Suffocate.isBreathbent(entity));
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityTeleportEvent(final EntityTeleportEvent event) {
		final Entity entity = event.getEntity();
		if (BendingPlayer.isWorldDisabled(entity.getWorld())) {
			return;
		}

		if (entity instanceof LivingEntity livingEntity) {
			if (MetalClips.isControlled(livingEntity)) {
				event.setCancelled(true);
			}
			for (final TempArmor armor : TempArmor.getTempArmorList(livingEntity)) {
				armor.revert();
			}
		}

		if (entity instanceof Player player) {
			EarthArmor armor = CoreAbility.getAbility(player, EarthArmor.class);
			if (armor != null) {
				armor.remove();
			}
		}

		handleMovementRestriction(event);
	}

	@EventHandler
	public void onHorizontalCollision(final HorizontalVelocityChangeEvent event) {
		if (event.getEntity() == event.getInstigator() || !(event.getEntity() instanceof LivingEntity)) {
			return;
		}

		final double minimumDistance = plugin.getConfig().getDouble("Properties.HorizontalCollisionPhysics.WallDamageMinimumDistance");
		final double maxDamage = plugin.getConfig().getDouble("Properties.HorizontalCollisionPhysics.WallDamageCap");
		final double damage = ((event.getDistanceTraveled() - minimumDistance) < 0 ? 0 : event.getDistanceTraveled() - minimumDistance) / (event.getDifference().length());
		if (damage > 0) {
			DamageHandler.damageEntity(event.getEntity(), Math.min(damage, maxDamage), event.getAbility());
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onInventoryClick(final InventoryClickEvent event) {
		for (final MetalClips clips : CoreAbility.getAbilities(MetalClips.class)) {
			if (clips.getTargetEntity() != null && clips.getTargetEntity() == event.getWhoClicked()) {
				event.setCancelled(true);
				return;
			}
		}

		for (int i = 0; i < 4; i++) {
			if (event.getSlot() == 36 + i && TempArmor.hasTempArmor(event.getWhoClicked())) {
				event.setCancelled(true);
				break;
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityBendingDeath(final EntityBendingDeathEvent event) {
		Entity entity = event.getEntity();
		Player attacker = event.getAttacker();
		Ability ability = event.getAbility();
		String abilityName = ability.getName();
		CoreAbility coreAbility = CoreAbility.getAbility(abilityName);
		BENDING_ENTITY_DEATH.put(entity, ability);

		if (entity instanceof Player player) {
			if (ConfigManager.languageConfig.get().getBoolean("DeathMessages.Enabled")) {
				BENDING_PLAYER_DEATH.put(player, Pair.of(ability.getElement().getColor() + abilityName, attacker));
				Bukkit.getScheduler().runTaskLater(plugin, () -> BENDING_PLAYER_DEATH.remove(player), 20L);
			}
			if (attacker != null && ProjectKorra.isStatisticsEnabled()) {
				StatisticsMethods.addStatisticAbility(attacker.getUniqueId(), coreAbility, com.projectkorra.projectkorra.util.Statistic.PLAYER_KILLS, 1);
			}
		}
		if (attacker != null && ProjectKorra.isStatisticsEnabled()) {
			StatisticsMethods.addStatisticAbility(attacker.getUniqueId(), coreAbility, com.projectkorra.projectkorra.util.Statistic.TOTAL_KILLS, 1);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerChat(final AsyncPlayerChatEvent event) {
		final Player player = event.getPlayer();
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		String elementKey = "Nonbender";
		ChatColor color = ChatColor.WHITE;
		if (bPlayer != null) {
			if (player.hasPermission("bending.avatar") || bPlayer.getElements().stream().anyMatch(Element::isAvatarElement)) {
				color = Element.AVATAR.getColor();
				elementKey = Element.AVATAR.getName();
			} else if (!bPlayer.getElements().isEmpty()) {
				color = bPlayer.getElements().getFirst().getColor();
				elementKey = bPlayer.getElements().getFirst().getName();
			}
		}
		final String element = ConfigManager.languageConfig.get().getString("Chat.Prefixes." + elementKey);
		event.setFormat(event.getFormat().replaceAll("(?i)\\{element}", color + element + ChatColor.RESET).replaceAll("(?i)\\{element_?color}", color + ""));

		if (!ConfigManager.languageConfig.get().getBoolean("Chat.Enable") || bPlayer == null) {
			return;
		}

		String format = ConfigManager.languageConfig.get().getString("Chat.Format");
		format = format.replace("<message>", "%2$s");
		format = format.replace("<name>", color + player.getDisplayName() + ChatColor.RESET);
		event.setFormat(format);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerDamage(final EntityDamageEvent event) {
		if (BendingPlayer.isWorldDisabled(event.getEntity().getWorld()) || GeneralMethods.isFakeEvent(event) || !(event.getEntity() instanceof Player player)) {
			return;
		}

		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null || bPlayer.isChiBlocked()) {
			return;
		}

		DamageCause cause = event.getCause();
		String boundAbility = bPlayer.getBoundAbilityName();

		if (FlightMultiAbility.getFlyingPlayers().contains(player.getUniqueId())) {
			final FlightMultiAbility flight = CoreAbility.getAbility(player, FlightMultiAbility.class);
			flight.cancel("taking damage");
		}

		if (cause == DamageCause.FALL) {
			if (bPlayer.hasElement(Element.EARTH)) {
				if (boundAbility.equalsIgnoreCase("Shockwave")) {
					new Shockwave(player, true);
				} else if (boundAbility.equalsIgnoreCase("Catapult")) {
					new EarthPillars(player, true);
				}
			}

			if (bPlayer.hasElement(Element.AIR)) {
				if (boundAbility.equalsIgnoreCase("AirBurst")) {
					new AirBurst(player, true);
				}
			}

			List<CoreAbility> damagePrevention = List.of(
					CoreAbility.getAbility(GracefulDescent.class),
					CoreAbility.getAbility(DensityShift.class),
					CoreAbility.getAbility(HydroSink.class),
					CoreAbility.getAbility(Acrobatics.class)
			);
			for (CoreAbility ability : damagePrevention) {
				if (ability != null && ability.isEnabled() && bPlayer.hasElement(ability.getElement()) && bPlayer.canBendPassive(ability) && bPlayer.canUsePassive(ability) && PassiveManager.hasPassive(player, ability)) {
					boolean cancelled = switch (ability) {
						case DensityShift ignored -> DensityShift.softenLanding(player);
						case HydroSink ignored -> HydroSink.applyNoFall(player);
						case Acrobatics ignored -> {
							final double damage = event.getDamage();
							final double reducedDamage = event.getDamage() * Acrobatics.getFallReductionFactor();
							final double finalDamage = damage - reducedDamage;
							event.setDamage(finalDamage);
							yield finalDamage <= 0.4;
						}
						default -> true;
					};
					if (cancelled) {
						event.setCancelled(true);
						break;
					}
				}
			}

			if (!event.isCancelled()) {
				final Flight flight = Manager.getManager(FlightHandler.class).getInstance(player);
				if (flight != null && flight.getPlayer() == flight.getSource()) {
					event.setCancelled(true);
				}
			}
		}

		CoreAbility heatControl = CoreAbility.getAbility(HeatControl.class);
		if ((cause == DamageCause.FIRE || cause == DamageCause.FIRE_TICK) && heatControl != null && bPlayer.hasElement(Element.FIRE) && bPlayer.canBendPassive(heatControl) && bPlayer.canUsePassive(heatControl)) {
			event.setCancelled(!HeatControl.canBurn(player));
		}

		if (cause == DamageCause.SUFFOCATION && bPlayer.hasElement(Element.EARTH) && TempBlock.isTempBlock(player.getEyeLocation().getBlock())) {
			event.setDamage(0D);
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerDamageFinal(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player player) {
			final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
			final EarthArmor earthArmor = CoreAbility.getAbility(player, EarthArmor.class);
			if (earthArmor != null) {
				earthArmor.updateAbsorbtion();
			}

			//Check if AvatarState will save them from death, or if AvatarState should activate due to low health
			if (AvatarState.activateLowHealth(bPlayer, event.getFinalDamage(), player.getHealth() - event.getFinalDamage() < 0)) {
				event.setDamage(0D);
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerDamageByPlayer(final EntityDamageByEntityEvent event) {
		final Entity source = event.getDamager();
		final Entity entity = event.getEntity();
		if (BendingPlayer.isWorldDisabled(entity.getWorld()) || GeneralMethods.isFakeEvent(event)) {
			return;
		}

		final FireBlastCharged fireball = FireBlastCharged.getFireball(source);
		if (fireball != null) {
			event.setCancelled(true);
			fireball.dealDamage(entity);
			return;
		}

		if (MovementHandler.isStopped(source)) {
			final CoreAbility ability = (CoreAbility) event.getDamager().getMetadata("movement:stop").getFirst().value();
			if (!(ability instanceof EarthGrab)) {
				event.setCancelled(true);
				return;
			}
		}

		if (entity instanceof Player player) {
			Suffocate.remove(player);
		}

		//Stop DamageHandler causing this event to fire infinitely
		if (entity instanceof LivingEntity livingEntity && DamageHandler.isReceivingDamage(livingEntity)) {
			return;
		}
		if (event.getCause() != DamageCause.ENTITY_ATTACK || !(source instanceof Player sourcePlayer)) {
            return;
        }

        final BendingPlayer bPLayer = BendingPlayer.getBendingPlayer(sourcePlayer);
        if (bPLayer == null) {
            return;
        }

		final boolean chi = bPLayer.canCurrentlyBendWithWeapons() && bPLayer.isElementToggled(Element.CHI);
        final CoreAbility ability = bPLayer.getBoundAbility();
        if (chi && ability instanceof ChiAbility && !bPLayer.isOnCooldown(ability) && bPLayer.canBendPassive(ability)) {
			if (ability instanceof Paralyze) {
				new Paralyze(sourcePlayer, entity);
			} else if (ability instanceof QuickStrike) {
				new QuickStrike(sourcePlayer, entity);
				event.setCancelled(true);
			} else if (ability instanceof SwiftKick) {
				new SwiftKick(sourcePlayer, entity);
				event.setCancelled(true);
			} else if (ability instanceof RapidPunch) {
				new RapidPunch(sourcePlayer, entity);
				event.setCancelled(true);
			}
        } else if (chi && ability == null && entity instanceof Player target && ChiPassive.willChiBlock(sourcePlayer, target)) {
			ChiPassive.blockChi(target);
		}

		PlayerSwingEvent swingEvent = new PlayerSwingEvent(sourcePlayer); //Allow addons to handle a swing without
		Bukkit.getPluginManager().callEvent(swingEvent);                  //needing to repeat the checks above themselves
		if (swingEvent.isCancelled()) {
			event.setCancelled(true);
		}
    }

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerDeath(final PlayerDeathEvent event) {
		Player player = event.getEntity();
		if (event.getKeepInventory()) {
			for (final TempArmor armor : TempArmor.getTempArmorList(player)) {
				armor.revert(event.getDrops(), true);
			}
			// Do nothing further. TempArmor drops are handled by the EntityDeath event and not PlayerDeath.
		}

		Pair<String, Player> deathInfo = BENDING_PLAYER_DEATH.remove(player);
		if (deathInfo != null) {
			final String ability = deathInfo.getLeft();
			final Player killer = deathInfo.getRight();
			final String tempAbility = ChatColor.stripColor(ability).replaceAll(" ", "");
			final CoreAbility coreAbil = CoreAbility.getAbility(tempAbility);
			String message = ConfigManager.languageConfig.get().getString("DeathMessages.Default");
			Element element = coreAbil != null ? coreAbil.getElement() : null;
			
			if (HorizontalVelocityTracker.hasBeenDamagedByHorizontalVelocity(player) && Arrays.asList(HorizontalVelocityTracker.abils).contains(tempAbility)) {
				if (ConfigManager.languageConfig.get().contains("Abilities." + element.getName() + "." + tempAbility + ".HorizontalVelocityDeath")) {
					message = ConfigManager.languageConfig.get().getString("Abilities." + element.getName() + "." + tempAbility + ".HorizontalVelocityDeath");
				}
			} else if (element != null) {
				if (element instanceof SubElement subElement) {
					element = subElement.getParentElement();
				}
				if (ConfigManager.languageConfig.get().contains("Abilities." + element.getName() + "." + tempAbility + ".DeathMessage")) {
					message = ConfigManager.languageConfig.get().getString("Abilities." + element.getName() + "." + tempAbility + ".DeathMessage");
				} else if (ConfigManager.languageConfig.get().contains("Abilities." + element.getName() + ".Combo." + tempAbility + ".DeathMessage")) {
					message = ConfigManager.languageConfig.get().getString("Abilities." + element.getName() + ".Combo." + tempAbility + ".DeathMessage");
				}
			}
			// TODO: pretty sure I'm able to remove that entire branch because isAvatarAbility was always false and we have Element.AVATAR which feeds into the branch before it
			message = message.replace("{victim}", event.getEntity().getName()).replace("{attacker}", killer.getName()).replace("{ability}", ability);
			event.setDeathMessage(message);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerItemDrop(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer != null && bPlayer.getBoundAbility() != null) {
			PLAYER_DROPPED_ITEM.add(player);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerInteraction(final PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null || !bPlayer.canBendInWorld()) {
			return;
		}

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			final UUID uuid = player.getUniqueId();
			if (RIGHT_CLICK_INTERACT.add(uuid)) { //Add if it isn't already in there. And if it isn't in there...
				Bukkit.getScheduler().runTaskLater(plugin, () -> RIGHT_CLICK_INTERACT.remove(uuid), 2L);
			}

			if (event.getHand() == EquipmentSlot.HAND && bPlayer.canCurrentlyBendWithWeapons()) {
				ComboManager.addComboAbility(player, event.getClickedBlock() != null ? ClickType.RIGHT_CLICK_BLOCK : ClickType.RIGHT_CLICK);
			}

			if (bPlayer.getBoundAbilityName().equalsIgnoreCase("EarthSmash")) {
				new EarthSmash(player, ClickType.RIGHT_CLICK);
			}
		}

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
			IceBullet iceBullet = CoreAbility.getAbility(player, IceBullet.class);
			if (iceBullet != null) {
				iceBullet.doRightClick();
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
		if (bPlayer == null || !bPlayer.canBendInWorld()) {
			return;
		}

		if (bPlayer.canCurrentlyBendWithWeapons()) {
			ComboManager.addComboAbility(player, ClickType.RIGHT_CLICK_ENTITY);
		}

		Entity rightClicked = event.getRightClicked();
		if (rightClicked.hasMetadata("earthgrab:trap")) {
			final EarthGrab earthGrab = (EarthGrab) rightClicked.getMetadata("earthgrab:trap").getFirst().value();
			earthGrab.damageTrap();
			event.setCancelled(true);
			return;
		} else if (rightClicked.hasMetadata("temparmorstand") || MovementHandler.isStopped(player) || Bloodbending.isBloodbent(player) || Suffocate.isBreathbent(player)) {
			event.setCancelled(true);
			return;
		}

		if (bPlayer.getBoundAbilityName().equalsIgnoreCase("HealingWaters") && event.getHand() == EquipmentSlot.HAND) {
			final HealingWaters instance = CoreAbility.getAbility(player, HealingWaters.class);
			if (instance != null && instance.charged) {
				instance.click();
				event.setCancelled(true);
				return;
			}
		}

		final UUID uuid = player.getUniqueId();
		if (!RIGHT_CLICK_INTERACT.contains(uuid) && rightClicked instanceof Player target) {
			if (FlightMultiAbility.getFlyingPlayers().contains(uuid)) {
				final FlightMultiAbility flight = CoreAbility.getAbility(player, FlightMultiAbility.class);
				flight.requestCarry(target);
				RIGHT_CLICK_INTERACT.add(uuid);
				Bukkit.getScheduler().runTaskLater(plugin, () -> RIGHT_CLICK_INTERACT.remove(uuid), 2L);
			} else if (FlightMultiAbility.getFlyingPlayers().contains(target.getUniqueId())) {
				FlightMultiAbility.acceptCarryRequest(player, target);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerItemDamage(final PlayerItemDamageEvent event) {
		final TempArmor armor = TempArmor.getVisibleTempArmor(event.getPlayer());
        if (armor == null) {
            return;
        }

        final ItemStack damaged = event.getItem();
        for (final ItemStack itemStack : armor.getNewArmor()) {
            if (itemStack != null && damaged.isSimilar(itemStack)) {
                event.setCancelled(true);
                break;
            }
        }
    }

	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		JUMPS.put(player, player.getStatistic(Statistic.JUMP));

		// Load the player's bending data from the database
		BendingPlayer.getOrLoadOfflineAsync(player);

		if (ProjectKorra.isStatisticsEnabled()) {
			Manager.getManager(StatisticsManager.class).load(player.getUniqueId());
		}

		if (ConfigManager.languageConfig.get().getBoolean("Chat.Branding.JoinMessage.Enabled")) {
			Bukkit.getScheduler().runTaskLater(plugin, () -> {
				ChatColor color = ChatColor.of(ConfigManager.languageConfig.get().getString("Chat.Branding.Color").toUpperCase());
				color = color == null ? ChatColor.GOLD : color;
				final String topBorder = ConfigManager.languageConfig.get().getString("Chat.Branding.Borders.TopBorder");
				final String bottomBorder = ConfigManager.languageConfig.get().getString("Chat.Branding.Borders.BottomBorder");
				if (!topBorder.isEmpty()) {
					player.sendMessage(ChatUtil.color(topBorder));
				}
				player.sendMessage(ChatUtil.multiline(color + "This server is running ProjectKorra version " + plugin.getDescription().getVersion() + " for bending! Find out more at http://www.projectkorra.com!"));
				if (!bottomBorder.isEmpty()) {
					player.sendMessage(ChatUtil.color(bottomBorder));
				}
			}, 20 * 4);
		}
	}

	@EventHandler
	public void onPlayerChangeWorld(final PlayerChangedWorldEvent event) {
		PassiveManager.registerPassives(event.getPlayer());
		BendingBoardManager.changeWorld(event.getPlayer());

		//Revert TempArmor when swapping worlds due to some worlds having different inventories (Multiverse Inventories)
		TempArmor armor = TempArmor.getVisibleTempArmor(event.getPlayer());
		if (armor != null) {
			armor.revert();
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerKick(final PlayerKickEvent event) {
		JUMPS.remove(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerMove(final PlayerMoveEvent event) {
		final Player player = event.getPlayer();
		if (BendingPlayer.isWorldDisabled(player.getWorld())) {
			return;
		}

		Location from = event.getFrom();
		Location to = event.getTo();
		if (to == null || from.toVector().equals(to.toVector())) {
			return;
		}

		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (MovementHandler.isStopped(player)) {
			if (to.getX() != from.getX() || to.getZ() != from.getZ() || to.getY() > from.getY()) {
				event.setCancelled(true);
			}
			return;
		}

		if (CoreAbility.hasAbility(player, WaterSpout.class) || CoreAbility.hasAbility(player, AirSpout.class)) {
			Vector velocity = new Vector(to.getX() - from.getX(), 0, to.getZ() - from.getZ());
			final double currentSpeed = velocity.length();
			final double maxSpeed = .2;
			if (currentSpeed > maxSpeed) {
				// apply only if moving set a factor
				velocity = velocity.normalize().multiply(maxSpeed);
				event.getPlayer().setVelocity(velocity);
			}
			return;
		}

		if (Bloodbending.isBloodbent(player)) {
			final BendingPlayer bender = Bloodbending.getBloodbender(player);
			if (bender != null && bender.isAvatarState()) {
				event.setCancelled(true);
				return;
			}

			final Location loc = Bloodbending.getBloodbendingLocation(player);
			if (player.getWorld().equals(loc.getWorld())) {
				Vector bentVelocity = Bloodbending.getBloodbendingVector(player);
				if (bentVelocity != null && !player.getVelocity().equals(bentVelocity)) {
					player.setVelocity(bentVelocity);
				}
			}
			return;
		}

		if (bPlayer != null) {
			if (bPlayer.hasElement(Element.AIR) || bPlayer.hasElement(Element.CHI)) {
				PassiveHandler.checkExhaustionPassives(player);
			}
			if (to.getBlock() != from.getBlock()) {
				FirePassive.handle(player);
			}
		}

		if (to.getY() > from.getY()) {
			Material block = from.getBlock().getType();
			if (block != Material.VINE && block != Material.LADDER) {
				final int current = player.getStatistic(Statistic.JUMP);
				final int last = JUMPS.get(player);

				if (last != current) {
					JUMPS.put(player, current);
					final double yDif = to.getY() - from.getY();
					if ((yDif < 0.035 || yDif > 0.037) && (yDif < 0.116 || yDif > 0.118)) {
						Bukkit.getServer().getPluginManager().callEvent(new PlayerJumpEvent(player, yDif));
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerGameModeChange(final PlayerGameModeChangeEvent event) {
		final Player player = event.getPlayer();
		if (event.getNewGameMode() == GameMode.SPECTATOR) {
			Commands.invincible.add(player.getName());
		} else if (event.getNewGameMode() != GameMode.SPECTATOR) {
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

		Commands.invincible.remove(player.getName());

		Preset.unloadPreset(player);

		for (final TempArmor armor : TempArmor.getTempArmorList(player)) {
			armor.revert();
		}

		if (MetalClips.isControlled(player)) {
			MetalClips.removeControlledEnitity(player);
		}

		MultiAbilityManager.remove(player);
		JUMPS.remove(player);

		for (final CoreAbility ability : CoreAbility.getAbilities()) {
			CoreAbility instance = CoreAbility.getAbility(player, ability.getClass());
			if (instance != null) {
				instance.remove();
			}
		}

		if (bPlayer == null) {
			return;
		}

		Bukkit.getScheduler().runTaskLater(plugin, //Run 1 tick later so they actually are offline
				() -> {
					if (ProjectKorra.isDatabaseCooldownsEnabled()) {
						bPlayer.saveCooldowns();
					}
					OfflineBendingPlayer converted = OfflineBendingPlayer.convertToOffline(bPlayer);
					if (!converted.isOnline()) { //We test if they are still offline. If they relog by joining on a different client, they will be online now, and an error will be thrown otherwise.
						converted.uncacheAfter(ConfigManager.defaultConfig.get().getLong("Properties.PlayerDataUnloadTime", 5 * 60 * 1000));
					}
				}, 1L);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerSneak(final PlayerToggleSneakEvent event) {
		final Player player = event.getPlayer();
		if (BendingPlayer.isWorldDisabled(player.getWorld())) {
			return;
		}

		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return;
		}

		final boolean sneaking = player.isSneaking();
		if (bPlayer.canCurrentlyBendWithWeapons()) {
			ComboManager.addComboAbility(player, sneaking ? ClickType.SHIFT_UP : ClickType.SHIFT_DOWN);
		}

		final String abilityName = bPlayer.getBoundAbilityName();
		if (!sneaking && Suffocate.isBreathbent(player)) {
			if (!(abilityName.equalsIgnoreCase("AirSwipe") || abilityName.equalsIgnoreCase("FireBlast")
					|| abilityName.equalsIgnoreCase("EarthBlast") || abilityName.equalsIgnoreCase("WaterManipulation"))) {
				event.setCancelled(true);
				return;
			}
		}

		if (!sneaking && (MovementHandler.isStopped(player) || Bloodbending.isBloodbent(player))) {
			event.setCancelled(true);
			return;
		} else if (bPlayer.isChiBlocked()) {
			event.setCancelled(true);
			return;
		}

		if (!sneaking) {
			BlockSource.update(player, ClickType.SHIFT_DOWN);
		}

		final CoreAbility ability = bPlayer.getBoundAbility();
		if (ability == null || !ability.isSneakAbility()) {
			if (PassiveManager.hasPassive(player, CoreAbility.getAbility(FerroControl.class))) {
				new FerroControl(player);
			}
			if (PassiveManager.hasPassive(player, CoreAbility.getAbility(FastSwim.class))) {
				new FastSwim(player);
			}
		}

		if (ability == null || ability instanceof AddonAbility || sneaking || !bPlayer.canBendIgnoreCooldowns(ability)
				|| !bPlayer.canCurrentlyBendWithWeapons() || !bPlayer.isElementToggled(ability.getElement())) {
			return;
		}

		switch(ability) {
			case Tornado ignored -> new Tornado(player);
			case AirBlast ignored -> AirBlast.setOrigin(player);
			case AirBurst ignored -> new AirBurst(player, false);
			case AirSuction ignored -> new AirSuction(player);
			case AirSwipe ignored -> new AirSwipe(player, true);
			case AirShield ignored -> new AirShield(player);
			case Suffocate ignored -> new Suffocate(player);
			case Bloodbending ignored -> new Bloodbending(player);
			case IceBlast ignored -> new IceBlast(player);
			case IceSpikeBlast ignored -> new IceSpikeBlast(player);
			case IceSpikePillar ignored -> new IceSpikeBlast(player);
			case IceSpikePillarField ignored -> new IceSpikeBlast(player);
			case OctopusForm ignored -> OctopusForm.form(player);
			case PhaseChange ignored -> {
				final PhaseChange phaseChange = CoreAbility.getAbility(player, PhaseChange.class);
				if (phaseChange == null) {
					new PhaseChange(player, PhaseChangeType.MELT);
				} else {
					phaseChange.startNewType(PhaseChangeType.MELT);
				}
			}
			case WaterManipulation ignored -> new WaterManipulation(player);
			case WaterBubble ignored -> new WaterBubble(player, true);
			case SurgeWall ignored -> SurgeWall.form(player);
			case SurgeWave ignored -> SurgeWall.form(player);
			case Torrent ignored -> Torrent.create(player);
			case TorrentWave ignored -> Torrent.create(player);
			case WaterArms ignored -> new WaterArms(player);
			case HealingWaters ignored -> new HealingWaters(player);
			case Catapult ignored -> new Catapult(player, true);
			case EarthBlast ignored -> new EarthBlast(player);
			case EarthArmor ignored -> new EarthArmor(player);
			case RaiseEarth ignored -> new RaiseEarthWall(player);
			case RaiseEarthWall ignored -> new RaiseEarthWall(player);
			case Collapse ignored -> new CollapseWall(player);
			case CollapseWall ignored -> new CollapseWall(player);
			case Shockwave ignored -> new Shockwave(player, false);
			case EarthTunnel ignored -> new EarthTunnel(player);
			case Tremorsense ignored -> {
				bPlayer.toggleTremorSense();
				ChatUtil.displayMovePreview(player);
				BendingBoardManager.updateAllSlots(player);
			}
			case Extraction ignored -> new Extraction(player);
			case LavaFlow ignored -> new LavaFlow(player, LavaFlow.AbilityType.SHIFT);
			case EarthSmash ignored -> new EarthSmash(player, ClickType.SHIFT_DOWN);
			case MetalClips ignored -> {
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
			}
			case EarthGrab ignored -> new EarthGrab(player, GrabMode.DRAG);
			case Blaze ignored -> new BlazeRing(player);
			case BlazeArc ignored -> new BlazeRing(player);
			case BlazeRing ignored -> new BlazeRing(player);
			case FireBlast ignored -> new FireBlastCharged(player);
			case FireBlastCharged ignored -> new FireBlastCharged(player);
			case HeatControl ignored -> new HeatControl(player, HeatControlType.COOK);
			case FireBurst ignored -> new FireBurst(player);
			case FireShield ignored -> new FireShield(player, true);
			case Lightning ignored -> new Lightning(player);
			case Combustion ignored -> new Combustion(player);
			case FireManipulation ignored -> new FireManipulation(player, FireManipulationType.SHIFT);
			default -> {}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerSlotChange(final PlayerItemHeldEvent event) {
		final Player player = event.getPlayer();
		if (BendingPlayer.isWorldDisabled(player.getWorld())) {
			return;
		} else if (!MultiAbilityManager.canChangeSlot(player, event.getNewSlot())) {
			event.setCancelled(true);
			return;
		}

		final int slot = event.getNewSlot() + 1;
		ChatUtil.displayMovePreview(player, slot);
		BendingBoardManager.changeActiveSlot(player, slot);

		if (ConfigManager.defaultConfig.get().getBoolean("Abilities.Water.WaterArms.DisplayBoundMsg")) {
			final WaterArms waterArms = CoreAbility.getAbility(player, WaterArms.class);
			if (waterArms != null) {
				waterArms.displayBoundMsg(slot);
				return;
			}
		}

		Bukkit.getScheduler().runTaskLater(plugin, () -> Illumination.slotChange(player), 1L);
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
		if ((main == null || main.getType().isAir()) && (off == null || off.getType().isAir())) {
			ComboManager.addComboAbility(player, ClickType.OFFHAND_TRIGGER);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(final PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		if (BendingPlayer.isWorldDisabled(player.getWorld()) || PLAYER_DROPPED_ITEM.remove(player) || event.getHand() != EquipmentSlot.HAND
				|| (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_AIR)
				|| (event.getAction() == Action.LEFT_CLICK_BLOCK && event.isCancelled())
				|| RIGHT_CLICK_INTERACT.contains(player.getUniqueId())) {
			return;
		}

		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return;
		}

		final Entity target = GeneralMethods.getTargetedEntity(player, 3);
		if (bPlayer.canCurrentlyBendWithWeapons()) {
			ComboManager.addComboAbility(player, target instanceof LivingEntity && player != target ? ClickType.LEFT_CLICK_ENTITY : ClickType.LEFT_CLICK);
		}

		if (Suffocate.isBreathbent(player)) {
			event.setCancelled(true);
			return;
		} else if (Bloodbending.isBloodbent(player) && !bPlayer.getBoundAbilityName().equalsIgnoreCase("AvatarState")) {
			event.setCancelled(true);
			return;
		} else if (MovementHandler.isStopped(player) && player.hasMetadata("movement:stop")) {
			final CoreAbility abil = (CoreAbility) player.getMetadata("movement:stop").getFirst().value();
			if (!(abil instanceof EarthGrab)) {
				event.setCancelled(true);
				return;
			}
		} else if (bPlayer.isChiBlocked()) {
			event.setCancelled(true);
			return;
		}

		BlockSource.update(player, ClickType.LEFT_CLICK);

		PlayerSwingEvent swingEvent = new PlayerSwingEvent(event.getPlayer()); //Allow addons to handle a swing without
		Bukkit.getPluginManager().callEvent(swingEvent);                       //needing to repeat the checks above themselves
		if (swingEvent.isCancelled()) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerSwingEvent event) {
		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null || !bPlayer.canBendInWorld()) {
			return;
		}

		final CoreAbility ability = bPlayer.getBoundAbility();
		if (ability == null) {
			if (MultiAbilityManager.hasMultiAbilityBound(player)) {
				final String multiAbility = MultiAbilityManager.getBoundMultiAbility(player);
				if (multiAbility.equalsIgnoreCase("WaterArms")) {
					new WaterArms(player);
				} else if (multiAbility.equalsIgnoreCase("Flight")) {
					new FlightMultiAbility(player);
				}
			}
			return;
		} else if (ability instanceof AddonAbility || !bPlayer.canBendIgnoreCooldowns(ability) || !bPlayer.canCurrentlyBendWithWeapons() || !bPlayer.isElementToggled(ability.getElement())) {
			return;
		}

		switch(ability) {
			case AirBlast ignored -> new AirBlast(player);
			case AirSuction ignored -> AirSuction.shoot(player);
			case AirBurst ignored -> AirBurst.coneBurst(player);
			case AirScooter ignored -> new AirScooter(player);
			case AirSpout ignored -> new AirSpout(player);
			case AirSwipe ignored -> new AirSwipe(player);
			case FlightAbility ignored -> new FlightMultiAbility(player);
			case Bloodbending ignored -> Bloodbending.launch(player);
			case IceBlast ignored -> IceBlast.activate(player);
			case IceSpikeBlast ignored -> IceSpikeBlast.activate(player);
			case IceSpikePillar ignored -> IceSpikeBlast.activate(player);
			case IceSpikePillarField ignored -> IceSpikeBlast.activate(player);
			case OctopusForm ignored -> new OctopusForm(player);
			case PhaseChange ignored -> {
				if (!CoreAbility.hasAbility(player, PhaseChange.class)) {
					new PhaseChange(player, PhaseChangeType.FREEZE);
				} else {
					final PhaseChange pc = CoreAbility.getAbility(player, PhaseChange.class);
					pc.startNewType(PhaseChangeType.FREEZE);
				}
			}
			case WaterBubble ignored -> new WaterBubble(player, false);
			case WaterSpout ignored -> new WaterSpout(player);
			case WaterSpoutWave ignored -> new WaterSpout(player);
			case WaterManipulation ignored -> WaterManipulation.moveWater(player);
			case SurgeWall ignored -> new SurgeWall(player);
			case SurgeWave ignored -> new SurgeWall(player);
			case Torrent ignored -> new Torrent(player);
			case TorrentWave ignored -> new Torrent(player);
			case Catapult ignored -> new Catapult(player, false);
			case EarthBlast ignored -> EarthBlast.throwEarth(player);
			case RaiseEarth ignored -> new RaiseEarth(player);
			case RaiseEarthWall ignored -> new RaiseEarth(player);
			case Collapse ignored -> new Collapse(player);
			case CollapseWall ignored -> new Collapse(player);
			case Shockwave ignored -> Shockwave.coneShockwave(player);
			case Ripple ignored -> Shockwave.coneShockwave(player);
			case EarthArmor ignored -> {
				final EarthArmor armor = CoreAbility.getAbility(player, EarthArmor.class);
				if (armor != null && armor.isFormed()) {
					armor.click();
				}
			}
			case Tremorsense ignored -> new Tremorsense(player, true);
			case MetalClips ignored -> {
				final MetalClips clips = CoreAbility.getAbility(player, MetalClips.class);
				if (clips == null) {
					new MetalClips(player, 0);
				} else if (clips.getMetalClipsCount() < (player.hasPermission("bending.ability.MetalClips.4clips") ? 4 : 3)) {
					clips.shootMetal();
				} else if (clips.getMetalClipsCount() == 4 && clips.isCanUse4Clips()) {
					clips.crush();
				}
			}
			case LavaSurge ignored -> {
				final LavaSurge surge = CoreAbility.getAbility(player, LavaSurge.class);
				if (surge != null) {
					surge.launch();
				}
			}
			case LavaFlow ignored -> new LavaFlow(player, AbilityType.CLICK);
			case EarthSmash ignored -> new EarthSmash(player, ClickType.LEFT_CLICK);
			case EarthGrab ignored -> new EarthGrab(player, GrabMode.PROJECTING);
			case Blaze ignored -> new Blaze(player);
			case BlazeArc ignored -> new Blaze(player);
			case BlazeRing ignored -> new Blaze(player);
			case FireBlast ignored -> new FireBlast(player);
			case FireBlastCharged ignored -> new FireBlast(player);
			case FireJet ignored -> new FireJet(player);
			case HeatControl ignored -> new HeatControl(player, HeatControlType.MELT);
			case Illumination ignored -> {
				if (ConfigManager.defaultConfig.get().getBoolean("Abilities.Fire.Illumination.Passive")) {
					bPlayer.toggleIllumination();
					ChatUtil.displayMovePreview(player);
					BendingBoardManager.updateAllSlots(player);
				} else {
					new Illumination(player);
				}
			}
			case FireBurst ignored -> FireBurst.coneBurst(player);
			case FireShield ignored -> new FireShield(player);
			case WallOfFire ignored -> new WallOfFire(player);
			case Combustion ignored -> Combustion.explode(player);
			case FireManipulation ignored -> {
				if (CoreAbility.hasAbility(player, FireManipulation.class)) {
					final FireManipulation fireManip = CoreAbility.getAbility(player, FireManipulation.class);
					if (fireManip.getFireManipulationType() == FireManipulationType.CLICK) {
						fireManip.click();
					}
				} else {
					new FireManipulation(player, FireManipulationType.CLICK);
				}
			}
			case HighJump ignored -> new HighJump(player);
			case Smokescreen ignored -> new Smokescreen(player);
			case WarriorStance ignored -> new WarriorStance(player);
			case AcrobatStance ignored -> new AcrobatStance(player);
			case AvatarState ignored -> {
				new AvatarState(player);
				ChatUtil.displayMovePreview(player);
				BendingBoardManager.updateAllSlots(player);
			}
			default -> {}
		}

		if (ability instanceof WaterAbility) {
			IceBullet iceBullet = CoreAbility.getAbility(player, IceBullet.class);
			if (iceBullet != null) {
				iceBullet.doLeftClick();
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerToggleFlight(final PlayerToggleFlightEvent event) {
		final Player player = event.getPlayer();
		if (BendingPlayer.isWorldDisabled(player.getWorld())) {
			return;
		} else if (CoreAbility.hasAbility(player, Tornado.class) || Bloodbending.isBloodbent(player) || Suffocate.isBreathbent(player)
				|| CoreAbility.hasAbility(player, FireJet.class) || CoreAbility.hasAbility(player, AvatarState.class)) {
			event.setCancelled(player.getGameMode() != GameMode.CREATIVE);
			return;
		}

		if (FlightMultiAbility.getFlyingPlayers().contains(player.getUniqueId()) && player.isFlying()) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerToggleGlide(final EntityToggleGlideEvent event) {
		if (!(event.getEntity() instanceof Player player) || BendingPlayer.isWorldDisabled(player.getWorld())) {
			return;
		}

		if (FlightMultiAbility.getFlyingPlayers().contains(player.getUniqueId()) && player.isGliding()) {
			event.setCancelled(true);
		} else if (ConfigManager.getConfig().getBoolean("Abilities.Fire.FireJet.ShowGliding") && CoreAbility.getAbility(player, FireJet.class) != null) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onProjectileHit(final ProjectileHitEvent event) {
		final Projectile projectile = event.getEntity();
		final Integer id = projectile.getEntityId();
		final Smokescreen smokescreen = Smokescreen.getSnowballs().remove(id);
		if (smokescreen != null) {
			final Location location = projectile.getLocation();
			Smokescreen.playEffect(location);
			for (final Entity hit : GeneralMethods.getEntitiesAroundPoint(location, smokescreen.getRadius())) {
				smokescreen.applyBlindness(hit);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPickupItem(final EntityPickupItemEvent event) {
		LivingEntity entity = event.getEntity();
		if (BendingPlayer.isWorldDisabled(entity.getWorld())) {
			return;
		}
		Item item = event.getItem();
		for (final MetalClips metalClips : CoreAbility.getAbilities(MetalClips.class)) {
			if (metalClips.getTrackedIngots().contains(item)) {
				event.setCancelled(true);
				return;
			}
		}

		TempArmor armor = TempArmor.getVisibleTempArmor(entity);
		ItemStack itemStack = item.getItemStack();
		int index = GeneralMethods.getArmorIndex(itemStack.getType());
		if (armor == null || index == -1) {
			return;
		}

		ItemStack prev = armor.getOldArmor()[index];
		if (GeneralMethods.compareArmor(itemStack.getType(), prev.getType()) > 0) {
			entity.getWorld().dropItemNaturally(entity.getLocation(), prev);
			armor.getOldArmor()[index] = itemStack;
			item.remove();
		}
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onInventoryPickupItem(final InventoryPickupItemEvent event) {
		Item item = event.getItem();
		if (!BendingPlayer.isWorldDisabled(item.getWorld())) {
			return;
		}
		for (final MetalClips metalClips : CoreAbility.getAbilities(MetalClips.class)) {
			if (metalClips.getTrackedIngots().contains(item)) {
				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler
	public void onTimeChange(WorldTimeEvent event) {
		for (CoreAbility ability : CoreAbility.getAbilitiesByInstances()) {
			if (ability instanceof WaterAbility || ability instanceof FireAbility) {
				ability.recalculateAttributes();
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onAttributeRecalc(AbilityRecalculateAttributeEvent event) {
		CoreAbility ability = event.getAbility();
		Player player = ability.getPlayer();
		Location location = ability.getLocation();
		if (event.hasMarker(DayNightFactor.class) && player != null && location != null) {
			boolean day = FireAbility.isDay(location.getWorld());
			boolean night = WaterAbility.isNight(location.getWorld());
			if (ability instanceof WaterAbility && night && player.hasPermission("bending.water.nightfactor")) {
				DayNightFactor dayNightFactor = event.getMarker(DayNightFactor.class);
				double factor = dayNightFactor.factor() != -1 ? dayNightFactor.factor() : WaterAbility.getNightFactor();
				//If the factor isn't the default, use the one in the annotation

				AttributeModifier modifier = dayNightFactor.invert() ? AttributeModifier.DIVISION : AttributeModifier.MULTIPLICATION;
				AttributeModification mod = AttributeModification.of(modifier, factor, AttributeModification.NIGHT_FACTOR);
				event.addModification(mod);
			} else if (ability instanceof FireAbility && day && player.hasPermission("bending.fire.dayfactor")) {
				DayNightFactor dayNightFactor = event.getMarker(DayNightFactor.class);
				double factor = dayNightFactor.factor() == -1 ? FireAbility.getDayFactor() : dayNightFactor.factor();
				//If the factor isn't the default, use the one in the annotation

				AttributeModifier modifier = dayNightFactor.invert() ? AttributeModifier.DIVISION : AttributeModifier.MULTIPLICATION;
				AttributeModification mod = AttributeModification.of(modifier, factor, AttributeModification.DAY_FACTOR);
				event.addModification(mod);
			}
		}

		//Blue fire has factors for a few attributes. But only do it for pure fire abilities and not combustion/lightning
		Element element = ability.getElement();
		BendingPlayer bPlayer = ability.getBendingPlayer();
		if ((element == Element.FIRE || element == Element.BLUE_FIRE) && bPlayer.hasElement(Element.BLUE_FIRE) && player.hasPermission("bending.fire.bluefirefactor")) {
            switch (event.getAttribute()) {
                case Attribute.DAMAGE -> {
                    double factor = BlueFireAbility.getDamageFactor();
                    event.addModification(AttributeModification.of(AttributeModifier.MULTIPLICATION, factor, AttributeModification.PRIORITY_NORMAL - 50, AttributeModification.BLUE_FIRE_DAMAGE));
                }
                case Attribute.COOLDOWN -> {
                    double factor = BlueFireAbility.getCooldownFactor();
                    event.addModification(AttributeModification.of(AttributeModifier.MULTIPLICATION, factor, AttributeModification.PRIORITY_NORMAL - 50, AttributeModification.BLUE_FIRE_COOLDOWN));
                }
                case Attribute.RANGE -> {
                    double factor = BlueFireAbility.getRangeFactor();
                    event.addModification(AttributeModification.of(AttributeModifier.MULTIPLICATION, factor, AttributeModification.PRIORITY_NORMAL - 50, AttributeModification.BLUE_FIRE_RANGE));
                }
            }
		}

		//AvatarState factors if the avatarstate is active
		if (bPlayer.isAvatarState()) {
			AttributeCache cache = CoreAbility.getAttributeCache(ability).get(event.getAttribute());
			if (cache != null && cache.getAvatarStateModifier().isPresent()) { //Check if there is a cached avatarstate modifier for this attribute
				event.addModification(cache.getAvatarStateModifier().get());
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onItemMerge(final ItemMergeEvent event) {
		Item entity = event.getEntity();
		if (BendingPlayer.isWorldDisabled(entity.getWorld())) {
			return;
		}

		for (final MetalClips metalClips : CoreAbility.getAbilities(MetalClips.class)) {
			List<Item> trackedIngots = metalClips.getTrackedIngots();
			if (trackedIngots.contains(entity) || trackedIngots.contains(event.getTarget())) {
				event.setCancelled(true);
				break;
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockPistonExtendEvent(final BlockPistonExtendEvent event) {
		if (BendingPlayer.isWorldDisabled(event.getBlock().getWorld())) {
			return;
		}
		for (final Block block : event.getBlocks()) {
			if (TempBlock.isTempBlock(block)) {
				event.setCancelled(true);
				break;
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockPistonRetractEvent(final BlockPistonRetractEvent event) {
		if (BendingPlayer.isWorldDisabled(event.getBlock().getWorld())) {
			return;
		}
		for (final Block block : event.getBlocks()) {
			if (TempBlock.isTempBlock(block)) {
				event.setCancelled(true);
				break;
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBendingSubElementChange(final PlayerChangeSubElementEvent event) {
		Player player = event.getTarget().getPlayer();
		if (player == null) return;
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) return;
		BendingBoardManager.updateAllSlots(player);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBindChange(final PlayerBindChangeEvent event) {
		Player player = event.getPlayer().getPlayer();
		if (player == null) {
			return;
		}

		if (event.isMultiAbility()) {
			Bukkit.getScheduler().runTaskLater(plugin, () -> BendingBoardManager.updateAllSlots(player), 1L);
		} else {
			BendingBoardManager.updateBoard(player, event.isBinding() ? event.getAbility() : "", false, event.getSlot());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerStanceChange(final PlayerStanceChangeEvent event) {
		final Player player = event.getPlayer();
		if (!event.getOldStance().isEmpty()) {
			BendingBoardManager.updateBoard(player, event.getOldStance(), false, 0);
		}
		if (!event.getNewStance().isEmpty()) {
			BendingBoardManager.updateBoard(player, event.getNewStance(), false, 0);
		}
	}

	@EventHandler
	public void onPluginUnload(PluginDisableEvent event) {
		RegionProtection.unloadPlugin((JavaPlugin) event.getPlugin());
		BendingPlayer.BEND_HOOKS.remove((JavaPlugin) event.getPlugin());
		BendingPlayer.BIND_HOOKS.remove((JavaPlugin) event.getPlugin());
	}

	@EventHandler
	public void onWorldUnload(WorldUnloadEvent event) {
		TempBlock.removeAllInWorld(event.getWorld());
	}

	@EventHandler
	private void preventArmorSwap(PlayerInteractEvent event) {
		//Prevents swapping armor pieces using right click while having TempArmor active, this will prevent Armor pieces from being duped/deleted.
		if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK || event.useItemInHand() == Event.Result.DENY) {
			return;
		}

		Player player = event.getPlayer();
		ItemStack mainHand = player.getInventory().getItemInMainHand();
		if (EnchantmentTarget.WEARABLE.includes(mainHand) && EnchantmentTarget.ARMOR.includes(mainHand) && TempArmor.hasTempArmor(player)) {
			event.setCancelled(true);
		}
	}

	public static HashMap<Player, Pair<String, Player>> getBendingPlayerDeath() {
		return BENDING_PLAYER_DEATH;
	}

	public static Set<UUID> getRightClickPlayers() {
		return RIGHT_CLICK_INTERACT;
	}

	/**
	 * Use {@link #getRightClickPlayers()} instead.
	 */
	@Deprecated
	public static List<UUID> getRightClickInteract() {
		return new ArrayList<>(RIGHT_CLICK_INTERACT);
	}

	/**
	 * Deprecated. Use {@link OfflineBendingPlayer#isToggled()} instead.
	 * @return list of players with bending toggled off
	 */
	@Deprecated
	public static ArrayList<UUID> getToggledOut() {
		return BendingPlayer.getOfflinePlayers().values().stream().filter(player -> !player.isToggled()).map(OfflineBendingPlayer::getUUID).collect(Collectors.toCollection(ArrayList::new));
	}

	public static Map<Player, Integer> getJumpStatistics() {
		return JUMPS;
	}
}
