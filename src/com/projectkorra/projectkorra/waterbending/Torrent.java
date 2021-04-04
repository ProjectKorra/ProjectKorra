package com.projectkorra.projectkorra.waterbendingv2;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.TorrentWave;
import com.projectkorra.projectkorra.waterbending.util.WaterReturn;
import com.projectkorra.projectkorra.waterbendingv2.util.WaterSource;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * @author Finn Bon
 */
public class Torrent extends WaterAbility {

	private enum State {
		SOURCE_SELECTED, // a source has been selected
		PULLING_SOURCE, // water is going from the source to the ring
		FORMING, // the source has arrived and a ring is being formed
		SPINNING, // a ring is currently around the player
		STREAM, // a stream of water is being bent by the player
		FREEZE_STREAM, // the stream should freeze on impact
	}

	// holds all torrent ice blocks and the ability id they were created with
	private static final Map<TempBlock, Integer> FROZEN_BLOCKS = new HashMap<>();

	private State state;
	private WaterSource source;
	private Location location;

	// this block is the tempblock that forms the animation of pulling water from the source into a ring
	private TempBlock sourceTempBlock;
	private LinkedList<TempBlock> animationBlocks;
	private List<Entity> hurtEntities;

	private boolean hit;
	private int hits = 1;

	// config values
	private double sourceRange;
	private double radius;
	private double range;
	private double streamLength, angle;
	private double knockback;
	private double deflectDamage;
	private int maxHits;
	private double damage;
	private double successiveDamage;
	private double knockup;
	private int freezeRadius;
	private boolean avoidFreezingFeet, avoidFreezingHead;
	private boolean revert;
	private long revertTime;
	private long cooldown;

	public void setFields() {
		this.angle = 0;
		this.sourceRange = getConfig().getDouble("Abilities.Water.Torrent.SelectRange");
		this.radius = getConfig().getDouble("Abilities.Water.Torrent.Radius");
		this.range = getConfig().getDouble("Abilities.Water.Torrent.Range");
		this.streamLength = getConfig().getDouble("Abilities.Water.Torrent.Angle");
		this.knockback = getConfig().getDouble("Abilities.Water.Torrent.Knockback");
		this.deflectDamage = getConfig().getDouble("Abilities.Water.Torrent.DeflectDamage");
		this.damage = getConfig().getDouble("Abilities.Water.Torrent.InitialDamage");
		this.successiveDamage = getConfig().getDouble("Abilities.Water.Torrent.SuccessiveDamage");
		this.maxHits = getConfig().getInt("Abilities.Water.Torrent.MaxHits");
		this.freezeRadius = getConfig().getInt("Abilities.Water.Torrent.MaxLayer");
		this.knockup = getConfig().getDouble("Abilities.Water.Torrent.Knockup");
		this.avoidFreezingHead = getConfig().getBoolean("Properties.Water.FreezePlayerHead");
		this.avoidFreezingFeet = getConfig().getBoolean("Properties.Water.FreezePlayerFeet");
		this.revert = getConfig().getBoolean("Abilities.Water.Torrent.Revert");
		this.revertTime = getConfig().getLong("Abilities.Water.Torrent.RevertTime");
		this.cooldown = getConfig().getLong("Abilities.Water.Torrent.Cooldown");

		animationBlocks = new LinkedList<>();
		hurtEntities = new ArrayList<>();
	}

