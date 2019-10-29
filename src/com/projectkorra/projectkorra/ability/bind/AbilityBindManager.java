package com.projectkorra.projectkorra.ability.bind;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.api.PlayerBindChangeEvent;
import com.projectkorra.projectkorra.event.PlayerCooldownChangeEvent;
import com.projectkorra.projectkorra.module.DatabaseModule;
import com.projectkorra.projectkorra.module.ModuleManager;
import com.projectkorra.projectkorra.player.BendingPlayer;
import com.projectkorra.projectkorra.player.BendingPlayerLoadedEvent;
import com.projectkorra.projectkorra.player.BendingPlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.sql.SQLException;

public class AbilityBindManager extends DatabaseModule<AbilityBindRepository> {

	private final BendingPlayerManager bendingPlayerManager;

	private AbilityBindManager() {
		super("Ability Binds", new AbilityBindRepository());

		this.bendingPlayerManager = ModuleManager.getModule(BendingPlayerManager.class);

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
		BendingPlayer bendingPlayer = event.getBendingPlayer();

		runAsync(() -> {
			try {
				String[] abilities = getRepository().selectPlayerAbilities(bendingPlayer.getId());

				bendingPlayer.setAbilities(abilities);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

	public Result bindAbility(Player player, String abilityName, int slot) {
		BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(player);

		PlayerBindChangeEvent playerBindChangeEvent = new PlayerBindChangeEvent(player, abilityName, slot, PlayerBindChangeEvent.Reason.ADD);
		getPlugin().getServer().getPluginManager().callEvent(playerBindChangeEvent);

		if (playerBindChangeEvent.isCancelled()) {
			return Result.CANCELLED;
		}

		bendingPlayer.setAbility(slot, abilityName);

		runAsync(() -> {
			try {
				getRepository().insertPlayerAbility(bendingPlayer.getId(), abilityName, slot);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});

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

		bendingPlayer.setAbility(slot, null);

		runAsync(() -> {
			try {
				getRepository().deletePlayerAbility(bendingPlayer.getId(), abilityName);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});

		return Result.SUCCESS;
	}

	public Result clearBinds(Player player) {
		BendingPlayer bendingPlayer = this.bendingPlayerManager.getBendingPlayer(player);

		PlayerBindChangeEvent playerBindChangeEvent = new PlayerBindChangeEvent(player, PlayerBindChangeEvent.Reason.REMOVE);
		getPlugin().getServer().getPluginManager().callEvent(playerBindChangeEvent);

		if (playerBindChangeEvent.isCancelled()) {
			return Result.CANCELLED;
		}

		bendingPlayer.setAbilities(new String[9]);

		runAsync(() -> {
			try {
				getRepository().deletePlayerAbilities(bendingPlayer.getId());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});

		return Result.SUCCESS;
	}

	public enum Result {
		SUCCESS, CANCELLED, ALREADY_EMPTY
	}
}
