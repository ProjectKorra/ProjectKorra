package com.projectkorra.projectkorra.earthbending.combo;

import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.ability.util.ComboUtil;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.region.RegionProtection;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.earthbending.EarthDome;
import com.projectkorra.projectkorra.util.ParticleEffect;

import java.util.ArrayList;

public class EarthDomeOthers extends EarthAbility implements ComboAbility {

	private Vector direction;
	private double range;
	@Attribute(Attribute.RANGE)
	private double maxRange;
	private Location loc;

	public EarthDomeOthers(final Player player) {
		super(player);

		if (!this.bPlayer.canBendIgnoreBinds(this) ||
			this.bPlayer.isOnCooldown("EarthDome")) {
			return;
		}

		this.loc = player.getLocation().clone();

		if (RegionProtection.isRegionProtected(player, this.loc) ||
			!isEarthbendable(this.loc.getBlock().getRelative(BlockFace.DOWN).getType(), true, true, true)) {
			return;
		}

		this.range = 0;
		this.direction = this.loc.getDirection().setY(0);
		this.maxRange = getConfig().getDouble("Abilities.Earth.EarthDome.Range");
		this.start();
	}

	@Override
	public void progress() {
		if (!this.player.isOnline() || this.player.isDead()) {
			this.remove(true);
			return;
		}
		if (this.range >= this.maxRange) {
			this.remove(true);
			return;
		}
		if (GeneralMethods.isRegionProtectedFromBuild(this.player, this.loc)) {
			this.remove(true);
			return;
		}

		this.range++;
		this.loc.add(this.direction.normalize());
		Block top = GeneralMethods.getTopBlock(this.loc, 2);

		while (!this.isEarthbendable(top)) {
			if (this.isTransparent(top)) {
				top = top.getRelative(BlockFace.DOWN);
			} else {
				this.remove(true);
				return;
			}
		}

		if (!this.isTransparent(top.getRelative(BlockFace.UP))) {
			this.remove(true);
			return;
		}

		this.loc.setY(top.getY() + 1);

		ParticleEffect.CRIT.display(this.loc, 9, 0.4, 0, 0.4, 0.001);
		ParticleEffect.BLOCK_DUST.display(this.loc, 7, 0.2, 0.1, 0.2, 0.001, this.loc.getBlock().getRelative(BlockFace.DOWN).getBlockData());

		for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(this.loc, 2)) {
			if (!(entity instanceof LivingEntity) || entity.getEntityId() == this.player.getEntityId()) {
				continue;
			}

			new EarthDome(this.player, entity.getLocation().clone().subtract(0, 1, 0));
			this.remove(false);
			return;
		}
	}

	public void remove(final boolean cooldown) {
		super.remove();
		if (cooldown) {
			this.bPlayer.addCooldown("EarthDome", getConfig().getLong("Abilities.Earth.EarthDome.Cooldown"));
		}
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
		return 0;
	}

	@Override
	public String getName() {
		return "EarthDomeOthers";
	}

	@Override
	public Location getLocation() {
		return this.loc != null ? this.loc : null;
	}

	@Override
	public boolean isHiddenAbility() {
		return true;
	}

	/**
	 * Accessor Method to get the instructions for using this combo.
	 *
	 * @param player
	 * @return The steps for the combo.
	 */
	@Override
	public Object createNewComboInstance(Player player) {
		return new EarthDomeOthers(player);
	}

	/**
	 * Returns the list of abilities which constitute the combo.
	 *
	 * @return An ArrayList containing the combo's steps.
	 */
	@Override
	public ArrayList<ComboManager.AbilityInformation> getCombination() {
		return ComboUtil.generateCombinationFromList(this, ConfigManager.defaultConfig.get().getStringList("Abilities.Earth.EarthDome.Combination.Others"));
	}
}
