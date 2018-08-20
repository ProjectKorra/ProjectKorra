package com.projectkorra.projectkorra.earthbending;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.attribute.Attribute;

public class EarthDome extends EarthAbility {

	public Location center;
	@Attribute(Attribute.RADIUS)
	public double radius;
	@Attribute(Attribute.HEIGHT)
	public int height;
	@Attribute(Attribute.COOLDOWN)
	public long cooldown;
	public Set<Block> checked = new HashSet<>();
	public Set<Block> corners = new HashSet<>();

	public EarthDome(final Player player, final Location center) {
		super(player);

		if (this.bPlayer.isOnCooldown("EarthDome")) {
			return;
		}

		this.center = center;
		this.radius = getConfig().getDouble("Abilities.Earth.EarthDome.Radius");
		this.height = getConfig().getInt("Abilities.Earth.EarthDome.Height");
		this.cooldown = getConfig().getLong("Abilities.Earth.EarthDome.Cooldown");

		start();
	}

	public EarthDome(final Player player) {
		this(player, player.getLocation().clone().subtract(0, 1, 0));
	}

	private Block getAppropriateBlock(final Block block) {
		if (!GeneralMethods.isSolid(block.getRelative(BlockFace.UP)) && GeneralMethods.isSolid(block)) {
			return block;
		}
		final Block top = GeneralMethods.getTopBlock(block.getLocation(), 2);
		if (GeneralMethods.isSolid(top.getRelative(BlockFace.UP))) {
			return null;
		}
		return top;
	}

	private List<Location> getCircle(final Location center, final double radius, double interval) {
		final List<Location> result = new ArrayList<>();
		interval = Math.toRadians(Math.abs(interval));
		for (double theta = 0; theta < 2 * Math.PI; theta += interval) {
			final double x = Math.cos(theta) * (radius + (Math.random() / 3.1));
			final double z = Math.sin(theta) * (radius + (Math.random() / 3.1));
			result.add(center.clone().add(x, 0, z));
		}
		return result;
	}

	@Override
	public void progress() {
		for (int i = 0; i < 2; i++) {
			for (final Location check : this.getCircle(center, this.radius + i, 10)) {
				Block b = check.getBlock();
				if (this.checked.contains(b)) {
					continue;
				}

				b = this.getAppropriateBlock(b);
				if (b == null) {
					continue;
				}

				new RaiseEarth(player, b.getLocation(), Math.round(this.height - i));
				this.checked.add(b);
			}

		}

		this.bPlayer.addCooldown("EarthDome", this.getCooldown());
		this.remove();
	}

	@Override
	public boolean isSneakAbility() {
		return false;
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
	public String getName() {
		return "EarthDomeHidden";
	}

	@Override
	public Location getLocation() {
		return this.center;
	}

	@Override
	public boolean isHiddenAbility() {
		return true;
	}

}
