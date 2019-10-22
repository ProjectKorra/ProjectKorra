package com.projectkorra.projectkorra.player;

import com.projectkorra.projectkorra.element.Element;
import com.projectkorra.projectkorra.element.SubElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class BendingPlayer
{
	private final int _playerId;
	private final UUID _uuid;
	private final String _playerName;
	private final long _firstLogin;

	private final List<Element> _elements;
	private final List<SubElement> _subElements;

	public BendingPlayer(int playerId, UUID uuid, String playerName, long firstLogin)
	{
		_playerId = playerId;
		_uuid = uuid;
		_playerName = playerName;
		_firstLogin = firstLogin;

		_elements = new ArrayList<>();
		_subElements = new ArrayList<>();
	}

	public int getId()
	{
		return _playerId;
	}

	public long getFirstLogin()
	{
		return _firstLogin;
	}

	public void addElements(Collection<Element> elements)
	{
		for (Element element : elements)
		{
			if (element instanceof SubElement)
			{
				_subElements.add((SubElement) element);
			}
			else
			{
				_elements.add(element);
			}
		}
	}
}
