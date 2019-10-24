package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.module.Module;
import com.projectkorra.projectkorra.util.ClickType;

import java.util.*;

public class ComboManager extends Module {

	private final Map<UUID, List<AbilityInformation>> recentlyUsed = new HashMap<>();

	private ComboManager() {
		super("Combo Ability");
	}

	/**
	 * Contains information on an ability used in a combo.
	 *
	 * @author kingbirdy
	 *
	 */
	public static class AbilityInformation {
		private String abilityName;
		private ClickType clickType;
		private long time;

		public AbilityInformation(final String name, final ClickType type) {
			this(name, type, 0);
		}

		public AbilityInformation(final String name, final ClickType type, final long time) {
			this.abilityName = name;
			this.clickType = type;
			this.time = time;
		}

		/**
		 * Compares if two {@link com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation}'s are equal without
		 * respect to {@link com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation#time time}.
		 *
		 * @param info The AbilityInformation to compare against
		 * @return True if they are equal without respect to time
		 */
		public boolean equalsWithoutTime(final com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation info) {
			return this.getAbilityName().equals(info.getAbilityName()) && this.getClickType().equals(info.getClickType());
		}

		/**
		 * Gets the name of the ability.
		 *
		 * @return The name of the ability.
		 */
		public String getAbilityName() {
			return this.abilityName;
		}

		/**
		 * Gets the {@link ClickType} of the {@link com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation}.
		 *
		 * @return The ClickType
		 */
		public ClickType getClickType() {
			return this.clickType;
		}

		public long getTime() {
			return this.time;
		}

		public void setAbilityName(final String abilityName) {
			this.abilityName = abilityName;
		}

		public void setClickType(final ClickType clickType) {
			this.clickType = clickType;
		}

		public void setTime(final long time) {
			this.time = time;
		}

		@Override
		public String toString() {
			return this.abilityName + " " + this.clickType + " " + this.time;
		}
	}

	public static class ComboAbilityInfo {
		private String name;
		private ArrayList<com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation> abilities;
		private Object comboType;

		public ComboAbilityInfo(final String name, final ArrayList<com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation> abilities, final Object comboType) {
			this.name = name;
			this.abilities = abilities;
			this.comboType = comboType;
		}

		public ArrayList<com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation> getAbilities() {
			return this.abilities;
		}

		public Object getComboType() {
			return this.comboType;
		}

		public String getName() {
			return this.name;
		}

		public void setAbilities(final ArrayList<com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation> abilities) {
			this.abilities = abilities;
		}

		public void setComboType(final Object comboType) {
			this.comboType = comboType;
		}

		public void setName(final String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return this.name;
		}
	}
}
