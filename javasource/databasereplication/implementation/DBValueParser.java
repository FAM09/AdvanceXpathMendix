package databasereplication.implementation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import replication.ReplicationSettings;
import replication.ValueParser;
import replication.interfaces.IValueParser;

import com.mendix.systemwideinterfaces.core.meta.IMetaPrimitive.PrimitiveType;


public class DBValueParser extends ValueParser {

	public DBValueParser( Map<String, IValueParser> customValueParsers, ReplicationSettings settings ) {
		super(settings, customValueParsers);
	}

	@Override
	public Object getValue( PrimitiveType type, String columnAlias, Object value ) throws ParseException {
		if ( value instanceof ResultSet ) {
			try {
				return super.getValue(type, columnAlias, ((ResultSet) value).getObject(columnAlias));
			}
			catch( SQLException e ) {
				throw new ParseException("Could not get the value for column: " + columnAlias, e);
			}
		}
		
		return super.getValue(type, columnAlias, value);
	}

	@Override
	public Object getValueFromDataSet( String keyAlias, PrimitiveType type, Object dataSet ) throws ParseException {
		try {
			return ((ResultSet) dataSet).getObject(keyAlias);
		}
		catch( SQLException e ) {
			throw new ParseException("Unable to find field: " + keyAlias + " in the resultSet", e);
		}
	}

	@Override
	public String getKeyValueFromAlias(Object recordDataSet, String keyAlias) throws ParseException {
		String keyValue;
		
		if ( this.customValueParsers.containsKey(keyAlias) ) {
			IValueParser vp = this.customValueParsers.get(keyAlias);
			Object value = vp.parseValue(getValueFromDataSet(keyAlias, PrimitiveType.String, recordDataSet));

			keyValue = getTrimmedValue(value, keyAlias);
		}
		else
			keyValue = getKeyValueByPrimitiveType(this.settings.getMemberType(keyAlias), keyAlias,
					getValueFromDataSet(keyAlias, this.settings.getMemberType(keyAlias), recordDataSet));
		
		return keyValue;
	}
}
