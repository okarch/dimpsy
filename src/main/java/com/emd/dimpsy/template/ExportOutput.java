package com.emd.dimpsy.template;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.io.IOUtils;
 
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Window;

import com.emd.dimpsy.command.InventoryCommand;
import com.emd.dimpsy.config.InventoryPreferences;

import com.emd.xlsutil.upload.UploadOutput;

import com.emd.util.Stringx;

import com.emd.zk.ZKContext;
import com.emd.zk.command.CommandException;

/**
 * <code>ExportOutput</code> handles download of output files.
 *
 * Created: Fri Apr 14 22:50:09 2017
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class ExportOutput extends InventoryCommand {
    private String formatSelector;

    private static Log log = LogFactory.getLog(ExportOutput.class);

    private static final String ALL_OUTPUT = "all-zipped";
    private static final String MIME_ZIP   = "application/zip";


    public ExportOutput() {
	super();
    }

    /**
     * Get the <code>FormatSelector</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getFormatSelector() {
	return formatSelector;
    }

    /**
     * Set the <code>FormatSelector</code> value.
     *
     * @param formatSelector The new FormatSelector value.
     */
    public final void setFormatSelector(final String formatSelector) {
	this.formatSelector = formatSelector;
    }

    private UploadOutput[] getSelectedOutput( Window wnd, boolean all ) {
	OutputFileSelector tList = (OutputFileSelector)findModelProducer( OutputFileSelector.class );
	UploadOutput[] outs = null;
	if( tList != null ) {
	    if( !all ) {
		UploadOutput out = tList.getSelectedOutput( wnd );
		if( out != null ) {
		    outs = new UploadOutput[1];
		    outs[0] = out;
		}
	    }
	    else 
		outs = tList.getAllOutput( wnd );
	}

	if( outs == null )
	    outs = new UploadOutput[0];
	return outs;
    }

    private String getSelectedFormat( Window wnd ) {
	Combobox cbFormat = (Combobox)wnd.getFellowIfAny( Stringx.getDefault(getFormatSelector(),"unknown") );
	String fmt = null;
	if( cbFormat != null ) {
	    Comboitem item = cbFormat.getSelectedItem();
	    if( item != null )
		fmt = item.getValue().toString();
	}
	return fmt;
    }

    private File createDownloadFile( File dir, UploadOutput[] outputs, boolean zipped )
	throws IOException {

	File downloadF = null;

	if( outputs.length > 1 )
	    downloadF = File.createTempFile( "dimpsy", ".zip", dir );
	else {
	    String fn = Stringx.getDefault(outputs[0].getFilename(), "" );
	    String ext = null;
	    if( zipped ) {
		fn = Stringx.before( fn, "." );
		ext = ".zip";
	    }
	    else {
		ext = "";
	    }

	    if( fn.length() > 0 )
		downloadF = new File( dir, fn+ext ); 
	    else
		downloadF = File.createTempFile( "dimpsy", ((ext.length()<=0)?".out":ext), dir );
	}
	return downloadF;
    }

    private boolean sendDownload( Window wnd, UploadOutput[] outputs, String format ) {
	if( outputs.length <= 0 ) {
	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: No output detected." );
	    return false;
	}

	// detect temp directory

	File dir = null;
	try {
	    File tempF = File.createTempFile( "excel", ".tmp" );
	    dir = tempF.getParentFile();
	    tempF.delete();
	}
	catch( IOException ioe ) {
	    log.error( ioe );
	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: "+
			 Stringx.getDefault( ioe.getMessage(), "General I/O error occured" ) );
	    return false;
	}

	// prepare zip container (if applicable)

	File downloadF = null;
	ZipOutputStream outs = null;
	String mime = null;
	try {
	    if( format.indexOf( "zipped" ) >= 0 ) {
		mime = MIME_ZIP;
		downloadF = createDownloadFile( dir, outputs, true );
		outs = new ZipOutputStream( new FileOutputStream(downloadF) );
	    }
	    else {
		downloadF = createDownloadFile( dir, outputs, false );
	    }
	}	    
	catch( IOException ioe ) {
	    log.error( ioe );
	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: "+
			 Stringx.getDefault( ioe.getMessage(), "General I/O error occured" ) );
	    return false;
	}

	try {
	    for( int i = 0; i < outputs.length; i++ ) {
		if( outs != null ) {
		    File tempF = outputs[i].writeOutputFile( dir );
		    ZipEntry ze = new ZipEntry( tempF.getName() );
		    outs.putNextEntry( ze );
		    InputStream ins = new FileInputStream( tempF );
		    int len = IOUtils.copy( ins, outs );
		    outs.closeEntry();
		    ins.close();
		    if( len <= 0 )
			log.warn( "File size is 0: "+tempF );
		}
		else
		    outputs[i].writeOutputFile( downloadF );
	    }
	    if( outs != null )
		outs.close();
	}	    
	catch( IOException ioe ) {
	    log.error( ioe );
	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: "+
			 Stringx.getDefault( ioe.getMessage(), "General I/O error occured" ) );
	    return false;
	}

	try {
	    Filedownload.save( downloadF, mime );
	    return true;
	}
	catch( FileNotFoundException fnfe ) {
	    log.error( fnfe );
	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: File "+downloadF+" not found." );
	}
	return false;
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

	String fmt = getSelectedFormat( wnd );
	if( fmt == null ) {
	    String msg = "Output format cannot be determined.";
	    log.error( msg );
	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: "+msg );
	    return;
	}
	UploadOutput[] outputs = getSelectedOutput( wnd, ALL_OUTPUT.equals(fmt) );
	if( sendDownload( wnd, outputs, fmt ) )
	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Download succeded." );

    }

}
