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
import com.emd.xlsutil.upload.UploadOutput;

import com.emd.util.Stringx;

import com.emd.zk.ZKContext;
import com.emd.zk.command.CommandException;

/**
 * The <code>SelectOutput</code> action populates the output file selection.
 *
 * Created: Wed Feb 15 16:43:39 2017
 *
 * @author <a href="mailto:">Oliver Karch</a>
 * @version 1.0
 */
public class SelectOutput extends InventoryCommand {

    private static Log log = LogFactory.getLog(SelectOutput.class);

    /**
     * Creates a new command to select from the result log.
     */
    public SelectOutput() {
	super();
    }

    private InventoryPreferences getPreferences() {
	return InventoryPreferences.getInstance( getPortletId(), getUserId() );
    }

    private OutputFileSelector getOutputFileSelector() {
	ModelProducer[] mp = getPreferences().getResult( OutputFileSelector.class );
	if( mp.length <= 0 )
      	    return null;
	return (OutputFileSelector)mp[0];
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

    private UploadOutput[] getOutputEntries( BatchEntry be ) 
	throws SQLException {

	long uId = be.getUploadid();

	DatasetDAO dao = getInventory();
     	UploadOutput[] outs = dao.findOutputByBatch( uId );
	
	// sort text output first

	List<UploadOutput> textOuts = new ArrayList<UploadOutput>();
	List<UploadOutput> otherOuts = new ArrayList<UploadOutput>();	
	for( int i = 0; i < outs.length; i++ ) {
	    if( outs[i].getMime().startsWith( "text/" ) || outs[i].getMime().equals( "application/xml" ) ) 
		textOuts.add( outs[i] );
	    else
		otherOuts.add( outs[i] );
	}
	log.debug( "Text outputs: "+textOuts.size()+" other formats: "+otherOuts.size() );
	textOuts.addAll( otherOuts );
	return (UploadOutput[])textOuts.toArray( outs );
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
	log.debug( "Selected output batch: "+((be != null)?be.toString():"null") );

	OutputFileSelector rl = getOutputFileSelector();
	if( rl == null ) {
	    log.error( "Cannot determine output file selector" );
	    return;
	}
	
	UploadOutput[] logs = null;
	try {
	    logs = getOutputEntries( be );
	}
	catch( SQLException sqe ) {
	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: "+
	 		 Stringx.getDefault( sqe.getMessage(), "General database error" ) );
	}

	Map ctxt = new HashMap();
	if( logs == null )
	    logs = new UploadOutput[0];
	ctxt.put( OutputFileSelector.RESULT, logs );

	log.debug( "Number of output file entries: "+logs.length );

	rl.assignModel( wnd, ctxt );
    }    
    
} 
