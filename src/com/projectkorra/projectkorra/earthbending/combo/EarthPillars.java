package com.projectkorra.projectkorra.earthbending.combo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.airbending.Suffocate;
import com.projectkorra.projectkorra.earthbending.RaiseEarth;
import com.projectkorra.projectkorra.util.ActionBar;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.ParticleEffect.BlockData;

public class EarthPillars extends EarthAbility implements ComboAbility {
	public double radius, damage, power, fallThreshold;
	public boolean damaging, stun;
	public long stunDuration;
	public Map<RaiseEarth, LivingEntity> entities;

	public EarthPillars(Player player, boolean fall) {
		super(player);
		setFields(fall);
		
		if (!bPlayer.canBendIgnoreBinds(this))return;
		if (!isEarthbendable(player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType(), true, true, false)) return;
		
		if (fall) {
			if (player.getFallDistance() < fallThreshold) {
				return;
			}
		}
		
		for (Entity e : GeneralMethods.getEntitiesAroundPoint(player.getLocation(), radius)) {
			if (e instanceof LivingEntity && e.getEntityId() != player.getEntityId() && isEarthbendable(e.getLocation().getBlock().getRelative(BlockFace.DOWN).getType(), true, true, false)) {
				ParticleEffect.BLOCK_DUST.display(new BlockData(e.getLocation().clone().subtract(0, 1, 0).getBlock().getType(), (byte)0), 1f, 0.1f, 1f, 0, 6, e.getLocation(), 255);
				affect((LivingEntity)e);
			}
		}
		
		bPlayer.addCooldown(this);
		start();
	}
	
	private void setFields(boolean fall) {
		this.radius = getConfig().getDouble("Abilities.Earth.EarthPillars.Radius");
		this.damage = getConfig().getDouble("Abilities.Earth.EarthPillars.Damage.Value");
		this.power = getConfig().getDouble("Abilities.Earth.EarthPillars.Power");
		this.damaging = getConfig().getBoolean("Abilities.Earth.EarthPillars.Damage.Enabled");
		this.stun = getConfig().getBoolean("Abilities.Earth.EarthPillars.Stun.Enabled");
		this.stunDuration = getConfig().getLong("Abilities.Earth.EarthPillars.Stun.Duration");
		this.entities = new HashMap<>();
		
		if (fall) {
			this.fallThreshold = getConfig().getDouble("Abilities.Earth.EarthPillars.FallThreshold");
			this.stun = false;
			this.damaging = true;
			this.damage *= power;
			this.radius = fallThreshold;
			this.power += fallThreshold/100;
		}
	}
	
	public void affect(LivingEntity lent) {
		if (stun) {
			if (lent instanceof Player) {
				stun((Player)lent);
			}
		}
		
		RaiseEarth re = new RaiseEarth(player, lent.getLocation().clone().subtract(0, 1, 0), 3);
		entities.put(re, lent);
	}
	
	public void stun(Player player) {
		if (Suffocate.isChannelingSphere(player)) {
			Suffocate.remove(player);
		}

		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return;
		}
		
		bPlayer.blockChi();
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_HURT, 2, 0);
		
		long start = System.currentTimeMillis();
		new BukkitRunnable() {
			@Override
			public void run() {
				if (!player.isOnline()) {
					bPlayer.unblockChi();
					cancel();
				}
				ActionBar.sendActionBar(Element.EARTH.getColor() + "* Stunned *", player);
				if (System.currentTimeMillis() >= start + stunDuration) {
					bPlayer.unblockChi();
					cancel();
				}
			}
		}.runTaskTimer(ProjectKorra.plugin, 0, 1);
	}

	@Override
	public void progress() {
		List<RaiseEarth> removal = new ArrayList<>();
		for (RaiseEarth abil : entities.keySet()) {
			if (abil.isRemoved() && abil.isStarted()) {
				LivingEntity lent = entities.get(abil);
				if (!lent.isDead()) {
					if (lent instanceof Player && !((Player)lent).isOnline()) continue;
					
					lent.setVelocity(new Vector(0, power, 0));
				}
				if (damaging) {
					DamageHandler.damageEntity(lent, damage, this);
				}
				
				removal.add(abil);
			}
		}
		
		for (RaiseEarth remove : removal) {
			entities.remove(remove);
		}
		
		if (entities.isEmpty()) {
			remove();
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
	public Object createNewComboInstance(Player player) {
		return new EarthPillars(player, false);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> earthPillars = new ArrayList<>();
		earthPillars.add(new AbilityInformation("Shockwave", ClickType.SHIFT_DOWN));
		earthPillars.add(new AbilityInformation("Shockwave", ClickType.SHIFT_UP));
		earthPillars.add(new AbilityInformation("Shockwave", ClickType.SHIFT_DOWN));
		earthPillars.add(new AbilityInformation("Catapult", ClickType.SHIFT_UP));
		return earthPillars;
	}

	@Override
	public String getInstructions() {
		return "Shockwave (Tap sneak) > Shockwave (Hold sneak) > Catapult (Release sneak)";
	}
}
