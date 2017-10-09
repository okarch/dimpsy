package com.emd.dimpsy.template;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Window;

import com.emd.dimpsy.command.InventoryCommand;
import com.emd.dimpsy.config.InventoryPreferences;
import com.emd.dimpsy.view.ModelProducer;

import com.emd.xlsutil.upload.UploadTemplate;
import com.emd.xlsutil.upload.UploadBatch;

import com.emd.xlsutil.dao.DatasetDAO;

// import com.emd.util.Parameter;
import com.emd.util.Stringx;

import com.emd.zk.ZKContext;
import com.emd.zk.command.CommandException;

/**
 * The <code>OpenOutput</code> action populates the output section.
 *
 * Created: Sun Jul 12 12:43:39 2015
 *
 * @author <a href="mailto:">Oliver Karch</a>
 * @version 1.0
 */
public class OpenOutput extends InventoryCommand {
    private long lastOpened;
    private UploadTemplate lastTemplate;

    private int numLogs;

    private static Log log = LogFactory.getLog(OpenOutput.class);

    private static final long EXPIRE_TIME = 10L * 60L * 1000L; // 10 minutes

    /**
     * Creates a new command to select the result log.
     */
    public OpenOutput() {
	super();
	this.lastOpened = 0L;
	this.numLogs = 20;
	this.lastTemplate = null;
    }

    private InventoryPreferences getPreferences() {
	return InventoryPreferences.getInstance( getPortletId(), getUserId() );
    }

    private OutputSelector getOutputSelector() {
	ModelProducer[] mp = getPreferences().getResult( OutputSelector.class );
	if( mp.length <= 0 )
	    return null;
	return (OutputSelector)mp[0];
    }

    private boolean logExpired( Window wnd ) {
	boolean exp = ((System.currentTimeMillis() - lastOpened) > EXPIRE_TIME);
	if( exp )
	    return true;
	if( (lastTemplate != null) && (!lastTemplate.equals(getTemplate(wnd))) )
	    return true;
	OutputSelector uSel = getOutputSelector();
	return (uSel.getEntryCount( wnd ) <= 0);
    }

    private UploadTemplate getTemplate( Window wnd ) {
	ModelProducer[] mp = getPreferences().getResult( TemplateModel.class );
	if( mp.length <= 0 )
	    return null;
	return ((TemplateModel)mp[0]).getSelectedTemplate( wnd );
    }

    /**
     * Initializes the log selector model.
     *
     * @param wnd the app window.
     * @param templ the upload template used.
     */
    public void initLogs( Window wnd, UploadTemplate templ ) 
	throws SQLException {

	DatasetDAO dao = getInventory();

	UploadBatch[] latestLogs = dao.findLatestOutput();

	log.debug( "Found "+latestLogs.length+" output entries, template upload batches: "+templ.getUploadBatches().length );
	int nLogs = Math.min( latestLogs.length, getNumLogs() );
	int nn = 0;
	
	TreeSet ts = new TreeSet<BatchEntry>( BatchEntry.COMPARATOR );
	for( int i = 0; i < latestLogs.length; i++ ) {
	    if( (templ.getUploadBatch( latestLogs[i].getUploadid()) != null) &&
		((nLogs < 0) || (nn < nLogs)) ) {
		
		log.debug( "Adding batch entry: "+latestLogs[i].getLogstamp() );
		BatchEntry be = new BatchEntry( latestLogs[i], templ.getTemplatename() );
		be.setPrintFormat( BatchEntry.FMT_OUTPUT );
		ts.add( be );
		nn++;
	    }
	}
	
	OutputSelector updSel = getOutputSelector();
	if( updSel == null ) {
	    log.error( "No output selector model found" );
	    return;
	}

	log.debug( "Number of batch entries: "+ts.size() );
	
	BatchEntry[] updls = new BatchEntry[ ts.size() ];
	Map context = new HashMap();
	context.put( OutputSelector.RESULT, (BatchEntry[])ts.toArray( updls ) );
	updSel.assignModel( wnd, context );
    }

    /**
     * Get the <code>NumLogs</code> value.
     *
     * @return an <code>int</code> value
     */
    public final int getNumLogs() {
	return numLogs;
    }

    /**
     * Set the <code>NumLogs</code> value.
     *
     * @param numLogs The new NumLogs value.
     */
    public final void setNumLogs(final int numLogs) {
	this.numLogs = numLogs;
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

	log.debug( "Output opened" );
	if( logExpired( wnd ) ) {
	    log.debug( "Expired output log" );
	    UploadTemplate iTempl = getTemplate( wnd );
	    if( iTempl == null ) {
		showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: No template selected" );
		return;
	    }
	    lastTemplate = iTempl;

	    try {
		initLogs( wnd, iTempl );
	    }
	    catch( SQLException sqe ) {
		showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: "+
			     Stringx.getDefault( sqe.getMessage(), "General database error" ) );
		return;
	    }

	    lastOpened = System.currentTimeMillis();
	}
    }    
    
} 
