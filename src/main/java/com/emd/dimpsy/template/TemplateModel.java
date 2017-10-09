package com.emd.dimpsy.template;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

import org.zkoss.zul.Combobox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.emd.dimpsy.auth.UserLogin;
import com.emd.dimpsy.view.DefaultModelProducer;
import com.emd.dimpsy.velocity.ZulTool;

import com.emd.xlsutil.dao.DatasetDAO;

import com.emd.xlsutil.dataset.Roles;
import com.emd.xlsutil.dataset.User;

import com.emd.xlsutil.upload.UploadException;
import com.emd.xlsutil.upload.UploadProcessor;
import com.emd.xlsutil.upload.UploadTemplate;

import com.emd.util.Stringx;

import com.emd.zk.ZKUtil;

/**
 * <code>TemplateModel</code> holds the templates currently available.
 *
 * Created: Sun Jul 12 09:24:09 2015
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class TemplateModel extends DefaultModelProducer implements EventListener {
    private String paramRowId;

    private static Log log = LogFactory.getLog(TemplateModel.class);

    private static final ZulTool zulTool = new ZulTool();

    public static final String COMPONENT_ID = "cbTemplate";
    public static final String RESULT = "result";

            // paramRowId="rowParamUpload"

    public static final String TXT_TEMPLATE      = "txtTemplate";
    public static final String TXT_TEMPLATENAME  = "txtTemplateName";

    public static final String ROW_PARAM         = "rowParamUpload";

    private static final Comparator<UploadTemplate> TEMPLATE_SORTER = new Comparator<UploadTemplate>() {
	public int compare(UploadTemplate o1, UploadTemplate o2) {
	    if( (o1 == null) && (o2 == null) )
		return 0;
	    return o1.getTemplatename().compareTo( o2.getTemplatename() );
	}
	public boolean equals(Object obj) {
	    return false;
	}
    };

    public TemplateModel() {
	super();
	setModelName( COMPONENT_ID );
    }

    /**
     * Initializes the model producer.
     *
     * @param wnd the application window.
     * @param context the execution context.
     */
    public void initModel( Window wnd, Map context ) {
	DatasetDAO dao = getInventory();
	if( dao == null ) {
	    writeMessage( wnd, "Error: No database access configured" );
	    return;
	}

	try {
	    UploadTemplate[] tList = dao.findTemplateByName( "" );

	    Combobox cbTempl = (Combobox)wnd.getFellowIfAny( getModelName() );
	    if( cbTempl != null ) {
		if( context == null )
		    context = new HashMap();
		context.put( RESULT, tList );
		assignModel( cbTempl, context );
	    } 
	}
	catch( SQLException sqe ) {
	    writeMessage( wnd, "Error: Cannot query database: "+Stringx.getDefault(sqe.getMessage(),"reason unknown") );
	    log.error( sqe );
	}
    }

    /**
     * Returns the selected upload template.
     *
     * @param wnd the app window.
     * @return the upload template currently selected (or null).
     */ 
    public UploadTemplate getSelectedTemplate( Window wnd ) {
	Combobox cbTempl = (Combobox)wnd.getFellowIfAny( getModelName() );
	UploadTemplate templ = null;
	if( cbTempl != null ) {
	    int sel = cbTempl.getSelectedIndex();
	    if( sel >= 0 )
		templ = (UploadTemplate)cbTempl.getModel().getElementAt( sel );
	}
	return templ;
    }

    /**
     * Get the <code>ParamRowId</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getParamRowId() {
	return Stringx.getDefault(paramRowId,ROW_PARAM);
    }

    /**
     * Set the <code>ParamRowId</code> value.
     *
     * @param paramRowId The new ParamRowId value.
     */
    public final void setParamRowId(final String paramRowId) {
	this.paramRowId = paramRowId;
    }

    private void pushAfterRenderIndex( int idx ) {
	Session ses = Sessions.getCurrent();
	if( ses != null )
	    ses.setAttribute( getModelName()+".afterRenderIndex", new Integer(idx) );
    }
    private int popAfterRenderIndex( int def ) {
	Session ses = Sessions.getCurrent();
	int idx = def;
	if( ses != null ) {
	    Integer ari = (Integer)ses.getAttribute( getModelName()+".afterRenderIndex" );
	    if( ari != null ) {
		idx = ari.intValue();
		ses.removeAttribute( getModelName()+".afterRenderIndex" );
	    }
	}
	return idx;
    }

    /**
     * Reloads the list of templates from the database and selects the given
     * template.
     *
     * @param wnd the app window.
     * @param selTemplate the upload template currently selected (or null).
     */ 
    public void reloadTemplates( Window wnd, UploadTemplate selTemplate ) {
	DatasetDAO dao = getInventory();
	if( dao == null ) {
	    writeMessage( wnd, "Error: No database access configured" );
	    return;
	}

	Combobox cbTempl = (Combobox)wnd.getFellowIfAny( getModelName() );
	try {
	    UploadTemplate[] tList = dao.findTemplateByName( "" );

	    if( cbTempl != null ) {
		Map context = new HashMap();
		context.put( RESULT, tList );
		assignModel( cbTempl, context );
	    } 
	}
	catch( SQLException sqe ) {
	    writeMessage( wnd, "Error: Cannot query database: "+Stringx.getDefault(sqe.getMessage(),"reason unknown") );
	    log.error( sqe );
	}

	ListModel tModel = null;
	if( (selTemplate != null) &&
	    (cbTempl != null) && 
	    ((tModel = cbTempl.getModel()) != null) ) {

	    log.debug( "Find index of template \""+selTemplate.getTemplatename()+"\"" );
	    int idx = -1;
	    UploadTemplate templ = null;
	    for( int i = 0; i < tModel.getSize(); i++ ) {
		templ = (UploadTemplate)tModel.getElementAt( i );
		if( templ.getTemplatename().equals( selTemplate.getTemplatename() ) ) {
		    idx = i;
		    break;
		}
	    }
	    if( idx >= 0 ) {
		log.debug( "Template index of \""+templ.getTemplatename()+"\": "+idx );
		pushAfterRenderIndex( idx );
		// cbTempl.setSelectedIndex( idx );
		// updateTemplateText( wnd, templ );
	    }		
	    else {
		log.error( "Template index "+idx+" could not be found for template \""+
			   selTemplate.getTemplatename()+"\"" ); 
	    }
	}
    }

    private String toEditText( String templ ) {
	return Stringx.getDefault( templ, "## Empty template" ).trim().replace( "\\n", "\n" );
    }

    private String updateTemplateText( Window wnd, UploadTemplate templ ) {
	Textbox txtTempl = (Textbox)wnd.getFellowIfAny( TXT_TEMPLATE );
	String tTxt = toEditText(templ.getTemplate());
	if( txtTempl != null )
	    txtTempl.setValue( tTxt );
	txtTempl = (Textbox)wnd.getFellowIfAny( TXT_TEMPLATENAME );
	if( txtTempl != null ) {
	    txtTempl.setValue( templ.getTemplatename() );
	}
	return tTxt;
    }

    private UploadTemplate loadParameterTemplate( Window wnd, String templTxt ) {
	int k = -1;
	int j = -1;
	if( ((k = templTxt.indexOf( "!params:" )) >= 0 ) &&
	    ((j = templTxt.indexOf( "\n", k+8 )) > 0 ) ) {
	    String pName = templTxt.substring( k+8, j ).trim();
	    if( pName.length() <= 0 ) {
		writeMessage( wnd, "Warning: Parameter template name is empty." );
		log.warn( "Parameter template name is empty." );
		return null;
	    }
	    
	    log.debug( "Load parameter template: "+pName );
	    DatasetDAO dao = getInventory();
	    try {
		UploadTemplate[] tList = dao.findTemplateByName( pName );
		if( tList.length <= 0 ) {
		    writeMessage( wnd, "Warning: Cannot find parameter template: \""+pName+"\"" );
		    log.warn( "Cannot find parameter template: \""+pName+"\"" );
		    return null;
		}
		return tList[0];
	    }
	    catch( SQLException sqe ) {
		writeMessage( wnd, "Error: Cannot query database: "+Stringx.getDefault(sqe.getMessage(),"reason unknown") );
		log.error( sqe );
	    }
	}
	return null;
    }

    private String extractRenderMarkup( String templTxt ) {
	int k = -1;
	int j = -1;
	String rText = null;
	if( ((k = templTxt.indexOf( "!renderstart" )) >= 0 ) &&
	    ((j = templTxt.indexOf( "!renderend", k+12 )) > 0 ) ) {
	    rText = templTxt.substring( k+12, j );
	    if( (j = rText.lastIndexOf( "##" )) > 0 )
		rText = rText.substring( 0, j );
	}
	if( rText != null ) {
	    StringBuilder stb = new StringBuilder( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" );
	    stb.append( rText );
	    return stb.toString().trim();
	}
	return null;
    }
    
    private void clearParameters( Window wnd ) {
	Component cmp = wnd.getFellowIfAny( getParamRowId() );
	if( cmp != null ) {
	    Component cc = null;
	    while( (cc = cmp.getLastChild()) != null ) {
		cc.detach();
	    }
	}
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

    private User validateUser( Window wnd, long reqRole ) {
	User user = null;
	try {
	    DatasetDAO dao = getInventory();
	    user = dao.findUserById( getLoginUserId() );
	}
	catch( SQLException sqe ) {
	    writeMessage( wnd, "Error: Cannot query database: "+Stringx.getDefault( sqe.getMessage(), "General database error" ) );
	    log.error( sqe );
	    return null;
	}

	if( user == null ) {
	    writeMessage( wnd, "Error: User id "+getLoginUserId()+" is unknown" );
	    return null;
	}

	if( !user.hasRole( reqRole ) ) {
	    writeMessage( wnd, "Error: User "+user+" requires permission \""+Roles.roleToString( reqRole )+"\"" );
	    return null;
	}
	return user;
    }

    private String renderParameterTemplate( Window wnd, UploadTemplate templ, String templTxt ) {
	String renderMarkup = extractRenderMarkup( templTxt );
	Component cmp = wnd.getFellowIfAny( getParamRowId() );
	boolean hasParameters = false;
	if( cmp != null ) {
	    // Component cc = null;
	    // while( (cc = cmp.getLastChild()) != null ) {
	    // 	cc.detach();
	    // }
	    if( (renderMarkup != null) &&
		(renderMarkup.length() > 0) ) {

		// log.debug( "Render markup: "+renderMarkup );

// 	    List childCmps = (List)cmp.getChildren().clone();

		Executions.createComponentsDirectly( renderMarkup, null, cmp, new HashMap() );
	    }
	}
	return renderMarkup;
    }

    private void runParameterTemplate( Window wnd, UploadTemplate templ, User user ) {
	UploadProcessor uProcessor = UploadProcessor.getInstance();

     	Map ctxt = new HashMap();
     	ctxt.put( "user", user );
	ctxt.put( "window", wnd );
	ctxt.put( "zul", zulTool );

	try {
	    uProcessor.processTemplate( templ, ctxt );
	}
	catch( UploadException uex ) {
	    writeMessage( wnd, "Error: "+Stringx.getDefault( uex.getMessage(), "General upload error" ) );
	    log.error( uex );
	}
    }

    private UploadTemplate[] sortTemplates( UploadTemplate[] templs ) {
	Arrays.sort( templs, TEMPLATE_SORTER );
	return templs;
    }

    private void updateOutput( Window wnd, UploadTemplate templ ) {
	OpenOutput oOutput = (OpenOutput)getPreferences().getCommand( OpenOutput.class );
	if( oOutput == null ) {
	    log.error( "Cannot find open output section" );
	    return;
	}

	try {
	    oOutput.initLogs( wnd, templ );
	}
	catch( SQLException sqe ) {
	    log.error( sqe );
	}
    }

    protected void assignCombobox( Combobox combobox, Map context ) {
	log.debug( "Template result model context: "+context );

	if( !combobox.isListenerAvailable( "onAfterRender", false ) )
	    combobox.addEventListener( "onAfterRender", this );
	if( !combobox.isListenerAvailable( Events.ON_SELECT, false ) )
	    combobox.addEventListener( Events.ON_SELECT, this );

	UploadTemplate[] tList = (UploadTemplate[])context.get( RESULT );
	if( tList == null )	    
	    combobox.setModel( new ListModelArray( new UploadTemplate[0] ) );
	else {
	    log.debug( "Assigning model, number of templates: "+tList.length );
	    combobox.setModel( new ListModelArray( sortTemplates(tList) ) );
	}
    }

    /**
     * Notifies this listener that an event occurs.
     */
    public void onEvent(Event event)
	throws java.lang.Exception {

	log.debug( "Template model selected: "+event );

	UploadTemplate templ = null;

	Combobox cb = (Combobox)event.getTarget();

	if( "onAfterRender".equals( event.getName() ) ) {
	    int idx = popAfterRenderIndex( 0 );
	    if( idx < cb.getItemCount() ) {
		cb.setSelectedIndex( idx );
		templ = (UploadTemplate)cb.getModel().getElementAt( idx );
	    }	    
	}	
	else if( Events.ON_SELECT.equals( event.getName() ) ) {
	    if( cb.getItemCount() > 0 ) {
		int idx = cb.getSelectedIndex();
		if( idx >= 0 )
		    templ = (UploadTemplate)cb.getModel().getElementAt( idx );
	    }
	}

	if( templ != null ) {
	    Window wnd = ZKUtil.findWindow( cb );
	    String templTxt = updateTemplateText( wnd, templ );
	    clearParameters( wnd );
	    UploadTemplate pTempl = loadParameterTemplate( wnd, templTxt );
	    if( pTempl != null ) {
		String tTxt = toEditText(pTempl.getTemplate());
		String rMarkup = renderParameterTemplate( wnd, pTempl, tTxt );
		if( rMarkup != null ) {
		    User user = validateUser( wnd, Roles.INVENTORY_UPLOAD );
		    if( user == null ) {
			String msg = "Error: Unable to authenticate user for upload: "+this.getLoginUserId();
			log.error( msg );
		    }
		    else {
			runParameterTemplate( wnd, pTempl, user );
		    }
		}
	    }
	    updateOutput( wnd, templ );
	}
    }
}
