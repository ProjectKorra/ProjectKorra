package com.projectkorra.projectkorra.ability.bind;

import com.projectkorra.projectkorra.ability.AbilityHandler;
import com.projectkorra.projectkorra.ability.AbilityManager;
import com.projectkorra.projectkorra.ability.api.PlayerBindChangeEvent;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.properties.GeneralPropertiesConfig;
import com.projectkorra.projectkorra.event.PlayerCooldownChangeEvent;
import com.projectkorra.projectkorra.module.ModuleManager;
import com.projectkorra.projectkorra.module.PlayerDatabaseModule;
import com.projectkorra.projectkorra.player.BendingPlayer;
import com.projectkorra.projectkorra.player.BendingPlayerLoadedEvent;
import com.projectkorra.projectkorra.player.BendingPlayerManager;
import com.projectkorra.projectkorra.util.ActionBar;
import com.projectkorra.projectkorra.util.TimeUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;

public class AbilityBindManager extends PlayerDatabaseModule<String[], AbilityBindRepository> {

	private final BendingPlayerManager bendingPlayerManager;
	private final AbilityManager abilityManager;

	private AbilityBindManager() {
		super("Ability Binds", new AbilityBindRepository());

		this.bendingPlayerManager = ModuleManager.getModule(BendingPlayerManager.class);
		this.abilityManager = ModuleManager.getModule(AbilityManager.class);

		runAsync(() -> {
			try {
				getRepository().createTables();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			runSync(() -> {
				log("Created database tables.");
			});
		});
	}

	@EventHandler
	public void onBendingPlayerLoaded(BendingPlayerLoadedEvent event) {
		Player player = event.getPlayer();
		BendingPlayer bendingPlayer = event.getBendingPlayer();

		runAsync(() -> {
			try {
				String[] abilities = getRepository().selectPlayerAbilities(bendingPlayer.getId());

				setData(player, abilities);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

	@EventHandler
	public void onCooldownChange(PlayerCooldownChangeEvent event) {
		Player player = event.getPlayer();

		int slot = player.getInventory().getHeldItemSlot();
		String abilityName = getData(player)[slot];

		if (abilityName != null && abilityName.equals(event.getAbility())) {
			displayMovePreview(player, slot);
		}
	}

	public Result bindAbility(Player player, String abilityName, int slot) {
		return bindAbility(player, abilityName, slot, true);
	}

	public Result bindAbility(Player player, String abilityName, int slot, boolean save) {
		BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(player);

		PlayerBindChangeEvent playerBindChangeEvent = new PlayerBindChangeEvent(player, abilityName, slot, PlayerBindChangeEvent.Reason.ADD);
		getPlugin().getServer().getPluginManager().callEvent(playerBindChangeEvent);

		if (playerBindChangeEvent.isCancelled()) {
			return Result.CANCELLED;
		}

		getData(player)[slot] = abilityName;

		if (save) {
			runAsync(() -> {
				try {
					getRepository().insertPlayerAbility(bendingPlayer.getId(), abilityName, slot);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			});
		}

		return Result.SUCCESS;
	}

	public Result unbindAbility(Player player, int slot) {
		BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(player);
		String abilityName = bendingPlayer.getAbility(slot);

		if (abilityName == null) {
			return Result.ALREADY_EMPTY;
		}

		PlayerBindChangeEvent playerBindChangeEvent = new PlayerBindChangeEvent(player, abilityName, slot, PlayerBindChangeEvent.Reason.REMOVE);
		getPlugin().getServer().getPluginManager().callEvent(playerBindChangeEvent);

		if (playerBindChangeEvent.isCancelled()) {
			return Result.CANCELLED;
		}

		getData(player)[slot] = null;

		runAsync(() -> {
			try {
				getRepository().deletePlayerAbility(bendingPlayer.getId(), abilityName);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});

		return Result.SUCCESS;
	}

	public Result clearAbilities(Player player) {
		BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(player);

		PlayerBindChangeEvent playerBindChangeEvent = new PlayerBindChangeEvent(player, PlayerBindChangeEvent.Reason.REMOVE);
		getPlugin().getServer().getPluginManager().callEvent(playerBindChangeEvent);

		if (playerBindChangeEvent.isCancelled()) {
			return Result.CANCELLED;
		}

		Arrays.fill(getData(player), null);

		runAsync(() -> {
			try {
				getRepository().deletePlayerAbilities(bendingPlayer.getId());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});

		return Result.SUCCESS;
	}

	public void setAbilities(Player player, String[] abilities) {
		setData(player, abilities);
	}

	public String getBoundAbility(Player player) {
		return getAbility(player, player.getInventory().getHeldItemSlot());
	}

	public String getAbility(Player player, int slot) {
		return getData(player)[slot];
	}

	public String[] getAbilities(Player player) {
		String[] abilities = getData(player);

		return Arrays.copyOf(abilities, abilities.length);
	}

	public void displayMovePreview(Player player, int slot) {
		if (!ConfigManager.getConfig(GeneralPropertiesConfig.class).BendingPreview) {
			return;
		}

		String abilityName = getData(player)[slot];

		if (abilityName == null) {
			ActionBar.sendActionBar("", player);
			return;
		}

		AbilityHandler abilityHandler = this.abilityManager.getHandler(abilityName);

		if (abilityHandler == null) {
			ActionBar.sendActionBar(abilityName, player);
			return;
		}

		BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(player);

		if (bendingPlayer.isOnCooldown(abilityName)) {
			long cooldown = bendingPlayer.getCooldown(abilityName) - System.currentTimeMillis();
			String display = abilityHandler.getElement().getColor() + (ChatColor.STRIKETHROUGH + abilityHandler.getName() + abilityHandler.getElement().getColor() + " - " + TimeUtil.formatTime(cooldown));

			ActionBar.sendActionBar(display, player);
			return;
		}

		String display = abilityHandler.getElement().getColor().toString();

		if (bendingPlayer.getStance() != null && bendingPlayer.getStance().getName().equals(abilityHandler.getName())) {
			display +=  ChatColor.UNDERLINE.toString();
		}

		display += abilityHandler.getName();

		ActionBar.sendActionBar(display, player);
	}

	@Override
	protected String[] addData(UUID uuid) {
		return new String[9];
	}

	public enum Result {
		SUCCESS, CANCELLED, ALREADY_EMPTY
	}
}
