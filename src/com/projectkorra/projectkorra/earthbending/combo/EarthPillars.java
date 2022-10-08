package com.projectkorra.projectkorra.earthbending.combo;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.ability.util.ComboUtil;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.earthbending.RaiseEarth;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EarthPillars extends EarthAbility implements ComboAbility {

	@Attribute(Attribute.RADIUS)
	private double radius;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.KNOCKUP)
	private double knockup;
	private double fallThreshold;
	private boolean damaging;
	private boolean firstTime;
	private Map<RaiseEarth, LivingEntity> entities;

	public EarthPillars(final Player player, final boolean fall) {
		super(player);
		this.setFields(fall);

		if (!this.bPlayer.canBendIgnoreBinds(this) || !isEarthbendable(player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType(), true, true, false)) {
			return;
		}

		if (fall) {
			if (player.getFallDistance() < this.fallThreshold) {
				return;
			}
		}

		this.firstTime = true;

		this.start();
	}

	private void setFields(final boolean fall) {
		this.radius = getConfig().getDouble("Abilities.Earth.EarthPillars.Radius");
		this.damage = getConfig().getDouble("Abilities.Earth.EarthPillars.Damage.Value");
		this.knockup = getConfig().getDouble("Abilities.Earth.EarthPillars.Knockup");
		this.damaging = getConfig().getBoolean("Abilities.Earth.EarthPillars.Damage.Enabled");
		this.entities = new HashMap<>();

		if (fall) {
			this.fallThreshold = getConfig().getDouble("Abilities.Earth.EarthPillars.FallThreshold");
			this.damaging = true;
			this.damage *= this.knockup;
			this.radius = this.fallThreshold;
			this.knockup += (this.player.getFallDistance() > this.fallThreshold ? this.player.getFallDistance() : this.fallThreshold) / 100;
		}
	}

	public void affect(final LivingEntity lent) {
		final RaiseEarth re = new RaiseEarth(this.player, lent.getLocation().clone().subtract(0, 1, 0), 3);
		this.entities.put(re, lent);
	}

	@Override
	public void progress() {
		if (this.firstTime) {
			for (final Entity e : GeneralMethods.getEntitiesAroundPoint(this.player.getLocation(), this.radius)) {
				if (e instanceof LivingEntity && e.getEntityId() != this.player.getEntityId() && isEarthbendable(e.getLocation().getBlock().getRelative(BlockFace.DOWN).getType(), true, true, false)) {
					ParticleEffect.BLOCK_DUST.display(e.getLocation(), 10, 1, 0.1, 1, e.getLocation().getBlock().getRelative(BlockFace.DOWN).getBlockData());
					this.affect((LivingEntity) e);
				}
			}

			if (this.entities.isEmpty()) {
				this.remove();
				return;
			}
			this.firstTime = false;
		}

		final List<RaiseEarth> removal = new ArrayList<>();
		for (final RaiseEarth abil : this.entities.keySet()) {
			if (abil.isRemoved() && abil.isStarted()) {
				final LivingEntity lent = this.entities.get(abil);
				if (!lent.isDead()) {
					if (lent instanceof Player && !((Player) lent).isOnline()) {
						continue;
					}
					GeneralMethods.setVelocity(this, lent, new Vector(0, this.knockup, 0));
				}
				if (this.damaging) {
					DamageHandler.damageEntity(lent, applyMetalPowerFactor(this.damage, lent.getLocation().getBlock().getRelative(BlockFace.DOWN)), this);
				}

				removal.add(abil);
			}
		}

		for (final RaiseEarth remove : removal) {
			this.entities.remove(remove);
		}

		if (this.entities.isEmpty()) {
			this.bPlayer.addCooldown(this);
			this.remove();
			return;
		}
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
		return getConfig().getLong("Abilities.Earth.EarthPillars.Cooldown");
	}

	@Override
	public String getName() {
		return "EarthPillars";
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public Object createNewComboInstance(final Player player) {
		return new EarthPillars(player, false);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		return ComboUtil.generateCombinationFromList(this, ConfigManager.defaultConfig.get().getStringList("Abilities.Earth.EarthPillars.Combination"));
	}
}
