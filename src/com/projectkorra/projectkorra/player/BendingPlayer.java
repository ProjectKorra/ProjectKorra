package com.projectkorra.projectkorra.player;

import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.cooldown.CooldownManager;
import com.projectkorra.projectkorra.element.Element;
import com.projectkorra.projectkorra.element.ElementManager;
import com.projectkorra.projectkorra.module.ModuleManager;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BendingPlayer
{
	private final BendingPlayerManager manager;
	private final ElementManager elementManager;
	private final CooldownManager cooldownManager;

	private final int playerId;
	private final UUID uuid;
	private final Player player;
	private final String playerName;
	private final long firstLogin;

	private final Set<Element> elements;

	public BendingPlayer(int playerId, UUID uuid, String playerName, long firstLogin)
	{
		this.manager = ModuleManager.getModule(BendingPlayerManager.class);
		this.elementManager = ModuleManager.getModule(ElementManager.class);
		this.cooldownManager = ModuleManager.getModule(CooldownManager.class);

		this.playerId = playerId;
		this.uuid = uuid;
		this.player = manager.getPlugin().getServer().getPlayer(uuid);
		this.playerName = playerName;
		this.firstLogin = firstLogin;

		this.elements = new HashSet<>();
	}

	public void addElement(Element element)
	{
		this.elements.add(element);
	}

	public void setElement(Element element)
	{
		this.elements.clear();
		this.elements.add(element);
	}

	public boolean hasElement(Element element)
	{
		if (element.equals(elementManager.getAvatar()))
		{
			return this.player.hasPermission("bending.avatar");
		}

		return this.elements.contains(element);
	}

	public boolean canBloodbend()
	{
		return this.elements.contains(elementManager.getBlood());
	}

	public boolean canUseHealing()
	{
		return this.elements.contains(elementManager.getHealing());
	}

	public boolean canIcebend()
	{
		return this.elements.contains(elementManager.getIce());
	}

	public boolean canPlantbend()
	{
		return this.elements.contains(elementManager.getPlant());
	}

	public boolean canLavabend()
	{
		return this.elements.contains(elementManager.getLava());
	}

	public boolean canMetalbend()
	{
		return this.elements.contains(elementManager.getMetal());
	}

	public boolean canSandbend()
	{
		return this.elements.contains(elementManager.getSand());
	}

	public boolean canCombustionbend()
	{
		return this.elements.contains(elementManager.getCombustion());
	}

	public boolean canUseLightning()
	{
		return this.elements.contains(elementManager.getLightning());
	}

	public boolean canUseFlight()
	{
		return this.elements.contains(elementManager.getFlight());
	}

	public boolean canUseSpiritual()
	{
		return this.elements.contains(elementManager.getSpiritual());
	}

	public void addCooldown(Ability ability)
	{
		addCooldown(ability, ability.getCooldown());
	}

	public void addCooldown(Ability ability, long duration)
	{
		addCooldown(ability.getName(), duration);
	}

	public void addCooldown(Ability ability, long duration, boolean permanent)
	{
		addCooldown(ability.getName(), duration, permanent);
	}

	public void addCooldown(String abilityName, long duration)
	{
		addCooldown(abilityName, duration, false);
	}

	public void addCooldown(String abilityName, long duration, boolean permanent)
	{
		cooldownManager.addCooldown(this.player, abilityName, duration, permanent);
	}

	public boolean isOnCooldown(Ability ability)
	{
		return isOnCooldown(ability.getName());
	}

	public boolean isOnCooldown(String abilityName)
	{
		return cooldownManager.isOnCooldown(this.player, abilityName);
	}

	public void removeCooldown(Ability ability)
	{
		removeCoolldown(ability.getName());
	}

	public void removeCoolldown(String abilityName)
	{
		cooldownManager.removeCooldown(this.player, abilityName);
	}

	public int getId()
	{
		return this.playerId;
	}

	public long getFirstLogin()
	{
		return this.firstLogin;
	}
}
