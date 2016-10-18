package com.projectkorra.projectkorra.earthbending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.LavaAbility;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class LavaSurge extends LavaAbility {
	
	private static final HashSet<FallingBlock> ALL_FALLING_BLOCKS = new HashSet<>();

	private boolean hasSurgeStarted;
	private boolean isFractureOpen;
	private boolean canSourceBeEarth;
	private int fallingBlocksCount;
	private int maxBlocks;
	private int particleInterval;
	private int fallingBlockInterval;
	private long time;
	private long lastTime;
	private long cooldown;
	private double impactDamage;
	private double fractureRadius;
	private double prepareRange;
	private double travelRange;
	private Block sourceBlock;
	private Random random;
	private Vector direction;
	private Location startLocation;
	private ArrayList<FallingBlock> fallingBlocks;
	private ArrayList<Block> fracture;	
	private ArrayList<TempBlock> fractureTempBlocks;
	private ArrayList<TempBlock> movingLava;
	private Map<FallingBlock, TempBlock> lavaBlocks;
	private ListIterator<Block> listIterator;
	
	public LavaSurge(Player player) {
		super(player);
		
		this.impactDamage = getConfig().getInt("Abilities.Earth.LavaSurge.Damage");
		this.cooldown = getConfig().getLong("Abilities.Earth.LavaSurge.Cooldown");
		this.fractureRadius = getConfig().getDouble("Abilities.Earth.LavaSurge.FractureRadius");
		this.prepareRange = getConfig().getInt("Abilities.Earth.LavaSurge.PrepareRange");
		this.travelRange = getConfig().getInt("Abilities.Earth.LavaSurge.TravelRange");
		this.maxBlocks = getConfig().getInt("Abilities.Earth.LavaSurge.MaxLavaWaves");
		this.canSourceBeEarth = getConfig().getBoolean("Abilities.Earth.LavaSurge.SourceCanBeEarth");
		this.particleInterval = 100;
		this.fallingBlockInterval = 100;
		
		this.random = new Random();
		this.fallingBlocks = new ArrayList<>();
		this.fracture = new ArrayList<>();
		this.fractureTempBlocks = new ArrayList<>();
		this.movingLava =  new ArrayList<>();
		this.lavaBlocks = new ConcurrentHashMap<>();
		
		if(!isEligible()) {
			return;
		} else if(bPlayer.isOnCooldown(this)) {
			return;
		}
		
		lastTime = System.currentTimeMillis();
	
		if(prepare()) {
			start();
		}
	}
	
	public boolean isEligible() {
		return bPlayer.canBend(this) && bPlayer.canLavabend();
	}
	
	public boolean prepare() {
		Block targetBlock = BlockSource.getEarthSourceBlock(player, prepareRange, ClickType.SHIFT_DOWN);
		
		if(targetBlock == null 
				|| !(targetBlock.getRelative(BlockFace.UP).getType() == Material.AIR) 
				&& !isLava(targetBlock.getRelative(BlockFace.UP))) {
			return false;
		}
		
		LavaSurge otherSurge = getAbility(player, this.getClass());
		if (otherSurge != null) {
			otherSurge.revertFracture();
		}
		
		if((canSourceBeEarth && isEarthbendable(targetBlock)) || isLavabendable(targetBlock)) {
			startLocation = targetBlock.getLocation().add(0, 1, 0);
			sourceBlock = targetBlock;
			return true;
		}
		
		return false;
	}
	
	public void launch() {
		Location targetLocation = GeneralMethods.getTargetedLocation(player, travelRange*2);

		try { 
			targetLocation = GeneralMethods.getTargetedEntity(player, travelRange*2, null).getLocation(); 
		} catch(NullPointerException e) {}
		
		if(targetLocation == null) {
			remove();
			return;
		}
		
		time = System.currentTimeMillis();
		direction = GeneralMethods.getDirection(startLocation, targetLocation).multiply(0.07);
		
		if(direction.getY() < 0) {
			direction.setY(0);
		}
		
		if(canSourceBeEarth) {
			openFracture();
		} else {
			skipFracture();
		}
	}
	
	public void openFracture() {
		List<Block> affectedBlocks = GeneralMethods.getBlocksAroundPoint(sourceBlock.getLocation(), fractureRadius);
		
		for(Block b : affectedBlocks) {
			if(isEarthbendable(b)) {
				fracture.add(b);
			}
		}
	
		listIterator = fracture.listIterator();
		isFractureOpen = true;
		bPlayer.addCooldown(this);
	}
	
	public void skipFracture() {
		listIterator = fracture.listIterator();
		isFractureOpen = true;
	}
	
	public void revertFracture() {
		for(TempBlock tb : fractureTempBlocks) {
			tb.revertBlock();
		}
		fracture.clear();
	}
	
	@Override
	public void remove() {
		super.remove();
		revertFracture();
	}
	
	public boolean canMoveThrough(Block block) {
		if(isTransparent(startLocation.getBlock()) ||
				isEarthbendable(startLocation.getBlock()) ||
				isLavabendable(startLocation.getBlock())) {
			return true;
		}
		return false;
	}

	public void removeLava() {
		for(TempBlock tb : lavaBlocks.values()) {
			tb.revertBlock();
		}
		movingLava.clear();
	}
	
	@Override
	public void progress() {
		long curTime = System.currentTimeMillis();
		if(!player.isOnline() || player.isDead()) {
			remove();
			return;
		} else if(!hasSurgeStarted && !bPlayer.getBoundAbilityName().equals(getName())) {
			remove();
			return;
		}
		
		if(!hasSurgeStarted && sourceBlock != null && curTime > lastTime + particleInterval) {
			lastTime = curTime;
			ParticleEffect.LAVA.display(sourceBlock.getLocation(), 0, 0, 0, 0, 1);
		} else if(hasSurgeStarted && curTime > lastTime + particleInterval) {
			lastTime = curTime;
			for(FallingBlock fblock : fallingBlocks) {
				ParticleEffect.LAVA.display(fblock.getLocation(), 0, 0, 0, 0, 1);
			}
		}
		
		if(isFractureOpen && !hasSurgeStarted) {
			if(!listIterator.hasNext()) {
				hasSurgeStarted = true;
			} else {
				Block b = listIterator.next();
				playEarthbendingSound(b.getLocation());
				
				for(int i = 0; i < 2; i++) {
					TempBlock tb = new TempBlock(b, Material.STATIONARY_LAVA, (byte) 0);
					fractureTempBlocks.add(tb);
				}
			}
		}

		if(hasSurgeStarted) {	
			if(fallingBlocksCount >= maxBlocks) {
				return;
			}
			
			if(curTime > time + (fallingBlockInterval * fallingBlocksCount)) {
				FallingBlock fbs = GeneralMethods.spawnFallingBlock(sourceBlock.getLocation().add(0, 1, 0), 11, (byte) 0);
				fallingBlocks.add(fbs);
				ALL_FALLING_BLOCKS.add(fbs);
				double x = random.nextDouble()/5;
				double z = random.nextDouble()/5;
				
				x = (random.nextBoolean()) ? -x : x;
				z = (random.nextBoolean()) ? -z : z;
				
				fbs.setVelocity(direction.clone().add(new Vector(x, 0.2, z)).multiply(1.2));
				fbs.setDropItem(false);
				
				for(Block b : fracture) {
					if(random.nextBoolean() && b != sourceBlock) {
						FallingBlock fb = GeneralMethods.spawnFallingBlock(b.getLocation().add(new Vector(0, 1, 0)), 11, (byte) 0);
						ALL_FALLING_BLOCKS.add(fb);
						fallingBlocks.add(fb);
						fb.setVelocity(direction.clone().add(new Vector(random.nextDouble()/10, 0.1, random.nextDouble()/10)).multiply(1.2));
						fb.setDropItem(false);
					}
				}

				fallingBlocksCount++;
			}
			
			for(FallingBlock fb : fallingBlocks) {
				for(Entity e : GeneralMethods.getEntitiesAroundPoint(fb.getLocation(), 2)) {
					if(e instanceof LivingEntity) {
						if(e.getEntityId() != player.getEntityId()) {
							DamageHandler.damageEntity(e, impactDamage, this);
							e.setFireTicks(100);
							GeneralMethods.setVelocity(e, direction.clone());
						}
					}
				}
			}
		}
	}

	@Override
	public String getName() {
		return null; // disabled
	}

	@Override
	public Location getLocation() {
		return startLocation;
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
	
	public static HashSet<FallingBlock> getAllFallingBlocks() {
		return ALL_FALLING_BLOCKS;
	}
	
	public boolean isHasSurgeStarted() {
		return hasSurgeStarted;
	}

	public void setHasSurgeStarted(boolean hasSurgeStarted) {
		this.hasSurgeStarted = hasSurgeStarted;
	}

	public boolean isFractureOpen() {
		return isFractureOpen;
	}

	public void setFractureOpen(boolean isFractureOpen) {
		this.isFractureOpen = isFractureOpen;
	}

	public boolean isCanSourceBeEarth() {
		return canSourceBeEarth;
	}

	public void setCanSourceBeEarth(boolean canSourceBeEarth) {
		this.canSourceBeEarth = canSourceBeEarth;
	}

	public int getFallingBlocksCount() {
		return fallingBlocksCount;
	}

	public void setFallingBlocksCount(int fallingBlocksCount) {
		this.fallingBlocksCount = fallingBlocksCount;
	}

	public int getMaxBlocks() {
		return maxBlocks;
	}

	public void setMaxBlocks(int maxBlocks) {
		this.maxBlocks = maxBlocks;
	}

	public int getParticleInterval() {
		return particleInterval;
	}

	public void setParticleInterval(int particleInterval) {
		this.particleInterval = particleInterval;
	}

	public int getFallingBlockInterval() {
		return fallingBlockInterval;
	}

	public void setFallingBlockInterval(int fallingBlockInterval) {
		this.fallingBlockInterval = fallingBlockInterval;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getLastTime() {
		return lastTime;
	}

	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}

	public double getImpactDamage() {
		return impactDamage;
	}

	public void setImpactDamage(double impactDamage) {
		this.impactDamage = impactDamage;
	}

	public double getFractureRadius() {
		return fractureRadius;
	}

	public void setFractureRadius(double fractureRadius) {
		this.fractureRadius = fractureRadius;
	}

	public double getPrepareRange() {
		return prepareRange;
	}

	public void setPrepareRange(double prepareRange) {
		this.prepareRange = prepareRange;
	}

	public double getTravelRange() {
		return travelRange;
	}

	public void setTravelRange(double travelRange) {
		this.travelRange = travelRange;
	}

	public Block getSourceBlock() {
		return sourceBlock;
	}

	public void setSourceBlock(Block sourceBlock) {
		this.sourceBlock = sourceBlock;
	}

	public Random getRandom() {
		return random;
	}

	public void setRandom(Random random) {
		this.random = random;
	}

	public Vector getDirection() {
		return direction;
	}

	public void setDirection(Vector direction) {
		this.direction = direction;
	}

	public Location getStartLocation() {
		return startLocation;
	}

	public void setStartLocation(Location startLocation) {
		this.startLocation = startLocation;
	}

	public ListIterator<Block> getListIterator() {
		return listIterator;
	}

	public void setListIterator(ListIterator<Block> listIterator) {
		this.listIterator = listIterator;
	}

	public ArrayList<FallingBlock> getFallingBlocks() {
		return fallingBlocks;
	}

	public ArrayList<Block> getFracture() {
		return fracture;
	}

	public ArrayList<TempBlock> getFractureTempBlocks() {
		return fractureTempBlocks;
	}

	public ArrayList<TempBlock> getMovingLava() {
		return movingLava;
	}

	public Map<FallingBlock, TempBlock> getLavaBlocks() {
		return lavaBlocks;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}
	
}
