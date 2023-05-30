package databasereplication.implementation.DbReader;

import replication.implementation.NotImplementedException;

import com.mendix.core.CoreException;
import com.mendix.systemwideinterfaces.core.IContext;

import databasereplication.implementation.ObjectBaseDBSettings;
import databasereplication.proxies.Database;


public class DMS2Reader {
	@SuppressWarnings("unused")
	public static void processDatabase(ObjectBaseDBSettings dbSettings, IContext sudoContext, Database curDatabase) throws CoreException {
        throw new NotImplementedException("DMS2 is not fully tested therefore do not selected this databases as source.");
	}
}
