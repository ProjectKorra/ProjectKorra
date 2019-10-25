package com.projectkorra.projectkorra.database;

import com.projectkorra.projectkorra.module.ModuleManager;

public class DatabaseQuery {

	private final String mysql;
	private final String sqlite;

	private DatabaseQuery(String mysql, String sqlite) {
		this.mysql = mysql;
		this.sqlite = sqlite;
	}

	public String getQuery() {
		switch (ModuleManager.getModule(DatabaseManager.class).getConfig().Engine) {
			case MYSQL:
				return this.mysql;
			case SQLITE:
				return this.sqlite;
		}

		return null;
	}

	public static Builder newBuilder() {
		return new Builder();
	}

	public static final class Builder {

		private String mysql;
		private String sqlite;

		public Builder mysql(String mysql) {
			this.mysql = mysql;
			return this;
		}

		public Builder sqlite(String sqlite) {
			this.sqlite = sqlite;
			return this;
		}

		public Builder query(String query) {
			this.mysql = query;
			this.sqlite = query;
			return this;
		}

		public DatabaseQuery build() {
			return new DatabaseQuery(this.mysql, this.sqlite);
		}
	}
}
