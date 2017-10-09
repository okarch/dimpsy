package com.emd.dimpsy.portlet;

// import java.util.Map;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;

import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.util.WebAppCleanup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.emd.dimpsy.config.InventoryPreferences;

import com.emd.xlsutil.dao.DatasetDAO;

/**
 * InventoryCleanup is used to cleanup the sample browser webapp.
 *
 * Created: Wed Mar 18 14:49:03 2015
 *
 * @author <a href="mailto:">Oliver Karch</a>
 * @version 1.0
 */
public class InventoryCleanup implements WebAppCleanup, ServletContextListener {

    private static Log log = LogFactory.getLog(InventoryCleanup.class);

    public InventoryCleanup() {	
    }

    public void cleanup(WebApp wapp)
	throws Exception {

// 	Map appattrs = wapp.getAttributes(); 
// 	Map map = (Map) appattrs.get(JpaUtil.JPA_EMF_MAP);
// 	if( map != null ) {
// 	    EntityManager em = (EntityManager)map.remove("biactorJPA");
// 	    if( em != null && em.isOpen() ) {
// 		log.debug( "Closing entity manager" );
// 		em.close();
// 	    }
// 	}

// 	BoneCPProviderSession[] bcps = BoneCPProviderSession.getSessions();
// 	log.debug( "Closing "+bcps.length+" connection pool(s)" );
// 	for( int i = 0; i < bcps.length; i++ ) 
// 	    bcps[i].close();
	
	log.debug( "Webapp cleanup done." );

// 	try {
// 	    JpaUtil.closeEntityManager( "biactorJPA" );
// 	}
// 	catch( Exception ex ) {
// 	    log.error( ex );
// 	}
    }

    /**
     * Receives notification that the web application initialization process is starting.
     *
     * @param sce the ServletContextEvent containing the ServletContext that is being initialized.
     */
    public void contextInitialized(ServletContextEvent sce) {
    }

    /**
     * Receives notification that the ServletContext is about to be shut down. 
     *
     * @param sce the ServletContextEvent containing the ServletContext that is being destroyed
     */
    public void contextDestroyed(ServletContextEvent sce) {
	log.debug( "Inventory browser context about to be destroyed." );

	InventoryPreferences[] prefs = InventoryPreferences.getInventorySessions();
	for( int i = 0; i < prefs.length; i++ ) {
	    String pid = prefs[i].getPortletId();
	    long uid = prefs[i].getUserId();
	    log.debug( "Cleaning database access of user "+uid+" portlet id "+pid );
	    DatasetDAO dao = prefs[i].getInventory();
	    dao.close();
	}
    }
    
}
