package databasereplication.implementation.DbReader;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import replication.MetaInfo;
import replication.ReplicationSettings.AssociationDataHandling;
import replication.ReplicationSettings.ObjectSearchAction;
import replication.ReplicationSettings.ChangeTracking;
import replication.ReplicationSettings.KeyType;
import replication.ValueParser;
import replication.implementation.CustomReplicationSettings;
import replication.implementation.ErrorHandler;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;

import databasereplication.implementation.DBValueParser;
import databasereplication.implementation.DatabaseConnector;
import databasereplication.implementation.ObjectBaseDBSettings;
import databasereplication.proxies.Column;
import databasereplication.proxies.Database;
import databasereplication.proxies.Table;

public class CustomReader {

	private static ILogNode _logNode = Core.getLogger("CustomReader");

	public static void processDatabase( ObjectBaseDBSettings dbSettings, IContext sudoContext, Database curDatabase ) throws CoreException {
		DatabaseConnector connector = new DatabaseConnector(dbSettings);

        Connection c = connector.getConnection();
		try {
			CustomReplicationSettings settings = new CustomReplicationSettings(sudoContext, Table.getType(), new ErrorHandler());
			settings.addColumnMapping("dbId", Table.MemberNames.DbId.toString(), KeyType.ObjectKey, false, null);
			settings.addColumnMapping("tName", Table.MemberNames.Name.toString(), KeyType.ObjectKey, false, null);
			settings.addAssociationMapping("dbAss", Table.MemberNames.Table_Database.toString(), Database.getType(),
					Database.MemberNames.DbId.toString(), null, KeyType.AssociationKey, false)
					.setObjectSearchAction(ObjectSearchAction.FindIgnore);

			settings.addAssociationMapping("colDbId", Column.MemberNames.Column_Table.toString(), Column.getType(),
					Column.MemberNames.DbId.toString(), null, KeyType.AssociationKey, false);
			settings.addAssociationMapping("colName", Column.MemberNames.Column_Table.toString(), Column.getType(),
					Column.MemberNames.Name.toString(), null, KeyType.AssociationKey, false);
			settings.addAssociationMapping("colLength", Column.MemberNames.Column_Table.toString(), Column.getType(),
					Column.MemberNames.Length.toString(), null, KeyType.NoKey, false);
			settings.addAssociationMapping("colTblId", Column.MemberNames.Column_Table.toString(), Column.getType(),
					Column.MemberNames.TableId.toString(), null, KeyType.AssociationKey, false);
			settings.addAssociationMapping("colDtype", Column.MemberNames.Column_Table.toString(), Column.getType(),
					Column.MemberNames.DataType.toString(), null, KeyType.NoKey, false);


			settings.getAssociationConfig(Column.MemberNames.Column_Table.toString())
					.setObjectSearchAction(ObjectSearchAction.FindCreate)
					.setAssociationDataHandling(AssociationDataHandling.Overwrite)
					.setCommitUnchangedObjects(false);

			settings.getMainObjectConfig()
					.setObjectSearchAction(ObjectSearchAction.FindCreate).setCommitUnchangedObjects(true)
					.removeUnusedObjects(ChangeTracking.TrackChanges, Table.MemberNames.UpdateCounter.toString());


			ValueParser vparser = new DBValueParser(settings.getValueParsers(), settings);
			MetaInfo info = new MetaInfo(settings, vparser, "CustomReader");

			DatabaseMetaData dbmd = c.getMetaData();
			boolean anyData = false;
			int schemaPassNr = 0;
			while( anyData == false && schemaPassNr < 3 ) {
				schemaPassNr++;
				ResultSet rs = null;
				switch (schemaPassNr) {
				case 1:
				case 3:
					rs = dbmd.getSchemas();
					break;
				case 2:
					rs = dbmd.getCatalogs();
					break;
				}

				while( rs.next() ) {
					String schemaName = "", catalogName = "";
					switch (schemaPassNr) {
					case 1:
						schemaName = rs.getString("TABLE_SCHEM");
						catalogName = rs.getString("TABLE_CATALOG");
						break;
					case 2:
						catalogName = rs.getString("TABLE_CAT");
						break;
					case 3:
						schemaName = rs.getString("TABLE_SCHEM");
						break;
					}

					_logNode.debug("Schema: " + schemaName + " - Catalog: " + catalogName);

					if ( catalogName == null )
						catalogName = "";
					if ( schemaName == null )
						schemaName = "";

					ResultSet tableRs = null;
					switch (schemaPassNr) {
					case 1:
					case 3:
						tableRs = dbmd.getTables(catalogName, schemaName, "%", null);
						break;
					case 2:
						tableRs = dbmd.getTables("", schemaName, "%", null);
						break;
					}


					while( tableRs.next() ) {
						String tableName = tableRs.getString("TABLE_NAME");
						String fullName = (!isSchemaQualifierRequired(dbmd, schemaName) ? "" : schemaName + ".") + tableName;
						anyData = true;

						if ( dbSettings.getTableFilters().size() == 0 || dbSettings.getTableFilters().contains(fullName) ) {
							String key = curDatabase.getDbId() + ValueParser.keySeparator + fullName.toLowerCase() + ValueParser.keySeparator;

							info.addValue(key, "dbId", curDatabase.getDbId());
							info.addValue(key, "tName", fullName);
							info.setAssociationValue(key, "dbAss", curDatabase.getDbId());

							_logNode.debug("Start getting columns for table: " + fullName);
							try {
								ResultSet colRs = dbmd.getColumns(catalogName, schemaName, tableName, "%");
								while( colRs.next() ) {
									String colName = colRs.getString("COLUMN_NAME");
									_logNode.trace("Adding column: " + colName + " for table: " + fullName);

									info.addAssociationValue(key, "colDbId", curDatabase.getDbId());
									info.addAssociationValue(key, "colName", colName);
									info.addAssociationValue(key, "colLength", colRs.getInt("COLUMN_SIZE"));
									info.addAssociationValue(key, "colTblId", key);
									info.addAssociationValue(key, "colDtype", colRs.getString("TYPE_NAME"));
								}
							}
							catch( SQLException e ) {
								_logNode.error(
										"An error occurred while processing columns for table: " + catalogName + "/" + schemaName + "/" + tableName + " - resuming the import",
										e);
							}
						}
						else
							_logNode.debug("Skipping table: " + fullName + " because it doesn't match the filters.");
					}
					tableRs.close();
				}
				rs.close();
				info.finished();
			}

			/*
			 * Prepare an OQL query that can be used in order to remove the unchanged tables
			 */
			String xPath = "//" + Table.getType() + "[" + Table.MemberNames.DbId.toString() + "=" + curDatabase.getDbId() + "][" + Table.MemberNames.UpdateCounter
					.toString() + "!=" + settings.getMainObjectConfig().getNewRemoveIndicatorValue() + " or " + Table.MemberNames.UpdateCounter
					.toString() + "=NULL]";
			info.removeUnchangedObjectsByQuery(xPath);

		}
		catch( Exception e ) {
			_logNode.error(e);
		}
		finally {
			connector.close();
		}
	}

	private static boolean isSchemaQualifierRequired(DatabaseMetaData dbmd, String schema) throws SQLException {
	  boolean isHsqldbDefault = dbmd.getDatabaseProductName().equals("HSQL Database Engine") && schema.equalsIgnoreCase("PUBLIC");
	  return !(schema.isEmpty() || isHsqldbDefault);
    }

}
