package com.projectkorra.ProjectKorra;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.projectkorra.ProjectKorra.airbending.chiblocking.ChiPassive;
import com.projectkorra.ProjectKorra.earthbending.EarthPassive;
import com.projectkorra.ProjectKorra.waterbending.WaterPassive;

public class PKListener implements Listener {

	ProjectKorra plugin;

	public PKListener(ProjectKorra plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Methods.createBendingPlayer(e.getPlayer().getUniqueId(), e.getPlayer().getName());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		Methods.saveBendingPlayer(e.getPlayer().getName());
		BendingPlayer.players.remove(e.getPlayer().getName());
	}

	@EventHandler
	public void onPlayerDamageByPlayer(EntityDamageByEntityEvent e) {
		Entity en = e.getEntity();
		if (en instanceof Player) {
			Player p = (Player) en; // This is the player getting hurt.
			if (e.getDamager() instanceof Player) { // This is the player hitting someone.
				Player damager = (Player) e.getDamager();
				if (Methods.canBendPassive(damager.getName(), Element.Chi)) {
					if (e.getCause() == DamageCause.ENTITY_ATTACK) {
						if (damager.getItemInHand() != null && Methods.isWeapon(damager.getItemInHand().getType()) && !ProjectKorra.plugin.getConfig().getBoolean("Properties.Chi.CanBendWithWeapons")) {
							// Above method checks if the player has an item in their hand, if it is a weapon, and if they can bend with weapons.
							if (Methods.getBoundAbility(damager) == null) { // We don't want them to be able to block chi if an ability is bound.
								if (ChiPassive.willChiBlock(p)) {
									ChiPassive.blockChi(p);
								}
							}
						}
					}
				}
			}
		}
	}
	@EventHandler
	public void onPlayerDamage(EntityDamageEvent e) {
		Entity en = e.getEntity();
		if (en instanceof Player) { // Player is the one being hurt.
			Player p = (Player) en;
			if (e.getCause() == DamageCause.FALL) { // Result is Fall Damage
				if (Methods.canBendPassive(p.getName(), Element.Air)) {
					e.setDamage(0.0);
				}
				if (Methods.canBendPassive(p.getName(), Element.Chi)) {
					double initdamage = e.getDamage();
					double newdamage = e.getDamage() * ChiPassive.FallReductionFactor;
					double finaldamage = initdamage - newdamage;
					e.setDamage(finaldamage);
				}
				if (Methods.canBendPassive(p.getName(), Element.Water)) {
					if (WaterPassive.applyNoFall(p)) {
						e.setDamage(0.0);
					}
				}

				if (Methods.canBendPassive(p.getName(), Element.Earth)) {
					if (EarthPassive.softenLanding(p)) {
						e.setDamage(0.0);
					}
				}
			}
		}
	}

}
