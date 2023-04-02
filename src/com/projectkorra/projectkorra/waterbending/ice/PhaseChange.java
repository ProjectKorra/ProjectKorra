package com.projectkorra.projectkorra.waterbending.ice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import com.projectkorra.projectkorra.region.RegionProtection;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Snow;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.IceAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.SurgeWall;
import com.projectkorra.projectkorra.waterbending.SurgeWave;
import com.projectkorra.projectkorra.waterbending.Torrent;
import com.projectkorra.projectkorra.waterbending.WaterSpoutWave;
import com.projectkorra.projectkorra.waterbending.multiabilities.WaterArmsSpear;

public class PhaseChange extends IceAbility {

	public static enum PhaseChangeType {
		FREEZE, MELT, CUSTOM;

		@Override
		public String toString() {
			if (this == FREEZE) {
				return "Freeze";
			} else if (this == MELT) {
				return "Melt";
			} else if (this == CUSTOM) {
				return "Custom";
			}
			return "";
		}
	}

	private final List<PhaseChangeType> active_types = new ArrayList<>();
	private static Map<TempBlock, Player> PLAYER_BY_BLOCK = new HashMap<>();
	private static List<Block> BLOCKS = new ArrayList<>();
	private final CopyOnWriteArrayList<TempBlock> blocks = new CopyOnWriteArrayList<>();
	private final Random r = new Random();

	@Attribute(Attribute.SELECT_RANGE)
	private double sourceRange = 8;

	// Freeze Variables.
	@Attribute("Freeze" + Attribute.COOLDOWN)
	private long freezeCooldown = 500;
	@Attribute("Freeze" + Attribute.RADIUS)
	private double freezeRadius = 3;
	@Attribute("FreezeDepth")
	private int depth = 1;
	@Attribute("Control" + Attribute.RADIUS)
	private double controlRadius = 25;

	// Melt Variables.
	private Location meltLoc;
	@Attribute("Melt" + Attribute.COOLDOWN)
	private long meltCooldown = 7000;
	private int meltRadius;
	@Attribute("Melt" + Attribute.RADIUS)
	private double meltMaxRadius = 7;
	@Attribute("Melt" + Attribute.SPEED)
	private double meltSpeed = 8;
	private double meltTicks = 0;
	private boolean allowMeltFlow;
	private final CopyOnWriteArrayList<Block> melted_blocks = new CopyOnWriteArrayList<>();

	public PhaseChange(final Player player, final PhaseChangeType type) {
		super(player);
		this.startNewType(type);
		this.start();
	}

	@Override
	public void progress() {
		if (!this.player.isOnline() || this.player.isDead()) {
			this.remove();
			return;
		}

		if (this.active_types.contains(PhaseChangeType.FREEZE)) {
			if (this.blocks.isEmpty()) {
				this.active_types.remove(PhaseChangeType.FREEZE);
				return;
			}

			for (final TempBlock tb : this.blocks) {
				if (tb.getLocation().getWorld() != this.player.getWorld()) {
					tb.revertBlock();
					this.blocks.remove(tb);
					PLAYER_BY_BLOCK.remove(tb);
					BLOCKS.remove(tb.getBlock());
				} else if (tb.getLocation().distanceSquared(this.player.getLocation()) > (this.controlRadius * this.controlRadius)) {
					tb.revertBlock();
					this.blocks.remove(tb);
					PLAYER_BY_BLOCK.remove(tb);
					BLOCKS.remove(tb.getBlock());
				}
			}
		}

		if (this.active_types.contains(PhaseChangeType.MELT)) {
			if (!this.player.isSneaking() || !this.bPlayer.canBend(this)) {
				this.active_types.remove(PhaseChangeType.MELT);
				this.bPlayer.addCooldown("PhaseChangeMelt", this.meltCooldown);
				this.meltRadius = 1;
				this.meltTicks = 0;
				return;
			}
			if (this.meltRadius >= this.meltMaxRadius) {
				this.meltRadius = 1;
			}
			final Location l = GeneralMethods.getTargetedLocation(this.player, this.sourceRange);
			this.resetMeltLocation(l);
			this.meltArea(l, this.meltRadius);
		}

		if (this.active_types.contains(PhaseChangeType.CUSTOM)) {
			for (final TempBlock tb : this.blocks) {
				if (tb.getLocation().getWorld() != this.player.getWorld()) {
					tb.revertBlock();
					this.blocks.remove(tb);
					PLAYER_BY_BLOCK.remove(tb);
					BLOCKS.remove(tb.getBlock());
				} else if (tb.getLocation().distanceSquared(this.player.getLocation()) > (this.controlRadius * this.controlRadius)) {
					tb.revertBlock();
					this.blocks.remove(tb);
					PLAYER_BY_BLOCK.remove(tb);
					BLOCKS.remove(tb.getBlock());
				}
			}
		}

		if (this.active_types.isEmpty()) {
			this.remove();
		}
	}

