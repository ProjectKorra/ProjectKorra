package com.projectkorra.ProjectKorra.firebending;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.TempBlock;
import com.projectkorra.ProjectKorra.Utilities.ParticleEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Carbogen on 11/02/15.
 */
public class HeatControl
{
	public static ConcurrentHashMap<Integer, HeatControl> instances = new ConcurrentHashMap<Integer, HeatControl>();

	public final double RANGE = ProjectKorra.plugin.getConfig().getDouble("Abilities.Fire.HeatControl.Solidify.Range");
	public final int RADIUS = ProjectKorra.plugin.getConfig().getInt("Abilities.Fire.HeatControl.Solidify.Radius");
	public final int REVERT_TIME = ProjectKorra.plugin.getConfig().getInt("Abilities.Fire.HeatControl.Solidify.RevertTime");

	public static int ID = 1;

	private Player player;
	private int id;
	private long lastBlockTime = 0;
	private long lastParticleTime = 0;
	private List<TempBlock> tblocks = new ArrayList<TempBlock>();

	public double range = RANGE;
	public int radius = RADIUS;
	public long revertTime = REVERT_TIME;

	public HeatControl(Player player)
	{
		player.sendMessage("HeatControl called.");

		if (!isEligible(player))
			return;

		player.sendMessage("isEligible() passed.");

		if(Methods.getLavaSourceBlock(player, getRange()) == null)
		{
			player.sendMessage("Cannot find lava block.");
			new Cook(player);
			return;
		}

		player.sendMessage("New HeatControl() created.");

		this.player = player;

		if(ID == Integer.MAX_VALUE - 1)
			ID = 0;

		this.id = ID;

		ID++;

		lastBlockTime = System.currentTimeMillis();

		instances.put(id, this);
	}

	public boolean isEligible(Player player)
	{
		if (!Methods.canBend(player.getName(), "HeatControl"))
			return false;

		if (Methods.getBoundAbility(player) == null)
			return false;

		if (!Methods.getBoundAbility(player).equalsIgnoreCase("HeatControl"))
			return false;

		return true;
	}

	public void freeze(List<Location> area)
	{
		if(System.currentTimeMillis() < lastBlockTime + 100)
			return;

		lastBlockTime = System.currentTimeMillis();

		List<Block> lava = new ArrayList<Block>();

		for(Location l : area)
			if(Methods.isLava(l.getBlock()))
				lava.add(l.getBlock());

		if(lava.size() == 0)
		{
			stop();
			return;
		}

		Block b = lava.get(Methods.rand.nextInt(lava.size()));

		TempBlock tb;

		if(TempBlock.isTempBlock(b))
		{
			tb = TempBlock.get(b);
			tb.setType(Material.STONE);
		}

		else tb = new TempBlock(b, Material.STONE, b.getData());

		if(!tblocks.contains(tb))
			tblocks.add(tb);
	}

	public void particles(List<Location> area)
	{
		if(System.currentTimeMillis() < lastParticleTime + 300)
			return;

		lastParticleTime = System.currentTimeMillis();

		for(Location l : area)
		{
			if(Methods.isLava(l.getBlock()))
				ParticleEffect.SMOKE.display(l, 0, 0, 0, 0.1f, 2);
		}
	}

	public void progress()
	{
		if(!player.isOnline() || player.isDead() || !isEligible(player) || !player.isSneaking())
		{
			stop();
			return;
		}

		Location targetlocation = Methods.getTargetedLocation(player, range);

		List<Location> area = Methods.getCircle(targetlocation, radius, 3, false, true, 0);

		particles(area);
		freeze(area);
	}

	public static void progressAll()
	{
		for(Integer id : instances.keySet())
		{
			instances.get(id).progress();
		}
	}

	public void stop()
	{
		ProjectKorra.plugin.getServer().getScheduler().scheduleSyncDelayedTask(ProjectKorra.plugin, new Runnable()
		{
			public void run()
			{
				revertAll();
				if(instances.containsKey(id))
					instances.remove(id);
			}
		}, getRevertTime());

	}

	public void revertAll()
	{
		for(TempBlock tb : tblocks)
		{
			tb.revertBlock();
		}

		tblocks.clear();
	}

	public Player getPlayer()
	{
		return player;
	}

	public double getRange()
	{
		return range;
	}

	public int getRadius()
	{
		return radius;
	}

	public long getRevertTime()
	{
		return revertTime;
	}

	public void setRange(double value)
	{
		range = value;
	}

	public void setRadius(int value)
	{
		radius = value;
	}

	public void setRevertTime(long value)
	{
		revertTime = value;
	}
}
