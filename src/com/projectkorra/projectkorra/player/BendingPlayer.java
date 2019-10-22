package com.projectkorra.projectkorra.player;

import java.util.UUID;

public class BendingPlayer
{
	private final int _playerId;
	private final UUID _uuid;
	private final String _playerName;
	private long _firstLogin;

	public BendingPlayer(int playerId, UUID uuid, String playerName, long firstLogin)
	{
		_playerId = playerId;
		_uuid = uuid;
		_playerName = playerName;
		_firstLogin = firstLogin;
	}

	public int getId()
	{
		return _playerId;
	}

	public long getFirstLogin()
	{
		return _firstLogin;
	}
}
