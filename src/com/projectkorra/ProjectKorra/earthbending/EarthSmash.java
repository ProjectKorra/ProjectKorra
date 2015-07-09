package com.projectkorra.ProjectKorra.earthbending;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.TempBlock;
import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.Utilities.ClickType;
import com.projectkorra.ProjectKorra.Utilities.ParticleEffect;
import com.projectkorra.ProjectKorra.airbending.AirMethods;
import com.projectkorra.ProjectKorra.waterbending.WaterMethods;

public class EarthSmash {
	public static enum State {
		START, LIFTING, LIFTED, GRABBED, SHOT, FLYING, REMOVED
	}
	
	public static ArrayList<EarthSmash> instances = new ArrayList<EarthSmash>();
	public static boolean ALLOW_GRAB = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Earth.EarthSmash.AllowGrab");
	public static boolean ALLOW_SHOOTING = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Earth.EarthSmash.AllowShooting");
	public static boolean ALLOW_FLIGHT = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Earth.EarthSmash.AllowFlight");
	public static double GRAB_RANGE = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.EarthSmash.GrabRange");
	public static double TRAVEL_RANGE = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.EarthSmash.ShotRange");
	public static double SHOOTING_DAMAGE = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.EarthSmash.Damage");

	public static double KNOCKBACK_POWER = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.EarthSmash.Knockback");
	public static double KNOCKUP_POWER = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.EarthSmash.Knockup");
	public static double FLYING_PLAYER_SPEED = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.EarthSmash.FlightSpeed");
	public static long CHARGE_TIME = ProjectKorra.plugin.getConfig().getLong("Abilities.Earth.EarthSmash.ChargeTime");
	public static long MAIN_COOLDOWN = ProjectKorra.plugin.getConfig().getLong("Abilities.Earth.EarthSmash.Cooldown");
	public static long FLYING_REMOVE_TIMER = ProjectKorra.plugin.getConfig().getLong("Abilities.Earth.EarthSmash.FlightTimer");
	public static long REMOVE_TIMER = ProjectKorra.plugin.getConfig().getLong("Abilities.Earth.EarthSmash.RemoveTimer");
	
	private static int REQUIRED_BENDABLE_BLOCKS = 11;
	private static int MAX_BLOCKS_TO_PASS_THROUGH = 3;
	private static double GRAB_DETECTION_RADIUS = 2.5;
	private static double FLIGHT_DETECTION_RADIUS = 3.8;
	private static long SHOOTING_ANIMATION_COOLDOWN = 25;
	private static long FLYING_ANIMATION_COOLDOWN = 0;
	private static long LIFT_ANIMATION_COOLDOWN = 30;
	
	private Player player;
	private BendingPlayer bplayer;
	private Block origin;
	private Location loc, destination;
	public State state = State.START;
	private int animCounter, progressCounter;
	private long time, delay, cooldown, flightRemove, flightStart;
	private double grabbedRange;
	private double grabRange, chargeTime, damage, knockback, knockup, flySpeed, shootRange;
	private ArrayList<Entity> affectedEntities = new ArrayList<Entity>();
	private ArrayList<BlockRepresenter> currentBlocks = new ArrayList<BlockRepresenter>();
	private ArrayList<TempBlock> affectedBlocks = new ArrayList<TempBlock>();
	
