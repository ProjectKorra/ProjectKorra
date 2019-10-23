package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.module.DatabaseModule;
import com.projectkorra.projectkorra.module.ModuleManager;
import com.projectkorra.projectkorra.player.BendingPlayer;
import com.projectkorra.projectkorra.player.BendingPlayerLoadedEvent;
import com.projectkorra.projectkorra.player.BendingPlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.sql.SQLException;

public class AbilityManager extends DatabaseModule<AbilityRepository>
{
	private final BendingPlayerManager _bendingPlayerManager;

	private AbilityManager()
	{
		super("Ability", new AbilityRepository());

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

			runSync(() ->
			{
				log("Created database tables.");
			});
		});
	}

	@EventHandler
	public void onBendingPlayerLoaded(BendingPlayerLoadedEvent event)
	{
		BendingPlayer bendingPlayer = event.getBendingPlayer();

		runAsync(() ->
		{
			try
			{
				String[] abilities = getRepository().selectPlayerAbilities(bendingPlayer.getId());

				bendingPlayer.setAbilities(abilities);
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		});
	}

	public boolean bindAbility(Player player, String abilityName, int slot)
	{
		PlayerBindAbilityEvent playerBindAbilityEvent = new PlayerBindAbilityEvent(player, abilityName);
		getPlugin().getServer().getPluginManager().callEvent(playerBindAbilityEvent);

		if (playerBindAbilityEvent.isCancelled())
		{
			String cancelMessage = playerBindAbilityEvent.getCancelMessage();

			if (cancelMessage != null)
			{
				GeneralMethods.sendBrandingMessage(player, cancelMessage);
			}

			return false;
		}

		BendingPlayer bendingPlayer = _bendingPlayerManager.getBendingPlayer(player);

		bendingPlayer.setAbility(slot, abilityName);

		runAsync(() ->
		{
			try
			{
				getRepository().insertPlayerAbility(bendingPlayer.getId(), abilityName, slot);
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		});

		return true;
	}

	public boolean unbindAbility(Player player, int slot)
	{
		BendingPlayer bendingPlayer = _bendingPlayerManager.getBendingPlayer(player);

		String abilityName = bendingPlayer.getAbility(slot);

		if (abilityName == null)
		{
			player.sendMessage("No ability bound");
			return false;
		}

		bendingPlayer.setAbility(slot, null);

		runAsync(() ->
		{
			try
			{
				getRepository().deletePlayerAbility(bendingPlayer.getId(), abilityName);
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		});

		return true;
	}

	public void clearBinds(Player player)
	{
		BendingPlayer bendingPlayer = _bendingPlayerManager.getBendingPlayer(player);

		bendingPlayer.setAbilities(new String[9]);

		runAsync(() ->
		{
			try
			{
				getRepository().deletePlayerAbilities(bendingPlayer.getId());
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		});
	}
}
