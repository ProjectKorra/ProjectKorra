package com.projectkorra.projectkorra.player;

import com.projectkorra.projectkorra.element.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BendingPlayer
{
	private final int _playerId;
	private final UUID _uuid;
	private final String _playerName;
	private final long _firstLogin;

	private final List<Element> _elements;

	public BendingPlayer(int playerId, UUID uuid, String playerName, long firstLogin)
	{
		_playerId = playerId;
		_uuid = uuid;
		_playerName = playerName;
		_firstLogin = firstLogin;

		_elements = new ArrayList<>();
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
