package com.emd.dimpsy.template;

import java.util.List;

import java.io.StringReader;

import java.io.IOException;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.io.IOUtils;
 
import org.apache.commons.lang.CharSetUtils;
// import org.apache.commons.lang.StringUtils;

import org.zkoss.zk.ui.Component;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener; 

import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.emd.dimpsy.command.InventoryCommand;
import com.emd.dimpsy.config.InventoryPreferences;

import com.emd.xlsutil.dao.DatasetDAO;
import com.emd.xlsutil.upload.UploadTemplate;

import com.emd.util.Stringx;

import com.emd.zk.ZKContext;
import com.emd.zk.command.CommandException;

/**
 * <code>EditTemplate</code> handles new template creation.
 *
 * Created: Tue Oct  4 19:50:09 2016
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public class EditTemplate extends InventoryCommand {

    private static Log log = LogFactory.getLog(EditTemplate.class);

    private static final String UNTITLED = "Untitled"; 

    private static final String COPY = "Copy";
    private static final String NEW  = "New";
    private static final String SAVE = "Save";
    private static final String DELETE = "Delete";
    private static final String RENAME = "Rename";

    public EditTemplate() {
	super();
    }

    private String createHeader() {
	StringBuilder stb = new StringBuilder( "## ######################\n## " );
	stb.append( UNTITLED );
	stb.append( "\n##\n## Created: " );
	stb.append( Stringx.currentDateString( "dd-MMM-yyyy HH:mm" ) );
	stb.append( "\n## ######################\n" );
	return stb.toString();
    }

    private void clearEditArea( Window wnd ) {
	Textbox txtTempl = (Textbox)wnd.getFellowIfAny( TemplateModel.TXT_TEMPLATE );
	if( txtTempl != null )
	    txtTempl.setValue( createHeader() );
    }

    private void setTemplateName( Window wnd, String newName ) {
	Textbox txtTempl = (Textbox)wnd.getFellowIfAny( TemplateModel.TXT_TEMPLATENAME );
	if( txtTempl != null ) {
	    txtTempl.setValue( newName );
	}
    }

    private String getTemplateName( Window wnd, String def ) {
	Textbox txtTempl = (Textbox)wnd.getFellowIfAny( TemplateModel.TXT_TEMPLATENAME );
	String tName = null;
	if( txtTempl != null ) 
	    tName = Stringx.getDefault( txtTempl.getValue(), Stringx.getDefault(def,"") ).trim();
	else
	    tName = "";
	return ((tName.length() <= 0)?def:tName);
    }

    private UploadTemplate getSelectedTemplate( Window wnd ) {
	TemplateModel tList = (TemplateModel)findModelProducer( TemplateModel.class );
	if( tList != null )
	    return tList.getSelectedTemplate( wnd );
	return null;
    }

    private UploadTemplate retrieveTemplate( Window wnd, String st ) {
	DatasetDAO inv = getInventory();
	if( inv == null )
	    return null;
	UploadTemplate[] templs = null;
	try {
	    templs = inv.findTemplateByName( st );
	}
	catch( SQLException sqe ) {
	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: "+
			 Stringx.getDefault( sqe.getMessage(), "General database error" ) );
	    log.error( sqe );
	    return null;
	}
	return ((templs.length > 0)?templs[0]:null);
    }

    private void loadTemplate( Window wnd ) {
	String st = getTemplateName( wnd, UNTITLED );
	UploadTemplate templ = retrieveTemplate( wnd, st );
	if( templ == null ) {
	    log.debug( "Template "+st+" does not exist." );
	    templ = new UploadTemplate();
	    templ.setTemplatename( st );
	    saveTemplate( wnd, templ );
	}
	else {
	    log.debug( "Template exists: "+st );


	    final UploadTemplate saveTempl = templ;
	    final Window targetWnd = wnd;
	    Messagebox.show( "Template \""+st+"\" exists already.\nDo you want to overwrite it?",
			     "Overwrite Template", 
			     Messagebox.YES+Messagebox.NO, 
			     Messagebox.QUESTION,
			     new EventListener() {
				 public void onEvent(Event event) {
				     if( (Messagebox.ON_YES.equals(event.getName()))) {
					 log.debug( "Template "+saveTempl+" about to be saved." );
					 saveTemplate( targetWnd, saveTempl );
				     }
				 }
			     });
	}
    }

    private void renameTemplate( Window wnd ) {
	UploadTemplate templ = getSelectedTemplate( wnd );
	if( templ == null ) {
	    log.error( "Cannot determine selected template" );
	    return;
	}
	String st = getTemplateName( wnd, UNTITLED );
	if( templ.getTemplatename().equals( st ) ) {
	    log.warn( "No change in template name. Rename ignored." );
	    return;
	}

	UploadTemplate destTempl = retrieveTemplate( wnd, st );

	if( destTempl != null ) {
	    final Window targetWnd = wnd;
	    final UploadTemplate saveTempl = destTempl;
	    saveTempl.setTemplatename( st );
	    Messagebox.show( "Template \""+st+"\" exists already.\nDo you want to overwrite it?",
			     "Overwrite Template", 
			     Messagebox.YES+Messagebox.NO, 
			     Messagebox.QUESTION,
			     new EventListener() {
				 public void onEvent(Event event) {
				     if( (Messagebox.ON_YES.equals(event.getName()))) {
					 log.debug( "Template "+saveTempl+" about to be overwritten." );
					 saveTemplate( targetWnd, saveTempl );
				     }
				 }
			     });
	}
	else {
	    templ.setTemplatename( st );
	    saveTemplate( wnd, templ );
	}
    }

    private boolean deleteTemplate( Window wnd ) {
	String st = getTemplateName( wnd, UNTITLED );
	UploadTemplate templ = retrieveTemplate( wnd, st );
	if( templ == null ) {
	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Warning: \""+st+"\" does not exist. Nothing to delete." );
	    return false;
	}	
	DatasetDAO inv = getInventory();
	if( inv == null )
	    return false;
	boolean del = false;
	try {
	    del = inv.deleteTemplate( templ );
	}
	catch( SQLException sqe ) {
	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: "+
			 Stringx.getDefault( sqe.getMessage(), "General database error" ) );
	    log.error( sqe );
	    return false;
	}
	if( del )
	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Template \""+st+"\" removed successfully." );
	return del;
    }

    private String fromEditText( String templ ) {
	return Stringx.getDefault( templ, createHeader() ).trim().replace( "\n", "\\n" );
    }

    private String cutComment( String st ) {
	int k = -1;
	String cSt = st.trim();
	if( (k = cSt.indexOf( "##" )) >= 0 ) {
	    if( k == 0 )
		cSt = "";
	    else
		cSt = cSt.substring( 0, k );
	}
	return cSt;
    }

    private boolean parseText( Window wnd, String templ ) {
	List<String> lines = null;
	try {
	    lines = IOUtils.readLines( new StringReader(templ) );
	}
	catch( IOException ioe ) {
	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: "+
			 Stringx.getDefault( ioe.getMessage(), "General i/o error" ) );
	    log.error( ioe );
	    return false;
	}
	int lnCount = 0;
	ParseState state = new ParseState();
	boolean parseOk = true;
	for( String ln : lines ) {
	    String st = cutComment( ln ); 
	    state.advanceLine();
	    if( st.length() <= 0 ) {
		log.debug( "Line "+state.getLineCount()+" is empty" );
		continue;
	    }
	    String clause = null;
	    if( state.unbalancedClause( st ) ) {
		String msg = "Unbalanced directive found at line "+String.valueOf(state.getLineCount());
		log.warn( msg );
		showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Warning: "+msg );
		parseOk = false;
	    }
	}
	if( state.unbalancedBlock() ) { 
	    String msg = "Unbalanced block found at line "+String.valueOf(state.getLastOpenBlock());
	    log.warn( msg );
	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Warning: "+msg );
	    parseOk = false;
	}
	return parseOk;
    }

    private void storeTemplate( Window wnd, UploadTemplate templ ) {
	DatasetDAO inv = getInventory();
	if( inv == null ) 
	    return;
	try {
	    templ = inv.storeTemplate( this.getUserId(), templ );
	}
	catch( SQLException sqe ) {
	    showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Error: "+
			 Stringx.getDefault( sqe.getMessage(), "General database error" ) );
	    log.error( sqe );
	    return;
	}
	showMessage( wnd, "rowMessageUpload", "lbMessageUpload", "Template \""+templ+"\" stored successfully." );
	updateTemplateList( wnd, templ );
    }

    private void saveTemplate( final Window wnd, final UploadTemplate templ ) {
	String tName = getTemplateName( wnd, UNTITLED );
	Textbox txtTempl = (Textbox)wnd.getFellowIfAny( TemplateModel.TXT_TEMPLATE );
	if( txtTempl != null ) {
	    String st = txtTempl.getValue().trim();
	    if( !parseText( wnd, st ) ) {
		final String editText = fromEditText(st);
		Messagebox.show( "Template \""+tName+"\" might cause problems.\nDo you want to save it anyway?",
				 "Save Template", 
				 Messagebox.YES+Messagebox.NO, 
				 Messagebox.QUESTION,
				 new EventListener() {
				     public void onEvent(Event event) {
					 if( (Messagebox.ON_YES.equals(event.getName()))) {
					     templ.setTemplate( editText );
					     storeTemplate( wnd, templ );
					 }
				     }
				 });
	    }
	    else {
		templ.setTemplate( fromEditText(st) );
		storeTemplate( wnd, templ );
	    }
	}
    }

    private void updateTemplateList( Window wnd, UploadTemplate tSel ) {
	// inform template model about new entries

	TemplateModel tList = (TemplateModel)findModelProducer( TemplateModel.class );
	if( tList != null )
	    tList.reloadTemplates( wnd, tSel );
    }

    // private void updateTemplateText( Window wnd, UploadTemplate templ ) {
    // 	Textbox txtTempl = (Textbox)wnd.getFellowIfAny( TemplateModel.TXT_TEMPLATE );
    // 	if( txtTempl != null )
    // 	    txtTempl.setValue( toEditText(templ.getTemplate()) );
    // 	Label lbTempl = (Label)wnd.getFellowIfAny( TemplateModel.LB_TEMPLATE );
    // 	if( lbTempl != null )
    // 	    lbTempl.setValue( templ.getTemplatename() );
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

	UploadTemplate templ = null;
	boolean renameIt = false;
	if( getCommandName().endsWith( SAVE ) ) {
	    loadTemplate( wnd );
	}
	else if( getCommandName().endsWith( DELETE ) && (deleteTemplate( wnd )) ) {
	    updateTemplateList( wnd, null );
	}

	else if( getCommandName().endsWith( RENAME ) ) {
	    renameTemplate( wnd );
	}
	else if( getCommandName().endsWith( NEW ) ) {
	    clearEditArea( wnd );
	    setTemplateName( wnd, UNTITLED );
	}
	else if( getCommandName().endsWith( COPY ) ) {
	    setTemplateName( wnd, UNTITLED );
	}

    }

}

class ParseState {
    int lastOpenBlock;
    int openBlocks;
    int lnCount;

    private static Log log = LogFactory.getLog(ParseState.class);

    private static final String[] KEY_OPEN_BLOCK = {
	"foreach",
	"macro",
	"if"
    };
    private static final String[] KEY_OTHER = {
	"elseif",
	"else",
	"parse",
	"stop",
	"set",
	"end"
    };

    private static final String KEEP_CHARS = "(){}[]\"'";

    ParseState() {
	this.lnCount = 0;
	this.lastOpenBlock = 0;
	this.openBlocks = 0;
    }

    int getLastOpenBlock() {
	return lastOpenBlock;
    }

    int advanceLine() {
	lnCount++;
	return lnCount;
    }

    int getLineCount() {
	return lnCount;
    }

    private String tail( String st, int idx ) {
	if( idx >= st.length() )
	    return "";
	return st.substring( idx );
    }

    private String compressClause( String clause ) {
	log.debug( "Inspecting clause: "+clause );
	String cClause = CharSetUtils.keep( clause, KEEP_CHARS );
	if( cClause.length() > 0 )
	    log.debug( "After compression: "+cClause );
	return cClause;
    }

    private boolean isBalanced( String clause ) {
	String cClause = compressClause( clause );
	boolean inQuote = false;
	int blockOpened = 0;
	for( int i = 0; i < cClause.length(); i++ ) {
	    char cc = cClause.charAt(i);
	    if( (cc == '\"') || (cc == '\'') ) {
		inQuote = !inQuote;
	    }
	    else if( (cc == '(') || (cc == '{') || (cc == '[') ) {
		blockOpened++;
	    }
	    else if( (cc == ')') || (cc == '}') || (cc == ']') ) {
		blockOpened--;
	    }
	}
	return !( (inQuote) || (blockOpened > 0) );
    }

    boolean unbalancedClause( String st ) {
	int k = 0; 
	int l = 0;
	boolean balanced = true;
	while( (k = st.indexOf( "#", l )) >= l ) {
	    k++;
	    String clause = tail( st, k );
	    if( clause.length() > 2 ) {
		boolean keyFound = false;
		for( int j = 0; j < KEY_OPEN_BLOCK.length; j++ ) {
		    if( clause.startsWith( KEY_OPEN_BLOCK[j] ) ) {
			lastOpenBlock = lnCount;
			openBlocks++;
			k+=KEY_OPEN_BLOCK[j].length();
			clause = tail( st, k );
			keyFound = true;
			break;
		    }
		}
		if( !keyFound ) {
		    for( int j = 0; j < KEY_OTHER.length; j++ ) {
			if( clause.startsWith( KEY_OTHER[j] ) ) {
			    k+=KEY_OTHER[j].length();
			    clause = tail( st, k );
			    keyFound = true;
			    if( j == KEY_OTHER.length-1 )
				openBlocks--;
			    break;
			}
		    }
		}
		if( keyFound &&  !isBalanced( clause ) ) {
		    balanced = false;
		    break;
		}
	    }
	    l=(k+1);
	}
	return !balanced;
    }

    boolean unbalancedBlock() {
	return (openBlocks != 0);
    }
}
