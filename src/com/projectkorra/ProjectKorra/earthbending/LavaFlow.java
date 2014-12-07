package com.projectkorra.ProjectKorra.earthbending;

import java.util.ArrayList;
import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.TempBlock;
import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.Utilities.ParticleEffect;
import com.projectkorra.ProjectKorra.waterbending.Plantbending;

public class LavaFlow 
{	
	public static enum AbilityType{
		SHIFT, CLICK
	}
	public static Material REVERT_MATERIAL = Material.STONE;
	public static long SHIFT_COOLDOWN = ProjectKorra.plugin.getConfig().getLong("Abilities.Earth.LavaFlow.ShiftCooldown");
	public static double SHIFT_PLATFORM_RADIUS = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.LavaFlow.ShiftPlatformRadius");
	public static double SHIFT_MAX_RADIUS = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.LavaFlow.ShiftRadius");
	public static double SHIFT_FLOW_SPEED = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.LavaFlow.ShiftFlowSpeed");
	public static double SHIFT_REMOVE_SPEED = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.LavaFlow.ShiftRemoveSpeed");
	public static long SHIFT_REMOVE_DELAY = ProjectKorra.plugin.getConfig().getLong("Abilities.Earth.LavaFlow.ShiftCleanupDelay");
	public static double PARTICLE_DENSITY = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.LavaFlow.ParticleDensity");

	public static double CLICK_RANGE = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.LavaFlow.ClickRange");
	public static double CLICK_LAVA_RADIUS = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.LavaFlow.ClickRadius");
	public static double CLICK_LAND_RADIUS = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.LavaFlow.ClickRadius");
	public static long CLICK_LAVA_DELAY = ProjectKorra.plugin.getConfig().getLong("Abilities.Earth.LavaFlow.ClickLavaStartDelay");
	public static long CLICK_LAND_DELAY = ProjectKorra.plugin.getConfig().getLong("Abilities.Earth.LavaFlow.ClickLandStartDelay");
	public static long CLICK_LAVA_COOLDOWN = ProjectKorra.plugin.getConfig().getLong("Abilities.Earth.LavaFlow.ClickLavaCooldown");
	public static long CLICK_LAND_COOLDOWN = ProjectKorra.plugin.getConfig().getLong("Abilities.Earth.LavaFlow.ClickLandCooldown");
	public static long CLICK_LAVA_CLEANUP_DELAY = ProjectKorra.plugin.getConfig().getLong("Abilities.Earth.LavaFlow.ClickLavaCleanupDelay");
	public static long CLICK_LAND_CLEANUP_DELAY = ProjectKorra.plugin.getConfig().getLong("Abilities.Earth.LavaFlow.ClickLandCleanupDelay");
	public static double LAVA_CREATE_SPEED = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.LavaFlow.ClickLavaCreateSpeed");
	public static double LAND_CREATE_SPEED = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.LavaFlow.ClickLandCreateSpeed");

	public static long AS_SHIFT_COOLDOWN = 0;
	public static double AS_SHIFT_PLATFORM_RADIUS = 3;
	public static double AS_SHIFT_MAX_RADIUS = 16;
	public static double AS_SHIFT_FLOW_SPEED = 0.2;
	public static double AS_SHIFT_REMOVE_SPEED = 6.0;
	public static long AS_SHIFT_REMOVE_DELAY = 12000;
	public static double AS_CLICK_RANGE = 15.0;
	public static double AS_CLICK_RADIUS = 8.0;
	public static long AS_CLICK_LAVA_DELAY = 2000;
	public static long AS_CLICK_LAND_DELAY = 0;
	public static long AS_CLICK_COOLDOWN = 0;

	public static int UPWARD_FLOW = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.LavaFlow.UpwardFlow");
	public static int DOWNWARD_FLOW = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.LavaFlow.DownwardFlow");
	public static boolean ALLOW_NATURAL_FLOW = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Earth.LavaFlow.AllowNaturalFlow");

	private static final double PARTICLE_OFFSET = 3;

	public static ArrayList<LavaFlow> instances = new ArrayList<LavaFlow>();
	public static ArrayList<TempBlock> totalBlocks = new ArrayList<TempBlock>();

