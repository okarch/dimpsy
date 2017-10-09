package com.emd.dimpsy.portlet;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zk.ui.metainfo.Property;

import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Window;

import com.emd.zk.ZKContext;
import com.emd.zk.CookieUtil;

import com.emd.dimpsy.auth.UserLogin;
import com.emd.dimpsy.command.InventoryCommand;
import com.emd.dimpsy.config.InventoryPreferences;

import com.emd.xlsutil.dao.DatasetDAO;
import com.emd.xlsutil.dataset.Roles;
import com.emd.xlsutil.dataset.User;
import com.emd.xlsutil.util.DataHasher;

import com.emd.util.ClassUtils;
import com.emd.util.Stringx;

/**
 * InventoryPortletController implements the controller of the dataset inventory portlet
 *
 * Created: Mon Jan 23 08:32:24 2017
 *
 * @author <a href="mailto:">Oliver Karch</a>
 * @version 1.0
 */
public class InventoryPortletController extends GenericForwardComposer {
    private ZKContext zkContext;

    Window wndBrowser;

    private static Log log = LogFactory.getLog(InventoryPortletController.class);

    private long mapUser( long zkUserId ) {
	Session session = Sessions.getCurrent();
	long userId = zkUserId;
	if( session != null ) {
	    Object nativeSession = session.getNativeSession();
	    if( nativeSession != null ) {
		log.debug( "Native session: "+nativeSession+" class: "+nativeSession.getClass() );
		Object sessionId = ClassUtils.get( nativeSession, "Id", nativeSession );
		userId = DataHasher.hash( sessionId.toString().getBytes() );
		if( userId > 0 )
		    userId = (-1L) * userId;
		log.debug( "Session id: "+userId );
	    }
	}
	return userId;
    }

    private void initInventory( String portletId, long userId ) {
	log.debug( "Initializing inventory view" );
	InventoryPreferences dp = InventoryPreferences.getInstance( portletId, userId );
	dp.initViews( wndBrowser, zkContext );
	log.debug( "Inventory view initialized" );
    }

    private void initButtonCommands( String portletId, long userId ) {
	InventoryPreferences dp = InventoryPreferences.getInstance( portletId, userId );	
	InventoryCommand[] cmds = dp.getCommands();
	for( int i = 0; i < cmds.length; i++ ) {
	    Component cmp = wndBrowser.getFellowIfAny( cmds[i].getCommandName() );
	    if( cmp != null ) {
		log.debug( "Wiring component "+cmp.getId()+" to action "+cmds[i].getClass().getName() );
		cmp.addEventListener( cmds[i].getEvent(), cmds[i] );
	    }
	}
	log.debug( "Inventory actions wired" );
    }

    private void initUserSession( String portletId, long userId ) {
	Session ses = Sessions.getCurrent();
	if( ses != null ) {
	    DatasetDAO dao = InventoryPreferences.getInstance( portletId, userId ).getInventory();
	    try {
		User usr = dao.findUserById( userId );
		if( usr != null ) {
		    ses.setAttribute( UserLogin.USER_KEY, usr );
		    boolean disable = !usr.hasRole( Roles.INVENTORY_UPLOAD );
		    Tab tab = (Tab)wndBrowser.getFellowIfAny( UserLogin.TAB_UPLOAD );
		    if( tab != null ) 
			tab.setDisabled( disable );
		    Label lb = (Label)wndBrowser.getFellowIfAny( UserLogin.LABEL_USER );
		    if( lb != null )
			lb.setValue( usr.getMuid()+" - "+usr.getUsername() );
		}
	    }
	    catch( SQLException sqe ) {
		log.error( sqe );
	    }
	}
    }

    private long getUserId() {
	String uid = CookieUtil.getCookieValue( UserLogin.USER_COOKIE );
	long userId = zkContext.getUserId();
	if( uid != null ) {
	    userId = Stringx.toLong(Stringx.after(uid,":"),userId);
	    log.info( "Remembering user id "+uid );
	}
	return userId;
    }
	
    public void doAfterCompose(Component comp) throws Exception {
	super.doAfterCompose(comp);
	
	if( zkContext == null )
	    zkContext = ZKContext.getInstance();

	String portletId = Stringx.getDefault(zkContext.getPortletId(),"n/a");
	log.debug( "Portlet id: "+portletId );

	// long userId = mapUser( zkContext.getUserId() );
	long userId = getUserId();
	log.debug( "User id: "+userId );
	
	initButtonCommands( portletId, userId );

	// initSetup( portletId, userId );

	initInventory( portletId, userId );

	initUserSession( portletId, userId );
    }

}
