package com.projectkorra.projectkorra.firebending;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.TempBlock;
import org.bukkit.inventory.ItemStack;

public class Illumination extends FireAbility {

	private static final Map<TempBlock, Player> BLOCKS = new ConcurrentHashMap<>();

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.RANGE)
	private double range;
	private int lightThreshold;
	private int lightLevel;
	private TempBlock block;
	private int oldLevel;

	private static boolean MODERN = GeneralMethods.getMCVersion() >= 1170;
	private static Material LIGHT;

	public Illumination(final Player player) {
		super(player);

		//Don't apply modifiers here, as this is active at all times and therefore needs
		//to have the fields updated
		this.range = getConfig().getDouble("Abilities.Fire.Illumination.Range");
		this.cooldown = getConfig().getLong("Abilities.Fire.Illumination.Cooldown");
		this.lightThreshold = getConfig().getInt("Abilities.Fire.Illumination.LightThreshold");

		if (MODERN) { //If we are in 1.17 and can use light blocks instead of torches
			this.lightLevel = getConfig().getInt("Abilities.Fire.Illumination.LightLevel");

			if (LIGHT == null) {
				LIGHT = Material.getMaterial("LIGHT");
			}
		}

		final Illumination oldIllumination = getAbility(player, Illumination.class);
		if (oldIllumination != null) {
			oldIllumination.remove();
			return;
		}

		if (this.bPlayer.isOnCooldown(this)) {
			return;
		}

		if (player.getLocation().getBlock().getLightLevel() < this.lightThreshold && (!MODERN || slotsFree(player))) {
			this.oldLevel = player.getLocation().getBlock().getLightLevel();
			this.bPlayer.addCooldown(this);
			this.set();
			this.start();
		}

	}

	@Override
	public void progress() {
		if (!this.bPlayer.canBendIgnoreBindsCooldowns(this)) {
			this.remove();
			return;
		}

		if (!this.bPlayer.isIlluminating()) {
			this.remove();
			return;
		}

		if (this.bPlayer.hasElement(Element.EARTH) && this.bPlayer.isTremorSensing()) {
			this.remove();
			return;
		}

		this.oldLevel = player.getLocation().getBlock().getLightLevel();
		if (this.oldLevel > this.lightThreshold) {
			this.remove();
			return;
		}

		if (WaterAbility.isWater(this.player.getEyeLocation().getBlock())) {
			this.remove();
			return;
		}

		if (this.block == null) {
			return;
		}

		if (!this.player.getWorld().equals(this.block.getBlock().getWorld())) {
			this.remove();
			return;
		}

		if (this.player.getLocation().distanceSquared(this.block.getLocation()) > this.range * this.range) {
			this.remove();
			return;
		}

		//If light blocks are supported
		if (MODERN) {
			ItemStack main = player.getInventory().getItemInMainHand();
			if (!slotsFree(player)) {
				this.remove();
				return;
			}

			Location hand = GeneralMethods.getMainHandLocation(player);
			if (main.getType() != Material.AIR) hand = GeneralMethods.getOffHandLocation(player);

			//Only display every 5 ticks
			if (getRunningTicks() % 3 == 0) playFirebendingParticles(hand, 1, 0, 0, 0);
		}

		this.set();
	}

	@Override
	public void remove() {
		super.remove();
		this.revert();
	}

	private void revert() {
		if (this.block != null) {
			BLOCKS.remove(this.block);
			this.block.revertBlock();
		}
	}

	private void set() {
		if (MODERN) { //Light block implementation
			Block eyeBlock = this.player.getEyeLocation().getBlock();
			int level = lightLevel;
			if (!eyeBlock.getType().isAir() && (this.block == null || !this.block.getBlock().equals(eyeBlock))) {
				for (BlockFace face : new BlockFace[] {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
					if (eyeBlock.getRelative(face).getType().isAir() || (this.block != null && this.block.getBlock().equals(eyeBlock.getRelative(face)))) {
						eyeBlock = eyeBlock.getRelative(face);
						level = lightLevel - 1; //Make the light level 1 less
						break;
					}
				}

				if (!eyeBlock.getType().isAir()) return; //Could not find suitable block
			}

			BlockData clonedData = LIGHT.createBlockData();
			((Levelled)clonedData).setLevel(level);
			if (this.block == null || (!eyeBlock.equals(this.block.getBlock()))) {
				this.revert();
				this.block = new TempBlock(eyeBlock, clonedData);
			}
		} else { //Legacy 1.16 illumination
			final Block standingBlock = this.player.getLocation().getBlock();
			final Block standBlock = standingBlock.getRelative(BlockFace.DOWN);
			if (!isIgnitable(standingBlock)) {
				return;
			} else if (this.block != null && standingBlock.equals(this.block.getBlock())) {
				return;
			} else if (Tag.LEAVES.isTagged(standBlock.getType())) {
				return;
			} else if (standingBlock.getType().name().endsWith("_FENCE") || standingBlock.getType().name().endsWith("_FENCE_GATE") || standingBlock.getType().name().endsWith("_WALL") || standingBlock.getType() == Material.IRON_BARS || standingBlock.getType().name().endsWith("_PANE")) {
				return;
			}

			if (this.block == null || !standBlock.equals(this.block.getBlock())) {
				this.revert();
				Material torch = bPlayer.canUseSubElement(SubElement.BLUE_FIRE) ? Material.SOUL_TORCH : Material.TORCH;
				this.block = new TempBlock(standingBlock, torch);
			}
		}

		BLOCKS.put(this.block, this.player);
	}

	@Override
	public String getName() {
		return "Illumination";
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
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	public double getRange() {
		return this.range;
	}

	public void setRange(final double range) {
		this.range = range;
	}

	public static boolean isModern() {
		return MODERN;
	}

	public TempBlock getBlock() {
		return this.block;
	}

	public void setBlock(final TempBlock block) {
		this.block = block;
	}

	public static Map<TempBlock, Player> getBlocks() {
		return BLOCKS;
	}

	/**
	 * Returns whether the block provided is a torch created by Illumination
	 *
	 * @param block The block being tested
	 */
	public static boolean isIlluminationTorch(final Block block) {
		final TempBlock tempBlock = TempBlock.get(block);

		if (tempBlock == null || ((!MODERN && block.getType() != Material.TORCH && block.getType() != Material.SOUL_TORCH) || block.getType() == LIGHT) || !BLOCKS.containsKey(tempBlock)) {
			return false;
		}

		return true;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

	private static boolean slotsFree(Player player) {
		ItemStack main = player.getInventory().getItemInMainHand();
		ItemStack off = player.getInventory().getItemInOffHand();
		return !(main.getType() != Material.AIR && off.getType() != Material.AIR);
	}

	public static void slotChange(Player player) {
		if (!MODERN) return;
		if (CoreAbility.hasAbility(player, Illumination.class)) return;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		Illumination dummy = (Illumination) CoreAbility.getAbility(Illumination.class);
		if (!dummy.isEnabled() || !bPlayer.isIlluminating() || !bPlayer.canUsePassive(dummy) || !bPlayer.canBendPassive(dummy)) return;
		if (!slotsFree(player)) return;

		new Illumination(player);
	}

}
