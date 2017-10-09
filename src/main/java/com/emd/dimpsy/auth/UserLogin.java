package com.emd.dimpsy.auth;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;

import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.emd.dimpsy.command.InventoryCommand;
import com.emd.dimpsy.config.InventoryPreferences;
import com.emd.dimpsy.view.ModelProducer;

import com.emd.xlsutil.dao.DatasetDAO;
import com.emd.xlsutil.dataset.Roles;
import com.emd.xlsutil.dataset.User;

import com.emd.util.Stringx;

import com.emd.zk.ZKContext;
import com.emd.zk.CookieUtil;
import com.emd.zk.command.CommandException;

/**
 * UserLogin evaluates username and password.
 *
 * Created: Sun Feb 12 12:43:39 2017
 *
 * @author <a href="mailto:">Oliver Karch</a>
 * @version 1.0
 */
public class UserLogin extends InventoryCommand {

    private static Log log = LogFactory.getLog(UserLogin.class);

    private static final int COOKIE_AGE = 160 * 24 * 60 * 60;  // 160 days

    private static final String CHK_REMEMBER = "chkLoginRember";

    private static final String TXT_USER     = "txtLoginUser";
    private static final String TXT_PASSWORD = "txtLoginPassword";
    private static final String GUEST        = "guest";

    public static final String USER_KEY    = "dimpsy.login.user";
    public static final String USER_COOKIE = "dimpsyMUID";
    public static final String TAB_UPLOAD   = "tbUpload";
    public static final String LABEL_USER   = "lbCurrentUser";


    /**
     * Creates a new command to login user.
     */
    public UserLogin() {
	super();
    }

    // private InventoryPreferences getPreferences() {
    // 	return InventoryPreferences.getInstance( getPortletId(), getUserId() );
    // }

    private String getUsername( Window wnd ) {
	Textbox tb = (Textbox)wnd.getFellowIfAny( TXT_USER );
	String term = null;
	if( tb != null ) 
	    term = Stringx.getDefault(tb.getValue(),GUEST).trim();
	else
	    term = GUEST;
	return term;
    }

    private String getPassword( Window wnd ) {
	Textbox tb = (Textbox)wnd.getFellowIfAny( TXT_PASSWORD );
	String term = null;
	if( tb != null ) 
	    term = Stringx.getDefault(tb.getValue(),"").trim();
	else
	    term = "";
	return term;
    }

    private User validateUser( Window wnd, String usrName, String pwd ) {
	User usr = null;
	DatasetDAO dao = getInventory();
	try {
	    usr = dao.findUserByMuid( usrName );
	}
	catch( SQLException sqe ) {
	    showMessage( wnd, "rowMessageLogin", "lbMessageLogin", "Error: "+
			 Stringx.getDefault( sqe.getMessage(), "General database error" ) );
	    log.error( sqe );
	    return null;
	}

	boolean auth = false;
	if( (usr != null) && (pwd.equals(usr.getApikey())) ) 
	    auth = true;

	if( !auth ) {
	    showMessage( wnd, "rowMessageLogin", "lbMessageLogin", "Warning: User "+usrName+
			 " could not be authenticated. Guest account will be used" );
	    try {
		usr = dao.findUserByMuid( GUEST );
	    }
	    catch( SQLException sqe ) {
		showMessage( wnd, "rowMessageLogin", "lbMessageLogin", "Error: "+
			     Stringx.getDefault( sqe.getMessage(), "General database error" ) );
		log.error( sqe );
		return null;
	    }
	}
	return usr;
    }

    private void setEnableUpload( Window wnd, boolean enable ) {
	Tab tab = (Tab)wnd.getFellowIfAny( TAB_UPLOAD );
	if( tab != null ) 
	    tab.setDisabled( !enable );
    }

    private void storeUser( Window wnd, User usr ) {
	Session ses = Sessions.getCurrent();
	if( ses != null ) {
	    ses.setAttribute( USER_KEY, usr );
	    Checkbox cb = (Checkbox)wnd.getFellowIfAny( CHK_REMEMBER );
	    if( (cb != null) && (cb.isChecked()) ) {
		String cValue = usr.getMuid()+":"+String.valueOf(usr.getUserid());
		Cookie cookie = CookieUtil.addCookie( USER_COOKIE, cValue, COOKIE_AGE );
		log.debug( "Cookie "+USER_COOKIE+" has been set to "+cValue );
	    }
	}
	Label lb = (Label)wnd.getFellowIfAny( LABEL_USER );
	if( lb != null )
	    lb.setValue( usr.getMuid()+" - "+usr.getUsername() );
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

	log.debug( "Login user" );

	String userName = getUsername( wnd );
	String password = getPassword( wnd );

	User usr = validateUser( wnd, userName, password );
	if( usr == null )
	    return;

	boolean uploadEnabled = usr.hasRole( Roles.INVENTORY_UPLOAD );
	if( !uploadEnabled ) 
	    showMessage( wnd, "rowMessageLogin", "lbMessageLogin", "Warning: To enable data upload user "+
			 usr+" requires permission \""+Roles.roleToString( Roles.INVENTORY_UPLOAD )+"\"" );
	else 
	    showMessage( wnd, "rowMessageLogin", "lbMessageLogin", "User "+usr+" logged in sucessfully" );

	storeUser( wnd, usr );
	setEnableUpload( wnd, uploadEnabled );	
    }    
    
}
