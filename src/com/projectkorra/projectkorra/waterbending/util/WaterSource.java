package com.projectkorra.projectkorra.waterbending.util;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.*;
import com.projectkorra.projectkorra.waterbending.plant.PlantRegrowth;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Finn Bon
 */
public class WaterSource {

	private static final LinkedList<Class<? extends WaterAbility>> SOURCE_ABILITIES = new LinkedList<>(
		Arrays.asList(
			Torrent.class,
			SurgeWall.class,
			WaterSpoutWave.class,
			OctopusForm.class
		)
	);

	private static final LinkedList<Class<? extends Entity>> SOURCE_ENTITIES = new LinkedList<>(
		Collections.singletonList(
			Slime.class
		)
	);

	private final Player player;
	private final Block block;
	private final Entity entity;
	private final WaterAbility ability;
	private final boolean allowPlantbending;

	private WaterSource(Player player, Block block, Entity entity, WaterAbility ability, boolean allowPlantbending) {
		this.player = player;
		this.block = block;
		this.entity = entity;
		this.ability = ability;
		this.allowPlantbending = allowPlantbending;
	}

	/**
	 * Method that tells you whether or not the source is still valid for a player and range
	 * @param range The maximum distance between the source and the player
	 * @return Whether or not the source is still valid
	 */
	public boolean isValid(double range) {
		if (isAbility() && !ability.isRemoved()) return true;

		Location loc = null;
		if (isBlock()) {
			if (
				!WaterAbility.isWaterbendable(block.getType()) ||
				(WaterAbility.isPlant(block.getType()) && !allowPlantbending)
			) return false;
			loc = block.getLocation();
		} else if (isEntity()) {
			if (player.getLocation().getWorld() != entity.getLocation().getWorld() || entity.isDead()) return false;
			loc = entity.getLocation();
		}
		return loc != null && loc.distanceSquared(player.getLocation()) <= range * range;
	}

	public void playIndicator() {
		Location loc;
		if (isBlock()) {
			loc = block.getLocation().add(0.5, 0.5, 0.5);
		} else if (isEntity()) {
			loc = entity.getLocation().add(0, 1, 0);
		} else {
			return;
		}

		ParticleEffect.SMOKE_NORMAL.display(loc, 4);
	}

	public Location use() {
		return use(b -> {});
	}

	/**
	 * This method consumes the source.
	 *  - In case of a block source, it removes the block and activates plant regrowth if necessary. If the source block was a tempblock, the consumer argument is called.
	 *  - In case of an entity, it gives them a damage tick but doesn't deal any actual damage.
	 *  - In case of an ability, that ability is removed.
	 * @param onTempBlockConsumed A callback to run additional logic when a tempblock is used as a source
	 * @return The location of the source that was just consumed
	 */
	public Location use(Consumer<Block> onTempBlockConsumed) {
		if (isEntity() && entity instanceof LivingEntity) {
			((LivingEntity) entity).damage(0);
			return entity.getLocation();
		}
		if (isBlock()) {
			if (TempBlock.isTempBlock(block)) {
				onTempBlockConsumed.accept(block);
			}

			if (WaterAbility.isPlant(block) || WaterAbility.isSnow(block)) {
				new PlantRegrowth(player, block);
				block.setType(Material.AIR);
			} else if (!GeneralMethods.isAdjacentToThreeOrMoreSources(block)) {
				block.setType(Material.AIR);
			}

			return block.getLocation();
		}
		if (isAbility()) {
			ability.remove();
			return ability.getLocation();
		}
		return null;
	}

	public boolean isBlock() {
		return block != null;
	}

	public boolean isEntity() {
		return entity != null;
	}

	public boolean isAbility() {
		return ability != null;
	}

	/**
	 * Tries to find a source for a player, as if they are manually trying to select one.
	 * @param player The player that tries to manually select a source
	 * @param range The maximum distance between the player and the source
	 * @param plantbending Whether plants are a valid source
	 * @return A WaterSource containing a valid source, or null if none was found
	 */
	public static WaterSource findManualSource(Player player, double range, boolean plantbending) {
		// try to find source entity
		Entity targetEntity = GeneralMethods.getTargetedEntity(player, range);
		if (targetEntity != null) {
			for (Class<? extends Entity> sourceEntity : SOURCE_ENTITIES) {
				if (targetEntity.getClass() == sourceEntity) {
					return new WaterSource(player, null, targetEntity, null, plantbending);
				}
			}
		}

		// try to find source block
		Block targetBlock = WaterAbility.getWaterSourceBlock(player, range, plantbending);
		if (targetBlock != null) {
			return new WaterSource(player, targetBlock, null, null, plantbending);
		}

		return null;
	}

	/**
	 * Tries to find a source for a player automatically, without the player interacting with one.
	 * @param player The player that tries to manually select a source
	 * @param range The maximum distance between the player and the source
	 * @param plantbending Whether plants are a valid source
	 * @return A WaterSource containing a valid source, or null if none was found
	 */
	public static WaterSource findAutoSource(Player player, double range, boolean plantbending) {
		// try to find source ability
		for (Class<? extends WaterAbility> sourceAbility : SOURCE_ABILITIES) {
			WaterAbility potentialSource = CoreAbility.getAbility(player, sourceAbility);
			if (potentialSource != null && potentialSource.canBeSource()) {
				return new WaterSource(player, null, null, potentialSource, plantbending);
			}
		}

		// try to find source entity
		List<Entity> targetEntities = GeneralMethods.getEntitiesAroundPoint(player.getLocation(), range);
		if (targetEntities.size() > 0) {
			for (Entity targetEntity : targetEntities) {
				if (SOURCE_ENTITIES.contains(targetEntity.getClass())) {
					return new WaterSource(player, null, targetEntity, null, plantbending);
				}
			}
		}

		// try to find source block
		Block targetBlock = BlockSource.getWaterSourceBlock(player, range, true, true, plantbending);
		if (targetBlock != null) {
			return new WaterSource(player, targetBlock, null, null, plantbending);
		}

		return null;
	}
}
