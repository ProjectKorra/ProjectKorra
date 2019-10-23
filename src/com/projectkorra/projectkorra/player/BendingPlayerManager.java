package com.projectkorra.projectkorra.player;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.event.PlayerCooldownChangeEvent;
import com.projectkorra.projectkorra.module.DatabaseModule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BendingPlayerManager extends DatabaseModule<BendingPlayerRepository>
{
	private final Map<UUID, BendingPlayer> _players = new HashMap<>();

	private final Set<UUID> _disconnected = new HashSet<>();
	private final long _databaseSyncInterval = 20 * 30;

	private BendingPlayerManager()
	{
		super("Bending Player", new BendingPlayerRepository());

		runAsync(() ->
		{
			try
			{
				getRepository().createTables();

				for (Player player : getPlugin().getServer().getOnlinePlayers())
				{
					loadBendingPlayer(player);
				}
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		});

		runTimer(() ->
		{
			_disconnected.forEach(_players::remove);
			_disconnected.clear();
		}, _databaseSyncInterval, _databaseSyncInterval);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onLogin(PlayerLoginEvent event)
	{
		if (_disconnected.remove(event.getPlayer().getUniqueId()))
		{
			return;
		}

		runAsync(() ->
		{
			loadBendingPlayer(event.getPlayer());
		});
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		_disconnected.add(event.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onCooldownChange(PlayerCooldownChangeEvent event)
	{
		Player player = event.getPlayer();
		BendingPlayer bendingPlayer = _players.get(player.getUniqueId());

		String ability = bendingPlayer.getBoundAbilityName();

		if (ability != null && ability.equals(event.getAbility()))
		{
			GeneralMethods.displayMovePreview(player);
		}
	}

	public void removeBending(Player player)
	{
		BendingPlayer bendingPlayer = _players.get(player.getUniqueId());

		bendingPlayer.setBendingRemoved(true);

		updateBendingRemoved(bendingPlayer);
	}

	public void returnBending(Player player)
	{
		BendingPlayer bendingPlayer = _players.get(player.getUniqueId());

		bendingPlayer.setBendingRemoved(false);

		updateBendingRemoved(bendingPlayer);
	}

	private void updateBendingRemoved(BendingPlayer bendingPlayer)
	{
		runAsync(() ->
		{
			try
			{
				getRepository().updateBendingRemoved(bendingPlayer);
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		});
	}

	private void loadBendingPlayer(Player player)
	{
		try
		{
			BendingPlayer bendingPlayer = getRepository().selectPlayer(player);

			runSync(() ->
			{
				_players.put(player.getUniqueId(), bendingPlayer);

				BendingPlayerLoadedEvent bendingPlayerLoadedEvent = new BendingPlayerLoadedEvent(player, bendingPlayer);
				getPlugin().getServer().getPluginManager().callEvent(bendingPlayerLoadedEvent);
			});
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public BendingPlayer getBendingPlayer(Player player)
	{
		return getBendingPlayer(player.getUniqueId());
	}

	public BendingPlayer getBendingPlayer(UUID uuid)
	{
		return _players.get(uuid);
	}
}