	public void startNewType(final PhaseChangeType type) {
		if (type == PhaseChangeType.MELT) {
			if (this.bPlayer.isOnCooldown("PhaseChangeMelt")) {
				return;
			}
		}

		this.active_types.add(type);
		this.setFields(type);
	}

	public void setFields(final PhaseChangeType type) {

		this.sourceRange = applyModifiers(getConfig().getInt("Abilities.Water.PhaseChange.SourceRange"));

		switch (type) {
			case FREEZE:
				this.depth = (int) applyModifiers(getConfig().getInt("Abilities.Water.PhaseChange.Freeze.Depth"));
				this.controlRadius = applyModifiers(getConfig().getDouble("Abilities.Water.PhaseChange.Freeze.ControlRadius"));
				this.freezeCooldown = applyInverseModifiers(getConfig().getLong("Abilities.Water.PhaseChange.Freeze.Cooldown"));
				this.freezeRadius = applyModifiers(getConfig().getInt("Abilities.Water.PhaseChange.Freeze.Radius"));

				this.freezeArea(GeneralMethods.getTargetedLocation(this.player, this.sourceRange));
				return;
			case MELT:
				this.meltRadius = 1;
				this.meltCooldown = applyInverseModifiers(getConfig().getLong("Abilities.Water.PhaseChange.Melt.Cooldown"));
				this.meltSpeed = applyModifiers(getConfig().getDouble("Abilities.Water.PhaseChange.Melt.Speed"));
				this.meltMaxRadius = applyModifiers(getConfig().getDouble("Abilities.Water.PhaseChange.Melt.Radius"));
				this.allowMeltFlow = getConfig().getBoolean("Abilities.Water.PhaseChange.Melt.AllowFlow");
				return;
			case CUSTOM:
				this.depth = (int) applyModifiers(getConfig().getInt("Abilities.Water.PhaseChange.Freeze.Depth"));
				this.controlRadius = applyModifiers(getConfig().getDouble("Abilities.Water.PhaseChange.Freeze.ControlRadius"));
				this.freezeCooldown = applyInverseModifiers(getConfig().getLong("Abilities.Water.PhaseChange.Freeze.Cooldown"));
				this.freezeRadius = applyModifiers(getConfig().getDouble("Abilities.Water.PhaseChange.Freeze.Radius"));

				this.meltRadius = 1;
				this.meltCooldown = applyInverseModifiers(getConfig().getLong("Abilities.Water.PhaseChange.Melt.Cooldown"));
				this.meltSpeed = applyModifiers(getConfig().getDouble("Abilities.Water.PhaseChange.Melt.Speed"));
				this.meltMaxRadius = applyModifiers(getConfig().getDouble("Abilities.Water.PhaseChange.Melt.Radius"));
				this.allowMeltFlow = getConfig().getBoolean("Abilities.Water.PhaseChange.Melt.AllowFlow");
		}
	}

	public void resetMeltLocation(final Location loc) {
		if (this.meltLoc == null) {
			this.meltLoc = loc;
			return;
		}

		if (this.meltLoc.getWorld().equals(loc.getWorld()) && this.meltLoc.distance(loc) < 1) {
			return;
		}

		if (!loc.equals(this.meltLoc)) {
			this.meltLoc = loc;
			this.meltRadius = 1;
		}
	}

	public ArrayList<BlockFace> getBlockFacesTowardsPlayer(final Location center) {
		final ArrayList<BlockFace> faces = new ArrayList<>();
		final Vector toPlayer = GeneralMethods.getDirection(center, this.player.getEyeLocation());
		final double[] vars = { toPlayer.getX(), toPlayer.getY(), toPlayer.getZ() };
		for (int i = 0; i < 3; i++) {
			if (vars[i] != 0) {
				faces.add(GeneralMethods.getBlockFaceFromValue(i, vars[i]));
			} else {
				continue;
			}
		}
		return faces;
	}

	public ArrayList<Block> getBlocksToFreeze(final Location center, final double radius) {
		final ArrayList<Block> blocks = new ArrayList<>();
		for (final Location l : GeneralMethods.getCircle(center, (int)radius, this.depth, false, true, 0)) {
			final Block b = l.getBlock();
			loop: for (int i = 1; i <= this.depth; i++) {
				for (final BlockFace face : this.getBlockFacesTowardsPlayer(center)) {
					if (ElementalAbility.isAir(b.getRelative(face, i))) {
						blocks.add(b);
						break loop;
					}
				}
			}
		}
		return blocks;
	}

