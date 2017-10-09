package com.emd.dimpsy.template;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;

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
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.emd.dimpsy.command.InventoryCommand;
import com.emd.dimpsy.config.InventoryPreferences;
import com.emd.dimpsy.view.ModelProducer;

import com.emd.xlsutil.dao.DatasetDAO;

import com.emd.xlsutil.upload.UploadBatch;
import com.emd.xlsutil.upload.UploadOutput;

import com.emd.io.WriterOutputStream;

import com.emd.util.Stringx;

import com.emd.zk.ZKContext;
import com.emd.zk.command.CommandException;

/**
 * The <code>SelectOutputFile</code> action populates the output content area.
 *
 * Created: Mon Feb 20 11:43:39 2017
 *
 * @author <a href="mailto:">Oliver Karch</a>
 * @version 1.0
 */
public class SelectOutputFile extends InventoryCommand {

    private final static String OUTPUT_CONTENT = "txtOutput";
    private final static String TEXT_PLAIN     = "text/plain";

    private final static String TXT_NO_DISPLAY = "Content cannot be displayed.\nContent type is ";

    private static Log log = LogFactory.getLog(SelectOutputFile.class);

    private final static String[] MIME_TEXT = {
	"text/",
	"application/xml"
    };

    /**
     * Creates a new command to select from the result log.
     */
    public SelectOutputFile() {
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

    private UploadOutput getSelectedEntry( Window wnd ) {
	Combobox cb = (Combobox)wnd.getFellowIfAny( getCommandName() );
	if( cb != null ) {
	    int idx = cb.getSelectedIndex();
	    if( idx >= 0 ) {
		return (UploadOutput)cb.getModel().getElementAt(idx);
	    }
	}
	return null;
    }

    private boolean isText( String mime ) {
	for( int i = 0; i < MIME_TEXT.length; i++ ) {
	    if( mime.startsWith( MIME_TEXT[i] ) )
		return true;
	}
	return false;
    }

    private String readAsText( UploadOutput out ) 
	throws IOException {

	String mime = out.getMime();
	String st = null;
	if( isText( Stringx.getDefault( out.getMime(), "" ) )) {
	    DatasetDAO dao = getInventory();
	    StringWriter stw = new StringWriter();
	    OutputStream outs = new WriterOutputStream(stw);
	    dao.writeUploadContentTo( out.getMd5sum(), out.getMime(), outs  );
	    st = stw.toString();
	    outs.close();
	}
	return st;
    }

    private void displayText( Window wnd, String txt ) {
	Textbox tb = (Textbox)wnd.getFellowIfAny( OUTPUT_CONTENT );
	if( tb != null ) 
	    tb.setValue( Stringx.getDefault(txt,"") );
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

	UploadOutput out = getSelectedEntry( wnd );
	log.debug( "Selected output file: "+out );

	displayText( wnd, "" );

	String txt = null;
	try {
	    txt = readAsText( out );
	}
	catch( IOException ioe ) {
	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: "+
	 		 Stringx.getDefault( ioe.getMessage(), "General I/O Error" ) );
	}
	
	if( txt == null ) 
	    txt = TXT_NO_DISPLAY+Stringx.getDefault(out.getMime(),"unknown");

	displayText( wnd, txt );

    }    
    
} 
