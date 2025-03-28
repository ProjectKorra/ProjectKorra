package com.projectkorra.projectkorra.earthbending;

import com.projectkorra.projectkorra.region.RegionProtection;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.attribute.Attribute;

public class Tremorsense extends EarthAbility {

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

	private boolean canBreak = false;
	private Block breakBlock;
	private boolean glowing;

	public Tremorsense(final Player player, final boolean clicked) {
		super(player);

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
		this.maxDepth = getConfig().getInt("Abilities.Earth.Tremorsense.MaxDepth");
		this.radius = getConfig().getInt("Abilities.Earth.Tremorsense.Radius");
		this.lightThreshold = (byte) getConfig().getInt("Abilities.Earth.Tremorsense.LightThreshold");
		this.cooldown = getConfig().getLong("Abilities.Earth.Tremorsense.Cooldown");
		this.stickyRange = getConfig().getInt("Abilities.Earth.Tremorsense.StickyRange");
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
					if (RegionProtection.isRegionProtected(this, blocki.getLocation())) {
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

		if (standBlock.getBlockData() instanceof Slab) {
			return;
		}
		
		if (isBendable && this.block == null) {
			this.block = standBlock;
			showGlowBlock();
		} else if (isBendable && !this.block.equals(standBlock)) {
			this.revertGlowBlock();
			this.block = standBlock;
			showGlowBlock();
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

	public boolean canBreak() {
		return canBreak;
	}

	public void setUpBreaking() {
		this.canBreak = true;
		this.breakBlock = this.block;
		revertGlowBlock();
	}

	public void revertGlowBlock() {
		if (this.block != null) {
			this.player.sendBlockChange(this.block.getLocation(), this.block.getBlockData());
			this.glowing = false;
		}
	}

	public void showGlowBlock() {
		if (this.block != null) {
			if (this.canBreak && this.block.getX() == this.breakBlock.getX() && this.block.getZ() == this.breakBlock.getZ()) {
				return;
			}
			this.canBreak = false;
			this.player.sendBlockChange(this.block.getLocation(), Material.GLOWSTONE.createBlockData());
			this.glowing = true;
		}
	}

	/**
	 * @return True if Tremorsense has a light right now
	 */
	public boolean isGlowing() {
		return this.glowing;
	}

	@Override
	public void remove() {
		super.remove();
		this.revertGlowBlock();
	}

	@Override
	public void progress() {
		//A replacement for the canBendIgnoreBindsCooldowns. Since this is used a passive, it should not turn off when bending is toggled.
		if (!this.bPlayer.canBind(this) || this.bPlayer.isChiBlocked() || this.bPlayer.isParalyzed()
				|| this.bPlayer.isBloodbent() || this.bPlayer.isControlledByMetalClips()
				|| getConfig().getStringList("Properties.DisabledWorlds").contains(player.getLocation().getWorld().getName())) {
			this.remove();
		} else if (this.player.getLocation().getBlock().getLightLevel() > this.lightThreshold) {
			this.remove();
		} else {
			this.tryToSetGlowBlock();
		}
	}

	public static void manage(final Server server) {
		for (final Player player : server.getOnlinePlayers()) {

			if (canTremorSense(player) && !hasAbility(player, Tremorsense.class)) {
				new Tremorsense(player, false);
			}
		}
	}

	public static boolean canTremorSense(final Player player) {
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		final Tremorsense this_ = (Tremorsense) getAbility(Tremorsense.class);

		if (this_ != null && bPlayer != null && this_.isEnabled() && bPlayer.canBendPassive(this_) && bPlayer.canUsePassive(this_)) {
			return true;
		}

		return false;
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

}
