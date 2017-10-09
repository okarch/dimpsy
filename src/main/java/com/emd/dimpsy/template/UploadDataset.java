package com.emd.dimpsy.template;

import java.sql.Timestamp;
import java.sql.SQLException;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.UploadEvent;

import org.zkoss.util.media.Media;

// import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Window;

import com.emd.dimpsy.auth.UserLogin;
import com.emd.dimpsy.command.InventoryCommand;
import com.emd.dimpsy.config.InventoryPreferences;
import com.emd.dimpsy.view.ModelProducer;
import com.emd.dimpsy.view.UIUtils;

import com.emd.xlsutil.dao.DatasetDAO;
import com.emd.xlsutil.dataset.Roles;
import com.emd.xlsutil.dataset.User;

import com.emd.xlsutil.upload.UploadTemplate;
import com.emd.xlsutil.upload.UploadBatch;
import com.emd.xlsutil.upload.UploadException;
import com.emd.xlsutil.upload.UploadProcessor;

// import com.emd.util.Parameter;
import com.emd.util.Stringx;

import com.emd.zk.ZKContext;
import com.emd.zk.command.CommandException;

/**
 * The <code>UploadDataset</code> action uploads a file to the dataset inventory.
 *
 * Created: Thu Jan 29 08:43:39 2017
 *
 * @author <a href="mailto:">Oliver Karch</a>
 * @version 1.0
 */
public class UploadDataset extends InventoryCommand {

    private static Log log = LogFactory.getLog(UploadDataset.class);

    /**
     * Creates a new command to upload samples from a file.
     */
    public UploadDataset() {
	super();
    }

    private InventoryPreferences getPreferences() {
	return InventoryPreferences.getInstance( getPortletId(), getUserId() );
    }

    // private File createUploadFile( String name ) 
    // 	throws IOException {

    // 	File tempF = File.createTempFile( "simbiom", null );
    // 	File dir = new File( tempF.getParentFile(), "simbiom" );
    // 	if( !dir.exists() && !dir.mkdir() ) {
    // 	    log.error( "Cannot create upload directory: "+dir );
    // 	    return null;
    // 	}
    // 	dir = new File( dir, String.valueOf(getUserId()) );
    // 	if( !dir.exists() && !dir.mkdir() ) {
    // 	    log.error( "Cannot create upload directory: "+dir );
    // 	    return null;
    // 	}
    // 	return new File( dir, Stringx.getDefault(name, "NONAME.temp" ) );
    // }

    private UploadBatch storeUpload( Event event, 
				     User usr, 
				     UploadTemplate templ ) {

	Window wnd = UIUtils.getWindow( event );

	UploadBatch uBatch = null;
	if( event instanceof UploadEvent ) {
	    Media media = ((UploadEvent)event).getMedia();
	    String fName = Stringx.getDefault(media.getName(), "" );
	    String fType = Stringx.getDefault(media.getContentType(), "" );

	    try {
		DatasetDAO dao = getInventory();
		uBatch = dao.createUploadBatch( usr.getUserid(), templ );
	    }
	    catch( SQLException sqe ) {
		showMessage( wnd, "rowMessageUpload", "lbMessageUpload", 
			     "Error: Registering batch - "+
			     Stringx.getDefault( sqe.getMessage(), "database access invalid" ) );
		log.error( sqe );
		return null;
	    }

	    log.debug( "Upload file name: "+fName+" format: "+fType+
		       " ("+Stringx.getDefault(media.getFormat(),"unknown")+
		       ") binary: "+media.isBinary()+" in memory: "+media.inMemory() );

	    uBatch.setFilename( fName );
	    uBatch.setMime( fType );

	    try {
	 	InputStream ins = media.getStreamData(); 	    
	 	uBatch.readUploadContent( ins );
	 	ins.close();
	 	log.debug( "Content uploaded. Checksum: "+uBatch.getMd5sum() );
	    } 
	    catch( IOException e ) {
		showMessage( wnd, "rowMessageUpload", "lbMessageUpload", 
			     "Error: Cannot store uploaded content - "+
			     Stringx.getDefault( e.getMessage(), "database access invalid" ) );
     	 	log.error( e );
		return null;
	    }
	}
	else 
	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: Upload content is missing" );
	return uBatch;
    }

    private UploadTemplate getTemplate( Window wnd ) {
	ModelProducer[] mp = getPreferences().getResult( TemplateModel.class );
	if( mp.length <= 0 )
	    return null;
	return ((TemplateModel)mp[0]).getSelectedTemplate( wnd );
    }