	public EarthSmash(Player player, ClickType type) {
		if(!GeneralMethods.hasPermission(player, "EarthSmash"))
			return;
		this.player = player;
		bplayer = GeneralMethods.getBendingPlayer(player.getName());
		this.time = System.currentTimeMillis();
		
		if(type == ClickType.SHIFT_DOWN || type == ClickType.SHIFT_UP && !player.isSneaking()) {		
			grabRange = GRAB_RANGE;
			chargeTime = CHARGE_TIME;
			cooldown = MAIN_COOLDOWN;
			damage = SHOOTING_DAMAGE;
			knockback = KNOCKBACK_POWER;
			knockup = KNOCKUP_POWER;
			flySpeed = FLYING_PLAYER_SPEED;
			flightRemove = FLYING_REMOVE_TIMER;
			shootRange = TRAVEL_RANGE;
			if(AvatarState.isAvatarState(player)) {
				grabRange = AvatarState.getValue(grabRange);
				chargeTime = AvatarState.getValue(chargeTime);
				cooldown = 0;
				damage = AvatarState.getValue(damage);
				knockback = AvatarState.getValue(knockback);
				knockup = AvatarState.getValue(knockup);
				flySpeed = AvatarState.getValue(flySpeed);
				flightRemove = Integer.MAX_VALUE;
				shootRange = AvatarState.getValue(shootRange);
			}
			
			EarthSmash flySmash = flyingInSmashCheck(player);
			if(flySmash != null) {
				flySmash.state = State.FLYING;
				flySmash.player = player;
				flySmash.flightStart = System.currentTimeMillis();
				return;
			}
			
			EarthSmash grabbedSmash = aimingAtSmashCheck(player, State.LIFTED);
			if(grabbedSmash == null)
			grabbedSmash = aimingAtSmashCheck(player, State.SHOT);
			if(grabbedSmash != null) {
				grabbedSmash.state = State.GRABBED;
				grabbedSmash.grabbedRange = grabbedSmash.loc.distance(player.getEyeLocation());
				grabbedSmash.player = player;
				return;
			}
		}
		else if(type == ClickType.LEFT_CLICK && player.isSneaking()) {
			for(EarthSmash smash : instances) {
				if(smash.state == State.GRABBED && smash.player == player) {
					smash.state = State.SHOT;
					smash.destination = player.getEyeLocation().clone().add
							(player.getEyeLocation().getDirection().normalize().multiply(smash.shootRange));
					smash.loc.getWorld().playEffect(smash.loc, Effect.GHAST_SHOOT, 0, 10);
				}
			}
			return;
		}
		else if(type == ClickType.RIGHT_CLICK && player.isSneaking()) {
			EarthSmash grabbedSmash = aimingAtSmashCheck(player, State.GRABBED);
			if(grabbedSmash != null) {
				player.teleport(grabbedSmash.loc.clone().add(0, 2, 0));
				grabbedSmash.state = State.FLYING;
				grabbedSmash.player = player;
				grabbedSmash.flightStart = System.currentTimeMillis();
			}
			return;
		}
		else {
			return;
		}
		instances.add(this);
	}
	
