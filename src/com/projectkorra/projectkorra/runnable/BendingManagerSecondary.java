package com.projectkorra.projectkorra.runnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.earthbending.metal.MetalClips;
import com.projectkorra.projectkorra.event.WorldTimeEvent;
import com.projectkorra.projectkorra.util.ActionBar;
import com.projectkorra.projectkorra.util.TempArmor;
import com.projectkorra.projectkorra.util.TempPotionEffect;
import com.projectkorra.projectkorra.waterbending.blood.Bloodbending;

public class BendingManagerSecondary implements Runnable {
	public static HashMap<World, String> events = new HashMap<World, String>(); // holds any current event.

	long time;
	long interval;
	private final HashMap<World, WorldTimeEvent.Time> times = new HashMap<>(); // true if day time

	public BendingManagerSecondary() {
		handleDayNight();
	}


    public void handleDayNight() {
		for (final World world : Bukkit.getServer().getWorlds()) {
			if (ConfigManager.defaultConfig.get().getStringList("Properties.DisabledWorlds").contains(world.getName())) {
				continue;
			}

			WorldTimeEvent.Time from = this.times.get(world);

			WorldTimeEvent.Time to = ElementalAbility.isDay(world) ? WorldTimeEvent.Time.DAY : WorldTimeEvent.Time.NIGHT;

			if (from == null) {
				this.times.put(world, to);
				continue;
			}

			if (from != to) {
				WorldTimeEvent event = new WorldTimeEvent(world, from, to);
				Bukkit.getPluginManager().callEvent(event);

				this.times.put(world, to);
			}
		}
	}

	public void handleCooldowns() {
		for (Map.Entry<UUID, BendingPlayer> entry : BendingPlayer.getPlayers().entrySet()) {
			BendingPlayer bPlayer = entry.getValue();

			bPlayer.removeOldCooldowns();
		}
	}

    @Override
    public void run() {
		TempPotionEffect.progressAll();
		this.handleCooldowns();
		this.handleDayNight();
        TempArmor.cleanup();
		for (final Player player : Bukkit.getOnlinePlayers()) {
			if (Bloodbending.isBloodbent(player)) {
				ActionBar.sendActionBar(Element.BLOOD.getColor() + "* Bloodbent *", player);
			} else if (MetalClips.isControlled(player)) {
				ActionBar.sendActionBar(Element.METAL.getColor() + "* MetalClipped *", player);
			}
		}
    }
}