    private long getLoginUserId() {
	Session ses = Sessions.getCurrent();
	if( ses != null ) {
	    User usr = (User)ses.getAttribute( UserLogin.USER_KEY );
	    if( usr != null )
		return usr.getUserid();
	}
	return this.getUserId();
    }

    // private UploadBatch createUploadBatch( UploadTemplate templ, String updContent ) {
    //  	UploadBatch uBatch = new UploadBatch();
    //  	uBatch.setTemplateid( templ.getTemplateid() );
    // 	if( updContent != null )
    // 	    uBatch.setUpload( updContent );
    // 	uBatch.setUploaded( new Timestamp(System.currentTimeMillis()) );
    // 	uBatch.setUserid( getUserId() );
    // 	return uBatch;
    // }

    private User validateUser( Window wnd, long reqRole ) {
	User user = null;
	try {
	    DatasetDAO dao = getInventory();
	    user = dao.findUserById( getLoginUserId() );
	}
	catch( SQLException sqe ) {
	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: "+
			 Stringx.getDefault( sqe.getMessage(), "General database error" ) );
	    log.error( sqe );
	    return null;
	}

	if( user == null ) {
	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: User id "+
			 getLoginUserId()+" is unknown" );
	    return null;
	}

	if( !user.hasRole( reqRole ) ) {
	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: User "+
			 user+" requires permission \""+Roles.roleToString( reqRole )+"\"" );
	    return null;
	}
	return user;
    }

    private void runUpload( Window wnd,
			    User user, 
			    UploadTemplate templ, 
			    long batchId )
	throws UploadException {

     	UploadProcessor uProcessor = UploadProcessor.getInstance();
	Map ctxt = new HashMap();
	ctxt.put( "user", user );
	uProcessor.processUpload( templ, batchId, ctxt );
    }	

    private void updateLog( Window wnd, UploadTemplate templ, long uploadid ) {
	OpenResultLog orl = (OpenResultLog)getPreferences().getCommand( OpenResultLog.class );
	if( orl != null ) {
	    try {
		orl.initLogs( wnd, templ );
	    }
	    catch( SQLException sqe ) {
		showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: "+
			     Stringx.getDefault( sqe.getMessage(), "General database error" ) );
		log.error( sqe );
	    }
	}
	SelectResultLog selLog = (SelectResultLog)getPreferences().getCommand( SelectResultLog.class );
	if( (selLog != null) && (selLog.selectUploadid( wnd, uploadid ) != null) ) {
	    try {
		selLog.execute( ZKContext.getInstance(), wnd );
	    }
	    catch( CommandException cex ) {
		log.error( cex );
	    }
	}
    }

    /**
     * Notifies this listener that an event occurs.
     *
     * @param event the event which occurred
     */
    public void onEvent( Event event )	throws Exception {
	Window wnd = UIUtils.getWindow( event );

	// verify user access

	User user = validateUser( wnd, Roles.INVENTORY_UPLOAD );
	if( user == null ) {
	    String msg = "Error: Unable to authenticate user for upload: "+this.getLoginUserId();
	    log.error( msg );
	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", msg );
	    return;
	}

	// determine template 
	
	UploadTemplate templ = getTemplate( wnd );
	if( templ == null ) {
	    String msg = "Error: Cannot determine template";
	    log.error( msg );
	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", msg );
	    return;
	}
	log.info( "Start upload using template: "+templ.getTemplatename() );

	// create batch and store upload

	UploadBatch uBatch = storeUpload( event, user, templ );
	if( uBatch == null ) 
	    return;

	showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Upload received: "+uBatch.getFilename()+
		     " type: "+uBatch.getMime() );

	// registering batch with the template

	templ.addUploadBatch( uBatch );
	log.info( "Upload batch registered: "+uBatch.getUploadid()+" md5: "+uBatch.getMd5sum() );

	try {
	    DatasetDAO dao = getInventory();
	    templ = dao.storeTemplate( user.getUserid(), templ );
	}
	catch( SQLException sqe ) {
	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: "+
			 Stringx.getDefault( sqe.getMessage(), "General database error" ) );
	    log.error( sqe );
	    return;
	}

	showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Batch registered, start processing "+uBatch.getFilename()+"..." );
	try {
	    runUpload( wnd, user, templ, uBatch.getUploadid() );
	}
	catch( UploadException uex ) {
	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: "+
			 Stringx.getDefault( uex.getMessage(), "General upload error" ) );
	    log.error( uex );
	    return;
	}

	updateLog( wnd, templ, uBatch.getUploadid() );
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
    }        
} 
