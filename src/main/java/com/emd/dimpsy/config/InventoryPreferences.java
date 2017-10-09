package com.emd.dimpsy.config;

/**
 * <code>InventoryPreferences</code> holds the user and portlet preferences.
 *
 * Created: Wed Mar 17 14:12:16 2015
 *
 * @author <a href="mailto:">Oliver</a>
 * @version 1.0
 */
// import java.lang.reflect.Method;

import java.net.URL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import java.sql.SQLException;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.beanutils.XMLBeanDeclaration;
import org.apache.commons.configuration.beanutils.BeanDeclaration;
import org.apache.commons.configuration.beanutils.BeanHelper;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Component;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.EventListener;

import org.zkoss.zul.Longbox;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Window;

import com.emd.dimpsy.command.InventoryCommand;

import com.emd.dimpsy.view.DefaultModelProducer;
import com.emd.dimpsy.view.ModelProducer;
import com.emd.dimpsy.view.UIUtils;

import com.emd.util.CommonsConfiguration;
import com.emd.util.Named;
import com.emd.util.Stringx;

import com.emd.xlsutil.dao.DatasetDAO;

import com.emd.zk.ZKContext;
import com.emd.zk.ZKUtil;

public class InventoryPreferences implements EventListener {
    private DatasetDAO      sampleInventoryDAO;
    private String                  portletId;
    private long                    userId;
    private List<InventoryCommand>  commands;
    private List<ModelProducer>     results;

    private static Map<String,InventoryPreferences> inventoryPreferences;

    private static final String RESOURCE_NAME = "dimpsy-config.xml";
    private static final long   DEFAULT_REFRESH = 6L * 60L * 1000L; // 6 minutes

    private static Log log = LogFactory.getLog(InventoryPreferences.class);

    public InventoryPreferences() {
	this.commands = new ArrayList<InventoryCommand>();
	this.results = new ArrayList<ModelProducer>();	
    }

    private static URL locateConfigFile() {
	URL url = InventoryPreferences.class.getClassLoader().getResource( RESOURCE_NAME );
	if( url == null )
	    url = InventoryPreferences.class.getResource( "/"+RESOURCE_NAME );
	return url;
    }

    private static InventoryPreferences readConfig( URL url ) {
	InventoryPreferences cq = null;
	try {
	    XMLConfiguration config = new XMLConfiguration( url );
	    config.setDelimiterParsingDisabled( true );
	    FileChangedReloadingStrategy frs = new FileChangedReloadingStrategy();
	    frs.setRefreshDelay( DEFAULT_REFRESH );
	    config.setReloadingStrategy( frs );
		
	    CommonsConfiguration cfg = new CommonsConfiguration( config );
	    cq = (InventoryPreferences)cfg.toObject();
	    log.debug( "Inventory configuration loaded: "+cq );
	}
	catch( ConfigurationException cex ) {
	    String errMsg = "Cannot load configuration from "+url+": "+
		Stringx.getDefault( cex.getMessage(), "" );
	    log.error( errMsg );
	    cq = new InventoryPreferences();
	}
	return cq;
    }

    /**
     * Returns a map of dossier sessions.
     *
     * @return the map of dossier sessions (can be null).
     */
    public synchronized static InventoryPreferences[] getInventorySessions() {
	if( inventoryPreferences == null )
	    return new InventoryPreferences[0];
	Collection<InventoryPreferences> vals = inventoryPreferences.values();
	InventoryPreferences[] dPrefs = new InventoryPreferences[ vals.size() ];
	return (InventoryPreferences[])vals.toArray( dPrefs );
    }

    /**
     * Returns an instance of the preferences. It will be constructed if it doesn't exist.
     *
     * @param portletId the portlet's id
     * @param userId the user's id
     *
     * @return an instance of the preferences
     */    
    public static InventoryPreferences getInstance( String portletId, long userId ) {
	if( inventoryPreferences == null ) 
	    inventoryPreferences = new HashMap<String,InventoryPreferences>();
	String puid = portletId+"."+userId;
	InventoryPreferences dp = inventoryPreferences.get( puid );
	if( dp == null ) {
	    log.debug( "Cannot find preferences for "+puid );
	    
	    URL cfgUrl = locateConfigFile();
	    if( cfgUrl == null ) {
		log.error( "Cannot locate "+RESOURCE_NAME+". No dossier preferences configured" );
		dp = new InventoryPreferences();
	    }
	    else {
		log.error( "Reading dossier configuration from "+cfgUrl );
		dp = readConfig( cfgUrl );
	    }
	    dp.setPortletId( portletId );
	    dp.setUserId( userId );
	    dp.updateCommands();
	    dp.updateResults();
	    dp.updateQueryActions();
	    
	    inventoryPreferences.put( puid, dp );
	}

	return dp;
    }

    void updateCommands() {
	Iterator<InventoryCommand> it = commands.iterator();
	while( it.hasNext() ) {
	    InventoryCommand qt = it.next();
	    qt.setPortletId( this.getPortletId() );
	    qt.setUserId( this.getUserId() );
	}
    }

