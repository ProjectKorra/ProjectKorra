package com.projectkorra.projectkorra.firebending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.FireAbility;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashSet;

public class HeatControlExtinguish extends FireAbility {

	private double range;
	private double radius;
	private long cooldown;
	private Location location;
	
	public HeatControlExtinguish(Player player) {
		super(player);
		
		if (bPlayer.isOnCooldown(this)) {
			return;
		}

		this.range = getConfig().getDouble("Abilities.Fire.HeatControl.Extinguish.Range");
		this.radius = getConfig().getDouble("Abilities.Fire.HeatControl.Extinguish.Radius");
		this.cooldown = GeneralMethods.getGlobalCooldown();
		
		this.range = getDayFactor(this.range);
		this.radius = getDayFactor(this.radius);
		if (isMeltable(player.getTargetBlock((HashSet<Material>) null, (int) range))) {
			new HeatControlMelt(player);
			return;
		}
		
		location = player.getTargetBlock((HashSet<Material>) null, (int) range).getLocation();
		for (Block block : GeneralMethods.getBlocksAroundPoint(location, radius)) {
			Material mat = block.getType();
			if (mat != Material.FIRE) {
				continue;
			} else if (GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
				continue;
			} else if (block.getType() == Material.FIRE) {
				block.setType(Material.AIR);
				block.getWorld().playEffect(block.getLocation(), Effect.EXTINGUISH, 0);
			}
		}

		bPlayer.addCooldown(this);
	}

	public static boolean canBurn(Player player) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return true;
		} else if (bPlayer.getBoundAbilityName().equals("HeatControl") || hasAbility(player, FireJet.class)) {
			player.setFireTicks(-1);
			return false;
		} else if (player.getFireTicks() > 80 && bPlayer.canBendPassive(Element.FIRE)) {
			player.setFireTicks(80);
		}
		return true;
	}

	@Override
	public String getName() {
		return "HeatControl";
	}

	@Override
	public void progress() { 
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}
	
}
