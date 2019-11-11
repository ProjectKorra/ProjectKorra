package com.projectkorra.projectkorra.earthbending;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.legacy.EarthAbility;
import com.projectkorra.projectkorra.ability.legacy.ElementalAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.configuration.configs.abilities.earth.TremorsenseConfig;

@SuppressWarnings("deprecation")
public class Tremorsense extends EarthAbility<TremorsenseConfig> {

	@Deprecated
	private static final Map<Block, Player> BLOCKS = new ConcurrentHashMap<Block, Player>();

	private byte lightThreshold;
	@Attribute("Depth")
	private int maxDepth;
	@Attribute(Attribute.RADIUS)
	private int radius;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	private Block block;
	@Attribute(Attribute.RANGE)
	private int stickyRange;

	public Tremorsense(final TremorsenseConfig config, final Player player, final boolean clicked) {
		super(config, player);

		if (!this.bPlayer.canBendIgnoreBinds(this)) {
			return;
		}

		this.setFields();
		final byte lightLevel = player.getLocation().getBlock().getLightLevel();

		if (lightLevel < this.lightThreshold && this.isEarthbendable(player.getLocation().getBlock().getRelative(BlockFace.DOWN))) {
			if (clicked) {
				this.bPlayer.addCooldown(this);
				this.activate();
			}
			this.start();
		}
	}

	private void setFields() {
		this.maxDepth = config.MaxDepth;
		this.radius = config.Radius;
		this.lightThreshold = config.LightThreshold;
		this.cooldown = config.Cooldown;
		this.stickyRange = config.StickyRange;
	}

	private void activate() {
		final Block block = this.player.getLocation().getBlock().getRelative(BlockFace.DOWN);
		for (int i = -this.radius; i <= this.radius; i++) {
			for (int j = -this.radius; j <= this.radius; j++) {
				boolean earth = false;
				boolean foundAir = false;
				Block smokeBlock = null;

				for (int k = 0; k <= this.maxDepth; k++) {
					final Block blocki = block.getRelative(BlockFace.EAST, i).getRelative(BlockFace.NORTH, j).getRelative(BlockFace.DOWN, k);
					if (GeneralMethods.isRegionProtectedFromBuild(this, blocki.getLocation())) {
						continue;
					}
					if (this.isEarthbendable(blocki) && !earth) {
						earth = true;
						smokeBlock = blocki;
					} else if (!this.isEarthbendable(blocki) && earth) {
						foundAir = true;
						break;
					} else if (!this.isEarthbendable(blocki) && !earth && !ElementalAbility.isAir(blocki.getType())) {
						break;
					}
				}
				if (foundAir) {
					smokeBlock.getWorld().playEffect(smokeBlock.getRelative(BlockFace.UP).getLocation(), Effect.SMOKE, 4, this.radius);
				}
			}
		}
	}

	private void tryToSetGlowBlock() {
		final Block standBlock = this.player.getLocation().getBlock().getRelative(BlockFace.DOWN);
		if (!this.bPlayer.isTremorSensing()) {
			if (this.block != null) {
				this.remove();
			}
			return;
		}

		final boolean isBendable = this.isEarthbendable(standBlock);

		if (isBendable && this.block == null) {
			this.block = standBlock;
			this.player.sendBlockChange(this.block.getLocation(), Material.GLOWSTONE, (byte) 1);
		} else if (isBendable && !this.block.equals(standBlock)) {
			this.revertGlowBlock();
			this.block = standBlock;
			this.player.sendBlockChange(this.block.getLocation(), Material.GLOWSTONE, (byte) 1);
		} else if (this.block == null) {
			return;
		} else if (!this.player.getWorld().equals(this.block.getWorld())) {
			this.remove();
			return;
		} else if (!isBendable) {
			if (this.stickyRange > 0) {
				if (standBlock.getLocation().distanceSquared(this.block.getLocation()) > this.stickyRange * this.stickyRange) {
					this.revertGlowBlock();
				}
			} else {
				this.revertGlowBlock();
			}

			return;
		}
	}

	public void revertGlowBlock() {
		if (this.block != null) {
			this.player.sendBlockChange(this.block.getLocation(), this.block.getType(), this.block.getData());
		}
	}

	@Override
	public void remove() {
		super.remove();
		this.revertGlowBlock();
	}

	@Override
	public void progress() {
		if (!this.bPlayer.canBendIgnoreBindsCooldowns(this) || this.player.getLocation().getBlock().getLightLevel() > this.lightThreshold) {
			this.remove();
			return;
		} else {
			this.tryToSetGlowBlock();
		}
	}

	public static void manage(final TremorsenseConfig config, final Server server) {
		for (final Player player : server.getOnlinePlayers()) {

			if (canTremorSense(player) && !hasAbility(player, Tremorsense.class)) {
				new Tremorsense(config, player, false);
			}
		}
	}

	public static boolean canTremorSense(final Player player) {
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer != null && bPlayer.canBendIgnoreBindsCooldowns(getAbility("Tremorsense"))) {
			return true;
		}

		return false;
	}

	@Deprecated
	/** No longer used; will be removed in the next version. */
	public static Map<Block, Player> getBlocks() {
		return BLOCKS;
	}

	@Override
	public String getName() {
		return "Tremorsense";
	}

	@Override
	public Location getLocation() {
		return this.player != null ? this.player.getLocation() : null;
	}

	@Override
	public long getCooldown() {
		return this.cooldown;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	public byte getLightThreshold() {
		return this.lightThreshold;
	}

	public void setLightThreshold(final byte lightThreshold) {
		this.lightThreshold = lightThreshold;
	}

	public int getMaxDepth() {
		return this.maxDepth;
	}

	public void setMaxDepth(final int maxDepth) {
		this.maxDepth = maxDepth;
	}

	public int getRadius() {
		return this.radius;
	}

	public void setRadius(final int radius) {
		this.radius = radius;
	}

	public Block getBlock() {
		return this.block;
	}

	public void setBlock(final Block block) {
		this.block = block;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}
	
	@Override
	public Class<TremorsenseConfig> getConfigType() {
		return TremorsenseConfig.class;
	}

}
