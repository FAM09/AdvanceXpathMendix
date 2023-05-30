// This file was generated by Mendix Studio Pro.
//
// WARNING: Only the following code will be retained when actions are regenerated:
// - the import list
// - the code between BEGIN USER CODE and END USER CODE
// - the code between BEGIN EXTRA CODE and END EXTRA CODE
// Other code you write will be lost the next time you deploy the project.
// Special characters, e.g., é, ö, à, etc. are supported in comments.

package databasereplication.actions;

import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.webui.CustomJavaAction;
import databasereplication.implementation.IDataManager;

public class AbortRunningMapping extends CustomJavaAction<java.lang.Boolean>
{
	private java.lang.String MappingName;
	private java.lang.Boolean CommitPartialDataSet;

	public AbortRunningMapping(IContext context, java.lang.String MappingName, java.lang.Boolean CommitPartialDataSet)
	{
		super(context);
		this.MappingName = MappingName;
		this.CommitPartialDataSet = CommitPartialDataSet;
	}

	@java.lang.Override
	public java.lang.Boolean executeAction() throws Exception
	{
		// BEGIN USER CODE
		IDataManager manager = IDataManager.getIntance( this.MappingName ); 
		if( manager != null )
			manager.abortImport( this.CommitPartialDataSet );
		
		return true;
		// END USER CODE
	}

	/**
	 * Returns a string representation of this action
	 * @return a string representation of this action
	 */
	@java.lang.Override
	public java.lang.String toString()
	{
		return "AbortRunningMapping";
	}

	// BEGIN EXTRA CODE
	// END EXTRA CODE
}
