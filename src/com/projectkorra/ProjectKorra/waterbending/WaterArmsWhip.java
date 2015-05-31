package com.projectkorra.ProjectKorra.waterbending;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.BendingManager;
import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.Commands;
import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.MultiAbilityManager;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.TempBlock;
import com.projectkorra.ProjectKorra.earthbending.EarthMethods;
import com.projectkorra.ProjectKorra.waterbending.WaterArms.Arm;
import com.projectkorra.rpg.WorldEvents;

public class WaterArmsWhip{

	/**
	 * Whip Enum value for deciding what ability should be executed.
	 */
	public enum Whip {
		Pull, Punch, Grapple, Grab;
	}

	private static FileConfiguration config = ProjectKorra.plugin.getConfig();

	public static ConcurrentHashMap<Integer, WaterArmsWhip> instances = new ConcurrentHashMap<Integer, WaterArmsWhip>();
	public static ArrayList<LivingEntity> grabbedEntities = new ArrayList<LivingEntity>();

	private Player player;
	private WaterArms waterArms;

	private int whipLength = config.getInt("Abilities.Water.WaterArms.WhipMode.MaxLength");
	private int whipLengthWeak = config.getInt("Abilities.Water.WaterArms.WhipMode.MaxLengthWeak");

	private int whipLengthNight = config.getInt("Abilities.Water.WaterArms.WhipMode.NightAugments.MaxLength.Normal");
	private int whipLengthFullMoon = config.getInt("Abilities.Water.WaterArms.WhipMode.NightAugments.MaxLength.FullMoon");

	private int initLength = config.getInt("Abilities.Water.WaterArms.Arms.InitialLength");
	private double damage = config.getDouble("Abilities.Water.WaterArms.WhipMode.Punch.PunchDamage");
	private boolean pullBlocks = config.getBoolean("Abilities.Water.WaterArms.WhipMode.Pull.PullBlocks");
	private long pullBlocksRevertDelay = config.getLong("Abilities.Water.WaterArms.WhipMode.Pull.BlockRevertDelay");
	private boolean grappleRespectRegions = config.getBoolean("Abilities.Water.WaterArms.WhipMode.Grapple.RespectRegions");
	private boolean usageCooldownEnabled = config.getBoolean("Abilities.Water.WaterArms.Arms.Cooldowns.UsageCooldownEnabled");
	private long usageCooldown = config.getLong("Abilities.Water.WaterArms.Arms.Cooldowns.UsageCooldown");

	private int activeLength = initLength;
	private int whipSpeed = 2;
	private boolean reverting = false;
	private boolean damaged = false;
	private boolean grappled = false;
	private boolean grabbed = false;
	private LivingEntity grabbedEntity;
	private Location end;
	private Arm arm;
	private Whip ability;

	private int id;
	private static int ID = Integer.MIN_VALUE;

	public WaterArmsWhip(Player player, Whip m){
		this.player = player;
		ability = m;
		getNightAugments();
		createInstance();
	}

	private void getNightAugments(){
		World world = player.getWorld();
		if(WaterMethods.isNight(world)){
			if(GeneralMethods.hasRPG()){
				if(BendingManager.events.get(world).equalsIgnoreCase(WorldEvents.LunarEclipse.toString())){
					whipLength = whipLengthFullMoon;
				}else if (BendingManager.events.get(world).equalsIgnoreCase("FullMoon")){
					whipLength = whipLengthFullMoon;
				}else{
					whipLength = whipLengthNight;
				}
			}else{
				if(WaterMethods.isFullMoon(world)){
					whipLength = whipLengthFullMoon;
				}else{
					whipLength = whipLengthNight;
				}
			}
		}
	}

	private void createInstance(){
		if(WaterArms.instances.containsKey(player)){
			waterArms = WaterArms.instances.get(player);
			waterArms.switchPreferredArm();
			arm = waterArms.getActiveArm();
			BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
			if(arm.equals(Arm.Left)){
				if(waterArms.isLeftArmCooldown() || bPlayer.isOnCooldown("WaterArms_LEFT")){
					return;
				}else{
					if(usageCooldownEnabled)
						bPlayer.addCooldown("WaterArms_LEFT", usageCooldown);
					waterArms.setLeftArmCooldown(true);
				}
			}
			if(arm.equals(Arm.Right)){
				if(waterArms.isRightArmCooldown() || bPlayer.isOnCooldown("WaterArms_RIGHT")){
					return;
				}else{
					if(usageCooldownEnabled)
						bPlayer.addCooldown("WaterArms_RIGHT", usageCooldown);
					waterArms.setRightArmCooldown(true);
				}
			}
		}else{
			return;
		}
		if(!waterArms.isFullSource()){
			whipLength = whipLengthWeak;
		}
		id = ID;
		instances.put(id, this);
		if (ID == Integer.MAX_VALUE)
			ID = Integer.MIN_VALUE;
		ID++;
	}

