package org.AF.SharePointUtilities.ListSPFiles;

import java.io.File;
import java.io.IOException;

import org.AF.SharePointUtilities.SharePointHelper.SharePointHelper;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.BooleanCell.BooleanCellFactory;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.IntCell.IntCellFactory;
import org.knime.core.data.def.StringCell.StringCellFactory;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelAuthentication;
import org.knime.core.node.defaultnodesettings.SettingsModelAuthentication.AuthenticationType;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;



/**
 * This is an example implementation of the node model of the
 * "ListSPFiles" node.
 * 
 * This example node performs simple number formatting
 * ({@link String#format(String, Object...)}) using a user defined format string
 * on all double columns of its input table.
 *
 * @author Another Fraud
 */
public class ListSPFilesNodeModel extends NodeModel {
    
 	private static final NodeLogger LOGGER = NodeLogger.getLogger(ListSPFilesNodeModel.class);

	
    static final String proxyAuth = "proxyAuth";
    static final String SharePointOnlineSiteURL = "SharePointOnlineSiteURL";
    static final String ClientToken = "ClientToken";
    static final String SharePointName = "SharePointName";
    static final String UseProxy = "UseProxy";
    static final String ProxyHost = "ProxyHost";
    static final String ProxyPort = "ProxyPort";
    static final String spFolderPath = "spFolderPath";
    
	
    
 

	
	static SettingsModelString createSpFolderPathSettingsModel() {
		SettingsModelString coof = new SettingsModelString(spFolderPath, "Shared%20Documents/<FolderName>");
		coof.setEnabled(true);
		return coof;				
	}	
	
	
	static SettingsModelAuthentication createProxySettingsModel() {
		SettingsModelAuthentication cps = new SettingsModelAuthentication(proxyAuth, AuthenticationType.CREDENTIALS);
		cps.setEnabled(false);
		return cps;
	}
	
	static SettingsModelString createSharePointUrlSettingsModel() {
		SettingsModelString coof = new SettingsModelString(SharePointOnlineSiteURL, "<yourSharepoint>.sharepoint.com");
		coof.setEnabled(true);
		return coof;				
	}	
	

	static SettingsModelAuthentication createClientTokenSettingsModel() {
		SettingsModelAuthentication cps = new SettingsModelAuthentication(ClientToken, AuthenticationType.CREDENTIALS);
		cps.setEnabled(true);
		return cps;				
	}	
	
	static SettingsModelString createSharePointNameSettingsModel() {
		SettingsModelString coof = new SettingsModelString(SharePointName, "<yourSharepointSiteName>");
		coof.setEnabled(true);
		return coof;				
	}	
	
	static SettingsModelString createUseProxySettingsModel() {
		SettingsModelString coof = new SettingsModelString(UseProxy, "no proxy");
		coof.setEnabled(true);
		return coof;				
	}	
    
    
	static SettingsModelIntegerBounded createProxyPortIndexModel() {
		SettingsModelIntegerBounded si = new SettingsModelIntegerBounded(ProxyPort, 8080, 0, 65535);
		si.setEnabled(false);
        return si;
    }	
	
	static SettingsModelString createProxyHostSettingsModel() {
		SettingsModelString coof = new SettingsModelString(ProxyHost, null);
		coof.setEnabled(false);
		return coof;				
	}		
	
	
	private final SettingsModelAuthentication m_proxy = createProxySettingsModel();
	private final SettingsModelAuthentication m_clientToken = createClientTokenSettingsModel();
	private final SettingsModelString m_sharePointName = createSharePointNameSettingsModel();
	private final SettingsModelString m_useProxy = createUseProxySettingsModel();
	private final SettingsModelIntegerBounded m_proxyPort = createProxyPortIndexModel();
	private final SettingsModelString m_proxyHost = createProxyHostSettingsModel();
	private final SettingsModelString m_sharePointUrl = createSharePointUrlSettingsModel();
	private final SettingsModelString m_SPFolderPath = createSpFolderPathSettingsModel();

	
	

