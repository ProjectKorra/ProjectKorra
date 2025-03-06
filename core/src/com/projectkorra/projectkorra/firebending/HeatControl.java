package com.projectkorra.projectkorra.firebending;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.projectkorra.projectkorra.attribute.markers.DayNightFactor;
import com.projectkorra.projectkorra.region.RegionProtection;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.earthbending.lava.LavaFlow;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.SurgeWave;
import com.projectkorra.projectkorra.waterbending.Torrent;
import com.projectkorra.projectkorra.waterbending.WaterManipulation;
import com.projectkorra.projectkorra.waterbending.WaterSpoutWave;
import com.projectkorra.projectkorra.waterbending.combo.IceWave;
import com.projectkorra.projectkorra.waterbending.ice.PhaseChange;
import com.projectkorra.projectkorra.waterbending.multiabilities.WaterArmsSpear;

public class HeatControl extends FireAbility {

	public enum HeatControlType {
		COOK, EXTINGUISH, MELT, SOLIDIFY
	}

	private static final Material[] COOKABLE_MATERIALS = { Material.BEEF, Material.CHICKEN, Material.COD, Material.PORKCHOP, Material.POTATO, Material.RABBIT, Material.MUTTON, Material.SALMON, Material.KELP, Material.WET_SPONGE, Material.CHORUS_FRUIT, Material.STICK };

	private HeatControlType heatControlType;

	// HeatControl Cook variables.

	private long cookTime;
	@Attribute("CookDuration") @DayNightFactor(invert = true)
	private long cookInterval;

	// HeatControl Extinguish variables.
	@Attribute("Extinguish" + Attribute.COOLDOWN) @DayNightFactor(invert = true)
	private long extinguishCooldown;
	@Attribute("Extinguish" + Attribute.RADIUS) @DayNightFactor
	private double extinguishRadius;

	// HeatControl Melt variables.
	@Attribute("Melt" + Attribute.RANGE) @DayNightFactor
	private double meltRange;
	@Attribute("Melt" + Attribute.RADIUS) @DayNightFactor
	private double meltRadius;
	private Location meltLocation;
	private static final Map<Block, TempBlock> MELTED_BLOCKS = new HashMap<>();

	// HeatControl Solidify variables.
	private int solidifyRadius;
	private long solidifyDelay;
	private long solidifyLastBlockTime;
	private long solidifyRevertTime;
	@Attribute("Solidify" + Attribute.RADIUS) @DayNightFactor
	private double solidifyMaxRadius;
	@Attribute("Solidify" + Attribute.RANGE) @DayNightFactor
	private double solidifyRange;
	private boolean solidifyRevert;
	private boolean solidifying;
	private Location solidifyLocation;
	private Random randy;

	public HeatControl(final Player player, final HeatControlType heatControlType) {
		super(player);

		this.heatControlType = heatControlType;
		this.setFields();

		if (this.heatControlType == HeatControlType.COOK) {
			if (!isCookable(player.getInventory().getItemInMainHand().getType())) {
				this.remove();
				new HeatControl(player, HeatControlType.SOLIDIFY);
				return;
			}
			this.start();

		} else if (this.heatControlType == HeatControlType.EXTINGUISH) {
			if (this.bPlayer.isOnCooldown(this.getName() + "Extinguish")) {
				this.remove();
				return;
			}

			this.start();

		} else if (this.heatControlType == HeatControlType.MELT) {
			this.meltLocation = GeneralMethods.getTargetedLocation(player, this.meltRange);
			for (final Block block : GeneralMethods.getBlocksAroundPoint(this.meltLocation, this.meltRadius)) {

				if (isMeltable(block)) {
					melt(player, block);
				}
			}

		} else if (this.heatControlType == HeatControlType.SOLIDIFY) {
			if (!this.bPlayer.canBend(this)) {
				return;
			} else if (getLavaBlock(player, this.solidifyRange) == null) {
				this.remove();
				new HeatControl(player, HeatControlType.EXTINGUISH);
				return;
			}

			this.solidifyLastBlockTime = System.currentTimeMillis();
			this.start();
		}

	}

