package com.projectkorra.projectkorra.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import com.projectkorra.projectkorra.ability.CoreAbility;

public class TempArmor {

	private static Map<LivingEntity, PriorityQueue<TempArmor>> INSTANCES = new ConcurrentHashMap<>();
	private static Map<LivingEntity, ItemStack[]> ORIGINAL = new HashMap<>();
	private static long defaultDuration = 30000L;

	private LivingEntity entity;
	private long startTime;
	private long duration;
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
		
		if (!INSTANCES.containsKey(entity)) {
			ORIGINAL.put(entity, oldArmor);
			PriorityQueue<TempArmor> queue = new PriorityQueue<>(10, new Comparator<TempArmor>() {

				@Override
				public int compare(TempArmor a, TempArmor b) {
					long current = System.currentTimeMillis();
					long remainingA = a.getStartTime() + a.getDuration() - current;
					long remainingB = b.getStartTime() + b.getDuration() - current;
					return (int) (remainingA - remainingB);
				}
				
			});
			
			INSTANCES.put(entity, queue);
		}
		setArmor(armorItems);
		
		INSTANCES.get(entity).add(this);
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
	
	private void updateArmor(TempArmor next) {
		ItemStack[] armor = next.newArmor;
		ItemStack[] actualArmor = new ItemStack[4];
		for (int i = 0; i < 4; i++) {
			if (armor[i] == null) {
				actualArmor[i] = next.oldArmor[i];
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

	/** 
	 * Destroys the TempArmor instance and removes it from the display queue.
	 * <br><br>
	 * Will also restore the player's armor to the state it was before any TempArmor instance was started, if the display queue is empty.
	 */
	public void revert() {
		if (this.removeAbilOnForceRevert && this.ability != null && !this.ability.isRemoved()) {
			this.ability.remove();
		}
		
		PriorityQueue<TempArmor> queue = INSTANCES.get(entity);
		
		if (queue.contains(this)) {
			TempArmor head = queue.peek();
			if (head.equals(this)) {
				queue.poll();
				if (!queue.isEmpty()) {
					updateArmor(queue.peek());
				}
			} else {
				queue.remove(this);
			}
		}
		
		if (queue.isEmpty()) {
			entity.getEquipment().setArmorContents(ORIGINAL.get(entity));
			INSTANCES.remove(entity);
			ORIGINAL.remove(entity);
		}
	}

	public static void cleanup() {
		for (LivingEntity entity : INSTANCES.keySet()) {
			PriorityQueue<TempArmor> queue = INSTANCES.get(entity);
			while (!queue.isEmpty() ) {
				TempArmor tarmor = queue.peek();
				if (System.currentTimeMillis() >= tarmor.getStartTime() + tarmor.getDuration()) {
					tarmor.revert();
				} else {
					break;
				}
			}
		}
	}

	/**
	 * Reverts all TempArmor instances. <b>Should only be used on server
	 * shutdown!</b>
	 */
	public static void revertAll() {
		for (LivingEntity entity : INSTANCES.keySet()) {
			while (!INSTANCES.get(entity).isEmpty()) {
				TempArmor armor = INSTANCES.get(entity).poll();
				armor.revert();
			}
		}
	}

	/**
	 * Whether the player is currently wearing temporary armor
	 * 
	 * @param entity The entity
	 * @return If the entity has temporary armor on
	 */
	public static boolean hasTempArmor(LivingEntity entity) {
		return INSTANCES.containsKey(entity) && !INSTANCES.get(entity).isEmpty();
	}

	/**
	 * Returns the temporary armor the player is currently wearing
	 * 
	 * @param entity The entity
	 * @return The TempArmor the entity is wearing, or <code>null</code> if they
	 *         aren't wearing any.
	 */
	public static TempArmor getVisibleTempArmor(LivingEntity entity) {
		if (!TempArmor.hasTempArmor(entity)) {
			return null;
		}
		return INSTANCES.get(entity).peek();
	}
	
	public static List<TempArmor> getTempArmorList(LivingEntity entity) {
		if (!TempArmor.hasTempArmor(entity)) {
			return Collections.emptyList();
		}
		return new ArrayList<>(INSTANCES.get(entity));
	}
}
