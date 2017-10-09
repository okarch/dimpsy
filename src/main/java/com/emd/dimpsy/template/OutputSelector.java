package com.emd.dimpsy.template;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Component;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

import org.zkoss.zul.Combobox;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.Window;

import com.emd.dimpsy.config.InventoryPreferences;
import com.emd.dimpsy.view.DefaultModelProducer;

import com.emd.xlsutil.dao.DatasetDAO;
import com.emd.xlsutil.upload.UploadBatch;

import com.emd.util.Stringx;

import com.emd.zk.ZKContext;

/**
 * <code>OutputSelector</code> holds the list of outputs produced by the upload batches.
 *
 * Created: Wed Feb 15 16:24:09 2017
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class OutputSelector extends DefaultModelProducer implements EventListener {

    private static Log log = LogFactory.getLog(OutputSelector.class);

    public static final String COMPONENT_ID = "cbOutputSelector";
    public static final String RESULT = "result";
    

    public OutputSelector() {
	super();
	setModelName( COMPONENT_ID );
    }

    /**
     * Initializes the model producer.
     *
     * @param wnd the application window.
     * @param context the execution context.
     */
    // public void initModel( Window wnd, Map context ) {
    // 	DatasetDAO dao = getSampleInventory();
    // 	if( dao == null ) {
    // 	    writeMessage( wnd, "Error: No database access configured" );
    // 	    return;
    // 	}

    // 	try {
    // 	    InventoryUploadTemplate[] tList = dao.findTemplateByName( "" );

    // 	    Combobox cbTempl = (Combobox)wnd.getFellowIfAny( getModelName() );
    // 	    if( cbTempl != null ) {
    // 		if( context == null )
    // 		    context = new HashMap();
    // 		context.put( RESULT, tList );
    // 		assignModel( cbTempl, context );
    // 	    } 
    // 	}
    // 	catch( SQLException sqe ) {
    // 	    writeMessage( wnd, "Error: Cannot query database: "+Stringx.getDefault(sqe.getMessage(),"reason unknown") );
    // 	    log.error( sqe );
    // 	}
    // }

    /**
     * Returns the number of entries.
     *
     * @param wnd the app window.
     * @return the number of entries.
     */ 
    public int getEntryCount( Window wnd ) {
	Combobox cbTempl = (Combobox)wnd.getFellowIfAny( getModelName() );
     	if( cbTempl != null ) 
	    return cbTempl.getItemCount();
	return 0;
    }	

    protected void assignCombobox( Combobox combobox, Map context ) {
	log.debug( "Output selector context: "+context );

	combobox.addEventListener( "onAfterRender", this );
	// combobox.addEventListener( Events.ON_SELECT, this );

	BatchEntry[] tList = (BatchEntry[])context.get( RESULT );
	if( tList == null )	    
	    combobox.setModel( new ListModelArray( new BatchEntry[0] ) );
	else {
	    log.debug( "Assigning model, number of upload batches: "+tList.length );
	    combobox.setModel( new ListModelArray( tList ) );
	}
    }

    private Window getWindow( Event evt ) {
	Component cmp = evt.getTarget();
	Window wnd = null;
	if( cmp != null )
	    wnd = ZKContext.findWindow( cmp );
	return wnd;
    }

    /**
     * Notifies this listener that an event occurs.
     */
    public void onEvent(Event event)
	throws java.lang.Exception {

	log.debug( "Upload batch selected: "+event );

	if( "onAfterRender".equals( event.getName() ) ) {
	    Combobox cb = (Combobox)event.getTarget();
	    if( cb.getItemCount() > 0 ) {
		cb.setSelectedIndex( 0 );
		SelectOutput selLog = (SelectOutput)InventoryPreferences.getInstance
		    ( getPortletId(), getUserId() ).getCommand( SelectOutput.class );
		if( selLog != null )
		    selLog.execute( ZKContext.getInstance(), getWindow( event ) );
	    }	    
	    else {
		cb.setSelectedIndex( -1 );
	    }
	}	
    }
}