	/**
	 * Creates a new torrent instance from either an automatic or manual source.
	 * @param player The player to create the torrent instance for
	 * @param selectSourceManually Whether or not the player tried to manually select a source
	 */
	public Torrent(Player player, boolean selectSourceManually) {
		super(player);

		setFields();

		// try to find a valid source for this torrent
		if (selectSourceManually) {
			// check for source in line of sight
			source = WaterSource.findManualSource(player, sourceRange, bPlayer.canPlantbend());
			state = State.SOURCE_SELECTED;
		} else {
			// find auto source
			source = WaterSource.findAutoSource(player, sourceRange, bPlayer.canPlantbend());
			state = State.PULLING_SOURCE;
		}

		// if we didn't find one, quit setting up
		if (source == null) {
			return;
		}

		// at this point, a source has been selected and we can start setting up a valid torrent
		// if a torrent was already active, check it's state and handle accordingly
		Torrent old = getAbility(player, getClass());
		if (old != null) {
			if (old.advanceStateByLeftClick()) {
				// the advance state returned true, remove it
				old.remove();
			} else {
				// the state of the old torrent was successfully advanced and thus we don't create a second one
				return;
			}
		}

		// check for cooldowns
		if (this.bPlayer.isOnCooldown("Torrent")) {
			return;
		}

		if (this.bPlayer.isAvatarState()) {
			this.knockback = getConfig().getDouble("Abilities.Avatar.AvatarState.Water.Torrent.Push");
			this.damage = getConfig().getDouble("Abilities.Avatar.AvatarState.Water.Torrent.InitialDamage");
			this.successiveDamage = getConfig().getDouble("Abilities.Avatar.AvatarState.Water.Torrent.SuccessiveDamage");
			this.maxHits = getConfig().getInt("Abilities.Avatar.AvatarState.Water.Torrent.MaxHits");
		}

		if (state == State.PULLING_SOURCE) {
			location = source.use(this::handleTorrentTempBlockAsSource);
		}
		start();
	}

	/**
	 * This method is called when a block is consumed as a source which is also part of a torrent freeze sphere
	 */
	private void handleTorrentTempBlockAsSource(Block block) {
		TempBlock tempBlock = TempBlock.get(block);
		if (tempBlock == null) return;

		int torrentId = FROZEN_BLOCKS.getOrDefault(tempBlock, -1);
		if (torrentId != -1) {
			massThaw(torrentId);
		} else if (isBendableWaterTempBlock(tempBlock)) {
			tempBlock.revertBlock();
		}
	}

	private void handleSourceSelected() {
		if (!source.isValid(sourceRange)) {
			remove();
			return;
		}
		source.playIndicator();
		if (player.isSneaking()) {
			state = State.PULLING_SOURCE;
			location = source.use(this::handleTorrentTempBlockAsSource);
			sourceTempBlock = new TempBlock(location.getBlock(), Material.WATER);
		}
	}

	private void handlePullingSource() {
		if (!player.isSneaking()) {
			remove();
			return;
		}

		// calculate where the source needs to travel to
		double startAngle = this.player.getEyeLocation().getDirection().angle(new Vector(1, 0, 0));
		double dX = radius * Math.cos(startAngle);
		double dZ = radius * Math.sin(startAngle);
		Location target = player.getEyeLocation().add(dX, 0, dZ);

		double heightDiff = target.getBlockY() - location.getBlockY();
		heightDiff = Math.abs(heightDiff) / heightDiff;

		if (heightDiff != 0) {
			this.location.add(new Vector(0, heightDiff, 0));
		} else {
			this.location.add(GeneralMethods.getDirection(this.location, target).normalize());
		}

		// if the source has arrived at it's destination
		if (this.location.distanceSquared(target) <= 1) {
			state = State.FORMING;
			sourceTempBlock.revertBlock();
			sourceTempBlock = null;
		} else {
			// revert the old tempblock
			sourceTempBlock.revertBlock();
			// check if new location is transparent, if not we've hit a wall
			Block block = location.getBlock();
			if (!isTransparent(player, block)) {
				remove();
				return;
			}
			// set a new tempblock
			sourceTempBlock = new TempBlock(block, Material.WATER);
		}
	}

