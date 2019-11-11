package com.projectkorra.projectkorra.module;

import com.projectkorra.projectkorra.database.DatabaseRepository;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class PlayerDatabaseModule<Data, T extends DatabaseRepository> extends DatabaseModule<T> {

	private final Map<UUID, Data> playerData = new HashMap<>();

	protected PlayerDatabaseModule(String name, T repository) {
		super(name, repository);
	}

	protected Data getData(Player player) {
		return getData(player.getUniqueId());
	}

	protected Data getData(UUID uuid) {
		return this.playerData.computeIfAbsent(uuid, this::addData);
	}

	protected void setData(Player player, Data data) {
		this.playerData.put(player.getUniqueId(), data);
	}

	protected abstract Data addData(UUID uuid);
}
