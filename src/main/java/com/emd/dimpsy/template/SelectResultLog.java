package com.emd.dimpsy.template;

/**
 * The <code>SelectResultLog</code> action populates the result log message view.
 *
 * Created: Tue Jul 14 12:43:39 2015
 *
 * @author <a href="mailto:">Oliver Karch</a>
 * @version 1.0
 */
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Window;

import com.emd.dimpsy.command.InventoryCommand;
import com.emd.dimpsy.config.InventoryPreferences;
import com.emd.dimpsy.view.ModelProducer;

import com.emd.xlsutil.dao.DatasetDAO;

import com.emd.xlsutil.upload.UploadBatch;
import com.emd.xlsutil.upload.UploadLog;

import com.emd.util.Stringx;

import com.emd.zk.ZKContext;
import com.emd.zk.command.CommandException;

public class SelectResultLog extends InventoryCommand {

    private static Log log = LogFactory.getLog(SelectResultLog.class);

    public static final String[] LEVELS = {
	"Info",
	"Error",
	"Warn"
    };

    /**
     * Creates a new command to select from the result log.
     */
    public SelectResultLog() {
	super();
    }

    private InventoryPreferences getPreferences() {
	return InventoryPreferences.getInstance( getPortletId(), getUserId() );
    }

    private ResultLog getResultLog() {
     	ModelProducer[] mp = getPreferences().getResult( ResultLog.class );
     	if( mp.length <= 0 )
     	    return null;
     	return (ResultLog)mp[0];
    }

    private BatchEntry getSelectedEntry( Window wnd ) {
	Combobox cb = (Combobox)wnd.getFellowIfAny( getCommandName() );
	if( cb != null ) {
	    int idx = cb.getSelectedIndex();
	    if( idx >= 0 ) {
		return (BatchEntry)cb.getModel().getElementAt(idx);
	    }
	}
	return null;
    }

    /**
     * Selects an entry by upload id.
     *
     * @param wnd the window.
     * @param uploadid the upload to select.
     *
     */
    public BatchEntry selectUploadid( Window wnd, long uploadid ) {
	Combobox cb = (Combobox)wnd.getFellowIfAny( getCommandName() );
	BatchEntry be = null;
	if( cb != null ) {
	    ListModel model = cb.getModel();
	    int numEntries = model.getSize();
	    int selIdx = -1;
	    for( int i = 0; i < numEntries; i++ ) {
		be = (BatchEntry)cb.getModel().getElementAt(i);
		if( be.getUploadid() == uploadid ) {
		    selIdx = i;
		    break;
		}
	    }
	    if( selIdx >= 0 )
		cb.setSelectedIndex( selIdx );
	}
	return be;
    }

    private String getLevelFilter( Window wnd ) {
	StringBuilder stb = new StringBuilder();

	for( int i = 0; i < LEVELS.length; i++ ) {
	    Checkbox chk = (Checkbox)wnd.getFellowIfAny( "chkResult"+LEVELS[i] );
	    if( (chk != null) && (chk.isChecked()) ) {
		if( stb.length() > 0 )
		    stb.append( "," );
		stb.append( LEVELS[i].toUpperCase() );
	    }
	}
	return stb.toString();
    }

    private UploadLog[] getLogEntries( BatchEntry be, String levels ) 
	throws SQLException {

	UploadBatch ub = be.getUploadBatch();
	if( ub == null )
	    throw new SQLException( "Cannot determine upload batch" );

	DatasetDAO dao = getInventory();
	return dao.findLogByUpload( ub, levels );
    }

    /**
     * Executes the <code>Command</code>
     * @param context
     *      an {@link com.emd.zk.ZKContext} object holds the ZK specific data
     * 
     * @param wnd
     *      an {@link  org.zkoss.zul.Window} object representing the form
     *
     */
    public void execute( ZKContext context, Window wnd )
	throws CommandException {

	BatchEntry be = getSelectedEntry( wnd );
	log.debug( "Selected result log: "+((be != null)?be.toString():"null") );

	String levelFilter = getLevelFilter( wnd );
	log.debug( "Levels to select: "+levelFilter );

	ResultLog rl = getResultLog();
	if( rl == null ) {
	    log.error( "Cannot determine result log" );
	    return;
	}
	
	UploadLog[] logs = null;
	try {
	    logs = getLogEntries( be, levelFilter );
	}
	catch( SQLException sqe ) {
	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: "+
			 Stringx.getDefault( sqe.getMessage(), "General database error" ) );
	}

	Map ctxt = new HashMap();
	if( logs == null )
	    logs = new UploadLog[0];
	ctxt.put( ResultLog.RESULT, logs );

	log.debug( "Number of relevant log entries: "+logs.length );

	rl.assignModel( wnd, ctxt );
    }    
    
} 
