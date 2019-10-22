package com.projectkorra.projectkorra.module;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.projectkorra.projectkorra.ProjectKorra;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public abstract class Module implements Listener
{
	private static final String LOG_FORMAT = "(%s) %s";

	private final String name;
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	protected Module(String name)
	{
		this.name = name;
	}

	protected final void enable()
	{
		long startTime = System.currentTimeMillis();
		log("Enabling...");

		getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
		onEnable();

		long finishTime = System.currentTimeMillis();
		log(String.format("Enabled! [%sms]", finishTime - startTime));
	}

	public void onEnable()
	{

	}

	protected final void disable()
	{
		long startTime = System.currentTimeMillis();
		log("Disabling...");

		HandlerList.unregisterAll(this);
		onDisable();

		long finishTime = System.currentTimeMillis();
		log(String.format("Disabled! [%sms]", finishTime - startTime));
	}

	public void onDisable()
	{

	}

	protected final void runSync(Runnable runnable)
	{
		getPlugin().getServer().getScheduler().runTask(getPlugin(), runnable);
	}

	protected final void runAsync(Runnable runnable)
	{
		getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), runnable);
	}

	protected final void runTimer(Runnable runnable, long delay, long period)
	{
		getPlugin().getServer().getScheduler().runTaskTimer(getPlugin(), runnable, delay, period);
	}

	protected final void runAsyncTimer(Runnable runnable, long delay, long period)
	{
		getPlugin().getServer().getScheduler().runTaskTimerAsynchronously(getPlugin(), runnable, delay, period);
	}

	public String getName()
	{
		return this.name;
	}

	protected Gson getGson()
	{
		return this.gson;
	}

	public final void log(String message)
	{
		log(Level.INFO, message);
	}

	public final void log(Level level, String message)
	{
		getPlugin().getLogger().log(level, String.format(LOG_FORMAT, getName(), message));
	}

	public ProjectKorra getPlugin()
	{
		return JavaPlugin.getPlugin(ProjectKorra.class);
	}
}