	/**
	 * Constructor for the node model.
	 */
	protected ListSPFilesNodeModel() {
		/**
		 * Here we specify how many data input and output tables the node should have.
		 * In this case its one input and one output table.
		 */
		super(0, 1);
	}


	
	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {
		/*
		 * The functionality of the node is implemented in the execute method. This
		 * implementation will format each double column of the input table using a user
		 * provided format String. The output will be one String column for each double
		 * column of the input containing the formatted number from the input table. For
		 * simplicity, all other columns are ignored in this example.
		 * 
		 * Some example log output. This will be printed to the KNIME console and KNIME
		 * log.
		 */
		LOGGER.info("Starting List SP Files");

		
	    /* Token variable declaration */
	    String token = m_clientToken.getPassword(getCredentialsProvider());
	    String folderpath = m_SPFolderPath.getStringValue();
	    String sharePointSite = m_sharePointUrl.getStringValue();
	    String sharePointName = m_sharePointName.getStringValue();

        
 	    String proxyUser = m_proxy.getUserName(getCredentialsProvider());
 	    String proxyPass = m_proxy.getPassword(getCredentialsProvider());	
    	String proxyHost = m_proxyHost.getStringValue();
		int proyPort = m_proxyPort.getIntValue();    
	    boolean proxyEnabled = m_useProxy.getStringValue().equals("Use Proxy");        
	
	    
	    /* Null or fail check */
	    if (!token.isEmpty())
	    { 
	        /* Upload path and file name declaration */
	        /*
	         * https://<your_domain>.sharepoint.com/_api/web/
	         * GetFolderByServerRelativeUrl('/Shared%20Documents/<FolderName>')/
	         * Files/
	         */
	  

		    	
	    	 String url = "https://"
	        		+ sharePointSite
	        		+ "/sites/"
	        		+ sharePointName
	        		+ "/_api/Web/GetFolderByServerRelativeUrl('"
	        		+ SharePointHelper.buildCompleteSPPath(folderpath, sharePointName)
	        		+ "')?$expand=Folders,Files&select=FileLeafRef,FileRef,Id";
	        

	     	
	
        	
        		
	        HttpClientBuilder clientbuilder = HttpClients.custom();
	        SharePointHelper.setProxyCredentials(clientbuilder, proxyEnabled, proxyHost, proyPort, proxyUser, proxyPass);
	   	 
	        HttpClient client = clientbuilder.build();
	        

	        
 
	        HttpPost post = new HttpPost(url);
	        SharePointHelper.createProxyRequestConfig(post, proxyEnabled, proxyHost, proyPort);
	        

		    
	        
		    post.setHeader("Authorization", "Bearer " + token);
		    post.setHeader("accept", "application/json;odata=verbose");

	        

	        /* Executing the post request */
		    ClassicHttpResponse response = (ClassicHttpResponse) client.execute(post);
	       
	        
	        String responseBody = EntityUtils.toString(response.getEntity());
	        

	        pushFlowVariableString("ResponseStatus", String.valueOf(response.getCode()));
	        pushFlowVariableString("ResponseString", responseBody);
	        

	        BufferedDataContainer container = exec.createDataContainer(getSpec());
	        
	     if(response.getCode()==200)
	     {
	    	
	    	 
	    	 parseJsonResult(responseBody, container);
	    	 container.close();
	
	    	 return new BufferedDataTable[] { container.getTable() };
	    }
	    else
	    {
	    	container.close();
	    	return new BufferedDataTable[] { container.getTable() };
	    }
	        

	    }
	    else
	    {
	    return new BufferedDataTable[] {};
	    }
	}



