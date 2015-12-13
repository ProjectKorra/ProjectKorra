package com.projectkorra.projectkorra.firebending;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.configuration.ConfigLoadable;
import com.projectkorra.projectkorra.earthbending.EarthMethods;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;

/**
 * Created by Carbogen on 11/02/15. Ability HeatControl
 */
public class HeatControl implements ConfigLoadable {

	public static ConcurrentHashMap<Player, HeatControl> instances = new ConcurrentHashMap<>();

	public static int RANGE = config.get().getInt("Abilities.Fire.HeatControl.Solidify.Range");
	public static int RADIUS = config.get().getInt("Abilities.Fire.HeatControl.Solidify.Radius");
	public static int REVERT_TIME = config.get().getInt("Abilities.Fire.HeatControl.Solidify.RevertTime");

	private Player player;
	private int currentRadius = 1;
	private long delay = 50;
	private long lastBlockTime = 0;
	private long lastParticleTime = 0;
	private Location center;
	private List<TempBlock> tblocks = new ArrayList<TempBlock>();

	public int range = RANGE;
	public int radius = RADIUS;
	public long revertTime = REVERT_TIME;

	public HeatControl(Player player) {
		/* Initial Checks */
		if (!isEligible(player))
			return;

		if (EarthMethods.getLavaSourceBlock(player, getRange()) == null) {
			new Cook(player);
			return;
		}

		/* End Initial Checks */

		this.player = player;

		lastBlockTime = System.currentTimeMillis();

		instances.put(player, this);
	}

	@SuppressWarnings("deprecation")
	public void freeze(List<Location> area) {
		if (System.currentTimeMillis() < lastBlockTime + delay)
			return;

		List<Block> lava = new ArrayList<Block>();

		for (Location l : area)
			if (EarthMethods.isLava(l.getBlock()))
				lava.add(l.getBlock());

		lastBlockTime = System.currentTimeMillis();

		if (lava.size() == 0) {
			currentRadius++;
			return;
		}

		Block b = lava.get(GeneralMethods.rand.nextInt(lava.size()));

		TempBlock tb;

		if (TempBlock.isTempBlock(b)) {
			tb = TempBlock.get(b);
			tb.setType(Material.STONE);
		}

		else
			tb = new TempBlock(b, Material.STONE, b.getData());

		if (!tblocks.contains(tb))
			tblocks.add(tb);

	}

	public Player getPlayer() {
		return player;
	}

	public int getRadius() {
		return radius;
	}

	public int getRange() {
		return range;
	}

	public long getRevertTime() {
		return revertTime;
	}

	public boolean isEligible(Player player) {
		if (!GeneralMethods.canBend(player.getName(), "HeatControl"))
			return false;

		if (GeneralMethods.getBoundAbility(player) == null)
			return false;

		if (!GeneralMethods.getBoundAbility(player).equalsIgnoreCase("HeatControl"))
			return false;

		return true;
	}

	public void particles(List<Location> area) {
		if (System.currentTimeMillis() < lastParticleTime + 300)
			return;

		lastParticleTime = System.currentTimeMillis();

		for (Location l : area) {
			if (EarthMethods.isLava(l.getBlock()))
				ParticleEffect.SMOKE.display(l, 0, 0, 0, 0.1f, 2);
		}
	}

	public boolean progress() {
		if (!player.isOnline() || player.isDead() || !isEligible(player) || !player.isSneaking()) {
			remove();
			return false;
		}

		if (currentRadius >= getRadius()) {
			remove();
			return false;
		}

		Location targetlocation = GeneralMethods.getTargetedLocation(player, range);

		resetLocation(targetlocation);

		List<Location> area = GeneralMethods.getCircle(center, currentRadius, 3, true, true, 0);

		particles(area);
		freeze(area);
		return true;
	}

	public static void progressAll() {
		for (HeatControl ability : instances.values()) {
			ability.progress();
		}
	}

	@Override
	public void reloadVariables() {
		RANGE = config.get().getInt("Abilities.Fire.HeatControl.Solidify.Range");
		RADIUS = config.get().getInt("Abilities.Fire.HeatControl.Solidify.Radius");
		REVERT_TIME = config.get().getInt("Abilities.Fire.HeatControl.Solidify.RevertTime");
		range = RANGE;
		radius = RADIUS;
		revertTime = REVERT_TIME;
	}

	public void remove() {
		final HeatControl ability = this;
		ProjectKorra.plugin.getServer().getScheduler().scheduleSyncDelayedTask(ProjectKorra.plugin, new Runnable() {
			public void run() {
				revertAll();
				instances.remove(ability);
			}
		}, getRevertTime());
	}

	public void resetLocation(Location loc) {
		if (center == null) {
			center = loc;
			return;
		}

		if (!loc.equals(center)) {
			currentRadius = 1;
			center = loc;
		}
	}

	public void revertAll() {
		for (TempBlock tb : tblocks) {
			tb.revertBlock();
		}
		tblocks.clear();
	}

	public void setRadius(int value) {
		radius = value;
	}

	public void setRange(int value) {
		range = value;
	}

	public void setRevertTime(long value) {
		revertTime = value;
	}
}
