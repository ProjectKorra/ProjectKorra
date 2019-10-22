package com.projectkorra.projectkorra.module;

import com.projectkorra.projectkorra.database.DatabaseRepository;

public abstract class DatabaseModule<T extends DatabaseRepository> extends Module
{
	private final T repository;

	protected DatabaseModule(String name, T repository)
	{
		super(name);

		this.repository = repository;
	}

	protected T getRepository()
	{
		return this.repository;
	}
}