	private void progress(){
		if(!WaterArms.instances.containsKey(player)){
			remove();
			return;
		}
		if(player.isDead() || !player.isOnline()){
			remove();
			return;
		}
		if(!MultiAbilityManager.hasMultiAbilityBound(player, "WaterArms")){
			remove();
			return;
		}

		if(activeLength < whipLength && !reverting){
			activeLength+=whipSpeed;
		}else if(activeLength > initLength){
			if(!grabbed){
				activeLength-=whipSpeed;
			}
		}else{
			remove();
			return;
		}

		if(activeLength == whipLength && !grabbed){
			reverting = true;
		}

		if(arm.equals(Arm.Left)){
			useLeftArm();
		}else{
			useRightArm();
		}
		dragEntity(end);
		grapplePlayer(end);
	}

	//START OF RECODE

	private boolean canPlaceBlock(Block block){
		if(!EarthMethods.isTransparentToEarthbending(player, block) && !(WaterMethods.isWater(block) && TempBlock.isTempBlock(block))){
			return false;
		}
		return true;
	}

	private void useLeftArm(){
		if(waterArms.displayLeftArm()){
			Location l1 = waterArms.getLeftArmEnd().clone();
			Vector dir = player.getLocation().getDirection();
			for(int i = 1; i <= activeLength; i++){
				Location l2 = l1.clone().add(dir.normalize().multiply(i));

				if(!canPlaceBlock(l2.getBlock())){
					if(!l2.getBlock().getType().equals(Material.BARRIER)){
						grappled = true;
					}
					reverting = true;
					break;
				}

				new TempBlock(l2.getBlock(), Material.STATIONARY_WATER, (byte) 0);
				WaterArms.revert.put(l2.getBlock(), 0L);

				if(i == activeLength){
					Location l3 = GeneralMethods.getRightSide(l2, 1);
					end = l3.clone();
					if(canPlaceBlock(l3.getBlock())){
						new TempBlock(l3.getBlock(), Material.STATIONARY_WATER, (byte) 3);
						WaterArms.revert.put(l3.getBlock(), 0L);
						checkLocation(l3);
					}else{
						if(!l3.getBlock().getType().equals(Material.BARRIER)){
							grappled = true;
						}
						reverting = true;
					}
				}
			}
		}
	}
	
	private void useRightArm(){
		if(waterArms.displayLeftArm()){
			Location l1 = waterArms.getRightArmEnd().clone();
			Vector dir = player.getLocation().getDirection();
			for(int i = 1; i <= activeLength; i++){
				Location l2 = l1.clone().add(dir.normalize().multiply(i));

				if(!canPlaceBlock(l2.getBlock())){
					if(!l2.getBlock().getType().equals(Material.BARRIER)){
						grappled = true;
					}
					reverting = true;
					break;
				}

				new TempBlock(l2.getBlock(), Material.STATIONARY_WATER, (byte) 0);
				WaterArms.revert.put(l2.getBlock(), 0L);

				if(i == activeLength){
					Location l3 = GeneralMethods.getLeftSide(l2, 1);
					end = l3.clone();
					if(canPlaceBlock(l3.getBlock())){
						new TempBlock(l3.getBlock(), Material.STATIONARY_WATER, (byte) 3);
						WaterArms.revert.put(l3.getBlock(), 0L);
						checkLocation(l3);
					}else{
						if(!l3.getBlock().getType().equals(Material.BARRIER)){
							grappled = true;
						}
						reverting = true;
					}
				}
			}
		}
	}