	public void progress() {
		progressCounter++;
		if(state == State.LIFTED && REMOVE_TIMER > 0 && System.currentTimeMillis() - time > REMOVE_TIMER) {
			remove();
			return;
		}
		if(state == State.START || state == State.FLYING || state == State.GRABBED) {
			if(player.isDead() || !player.isOnline()) {
				remove();
				return;
			}
		}
		else if(state == State.START) {
			String ability = GeneralMethods.getBoundAbility(player);
			if(ability == null || !ability.equalsIgnoreCase("EarthSmash") || bplayer.isOnCooldown("EarthSmash")) {
				remove();
				return;
			}
		}
		else if(state == State.START || state == State.FLYING || state == State.GRABBED) {
			if(!GeneralMethods.canBend(player.getName(), "EarthSmash")) {
				remove();
				return;
			}
		}
		
		if(state == State.START && progressCounter > 1) {
			if(!player.isSneaking()) {
				if(System.currentTimeMillis() - time > chargeTime) {
					origin = EarthMethods.getEarthSourceBlock(player, grabRange);
					if(origin == null){
						remove();
						return;
					}
					bplayer.addCooldown("EarthSmash", cooldown);
					loc = origin.getLocation();
					state = State.LIFTING;
				}
				else {
					remove();
					return;
				}
			}
			else if(System.currentTimeMillis() - time > chargeTime) {
				Location tempLoc = player.getEyeLocation().add(player.getEyeLocation()
						.getDirection().normalize().multiply(1.2));
				tempLoc.add(0, 0.3, 0);
				ParticleEffect.SMOKE.display(tempLoc, 0.3F, 0.1F, 0.3F, 0, 4); 
			}
		}
		else if(state == State.LIFTING) {
			if(System.currentTimeMillis() - delay >= LIFT_ANIMATION_COOLDOWN) {
				delay = System.currentTimeMillis();
				animateLift();
			}
		}
		else if(state == State.GRABBED) {
			if(player.isSneaking()) {
				revert();
				Location oldLoc = loc.clone();
				loc = player.getEyeLocation().add(
						player.getEyeLocation().getDirection().normalize().multiply(grabbedRange));
				
				//Check to make sure the new location is available to move to
				for(Block block : getBlocks())
					if(block.getType() != Material.AIR && !EarthMethods.isTransparentToEarthbending(player, block)) {
						loc = oldLoc;
						break;
					}
				WaterMethods.removeWaterSpouts(loc, 2, player);
				AirMethods.removeAirSpouts(loc, 2, player);
				draw();
				return;
			}
			else {
				state = State.LIFTED;
				return;
			}
		}
		else if(state == State.SHOT) {
			if(System.currentTimeMillis() - delay >= SHOOTING_ANIMATION_COOLDOWN) {
				delay = System.currentTimeMillis();
				if(GeneralMethods.isRegionProtectedFromBuild(player, "EarthSmash", loc)) {
					remove();
					return;
				}
				revert();
				loc.add(GeneralMethods.getDirection(loc, destination).normalize().multiply(1));
				if(loc.distanceSquared(destination) < 4) {
					remove();
					return;
				}
				// If an earthsmash runs into too many blocks we should remove it
				int badBlocksFound = 0;
				for(Block block : getBlocks())
					if(block.getType() != Material.AIR && 
						(!EarthMethods.isTransparentToEarthbending(player, block) || block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER))
						badBlocksFound++;
				
				if(badBlocksFound > MAX_BLOCKS_TO_PASS_THROUGH) {
					remove();
					return;
				}
				WaterMethods.removeWaterSpouts(loc, 2, player);
				AirMethods.removeAirSpouts(loc, 2, player);
				shootingCollisionDetection();
				draw();
				smashToSmashCollisionDetection();
			}
			return;
		}
		else if(state == State.FLYING) {
			if(!player.isSneaking()){
				remove();
				return;
			}
			else if(System.currentTimeMillis() - delay >= FLYING_ANIMATION_COOLDOWN)
			{
				delay = System.currentTimeMillis();
				if(GeneralMethods.isRegionProtectedFromBuild(player, "EarthSmash", loc)) {
					remove();
					return;
				}
				revert();
				destination = player.getEyeLocation().clone().add
						(player.getEyeLocation().getDirection().normalize().multiply(shootRange));
				Vector direction = GeneralMethods.getDirection(loc, destination).normalize();
				
				List<Entity> entities = GeneralMethods.getEntitiesAroundPoint(loc.clone().add(0,2,0), FLIGHT_DETECTION_RADIUS);
				if(entities.size() == 0){
					remove();
					return;
				}
				for(Entity entity : entities)
					entity.setVelocity(direction.clone().multiply(flySpeed));
				
				//These values tend to work well when dealing with a person aiming upward or downward.
				if(direction.getY() < -0.35)
					loc = player.getLocation().clone().add(0,-3.2,0);
				else if(direction.getY() > 0.35)
					loc = player.getLocation().clone().add(0,-1.7,0);
				else
					loc = player.getLocation().clone().add(0,-2.2,0);
				draw();
			}
			if(System.currentTimeMillis() - flightStart > flightRemove){
				remove();
				return;
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public void animateLift()
	{
		/**
		 * Begins animating the EarthSmash from the ground. The lift animation consists
		 * of 3 steps, and each one has to design the shape in the ground that removes the
		 * earthbendable material. We also need to make sure that there is a clear path for
		 * the EarthSmash to rise, and that there is enough earthbendable material for it to be created.
		 */
		if(animCounter < 4) {
			revert();
			loc.add(0,1,0);
			//Remove the blocks underneath the rising smash
			if(animCounter == 0) {
				//Check all of the blocks and make sure that they can be removed AND make sure there is enough dirt
				int totalBendableBlocks = 0;
				for(int x = -1; x <= 1; x++)
					for(int y = -2; y <= -1; y++)
						for(int z = -1; z <= 1; z++) {
							Block block = loc.clone().add(x,y,z).getBlock();
							if(GeneralMethods.isRegionProtectedFromBuild(player, "EarthSmash", block.getLocation())) {
								remove();
								return;
							}
							if(isEarthbendableMaterial(block.getType()))
								totalBendableBlocks++;
						}
				if(totalBendableBlocks < REQUIRED_BENDABLE_BLOCKS) {
					remove();
					return;
				}
				//Make sure there is a clear path upward otherwise remove
				for(int y = 0; y <= 3; y++) {
					Block tempBlock = loc.clone().add(0,y,0).getBlock();
					if(!EarthMethods.isTransparentToEarthbending(player, tempBlock) && tempBlock.getType() != Material.AIR) {
						remove();
						return;
					}
				}
				//Design what this EarthSmash looks like by using BlockRepresenters
				Location tempLoc = loc.clone().add(0,-2,0);
				for(int x = -1; x <= 1; x++)
					for(int y = -1; y <= 1; y++)
						for(int z = -1; z <= 1; z++)
							if((Math.abs(x) + Math.abs(y) + Math.abs(z)) % 2 == 0) {
								Block block = tempLoc.clone().add(x,y,z).getBlock();
								currentBlocks.add(new BlockRepresenter(x, y, z, selectMaterialForRepresenter(block.getType()), block.getData()));
							}
							
				//Remove the design of the second level of removed dirt
				for(int x = -1; x <= 1; x++)
					for(int z = -1; z <= 1; z++) {
						if((Math.abs(x) + Math.abs(z)) % 2 == 1) {
							Block block = loc.clone().add(x,-2,z).getBlock();
							if(isEarthbendableMaterial(block.getType()))
								EarthMethods.addTempAirBlock(block);
						}	
						
						//Remove the first level of dirt
						Block block = loc.clone().add(x,-1,z).getBlock();
						if(isEarthbendableMaterial(block.getType()))
								EarthMethods.addTempAirBlock(block);
						
					}
				/*
				 * We needed to calculate all of the blocks based on the location being 1 above the initial
				 * bending block, however we want to animate it starting from the original bending block.
				 * We must readjust the location back to what it originally was.
				 */
				loc.add(0,-1,0);

			}
			//Move any entities that are above the rock
			List<Entity> entities = GeneralMethods.getEntitiesAroundPoint(loc, 2.5);
			for(Entity entity : entities) {
				org.bukkit.util.Vector velocity = entity.getVelocity();
				entity.setVelocity(velocity.add(new Vector(0,0.36,0)));
			}
			loc.getWorld().playEffect(loc, Effect.GHAST_SHOOT, 0, 7);
			draw();
		}
		else {
			state = State.LIFTED;
		}
		animCounter++;
	}
	
	public void draw() {
		/**
		 * Redraws the blocks for this instance of EarthSmash.
		 */
		if(currentBlocks.size() == 0) {
			remove();
			return;
		}
		for(BlockRepresenter blockRep : currentBlocks) {
			Block block = loc.clone().add(blockRep.getX(),blockRep.getY(),blockRep.getZ()).getBlock();
			if(player != null && EarthMethods.isTransparentToEarthbending(player,block)) {
				affectedBlocks.add(new TempBlock(block, blockRep.getType(), blockRep.getData()));
				EarthMethods.tempNoEarthbending.add(block);
			}
		}
	}	
	
	public void revert() {
		checkRemainingBlocks();
		for(int i = 0; i < affectedBlocks.size(); i++) {
			TempBlock tblock = affectedBlocks.get(i);
			EarthMethods.tempNoEarthbending.remove(tblock.getBlock());
			tblock.revertBlock();
			affectedBlocks.remove(i);
			i--;
		}
	}
	
	public void checkRemainingBlocks() {
		/**
		 * Checks to see which of the blocks are still attached to the
		 * EarthSmash, remember that blocks can be broken or used in other abilities
		 * so we need to double check and remove any that are not still attached.
		 * 
		 * Also when we remove the blocks from instances, movedearth, or tempair
		 * we should do it on a delay because tempair takes a couple seconds before
		 * the block shows up in that map.
		 */
		for(int i = 0; i < currentBlocks.size(); i++) {
			BlockRepresenter brep = currentBlocks.get(i);
			final Block block = loc.clone().add(brep.getX(), brep.getY(), brep.getZ()).getBlock();
			// Check for grass because sometimes the dirt turns into grass.
			if(block.getType() != brep.getType() 
					&& (block.getType() != Material.GRASS)
					&& (block.getType() != Material.COBBLESTONE)) {
				currentBlocks.remove(i);
				i--;
			}
		}
	}
	
	public void remove() {
		state = State.REMOVED;
		revert();
		instances.remove(this);
	}
	
	public List<Block> getBlocks() {
		/**
		 * Gets the blocks surrounding the EarthSmash's loc.
		 * This method ignores the blocks that should be Air, and only returns the ones that are dirt.
		 */
		List<Block> blocks = new ArrayList<Block>();
		for(int x = -1; x <= 1; x++)
			for(int y = -1; y <= 1; y++)
				for(int z = -1; z <= 1; z++)
					if((Math.abs(x) + Math.abs(y) + Math.abs(z)) % 2 == 0) //Give it the cool shape
						if(loc != null)
							blocks.add(loc.getWorld().getBlockAt(loc.clone().add(x,y,z)));
		return blocks;
	}
	
	public List<Block> getBlocksIncludingInner() {
		/**
		 * Gets the blocks surrounding the EarthSmash's loc.
		 * This method returns all the blocks surrounding the loc, including dirt and air.
		 */
		List<Block> blocks = new ArrayList<Block>();
		for(int x = -1; x <= 1; x++)
			for(int y = -1; y <= 1; y++)
				for(int z = -1; z <= 1; z++)
					if(loc != null)
						blocks.add(loc.getWorld().getBlockAt(loc.clone().add(x,y,z)));
		return blocks;
	}
	
	public static Material selectMaterial(Material mat) {
		/**
		 * Switches the Sand Material and Gravel to SandStone and stone respectively,
		 * since gravel and sand cannot be bent due to gravity.
		 */
		if(mat == Material.SAND) return Material.SANDSTONE;
		else if(mat == Material.GRAVEL) return Material.STONE;
		else return mat;
	}
	
	public Material selectMaterialForRepresenter(Material mat) {
		Material tempMat = selectMaterial(mat);
		Random rand = new Random();
		if(!isEarthbendableMaterial(tempMat)) {
			if(currentBlocks.size() < 1)
				return Material.DIRT;
			else 
				return currentBlocks.get(rand.nextInt(currentBlocks.size())).getType();
		}
		return tempMat;
	}
	
	private EarthSmash aimingAtSmashCheck(Player player, State reqState) {
		/**
		 * Determines if a player is trying to grab an EarthSmash.
		 * A player is trying to grab an EarthSmash if they are staring at it and holding shift.
		 */
		if(!ALLOW_GRAB) 
			return null;
		@SuppressWarnings("deprecation")
		List<Block> blocks = player.getLineOfSight(EarthMethods.getTransparentEarthbending(), (int) Math.round(grabRange));
		for(EarthSmash smash : instances) {
			if(reqState == null || smash.state == reqState)
				for(Block block : blocks)
					if(block.getLocation().getWorld() == smash.loc.getWorld() && 
					block.getLocation().distanceSquared(smash.loc) <= Math.pow(GRAB_DETECTION_RADIUS, 2))
						return smash;
		}
		return null;	
	}
	
	public void shootingCollisionDetection() {
		/**
		 * This method handles any collision between an EarthSmash and the surrounding entities,
		 * the method only applies to earthsmashes that have already been shot.
		 */
		List<Entity> entities = GeneralMethods.getEntitiesAroundPoint(loc, FLIGHT_DETECTION_RADIUS);
		for(Entity entity : entities)
			if(entity instanceof LivingEntity 
					&& entity != player
					&& !affectedEntities.contains(entity)) {
				affectedEntities.add(entity);
				double damage = currentBlocks.size() / 13 * this.damage;
				GeneralMethods.damageEntity(player, entity, damage);
				Vector travelVec = GeneralMethods.getDirection(loc, entity.getLocation());
				entity.setVelocity(travelVec.setY(knockup).normalize().multiply(knockback));
			}
	}	
	public void smashToSmashCollisionDetection() {
		/**
		 * EarthSmash to EarthSmash collision can only happen when one of the Smashes have
		 * been shot by a player. If we find out that one of them have collided then we want to return
		 * since a smash can only remove 1 at a time.
		 */
		for(int i = 0; i < instances.size(); i++) {
			EarthSmash smash = instances.get(i);
			if(smash.loc != null) {
				if(smash != this && smash.loc.getWorld() == loc.getWorld() &&smash.loc.distanceSquared(loc) < Math.pow(FLIGHT_DETECTION_RADIUS, 2)) {
					smash.remove();
					remove();
					i-=2;
					return;
				}
			}
		}
	}
	
	private static EarthSmash flyingInSmashCheck(Player player) {
		/**
		 * Determines whether or not a player is trying to fly ontop of an EarthSmash.
		 * A player is considered "flying" if they are standing ontop of the earthsmash and holding shift.
		 */
		if(!ALLOW_FLIGHT)
			return null;
		
		for(EarthSmash smash : instances) {
			//Check to see if the player is standing on top of the smash.
			if(smash.state == State.LIFTED) {
				if(smash.loc.getWorld().equals(player.getWorld()) 
						&& smash.loc.clone().add(0,2,0).distanceSquared(player.getLocation()) <= Math.pow(FLIGHT_DETECTION_RADIUS, 2)) {
					return smash;
				}
			}
		}
		return null;
	}
	
	public static boolean isEarthbendableMaterial(Material mat) {
		for (String s : ProjectKorra.plugin.getConfig().getStringList("Properties.Earth.EarthbendableBlocks")) {
			if (mat == Material.getMaterial(s))
				return true;
		}
		return false;
	}
	
	public static void progressAll() {
		for(int i = 0; i < instances.size(); i++)
			instances.get(i).progress();
	}
	
	public static void removeAll() {
		for(int i = 0; i < instances.size(); i++) {
			instances.get(i).remove();
			i--;
		}
	}
	
	public class BlockRepresenter {
		/**
		 * A BlockRepresenter is used to keep track of each of
		 * the individual types of blocks that are attached to an EarthSmash.
		 * Without the representer then an EarthSmash can only be made up of 1 material
		 * at a time. For example, an ESmash that is entirely dirt, coalore, or sandstone.
		 * Using the representer will allow all the materials to be mixed together.
		 */
		private int x, y, z;
		private Material type;
		private byte data;
		public BlockRepresenter(int x, int y, int z, Material type, byte data) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.type = type;
			this.data = data;
		}
		public int getX() {
			return x;
		}
		
		public int getY() {
			return y;
		}
		
		public int getZ() {
			return z;
		}
		
		public Material getType() {
			return type;
		}
		
		public byte getData() {
			return data;
		}
		
		public void setX(int x) {
			this.x = x;
		}
		
		public void setY(int y) {
			this.y = y;
		}
		
		public void setZ(int z) {
			this.z = z;
		}
		
		public void setType(Material type) {
			this.type = type;
		}
		
		public void setData(byte data) {
			this.data = data;
		}
		
		public String toString() {
			return x + ", " + y + ", " + z + ", " + type.toString(); 
		}
	}
	
	public class Pair<F, S> {
	    private F first; //first member of pair
	    private S second; //second member of pair

	    public Pair(F first, S second) {
	        this.first = first;
	        this.second = second;
	    }

	    public void setFirst(F first) {
	        this.first = first;
	    }

	    public void setSecond(S second) {
	        this.second = second;
	    }

	    public F getFirst() {
	        return first;
	    }

	    public S getSecond() {
	        return second;
	    }
	}
	
	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public long getCooldown() {
		return cooldown;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
		if(player != null)
			bplayer.addCooldown("EarthSmash", cooldown);
	}

	public double getGrabRange() {
		return grabRange;
	}

	public void setGrabRange(double grabRange) {
		this.grabRange = grabRange;
	}

	public double getChargeTime() {
		return chargeTime;
	}

	public void setChargeTime(double chargeTime) {
		this.chargeTime = chargeTime;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public double getKnockback() {
		return knockback;
	}

	public void setKnockback(double knockback) {
		this.knockback = knockback;
	}

	public double getKnockup() {
		return knockup;
	}

	public void setKnockup(double knockup) {
		this.knockup = knockup;
	}

	public double getFlySpeed() {
		return flySpeed;
	}

	public void setFlySpeed(double flySpeed) {
		this.flySpeed = flySpeed;
	}

	public double getShootRange() {
		return shootRange;
	}

	public void setShootRange(double shootRange) {
		this.shootRange = shootRange;
	}

	public long getFlightRemove() {
		return flightRemove;
	}

	public void setFlightRemove(long flightRemove) {
		this.flightRemove = flightRemove;
	}
}