	public void setFields() {
		if (this.heatControlType == HeatControlType.COOK) {
			this.cookTime = System.currentTimeMillis();
			this.cookInterval = getConfig().getLong("Abilities.Fire.HeatControl.Cook.Interval");
		} else if (this.heatControlType == HeatControlType.EXTINGUISH) {
			this.extinguishCooldown = getConfig().getLong("Abilities.Fire.HeatControl.Extinguish.Cooldown");
			this.extinguishRadius = getConfig().getLong("Abilities.Fire.HeatControl.Extinguish.Radius");
		} else if (this.heatControlType == HeatControlType.MELT) {
			this.meltRange = getConfig().getDouble("Abilities.Fire.HeatControl.Melt.Range");
			this.meltRadius = getConfig().getDouble("Abilities.Fire.HeatControl.Melt.Radius");
		} else if (this.heatControlType == HeatControlType.SOLIDIFY) {
			this.solidifyRadius = 1;
			this.solidifyDelay = 50;
			this.solidifyLastBlockTime = 0;
			this.solidifyMaxRadius = getConfig().getDouble("Abilities.Fire.HeatControl.Solidify.MaxRadius");
			this.solidifyRange = getConfig().getDouble("Abilities.Fire.HeatControl.Solidify.Range");
			this.solidifyRevert = getConfig().getBoolean("Abilities.Fire.HeatControl.Solidify.Revert");
			this.solidifyRevertTime = getConfig().getLong("Abilities.Fire.HeatControl.Solidify.RevertTime");
			this.randy = new Random();
		}
	}