	private void checkLocation(Location location){
		Location endOfArm = waterArms.getLeftArmEnd().clone();
		switch(ability){
		case Pull:
			for(Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2)){
				if(entity instanceof Player && Commands.invincible.contains(((Player) entity).getName())){
					continue;
				}
				Vector vector = endOfArm.toVector().subtract(entity.getLocation().toVector());
				entity.setVelocity(vector.multiply(0.15));
			}
			break;
		case Punch:
			for(Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2)){
				if(entity instanceof Player && Commands.invincible.contains(((Player) entity).getName())){
					continue;
				}
				Vector vector = entity.getLocation().toVector().subtract(endOfArm.toVector());
				entity.setVelocity(vector.multiply(0.15));
				if(entity instanceof LivingEntity){
					if(entity.getEntityId() != player.getEntityId()){
						damaged = true;
						GeneralMethods.damageEntity(player, entity, damage);
					}
				}
			}
			break;
		case Grapple:
			grapplePlayer(end);
			break;
		case Grab:
			if(grabbedEntity == null){
				for(Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2)){
					if(entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId() && !grabbedEntities.contains(entity)){
						System.out.println("Grabbed Entity!");
						grabbedEntities.add((LivingEntity) entity);
						grabbedEntity = (LivingEntity) entity;
						grabbed = true;
						reverting = true;
						waterArms.setActiveArmCooldown(true);
						break;
					}
				}
			}
			break;
		default:
			break;
		}
	}

	private void dragEntity(Location location){
		if(grabbedEntity != null){
			if(!waterArms.canDisplayCurrentArm() || grabbedEntity.isDead()){
				grabbed = false;
				grabbedEntities.remove(grabbedEntity);
				return;
			}
			Location newlocation = grabbedEntity.getLocation();
			double distance = location.distance(newlocation);
			double dx, dy, dz;
			dx = location.getX() - newlocation.getX();
			dy = location.getY() - newlocation.getY();
			dz = location.getZ() - newlocation.getZ();
			Vector vector = new Vector(dx, dy, dz);
			if (distance > .5) {
				grabbedEntity.setVelocity(vector.normalize().multiply(1));
			} else {
				grabbedEntity.setVelocity(new Vector(0, 0, 0));
			}
			grabbedEntity.setFallDistance(0);
			if (grabbedEntity instanceof Creature) {
				((Creature) grabbedEntity).setTarget(null);
			}
		}
	}
	
	private void grapplePlayer(Location location){
		if(reverting && grappled && player != null && end != null && ability.equals(Whip.Grapple)){
			Vector vector = player.getLocation().toVector().subtract(location.toVector());
			player.setVelocity(vector.multiply(-0.25));
			player.setFallDistance(0);
		}
	}

	//END OF RECODE

	@SuppressWarnings("deprecation")
	private void spawnFallingBlock(Location location, Location armbase){
		Block block = location.getBlock();
		Material mat = block.getType();
		byte data = block.getData();
		if(!GeneralMethods.isRegionProtectedFromBuild(player, "WaterArms", location)){
			Location spawnLoc = location.clone().toVector().subtract(player.getLocation().clone().getDirection().multiply(1)).toLocation(player.getWorld());

			new TempBlock(block, Material.AIR, (byte) 0);
			WaterArms.revert.put(block, System.currentTimeMillis() + pullBlocksRevertDelay);

			if(!WaterArms.isUnbreakable(spawnLoc.getBlock())){
				new TempBlock(spawnLoc.getBlock(), Material.AIR, (byte) 0);
				WaterArms.revert.put(spawnLoc.getBlock(), System.currentTimeMillis() + pullBlocksRevertDelay);
			}else{
				return;
			}

			FallingBlock fBlock = player.getWorld().spawnFallingBlock(spawnLoc.clone(), mat, data);
			fBlock.setDropItem(false);
			Vector from = fBlock.getLocation().toVector();
			Vector to = armbase.clone().toVector();
			Vector vector = to.subtract(from);
			fBlock.setVelocity(vector.multiply(0.15));
			WaterArms.falling.put(fBlock, player);
		}
	}

	private boolean canModifyBlock(Block block, Location toLoc){
		if(!EarthMethods.isTransparentToEarthbending(player, block) && !WaterMethods.isWater(block)){
			if(GeneralMethods.isRegionProtectedFromBuild(player, "WaterArms", block.getLocation())){
				if(!grappleRespectRegions() && block.getType().isSolid() && !EarthMethods.isTransparentToEarthbending(player, block))
					grappled = true;
				reverting = true;
				return true;
			}
			if(EarthMethods.isTransparentToEarthbending(player, block)){
				if(!block.getType().equals(Material.BARRIER)){
					grappled = true;
				}
				reverting = true;
				return true;
			}
			if(!(TempBlock.isTempBlock(block) && TempBlock.get(block).equals(Material.AIR))){
				if(!block.isLiquid() && block.getType().isSolid() && ability.equals(Whip.Pull) && pullBlocks && player.hasPermission("bending.ability.WaterArms.PullBlocks")){
					if(!EarthMethods.isTransparentToEarthbending(player, block))
						spawnFallingBlock(block.getLocation(), toLoc);
				}
				grappled = true;
				reverting = true;
				return true;
			}	
		}
		return false;
	}

	private boolean grappleRespectRegions(){
		if(!grappleRespectRegions)
			if(!ability.equals(Whip.Grapple))
				return true;
			else
				return false;
		else
			return true;
	}

	private void remove(){
		if(WaterArms.instances.containsKey(player)){
			if(arm.equals(Arm.Left)){
				waterArms.setLeftArmCooldown(false);
			}else{
				waterArms.setRightArmCooldown(false);
			}
			if(damaged)
				waterArms.setMaxPunches(waterArms.getMaxPunches() - 1);
			waterArms.setMaxUses(waterArms.getMaxUses() - 1);
		}
		instances.remove(id);
	}

	public static void progressAll(){
		for(int ID : instances.keySet())
			instances.get(ID).progress();
	}

	public static void removeAll(){
		instances.clear();
	}
}