	private void parseJsonResult(String responseBody, BufferedDataContainer container) {
		JSONObject jsonObj = new JSONObject(responseBody); 
		    // "I want to iterate though the objects in the array..."

		    JSONObject innerObject = jsonObj.getJSONObject("d").getJSONObject("Files");
		    
		    JSONArray jsonArray = innerObject.getJSONArray("results");
		    int rowCnt = 0;
		    
		    
		    for (int i = 0, size = jsonArray.length(); i < size; i++)
		    {
		      JSONObject objectInArray = jsonArray.getJSONObject(i);
		            
		      
		      addRow(
		    		  container
		    		  ,"Row:"+String.valueOf(rowCnt)
		    		  ,SharePointHelper.getJsonString(objectInArray, "CheckInComment")
		    		  ,SharePointHelper.getJsonInt(objectInArray, "CheckOutType")
		    		  ,SharePointHelper.getJsonString(objectInArray, "ContentTag")
		    		  ,SharePointHelper.getJsonString(objectInArray, "ETag")
		    		  ,SharePointHelper.getJsonBoolean(objectInArray, "Exists")
		    		  ,SharePointHelper.getJsonBoolean(objectInArray, "IrmEnabled")
		    		  ,SharePointHelper.getJsonString(objectInArray, "Length")
		    		  ,SharePointHelper.getJsonInt(objectInArray, "Level")
		    		  ,SharePointHelper.getJsonString(objectInArray, "LinkingUri")
		    		  ,SharePointHelper.getJsonString(objectInArray, "LinkingUrl")
		    		  ,SharePointHelper.getJsonInt(objectInArray, "MajorVersion")
		    		  ,SharePointHelper.getJsonInt(objectInArray, "MinorVersion")
		    		  ,SharePointHelper.getJsonString(objectInArray, "Name")
		    		  ,SharePointHelper.getJsonString(objectInArray, "ServerRelativeUrl")
		    		  ,SharePointHelper.getJsonString(objectInArray, "TimeCreated")
		    		  ,SharePointHelper.getJsonString(objectInArray, "TimeLastModified")
		    		  ,SharePointHelper.getJsonString(objectInArray, "Title")
		    		  ,SharePointHelper.getJsonString(objectInArray, "UniqueId")
		    		  ,"SP.File"
		    		  ,null
		       );		      
		      
		      
		      rowCnt++;
		    }
		    
		    
		    
		    innerObject = jsonObj.getJSONObject("d").getJSONObject("Folders"); 
		    jsonArray = innerObject.getJSONArray("results");

		    
		    for (int i = 0, size = jsonArray.length(); i < size; i++)
		    {
		      JSONObject objectInArray = jsonArray.getJSONObject(i);
		      addRow(
		    		  container
		    		  ,"Row:"+String.valueOf(rowCnt)
		    		  ,""
		    		  ,null
		    		  ,""
		    		  ,""
		    		  ,SharePointHelper.getJsonBoolean(objectInArray, "Exists")
		    		  ,null
		    		  ,""
		    		  ,null
		    		  ,""
		    		  ,""
		    		  ,null
		    		  ,null
		    		  ,SharePointHelper.getJsonString(objectInArray, "Name")
		    		  ,SharePointHelper.getJsonString(objectInArray, "ServerRelativeUrl")
		    		  ,SharePointHelper.getJsonString(objectInArray, "TimeCreated")
		    		  ,SharePointHelper.getJsonString(objectInArray, "TimeLastModified")
		    		  ,""
		    		  ,SharePointHelper.getJsonString(objectInArray, "UniqueId")
		    		  ,"SP.Folder"
		    		  ,SharePointHelper.getJsonInt(objectInArray, "ItemCount")
		       );
		      rowCnt++;
		    }		    
			
	
	}


	private void addRow(
			BufferedDataContainer container
			,String key
			,String CheckInComment
			,Integer CheckOutType
			,String ContentTag
			,String ETag
			,Boolean Exists
			,Boolean IrmEnabled
			,String Length
			,Integer Level
			,String LinkingUri
			,String LinkingUrl
			,Integer MajorVersion
			,Integer MinorVersion
			,String Name
			,String ServerRelativeUrl
			,String TimeCreated
			,String TimeLastModified
			,String Title
			,String UniqueId
			,String Type
			,Integer ItemCount
			)
	{
		container.addRowToTable(
				new DefaultRow(new RowKey(key), new DataCell[] { 
						
						
						StringCellFactory.create(Name)
						,StringCellFactory.create(Type)
						,StringCellFactory.create(TimeCreated)
						,StringCellFactory.create(TimeLastModified)
						,StringCellFactory.create(Title)
						,StringCellFactory.create(UniqueId)
						
					
						,StringCellFactory.create(LinkingUri)
						,StringCellFactory.create(LinkingUrl)
						,StringCellFactory.create(ServerRelativeUrl)
						
						,SharePointHelper.nullableIntCell(MajorVersion)
						,SharePointHelper.nullableIntCell(MinorVersion)
								
						
						,StringCellFactory.create(CheckInComment)
						,SharePointHelper.nullableIntCell(CheckOutType)
						,StringCellFactory.create(ContentTag)
						,StringCellFactory.create(ETag)
						,SharePointHelper.nullableBoolCell(Exists)
						,SharePointHelper.nullableBoolCell(IrmEnabled)
						,StringCellFactory.create(Length)
						,SharePointHelper.nullableIntCell(Level) 
						,SharePointHelper.nullableIntCell(ItemCount)

	
				
				}));
	}
	

	
	private DataTableSpec getSpec()
	{
		
		
		DataTableSpecCreator crator = new DataTableSpecCreator();
		
		
		
		crator.addColumns(new DataColumnSpecCreator("Name", StringCellFactory.TYPE).createSpec());
		crator.addColumns(new DataColumnSpecCreator("Type", StringCellFactory.TYPE).createSpec());
		crator.addColumns(new DataColumnSpecCreator("TimeCreated", StringCellFactory.TYPE).createSpec());
		crator.addColumns(new DataColumnSpecCreator("TimeLastModified", StringCellFactory.TYPE).createSpec());
		crator.addColumns(new DataColumnSpecCreator("Title", StringCellFactory.TYPE).createSpec());
		crator.addColumns(new DataColumnSpecCreator("UniqueId", StringCellFactory.TYPE).createSpec());

		crator.addColumns(new DataColumnSpecCreator("LinkingUri", StringCellFactory.TYPE).createSpec());
		crator.addColumns(new DataColumnSpecCreator("LinkingUrl", StringCellFactory.TYPE).createSpec());
		
		crator.addColumns(new DataColumnSpecCreator("ServerRelativeUrl", StringCellFactory.TYPE).createSpec());
						
		crator.addColumns(new DataColumnSpecCreator("MajorVersion", IntCellFactory.TYPE).createSpec());
		crator.addColumns(new DataColumnSpecCreator("MinorVersion", IntCellFactory.TYPE).createSpec());
		
		
		crator.addColumns(new DataColumnSpecCreator("CheckInComment", StringCellFactory.TYPE).createSpec());
		crator.addColumns(new DataColumnSpecCreator("CheckOutType", IntCellFactory.TYPE).createSpec());
		crator.addColumns(new DataColumnSpecCreator("ContentTag", StringCellFactory.TYPE).createSpec());
		crator.addColumns(new DataColumnSpecCreator("ETag", StringCellFactory.TYPE).createSpec());
		crator.addColumns(new DataColumnSpecCreator("Exists", BooleanCellFactory.TYPE).createSpec());
		crator.addColumns(new DataColumnSpecCreator("IrmEnabled", BooleanCellFactory.TYPE).createSpec());
		crator.addColumns(new DataColumnSpecCreator("Length", StringCellFactory.TYPE).createSpec());
		crator.addColumns(new DataColumnSpecCreator("Level", IntCellFactory.TYPE).createSpec());
		crator.addColumns(new DataColumnSpecCreator("ItemCount", IntCellFactory.TYPE).createSpec());	
  	
        	
		return crator.createSpec();
	}
	
	
	
	
	

	
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
		return new DataTableSpec[] { getSpec() };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		/*
		 * Save user settings to the NodeSettings object. SettingsModels already know how to
		 * save them self to a NodeSettings object by calling the below method. In general,
		 * the NodeSettings object is just a key-value store and has methods to write
		 * all common data types. Hence, you can easily write your settings manually.
		 * See the methods of the NodeSettingsWO.
		 */
		
