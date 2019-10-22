package com.projectkorra.projectkorra.database;

import com.projectkorra.projectkorra.module.ModuleManager;

public class DatabaseQuery
{
	private final String _mysql;
	private final String _sqlite;

	private DatabaseQuery(String mysql, String sqlite)
	{
		_mysql = mysql;
		_sqlite = sqlite;
	}

	public String getQuery()
	{
		switch (ModuleManager.getModule(DatabaseManager.class).getConfig().Engine)
		{
		case MYSQL:
			return _mysql;
		case SQLITE:
			return _sqlite;
		}

		return null;
	}

	public static Builder newBuilder()
	{
		return new Builder();
	}

	public static final class Builder
	{
		private String _mysql;
		private String _sqlite;

		public Builder mysql(String mysql)
		{
			_mysql = mysql;
			return this;
		}

		public Builder sqlite(String sqlite)
		{
			_sqlite = sqlite;
			return this;
		}

		public Builder query(String query)
		{
			_mysql = query;
			_sqlite = query;
			return this;
		}

		public DatabaseQuery build()
		{
			return new DatabaseQuery(_mysql, _sqlite);
		}
	}
}
