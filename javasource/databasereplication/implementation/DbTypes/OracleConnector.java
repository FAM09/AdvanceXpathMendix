package databasereplication.implementation.DbTypes;

import java.text.SimpleDateFormat;
import java.util.Date;

import replication.ValueParser;
import replication.ValueParser.ParseException;

import com.mendix.core.CoreRuntimeException;
import com.mendix.systemwideinterfaces.core.meta.IMetaPrimitive.PrimitiveType;

import databasereplication.interfaces.IDatabaseSettings;


public class OracleConnector extends IDatabaseConnector {

	protected OracleConnector( IDatabaseSettings settings ) {
		this.connectionString = "jdbc:oracle:thin:" + settings.getUserName() + "/" +
				settings.getPassword() + "@" + settings.getAddress();
		if ( settings.getPort() != null && settings.getPort().length() > 0 )
			this.connectionString += ":" + settings.getPort();

		if ( settings.getServiceName() != null && !"".equals(settings.getServiceName()) ) {
			if ( settings.getDatabaseName() != null && !"".equals(settings.getDatabaseName()) )
				throw new CoreRuntimeException("Invalid configuration, you cannot use both the SID(" + settings.getServiceName() + ") and a Service Name (" + settings.getServiceName() + ")");
			this.connectionString += "/" + settings.getServiceName();
		}
		else if ( settings.getDatabaseName() != null && !"".equals(settings.getDatabaseName()) )
			this.connectionString += ":" + settings.getDatabaseName();
	}

	@Override
	public String getConnectionString() {
		return this.connectionString;
	}

	@Override
	public String getDriverClass() {
		return "oracle.jdbc.OracleDriver";
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
		return false;
	}

	@Override
	public boolean separatEscapeSchemaTable() {
		return true;
	}
	
	private static final SimpleDateFormat format = new SimpleDateFormat( "yyyyMMdd");
	@Override
	public String formatValueForUpdate( PrimitiveType type, String strValue, Object objectValue ) throws ParseException {
		if( objectValue == null )
			return null;
		
		
		if( type == PrimitiveType.DateTime ) {
			Date dValue = ValueParser.getDateValue(null, objectValue, null);
			
			return "to_date('" + format.format(dValue) + "','YYYYMMDD')";
		}
		else 
			return strValue;
	}
	
	@Override
	public boolean closeConnectionAfterQuery() {
		return true;
	}
}
