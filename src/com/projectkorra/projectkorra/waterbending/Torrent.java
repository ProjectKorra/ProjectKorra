package com.projectkorra.projectkorra.waterbending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class Torrent extends WaterAbility {

	private static final double CLEANUP_RANGE = 50;
	private static final Map<TempBlock, Player> FROZEN_BLOCKS = new ConcurrentHashMap<>();
	
	private boolean sourceSelected;
	private boolean settingUp;
	private boolean forming;
	private boolean formed;
	private boolean launch;
	private boolean launching;
	private boolean freeze;
	private int layer;
	private int maxLayer;
	private int maxHits;
	private int hits = 1;
	private long time;
	private long interval;
	private long cooldown;
	private double startAngle;
	private double angle;
	private double radius;
	private double push;
	private double maxUpwardForce;
	private double damage;
	private double successiveDamage;
	private double deflectDamage;
	private double range;
	private double selectRange;
	private Block sourceBlock;
	private TempBlock source;
	private Location location;
	private ArrayList<TempBlock> blocks;
	private ArrayList<TempBlock> launchedBlocks;
	private ArrayList<Entity> hurtEntities;
	
	public Torrent(Player player) {
		super(player);

		this.layer = 0;
		this.startAngle = 0;
		this.maxLayer = getConfig().getInt("Abilities.Water.Torrent.MaxLayer");
		this.push = getConfig().getDouble("Abilities.Water.Torrent.Push");
		this.angle = getConfig().getDouble("Abilities.Water.Torrent.Angle");
		this.radius = getConfig().getDouble("Abilities.Water.Torrent.Radius");
		this.maxUpwardForce = getConfig().getDouble("Abilities.Water.Torrent.MaxUpwardForce");
		this.interval = getConfig().getLong("Abilities.Water.Torrent.Interval");
		this.damage = getConfig().getDouble("Abilities.Water.Torrent.InitialDamage");
		this.successiveDamage = getConfig().getDouble("Abilities.Water.Torrent.SuccessiveDamage");
		this.maxHits = getConfig().getInt("Abilities.Water.Torrent.MaxHits");
		this.deflectDamage = getConfig().getDouble("Abilities.Water.Torrent.DeflectDamage");
		this.range = getConfig().getDouble("Abilities.Water.Torrent.Range");
		this.selectRange = getConfig().getDouble("Abilities.Water.Torrent.SelectRange");
		this.cooldown = getConfig().getLong("Abilities.Water.Torrent.Cooldown");
		this.blocks = new ArrayList<>();
		this.launchedBlocks = new ArrayList<>();
		this.hurtEntities = new ArrayList<>();
		
		Torrent oldTorrent = getAbility(player, Torrent.class);
		if (oldTorrent != null) {
			if (!oldTorrent.sourceSelected) {
				oldTorrent.use();
				bPlayer.addCooldown("Torrent", oldTorrent.cooldown);
				return;
			} else {
				oldTorrent.remove();
			}
		}
		
		if (bPlayer.isOnCooldown("Torrent")) {
			return;
		}
		
		time = System.currentTimeMillis();
		sourceBlock = BlockSource.getWaterSourceBlock(player, selectRange, ClickType.LEFT_CLICK, true, true, bPlayer.canPlantbend());
		if (sourceBlock != null && !GeneralMethods.isRegionProtectedFromBuild(this, sourceBlock.getLocation())) {
			sourceSelected = true;
			start();
		}
	}

	private void freeze() {
		if (layer == 0) {
			return;
		} else if (!bPlayer.canBendIgnoreBindsCooldowns(getAbility("PhaseChange"))) {
			return;
		}
		
		List<Block> ice = GeneralMethods.getBlocksAroundPoint(location, layer);
		for (Block block : ice) {
			if (isTransparent(player, block) && block.getType() != Material.ICE) {
				TempBlock tblock = new TempBlock(block, Material.ICE, (byte) 0);
				FROZEN_BLOCKS.put(tblock, player);
				playIcebendingSound(block.getLocation());
			}
		}
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreCooldowns(this)) {
			remove();
			return;
		}

		if (System.currentTimeMillis() > time + interval) {
			time = System.currentTimeMillis();

			if (sourceSelected) {
				if (sourceBlock.getLocation().distanceSquared(player.getLocation()) > selectRange * selectRange) {
					return;
				}

				if (player.isSneaking()) {
					sourceSelected = false;
					settingUp = true;
					
					if (isPlant(sourceBlock) || isSnow(sourceBlock)) {
						new PlantRegrowth(player, sourceBlock);
						sourceBlock.setType(Material.AIR);
					} else if (!GeneralMethods.isAdjacentToThreeOrMoreSources(sourceBlock)) {
						sourceBlock.setType(Material.AIR);
					}
					source = new TempBlock(sourceBlock, Material.STATIONARY_WATER, (byte) 8);
					location = sourceBlock.getLocation();
				} else {
					playFocusWaterEffect(sourceBlock);
					return;
				}
			}

			if (settingUp) {
				if (!player.isSneaking()) {
					location = source.getLocation();
					remove();
					return;
				}
				
				Location eyeLoc = player.getEyeLocation();
				double startAngle = player.getEyeLocation().getDirection().angle(new Vector(1, 0, 0));
				double dx = radius * Math.cos(startAngle);
				double dy = 0;
				double dz = radius * Math.sin(startAngle);
				Location setup = eyeLoc.clone().add(dx, dy, dz);

				if (!location.getWorld().equals(player.getWorld())) {
					remove();
					return;
				} else if (location.distanceSquared(setup) > range * range) {
					remove();
					return;
				}

				if (location.getBlockY() > setup.getBlockY()) {
					Vector direction = new Vector(0, -1, 0);
					location = location.clone().add(direction);
				} else if (location.getBlockY() < setup.getBlockY()) {
					Vector direction = new Vector(0, 1, 0);
					location = location.clone().add(direction);
				} else {
					Vector direction = GeneralMethods.getDirection(location, setup).normalize();
					location = location.clone().add(direction);
				}

				if (location.distanceSquared(setup) <= 1) {
					settingUp = false;
					source.revertBlock();
					source = null;
					forming = true;
				} else if (!location.getBlock().equals(source.getLocation().getBlock())) {
					source.revertBlock();
					source = null;
					Block block = location.getBlock();
					if (!isTransparent(player, block)) {
						remove();
						return;
					}
					source = new TempBlock(location.getBlock(), Material.STATIONARY_WATER, (byte) 8);
				}
			}
			if (forming && !player.isSneaking()) {
				location = player.getEyeLocation().add(radius, 0, 0);
				remove();
				return;
			}

			if (forming || formed) {
				if ((new Random()).nextInt(4) == 0) {
					playWaterbendingSound(location);
				}
				for (double theta = startAngle; theta < angle + startAngle; theta += 20) {
					Location loc = player.getEyeLocation();
					double phi = Math.toRadians(theta);
					double dx = Math.cos(phi) * radius;
					double dy = 0;
					double dz = Math.sin(phi) * radius;
					loc.add(dx, dy, dz);
					if(GeneralMethods.isAdjacentToThreeOrMoreSources(loc.getBlock())) {
						ParticleEffect.WATER_BUBBLE.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0f, 5, loc.getBlock().getLocation().clone().add(.5,.5,.5), 257D);
					}
					loc.subtract(dx, dy, dz);
				}
				if (angle < 220) {
					angle += 20;
				} else {
					forming = false;
					formed = true;
				}
				
				formRing();
				if (blocks.isEmpty()) {
					remove();
					return;
				}

			}

			if (formed && !player.isSneaking() && !launch) {
				new TorrentWave(player, radius);
				remove();
				return;
			}

			if (launch && formed) {
				launching = true;
				launch = false;
				formed = false;
				if (!launch()) {
					returnWater(location);
					remove();
					return;
				}
			}

			if (launching) {
				if (!player.isSneaking()) {
					remove();
					return;
				}
				if (!launch()) {
					remove();
					return;
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	private boolean launch() {
		if (launchedBlocks.isEmpty() && blocks.isEmpty()) {
			return false;
		}

		if (launchedBlocks.isEmpty()) {
			clearRing();
			Location loc = player.getEyeLocation();
			ArrayList<Block> doneBlocks = new ArrayList<Block>();
			for (double theta = startAngle; theta < angle + startAngle; theta += 20) {
				double phi = Math.toRadians(theta);
				double dx = Math.cos(phi) * radius;
				double dy = 0;
				double dz = Math.sin(phi) * radius;
				Location blockloc = loc.clone().add(dx, dy, dz);
				
				if (Math.abs(theta - startAngle) < 10) {
					location = blockloc.clone();
				}
				
				Block block = blockloc.getBlock();
				if (!doneBlocks.contains(block) && !GeneralMethods.isRegionProtectedFromBuild(this, blockloc)) {
					if (isTransparent(player, block)) {
						launchedBlocks.add(new TempBlock(block, Material.STATIONARY_WATER, (byte) 8));
						doneBlocks.add(block);
					} else if (!isTransparent(player, block)) {
						break;
					}
				}
			}
			if (launchedBlocks.isEmpty()) {
				return false;
			} else {
				return true;
			}
		}

		Entity target = GeneralMethods.getTargetedEntity(player, range, hurtEntities);
		Location targetLoc = player.getTargetBlock(getTransparentMaterialSet(), (int) range).getLocation();
		if (target != null) {
			targetLoc = target.getLocation();
		}

		ArrayList<TempBlock> newBlocks = new ArrayList<TempBlock>();
		List<Entity> entities = GeneralMethods.getEntitiesAroundPoint(player.getLocation(), range + 5);
		List<Entity> affectedEntities = new ArrayList<Entity>();
		Block realBlock = launchedBlocks.get(0).getBlock();
		Vector dir = GeneralMethods.getDirection(location, targetLoc).normalize();

		if (target != null) {
			targetLoc = location.clone().add(dir.clone().multiply(10));
		}
		if (layer == 0) {
			location = location.clone().add(dir);
		}

		Block locBlock = location.getBlock();
		if (location.distanceSquared(player.getLocation()) > range * range || GeneralMethods.isRegionProtectedFromBuild(this, location)) {
			if (layer < maxLayer) {
				if (freeze || layer < 1) {
					layer++;
				}
			}
			if (launchedBlocks.size() == 1) {
				remove();
				return false;
			}
		} else if (!isTransparent(player, locBlock)) {
			if (layer < maxLayer) {
				if (layer == 0) {
					hurtEntities.clear();
				}
				if (freeze || layer < 1) {
					layer++;
				}
			}
			if (freeze) {
				freeze();
			} else if (launchedBlocks.size() == 1) {
				location = realBlock.getLocation();
				remove();
				return false;
			}
		} else {
			if (locBlock.equals(realBlock) && layer == 0) {
				return true;
			}
			if (locBlock.getLocation().distanceSquared(targetLoc) > 1) {
				if (isWater(locBlock)) {
					ParticleEffect.WATER_BUBBLE.display((float) Math.random(), (float) Math.random(), (float) Math.random(), 0f, 5, locBlock.getLocation().clone().add(.5,.5,.5), 257D);
				}
				newBlocks.add(new TempBlock(locBlock, Material.STATIONARY_WATER, (byte) 8));
			} else {
				if (layer < maxLayer) {
					if (layer == 0) {
						hurtEntities.clear();
					}
					if (freeze || layer < 1) {
						layer++;
					}
				}
				if (freeze) {
					freeze();
				}
			}
		}

		for (int i = 0; i < launchedBlocks.size(); i++) {
			TempBlock block = launchedBlocks.get(i);
			if (i == launchedBlocks.size() - 1) {
				block.revertBlock();
			} else {
				newBlocks.add(block);
				for (Entity entity : entities) {
					if (entity.getWorld() != block.getBlock().getWorld()) {
						continue;
					}
					if (entity.getLocation().distanceSquared(block.getLocation()) <= 1.5 * 1.5 && !affectedEntities.contains(entity)) {
						if (i == 0) {
							affect(entity, dir);
						} else {
							affect(entity, GeneralMethods.getDirection(block.getLocation(), launchedBlocks.get(i - 1).getLocation()).normalize());
						}
						affectedEntities.add(entity);
					}
				}
			}
		}

		launchedBlocks.clear();
		launchedBlocks.addAll(newBlocks);

		if (launchedBlocks.isEmpty()) {
			return false;
		}
		return true;
	}

	private void formRing() {
		clearRing();
		startAngle += 30;
		
		Location loc = player.getEyeLocation();
		ArrayList<Block> doneBlocks = new ArrayList<Block>();
		ArrayList<Entity> affectedEntities = new ArrayList<Entity>();
		List<Entity> entities = GeneralMethods.getEntitiesAroundPoint(loc, radius + 2);
		
		for (double theta = startAngle; theta < angle + startAngle; theta += 20) {
			double phi = Math.toRadians(theta);
			double dx = Math.cos(phi) * radius;
			double dy = 0;
			double dz = Math.sin(phi) * radius;
			Location blockLoc = loc.clone().add(dx, dy, dz);
			Block block = blockLoc.getBlock();
			if (!doneBlocks.contains(block)) {
				if (isTransparent(player, block)) {		
						blocks.add(new TempBlock(block, Material.STATIONARY_WATER, (byte) 8));
						doneBlocks.add(block);
					for (Entity entity : entities) {
						if (entity.getWorld() != blockLoc.getWorld()) {
							continue;
						}
						if (!affectedEntities.contains(entity) && entity.getLocation().distanceSquared(blockLoc) <= 1.5 * 1.5) {
							deflect(entity);
						}
					}
				}
			}
		}
	}

	private void clearRing() {
		for (TempBlock block : blocks) {
			block.revertBlock();
		}
		blocks.clear();
	}

	@Override
	public void remove() {
		super.remove();
		clearRing();
		for (TempBlock block : launchedBlocks) {
			block.revertBlock();
		}
		
		launchedBlocks.clear();
		if (source != null) {
			source.revertBlock();
		}
		
		if (location != null) {
			returnWater(location);
		}
	}

	private void returnWater(Location location) {
		new WaterReturn(player, location.getBlock());
	}

	@SuppressWarnings("deprecation")
	public static void create(Player player) {
		if (hasAbility(player, Torrent.class)) {
			return;
		}
		
		if (WaterReturn.hasWaterBottle(player)) {
			Location eyeLoc = player.getEyeLocation();
			Block block = eyeLoc.add(eyeLoc.getDirection().normalize()).getBlock();
			if (isTransparent(player, block) && isTransparent(player, eyeLoc.getBlock())) {
				if(block.getType() != Material.WATER) {
					block.setType(Material.STATIONARY_WATER);
					block.setData((byte) 8);
				}
				Torrent tor = new Torrent(player);
				
				if (tor.sourceSelected || tor.settingUp) {
					WaterReturn.emptyWaterBottle(player);
				} else {
					block.setType(Material.AIR);
				}
			}
		}
	}

	private void use() {
		launch = true;
		if (launching) {
			freeze = true;
		}
	}

	private void deflect(Entity entity) {
		if (entity.getEntityId() == player.getEntityId()) {
			return;
		}
		double x, z, vx, vz, mag;
		double angle = 50;
		angle = Math.toRadians(angle);

		x = entity.getLocation().getX() - player.getLocation().getX();
		z = entity.getLocation().getZ() - player.getLocation().getZ();

		mag = Math.sqrt(x * x + z * z);

		vx = (x * Math.cos(angle) - z * Math.sin(angle)) / mag;
		vz = (x * Math.sin(angle) + z * Math.cos(angle)) / mag;

		Vector vec = new Vector(vx, 0, vz).normalize().multiply(push);
		Vector velocity = entity.getVelocity();
		
		if (bPlayer.isAvatarState()) {
			velocity.setX(AvatarState.getValue(vec.getX()));
			velocity.setZ(AvatarState.getValue(vec.getZ()));
		} else {
			velocity.setX(vec.getX());
			velocity.setZ(vec.getY());
		}

		GeneralMethods.setVelocity(entity, velocity);
		entity.setFallDistance(0);
		if (entity instanceof LivingEntity) {
			double damageDealt = getNightFactor(deflectDamage);
			DamageHandler.damageEntity(entity, damageDealt, this);
			AirAbility.breakBreathbendingHold(entity);
		}
	}

	private void affect(Entity entity, Vector direction) {
		if (entity.getEntityId() == player.getEntityId()) {
			return;
		}
		if (direction.getY() > maxUpwardForce) {
			direction.setY(maxUpwardForce);
		}
		if (!freeze) {
			entity.setVelocity(direction.multiply(push));
		}
		if (entity instanceof LivingEntity && !hurtEntities.contains(entity)) {
			double damageDealt = getNightFactor(damage);
			if (hits > 1 && hits <= maxHits) {
				damageDealt = getNightFactor(successiveDamage);
			}
			if (hits == maxHits) {
				hits = maxHits + 1;
			} else {
				hits += 1;
			}
			DamageHandler.damageEntity(entity, damageDealt, this);
			AirAbility.breakBreathbendingHold(entity);
			hurtEntities.add(entity);
			((LivingEntity) entity).setNoDamageTicks(0);
		}
	}

	public static void progressAllCleanup() {
		for (TempBlock block : FROZEN_BLOCKS.keySet()) {
			Player player = FROZEN_BLOCKS.get(block);
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
			if (bPlayer == null) {
				return;
			} else if (block.getBlock().getType() != Material.ICE) {
				FROZEN_BLOCKS.remove(block);
				continue;
			} else if (!player.isOnline()) {
				thaw(block);
				continue;
			} else if (block.getBlock().getWorld() != player.getWorld()) {
				thaw(block);
				continue;
			} else if (block.getLocation().distanceSquared(player.getLocation()) > CLEANUP_RANGE * CLEANUP_RANGE 
					|| !bPlayer.canBendIgnoreBindsCooldowns(getAbility("Torrent"))) {
				thaw(block);
			}
		}
	}

	public static void thaw(Block block) {
		if (TempBlock.isTempBlock(block)) {
			TempBlock tblock = TempBlock.get(block);
			if (FROZEN_BLOCKS.containsKey(tblock)) {
				thaw(tblock);
			}
		}
	}

	public static void thaw(TempBlock block) {
		block.revertBlock();
		FROZEN_BLOCKS.remove(block);
	}

	public static boolean canThaw(Block block) {
		if (TempBlock.isTempBlock(block)) {
			TempBlock tblock = TempBlock.get(block);
			return !FROZEN_BLOCKS.containsKey(tblock);
		}
		return true;
	}

	public static void removeCleanup() {
		for (TempBlock block : FROZEN_BLOCKS.keySet()) {
			thaw(block);
		}
	}

	public static boolean wasBrokenFor(Player player, Block block) {
		Torrent torrent = getAbility(player, Torrent.class);
		if (torrent != null) {
			if (torrent.sourceBlock == null) {
				return false;
			}
			if (torrent.sourceBlock.equals(block)) {
				return true;
			}
		}
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

	@Override
	public long getCooldown() {
		return cooldown;
	}
	
	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	public boolean isSourceSelected() {
		return sourceSelected;
	}

	public void setSourceSelected(boolean sourceSelected) {
		this.sourceSelected = sourceSelected;
	}

	public boolean isSettingUp() {
		return settingUp;
	}

	public void setSettingUp(boolean settingUp) {
		this.settingUp = settingUp;
	}

	public boolean isForming() {
		return forming;
	}

	public void setForming(boolean forming) {
		this.forming = forming;
	}

	public boolean isFormed() {
		return formed;
	}

	public void setFormed(boolean formed) {
		this.formed = formed;
	}

	public boolean isLaunch() {
		return launch;
	}

	public void setLaunch(boolean launch) {
		this.launch = launch;
	}

	public boolean isLaunching() {
		return launching;
	}

	public void setLaunching(boolean launching) {
		this.launching = launching;
	}

	public boolean isFreeze() {
		return freeze;
	}

	public void setFreeze(boolean freeze) {
		this.freeze = freeze;
	}

	public int getLayer() {
		return layer;
	}

	public void setLayer(int layer) {
		this.layer = layer;
	}

	public int getMaxLayer() {
		return maxLayer;
	}

	public void setMaxLayer(int maxLayer) {
		this.maxLayer = maxLayer;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public double getStartAngle() {
		return startAngle;
	}

	public void setStartAngle(double startAngle) {
		this.startAngle = startAngle;
	}

	public double getAngle() {
		return angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getPush() {
		return push;
	}

	public void setPush(double push) {
		this.push = push;
	}

	public double getMaxUpwardForce() {
		return maxUpwardForce;
	}

	public void setMaxUpwardForce(double maxUpwardForce) {
		this.maxUpwardForce = maxUpwardForce;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public double getDeflectDamage() {
		return deflectDamage;
	}

	public void setDeflectDamage(double deflectDamage) {
		this.deflectDamage = deflectDamage;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getSelectRange() {
		return selectRange;
	}

	public void setSelectRange(double selectRange) {
		this.selectRange = selectRange;
	}

	public Block getSourceBlock() {
		return sourceBlock;
	}

	public void setSourceBlock(Block sourceBlock) {
		this.sourceBlock = sourceBlock;
	}

	public TempBlock getSource() {
		return source;
	}

	public void setSource(TempBlock source) {
		this.source = source;
	}

	public ArrayList<TempBlock> getBlocks() {
		return blocks;
	}

	public void setBlocks(ArrayList<TempBlock> blocks) {
		this.blocks = blocks;
	}

	public static double getCleanupRange() {
		return CLEANUP_RANGE;
	}

	public static Map<TempBlock, Player> getFrozenBlocks() {
		return FROZEN_BLOCKS;
	}

	public ArrayList<TempBlock> getLaunchedBlocks() {
		return launchedBlocks;
	}

	public ArrayList<Entity> getHurtEntities() {
		return hurtEntities;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
	
}
