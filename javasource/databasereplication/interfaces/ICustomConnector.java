package databasereplication.interfaces;

import java.sql.Connection;

import com.mendix.core.CoreException;

public interface ICustomConnector {
	
	public abstract String getDriverClass();
	
	public abstract String getConnectionString(IDatabaseSettings settings);

	public abstract Connection getConnection(IDatabaseSettings settings) throws CoreException;
}
