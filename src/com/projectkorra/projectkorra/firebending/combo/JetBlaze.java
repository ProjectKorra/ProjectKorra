package com.projectkorra.projectkorra.firebending.combo;

import java.util.ArrayList;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.ComboUtil;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.firebending.FireJet;
import com.projectkorra.projectkorra.util.ParticleEffect;

public class JetBlaze extends FireAbility implements ComboAbility {

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute(Attribute.FIRE_TICK)
	private double fireTicks;
	private ArrayList<LivingEntity> affectedEntities;
	private ArrayList<FireComboStream> tasks;
	@Attribute(Attribute.DURATION)
	private long duration;

	private final FireJet fireJet;

	public JetBlaze(final Player player) {
		super(player);

		this.fireJet = CoreAbility.getAbility(player, FireJet.class);
		if (!this.bPlayer.canBendIgnoreBinds(this)
				|| CoreAbility.hasAbility(player, JetBlast.class) || fireJet == null) {
			return;
		}

		this.affectedEntities = new ArrayList<>();
		this.tasks = new ArrayList<>();

		this.damage = applyModifiersDamage(getConfig().getDouble("Abilities.Fire.JetBlaze.Damage"));
		this.duration = getConfig().getLong("Abilities.Fire.JetBlaze.Duration");
		this.speed = getConfig().getDouble("Abilities.Fire.JetBlaze.Speed");
		this.cooldown = applyModifiersCooldown(getConfig().getLong("Abilities.Fire.JetBlaze.Cooldown"));
		this.fireTicks = getConfig().getDouble("Abilities.Fire.JetBlaze.FireTicks");

		if (this.bPlayer.isAvatarState()) {
			this.cooldown = 0;
			this.damage = getConfig().getDouble("Abilities.Avatar.AvatarState.Fire.JetBlaze.Damage");
			this.fireTicks = getConfig().getDouble("Abilities.Avatar.AvatarState.Fire.JetBlaze.FireTicks");
		}

		this.fireJet.setSpeed(speed);
		this.fireJet.setDuration(duration);
		this.start();
	}

	@Override
	public Object createNewComboInstance(final Player player) {
		return new JetBlaze(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		return ComboUtil.generateCombinationFromList(this, ConfigManager.defaultConfig.get().getStringList("Abilities.Fire.JetBlaze.Combination"));
	}

	@Override
	public void progress() {
		if (this.fireJet.isRemoved()) {
			remove();
			return;
		}

		Vector streamDir = this.player.getVelocity().multiply(-1);
		final FireComboStream fs = new FireComboStream(this.player, this, streamDir, this.player.getLocation(), 5, 1);
		fs.setDensity(8);
		fs.setSpread(1.0F);
		fs.setUseNewParticles(true);
		fs.setCollisionRadius(2);
		fs.setParticleEffect(ParticleEffect.SMOKE_LARGE);
		fs.setDamage(this.damage);
		fs.setFireTicks(this.fireTicks);
		fs.runTaskTimer(ProjectKorra.plugin, 0, 1L);
		this.tasks.add(fs);
		this.player.getWorld().playSound(this.player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1, 0F);
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
		this.bPlayer.addCooldown("JetBlaze", this.cooldown);
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
		return "JetBlaze";
	}

	@Override
	public Location getLocation() {
		return this.player.getLocation();
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	public ArrayList<LivingEntity> getAffectedEntities() {
		return this.affectedEntities;
	}
}
