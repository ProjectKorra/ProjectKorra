package com.projectkorra.projectkorra.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;

public class TempArmor {

	private static Map<LivingEntity, TempArmor> INSTANCES = new ConcurrentHashMap<LivingEntity, TempArmor>();
	private static long defaultDuration = 30000L;

	private LivingEntity entity;
	private long startTime;
	private long duration;
	private BukkitTask endTimer;
	private ItemStack[] oldArmor;
	private ItemStack[] newArmor;
	private CoreAbility ability;
	private boolean removeAbilOnForceRevert = false;

	/**
	 * Creates a set of temporary armor on the player. This armor cannot be
	 * tinkered with, dropped, and will restore the player's old armor when the
	 * duration expires or when {@link #revert()} is called.
	 * 
	 * @param entity The player
	 * @param armorItems The armor that should be set onto the player. Optional
	 *            - can be set later.
	 */
	public TempArmor(LivingEntity entity, ItemStack[] armorItems) {
		this(entity, defaultDuration, null, armorItems);
	}

	/**
	 * Creates a set of temporary armor on the player. This armor cannot be
	 * tinkered with, dropped, and will restore the player's old armor when the
	 * duration expires or when {@link #revert()} is called.
	 * 
	 * @param entity The player
	 * @param ability The ability that is creating the armor
	 * @param armorItems The armor that should be set onto the player. Optional
	 *            - can be set later.
	 */
	public TempArmor(LivingEntity entity, CoreAbility ability, ItemStack[] armorItems) {
		this(entity, defaultDuration, ability, armorItems);
	}

	/**
	 * Creates a set of temporary armor on the player. This armor cannot be
	 * tinkered with, dropped, and will restore the player's old armor when the
	 * duration expires or when {@link #revert()} is called.
	 * 
	 * @param entity The player
	 * @param duration How long the armor is to last. In milliseconds.
	 * @param ability The ability that is creating the armor
	 * @param armorItems The armor that should be set onto the player. Optional
	 *            - can be set later.
	 */
	public TempArmor(LivingEntity entity, long duration, CoreAbility ability, ItemStack[] armorItems) {
		if (duration <= 0)
			duration = defaultDuration;

		this.entity = entity;
		this.startTime = System.currentTimeMillis();
		this.duration = duration;
		this.ability = ability;

		this.oldArmor = new ItemStack[] { new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR) };

		for (int i = 0; i < 4; i++) {
			if (this.entity.getEquipment().getArmorContents()[i] != null) {
				this.oldArmor[i] = this.entity.getEquipment().getArmorContents()[i].clone();
			}
		}

		this.newArmor = armorItems.clone();

		ItemStack[] actualArmor = new ItemStack[4];
		for (int i = 0; i < 4; i++) {
			if (armorItems[i] == null) {
				actualArmor[i] = this.oldArmor[i];
			} else {
				actualArmor[i] = armorItems[i];
			}
		}

		this.entity.getEquipment().setArmorContents(actualArmor);

		//This auto reverts the armor after a certain amount of time. We're doing it
		//this way instead of checking if it should be reverted every tick in a runnable
		this.endTimer = new BukkitRunnable() {

			@Override
			public void run() {
				endTimer = null;
				revert();
			}
		}.runTaskLater(ProjectKorra.plugin, duration / 50);

		INSTANCES.put(entity, this);
	}

	/**
	 * Filters out any TempArmor from the drop list and replaces it with the
	 * original armor. Used when the player/mob dies.
	 * 
	 * @param drops The original item drop list
	 * @return The drop list with the old armor added in place of the temp armor
	 */
	public List<ItemStack> filterArmor(List<ItemStack> drops) {
		List<ItemStack> newDrops = new ArrayList<ItemStack>();

		for (ItemStack drop : drops) {
			boolean match = false;
			for (ItemStack armorPiece : newArmor) {
				if (armorPiece.isSimilar(drop)) {
					match = true;
					break;
				}
			}
			if (!match) {
				newDrops.add(drop);
			}
		}

		for (ItemStack armorPiece : oldArmor) {
			if (armorPiece != null && armorPiece.getType() != Material.AIR) {
				newDrops.add(armorPiece);
			}
		}
		return newDrops;
	}

	public CoreAbility getAbility() {
		return ability;
	}

	public LivingEntity getEntity() {
		return entity;
	}

	public long getDuration() {
		return duration;
	}

	public ItemStack[] getNewArmor() {
		return newArmor;
	}

	public ItemStack[] getOldArmor() {
		return oldArmor;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setArmor(ItemStack[] armor) {
		this.newArmor = armor;

		ItemStack[] actualArmor = new ItemStack[4];
		for (int i = 0; i < 4; i++) {
			if (armor[i] == null) {
				actualArmor[i] = this.oldArmor[i];
			} else {
				actualArmor[i] = armor[i];
			}
		}

		this.entity.getEquipment().setArmorContents(actualArmor);
	}

	/**
	 * Sets whether the ability that created the TempArmor should be forcefully
	 * removed if the armor is forced to be reverted. Such cases are things like
	 * on player death, etc.
	 * 
	 * @param bool
	 */
	public void setRemovesAbilityOnForceRevert(boolean bool) {
		this.removeAbilOnForceRevert = bool;
	}

	/** Destroys the TempArmor instance and restores the player's old armor. */
	public void revert() {
		if (this.endTimer != null) {
			this.endTimer.cancel();
		}

		this.entity.getEquipment().setArmorContents(this.oldArmor);

		if (this.removeAbilOnForceRevert && this.ability != null && !this.ability.isRemoved()) {
			this.ability.remove();
		}

		INSTANCES.remove(this.entity);
	}

	/**
	 * Reverts all TempArmor instances. <b>Should only be used on server
	 * shutdown!</b>
	 */
	public static void revertAll() {
		for (TempArmor armor : INSTANCES.values()) {
			armor.revert();
		}
	}

	/**
	 * Whether the player is currently wearing temporary armor
	 * 
	 * @param player The player
	 * @return If the player has temporary armor on
	 */
	public static boolean hasTempArmor(LivingEntity player) {
		return INSTANCES.containsKey(player);
	}

	/**
	 * Returns the temporary armor the player is currently wearing
	 * 
	 * @param player The player
	 * @return The TempArmor the player is wearing, or <code>null</code> if they
	 *         aren't wearing any.
	 */
	public static TempArmor getTempArmor(LivingEntity player) {
		return INSTANCES.get(player);
	}
}
