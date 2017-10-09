package com.emd.dimpsy.velocity;

// import java.io.ByteArrayInputStream;
// import java.io.ByteArrayOutputStream;
// import java.io.File;
// import java.io.FileOutputStream;
// import java.io.InputStream;
// import java.io.IOException;
// import java.io.OutputStream;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.velocity.tools.generic.SafeConfig;

import org.zkoss.zk.ui.Component;

import org.zkoss.zul.Combobox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.GroupsModel;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.Window;

// import com.emd.util.Stringx;

// #set( $dummy = $zul.addModel( $window, "cbDatasets", $datasets ) )
// ## ########################
// ## Create property set
// ## ########################
// #set( $excelParams = $db.findPropertySetByName( "Excel file report" ) )
// #if( !$excelParams )
// #set( $excelParams = $db.createPropertySet( "Excel file report", "upload input" ) )
// #set( $dummy = $db.addUploadMessage( $upload, "INFO", 0, "Propertyset created: $excelParams" ) )
// #end
// ## ########################
// ## Add parameter datasetName
// ## ########################
// #set( $paramDataset = $parameters.createParameter( $window, "txtDatasetName", "datasetName" ) )
// #set( $paramDataset = $db.addParameter( $excelParams, $paramDataset ) )

// #set( $dummy = $parameters.registerAction( "btDatasetBrowse", "onClick", $upload, $db, " )

/**
 * <code>ZulTool</code> is a tool that can be placed in a velocity context to
 * help dealing with ZK components.
 *
 * Created: Wed Aug  9 21:59:33 2017
 *
 * @author <a href="mailto:">Oliver Karch</a>
 * @version 1.0
 */
public class ZulTool extends SafeConfig {

    private static Log log = LogFactory.getLog(ZulTool.class);

    /**
     * Creates the tool
     */
    public ZulTool() {
	super();
    }
    
    /**
     * Adds a model to the given zul component id.
     *
     * @param wnd the window.
     * @param component the id of the component.
     * @param items the items to be assigned.
     *
     * @return an instiated transformer.
     */
    public void addModel( Object wnd, Object component, Object items ) {
	Window win = null;

	if( wnd instanceof Window )
	    win = (Window)wnd;
	else
	    return;

	if( component == null )
	    return;

	Component comp = null;
	if( component instanceof Component )
	    comp = (Component)component;
	else 
	    comp = win.getFellowIfAny( component.toString() );

	if( comp == null )
	    return;
	
	if( items == null )
	    items = Collections.emptyList();

	if( comp instanceof Grid ) {
	    if( items instanceof GroupsModel )
		((Grid)comp).setModel( (GroupsModel)items );
	    else if( items instanceof ListModel )
		((Grid)comp).setModel( (ListModel)items );
	    else if( items instanceof Collection )
		((Grid)comp).setModel( new ListModelList( (Collection)items ) );
	}
	else if( comp instanceof Listbox ) {
	    if( items instanceof GroupsModel )
		((Listbox)comp).setModel( (GroupsModel)items );
	    else if( items instanceof ListModel )
		((Listbox)comp).setModel( (ListModel)items );
	    else if( items instanceof Collection )
		((Listbox)comp).setModel( new ListModelList( (Collection)items ) );
	}
	else if( comp instanceof Combobox ) {
	    if( items instanceof ListModel )
		((Combobox)comp).setModel( (ListModel)items );
	    else if( items instanceof Collection )
		((Combobox)comp).setModel( new ListModelList( (Collection)items ) );
	}
	else if( comp instanceof Tree ) {
	    if( items instanceof TreeModel )
		((Tree)comp).setModel( (TreeModel)items );
	    else if( items instanceof Collection ) {
		// convert to tree model
	    }
	}
	
    }

}
