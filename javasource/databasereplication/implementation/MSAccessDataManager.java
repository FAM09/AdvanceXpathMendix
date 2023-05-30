package databasereplication.implementation;

import replication.ReplicationSettings.MendixReplicationException;
import replication.helpers.MessageOptions;
import replication.implementation.NotImplementedException;

import com.mendix.core.CoreException;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.systemwideinterfaces.core.UserAction;

import databasereplication.proxies.ReplicationStatusValues;

public class MSAccessDataManager extends IDataManager {
	
	protected MSAccessDataManager( IMendixObject tableMapping, String replicationName, DBReplicationSettings settings ) {
		super(tableMapping, replicationName, settings);
	}
	

	@Override
	public IMendixObject startSynchronizing(UserAction<?> action, Boolean applyEntityAccess) throws CoreException {
		IMendixObject result = null;
		try {
			this.prepareForSynchronization(action, applyEntityAccess);

			this.settings.getInfoHandler().printGeneralInfoMessage(this.settings.getLanguage(), MessageOptions.START_SYNCHRONIZING);

			throw new NotImplementedException("Synchronization for MS Access is not implemented");
//			com.healthmarketscience.jackcess.Database db = com.healthmarketscience.jackcess.Database.open(new File(this.settings.getDbSettings().getDatabaseName()));
//			com.healthmarketscience.jackcess.Table table = db.getTable( this.settings.getFromTableName());
//			
//			this.processResultSet(table);
		}
		catch (Exception e) {
			result = this.callFinishingMicroflow( ReplicationStatusValues.Failed );
			if( e instanceof MendixReplicationException )
				throw (MendixReplicationException) e;
			else if( !this.settings.getErrorHandler().generalException(e, e.getMessage()) )
				throw new MendixReplicationException( e );
		}
		finally {
			_instances.remove(this.replicationName);
			this.info.TimeMeasurement.endPerformanceTest("Over all");
		}
		
		return result;
	}
	
//	private void processResultSet(com.healthmarketscience.jackcess.Table table) throws Exception {
//		List<com.healthmarketscience.jackcess.Column> columnList = table.getColumns();
//		
//		this.info.TimeMeasurement.startPerformanceTest("Process resultset metadata");
//		int nrOfColumns = columnList.size();
//		List<String> columns = new ArrayList<String>(nrOfColumns);
//		List<String> referenceColumns = new ArrayList<String>(5);
//		List<String> referenceSetColumns = new ArrayList<String>(1);
//		com.healthmarketscience.jackcess.Column column;
//		String columnName;
//		HashMap<String, PrimitiveType> colTypes = new HashMap<String, PrimitiveType>();
//		PrimitiveType type;
//		for( int i = 0; i < nrOfColumns; i++ ) {
//			column = columnList.get(i);
//			columnName = column.getName();
//			type = this.settings.getMemberType(columnName);
//			colTypes.put(columnName, type);
//			this.settings.getInfoHandler().printTraceMessage(this.settings.getLanguage(), MessageOptions.RETRIEVED_COLUMN_IN_DATASET, i, columnName, (type == null ? "(unknown)" : type) );
//
//			if( this.settings.colIsReference(columnName) ) {
//				referenceColumns.add(columnName);
//			}
//			else if( this.settings.colIsReferenceSet(columnName) ) {
//				referenceSetColumns.add(columnName);
//			}
//			else {
//				columns.add( columnName );
//			}
//		}
//		this.info.TimeMeasurement.endPerformanceTest(true,"Process resultset metadata");
//
//		if( this.settings.useTransactions() )
//			this.settings.getContext().startTransaction();
//		try {
//			//Interface which changes several times to the batch that should be used, which can be either the create or the change batch
//			this.info.TimeMeasurement.startPerformanceTest("Processed all records from resultset");
//			int rowNr = 0;
//			Map<String, Object> row;
//			while((row = table.getNextRow()) != null) {
//				TreeMap<String, Boolean> keys = this.settings.getKeys();
//				try {
//					//TODO fix this  currently only creates new records
//					String objectKey = this.valueParser.buildObjectKey(null, keys);
//
//					for( String colName : columns ) {
//						String columnId = table.getName()+"."+colName;
//						
//						try {
//							type = this.settings.getMemberType( this.settings.getMemberNameByAlias( this.settings.getAliasByOriginalName(columnId) ) );
//							if( type != null ) {
//								try {
//									this.info.addValue(objectKey, this.settings.getAliasByOriginalName(columnId), this.valueParser.getValue(type, this.settings.getAliasByOriginalName(columnId), row.get(colName)));
//								}
//								catch (ParseException e) {
//									if( !this.settings.getErrorHandler().valueException(e, e.getMessage()) )
//										throw e;
//								}
//							}
//							else if( !this.settings.getErrorHandler().invalidSettingsException(null, MessageOptions.UNKNOWN_COLUMN.getMessage(this.settings.getLanguage(), colName) ) )
//								throw new MendixReplicationException(MessageOptions.UNKNOWN_COLUMN.getMessage(this.settings.getLanguage(), colName), MetaInfo._version);
//						}
//						catch (MendixReplicationException e) {
//							if(e.getMessage() ==null || !e.getMessage().startsWith("The original column name is not a valid for"))
//								throw e;
//						}
//					}
//
//					for( String colName : referenceColumns ) {
//						try {
//							this.info.setAssociationValue(objectKey, colName, ValueParser.getValueByType(colTypes.get(colName), row.get(colName)));
//						}
//						catch (ParseException e) {
//							if( !this.settings.getErrorHandler().valueException(e, e.getMessage()) )
//								throw new ParseException("Error while parsing column: " + colName + ", exception: " + e.getMessage(), e);
//						}
//					}
//					if( referenceSetColumns.size() > 0 ) {
//						for( String colName : referenceSetColumns ) {
//							try {
//								this.info.addAssociationValue(objectKey, colName, ValueParser.getValueByType(colTypes.get(colName), row.get(colName)));
//							}
//							catch (ParseException e) {
//								if( !this.settings.getErrorHandler().valueException(e, e.getMessage()) )
//									throw e;
//							}
//						}
//					}
//				}
//				catch (ParseException e) {
//					if( !this.settings.getErrorHandler().valueException(e, e.getMessage()) )
//						throw new MendixReplicationException("Exception occured on row: " + rowNr + "Exception: " + e.getMessage(), MetaInfo._version, e);
//				}
//				catch (Exception e) {
//					throw new MendixReplicationException("Exception occured on row: " + rowNr + "Exception: " + e.getMessage(), MetaInfo._version, e);
//				}
//				finally {
//					rowNr++;
//				}
//			}
//
//
//			this.info.TimeMeasurement.endPerformanceTest("Processed all records from resultset");
//			this.info.TimeMeasurement.startPerformanceTest("Finishing and cleaning up");
//			this.info.finish();
//			this.info.clear();
//			this.info.TimeMeasurement.endPerformanceTest("Finishing and cleaning up");
//		}
//		catch (Exception e) {
//			if( this.settings.useTransactions() )
//				this.settings.getContext().rollbackTransAction();
//			
//			throw e;
//		}
//
//		if( this.settings.useTransactions() )
//			this.settings.getContext().endTransaction();
//	}
}
