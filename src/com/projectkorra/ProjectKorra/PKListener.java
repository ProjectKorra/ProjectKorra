package com.projectkorra.ProjectKorra;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.kitteh.tag.AsyncPlayerReceiveNameTagEvent;

import com.projectkorra.ProjectKorra.chiblocking.ChiPassive;
import com.projectkorra.ProjectKorra.earthbending.EarthPassive;
import com.projectkorra.ProjectKorra.firebending.Enflamed;
import com.projectkorra.ProjectKorra.firebending.FireStream;
import com.projectkorra.ProjectKorra.waterbending.WaterCore;
import com.projectkorra.ProjectKorra.waterbending.WaterPassive;

public class PKListener implements Listener {

	ProjectKorra plugin;

	public PKListener(ProjectKorra plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockFlowTo(BlockFromToEvent event) {
		Block toblock = event.getToBlock();
		Block fromblock = event.getBlock();
		if (Methods.isWater(fromblock)) {
			if (!event.isCancelled()) {
				if (Methods.isAdjacentToFrozenBlock(toblock) || Methods.isAdjacentToFrozenBlock(fromblock)) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Methods.createBendingPlayer(e.getPlayer().getUniqueId(), e.getPlayer().getName());
		Player player = e.getPlayer();
		List<Element> elements = Methods.getBendingPlayer(e.getPlayer().getName()).getElements();
		if (plugin.getConfig().getBoolean("Properties.Chat.ChatPrefixes")) {
			if (elements.size() > 1)
				player.setDisplayName(plugin.getConfig().getString("Properties.Chat.AvatarPrefix") + player.getName());
			else if (elements.get(0).equals(Element.Earth))
				player.setDisplayName(plugin.getConfig().getString("Properties.Chat.EarthPrefix") + player.getName());
			else if (elements.get(0).equals(Element.Air))
				player.setDisplayName(plugin.getConfig().getString("Properties.Chat.AirPrefix") + player.getName());
			else if (elements.get(0).equals(Element.Water))
				player.setDisplayName(plugin.getConfig().getString("Properties.Chat.WaterPrefix") + player.getName());
			else if (elements.get(0).equals(Element.Fire))
				player.setDisplayName(plugin.getConfig().getString("Properties.Chat.FirePrefix") + player.getName());
			else if (elements.get(0).equals(Element.Chi))
				player.setDisplayName(plugin.getConfig().getString("Properties.Chat.ChiPrefix") + player.getName());
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		Methods.saveBendingPlayer(e.getPlayer().getName());
		BendingPlayer.players.remove(e.getPlayer().getName());
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityCombust(EntityCombustEvent event) {
		Entity entity = event.getEntity();
		Block block = entity.getLocation().getBlock();
		if (FireStream.ignitedblocks.containsKey(block) && entity instanceof LivingEntity) {
			new Enflamed(entity, FireStream.ignitedblocks.get(block));
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityDamageEvent(EntityDamageEvent event) {
		Entity entity = event.getEntity();
		if (event.getCause() == DamageCause.FIRE && FireStream.ignitedblocks.containsKey(entity.getLocation().getBlock())) {
			new Enflamed(entity, FireStream.ignitedblocks.get(entity.getLocation().getBlock()));
		}

		if (Enflamed.isEnflamed(entity) && event.getCause() == DamageCause.FIRE_TICK) {
			event.setCancelled(true);
			Enflamed.dealFlameDamage(entity);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockMeltEvent(BlockFadeEvent event) {
		Block block = event.getBlock();
		if (block.getType() == Material.FIRE) {
			return;
		}
		if (FireStream.ignitedblocks.containsKey(block)) {
			FireStream.remove(block);
		}
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

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockPhysics(BlockPhysicsEvent event) {
		Block block = event.getBlock();
		if (TempBlock.isTempBlock(block) || TempBlock.isTouchingTempBlock(block)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockForm(BlockFormEvent event) {
		if (!WaterCore.canPhysicsChange(event.getBlock()))
			event.setCancelled(true);
	}

	public void onNameTag(AsyncPlayerReceiveNameTagEvent e) {
		List<Element> elements = Methods.getBendingPlayer(e.getNamedPlayer().getName()).getElements();
		if (elements.size() > 1)
			e.setTag(ChatColor.LIGHT_PURPLE + e.getNamedPlayer().getName());
		else if (elements.get(0).equals(Element.Earth))
			e.setTag(ChatColor.GREEN + e.getNamedPlayer().getName());
		else if (elements.get(0).equals(Element.Air))
			e.setTag(ChatColor.GRAY + e.getNamedPlayer().getName());
		else if (elements.get(0).equals(Element.Water))
			e.setTag(ChatColor.AQUA + e.getNamedPlayer().getName());
		else if (elements.get(0).equals(Element.Fire))
			e.setTag(ChatColor.RED + e.getNamedPlayer().getName());
		else if (elements.get(0).equals(Element.Chi))
			e.setTag(ChatColor.GOLD + e.getNamedPlayer().getName());
	}
}
