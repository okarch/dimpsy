package com.emd.dimpsy.view;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.Component;

import org.zkoss.zul.Combobox;
import org.zkoss.zul.Column;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Window;

import com.emd.dimpsy.config.InventoryPreferences;

import com.emd.util.BidirectionalComparator;
import com.emd.util.Stringx;

import com.emd.xlsutil.dao.DatasetDAO;

import com.emd.zk.ZKContext;

/**
 * DefaultModelProducer is a <code>ModelProducer</code> which provides helper functionality to specialized classes.
 *
 * Created: Sat Mar 28 09:10:02 2015
 *
 * @author <a href="mailto:">Oliver</a>
 * @version 1.0
 */
public class DefaultModelProducer implements ModelProducer {
    private String               modelName;
    private String               portletId;
    private String messageRowId;

    private long                 userId;

    private Object               result;

    private static Log log = LogFactory.getLog(DefaultModelProducer.class);

    /**
     * Creates a new default model producer which does nothing.
     */
    public DefaultModelProducer() {
	this.modelName = "noOp";
    }

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
     * Convenience method to return the preferences.
     * @return inventory preferences
     */
    public InventoryPreferences getPreferences() {
	return InventoryPreferences.getInstance( portletId, userId );
    }

    /**
     * Get the <code>Result</code> value.
     *
     * @return an <code>Object</code> value
     */
    public final Object getResult() {
	return result;
    }

    /**
     * Set the <code>Result</code> value.
     *
     * @param result The new Result value.
     */
    public final void setResult(final Object result) {
	this.result = result;
    }

    /**
     * Get the <code>ModelName</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getModelName() {
	return modelName;
    }

    /**
     * Set the <code>ModelName</code> value.
     *
     * @param modelName The new ModelName value.
     */
    public final void setModelName(final String modelName) {
	this.modelName = modelName;
    }

    /**
     * Get the <code>MessageRowId</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getMessageRowId() {
	return messageRowId;
    }

    /**
     * Set the <code>MessageRowId</code> value.
     *
     * @param messageRowId The new MessageRowId value.
     */
    public final void setMessageRowId(final String messageRowId) {
	this.messageRowId = messageRowId;
    }

    private String getLabelId() {
	return Stringx.getDefault(getMessageRowId(),"rowMessage").replace( "row", "lb" );
    }

    protected void writeMessage( Window wnd, String msg ) {
	UIUtils.showMessage( wnd, Stringx.getDefault(getMessageRowId(),"rowMessage"), getLabelId(), msg );

  	// Label lbMsg = (Label)wnd.getFellowIfAny( "lbMessage" );
	// if( lbMsg == null ) {
	//     Hlayout vl = (Hlayout)wnd.getFellowIfAny( "hlInput" );
	//     if( vl != null ) {
	// 	Label label = new Label( msg );
	// 	label.setId( "lbMessage" );
	// 	label.setParent( vl );
	// 	if( msg.startsWith( "Error:" ) )
	// 	    label.setStyle( "background:#f4b1b1;" );
	// 	else
	// 	    label.setStyle( "background:#ccffcc;" );
	// 	log.debug( "New message label created for \""+msg+"\"" );
	//     }
	// }
	// else {
	//     lbMsg.setValue( msg );
	//     log.debug( "Message label updated: \""+msg+"\"" );
	// }
    }

    protected void writeMessage( Grid grid, String msg ) {
	Window wnd = ZKContext.findWindow( grid );
	if( wnd != null )
	    writeMessage( wnd, msg );
    }
    protected void writeMessage( Listbox listbox, String msg ) {
	Window wnd = ZKContext.findWindow( listbox );
	if( wnd != null )
	    writeMessage( wnd, msg );
    }
    protected void writeMessage( Combobox combobox, String msg ) {
	Window wnd = ZKContext.findWindow( combobox );
	if( wnd != null )
	    writeMessage( wnd, msg );
    }

    protected void initSort( Grid grid, ColumnFormatter cf, String colId, Comparator comp ) {
     	Column col = (Column)grid.getFellowIfAny( colId );
     	if( col != null ) {
     	    col.setSortAscending( new RowComparator( cf, col, new BidirectionalComparator( comp ) ) );
     	    col.setSortDescending( new RowComparator( cf, col, new BidirectionalComparator( comp, true ) ) );
     	}
    }

    /**
     * Initializes the model producer.
     *
     * @param wnd the application window.
     * @param context the execution context.
     */
    public void initModel( Window wnd, Map context ) {
    }

    /**
     * Updates the model producer.
     *
     * @param wnd the application window.
     * @param context the execution context.
     */
    public void updateModel( Window wnd, Map context ) {
	// Component cmp = wnd.getFellowIfAny( this.getModelName() );
	// if( cmp != null )
	//     this.assignModel( cmp, ((context==null)?new HashMap():context) );
    }

    private void initFromContext( Map context ) {
	if( getPortletId() == null )
	    setPortletId(Stringx.getDefault((String)context.get( "portletid" ),"UNKNOWN"));
	if( getUserId() <= 0 )
	    setUserId( Stringx.toLong(Stringx.getDefault((String)context.get( "userid" ),"-1"),-1L) );
	setResult( context.get( "result" ) );
    }

    protected void assignGrid( Grid grid, Map context ) {
    }
    protected void assignListbox( Listbox listbox, Map context ) {
    }
    protected void assignCombobox( Combobox combobox, Map context ) {
    }
    protected void assignTree( Tree tree, Map context ) {
    }

    protected void updateActions( String pId, long uId ) {
    }

    /**
     * Creates a new list model.
     *
     * @param cmp the component which gets the model assgned.
     * @param context the execution context which includes the current dossier etc.
     */
    public void assignModel( Component cmp, Map context ) {
	// assign parameters from context

	initFromContext( context );

	updateActions( getPortletId(), getUserId() );

	// if component is a window try to identify the target

	Component comp = cmp;
	if( cmp instanceof Window ) 
	    comp = ((Window)cmp).getFellowIfAny( getModelName() );

	// call protected assignMethods

	if( comp instanceof Grid ) {
	    assignGrid( (Grid)comp, context );
	}
	else if( comp instanceof Listbox ) {
	    assignListbox( (Listbox)comp, context );
	}
	else if( comp instanceof Combobox ) {
	    assignCombobox( (Combobox)comp, context );
	}
	else if( comp instanceof Tree ) {
	    assignTree( (Tree)comp, context );
	}
    }

    /**
     * Returns a human readable string of the model.
     *
     * @return a human readable representation of this model.
     */
    public String toString() {
	return Stringx.getDefault(getModelName(),"");
    }
    
}
