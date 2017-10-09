package com.emd.dimpsy.template;

import java.sql.Timestamp;

import java.util.Comparator;

import com.emd.xlsutil.upload.UploadBatch;

import com.emd.util.Stringx;

/**
 * <code>BatchEntry</code> decorates an upload batch to be displayed in a list.
 *
 * Created: Mon Jul 13 12:52:38 2015
 *
 * @author <a href="mailto:m01061@tonga.bci.merck.de">Oliver Karch</a>
 * @version 1.0
 */
public class BatchEntry implements Comparable {
    private UploadBatch uploadBatch;
    private String templatename;
    private int printFormat;
    /**
     * Describe mime here.
     */
    private String mime;

    public static final int FMT_MESSAGE = 0;
    public static final int FMT_OUTPUT  = 1;

    public static final Comparator<BatchEntry> COMPARATOR = new Comparator<BatchEntry> () {
	public int compare( BatchEntry o1, BatchEntry o2 ) {
	    if( (o1 == null) && (o2 == null) )
		return 0;

	    Timestamp ts1 = ((BatchEntry)o1).getLogstamp();
	    Timestamp ts2 = ((BatchEntry)o2).getLogstamp();
	    
	    return ((-1)*ts1.compareTo(ts2));
	}
    };

    public BatchEntry( UploadBatch uploadBatch, String templateN ) {
	this.uploadBatch = uploadBatch;
	this.templatename = templateN;
	this.printFormat = FMT_MESSAGE;
    }

    /**
     * Get the <code>UploadBatch</code> value.
     *
     * @return an <code>UploadBatch</code> value
     */
    public final UploadBatch getUploadBatch() {
	return uploadBatch;
    }

    /**
     * Set the <code>UploadBatch</code> value.
     *
     * @param uploadBatch The new UploadBatch value.
     */
    public final void setUploadBatch(final UploadBatch uploadBatch) {
	this.uploadBatch = uploadBatch;
    }

    /**
     * Get the <code>Templatename</code> value.
     *
     * @return a <code>String</code> value
     */
    public final String getTemplatename() {
	return templatename;
    }

    /**
     * Set the <code>Templatename</code> value.
     *
     * @param templatename The new Templatename value.
     */
    public final void setTemplatename(final String templatename) {
	this.templatename = templatename;
    }

    /**
     * Get the <code>Logstamp</code> value.
     *
     * @return a <code>Timestamp</code> value
     */
    public final Timestamp getLogstamp() {
	return uploadBatch.getLogstamp();
    }

    /**
     * Get the <code>Uploadid</code> value.
     *
     * @return a <code>long</code> value
     */
    public final long getUploadid() {
	return uploadBatch.getUploadid();
    }

    /**
     * Get the <code>PrintFormat</code> value.
     *
     * @return an <code>int</code> value
     */
    public final int getPrintFormat() {
	return printFormat;
    }

    /**
     * Set the <code>PrintFormat</code> value.
     *
     * @param printFormat The new PrintFormat value.
     */
    public final void setPrintFormat(final int printFormat) {
	this.printFormat = printFormat;
    }

    /**
     * Get the <code>Filename</code> value.
     *
     * @return a <code>String</code> value
     */
    // public final String getFilename() {
    // 	String fn = Stringx.getDefault( uploadBatch.getFilename(), "unknown" ).trim();
    // 	return ((fn.length() > 0)?fn:"unknown");
    // }

    /**
     * Get the <code>Mime</code> value.
     *
     * @return a <code>String</code> value
     */
    // public final String getMime() {
    // 	String fn = Stringx.getDefault( uploadBatch.getMime(), "unknown type" ).trim();
    // 	return ((fn.length() > 0)?fn:"unknown type");
    // }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     * @return true if this object is the same as the obj argument; false otherwise.
     */
    public boolean equals(Object obj) {
	if( obj instanceof BatchEntry ) {
	    BatchEntry f = (BatchEntry)obj;
	    return (f.getUploadid() == this.getUploadid() );
	}
	return false;
    }

    /**
     * Compares this object with the specified object for order. 
     * Returns a negative integer, zero, or a positive integer as this object is less than, 
     * equal to, or greater than the specified object. 
     *
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
     */
    public int compareTo( Object o) {
	BatchEntry be = (BatchEntry)o;
	return COMPARATOR.compare( this, be );
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object.
     */
    public int hashCode() {
	return String.valueOf(getUploadid()).hashCode();
    }

    /**
     * Returns a human readable string representation of this object.
     *
     * @return a human readble string.
     */
    public String toString() {
	StringBuilder stb = new StringBuilder();
	stb.append( Stringx.getDateString( "EEE, d MMM, yyyy, HH:mm", getLogstamp() ) );
	stb.append( " " );
	stb.append( getTemplatename() );
	stb.append( " (" );
	stb.append( String.valueOf(uploadBatch.getNummsg()) );
	if( printFormat == FMT_OUTPUT )
	    stb.append( " outputs)" );
	else
	    stb.append( " messages)" );
	return stb.toString();
    }
}