    void updateResults() {
	Iterator<ModelProducer> it = results.iterator();
	while( it.hasNext() ) {
	    ModelProducer qt = it.next();
	    if( qt instanceof DefaultModelProducer ) {
		((DefaultModelProducer)qt).setPortletId( this.getPortletId() );
		((DefaultModelProducer)qt).setUserId( this.getUserId() );
	    }
	}
    }

    void updateQueryActions() {
	// Iterator<QueryTranslator> it = queries.iterator();
	// while( it.hasNext() ) {
	//     QueryTranslator qt = it.next();
	//      if( qt instanceof AbstractQueryTranslator )
	// 	 ((AbstractQueryTranslator)qt).updateActions( this.getPortletId(), this.getUserId() );
	// }
    }

    /**
     * Get the PortletId value.
     * @return the PortletId value.
     */
    public String getPortletId() {
	return portletId;
    }

    /**
     * Set the PortletId value.
     * @param newPortletId The new PortletId value.
     */
    public void setPortletId(String newPortletId) {
	this.portletId = newPortletId;
    }

    /**
     * Get the UserId value.
     * @return the UserId value.
     */
    public long getUserId() {
	return userId;
    }

    /**
     * Set the UserId value.
     * @param newUserId The new UserId value.
     */
    public void setUserId(long newUserId) {
	this.userId = newUserId;
    }

    /**
     * Sets the database access object.
     *
     * @param dao database access object
     */
    public void setInventory( DatasetDAO dao ) {
	this.sampleInventoryDAO = dao;
    }

    /**
     * Get the DatasetDAO value.
     * @return the DatasetDAO value.
     */
    public DatasetDAO getInventory() {
	return this.sampleInventoryDAO;
    }

    /**
     * Adds a command
     *
     * @param cmd the <code>InventoryCommand</code>
     */
    public void addCommand( InventoryCommand cmd ) {
	commands.add( cmd );
    }

    /**
     * Removes the Command value.
     *
     * @param cmd the name of the command.
     * @return the Command value.
     */
    public InventoryCommand removeCommand( Window wnd, String cmd ) {
	Iterator<InventoryCommand> it = commands.iterator();
	InventoryCommand qt = null;
	while( it.hasNext() ) {
	    qt = it.next();
	    if( qt.getCommandName().equals(cmd) ) 
		break;
	}
	if( qt != null ) {
	    commands.remove( qt );
	    Component cmp = wnd.getFellowIfAny( cmd );
	    if( cmp != null )
		cmp.removeEventListener( qt.getEvent(), qt );
	}
	return qt;
    }

    /**
     * Get the Command value.
     *
     * @param cmd the name of the command.
     * @return the Command value.
     */
    public InventoryCommand getCommand( String cmd ) {
	Iterator<InventoryCommand> it = commands.iterator();
	while( it.hasNext() ) {
	    InventoryCommand qt = it.next();
	    if( qt.getCommandName().equals(cmd) ) 
		return qt;
	}
	return new InventoryCommand();
    }

    /**
     * Get the Command value.
     *
     * @param cmd the class of the command.
     * @return the Command instance.
     */
    public InventoryCommand getCommand( Class cmd ) {
	Iterator<InventoryCommand> it = commands.iterator();
	while( it.hasNext() ) {
	    InventoryCommand qt = it.next();
	    if( cmd.isInstance( qt ) ) 
		return qt;
	}
	return null;
    }

    /**
     * Set the Command value.
     * @param newCommand The new Command value.
     */
    public void setCommand(InventoryCommand newCommand) {
	addCommand( newCommand );
    }

    /**
     * Get the Commands.
     *
     * @return a (possibly empty) array of commands.
     */
    public InventoryCommand[] getCommands() {
	InventoryCommand[] cmds = new InventoryCommand[ commands.size() ];
	return (InventoryCommand[])commands.toArray( cmds );
    }

    /**
     * Adds a result model.
     *
     * @param cmd the <code>ModelProducer</code>
     */
    public void addResult( ModelProducer cmd ) {
	results.add( cmd );
    }

    /**
     * Get the Result value.
     *
     * @param cmd the name of the result model.
     * @return the Result value.
     */
    public ModelProducer getResult( String cmd ) {
	Iterator<ModelProducer> it = results.iterator();
	while( it.hasNext() ) {
	    ModelProducer qt = it.next();
	    if( (qt instanceof DefaultModelProducer) && (((DefaultModelProducer)qt).getModelName().equals(cmd)) ) 
		return qt;
	}
	return null;
    }

    /**
     * Get the Result value by class.
     *
     * @param cmd the name of the result model.
     * @return the Result value.
     */
    public ModelProducer[] getResult( Class cmd ) {
	Iterator<ModelProducer> it = results.iterator();
	List<ModelProducer> mps = new ArrayList<ModelProducer>();
	while( it.hasNext() ) {
	    ModelProducer qt = it.next();
	    if( cmd.isInstance( qt ) ) 
		mps.add( qt );
	}
	ModelProducer[] mProds = new ModelProducer[ mps.size() ];
	return (ModelProducer[])mps.toArray( mProds );
    }

