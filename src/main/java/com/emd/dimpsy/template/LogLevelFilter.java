package com.emd.dimpsy.template;

// import java.sql.SQLException;

// import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Window;

import com.emd.dimpsy.command.InventoryCommand;
import com.emd.dimpsy.config.InventoryPreferences;

import com.emd.util.Stringx;

import com.emd.zk.ZKContext;
import com.emd.zk.command.CommandException;

/**
 * Describe class UseFilter here.
 *
 *
 * Created: Mon Jun  8 08:10:42 2015
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class LogLevelFilter extends InventoryCommand {
    private static Log log = LogFactory.getLog(LogLevelFilter.class);

    /**
     * Creates a new command to activate or deactivate filter usage.
     */
    public LogLevelFilter() {
	super();
    }

    private void checkSwitchOff( Window wnd ) {
      	Checkbox chk = (Checkbox)wnd.getFellowIfAny( getCommandName() );
	if( (chk != null) && !chk.isChecked() ) {
	    int numLevels = SelectResultLog.LEVELS.length;
	    int nCheck = numLevels;
	    for( int i = 0; i < numLevels; i++ ) {
		chk = (Checkbox)wnd.getFellowIfAny( "chkResult"+SelectResultLog.LEVELS[i] );
		if( !chk.isChecked() )
		    nCheck--;
	    }
	    if( nCheck <= 0 ) {
		log.debug( "No log level selected, select all others" );
		for( int i = 0; i < numLevels; i++ ) {
		    String chkId = "chkResult"+SelectResultLog.LEVELS[i];
		    if( !chkId.equals( getCommandName() ) ) {
			chk = (Checkbox)wnd.getFellowIfAny( "chkResult"+SelectResultLog.LEVELS[i] );
			if( !chk.isChecked() )
			    chk.setChecked( true );
		    }
		}
	    }
	}
    }

    /**
     * Tests whether the filter criteria are active.
     *
     * @param wnd the application window.
     * @return checkbox value.
     */
    // public boolean isUseFilterChecked( Window wnd ) {
    //  	Checkbox chk = (Checkbox)wnd.getFellowIfAny( getCommandName() );
    //  	return (( chk != null )? chk.isChecked() : false);
    // }

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

	checkSwitchOff( wnd );

	SelectResultLog selLog = (SelectResultLog)InventoryPreferences.getInstance
	    ( getPortletId(), getUserId() ).getCommand( SelectResultLog.class );
	if( selLog != null )
	    selLog.execute( context, wnd );
    }
}