	public void freezeArea(final Location center, final double radius, final PhaseChangeType type) {
		if (type == PhaseChangeType.FREEZE) {
			if (this.bPlayer.isOnCooldown("PhaseChangeFreeze")) {
				return;
			}
		}

		if (this.depth > 1) {
			center.subtract(0, this.depth - 1, 0);
		}

		final ArrayList<Block> toFreeze = this.getBlocksToFreeze(center, radius);
		for (final Block b : toFreeze) {
			this.freeze(b);
		}

		if (!this.blocks.isEmpty()) {
			if (type == PhaseChangeType.FREEZE) {
				this.bPlayer.addCooldown("PhaseChangeFreeze", this.freezeCooldown);
			}
		}
	}

	public void freezeArea(final Location center, final double radius) {
		this.freezeArea(center, radius, PhaseChangeType.FREEZE);
	}

	public void freezeArea(final Location center, final PhaseChangeType type) {
		this.freezeArea(center, this.freezeRadius, type);
	}

	public void freezeArea(final Location center) {
		this.freezeArea(center, this.freezeRadius);
	}

	public void freeze(final Block b) {
		if (b.getWorld() != this.player.getWorld()) {
			return;
		}

		if (b.getLocation().distanceSquared(this.player.getLocation()) > this.controlRadius * this.controlRadius) {
			return;
		}

		if (RegionProtection.isRegionProtected(this.player, b.getLocation(), this)) {
			return;
		}

		if (!isWater(b)) {
			return;
		}

		TempBlock tb = null;

		if (TempBlock.isTempBlock(b)) {
			tb = TempBlock.get(b);
			if (this.melted_blocks.contains(tb.getBlock())) {
				this.melted_blocks.remove(tb.getBlock());
				tb.revertBlock();
				tb.setType(Material.ICE);
			}
		}
		if (tb == null) {
			tb = new TempBlock(b, Material.ICE);
		}
		this.blocks.add(tb);
		PLAYER_BY_BLOCK.put(tb, this.player);
		BLOCKS.add(tb.getBlock());
		playIcebendingSound(b.getLocation());
	}

	public void meltArea(final Location center, final int radius) {
		final List<Block> ice = new ArrayList<Block>();
		for (final Location l : GeneralMethods.getCircle(center, radius, 3, true, true, 0)) {
			if (isIce(l.getBlock()) || isSnow(l.getBlock())) {
				ice.add(l.getBlock());
			}
		}

		this.meltTicks += this.meltSpeed / 20;

		for (int i = 0; i < this.meltTicks % (this.meltSpeed); i++) {
			if (ice.size() == 0) {
				this.meltRadius++;
				return;
			}

			final Block b = ice.get(this.r.nextInt(ice.size()));
			this.melt(b);
			ice.remove(b);
		}
	}

	public void meltArea(final Location center) {
		this.meltArea(center, this.meltRadius);
	}

	public void melt(final Block b) {
		if (b.getWorld() != this.player.getWorld()) {
			return;
		}
		if (b.getLocation().distanceSquared(this.player.getLocation()) > this.controlRadius * this.controlRadius) {
			return;
		}
		if (RegionProtection.isRegionProtected(this.player, b.getLocation(), this)) {
			return;
		}
		if (SurgeWall.getWallBlocks().containsKey(b)) {
			return;
		}
		if (SurgeWave.isBlockWave(b)) {
			return;
		}
		if (!SurgeWave.canThaw(b)) {
			SurgeWave.thaw(b);
			return;
		}
		if (!Torrent.canThaw(b)) {
			Torrent.thaw(b);
			return;
		}
		if (WaterArmsSpear.canThaw(b)) {
			WaterArmsSpear.thaw(b);
			return;
		}
		if (WaterSpoutWave.canThaw(b)) {
			WaterSpoutWave.thaw(b);
			return;
		}
		if (TempBlock.isTempBlock(b)) {
			final TempBlock tb = TempBlock.get(b);

			if (!isIce(tb.getBlock()) && !isSnow(tb.getBlock())) {
				return;
			}

			if (PLAYER_BY_BLOCK.containsKey(tb)) {
				thaw(tb);
			}

			if (b.getType() == Material.SNOW) {
				if (b.getBlockData() instanceof Snow) {
					final Snow snow = (Snow) b.getBlockData();
					if (snow.getLayers() == snow.getMinimumLayers()) {
						tb.revertBlock();
						new TempBlock(b, Material.AIR.createBlockData(), 120 * 1000L);
					} else {
						tb.revertBlock();
						snow.setLayers(snow.getLayers() - 1);
						new TempBlock(b, snow, 120 * 1000L);
					}
				}
			}

			if (isIce(tb.getBlock()) && ElementalAbility.isWater(tb.getState().getBlockData())) {
				tb.revertBlock();
			}
		} else if (isWater(b)) {
			// Figure out what to do here also.
		} else if (isIce(b)) {
			if (b.getWorld().getEnvironment() == World.Environment.NETHER) {
				if (this.allowMeltFlow) {
					b.setType(Material.AIR);
				} else {
					new TempBlock(b, Material.AIR);
				}
			} else {
				if (this.allowMeltFlow) {
					b.setType(Material.WATER);
					b.setBlockData(GeneralMethods.getWaterData(0));
				} else {
					new TempBlock(b, Material.WATER);
				}
			}

			this.melted_blocks.add(b);
		} else if (b.getType() == Material.SNOW_BLOCK || b.getType() == Material.SNOW) {
			if (b.getBlockData() instanceof Snow) {
				final Snow snow = (Snow) b.getBlockData();
				if (snow.getLayers() == snow.getMinimumLayers()) {
					new TempBlock(b, Material.AIR.createBlockData(), 120 * 1000L);
				} else {
					snow.setLayers(snow.getLayers() - 1);
					new TempBlock(b, snow, 120 * 1000L);
				}
			}

			this.melted_blocks.add(b);
		}
		playWaterbendingSound(b.getLocation());
	}

