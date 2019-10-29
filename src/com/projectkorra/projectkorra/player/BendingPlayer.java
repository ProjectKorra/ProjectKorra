package com.projectkorra.projectkorra.player;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.AbilityManager;
import com.projectkorra.projectkorra.ability.PassiveAbilityManager;
import com.projectkorra.projectkorra.ability.api.ChiAbility;
import com.projectkorra.projectkorra.ability.bind.AbilityBindManager;
import com.projectkorra.projectkorra.ability.info.AbilityInfo;
import com.projectkorra.projectkorra.ability.info.AvatarAbilityInfo;
import com.projectkorra.projectkorra.cooldown.CooldownManager;
import com.projectkorra.projectkorra.element.Element;
import com.projectkorra.projectkorra.element.ElementManager;
import com.projectkorra.projectkorra.element.SubElement;
import com.projectkorra.projectkorra.module.ModuleManager;
import org.bukkit.entity.Player;

import java.util.*;

public class BendingPlayer {

	private final BendingPlayerManager manager;
	private final ElementManager elementManager;
	private final AbilityManager abilityManager;
	private final PassiveAbilityManager passiveAbilityManager;
	private final AbilityBindManager abilityBindManager;
	private final CooldownManager cooldownManager;

	private final int playerId;
	private final UUID uuid;
	private final Player player;
	private final String playerName;
	private final long firstLogin;

	private final Set<Element> elements;
	private final Set<Element> toggledElements;
	private final String[] abilities;

	private ChiAbility stance;
	private boolean bendingPermanentlyRemoved;
	private boolean toggled;
	private boolean tremorSense;
	private boolean illumination;
	private boolean chiBlocked;
	private long slowTime;

	public BendingPlayer(int playerId, UUID uuid, String playerName, long firstLogin) {
		this.manager = ModuleManager.getModule(BendingPlayerManager.class);
		this.elementManager = ModuleManager.getModule(ElementManager.class);
		this.abilityManager = ModuleManager.getModule(AbilityManager.class);
		this.passiveAbilityManager = ModuleManager.getModule(PassiveAbilityManager.class);
		this.abilityBindManager = ModuleManager.getModule(AbilityBindManager.class);
		this.cooldownManager = ModuleManager.getModule(CooldownManager.class);

		this.playerId = playerId;
		this.uuid = uuid;
		this.player = manager.getPlugin().getServer().getPlayer(uuid);
		this.playerName = playerName;
		this.firstLogin = firstLogin;

		this.elements = new HashSet<>();
		this.toggledElements = new HashSet<>();
		this.abilities = new String[9];
	}

	public Set<Element> getElements() {
		return new HashSet<>(this.elements);
	}

	public boolean addElement(Element element) {
		return this.elements.add(element);
	}

	public boolean removeElement(Element element) {
		return this.elements.remove(element);
	}

	public void clearElements() {
		this.elements.clear();
	}

	public boolean hasElement(Element element) {
		if (element.equals(this.elementManager.getAvatar())) {
			return this.player.hasPermission("bending.avatar");
		}

		return this.elements.contains(element);
	}

	public boolean canBloodbend() {
		return this.elements.contains(this.elementManager.getBlood());
	}

	public boolean canUseHealing() {
		return this.elements.contains(this.elementManager.getHealing());
	}

	public boolean canIcebend() {
		return this.elements.contains(this.elementManager.getIce());
	}

	public boolean canPlantbend() {
		return this.elements.contains(this.elementManager.getPlant());
	}

	public boolean canLavabend() {
		return this.elements.contains(this.elementManager.getLava());
	}

	public boolean canMetalbend() {
		return this.elements.contains(this.elementManager.getMetal());
	}

	public boolean canSandbend() {
		return this.elements.contains(this.elementManager.getSand());
	}

	public boolean canCombustionbend() {
		return this.elements.contains(this.elementManager.getCombustion());
	}

	public boolean canUseLightning() {
		return this.elements.contains(this.elementManager.getLightning());
	}

	public boolean canUseFlight() {
		return this.elements.contains(this.elementManager.getFlight());
	}

	public boolean canUseSpiritual() {
		return this.elements.contains(this.elementManager.getSpiritual());
	}

	public boolean isElementToggled(Element element) {
		return this.toggledElements.contains(element);
	}

	public void toggleElement(Element element) {
		if (this.toggledElements.contains(element)) {
			this.toggledElements.remove(element);
		} else {
			this.toggledElements.add(element);
		}
	}

