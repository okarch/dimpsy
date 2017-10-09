package com.emd.dimpsy.template;

import java.util.Date;

import java.sql.Timestamp;

import org.apache.commons.lang.time.DateFormatUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;

import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;

import com.emd.dimpsy.view.ColumnFormatter;
import com.emd.xlsutil.upload.UploadLog;

import com.emd.util.Stringx;

/**
 * SampleResultRenderer renders the sample search result row.
 *
 * Created: Mon Apr 7 20:30:01 2015
 *
 * @author <a href="mailto:">Oliver</a>
 * @version 1.0
 */
public class UploadLogRenderer implements RowRenderer, ColumnFormatter {

    private static Log log = LogFactory.getLog(UploadLogRenderer.class);

    private static final String[] columnNames = {
	"colLogstamp",
	"colLevel",
	"colLine",
	"colMessage"
    };

    public UploadLogRenderer() {
    }

    /**
     * Formats a column's content.
     *
     * @param colName the column name.
     * @param data the data object.
     * @return a string representing the content.
     */
    public String formatColumn( String colName, Object data ) {
	UploadLog log = (UploadLog)data;

	if( "colLogstamp".equals( colName ) ) {
	    return formatDate(log.getLogstamp(), "ddMMMyyyy hh:mm:ss.SSS" );
	}

	else if( "colLevel".equals( colName ) ) {
	    return Stringx.getDefault(log.getLevel(), "");
	}

	else if( "colLine".equals( colName ) ) {
	    return String.valueOf( log.getLine() );
	}

	else if( "colMessage".equals( colName ) ) {
	    return Stringx.getDefault( log.getMessage(), "");
	}

	return "";
    }

    private String formatDate( Date dt, String fmt ) {
	if( dt == null )
	    return "";
	return DateFormatUtils.format( dt, fmt );
    }

    /**
     * Renders the data to the specified row.
     *
     * @param row the row to render the result.
     * @param data that is returned from ListModel.getElementAt(int) 
     * @exception java.lang.Exception
     */
    public void render( Row row, Object data, int index)
	throws java.lang.Exception {

	if( data instanceof UploadLog ) {
	    UploadLog log = (UploadLog)data;

	    for( int i = 0; i < columnNames.length; i++ ) 
		(new Label( formatColumn( columnNames[i], log ) )).setParent( row );
	}
    }

}
