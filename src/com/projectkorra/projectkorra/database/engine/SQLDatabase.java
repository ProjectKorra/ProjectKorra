package com.projectkorra.projectkorra.database.engine;

import java.sql.Connection;

public interface SQLDatabase
{
	Connection getConnection();

	void close();
}