	/**
	 * Only works with PhaseChange frozen blocks!
	 *
	 * @param tb TempBlock being thawed
	 * @return true when it is thawed successfully
	 */
	public static boolean thaw(final TempBlock tb) {
		if (!PLAYER_BY_BLOCK.containsKey(tb)) {
			return false;
		} else {
			final Player p = PLAYER_BY_BLOCK.get(tb);
			final PhaseChange pc = getAbility(p, PhaseChange.class);
			if (pc == null) {
				return false;
			}
			PLAYER_BY_BLOCK.remove(tb);
			BLOCKS.remove(tb.getBlock());
			if (pc.getFrozenBlocks() != null) {
				pc.getFrozenBlocks().remove(tb);
				tb.revertBlock();
				return true;
			}
			return false;
		}
	}

	/**
	 * Only works if the block is a {@link TempBlock} and PhaseChange frozen!
	 *
	 * @param b Block being thawed
	 * @return false if not a {@link TempBlock}
	 */
	public static boolean thaw(final Block b) {
		if (!TempBlock.isTempBlock(b)) {
			return false;
		}
		final TempBlock tb = TempBlock.get(b);
		return thaw(tb);
	}

	public static Map<TempBlock, Player> getFrozenBlocksMap() {
		return PLAYER_BY_BLOCK;
	}

	public static List<Block> getFrozenBlocksAsBlock() {
		return BLOCKS;
	}

	public void revertFrozenBlocks() {
		if (this.active_types.contains(PhaseChangeType.FREEZE)) {
			for (final TempBlock tb : this.blocks) {
				PLAYER_BY_BLOCK.remove(tb);
				BLOCKS.remove(tb.getBlock());
				tb.revertBlock();
			}
			this.blocks.clear();
		}
	}

	public void revertMeltedBlocks() {
		if (this.active_types.contains(PhaseChangeType.MELT)) {
			for (final Block b : this.melted_blocks) {
				if (TempBlock.isTempBlock(b)) {
					TempBlock.get(b).revertBlock();
				} else {
					b.setType(Material.ICE);
				}
			}
			this.melted_blocks.clear();
		}
	}

	@Override
	public void remove() {
		super.remove();
		this.revertFrozenBlocks();
		this.revertMeltedBlocks();
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public long getCooldown() {
		return 0;
	}

	@Override
	public String getName() {
		return "PhaseChange";
	}

	@Override
	public Location getLocation() {
		return null;
	}

	public int getDepth() {
		return this.depth;
	}

	public void setDepth(final int value) {
		this.depth = value;
	}

	public double getSourceRange() {
		return this.sourceRange;
	}

	public void setSourceRange(final double value) {
		this.sourceRange = value;
	}

	public double getFreezeControlRadius() {
		return this.controlRadius;
	}

	public double getMeltRadius() {
		return this.meltRadius;
	}

	public void setFreezeControlRadius(final double value) {
		this.controlRadius = value;
	}

	public CopyOnWriteArrayList<TempBlock> getFrozenBlocks() {
		return this.blocks;
	}

	public CopyOnWriteArrayList<Block> getMeltedBlocks() {
		return this.melted_blocks;
	}

	public List<PhaseChangeType> getActiveTypes() {
		return this.active_types;
	}
}