	@Override
	public void progress() {

		if (!this.bPlayer.canBend(this)) {
			this.remove();
			return;
		}

		if (this.heatControlType == HeatControlType.COOK) {

			if (!this.player.isSneaking()) {
				this.remove();
				return;
			}

			if (!isCookable(this.player.getInventory().getItemInMainHand().getType())) {
				this.remove();
				return;
			}

			if (System.currentTimeMillis() - this.cookTime > this.cookInterval) {
				this.cook();
				this.cookTime = System.currentTimeMillis();
				return;
			}

			this.displayCookParticles();

		} else if (this.heatControlType == HeatControlType.EXTINGUISH) {

			if (!this.player.isSneaking()) {
				this.bPlayer.addCooldown(this.getName() + "Extinguish", this.extinguishCooldown);
				this.remove();
				return;
			}

			final Set<Material> blocks = new HashSet<>();
			for (final Material material : getTransparentMaterials()) {
				blocks.add(material);
			}

			for (final Block block : GeneralMethods.getBlocksAroundPoint(this.player.getLocation(), this.extinguishRadius)) {
				final Material material = block.getType();
				if (isFire(material) && !GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {

					block.setType(Material.AIR);
					block.getWorld().playEffect(block.getLocation(), Effect.EXTINGUISH, 0);
				} else if (block.getType() == Material.WET_SPONGE) {
					if (!isWater(block.getRelative(BlockFace.UP)) && !isWater(block.getRelative(BlockFace.DOWN)) && !isWater(block.getRelative(BlockFace.NORTH)) && !isWater(block.getRelative(BlockFace.SOUTH)) && !isWater(block.getRelative(BlockFace.EAST)) && !isWater(block.getRelative(BlockFace.WEST))) {
						dryWetBlocks(block, this, ThreadLocalRandom.current().nextInt(5) == 0);
					}
				}
			}

		} else if (this.heatControlType == HeatControlType.SOLIDIFY) {

			if (this.solidifyRadius >= this.solidifyMaxRadius) {
				this.remove();
				return;
			}

			if (!this.player.isSneaking()) {
				this.remove();
				return;
			}

			if (!this.solidifying) {
				this.solidifying = true;
			}

			final Location targetLocation = GeneralMethods.getTargetedLocation(this.player, this.solidifyRange);

			this.resetLocation(targetLocation);
			final List<Location> area = GeneralMethods.getCircle(this.solidifyLocation, this.solidifyRadius, 3, false, true, 0);
			this.solidify(area);
		}

	}

	private void cook() {
		final ItemStack cooked = this.getCooked(this.player.getInventory().getItemInMainHand());
		final HashMap<Integer, ItemStack> cantFit = this.player.getInventory().addItem(cooked);
		for (final int id : cantFit.keySet()) {
			this.player.getWorld().dropItem(this.player.getEyeLocation(), cantFit.get(id));
		}

		final int amount = this.player.getInventory().getItemInMainHand().getAmount();
		if (amount == 1) {
			this.player.getInventory().clear(this.player.getInventory().getHeldItemSlot());
		} else {
			this.player.getInventory().getItemInMainHand().setAmount(amount - 1);
		}
	}

	private ItemStack getCooked(final ItemStack is) {
		ItemStack cooked = new ItemStack(Material.AIR);
		final Material material = is.getType();

		switch (material) {
			case BEEF:
				cooked = new ItemStack(Material.COOKED_BEEF);
				break;
			case COD:
				cooked = new ItemStack(Material.COOKED_COD);
				break;
			case CHICKEN:
				cooked = new ItemStack(Material.COOKED_CHICKEN);
				break;
			case PORKCHOP:
				cooked = new ItemStack(Material.COOKED_PORKCHOP);
				break;
			case POTATO:
				cooked = new ItemStack(Material.BAKED_POTATO);
				break;
			case MUTTON:
				cooked = new ItemStack(Material.COOKED_MUTTON);
				break;
			case RABBIT:
				cooked = new ItemStack(Material.COOKED_RABBIT);
				break;
			case SALMON:
				cooked = new ItemStack(Material.COOKED_SALMON);
				break;
			case KELP:
				cooked = new ItemStack(Material.DRIED_KELP);
				break;
			case CHORUS_FRUIT:
				cooked = new ItemStack(Material.POPPED_CHORUS_FRUIT);
				break;
			case WET_SPONGE:
				cooked = new ItemStack(Material.SPONGE);
				break;
			case STICK:
				cooked = bPlayer.canUseSubElement(SubElement.BLUE_FIRE) ? new ItemStack(Material.SOUL_TORCH) : new ItemStack(Material.TORCH);
			default:
				break;
		}

		return cooked;
	}

	public void displayCookParticles() {
		playFirebendingParticles(this.player.getLocation().clone().add(0, 1, 0), 3, 0.5, 0.5, 0.5);
		emitFirebendingLight(this.player.getLocation().clone().add(0, 1, 0));
		ParticleEffect.SMOKE_NORMAL.display(this.player.getLocation().clone().add(0, 1, 0), 2, 0.5, 0.5, 0.5);
	}

	public static boolean isCookable(final Material material) {
		return Arrays.asList(COOKABLE_MATERIALS).contains(material);
	}

	public static boolean canBurn(final Player player) {
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return true;
		} else if (bPlayer.getBoundAbilityName().equals("HeatControl") || hasAbility(player, FireJet.class)) {
			player.setFireTicks(-1);
			return false;
		} else if (player.getFireTicks() > 80 && bPlayer.canBendPassive(getAbility(HeatControl.class))) {
			player.setFireTicks(80);
		}
		return true;
	}

	public static void melt(final Player player, final Block block) {
		if (RegionProtection.isRegionProtected(player, block.getLocation(), "HeatControl")) {
			return;
		} else if (!SurgeWave.canThaw(block)) {
			SurgeWave.thaw(block);
			return;
		} else if (!Torrent.canThaw(block)) {
			Torrent.thaw(block);
			return;
		} else if (WaterArmsSpear.canThaw(block)) {
			WaterArmsSpear.thaw(block);
			return;
		}

		if (TempBlock.isTempBlock(block)) {
			final TempBlock tb = TempBlock.get(block);
			if (PhaseChange.getFrozenBlocksMap().containsKey(tb)) {
				new PhaseChange(player, PhaseChange.PhaseChangeType.MELT).melt(tb.getBlock());
			}
		}

		WaterSpoutWave.thaw(block);
		IceWave.thaw(block);

		if (isMeltable(block) && !TempBlock.isTempBlock(block) && WaterManipulation.canPhysicsChange(block)) {
			if (isSnow(block)) {
				block.setType(Material.AIR);
				return;
			} else {
				final TempBlock tb = new TempBlock(block, Material.WATER);
				MELTED_BLOCKS.put(block, tb);

				new BukkitRunnable() {
					@Override
					public void run() {
						final TempBlock melted = MELTED_BLOCKS.get(block);
						if (melted != null) {
							melted.revertBlock();
						}
						MELTED_BLOCKS.remove(block);
					}
				}.runTaskLater(ProjectKorra.plugin, 5 * 20 * 60);
			}
		}
	}