	private void handleHolding() {
		if (ThreadLocalRandom.current().nextInt(4) == 0) {
			playWaterbendingSound(this.location);
		}

		// if the player releases sneak, let loose the tidal wave
		if (!player.isSneaking()) {
			if (state == State.SPINNING) {
				new TorrentWave(player, radius);
			}
			remove();
			return;
		}

		// revert all tempblocks from previous render
		this.animationBlocks.forEach(TempBlock::revertBlock);
		this.animationBlocks.clear();

		// display the spinning ring
		Block lastBlock = null;
		// List<Entity> affectedEntities = new ArrayList<>();
		List<Entity> entities = GeneralMethods.getEntitiesAroundPoint(player.getEyeLocation(), radius + 2);
		Location loc = player.getEyeLocation();

		// for loop to display the
		for (double theta = this.angle; theta < this.streamLength + this.angle; theta += 20) {
			double phi = Math.toRadians(theta);
			double x = Math.cos(phi) * this.radius;
			double z = Math.sin(phi) * this.radius;
			loc.add(x, 0, z);
			Block block = loc.getBlock();
			if (block != lastBlock) {
				lastBlock = block;
				if (isTransparent(block)) {
					this.animationBlocks.add(new TempBlock(block, Material.WATER));
					for (Entity entity : entities) {
						if (entity.getWorld() != loc.getWorld()) continue;
						if (entity.getLocation().distanceSquared(loc) <= 1.5 * 1.5) {
							this.pushAway(entity);
						}
					}
				}
				if (isWater(loc.getBlock()) && GeneralMethods.isAdjacentToThreeOrMoreSources(loc.getBlock())) {
					ParticleEffect.WATER_BUBBLE.display(loc.getBlock().getLocation().clone().add(.5, .5, .5), 5, Math.random(), Math.random(), Math.random(), 0);
				}
			}
			loc.subtract(x, 0, z);
		}

		if (this.streamLength < 220) {
			this.streamLength += 20;
		} else if (state == State.FORMING) {
			state = State.SPINNING;
		}
		this.angle = (this.angle + 30) % 360;
	}

	private void handleStream() {
		if (!player.isSneaking()) {
			remove();
			return;
		}

		Block streamHead = animationBlocks.getLast().getBlock();

		final Entity target = GeneralMethods.getTargetedEntity(this.player, this.range, this.hurtEntities);
		Location targetLoc = this.player.getTargetBlock(getTransparentMaterialSet(), (int) this.range).getLocation();
		if (target != null) {
			targetLoc = target.getLocation();
		}

		Vector dir = GeneralMethods.getDirection(this.location, targetLoc).normalize();

		// this makes the torrent stream shoot through the target entity instead of stop at it
		if (target != null) {
			targetLoc = location.clone().add(dir.clone().multiply(10));
		}

		if (!hit) {
			this.location.add(dir);
			animationBlocks.removeFirst().revertBlock();
		}

		// if stream is too far or in a protected region
		if (location.distanceSquared(player.getEyeLocation()) > range * range || GeneralMethods.isRegionProtectedFromBuild(this, location)) {
			if (!hit) {
				hit = true;
			}
			// TODO: Do layer stuff?
			if (animationBlocks.size() <= 1) {
				this.remove();
			}
		} else if (!isTransparent(player, location.getBlock())) {
			if (state == State.FREEZE_STREAM) {
				freeze();
			} else {
				remove();
			}
		} else {
			if (location.getBlock().equals(streamHead)) {
				hit = true;
				return;
			}
			if (location.distanceSquared(targetLoc) > 1) {
				if (isWaterbendable(location.getBlock())) {
					ParticleEffect.WATER_BUBBLE.display(location.clone().add(.5, .5, .5), 5, Math.random(), Math.random(), Math.random(), 0);
				}
				animationBlocks.addLast(new TempBlock(location.getBlock(), Material.WATER));
			} else if (state == State.FREEZE_STREAM) { // Why was this here in the original?
				freeze();
			}
		}

		// find all nearby entities
		int radius = (int) Math.floor(animationBlocks.size() / 2.0) + 1;
		List<Entity> affectedEntities = GeneralMethods.getEntitiesAroundPoint(
			animationBlocks.get(radius).getLocation(),
			radius
		);

		// pull entities in the stream
		int i = animationBlocks.size();
		for (TempBlock animationBlock : animationBlocks) {
			i--;
			Block block = animationBlock.getBlock();
			Iterator<Entity> it = affectedEntities.iterator();
			Entity entity;
			while (it.hasNext()) {
				entity = it.next();
				if (entity.getWorld() != block.getWorld()) {
					it.remove();
				}
				if (entity.getLocation().distanceSquared(block.getLocation()) <= 1.5 * 1.5) {
					if (i == animationBlocks.size() - 1) {
						dragTarget(entity, dir);
					} else {
						dragTarget(entity, GeneralMethods.getDirection(block.getLocation(), animationBlocks.get(i + 1).getLocation()).normalize());
					}
					it.remove();
				}
			}
		}

		if (animationBlocks.isEmpty()) {
			remove();
		}
	}

