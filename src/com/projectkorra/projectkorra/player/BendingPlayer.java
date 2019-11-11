package com.projectkorra.projectkorra.player;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.AbilityManager;
import com.projectkorra.projectkorra.ability.PassiveAbilityManager;
import com.projectkorra.projectkorra.ability.legacy.ChiAbility;
import com.projectkorra.projectkorra.ability.bind.AbilityBindManager;
import com.projectkorra.projectkorra.ability.info.AbilityInfo;
import com.projectkorra.projectkorra.ability.api.AvatarAbilityInfo;
import com.projectkorra.projectkorra.cooldown.CooldownManager;
import com.projectkorra.projectkorra.element.Element;
import com.projectkorra.projectkorra.element.ElementManager;
import com.projectkorra.projectkorra.element.SubElement;
import com.projectkorra.projectkorra.module.ModuleManager;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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

	private final Set<Element> toggledElements;

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

		this.toggledElements = new HashSet<>();
	}

	public Set<Element> getElements() {
		return this.elementManager.getElements(this.player);
	}

	protected boolean addElement(Element element) {
		return this.elementManager.addElement(this.player, element);
	}

	protected boolean removeElement(Element element) {
		return this.elementManager.removeElement(this.player, element);
	}

	protected void clearElements() {
		this.elementManager.clearElements(this.player);
	}

	public boolean hasElement(Element element) {
		return this.elementManager.hasElement(this.player, element);
	}

	public boolean canBloodbend() {
		return this.elementManager.hasElement(this.player, this.elementManager.getBlood());
	}

	public boolean canBloodbendAtAnytime() {
		return canBloodbend() && this.player.hasPermission("bending.water.bloodbending.anytime");
	}

	public boolean canUseHealing() {
		return this.elementManager.hasElement(this.player, this.elementManager.getHealing());
	}

	public boolean canIcebend() {
		return this.elementManager.hasElement(this.player, this.elementManager.getIce());
	}

	public boolean canPlantbend() {
		return this.elementManager.hasElement(this.player, this.elementManager.getPlant());
	}

	public boolean canLavabend() {
		return this.elementManager.hasElement(this.player, this.elementManager.getLava());
	}

	public boolean canMetalbend() {
		return this.elementManager.hasElement(this.player, this.elementManager.getMetal());
	}

	public boolean canSandbend() {
		return this.elementManager.hasElement(this.player, this.elementManager.getSand());
	}

	public boolean canCombustionbend() {
		return this.elementManager.hasElement(this.player, this.elementManager.getCombustion());
	}

	public boolean canUseLightning() {
		return this.elementManager.hasElement(this.player, this.elementManager.getLightning());
	}

	public boolean canUseFlight() {
		return this.elementManager.hasElement(this.player, this.elementManager.getFlight());
	}

	public boolean canUseSpiritual() {
		return this.elementManager.hasElement(this.player, this.elementManager.getSpiritual());
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
		return this.abilityBindManager.getBoundAbility(this.player);
	}

	public String getAbility(int slot) {
		return this.abilityBindManager.getAbility(this.player, slot);
	}

	public String[] getAbilities() {
		return this.abilityBindManager.getAbilities(this.player);
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