    /**
     * Set the Result value.
     * @param newCommand The new Result  value.
     */
    public void setResult(ModelProducer newCommand) {
	addResult( newCommand );
    }

    /**
     * Removes the Result value.
     * @param newCommand The Result value to be removed.
     */
    public ModelProducer removeResult(ModelProducer newCommand) {
	return (results.remove( newCommand )?newCommand:null);
    }

    /**
     * Removes the Result value.
     * @param cmd Result model name to be removed.
     */
    public ModelProducer removeResult( String cmd ) {
	ModelProducer mp = getResult( cmd );
	if( mp == null )
	    return null;

	return this.removeResult( mp );
    }

    /**
     * Get the Commands.
     *
     * @return a (possibly empty) array of commands.
     */
    public ModelProducer[] getResults() {
	ModelProducer[] cmds = new ModelProducer[ results.size() ];
	return (ModelProducer[])results.toArray( cmds );
    }
    
    private void writeMessage( Window wnd, String msg ) {
	UIUtils.showMessage( wnd, "rowMessage", "lbMessage", msg );
    }

    /**
     * Initializes all the views of the browser.
     *
     * @param wnd The application window.
     * @param zk The application context.
     */
    public void initViews( Window wnd, ZKContext zk ) {
	ModelProducer[] models = getResults();
	log.debug( "Initialize "+String.valueOf(models.length)+" result models" );
	for( int i = 0; i < models.length; i++ ) {
	    models[i].initModel( wnd, null );
	    log.debug( "Model "+models[i]+" initialized" );
	}
	for( int i = 0; i < models.length; i++ ) {
	    models[i].updateModel( wnd, null );
	    log.debug( "Model "+models[i]+" updated" );
	}
    }

    /**
     * Notifies this listener that an event occurs.
     */
    public void onEvent(Event event)
	throws java.lang.Exception {

	Component cmp = event.getTarget();
	if( cmp instanceof Tab ) {

	    // Tab tb = (Tab)cmp;
	    // String tabSt = Stringx.getDefault(tb.getLabel(), "" );
	    // if( "Summary".equals( tabSt ) )
	    // 	return;

	    // log.debug( "Selecting tab: "+tabSt );
	    // Window wnd = ZKUtil.findWindow( cmp );

	    // DossierResult dossier = null;
	    // boolean storeResult = false;
	    // long geneId = -1L;
	    // SampleInventoryDAO dao = getDatabase();
	    // boolean searchable = false;

	    // try {
	    // 	searchable = dao.isSearchable( getPortletId() );
	    // 	geneId = dao.getGeneId( getPortletId() );
	    // 	if( geneId <= 0 ) {
	    // 	    if( !searchable ) {
	    // 		writeMessage( wnd, "Error: No gene id configured" );
	    // 		return;
	    // 	    }
	    // 	    else {
	    // 		geneId = searchGene( wnd );
	    // 		if( geneId <= 0 ) 
	    // 		    dossier = DossierResult.createInfo( "No gene selected" ); 
	    // 	    }
	    // 	}
	    // 	else
	    // 	    dossier = dao.getDossier( geneId, tabSt );
	    // }
	    // catch( SQLException sqe ) {
	    // 	writeMessage( wnd, "Error: Cannot query database: "+Stringx.getDefault(sqe.getMessage(),"reason unknown") );
	    // 	log.error( sqe );
	    // 	return;
	    // }

	    // QueryTranslator qt = getQueryTranslator( tabSt );
	    // if( (!isViewAvailable( tabSt+geneId )) || 
	    // 	((dossier != null) && (dossier.isExpired())) ) {

	    // 	log.debug( "Need to query "+qt );
	    // 	List<DossierResult> res = new ArrayList<DossierResult>();
	    // 	if( qt != null )
	    // 	    qt.runQuery( createQuery(geneId, tabSt, wnd), res );

	    // 	if( res.size() > 0 ) {
	    // 	    dossier = res.get(0);
	    // 	    storeResult = true;
	    // 	}
	    // }

	    // if( (dossier != null) && (qt != null) ) {
	    // 	log.debug( "Layout "+tabSt+" results" );
	    // 	Map context = createContext( dossier, searchable ); 
	    // 	qt.layout( wnd, context );
	    // 	setViewAvailable( tabSt+geneId, true );
	    // 	if( storeResult && (geneId > 0L) ) {
	    // 	    log.debug( "Storing "+tabSt+" results" );
	    // 	    try {
	    // 		dao.setDossier( geneId, dossier );
	    // 	    }
	    // 	    catch( SQLException sqe ) {
	    // 		writeMessage( wnd, "Error: Cannot store dossier results: "+Stringx.getDefault(sqe.getMessage(),"reason unknown") );
	    // 		log.error( sqe );
	    // 	    }
	    // 	}
	    // }
	}
    }

}
