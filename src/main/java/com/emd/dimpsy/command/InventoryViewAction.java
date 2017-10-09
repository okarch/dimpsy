package com.emd.dimpsy.command;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;

import org.zkoss.zul.Window;

import com.emd.dimpsy.config.InventoryPreferences;
import com.emd.dimpsy.view.UIUtils;

import com.emd.xlsutil.dao.DatasetDAO;

import com.emd.zk.ZKContext;
import com.emd.zk.view.ViewAction;

/**
 * <code>InventoryViewAction</code> is a no-op implementation of sample browser view actions
 * which can be used to extend it.
 *
 * Created: Thu Nov 22 08:28:07 2012
 *
 * @author <a href="mailto:">Oliver</a>
 * @version 1.0
 */
public class InventoryViewAction extends ViewAction {
    private String               portletId;
    private long                 userId;
    // private List<ActionProcessor> processors;

    private static Log log = LogFactory.getLog(InventoryViewAction.class);

    /**
     * Creates a no-op button action
     */
    public InventoryViewAction() {
	super();
	// processors = new ArrayList<ActionProcessor>();
    }

    protected InventoryViewAction( InventoryViewAction va, String suffix ) {
	super();
	this.portletId = va.getPortletId();
	this.userId = va.getUserId();
	this.setEvent( va.getEvent() );
	String cmpId = va.getComponent();
	int k = -1;
	if( (cmpId != null) && ((k = cmpId.lastIndexOf( "_" )) > 0) ) 
	    cmpId = cmpId.substring( 0, k )+"_"+suffix;
	this.setComponent( cmpId );
    }
	// Properties props = va.getParameters();
	// for( String pn : props.stringPropertyNames() ) {
	//     String pv = props.getProperty( pn );
	//     if( pv != null )		

    /**
     * Get the PortletId value.
     * @return the PortletId value.
     */
    public String getPortletId() {
	return portletId;
    }

    /**
     * Set the PortletId value.
     * @param newPortletId The new PortletId value.
     */
    public void setPortletId(String newPortletId) {
	this.portletId = newPortletId;
    }

    /**
     * Get the UserId value.
     * @return the UserId value.
     */
    public long getUserId() {
	return userId;
    }

    /**
     * Set the UserId value.
     * @param newUserId The new UserId value.
     */
    public void setUserId(long newUserId) {
	this.userId = newUserId;
    }

    /**
     * Convenience method to return the dossier database.
     * @return dossier preferences
     */
    public DatasetDAO getInventory() {
	return InventoryPreferences.getInstance( portletId, userId ).getInventory();	
    }

    /**
     * Registers this event handler if the component
     * is available
     *
     * @param wnd the window where to look for the component
     * @param params the parameters passed by the view
     */
    // public void unregisterEvent( Window wnd ) {
    //  	String cmpId = getComponent();
    //  	String evt = getEvent();
    //  	Component cmp = null;
    // 	if( (cmpId != null) && ((cmp = wnd.getFellowIfAny(cmpId)) != null) ) 
    // 	    cmp.removeEventListener( getEvent(), this );
    // }

    /**
     * Copies the action and adapts the suffixes of components accordingly.
     *
     * @param suffix the new suffix
     * @return a new view action.
     */
    public InventoryViewAction copyAction( String suffix ) {
	return new InventoryViewAction( this, suffix );
    }

    /**
     * Get the Processor value.
     * @return the Processor value.
     */
    // public ActionProcessor[] getProcessors() {
    // 	for( ActionProcessor proc : processors ) {
    // 	    if( proc instanceof DefaultActionProcessor ) {
    // 		((DefaultActionProcessor)proc).setPortletId( getPortletId() );
    // 		((DefaultActionProcessor)proc).setUserId( getUserId() );
    // 	    }
    // 	}
    // 	ActionProcessor[] procs = new ActionProcessor[ processors.size() ];
    // 	return (ActionProcessor[])processors.toArray( procs );
    // }

    /**
     * Set the Processor value.
     * @param newProcessor The new Processor value.
     */
    // public ActionProcessor addProcessor(ActionProcessor newProcessor) {
    // 	processors.add( newProcessor );
    // 	return newProcessor;
    // }

    /**
     * Set the Processor value.
     * @param newProcessor The new Processor value.
     */
    // public void setProcessor(ActionProcessor newProcessor) {
    // 	addProcessor( newProcessor );
    // }

    // protected void applyProcessors( Event event ) {
    // 	ActionProcessor[] procs = getProcessors();
    // 	for( int i = 0; i < procs.length; i++ ) 
    // 	    procs[i].processAction( event );
    // }
    
    protected Window getWindow( Event evt ) {
	Component cmp = evt.getTarget();
	Window wnd = null;
	if( cmp != null )
	    wnd = ZKContext.findWindow( cmp );
	return wnd;
    }

    protected void showMessage( Window wnd, 
				String parentId,
				String labelId, 
				String msg ) {
	UIUtils.showMessage( wnd, parentId, labelId, msg );
    }

    protected void clearMessage( Window wnd, String labelId ) {
	UIUtils.clearMessage( wnd, labelId );
    }

    // protected void updateList( Window wnd, String cmpId, ListRow row ) {
    // 	Grid gr = (Grid)wnd.getFellowIfAny( cmpId );
    // 	ListModelList model = null;
    // 	if( (gr != null) && ((model = (ListModelList)gr.getModel()) != null) ) 
    // 	    model.add( row );
    // }

    // protected void deleteList( Window wnd, String cmpId, ListRow row ) {
    // 	Grid gr = (Grid)wnd.getFellowIfAny( cmpId );
    // 	ListModelList model = null;
    // 	if( (gr != null) && ((model = (ListModelList)gr.getModel()) != null) ) 
    // 	    model.remove( row );
    // }

}
