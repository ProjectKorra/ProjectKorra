package com.projectkorra.projectkorra.chiblocking.combo;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.MovementHandler;

public class Immobilize extends ChiAbility implements ComboAbility {

	private long duration;
	private long cooldown;
	private Entity target;
	
	public Immobilize(Player player) {
		super(player);
		
		this.cooldown = getConfig().getLong("Abilities.Chi.Immobilize.Cooldown");
		this.duration = getConfig().getLong("Abilities.Chi.Immobilize.ParalyzeDuration");
		target = GeneralMethods.getTargetedEntity(player, 5);
		if (!bPlayer.canBendIgnoreBinds(this)) {
			return;
		}
		if (target == null) {
			remove();
			return;
		} else {
			paralyze(target, duration);
			bPlayer.addCooldown(this);
		}
	}
	
	/**
	 * Paralyzes the target for the given duration. The player will be unable to
	 * move or interact for the duration.
	 * 
	 * @param target The Entity to be paralyzed
	 * @param duration The time in milliseconds the target will be paralyzed
	 */
	private static void paralyze(Entity target, Long duration) {
		MovementHandler mh = new MovementHandler((LivingEntity) target, CoreAbility.getAbility(Immobilize.class));
		mh.stopWithDuration(duration/1000*20, Element.CHI.getColor() + "* Immobilized *");
	}

	@Override
	public String getName() {
		return "Immobilize";
	}

	@Override
	public void progress() {
	}

	@Override
	public boolean isSneakAbility() {
		// TODO Auto-generated method stub
		return true;
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
	public Location getLocation() {
		return target != null ? target.getLocation() : null;
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return new Immobilize(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> immobilize = new ArrayList<>();
		immobilize.add(new AbilityInformation("QuickStrike", ClickType.LEFT_CLICK_ENTITY));
		immobilize.add(new AbilityInformation("SwiftKick", ClickType.LEFT_CLICK_ENTITY));
		immobilize.add(new AbilityInformation("QuickStrike", ClickType.LEFT_CLICK_ENTITY));
		immobilize.add(new AbilityInformation("QuickStrike", ClickType.LEFT_CLICK_ENTITY));
		return immobilize;
	}
	
	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public Entity getTarget() {
		return target;
	}

	public void setTarget(Entity target) {
		this.target = target;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}
	
	@Override
	public String getInstructions() {
		return "QuickStrike > SwiftKick > QuickStrike > QuickStrike";
	}
}
