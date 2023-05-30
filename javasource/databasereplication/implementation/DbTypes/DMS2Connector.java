package databasereplication.implementation.DbTypes;

import databasereplication.interfaces.IDatabaseSettings;


public class DMS2Connector extends IDatabaseConnector {

	protected DMS2Connector( IDatabaseSettings settings ) {
		this.connectionString = "jdbc:unisys:dmsql:Unisys.DMSII:resource=" + settings.getDatabaseName() +
				((settings.getAddress() != null && !"".equals(settings.getAddress())) ? ";host=" + settings.getAddress() : "") +
				((settings.getPort() != null && !"".equals(settings.getPort())) ? ";port=" + settings.getPort() : "") +
				";user=" + settings.getUserName() +
				";password=" + settings.getPassword();
	}

	@Override
	public String getConnectionString() {
		return this.connectionString;
	}

	@Override
	public String getDriverClass() {
		return "com.unisys.jdbc.dmsql.Driver";
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
		return false;
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
		return false;
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