	private Player player;
	private BendingPlayer bplayer;
	private long time;
	public int shiftCounter = 0;
	private boolean removing = false;
	private boolean makeLava = true;
	private boolean clickIsFinished = false;
	private AbilityType type;
	private Location origin;
	private double currentRadius = 0;
	private ArrayList<TempBlock> affectedBlocks = new ArrayList<TempBlock>();
	private ArrayList<BukkitRunnable> tasks = new ArrayList<BukkitRunnable>();

	public LavaFlow(Player player, AbilityType type)
	{
		time = System.currentTimeMillis();
		this.player = player;
		this.type = type;
		bplayer = Methods.getBendingPlayer(player.getName());

		if(type == AbilityType.SHIFT)
		{
			// Update the shift counter for all the player's LavaFlows
			ArrayList<LavaFlow> shiftFlows = LavaFlow.getLavaFlow(player,LavaFlow.AbilityType.SHIFT);
			if(shiftFlows.size() > 0 && !player.isSneaking())
				for(LavaFlow lf : shiftFlows)
					lf.shiftCounter++;

			if(bplayer.isOnCooldown("lavaflowcooldownshift")){
				remove();
				return;
			}
			instances.add(this);
		}
		else if(type == AbilityType.CLICK)
		{
			double range = AvatarState.isAvatarState(player) ? AS_CLICK_RANGE : CLICK_RANGE;
			Block sourceBlock = getEarthSourceBlock(player, range);
			if(sourceBlock == null){
				remove();
				return;
			}
			origin = sourceBlock.getLocation();
			makeLava = !isLava(sourceBlock);
			long cooldown = makeLava ? CLICK_LAVA_COOLDOWN : CLICK_LAND_COOLDOWN;
			cooldown = AvatarState.isAvatarState(player) ? AS_CLICK_COOLDOWN : cooldown;

			if(makeLava){
				if(bplayer.isOnCooldown("lavaflowmakelava")){
					remove();
					return;
				}
				else
					bplayer.addCooldown("lavaflowmakelava", cooldown);
			}

			if(!makeLava){
				if(bplayer.isOnCooldown("lavaflowmakeland")){
					remove();
					return;
				}
				else
					bplayer.addCooldown("lavaflowmakeland", cooldown);
			}			
			instances.add(this);
		}
	}

	public void progress()
	{
		if(shiftCounter > 0 && type == AbilityType.SHIFT){
			remove();
			return;
		}
		else if(removing) 
			return;
		else if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}		