	public String getBoundAbility() {
		int slot = this.player.getInventory().getHeldItemSlot();
		return this.abilities[slot];
	}

	public String getAbility(int slot) {
		return this.abilities[slot];
	}

	public List<String> getAbilities() {
		return Arrays.asList(this.abilities);
	}

	public boolean canBind(AbilityInfo abilityInfo) {
		if (abilityInfo == null || !this.player.isOnline()) {
			return false;
		}

		if (!this.player.hasPermission("bending.ability." + abilityInfo.getName())) {
			return false;
		}

		Element element = abilityInfo.getElement();

		if (!hasElement(element) && !(abilityInfo instanceof AvatarAbilityInfo && !((AvatarAbilityInfo) abilityInfo).requireAvatar())) {
			return false;
		}

		if (element instanceof SubElement) {
			if (!hasElement(((SubElement) element).getParent())) {
				return false;
			}
		}

		return true;
	}

	public void setAbilities(String[] abilities) {
		System.arraycopy(abilities, 0, this.abilities, 0, 9);
	}

	public void setAbility(int slot, String abilityName) {
		this.abilities[slot] = abilityName;
	}

	public long getCooldown(Ability ability) {
		return getCooldown(ability.getName());
	}

	public long getCooldown(String abilityName) {
		return this.cooldownManager.getCooldown(this.player, abilityName);
	}

	public void addCooldown(Ability ability) {
		addCooldown(ability, ability.getCooldown());
	}

	public void addCooldown(Ability ability, long duration) {
		addCooldown(ability.getName(), duration);
	}

	public void addCooldown(Ability ability, long duration, boolean permanent) {
		addCooldown(ability.getName(), duration, permanent);
	}

	public void addCooldown(String abilityName, long duration) {
		addCooldown(abilityName, duration, false);
	}

	public void addCooldown(String abilityName, long duration, boolean permanent) {
		this.cooldownManager.addCooldown(this.player, abilityName, duration, permanent);
	}

	public boolean isOnCooldown(Ability ability) {
		return isOnCooldown(ability.getName());
	}

	public boolean isOnCooldown(String abilityName) {
		return this.cooldownManager.isOnCooldown(this.player, abilityName);
	}

	public void removeCooldown(Ability ability) {
		removeCooldown(ability.getName());
	}

	public void removeCooldown(String abilityName) {
		this.cooldownManager.removeCooldown(this.player, abilityName);
	}

	public boolean canCurrentlyBendWithWeapons() {
		if (getBoundAbility() == null) {
			return false;
		}

		if (this.player.getInventory().getItemInMainHand() == null) {
			return true;
		}

		boolean noWeaponElement = true; // GeneralMethods.getElementsWithNoWeaponBending().contains(this.abilityManager.getAbility())

		if (!noWeaponElement) {
			return true;
		}

		boolean hasWeapon = GeneralMethods.isWeapon(this.player.getInventory().getItemInMainHand().getType());

		return !hasWeapon;
	}

	public ChiAbility getStance() {
		return this.stance;
	}

	public void setStance(ChiAbility stance) {
		this.stance = stance;
	}

	public boolean isBendingPermanentlyRemoved() {
		return this.bendingPermanentlyRemoved;
	}

	protected void setBendingPermanentlyRemoved(boolean bendingPermanentlyRemoved) {
		this.bendingPermanentlyRemoved = bendingPermanentlyRemoved;
	}

	public boolean isToggled() {
		return this.toggled;
	}

	public void toggleBending() {
		this.toggled = !this.toggled;
		this.passiveAbilityManager.registerPassives(this.player);
	}

	public boolean isTremorSensing() {
		return this.tremorSense;
	}

	public void toggleTremorSense() {
		this.tremorSense = !this.tremorSense;
	}

	public boolean isIlluminating() {
		return this.illumination;
	}

	public void toggleIllumination() {
		this.illumination = !this.illumination;
	}

	public boolean isChiBlocked() {
		return this.chiBlocked;
	}

	public void blockChi() {
		this.chiBlocked = true;
	}

	public void unblockChi() {
		this.chiBlocked = false;
	}

	public boolean canBeSlowed() {
		return System.currentTimeMillis() > this.slowTime;
	}

	public void slow(long cooldown) {
		this.slowTime = System.currentTimeMillis() + cooldown;
	}

	public int getId() {
		return this.playerId;
	}

	public long getFirstLogin() {
		return this.firstLogin;
	}
}
