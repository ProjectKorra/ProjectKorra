package com.projectkorra.projectkorra.firebending.combo;

import java.util.ArrayList;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.ComboUtil;
import com.projectkorra.projectkorra.attribute.markers.DayNightFactor;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.firebending.FireJet;
import com.projectkorra.projectkorra.util.ParticleEffect;
import org.bukkit.util.Vector;

public class JetBlast extends FireAbility implements ComboAbility {

	@Attribute(Attribute.COOLDOWN) @DayNightFactor(invert = true)
	private long cooldown;
	@Attribute(Attribute.SPEED) @DayNightFactor
	private double speed;
	private ArrayList<FireComboStream> tasks;
	@Attribute(Attribute.DURATION) @DayNightFactor
	private long duration;

	private final FireJet fireJet;

	public JetBlast(final Player player) {
		super(player);

		this.fireJet = CoreAbility.getAbility(player, FireJet.class);
		if (!this.bPlayer.canBendIgnoreBinds(this)
				|| CoreAbility.hasAbility(player, JetBlaze.class) || fireJet == null) {
			return;
		}

		this.tasks = new ArrayList<>();
		this.speed = getConfig().getDouble("Abilities.Fire.JetBlast.Speed");
		this.cooldown = getConfig().getLong("Abilities.Fire.JetBlast.Cooldown");
		this.duration = getConfig().getLong("Abilities.Fire.JetBlast.Duration");

		if (this.bPlayer.isAvatarState()) {
			this.cooldown = 0;
		}

		this.fireJet.setSpeed(speed);
		this.fireJet.setDuration(duration);
		this.start();
		this.playExplosion();
	}

	private void playExplosion() {
		final float spread = 0F;
		ParticleEffect.EXPLOSION_LARGE.display(this.player.getLocation(), 1, spread, spread, spread, 0);
		this.player.getWorld().playSound(this.player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 15, 0F);
	}

	@Override
	public Object createNewComboInstance(final Player player) {
		return new JetBlast(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		return ComboUtil.generateCombinationFromList(this, ConfigManager.defaultConfig.get().getStringList("Abilities.Fire.JetBlast.Combination"));
	}

	@Override
	public void progress() {
		if (this.fireJet.isRemoved()) {
			remove();
			return;
		}

		Vector streamDir = this.player.getVelocity().multiply(-1);
		final FireComboStream fs = new FireComboStream(this.player, this, streamDir, this.player.getLocation(), 3, 0.5);
		fs.setDensity(1);
		fs.setSpread(0.9F);
		fs.setUseNewParticles(true);
		fs.setCollides(false);
		fs.runTaskTimer(ProjectKorra.plugin, 0, 1L);
		this.tasks.add(fs);
	}

	@Override
	public void remove() {
		if (!this.fireJet.isRemoved()) {
			this.fireJet.remove();
		}

		for (final FireComboStream task : this.tasks) {
			task.remove();
		}
		super.remove();
		this.bPlayer.addCooldown("JetBlast", this.cooldown);
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
