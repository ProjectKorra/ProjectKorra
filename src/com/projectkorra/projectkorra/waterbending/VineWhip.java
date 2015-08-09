package com.projectkorra.projectkorra.waterbending;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.StockAbility;
import com.projectkorra.projectkorra.ability.api.CoreAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.earthbending.EarthMethods;
import com.projectkorra.projectkorra.util.TempBlock;

public class VineWhip extends CoreAbility {
	
	public FileConfiguration config = ConfigManager.defaultConfig.get();
	
	private double RANGE = config.getDouble("Abilities.Water.VineWhip.Range");
	private double DAMAGE = config.getDouble("Abilities.Water.VineWhip.Damage");
	private int SELECT = config.getInt("Abilities.Water.VineWhip.SourceRange");
	private long REVERT = config.getLong("Abilities.Water.VineWhip.RevertTime");
	private double SPEED = config.getDouble("Abilities.Water.VineWhip.Speed");
	private long COOLDOWN = config.getLong("Abilities.Water.VineWhip.CoolDown");
	
	public static ConcurrentHashMap<Block, Long> revert = new ConcurrentHashMap<Block, Long>();
	public static ArrayList<FallingBlock> dontsolidify = new ArrayList<FallingBlock>();
	public Boolean clicked;
	public Player player;
	public Vector dir;
	public Location whip;
	public Block source;
	public boolean Hit;
	private int rangeCount;
	public Location targetLoc;
	
	static boolean canBend(Player player) {
		  if (GeneralMethods.getBoundAbility(player) == null) return false;
		  if (!GeneralMethods.canBend(player.getName(), "VineWhip")) return false;
		  if (GeneralMethods.isRegionProtectedFromBuild(player, "VineWhip", player.getLocation())) return false;
		  if (GeneralMethods.getBendingPlayer(player.getName()).isOnCooldown("VineWhip")) return false;
		  if (GeneralMethods.getBendingPlayer(player).isChiBlocked()) return false;
		  if (!GeneralMethods.getBendingPlayer(player).isToggled()) return false;
		  if (GeneralMethods.getBoundAbility(player).equalsIgnoreCase("VineWhip")) return true;
		  return false;
	  	}
	
	public VineWhip(Player player){
		if(!canBend(player))
			return;
		
		Block block = WaterMethods.getPlantSourceBlock(player, this.SELECT, false);
		if(block != null && !TempBlock.isTempBlock(block)) {
			if(containsPlayer(player, VineWhip.class)) {
				VineWhip vw = (VineWhip) getAbilityFromPlayer(player, VineWhip.class);
				vw.remove();
			}
			this.player = player;
			source = block;
			whip = this.source.getLocation().add(0.5, 0.5, 0.5);
			rangeCount = 0;
			clicked = false;
			targetLoc = null;
			putInstance(player, this);
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void revertBlocks() {
		for(Block b : revert.keySet()){
			long time = revert.get(b);
			if(System.currentTimeMillis() > time){
				TempBlock.revertBlock(b, Material.AIR);
				FallingBlock fb = GeneralMethods.spawnFallingBlock(b.getLocation().add(0.5, 0.5, 0.5), Material.LEAVES, b.getData());
				dontsolidify.add(fb);
				fb.setDropItem(false);
				revert.remove(b);
			}
		}
	}
	
	static void repeat() {
		revertBlocks();
		progressAll(VineWhip.class);
	}

	@SuppressWarnings("deprecation")
	public boolean progress() {
		if(player.isDead() || !player.isOnline()){
			stop();
			return false;
		}
		if(GeneralMethods.getBoundAbility(player) == null || !GeneralMethods.getBoundAbility(player).equals("VineWhip")){
			stop();
			return false;
		}
		if(player.getWorld() != this.source.getWorld() || player.getWorld() != this.whip.getWorld()) {
			stop();
			return false;
		}
		if(!clicked){
			
			this.source.getWorld().playEffect(this.source.getLocation().add(0.5, 0.5, 0.5), Effect.SMOKE, 4, (int) SELECT);
			
			if(player.getLocation().distance(this.source.getLocation().add(0.5, 0.5, 0.5)) > SELECT){
				stop();
				return false;
			}
			
		}else{
			this.dir = GeneralMethods.getDirection(this.whip, targetLoc).normalize().multiply(SPEED);
			if(revert.containsKey(this.targetLoc.getBlock())) {
				stop();
				return false;
			}
			if(rangeCount > RANGE){
				stop();
				return false;
			}
			this.whip.add(this.dir);
				
			if(!revert.containsKey(this.whip.getBlock())){
				if(EarthMethods.isTransparentToEarthbending(player, this.whip.getBlock()) || WaterMethods.isPlantbendable(this.whip.getBlock(), false)){
						
					new TempBlock(this.whip.getBlock(), Material.LEAVES, this.source.getData());
					revert.put(this.whip.getBlock(), System.currentTimeMillis() + REVERT);
					if(GeneralMethods.isRegionProtectedFromBuild(player, "VineWhip", this.whip)) {
						stop();
						return false;
					}
					rangeCount++;
						
					for(Entity e : GeneralMethods.getEntitiesAroundPoint(this.whip, 2D)){
						if(e instanceof LivingEntity && e.getEntityId() != player.getEntityId()
								&& !(e instanceof ArmorStand)){
							GeneralMethods.damageEntity(player, e, DAMAGE);
							e.setVelocity(GeneralMethods.getDirection(this.whip, e.getLocation()).normalize().multiply(0.6));
							stop();
							return true;
						}
					}
						
				}else{
						
					stop();
						
				}
			}
		}
		return true;
	}
	
	@SuppressWarnings("deprecation")
	public static Location getTargetLocation(Player player, double range) {
		ArrayList<Entity> avoid = new ArrayList<Entity>();
		Entity victim = GeneralMethods.getTargetedEntity(player, range, avoid);
		if(victim != null) {
			return victim.getLocation();
		} else {
			Integer[] avoidBlocks = {Material.LEAVES.getId(), Material.LEAVES_2.getId(), 0, 6, 8, 9, 10, 11, 27, 28, 30, 31, 32, 37, 38, 39, 40, 50, 51, 55, 59, 66, 68, 69, 70, 72,
					75, 76, 77, 78, 83, 90, 93, 94, 104, 105, 106, 111, 115, 119, 127, 131, 132, 175};
			return GeneralMethods.getTargetedLocation(player, range, avoidBlocks);
		}
	}
	
	public void stop() {
		GeneralMethods.getBendingPlayer(player).addCooldown("VineWhip", COOLDOWN);
		remove();
	}
	
	public static void clear() {
		removeAll(VineWhip.class);
		
		for(Block b : revert.keySet()){
			TempBlock.revertBlock(b, Material.AIR);
			revert.remove(b);
		}
	}

	@Override
	public void reloadVariables() {
		// TODO Auto-generated method stub
		this.RANGE = config.getDouble("Abilities.Water.VineWhip.Range");
		this.DAMAGE = config.getDouble("Abilities.Water.VineWhip.Damage");
		this.SELECT = config.getInt("Abilities.Water.VineWhip.SourceRange");
		this.REVERT = config.getLong("Abilities.Water.VineWhip.RevertTime");
		this.SPEED = config.getDouble("Abilities.Water.VineWhip.Speed");
		this.COOLDOWN = config.getLong("Abilities.Water.VineWhip.CoolDown");
	}
	
	public InstanceType getInstanceType() {
		return InstanceType.SINGLE;
	}
	
	@Override
	public StockAbility getStockAbility() {
		// TODO Auto-generated method stub
		return StockAbility.VineWhip;
	}

	
}
