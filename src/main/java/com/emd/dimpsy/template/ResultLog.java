package com.emd.dimpsy.template;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

import org.zkoss.zul.Grid;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.Window;

import com.emd.dimpsy.view.DefaultModelProducer;

import com.emd.xlsutil.dao.DatasetDAO;
import com.emd.xlsutil.upload.UploadLog;

import com.emd.util.DateComparator;
import com.emd.util.NumericComparator;
import com.emd.util.StringComparator;
import com.emd.util.Stringx;

/**
 * <code>ResultLog</code> holds the list of log messages.
 *
 * Created: Tue Jul 14 18:24:09 2015
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class ResultLog extends DefaultModelProducer {

    private static Log log = LogFactory.getLog(ResultLog.class);

    public static final String COMPONENT_ID = "grResultLog";
    public static final String RESULT = "result";
    

    public ResultLog() {
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
     * Returns the selected upload template.
     *
     * @param wnd the app window.
     * @return the upload template currently selected (or null).
     */ 
    // public InventoryUploadTemplate getSelectedTemplate( Window wnd ) {
    // 	Combobox cbTempl = (Combobox)wnd.getFellowIfAny( getModelName() );
    // 	InventoryUploadTemplate templ = null;
    // 	if( cbTempl != null ) {
    // 	    int sel = cbTempl.getSelectedIndex();
    // 	    if( sel >= 0 )
    // 		templ = cbTempl.getModel().getElementAt( sel );
    // 	}
    // 	return templ;
    // }

    protected void assignGrid( Grid grid, Map context ) {
	log.debug( "Result log context: "+context );
	
	UploadLogRenderer ulr = new UploadLogRenderer();
	grid.setRowRenderer( ulr );

	UploadLog[] logMessages = (UploadLog[])context.get( RESULT );
	if( logMessages == null )	    
	    grid.setModel( new ListModelArray( new UploadLog[0] ) );
	else {
	    log.debug( "Assigning model, number of log messages: "+logMessages.length );
	    grid.setModel( new ListModelArray( logMessages ) );
	}

	initSort( grid, ulr, "colLogstamp", new DateComparator( "ddMMMyyyy hh:mm:ss.SSS" ) );
	initSort( grid, ulr, "colLevel", new StringComparator( true )  );
	initSort( grid, ulr, "colLine", new NumericComparator() );
	initSort( grid, ulr, "colMessage", new StringComparator( true ) );

	writeMessage( grid, String.valueOf(((logMessages != null)?logMessages.length:0))+" messages shown" );

    }

}