	private void dragTarget(Entity entity, Vector dir) {
		if (entity.getEntityId() == player.getEntityId()) {
			return;
		}

		if (
			GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation()) ||
			(entity instanceof Player && Commands.invincible.contains(entity.getName()))
		) {
			return;
		}
		if (dir.getY() > this.knockup) {
			dir.setY(this.knockup);
		}
		if (state == State.FREEZE_STREAM) {
			entity.setVelocity(dir.multiply(this.knockback));
		}
		if (!(entity instanceof LivingEntity) || this.hurtEntities.contains(entity)) return;

		double totalDamage = hits > 1 ? getNightFactor(successiveDamage) : getNightFactor(damage);

		if (this.hits <= this.maxHits) {
			this.hits++;
		}

		DamageHandler.damageEntity(entity, totalDamage, this);
		AirAbility.breakBreathbendingHold(entity);
		this.hurtEntities.add(entity);
		((LivingEntity) entity).setNoDamageTicks(0);
	}

	private void freeze() {
		if (!this.bPlayer.canBendIgnoreBindsCooldowns(getAbility("PhaseChange"))) {
			return;
		}
		List<Block> blocksToFreeze = GeneralMethods.getBlocksAroundPoint(this.location, this.freezeRadius);
		List<Entity> trapped = GeneralMethods.getEntitiesAroundPoint(this.location, this.freezeRadius);
		Collection<Block> avoidFeet = trapped.stream().filter(e -> e instanceof Player).map(e -> e.getLocation().getBlock()).collect(Collectors.toSet());
		Collection<Block> avoidHead = avoidFeet.stream().map(block -> block.getRelative(BlockFace.UP)).collect(Collectors.toSet());
		if (!avoidFreezingFeet)
			blocksToFreeze.removeAll(avoidFeet);
		if (!avoidFreezingHead)
			blocksToFreeze.removeAll(avoidHead);

		for (Block toFreeze : blocksToFreeze) {
			if (!isTransparent(toFreeze) || isIce(toFreeze)) continue;

			TempBlock ice = new TempBlock(toFreeze, Material.ICE);
			FROZEN_BLOCKS.put(ice, getId());
			if (revert) {
				ice.setRevertTime(revertTime + (new Random().nextInt((500 + 500) + 1) - 500));
			}
			playIcebendingSound(toFreeze.getLocation());
		}
	}

	@SuppressWarnings("DuplicatedCode")
	private void pushAway(Entity entity) {
		if (entity.getEntityId() == player.getEntityId()) {
			return;
		}
		if (GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation()) || ((entity instanceof Player) && Commands.invincible.contains(entity.getName()))) {
			return;
		}
		double x, z, vx, vz, mag;
		double angle = 50;
		angle = Math.toRadians(angle);

		x = entity.getLocation().getX() - this.player.getLocation().getX();
		z = entity.getLocation().getZ() - this.player.getLocation().getZ();

		mag = Math.sqrt(x * x + z * z);

		vx = (x * Math.cos(angle) - z * Math.sin(angle)) / mag;
		vz = (x * Math.sin(angle) + z * Math.cos(angle)) / mag;

		final Vector vec = new Vector(vx, 0, vz).normalize().multiply(this.knockback);
		final Vector velocity = entity.getVelocity();

		if (this.bPlayer.isAvatarState()) {
			velocity.setX(AvatarState.getValue(vec.getX()));
			velocity.setZ(AvatarState.getValue(vec.getZ()));
		} else {
			velocity.setX(vec.getX());
			velocity.setZ(vec.getY());
		}

		GeneralMethods.setVelocity(entity, velocity);
		entity.setFallDistance(0);
		if (entity instanceof LivingEntity) {
			final double damageDealt = this.getNightFactor(this.deflectDamage);
			DamageHandler.damageEntity(entity, damageDealt, this);
			AirAbility.breakBreathbendingHold(entity);
		}
	}

	// this method thaws an entire sphere for a given torrent id
	private static void massThaw(int torrentId) {
		for (Map.Entry<TempBlock, Integer> entry : FROZEN_BLOCKS.entrySet()) {
			if (entry.getValue() != torrentId) continue;
			thaw(entry.getKey());
		}
	}

	public static void massThaw(TempBlock tempBlock) {
		int torrentId = FROZEN_BLOCKS.getOrDefault(tempBlock, -1);
		if (torrentId != -1) {
			massThaw(torrentId);
		}
	}

	public static void thaw(TempBlock tempBlock) {
		tempBlock.revertBlock();
		FROZEN_BLOCKS.remove(tempBlock);
	}

	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		if (location != null && (
			location.getWorld() != player.getLocation().getWorld() ||
			location.distanceSquared(player.getLocation()) > range * range
		)) {
			remove();
			return;
		}
		switch (state) {
			case SOURCE_SELECTED:
				handleSourceSelected();
				break;
			case PULLING_SOURCE:
				handlePullingSource();
				break;
			case FORMING:
			case SPINNING:
				handleHolding();
				break;
			case STREAM:
			case FREEZE_STREAM:
				handleStream();
				break;
		}
	}

	@Override
	public void remove() {
		super.remove();
		for (TempBlock block : animationBlocks) {
			block.revertBlock();
		}
		animationBlocks.clear();
		if (this.sourceTempBlock != null) {
			this.sourceTempBlock.revertBlock();
		}

		if (this.location != null) {
			// remove until bottlebending are supported?
			new WaterReturn(this.player, location.getBlock());
		}
	}

	@Override
	public boolean canBeSource() {
		return state == State.SPINNING;
	}

	/**
	 * Tries to advance the state of the ability by left clicking.
	 * @return Whether or not the ability should be removed
	 */
	private boolean advanceStateByLeftClick() {
		switch (state) {
			case SPINNING:
				// when left clicking on holding, launch it
				state = State.STREAM;
				// if streamBlocks is empty we just entered this state for the first time, copy the ring blocks to the stream
				this.location = animationBlocks.getLast().getLocation();
				return false;
			case STREAM:
				// when left clicking during stream, prepare freeze
				state = State.FREEZE_STREAM;
				return false;
			case SOURCE_SELECTED:
				// when left clicking and the previous torrent only had a source selected,
				// we just want to select a new source
				return true;
		}
		return false;
	}

	private void startRingFormation() {
		state = State.PULLING_SOURCE;
	}

	/**
	 * this method is called when a player attempts to form a torrent ring by sneaking.
	 * This only works when the player has a source ability active or has selected a source.
	 * @param player The player that wants to form a torrent
	 */
	public static void onSneak(Player player) {
		Torrent torrent = CoreAbility.getAbility(player, Torrent.class);
		// if no torrent instance is active, no source was selected
		if (torrent == null) {
			// create a new torrent instance and let it attempt to find it's own source
			new Torrent(player, false);
		} else {
			// if we already had a torrent instance, we want to tell it to form the ring
			torrent.startRingFormation();
		}
	}

	public static boolean canThaw(final Block block) {
		return TempBlock.isTempBlock(block) && !FROZEN_BLOCKS.containsKey(TempBlock.get(block));
	}

	public static void progressAllCleanup() {
		for (TempBlock block : FROZEN_BLOCKS.keySet()) {
			if (block.getBlock().getType() != Material.ICE) {
				FROZEN_BLOCKS.remove(block);
			}
		}
	}

	public static List<TempBlock> getFrozenBlocks() {
		return new ArrayList<>(FROZEN_BLOCKS.keySet());
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public boolean isCollidable() {
		return state != State.SOURCE_SELECTED && state != State.PULLING_SOURCE;
	}

	@Override
	public boolean allowBreakPlants() {
		return false;
	}

	@Override
	public String getName() {
		return "Torrent";
	}

	@Override
	public Location getLocation() {
		return location;
	}
}
