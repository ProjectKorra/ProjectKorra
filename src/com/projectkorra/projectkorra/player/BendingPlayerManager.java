package com.projectkorra.projectkorra.player;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.event.PlayerChangeElementEvent;
import com.projectkorra.projectkorra.event.PlayerCooldownChangeEvent;
import com.projectkorra.projectkorra.module.DatabaseModule;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;

public class BendingPlayerManager extends DatabaseModule<BendingPlayerRepository> {

	private final Map<UUID, BendingPlayer> players = new HashMap<>();

	private final Set<UUID> disconnected = new HashSet<>();
	private final long databaseSyncInterval = 20 * 30;

	private BendingPlayerManager() {
		super("Bending Player", new BendingPlayerRepository());

		runAsync(() -> {
			try {
				getRepository().createTables();

				for (Player player : getPlugin().getServer().getOnlinePlayers()) {
					loadBendingPlayer(player);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});

		runTimer(() -> {
			this.disconnected.forEach(this.players::remove);
			this.disconnected.clear();
		}, this.databaseSyncInterval, this.databaseSyncInterval);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onLogin(PlayerLoginEvent event) {
		if (this.disconnected.remove(event.getPlayer().getUniqueId())) {
			return;
		}

		runAsync(() -> {
			loadBendingPlayer(event.getPlayer());
		});
	}

	@EventHandler
	public void onBendingPlayerLoaded(BendingPlayerLoadedEvent event) {
		Player player = event.getPlayer();
		BendingPlayer bendingPlayer = event.getBendingPlayer();

		if (bendingPlayer.isToggled()) {

		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		this.disconnected.add(event.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onChangeElement(PlayerChangeElementEvent event) {
		Player player = event.getPlayer();
		BendingPlayer bendingPlayer = this.players.get(player.getUniqueId());

		switch (event.getReason()) {
			case ADD:
				bendingPlayer.addElement(event.getElement());
				break;
			case SET:
				bendingPlayer.clearElements();
				bendingPlayer.addElement(event.getElement());
				break;
			case REMOVE:
				bendingPlayer.removeElement(event.getElement());
				break;
			case CLEAR:
				bendingPlayer.clearElements();
				break;
		}
	}

	public void setBendingPermanentlyRemoved(Player player, boolean removed) {
		BendingPlayer bendingPlayer = this.players.get(player.getUniqueId());

		bendingPlayer.setBendingPermanentlyRemoved(removed);

		updateBendingRemoved(bendingPlayer);
	}

	private void updateBendingRemoved(BendingPlayer bendingPlayer) {
		runAsync(() -> {
			try {
				getRepository().updateBendingRemoved(bendingPlayer);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

	private void loadBendingPlayer(Player player) {
		try {
			BendingPlayer bendingPlayer = getRepository().selectPlayer(player.getUniqueId(), player.getName());

			runSync(() -> {
				this.players.put(player.getUniqueId(), bendingPlayer);

				BendingPlayerLoadedEvent bendingPlayerLoadedEvent = new BendingPlayerLoadedEvent(player, bendingPlayer);
				getPlugin().getServer().getPluginManager().callEvent(bendingPlayerLoadedEvent);
			});
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void loadBendingPlayer(UUID uuid, Consumer<BendingPlayer> consumer) {
		runAsync(() -> {
			try {
				BendingPlayer bendingPlayer = getRepository().selectPlayer(uuid, null);

				runSync(() -> {
					this.players.put(uuid, bendingPlayer);
					this.disconnected.add(uuid);

					consumer.accept(bendingPlayer);
				});
			} catch (SQLException e) {
				consumer.accept(null);
				e.printStackTrace();
			}
		});
	}

	public BendingPlayer getBendingPlayer(Player player) {
		return getBendingPlayer(player.getUniqueId());
	}

	public BendingPlayer getBendingPlayer(UUID uuid) {
		return this.players.get(uuid);
	}
}