		if(type == AbilityType.SHIFT)
		{
			double removeDelay = AvatarState.isAvatarState(player) ? AS_SHIFT_REMOVE_DELAY : SHIFT_REMOVE_DELAY;
			if(System.currentTimeMillis() - time > removeDelay){
				remove();
				return;
			}
			if(!player.isSneaking() && !removing){
				if(affectedBlocks.size() > 0){
					removeOnDelay();
					removing = true;
					long cooldown = AvatarState.isAvatarState(player) ? AS_SHIFT_COOLDOWN : SHIFT_COOLDOWN;
					bplayer.addCooldown("lavaflowcooldownshift", cooldown);
				}
				else
					remove();
				return;
			}

			String ability = Methods.getBoundAbility(player);
			if(ability == null){
				remove();
				return;
			}
			else if (!ability.equalsIgnoreCase("LavaFlow") || !Methods.canBend(player.getName(), "LavaFlow")){
				remove();
				return;
			}
			else if(origin == null){
				origin = player.getLocation().clone().add(0,-1,0);
				if(!Methods.isEarthbendable(player, origin.getBlock()) && origin.getBlock().getType() != Material.GLOWSTONE){
					remove();
					return;
				}
			}

			double platformRadius = AvatarState.isAvatarState(player) ? AS_SHIFT_PLATFORM_RADIUS : SHIFT_PLATFORM_RADIUS;
			double maxRadius = AvatarState.isAvatarState(player) ? AS_SHIFT_MAX_RADIUS : SHIFT_MAX_RADIUS;
			double flowSpeed = AvatarState.isAvatarState(player) ? AS_SHIFT_FLOW_SPEED : SHIFT_FLOW_SPEED;
			for(double x = -currentRadius; x <= currentRadius + PARTICLE_OFFSET; x++)
				for(double z = -currentRadius; z < currentRadius + PARTICLE_OFFSET; z++)
				{
					Location loc = origin.clone().add(x,0,z);
					Block block = getTopBlock(loc,UPWARD_FLOW,DOWNWARD_FLOW);
					if(block == null)
						continue;

					double dSquared = distanceSquaredXZ(block.getLocation(),origin);	
					if(!isLava(block) && dSquared > Math.pow(platformRadius, 2))
					{
						if(dSquared < Math.pow(currentRadius, 2) 
								&&	!Methods.isRegionProtectedFromBuild(player, "LavaFlow", block.getLocation()))
						{
							if(dSquared < platformRadius * 4 || getAdjacentLavaBlocks(block.getLocation()).size() > 0)
								createLava(block);
						}
						else if(Math.random() < PARTICLE_DENSITY
								&& dSquared < Math.pow(currentRadius + PARTICLE_OFFSET, 2)
								&& currentRadius + PARTICLE_OFFSET < maxRadius) {
							try {
								ParticleEffect.LAVA.sendToPlayers(Methods.getPlayersAroundPoint(loc, 100), loc, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0, 1);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}

				}
			currentRadius += flowSpeed;
			if(currentRadius > maxRadius) currentRadius = maxRadius;		
		}

		/**
		 * The variable makeLava refers to whether or not the ability is trying to 
		 * remove land in place of lava or if makeLava = false then lava is being replaced
		 * with land.
		 * 
		 * Notice we have separate variables between both versions, because
		 * most of the time making lava will have longer delays and longer cooldowns.
		 */
		else if(type == AbilityType.CLICK)
		{
			long curTime = System.currentTimeMillis() - time;
			double delay;
			if(AvatarState.isAvatarState(player))
				delay = makeLava ? AS_CLICK_LAVA_DELAY : AS_CLICK_LAND_DELAY;
			else
				delay = makeLava ? CLICK_LAVA_DELAY : CLICK_LAND_DELAY;
			if(makeLava && curTime > CLICK_LAVA_CLEANUP_DELAY)
			{
				remove();
				return;
			}
			else if(!makeLava && curTime > CLICK_LAND_CLEANUP_DELAY)
			{
				remove();
				return;
			}
			else if(!makeLava && curTime < delay)
				return;
			else if(makeLava && curTime < delay)
			{
				double radius = AvatarState.isAvatarState(player) ? AS_CLICK_RADIUS : CLICK_LAVA_RADIUS;
				for(double x = -radius; x <= radius; x++)
					for(double z = -radius; z <= radius; z++)
					{							
						Location loc = origin.clone().add(x,0,z);
						Block tempBlock = getTopBlock(loc,UPWARD_FLOW,DOWNWARD_FLOW);
						if(tempBlock != null 
								&& !isLava(tempBlock) 
								&& Math.random() < PARTICLE_DENSITY
								&& tempBlock.getLocation().distanceSquared(origin) <= Math.pow(radius,2))
							try {
								ParticleEffect.LAVA.sendToPlayers(Methods.getPlayersAroundPoint(loc, 100), loc, 0, 0, 0, 0, 1);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

					}
				return;
			}

			/*
			 *  Start to create all of the lava, if all of the lava has been created then we are finished with
			 *  this instance of LavaFlow, but we need to keep it running so that we can revert the blocks.
			 */
			if(!clickIsFinished)
			{
				clickIsFinished = true;
				double radius = makeLava ? CLICK_LAVA_RADIUS : CLICK_LAND_RADIUS;
				radius = AvatarState.isAvatarState(player) ? AS_CLICK_RADIUS : radius;
				for(double x = -radius; x <= radius; x++)
					for(double z = -radius; z <= radius; z++)
					{
						Location loc = origin.clone().add(x,0,z);
						Block tempBlock = getTopBlock(loc,UPWARD_FLOW,DOWNWARD_FLOW);
						if(tempBlock == null)
							continue;

						double dSquared = distanceSquaredXZ(tempBlock.getLocation(),origin);					
						if(dSquared < Math.pow(radius,2) && !Methods.isRegionProtectedFromBuild(player, "LavaFlow", loc))
						{
							if(makeLava && !isLava(tempBlock))
							{
								clickIsFinished = false;
								if(Math.random() < LAVA_CREATE_SPEED)
									createLava(tempBlock);
								else
									try {
										ParticleEffect.LAVA.sendToPlayers(Methods.getPlayersAroundPoint(loc, 100), loc, 0, 0, 0, 0, 1);
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
							}
							else if(!makeLava && isLava(tempBlock))
							{
								clickIsFinished = false;
								if(Math.random() < LAND_CREATE_SPEED)
									removeLava(tempBlock);									
							}
						}
					}
				return;
			}

		}
	}
	public void createLava(Block block)
	{
		/**
		 * Creates a LavaBlock and also accounts for
		 * melting over plants by creating new Plantbending() objects.
		 * It also appends the TempBlock to our arraylist called totalBlocks.
		 * 
		 * NOTE: (DISABLED) Due to LavaSurge's check on TempBlocks we have to remove
		 * our tempblocks from TempBlock.instances so that players will
		 * be able to LavaSurge our blocks.
		 */
		boolean valid = false;
		if(!isEarthbendableMaterial(block.getType(), player) && Methods.isPlant(block)){
			new Plantbending(block);
			block.setType(Material.AIR);
			valid = true;
		}
		else if(isEarthbendableMaterial(block.getType(), player))
			valid = true;


		if(valid){
			TempBlock tblock = new TempBlock(block,Material.STATIONARY_LAVA,(byte) 0);
			totalBlocks.add(tblock);
			affectedBlocks.add(tblock);
			if(ALLOW_NATURAL_FLOW)
				TempBlock.instances.remove(block);
		}
	}
	public void removeLava(Block testBlock)
	{
		/**
		 * Removes a lava block if it is inside of our
		 * ArrayList of TempBlocks. 
		 */
		for(int i = 0; i < totalBlocks.size(); i++)
		{
			TempBlock tblock = totalBlocks.get(i);
			Block block = tblock.getBlock();
			if(block.equals(testBlock)){
				tblock.revertBlock();
				totalBlocks.remove(i);
				affectedBlocks.remove(tblock);
				return;
			}
		}
		testBlock.setType(REVERT_MATERIAL);

	}

	public void removeOnDelay()
	{
		/**
		 * Causes this instance of LavaFlow to remove() after a specified
		 * amount of time. This is useful for causing the Shift version of the
		 * ability to automatically clean up over time.
		 */
		BukkitRunnable br = new BukkitRunnable(){
			public void run(){
				remove();
			}
		};
		double delay = AvatarState.isAvatarState(player) ? AS_SHIFT_REMOVE_DELAY : SHIFT_REMOVE_DELAY;
		br.runTaskLater(ProjectKorra.plugin, (long) (delay / 1000.0 * 20.0));
		tasks.add(br);
	}

	public void remove()
	{
		/**
		 * Removes this instance of LavaFlow, cleans up
		 * any blocks that are remaining in totalBlocks,
		 * and cancels any remaining tasks.
		 */
		instances.remove(this);
		double removeSpeed = AvatarState.isAvatarState(player) ? AS_SHIFT_REMOVE_SPEED : SHIFT_REMOVE_SPEED;
		for(int i = affectedBlocks.size() - 1; i > -1 ; i--)
		{
			final TempBlock tblock = affectedBlocks.get(i);
			new BukkitRunnable(){
				public void run(){
					tblock.revertBlock();
				}
			}.runTaskLater(ProjectKorra.plugin, (long) (i / removeSpeed));
			if(totalBlocks.contains(tblock))
			{
				affectedBlocks.remove(tblock);
				totalBlocks.remove(tblock);
			}
		}
		for(BukkitRunnable task : tasks)
			task.cancel();
	}

	public static void progressAll()
	{
		for(int i = instances.size() - 1; i >= 0; i--)
			instances.get(i).progress();
	}

	public static void removeAll()
	{
		for(int i = instances.size() - 1; i >= 0; i--)
		{
			instances.get(i).remove();
		}
	}

	public static ArrayList<Block> getAdjacentBlocks(Location loc)
	{
		/**
		 * Returns an ArrayList of all the surrounding blocks for loc,
		 * but it excludes the block that is contained at Loc.
		 */
		ArrayList<Block> list = new ArrayList<Block>();
		Block block = loc.getBlock();
		for(int x = -1; x <= 1; x++)
			for(int y = -1; y <= 1; y++)
				for(int z = -1; z <= 1; z++)
					if(!(x == 0 && y  == 0 && z == 0))
						list.add(block.getLocation().add(x,y,z).getBlock());
		return list;
	}
	public static ArrayList<Block> getAdjacentLavaBlocks(Location loc)
	{
		/**
		 * Returns a list of all the Lava blocks that are adjacent to the
		 * block at loc.
		 */
		ArrayList<Block> list = getAdjacentBlocks(loc);
		for(int i = 0; i < list.size(); i++)	
		{
			Block block = list.get(i);
			if(!isLava(block))
			{
				list.remove(i);
				i--;
			}
		}
		return list;
	}
	public static boolean isEarthbendableMaterial(Material mat, Player player)
	{
		/**
		 * A version of Methods.isEarthbendable that avoids using the
		 * isRegionProtected call since it isn't necessary in the case of just
		 * checking a specific material.
		 */
		for (String s : ProjectKorra.plugin.getConfig().getStringList("Properties.Earth.EarthbendableBlocks"))
			if (mat == Material.getMaterial(s))
				return true;
		if (ProjectKorra.plugin.getConfig().getStringList("Properties.Earth.MetalBlocks").contains(mat.toString()) && Methods.canMetalbend(player)) {
			return true;
		}
		return false;
	}
	public static Block getTopBlock(Location loc, int range){
		return getTopBlock(loc,range,range);
	}
	public static Block getTopBlock(Location loc, int positiveY, int negativeY)
	{
		/**
		 * Returns the top Earthbendable block based around loc.
		 * PositiveY is the maximum amount of distance it will check upward.
		 * Similarly, negativeY is for downward.
		 */
		Block block = loc.getBlock();
		Block blockHolder = block;
		int y = 0;
		//Only one of these while statements will go
		while(blockHolder.getType() != Material.AIR && Math.abs(y) < Math.abs(positiveY))
		{
			y++;
			Block tempBlock = loc.clone().add(0,y,0).getBlock();
			if(tempBlock.getType() == Material.AIR) 
				return blockHolder;
			blockHolder = tempBlock;
		}

		while(blockHolder.getType() == Material.AIR && Math.abs(y) < Math.abs(negativeY))
		{
			y--;
			blockHolder = loc.clone().add(0,y,0).getBlock();
			if(blockHolder.getType() != Material.AIR) 
				return blockHolder;

		}
		return null;
	}
	public static boolean isLava(Block block){
		return block.getType() == Material.LAVA || block.getType() == Material.STATIONARY_LAVA;
	}

	public static Block getEarthSourceBlock(Player player, double range) {
		/**
		 * A version of Methods.getEarthSourceBlock but this one force
		 * allows the use of Lava.
		 */
		HashSet<Byte> bendables = Methods.getTransparentEarthbending();
		bendables.remove((byte) 10);
		bendables.remove((byte) 11);
		Block testblock = player.getTargetBlock(bendables,
				(int) range);
		if ((!Methods.isRegionProtectedFromBuild(player, "LavaFlow", testblock.getLocation()))
				&& (isEarthbendableMaterial(testblock.getType(), player) || isLava(testblock)))
			return testblock;

		Location location = player.getEyeLocation();
		Vector vector = location.getDirection().clone().normalize();
		for (double i = 0; i <= range; i++) {
			Block block = location.clone().add(vector.clone().multiply(i))
					.getBlock();
			if (Methods.isRegionProtectedFromBuild(player, "RaiseEarth", location))
				continue;
			if (isEarthbendableMaterial(testblock.getType(), player) || isLava(testblock)) {
				return block;
			}
		}
		return null;
	}
	public static double distanceSquaredXZ(Location l1, Location l2)
	{
		/**
		 * Gets the distance between 2 locations but ignores their Y values.
		 * This was useful in allowing the flow of lava to look more natural and
		 * not be substantially shortened by the Y distance if it is flowing upward or downward.
		 */
		Location temp1 = l1.clone();
		Location temp2 = l2.clone();
		temp1.setY(0);
		temp2.setY(0);
		return temp1.distanceSquared(temp2);
	}
	public static ArrayList<LavaFlow> getLavaFlow(Player player)
	{
		/**
		 * Returns all the LavaFlows created by a specific player.
		 */
		ArrayList<LavaFlow> list = new ArrayList<LavaFlow>();
		for(LavaFlow lf : instances)
			if(lf.player != null && lf.player == player)
				list.add(lf);
		return list;
	}
	public static ArrayList<LavaFlow> getLavaFlow(Player player, AbilityType type)
	{
		/**
		 * Returns all of the LavaFlows created by a specific player but
		 * filters the abilities based on shift or click.
		 */
		ArrayList<LavaFlow> list = new ArrayList<LavaFlow>();
		for(LavaFlow lf : instances)
			if(lf.player != null && lf.player == player && lf.type != null && lf.type == type)
				list.add(lf);
		return list;
	}
}
