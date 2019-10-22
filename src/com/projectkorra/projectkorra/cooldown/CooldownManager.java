package com.projectkorra.projectkorra.cooldown;

import com.projectkorra.projectkorra.event.PlayerCooldownChangeEvent;
import com.projectkorra.projectkorra.module.DatabaseModule;
import com.projectkorra.projectkorra.module.ModuleManager;
import com.projectkorra.projectkorra.player.BendingPlayer;
import com.projectkorra.projectkorra.player.BendingPlayerLoadedEvent;
import com.projectkorra.projectkorra.player.BendingPlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.function.Function;

public class CooldownManager extends DatabaseModule<CooldownRepository>
{
	private final BendingPlayerManager _bendingPlayerManager;

	private final Map<UUID, Map<String, Cooldown>> _cooldownMap = new HashMap<>();
	private final Map<UUID, PriorityQueue<Cooldown>> _cooldownQueue = new HashMap<>();

	private final Function<UUID, PriorityQueue<Cooldown>> _queueFunction = uuid -> new PriorityQueue<>(Comparator.comparing(cooldown -> cooldown.ExpireTime));

	private CooldownManager()
	{
		super("Cooldown", new CooldownRepository());

		_bendingPlayerManager = ModuleManager.getModule(BendingPlayerManager.class);

		runAsync(() ->
		{
			try
			{
				getRepository().createTables();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		});

		runTimer(() ->
		{
			_cooldownQueue.forEach((uuid, cooldowns) ->
			{
				long currentTime = System.currentTimeMillis();

				while (!cooldowns.isEmpty())
				{
					Cooldown cooldown = cooldowns.peek();

					if (currentTime < cooldown.ExpireTime)
					{
						break;
					}

					_cooldownMap.get(uuid).remove(cooldown.AbilityName);
					cooldowns.poll();

					if (cooldown.Permanent)
					{
						int playerId = _bendingPlayerManager.getBendingPlayer(uuid).getId();

						runAsync(() ->
						{
							try
							{
								getRepository().deleteCooldown(playerId, cooldown.AbilityName);
							}
							catch (SQLException e)
							{
								e.printStackTrace();
							}
						});
					}
				}
			});

			_cooldownMap.values().removeIf(Map::isEmpty);
			_cooldownQueue.values().removeIf(PriorityQueue::isEmpty);
		}, 1, 1);
	}

	@EventHandler
	public void onBendingPlayerLoaded(BendingPlayerLoadedEvent event)
	{
		Player player = event.getPlayer();
		BendingPlayer bendingPlayer = event.getBendingPlayer();

		runAsync(() ->
		{
			try
			{
				Map<String, Cooldown> cooldowns = getRepository().selectCooldowns(bendingPlayer.getId());

				_cooldownMap.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).putAll(cooldowns);
				_cooldownQueue.computeIfAbsent(player.getUniqueId(), _queueFunction).addAll(cooldowns.values());
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		});
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		_cooldownMap.remove(event.getPlayer().getUniqueId());
		_cooldownQueue.remove(event.getPlayer().getUniqueId());
	}

	public void addCooldown(Player player, String abilityName, long duration, boolean permanent)
	{
		if (duration <= 0)
		{
			return;
		}

		PlayerCooldownChangeEvent event = new PlayerCooldownChangeEvent(player, abilityName, duration, PlayerCooldownChangeEvent.Result.ADDED);
		getPlugin().getServer().getPluginManager().callEvent(event);

		if (event.isCancelled())
		{
			return;
		}

		long expireTime = System.currentTimeMillis() + duration;
		Cooldown cooldown = new Cooldown(abilityName, expireTime, permanent);

		_cooldownMap.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(abilityName, cooldown);
		_cooldownQueue.computeIfAbsent(player.getUniqueId(), _queueFunction).add(cooldown);

		if (permanent)
		{
			int playerId = _bendingPlayerManager.getBendingPlayer(player).getId();

			runAsync(() ->
			{
				try
				{
					getRepository().insertCooldown(playerId, abilityName, expireTime);
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			});
		}
	}

	public long getCooldown(Player player, String abilityName)
	{
		Map<String, Cooldown> cooldowns = _cooldownMap.get(player.getUniqueId());

		if (cooldowns != null && cooldowns.containsKey(abilityName))
		{
			return cooldowns.get(abilityName).ExpireTime;
		}

		return -1L;
	}

	public boolean isOnCooldown(Player player, String abilityName)
	{
		Map<String, Cooldown> cooldowns = _cooldownMap.get(player.getUniqueId());

		return cooldowns != null && cooldowns.containsKey(abilityName);
	}

	public void removeCooldown(Player player, String abilityName)
	{
		UUID uuid = player.getUniqueId();
		Map<String, Cooldown> cooldowns = _cooldownMap.get(player.getUniqueId());

		if (cooldowns == null)
		{
			return;
		}

		Cooldown cooldown = cooldowns.remove(abilityName);

		if (cooldown == null)
		{
			return;
		}

		if (_cooldownQueue.containsKey(uuid))
		{
			_cooldownQueue.get(uuid).remove(cooldown);
		}

		if (cooldown.Permanent)
		{
			int playerId = _bendingPlayerManager.getBendingPlayer(player).getId();

			runAsync(() ->
			{
				try
				{
					getRepository().deleteCooldown(playerId, cooldown.AbilityName);
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			});
		}
	}

	public static class Cooldown
	{
		final String AbilityName;
		final long ExpireTime;
		final boolean Permanent;

		public Cooldown(String abilityName, long expireTime)
		{
			this(abilityName, expireTime, false);
		}

		public Cooldown(String abilityName, long expireTime, boolean permanent)
		{
			AbilityName = abilityName;
			ExpireTime = expireTime;
			Permanent = permanent;
		}


	}
}
