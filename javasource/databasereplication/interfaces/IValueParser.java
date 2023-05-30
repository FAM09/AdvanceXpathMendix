package databasereplication.interfaces;

import java.sql.ResultSet;
import java.sql.SQLException;

import replication.ValueParser.ParseException;

public abstract class IValueParser implements replication.interfaces.IValueParser {

	public Object parseValue(String columnAlias, ResultSet rs) throws ParseException {
		try {
			return parseValue( rs.getObject(columnAlias) );
        }
        catch (SQLException e) {
        	throw new ParseException(e);
        }
	}
	
}
