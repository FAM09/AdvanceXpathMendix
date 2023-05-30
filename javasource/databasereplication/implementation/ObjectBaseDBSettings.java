package databasereplication.implementation;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.systemwideinterfaces.MendixRuntimeException;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixIdentifier;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import databasereplication.implementation.DbTypes.DbConnectionFactory;
import databasereplication.implementation.DbTypes.IDatabaseConnector;
import databasereplication.interfaces.IDatabaseSettings;
import databasereplication.proxies.DBType;
import databasereplication.proxies.Database;
import databasereplication.proxies.TableFilter;

public class ObjectBaseDBSettings extends IDatabaseSettings {
	
	private static final String CHIPHER_NAME = "AES/CBC/PKCS5PADDING";
	private IMendixObject dbObject;
	private IContext context;
	private FilterList tablesFilter;

	public ObjectBaseDBSettings( IContext context, IMendixObject databaseObject ) {
		this.dbObject = databaseObject;
		this.context = context;
	}

	@Override
	public String getAddress() {
		String dbAddress = this.dbObject.getValue(this.getContext(), Database.MemberNames.DatabaseURL.toString());
		if( dbAddress == null )
			return null;
		
		return dbAddress.trim();
	}

	@Override
	public String getDatabaseName() {
		String dbName = this.dbObject.getValue(this.getContext(), Database.MemberNames.DatabaseName.toString());
		if( dbName == null )
			return null;
		
		return dbName.trim();
	}


	@Override
	public IDatabaseConnector getDatabaseConnection() {
		DbConnectionFactory dbCFactory = DbConnectionFactory.getInstance(
				DBType.valueOf((String) this.dbObject.getValue(this.getContext(), Database.MemberNames.DatabaseType.toString())), this);
		return dbCFactory.getConnector();
	}

	@Override
	public String getPassword() {
		String dbPassword = this.dbObject.getValue(this.getContext(), Database.MemberNames.DatabasePassword_Encrypted.toString());
		if ( dbPassword != null && !"".equals(dbPassword) )
			return decryptString(dbPassword);

		return this.dbObject.getValue(this.getContext(), Database.MemberNames.DatabasePassword.toString());
	}

	@Override
	public String getPort() {
		Integer value = this.dbObject.getValue(this.getContext(), Database.MemberNames.Port.toString());
		if ( value != null )
			return String.valueOf(value);

		return null;
	}

	@Override
	public String getServiceName() {
		String service = this.dbObject.getValue(this.getContext(), Database.MemberNames.ServiceName.toString());
		if( service == null )
			return null;
		
		return service.trim();
	}

	@Override
	public String getUserName() {
		String user = this.dbObject.getValue(this.getContext(), Database.MemberNames.DatabaseUser.toString());
		if( user == null )
			return null;
		
		return user.trim();
	}

	@Override
	public IContext getContext() {
		return this.context;
	}

	@Override
	public boolean useIntegratedAuthentication() {
		return (Boolean) this.dbObject.getValue(this.getContext(), Database.MemberNames.useIntegratedSecurity.toString());
	}

	public static String decryptString( String valueToDecrypt ) {
		if ( valueToDecrypt == null )
			return null;
		try {
			Cipher c = Cipher.getInstance(CHIPHER_NAME);
			
			String[] s = valueToDecrypt.split(";");
			if ( s.length < 2 ) // Not an encrypted string, just return the original value.
				return valueToDecrypt;
			Decoder decoder = Base64.getDecoder();
			byte[] decodedVector = decoder.decode(s[0]);
			byte[] decodedEncryptedData = decoder.decode(s[1]);
			c.init(Cipher.DECRYPT_MODE, createSecretKeySpec(), new IvParameterSpec(decodedVector));
			
			byte[] originalData = c.doFinal(decodedEncryptedData);
			
			return new String(originalData, StandardCharsets.UTF_8);
		}
		catch( Exception e ) {
			throw new MendixRuntimeException("Unable to decrypt the password", e);
		}
	}

	public static String encryptString( String valueToEncrypt ) throws Exception {
		if ( valueToEncrypt == null )
			return null;
		Cipher c = Cipher.getInstance(CHIPHER_NAME);
		c.init(Cipher.ENCRYPT_MODE, createSecretKeySpec());
		
		byte[] encryptedData = c.doFinal(valueToEncrypt.getBytes(StandardCharsets.UTF_8));
		
		Encoder encoder = Base64.getEncoder();
		String encodedVector = encoder.encodeToString(c.getIV());
		String encodedEncryptedData = encoder.encodeToString(encryptedData);
		
		return encodedVector + ";" + encodedEncryptedData; 
	}
	
	private static SecretKeySpec createSecretKeySpec() {
		final String key = "n0cwq7cmwq978c0m";
		
		return new SecretKeySpec(key.getBytes(), "AES");
	}

	@Override
	public FilterList getTableFilters() throws CoreException {
		this.tablesFilter = null;

		if ( this.tablesFilter == null ) {
			this.tablesFilter = new FilterList();
			List<IMendixObject> result = Core.createXPathQuery(String.format("//%s[%s=$value]",
					TableFilter.entityName,
					TableFilter.MemberNames.TableFilter_Database.toString()))
					.setVariable("value",  this.dbObject.getId())
					.execute(getContext());
			if ( result.size() > 0 ) {
				for( IMendixObject resultObj : result ) {
					this.tablesFilter.add((String) resultObj.getValue(getContext(), TableFilter.MemberNames.Filter.toString()));
				}
			}
		}

		return this.tablesFilter;
	}

	@Override
	public IMendixIdentifier getCustomConnectionInfo() {
		return this.dbObject.getValue(this.context, Database.MemberNames.Database_CustomConnectionInfo.toString());
	}
}
