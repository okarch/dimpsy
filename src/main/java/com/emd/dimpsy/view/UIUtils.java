package com.emd.dimpsy.view;

import org.zkoss.zk.ui.Component;

import org.zkoss.zk.ui.event.Event;

import org.zkoss.zul.Cell;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Window;

import com.emd.zk.ZKContext;

/**
 * UIUtils provides some helper functions for ui presentation.
 *
 * Created: Mon Mar 23 07:50:52 2015
 *
 * @author <a href="mailto:">Oliver</a>
 * @version 1.0
 */
public class UIUtils {

    private UIUtils() { }

    private static Cell styledCell( Cell cell, String msg ) {
	if( cell == null ) 
	    cell = new Cell();

	if( msg.startsWith( "Error:" ) ) {
	    cell.setStyle( "text-align:center; background:#f4b1b1;" );
	}
	else if( msg.startsWith( "Warning:" ) ) {
	    cell.setStyle( "text-align:center; background:#f48808;" );
	}
	else {
	    cell.setStyle( "text-align:center;" );
	}
	return cell;
    }

    public static void showMessage( Window wnd, 
				    String parentId,
				    String labelId, 
				    String msg ) {

	Label lbMsg = (Label)wnd.getFellowIfAny( labelId );
	if( lbMsg == null ) {
	    Component cmp = wnd.getFellowIfAny( parentId );
	    if( cmp == null )
		return;

	    Cell cell = styledCell( null, msg );
	    Label label = new Label( msg );
	    label.setId( labelId );
	    label.setParent( cell );

	    if( cmp instanceof Grid ) {
		Row row = new Row();
		cell.setColspan( 2 );
		cell.setParent( row );
		Rows rows = ((Grid)cmp).getRows();
		row.setParent( rows );
	    }
	    else {
		cell.setColspan( 2 );
		cell.setParent( cmp );
	    }
	}
	else {
	    Cell cell = (Cell)lbMsg.getParent();
	    styledCell( cell, msg );
	    lbMsg.setValue( msg );
	}
    }

    public static void clearMessage( Window wnd, String labelId ) {
	Label lbMsg = (Label)wnd.getFellowIfAny( labelId );
	if( lbMsg != null ) {
	    Cell cell = (Cell)lbMsg.getParent();
	    lbMsg.detach();
	    cell.detach();	    
	    // Component row = cell.getParent();
	    // if( row instanceof Row )
	    // 	row.detach();
	}
    }

    public static Window getWindow( Event evt ) {
	Component cmp = evt.getTarget();
	Window wnd = null;
	if( cmp != null )
	    wnd = ZKContext.findWindow( cmp );
	return wnd;
    }


}
