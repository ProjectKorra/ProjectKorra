package com.projectkorra.projectkorra.firebending.combo;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.firebending.FireJet;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.ParticleEffect;

public class JetBlast extends FireAbility implements ComboAbility {

	private boolean firstTime;
	private long time;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.SPEED)
	private double speed;
	private ArrayList<FireComboStream> tasks;
	@Attribute(Attribute.DURATION)
	private long duration;

	public JetBlast(final Player player) {
		super(player);

		if (!this.bPlayer.canBendIgnoreBinds(this)) {
			return;
		}

		this.firstTime = true;
		this.time = System.currentTimeMillis();
		this.tasks = new ArrayList<>();

		this.speed = getConfig().getDouble("Abilities.Fire.JetBlast.Speed");
		this.cooldown = getConfig().getLong("Abilities.Fire.JetBlast.Cooldown");
		this.duration = getConfig().getLong("Abilities.Fire.JetBlast.Duration");

		if (this.bPlayer.isAvatarState()) {
			this.cooldown = 0;
		}

		this.start();
	}

	@Override
	public Object createNewComboInstance(final Player player) {
		return new JetBlast(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		final ArrayList<AbilityInformation> jetBlast = new ArrayList<>();
		jetBlast.add(new AbilityInformation("FireJet", ClickType.SHIFT_DOWN));
		jetBlast.add(new AbilityInformation("FireJet", ClickType.SHIFT_UP));
		jetBlast.add(new AbilityInformation("FireJet", ClickType.SHIFT_DOWN));
		jetBlast.add(new AbilityInformation("FireJet", ClickType.SHIFT_UP));
		jetBlast.add(new AbilityInformation("FireShield", ClickType.SHIFT_DOWN));
		jetBlast.add(new AbilityInformation("FireShield", ClickType.SHIFT_UP));
		jetBlast.add(new AbilityInformation("FireJet", ClickType.LEFT_CLICK));
		return jetBlast;
	}

	@Override
	public void progress() {
		if (System.currentTimeMillis() - this.time > this.duration) {
			this.remove();
			return;
		} else if (hasAbility(this.player, FireJet.class)) {
			if (this.firstTime) {
				if (this.bPlayer.isOnCooldown("JetBlast") && !this.bPlayer.isAvatarState()) {
					this.remove();
					return;
				}

				this.bPlayer.addCooldown("JetBlast", this.cooldown);
				this.firstTime = false;
				final float spread = 0F;
				ParticleEffect.EXPLOSION_LARGE.display(this.player.getLocation(), 1, spread, spread, spread, 0);
				this.player.getWorld().playSound(this.player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 15, 0F);
			}
			final FireJet fj = getAbility(this.player, FireJet.class);
			fj.setSpeed(this.speed);
			fj.setDuration(this.duration);

			final FireComboStream fs = new FireComboStream(this.player, this, this.player.getVelocity().clone().multiply(-1), this.player.getLocation(), 3, 0.5);

			fs.setDensity(1);
			fs.setSpread(0.9F);
			fs.setUseNewParticles(true);
			fs.setCollides(false);
			fs.runTaskTimer(ProjectKorra.plugin, 0, 1L);
			this.tasks.add(fs);
		}
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public long getCooldown() {
		return this.cooldown;
	}

	@Override
	public String getName() {
		return "JetBlast";
	}

	@Override
	public Location getLocation() {
		return this.player.getLocation();
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}
}
