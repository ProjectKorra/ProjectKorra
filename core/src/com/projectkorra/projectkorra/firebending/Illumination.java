package com.projectkorra.projectkorra.firebending;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.attribute.markers.DayNightFactor;
import com.projectkorra.projectkorra.earthbending.Tremorsense;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import org.bukkit.inventory.ItemStack;

public class Illumination extends FireAbility {

	private static final Map<Block, Player> BLOCKS = new ConcurrentHashMap<>();

	@Attribute(Attribute.COOLDOWN) @DayNightFactor(invert = true)
	private long cooldown;
	@Attribute(Attribute.RANGE) @DayNightFactor
	private double range;
	private int lightThreshold;
	private int lightLevel;
	private Block block;
	private int oldLevel;

	public Illumination(final Player player) {
		super(player);

		//Don't apply modifiers here, as this is active at all times and therefore needs
		//to have the fields updated
		this.range = getConfig().getDouble("Abilities.Fire.Illumination.Range");
		this.cooldown = getConfig().getLong("Abilities.Fire.Illumination.Cooldown");
		this.lightThreshold = getConfig().getInt("Abilities.Fire.Illumination.LightThreshold");

		this.lightLevel = getConfig().getInt("Abilities.Fire.Illumination.LightLevel");

		final Illumination oldIllumination = getAbility(player, Illumination.class);
		if (oldIllumination != null) {
			oldIllumination.remove();
			return;
		}

		if (this.bPlayer.isOnCooldown(this)) {
			return;
		}

		if (player.getLocation().getBlock().getLightLevel() < this.lightThreshold && slotsFree(player) && !isTremorsensing()) {
			this.oldLevel = player.getLocation().getBlock().getLightLevel();
			this.bPlayer.addCooldown(this);
			this.set();
			this.start();
		}

	}

	@Override
	public void progress() {
		//A replacement for the canBendIgnoreBindsCooldowns. Since this is used a passive, it should not turn off when bending is toggled.
		if (!this.bPlayer.canBind(this) || this.bPlayer.isChiBlocked() || this.bPlayer.isParalyzed()
				|| this.bPlayer.isBloodbent() || this.bPlayer.isControlledByMetalClips()
				|| getConfig().getStringList("Properties.DisabledWorlds").contains(player.getLocation().getWorld().getName())) {
			this.remove();
			return;
		}

		if (!this.bPlayer.isIlluminating()) {
			this.remove();
			return;
		}

		if (isTremorsensing()) {
			this.remove();
			return;
		}

		if (WaterAbility.isWater(getLocation().getBlock())) {
			this.remove();
			return;
		}

		if (this.block == null) {
			this.remove();
			return;
		}

		ItemStack main = player.getInventory().getItemInMainHand();
		if (!slotsFree(player)) {
			this.remove();
			return;
		}

		Location hand = GeneralMethods.getMainHandLocation(player);
		if (main.getType() != Material.AIR) hand = GeneralMethods.getOffHandLocation(player);

		//Only display every 5 ticks
		if (getRunningTicks() % 3 == 0) playFirebendingParticles(hand, 1, 0, 0, 0);

		this.set();
	}

	public boolean isTremorsensing() {
		return this.bPlayer.hasElement(Element.EARTH) && this.bPlayer.isTremorSensing()
				&& CoreAbility.getAbility(this.player, Tremorsense.class) != null && CoreAbility.getAbility(this.player, Tremorsense.class).isGlowing();
	}

	@Override
	public void remove() {
		super.remove();
		this.revert();
	}

	private void revert() {
		if (this.block != null) {
			BLOCKS.remove(this.block);
			this.block.getWorld().getPlayers().forEach(p -> p.sendBlockChange(this.block.getLocation(), this.block.getBlockData()));
		}
	}

	private void set() {
		Block eyeBlock = this.player.getEyeLocation().getBlock();
		int level = lightLevel;
		if (!eyeBlock.getType().isAir() && (this.block == null || !this.block.equals(eyeBlock))) {
			for (BlockFace face : GeneralMethods.ADJACENT_FACES) {
				if (eyeBlock.getRelative(face).getType().isAir() || (this.block != null && this.block.equals(eyeBlock.getRelative(face)))) {
					eyeBlock = eyeBlock.getRelative(face);
					level = lightLevel - 1; //Make the light level 1 less
					break;
				}
			}

			if (!eyeBlock.getType().isAir()) return; //Could not find suitable block
		}

		BlockData clonedData = Material.LIGHT.createBlockData();
		((Levelled)clonedData).setLevel(level);

		if ((!eyeBlock.equals(this.block))) { //On block change
			this.revert();

			this.oldLevel = player.getLocation().getBlock().getLightLevel();

			if (this.oldLevel > this.lightThreshold) {
				remove();
				return;
			}

			this.block = eyeBlock;
			this.block.getWorld().getPlayers().forEach(p -> p.sendBlockChange(this.block.getLocation(), clonedData));
		} else if (getCurrentTick() % 10 == 0) { //Update to all players in the area every half a second anyway
			//We have to set the block back to the actual one because if they couldn't render the initial block change,
			//(due to it not being in render distance) then no further packets will modify the block either.
			this.block.getWorld().getPlayers().forEach(p -> {
				p.sendBlockChange(this.block.getLocation(), this.block.getBlockData());
				p.sendBlockChange(this.block.getLocation(), clonedData);
			});
		}

		BLOCKS.put(this.block, this.player);
	}

	@Override
	public String getName() {
		return "Illumination";
	}

	@Override
	public Location getLocation() {
		return this.player != null ? this.player.getEyeLocation() : null;
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

	public Block getBlock() {
		return this.block;
	}

	public void setBlock(final Block block) {
		this.block = block;
	}

	public static Map<Block, Player> getBlocks() {
		return BLOCKS;
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
		if (CoreAbility.hasAbility(player, Illumination.class)) return;
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		Illumination dummy = (Illumination) CoreAbility.getAbility(Illumination.class);
		if (!dummy.isEnabled() || !bPlayer.isIlluminating() || !bPlayer.canUsePassive(dummy) || !bPlayer.canBendPassive(dummy)) return;
		if (!slotsFree(player)) return;

		new Illumination(player);
	}

}