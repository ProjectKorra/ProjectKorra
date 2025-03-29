package com.projectkorra.projectkorra.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import com.projectkorra.projectkorra.ability.CoreAbility;

public class TempArmor {

	private static final Map<LivingEntity, PriorityQueue<TempArmor>> INSTANCES = new ConcurrentHashMap<>();
	private static final Map<LivingEntity, ItemStack[]> ORIGINAL = new HashMap<>();
	private static final long DEFAULT_DURATION = 30000L;

	private final CoreAbility ability;
	private final LivingEntity entity;
	private final long startTime;
	private final long duration;
	private final ItemStack[] oldArmor;
	private ItemStack[] newArmor;
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
	public TempArmor(final LivingEntity entity, final ItemStack[] armorItems) {
		this(entity, DEFAULT_DURATION, null, armorItems);
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
	public TempArmor(final LivingEntity entity, final CoreAbility ability, final ItemStack[] armorItems) {
		this(entity, DEFAULT_DURATION, ability, armorItems);
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
	public TempArmor(final LivingEntity entity, long duration, final CoreAbility ability, final ItemStack[] armorItems) {
		if (duration <= 0) {
			duration = DEFAULT_DURATION;
		}

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
			ORIGINAL.put(entity, this.oldArmor);
			final PriorityQueue<TempArmor> queue = new PriorityQueue<>(10, (a, b) -> {
				final long current = System.currentTimeMillis();
				final long remainingA = a.getStartTime() + a.getDuration() - current;
				final long remainingB = b.getStartTime() + b.getDuration() - current;
				return (int) (remainingA - remainingB);
			});

			INSTANCES.put(entity, queue);
		}
		this.setArmor(armorItems);

		INSTANCES.get(entity).add(this);
	}

	/**
	 * Filters out any TempArmor from the drop list and replaces it with the
	 * original armor. Used when the player/mob dies.
	 *
	 * @param drops The original item drop list
	 * @return The drop list with the old armor added in place of the temp armor
	 */
	public List<ItemStack> filterArmor(final List<ItemStack> drops) {
		final List<ItemStack> newDrops = new ArrayList<ItemStack>();

		for (final ItemStack drop : drops) {
			boolean match = false;
			for (final ItemStack armorPiece : this.newArmor) {
				if (armorPiece != null && armorPiece.isSimilar(drop)) {
					match = true;
					break;
				}
			}
			if (!match) {
				newDrops.add(drop);
			}
		}

		for (final ItemStack armorPiece : this.oldArmor) {
			if (armorPiece != null && armorPiece.getType() != Material.AIR) {
				newDrops.add(armorPiece);
			}
		}
		return newDrops;
	}

	public CoreAbility getAbility() {
		return this.ability;
	}

	public LivingEntity getEntity() {
		return this.entity;
	}

	public long getDuration() {
		return this.duration;
	}

	public ItemStack[] getNewArmor() {
		return this.newArmor;
	}

	public ItemStack[] getOldArmor() {
		return this.oldArmor;
	}

	public long getStartTime() {
		return this.startTime;
	}

	public void setArmor(final ItemStack[] armor) {
		final ItemStack[] actualArmor = new ItemStack[4];
		for (int i = 0; i < 4; i++) {
			ItemStack piece = armor[i];
			actualArmor[i] = piece != null ? piece : this.oldArmor[i];
		}
		this.newArmor = armor;
		this.entity.getEquipment().setArmorContents(actualArmor);
	}

	private void updateArmor(final TempArmor next) {
		final ItemStack[] armor = next.newArmor;
		final ItemStack[] actualArmor = new ItemStack[4];
		for (int i = 0; i < 4; i++) {
			ItemStack piece = armor[i];
			actualArmor[i] = piece != null ? piece : next.oldArmor[i];
		}
		this.entity.getEquipment().setArmorContents(actualArmor);
	}

	/**
	 * Sets whether the ability that created the TempArmor should be forcefully
	 * removed if the armor is forced to be reverted. Such cases are things like
	 * on player death, etc.
	 */
	public void setRemovesAbilityOnForceRevert(final boolean removeAbilOnForceRevert) {
		this.removeAbilOnForceRevert = removeAbilOnForceRevert;
	}

	public void revert() {
		revert(null, true);
	}
	
	/**
	 * Destroys the TempArmor instance and removes it from the display queue.
	 * <br>
	 * <br>
	 * Will also restore the player's armor to the state it was before any
	 * TempArmor instance was started, if the display queue is empty.
	 * 
	 * @param drops A list of drops to filter temporary armor from when reverting, null if n/a
	 * @param keepInv Whether the keepInventory gamerule is on
	 */
	public void revert(List<ItemStack> drops, boolean keepInv) {
		final PriorityQueue<TempArmor> queue = INSTANCES.get(this.entity);

		if (queue.contains(this)) {
			final TempArmor head = queue.peek();
			if (head.equals(this)) {
				queue.poll();
				if (!queue.isEmpty()) {
					this.updateArmor(queue.peek());
				}
			} else {
				queue.remove(this);
			}
		}
		
		if (drops != null) {
			for (ItemStack is : newArmor) {
				if (is != null) {
					drops.remove(is);
				}
			}
		}

		if (queue.isEmpty()) {
			this.entity.getEquipment().setArmorContents(ORIGINAL.get(this.entity));

			if (drops != null && !keepInv) {
				for (ItemStack is : ORIGINAL.get(this.entity)) {
					if (is != null) {
						drops.add(is);
					}
				}
			}

			INSTANCES.remove(this.entity);
			ORIGINAL.remove(this.entity);
		}

		if (this.removeAbilOnForceRevert && this.ability != null && !this.ability.isRemoved()) {
			this.ability.remove();
		}
	}

	public static void cleanup() {
		for (final LivingEntity entity : INSTANCES.keySet()) {
			final PriorityQueue<TempArmor> queue = INSTANCES.get(entity);
			while (!queue.isEmpty()) {
				final TempArmor tarmor = queue.peek();
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
		for (final LivingEntity entity : INSTANCES.keySet()) {
			while (!INSTANCES.get(entity).isEmpty()) {
				final TempArmor armor = INSTANCES.get(entity).poll();
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
	public static boolean hasTempArmor(final LivingEntity entity) {
		return INSTANCES.containsKey(entity) && !INSTANCES.get(entity).isEmpty();
	}

	/**
	 * Returns the temporary armor the player is currently wearing
	 *
	 * @param entity The entity
	 * @return The TempArmor the entity is wearing, or <code>null</code> if they
	 *         aren't wearing any.
	 */
	public static TempArmor getVisibleTempArmor(final LivingEntity entity) {
		if (!TempArmor.hasTempArmor(entity)) {
			return null;
		}
		return INSTANCES.get(entity).peek();
	}

	public static List<TempArmor> getTempArmorList(final LivingEntity entity) {
		if (!TempArmor.hasTempArmor(entity)) {
			return Collections.emptyList();
		}
		return new ArrayList<>(INSTANCES.get(entity));
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
}
