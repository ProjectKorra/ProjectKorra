package com.projectkorra.projectkorra.chiblocking.combo;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.api.ChiAbility;
import com.projectkorra.projectkorra.ability.api.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.abilities.chi.ImmobilizeConfig;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.MovementHandler;

public class Immobilize extends ChiAbility<ImmobilizeConfig> implements ComboAbility {

	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	private Entity target;

	public Immobilize(final ImmobilizeConfig config, final Player player) {
		super(config, player);

		this.cooldown = config.Cooldown;
		this.duration = config.ParalyzeDuration;
		this.target = GeneralMethods.getTargetedEntity(player, 5);
		if (!this.bPlayer.canBendIgnoreBinds(this)) {
			return;
		}
		if (this.target == null) {
			this.remove();
			return;
		} else {
			if (GeneralMethods.isRegionProtectedFromBuild(this, this.target.getLocation()) || ((this.target instanceof Player) && Commands.invincible.contains(((Player) this.target).getName()))) {
				return;
			}
			paralyze(this.target, this.duration);
			this.bPlayer.addCooldown(this);
		}
	}

	/**
	 * Paralyzes the target for the given duration. The player will be unable to
	 * move or interact for the duration.
	 *
	 * @param target The Entity to be paralyzed
	 * @param duration The time in milliseconds the target will be paralyzed
	 */
	private static void paralyze(final Entity target, final Long duration) {
		final MovementHandler mh = new MovementHandler((LivingEntity) target, CoreAbility.getAbility(Immobilize.class));
		mh.stopWithDuration(duration / 1000 * 20, Element.CHI.getColor() + "* Immobilized *");
	}

	@Override
	public String getName() {
		return "Immobilize";
	}

	@Override
	public void progress() {}

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
		return this.cooldown;
	}

	@Override
	public Location getLocation() {
		return this.target != null ? this.target.getLocation() : null;
	}

	@Override
	public Object createNewComboInstance(final Player player) {
		return new Immobilize(ConfigManager.getConfig(ImmobilizeConfig.class), player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		final ArrayList<AbilityInformation> immobilize = new ArrayList<>();
		immobilize.add(new AbilityInformation("QuickStrike", ClickType.LEFT_CLICK_ENTITY));
		immobilize.add(new AbilityInformation("SwiftKick", ClickType.LEFT_CLICK_ENTITY));
		immobilize.add(new AbilityInformation("QuickStrike", ClickType.LEFT_CLICK_ENTITY));
		immobilize.add(new AbilityInformation("QuickStrike", ClickType.LEFT_CLICK_ENTITY));
		return immobilize;
	}

	public long getDuration() {
		return this.duration;
	}

	public void setDuration(final long duration) {
		this.duration = duration;
	}

	public Entity getTarget() {
		return this.target;
	}

	public void setTarget(final Entity target) {
		this.target = target;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}
	
	@Override
	public Class<ImmobilizeConfig> getConfigType() {
		return ImmobilizeConfig.class;
	}
}
