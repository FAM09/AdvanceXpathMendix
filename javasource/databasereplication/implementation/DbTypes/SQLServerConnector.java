package databasereplication.implementation.DbTypes;

import databasereplication.interfaces.IDatabaseSettings;


public class SQLServerConnector extends IDatabaseConnector {

	protected SQLServerConnector( IDatabaseSettings settings ) {
		this.connectionString = "jdbc:sqlserver://" + settings.getAddress() +
				(settings.getPort() != null && settings.getPort().length() > 0 ? ":" + settings.getPort() : "") +
				(settings.getServiceName() != null && settings.getServiceName().length() > 0 ? "\\" + settings.getServiceName() : "") + ";" +
				"databaseName=" + settings.getDatabaseName();

		if ( settings.useIntegratedAuthentication() ) {
			this.connectionString += ";integratedSecurity=true";
		}
	}


	@Override
	public String getConnectionString() {
		return this.connectionString;
	}

	@Override
	public String getDriverClass() {
		return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	}

	@Override
	public String getEscapeClose() {
		return "]";
	}

	@Override
	public String getEscapeOpen() {
		return "[";
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
