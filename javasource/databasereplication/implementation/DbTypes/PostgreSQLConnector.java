package databasereplication.implementation.DbTypes;

import databasereplication.interfaces.IDatabaseSettings;


public class PostgreSQLConnector extends IDatabaseConnector {

	protected PostgreSQLConnector( IDatabaseSettings settings ) {
		this.connectionString = "jdbc:postgresql://" + settings.getAddress() +
				(settings.getPort() != null && settings.getPort().length() > 0 ? ":" + settings.getPort() : "")
				+ "/" + settings.getDatabaseName();
	}

	@Override
	public String getConnectionString() {
		return this.connectionString;
	}

	@Override
	public String getDriverClass() {
		return "org.postgresql.Driver";
	}

	@Override
	public String getEscapeClose() {
		return "\"";
	}

	@Override
	public String getEscapeOpen() {
		return "\"";
	}

	@Override
	public boolean shouldEscapeColumnNames() {
		return true;
	}

	@Override
	public boolean shouldEscapeTableNames() {
		return true;
	}

	@Override
	public boolean shouldEscapeColumnAlias() {
		return true;
	}

	@Override
	public boolean shouldEscapeTableAlias() {
		return true;
	}

	@Override
	public boolean allowASToken() {
		return true;
	}

	@Override
	public boolean separatEscapeSchemaTable() {
		return false;
	}
	
	@Override
	public boolean closeConnectionAfterQuery() {
		return true;
	}
}