	public void solidify(final List<Location> area) {
		if (System.currentTimeMillis() < this.solidifyLastBlockTime + this.solidifyDelay) {
			return;
		}

		final List<Block> lava = new ArrayList<Block>();
		for (final Location l : area) {
			if (isLava(l.getBlock())) {
				lava.add(l.getBlock());
			}
		}

		this.solidifyLastBlockTime = System.currentTimeMillis();
		if (lava.size() == 0) {
			this.solidifyRadius++;
			return;
		}

		final Block b = lava.get(this.randy.nextInt(lava.size()));

		final Material tempRevertMaterial = Material.MAGMA_BLOCK;

		final TempBlock tempBlock;
		if (TempBlock.isTempBlock(b)) {
			tempBlock = TempBlock.get(b);
			tempBlock.setType(tempRevertMaterial);
		} else {
			tempBlock = new TempBlock(b, tempRevertMaterial);
		}

		if (LavaFlow.isLavaFlowBlock(tempBlock.getBlock())) {
			new BukkitRunnable() {
				@Override
				public void run() {
					if (tempBlock != null) {
						ParticleEffect.SMOKE_NORMAL.display(tempBlock.getBlock().getLocation().clone().add(0.5, 1, 0.5), 3, 0.1, 0.1, 0.1, 0.01);
						if (HeatControl.this.randy.nextInt(3) == 0) {
							tempBlock.getBlock().getWorld().playSound(tempBlock.getBlock().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5F, 1);
						}

						LavaFlow.removeBlock(tempBlock.getBlock());
					}
				}
			}.runTaskLater(ProjectKorra.plugin, 20);

			return;
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				if (tempBlock != null) {
					final boolean bool = Math.random() > .5 ? true : false;
					if (HeatControl.this.solidifyRevert) {
						if (bool) {
							tempBlock.setType(Material.STONE);
						} else {
							tempBlock.setType(Material.COBBLESTONE);
						}
						tempBlock.setRevertTime(HeatControl.this.solidifyRevertTime);
					} else {
						tempBlock.revertBlock();
						if (bool) {
							tempBlock.getBlock().setType(Material.STONE);
						} else {
							tempBlock.getBlock().setType(Material.COBBLESTONE);
						}
					}

					ParticleEffect.SMOKE_NORMAL.display(tempBlock.getBlock().getLocation().clone().add(0.5, 1, 0.5), 3, 0.1, 0.1, 0.1, 0.01);
					if (HeatControl.this.randy.nextInt(3) == 0) {
						tempBlock.getBlock().getWorld().playSound(tempBlock.getBlock().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5F, 1);
					}
				}
			}
		}.runTaskLater(ProjectKorra.plugin, 20);
	}

	public void resetLocation(final Location loc) {
		if (this.solidifyLocation == null) {
			this.solidifyLocation = loc;
			return;
		}

		if (!loc.equals(this.solidifyLocation)) {
			this.solidifyRadius = 1;
			this.solidifyLocation = loc;
		}
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		if (this.heatControlType != null) {
			return this.heatControlType.equals(HeatControlType.COOK);
		} else {
			return false;
		}
	}

	@Override
	public long getCooldown() {
		return 0;
	}

	@Override
	public String getName() {
		return "HeatControl";
	}

	@Override
	public Location getLocation() {
		return this.player.getLocation();
	}

	public static Block getLavaBlock(final Player player, final double range) {
		final Location location = player.getEyeLocation();
		final Vector vector = location.getDirection().clone().normalize();

		for (double i = 0; i <= range; i++) {
			final Block block = location.clone().add(vector.clone().multiply(i)).getBlock();
			if (GeneralMethods.isRegionProtectedFromBuild(player, location)) {
				continue;
			}
			if (isLava(block)) {
				if (block.getBlockData() instanceof Levelled) {
					if (((Levelled) block.getBlockData()).getLevel() != 0) {
						continue;
					}
				}
				return block;
			}
		}
		return null;
	}

	public static Collection<TempBlock> getMeltedBlocks() {
		return MELTED_BLOCKS.values();

	}

}
