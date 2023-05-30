package databasereplication.implementation.DbTypes;

import databasereplication.interfaces.IDatabaseSettings;


public class InformixConnector extends IDatabaseConnector {

	protected InformixConnector( IDatabaseSettings settings ) {

		this.connectionString = "jdbc:informix-sqli://" + settings.getAddress() + ":" +
				(settings.getPort() != null && settings.getPort().length() > 0 ? settings.getPort() : "") +
				(settings.getDatabaseName() != null && settings.getDatabaseName().length() > 0 ? "/" + settings.getDatabaseName() + ":" : "") +
				"informixserver=" + settings.getServiceName();
		// + ";" + "user=" + settings.getUserName() + ";password=" + settings.getPassword()
	}

	@Override
	public String getConnectionString() {
		return this.connectionString;
	}

	@Override
	public String getDriverClass() {
		return "com.informix.jdbc.IfxDriver";
	}

	@Override
	public String getEscapeClose() {
		return "";
	}

	@Override
	public String getEscapeOpen() {
		return "";
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
		return false;
	}
}
