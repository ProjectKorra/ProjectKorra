package com.projectkorra.projectkorra.chiblocking;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;

public class ChiCombo extends ChiAbility implements ComboAbility {

	private Player player;
	private String name;
	private Ability ability;

	public ChiCombo(Player player, String name) {
		super(player);
		
		this.name = name;
		
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		}
		
		if (name.equalsIgnoreCase("Immobilize")) {
			this.ability = new Immobilize(player);
		}
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Ability getAbility() {
		return ability;
	}
	
	public void setAbility(Ability ability) {
		this.ability = ability;
	}
	
	@Override
	public void progress() {	
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
	public Location getLocation() {
		return null;
	}

	@Override
	public String getInstructions() {
		return null;
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return null;
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		return null;
	}
	
	public class Immobilize extends ChiAbility implements ComboAbility {

		private Entity target;
		private long cooldown;		
		private long duration;
		private double distance;

		
		public Immobilize(Player player) {
			super(player);
			
			if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
				return;
			}
			
			this.duration = getConfig().getLong("Abilities.Chi.ChiCombo.Immobilize.Duration");
			this.cooldown = getConfig().getLong("Abilities.Chi.ChiCombo.Immobilize.Cooldown");
			this.distance = getConfig().getDouble("Abilities.Chi.ChiCombo.Immobilize.Distance");
			
			this.target = GeneralMethods.getTargetedEntity(player, distance);
			if (target == null) {
				return;
			}
			
			new Paralyze(this, target, duration, true, true);
		}
		
		public Entity getTarget() {
			return target;
		}
		
		public void setTarget(Entity target) {
			this.target = target;
		}
		
		public long getCooldown() {
			return cooldown;
		}
		
		public void setCooldown(long cooldown) {
			this.cooldown = cooldown;
		}
		
		public long getDuration() {
			return duration;
		}
		
		public void setDuration(long duration)  {
			this.duration = duration;
		}
		
		public double getDistance() {
			return distance;
		}
		
		public void setDistance(double distance) {
			this.distance = distance;
		}
		
		@Override
		public Location getLocation() {
			return target != null ? target.getLocation() : null;
		}

		@Override
		public void progress() {
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
		public String getName() {
			return "Immobilize";
		}

		@Override
		public String getInstructions() {
			return null;
		}

		@Override
		public Object createNewComboInstance(Player player) {
			return null;
		}

		@Override
		public ArrayList<AbilityInformation> getCombination() {
			return null;
		}
	}
}
