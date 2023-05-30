package databasereplication.implementation;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import replication.MetaInfo;
import replication.ReplicationSettings.MendixReplicationException;
import replication.ValueParser.ParseException;
import replication.helpers.MessageOptions;

import com.mendix.core.CoreException;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.systemwideinterfaces.core.UserAction;
import com.mendix.systemwideinterfaces.core.meta.IMetaPrimitive.PrimitiveType;

import databasereplication.proxies.ReplicationStatusValues;

public class DatabaseDataManager extends IDataManager {
	/**
	 * Try to find an existing instance of the Synchronizer. Each different importer must be unique by Name
	 * The language is changed after the existing instance if retrieved or after a new instance is created
	 *
	 *
	 * @param replicationName
	 * @param currentLanguage
	 * @param customSynchronizerHandler
	 * @return
	 * @throws CoreException
	 */
	protected DatabaseDataManager( IMendixObject tableMapping, String replicationName, DBReplicationSettings settings) {
		super( tableMapping, replicationName, settings);
	}

	/**
	 * Start synchronizing the objects
	 *  First a connection will be made with the database based on the database type and connection information which was declared in the SynchronizerHandler
	 *  Execute the query and create or synchronize a MendixObject for each row in the result
	 * @param applyEntityAcces
	 * 
	 * @param UserAction, this action can be used to return any feedback to the current user
	 * @throws CoreException
	 */
	@Override
	public IMendixObject startSynchronizing(UserAction<?> action, Boolean applyEntityAcces) throws CoreException {
		IMendixObject result = null;
		try {
			this.prepareForSynchronization(action, applyEntityAcces);

			this.settings.getInfoHandler().printGeneralInfoMessage(this.settings.getLanguage(), MessageOptions.START_SYNCHRONIZING);

			String query = this.settings.getQuery();
			this.settings.getInfoHandler().queryMessage(this.settings.getLanguage(), query );

			try (DatabaseConnector connector = new DatabaseConnector(this.settings.getDbSettings(),
					this.settings.getLanguage(), this.settings.getErrorHandler())) {
				Connection connection = connector.getConnection();
				connection.setAutoCommit(false);

				if (query.length() > 0 && this.state == RunningState.Running) {
					try (Statement statement = connection.createStatement()) {
						this.info.TimeMeasurement.startPerformanceTest("Execute query on foreign db");
						statement.setFetchSize(getSettings().Configuration.MetaInfoProcessingBatchThreshold);
						ResultSet rs = statement.executeQuery(query);
						this.info.TimeMeasurement.endPerformanceTest("Execute query on foreign db");
						result = this.processResultsSet(rs, this.valueParser);
					}
				}
			} catch (SQLException e) {
				if (!this.settings.getErrorHandler().queryException(e, MessageOptions.COULD_NOT_EXECUTE_QUERY
						.getMessage(this.settings.getLanguage(), e.getMessage(), query))) {
				  	throw new MendixReplicationException(MessageOptions.COULD_NOT_EXECUTE_QUERY
							.getMessage(this.settings.getLanguage(), e.getMessage(), query), MetaInfo._version, e);
			  }
			}

		} catch (Exception e) {
			result = this.callFinishingMicroflow(ReplicationStatusValues.Failed);
			if (e instanceof MendixReplicationException)
				throw (MendixReplicationException) e;
			else if (!this.settings.getErrorHandler().generalException(e, e.getMessage()))
				throw new MendixReplicationException(e);
		} finally {
			_instances.remove(this.replicationName);

			if (this.info != null && this.info.TimeMeasurement != null)
				this.info.TimeMeasurement.endPerformanceTest("Over all");
		}
		return result;

	}