		m_proxy.saveSettingsTo(settings);

		m_sharePointName.saveSettingsTo(settings);
		m_useProxy.saveSettingsTo(settings);
		m_proxyPort.saveSettingsTo(settings);
		m_proxyHost.saveSettingsTo(settings);
		m_sharePointUrl.saveSettingsTo(settings);
		m_SPFolderPath.saveSettingsTo(settings);
		m_clientToken.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		/*
		 * Load (valid) settings from the NodeSettings object. It can be safely assumed that
		 * the settings are validated by the method below.
		 * 
		 * The SettingsModel will handle the loading. After this call, the current value
		 * (from the view) can be retrieved from the settings model.
		 */

		m_proxy.loadSettingsFrom(settings);
		m_SPFolderPath.loadSettingsFrom(settings);
		m_sharePointName.loadSettingsFrom(settings);
		m_useProxy.loadSettingsFrom(settings);
		m_proxyPort.loadSettingsFrom(settings);
		m_proxyHost.loadSettingsFrom(settings);
		m_sharePointUrl.loadSettingsFrom(settings);
		m_clientToken.loadSettingsFrom(settings);
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		/*
		 * Check if the settings could be applied to our model e.g. if the user provided
		 * format String is empty. In this case we do not need to check as this is
		 * already handled in the dialog. Do not actually set any values of any member
		 * variables.
		 */
		m_proxy.validateSettings(settings);
		m_clientToken.validateSettings(settings);
		m_SPFolderPath.validateSettings(settings);
		m_sharePointName.validateSettings(settings);
		m_useProxy.validateSettings(settings);
		m_proxyPort.validateSettings(settings);
		m_proxyHost.validateSettings(settings);
		m_sharePointUrl.validateSettings(settings);

	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		/*
		 * Advanced method, usually left empty. Everything that is
		 * handed to the output ports is loaded automatically (data returned by the execute
		 * method, models loaded in loadModelContent, and user settings set through
		 * loadSettingsFrom - is all taken care of). Only load the internals
		 * that need to be restored (e.g. data used by the views).
		 */
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		/*
		 * Advanced method, usually left empty. Everything
		 * written to the output ports is saved automatically (data returned by the execute
		 * method, models saved in the saveModelContent, and user settings saved through
		 * saveSettingsTo - is all taken care of). Save only the internals
		 * that need to be preserved (e.g. data used by the views).
		 */
	}

	@Override
	protected void reset() {
		/*
		 * Code executed on a reset of the node. Models built during execute are cleared
		 * and the data handled in loadInternals/saveInternals will be erased.
		 */
	}
}

