package com.projectkorra.projectkorra.player;

import com.projectkorra.projectkorra.module.DatabaseModule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BendingPlayerManager extends DatabaseModule<BendingPlayerRepository>
{
	private final Map<UUID, BendingPlayer> _players = new HashMap<>();

	private BendingPlayerManager()
	{
		super("Bending Player", new BendingPlayerRepository());

		runAsync(() ->
		{
			getRepository().createTable();

			runSync(() ->
			{
				log("Created database table.");
			});
		});
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onLogin(PlayerLoginEvent event)
	{
		Player player = event.getPlayer();

		runAsync(() ->
		{
			BendingPlayer bendingPlayer = getRepository().selectPlayer(player);

			runSync(() ->
			{
				_players.put(player.getUniqueId(), bendingPlayer);
			});
		});
	}

	public BendingPlayer getBendingPlayer(Player player)
	{
		return _players.get(player.getUniqueId());
	}
}