	protected IMendixObject processResultsSet(ResultSet rs, DBValueParser valueParser) throws Exception {
		IMendixObject result = null;
		if (rs != null) {
			try {
				this.info.TimeMeasurement.startPerformanceTest("Process resultset metadata");
				ResultSetMetaData md = rs.getMetaData();
				int nrOfColumns = md.getColumnCount();
				List<String> columns = new ArrayList<String>(nrOfColumns);
				List<String> referenceColumns = new ArrayList<String>(5);
				List<String> referenceSetColumns = new ArrayList<String>(1);
				String columnName;
				HashMap<String, PrimitiveType> colTypes = new HashMap<String, PrimitiveType>();
				PrimitiveType type;
				for( int i = 1; i <= nrOfColumns; i++ ) {
					columnName = md.getColumnLabel(i);
					type = this.settings.getMemberType(columnName);
					colTypes.put(columnName, type);
					this.settings.getInfoHandler().printTraceMessage(this.settings.getLanguage(), MessageOptions.RETRIEVED_COLUMN_IN_DATASET, i, columnName, (type == null ? "(unknown)" : type) );

					if( this.settings.treatFieldAsReference(columnName) ) {
						referenceColumns.add(columnName);
					}
					else if( this.settings.treatFieldAsReferenceSet(columnName) ) {
						referenceSetColumns.add(columnName);
					}
					else {
						columns.add( columnName );
					}
				}
				this.info.TimeMeasurement.endPerformanceTest(true,"Process resultset metadata");

				if( this.settings.useTransactions() )
					this.settings.getContext().startTransaction();
				try {
					//Interface which changes several times to the batch that should be used, which can be either the create or the change batch
					this.info.TimeMeasurement.startPerformanceTest("Processed all records from resultset");
					while(rs.next() && this.state == RunningState.Running ) {
						try {
							String objectKey = valueParser.buildObjectKey(rs, this.settings.getMainObjectConfig());

							for( String colName : columns ) {
								type = this.settings.getMemberType(colName);
								if( type != null ) {
									try {
										this.info.addValue(objectKey, colName, valueParser.getValue(type, colName, rs));
									}
									catch (ParseException e) {
										if( !this.settings.getErrorHandler().valueException(e, e.getMessage()) )
											throw e;
									}
								}
								else if( !this.settings.getErrorHandler().invalidSettingsException(null, MessageOptions.UNKNOWN_COLUMN.getMessage(this.settings.getLanguage(), colName) ) )
									throw new MendixReplicationException(MessageOptions.UNKNOWN_COLUMN.getMessage(this.settings.getLanguage(), colName), MetaInfo._version);
							}

							for( String colName : referenceColumns ) {
								try {
									this.info.setAssociationValue(objectKey, colName, valueParser.getValue(colTypes.get(colName), colName, rs));
								}
								catch (ParseException e) {
									if( !this.settings.getErrorHandler().valueException(e, e.getMessage()) )
										throw new ParseException("Error while parsing column: " + colName + ", exception: " + e.getMessage(), e);
								}
							}
							if( referenceSetColumns.size() > 0 ) {
								for( String colName : referenceSetColumns ) {
									try {
										this.info.addAssociationValue(objectKey, colName, valueParser.getValue(colTypes.get(colName), colName, rs));
									}
									catch (ParseException e) {
										if( !this.settings.getErrorHandler().valueException(e, e.getMessage()) )
											throw e;
									}
								}
							}
						}
						catch (ParseException e) {
							if( !this.settings.getErrorHandler().valueException(e, e.getMessage()) )
								throw e;
						}
					}

					if( this.state == RunningState.AbortRollback && rs.next() )
						throw new MendixReplicationException( "Aborting the import: " + this.replicationName, MetaInfo._version );


					this.info.TimeMeasurement.endPerformanceTest("Processed all records from resultset");
					this.info.TimeMeasurement.startPerformanceTest("Finishing and cleaning up");
					this.info.finish();
					result = this.callFinishingMicroflow( ReplicationStatusValues.Succesfull );
					this.info.clear();
					this.info.TimeMeasurement.endPerformanceTest("Finishing and cleaning up");
				}
				catch (Exception e) {
					if( this.settings.useTransactions() )
						this.settings.getContext().rollbackTransAction();

					throw e;
				}

				if (this.settings.useTransactions())
					this.settings.getContext().endTransaction();
			} catch (SQLException e) {
				if (!this.settings.getErrorHandler().queryException(e,
						MessageOptions.ERROR_READING_RESULTSET.getMessage(this.settings.getLanguage())))
					throw new MendixReplicationException(
							MessageOptions.ERROR_READING_RESULTSET.getMessage(this.settings.getLanguage()),
							MetaInfo._version, e);
			} finally {
				rs.close();
			}
		}
		return result;
	}

}
