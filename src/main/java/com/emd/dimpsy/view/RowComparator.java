package com.emd.dimpsy.view;

import java.util.Comparator;

import org.zkoss.zul.Column;

/**
 * RowComparator compares formatted columns to provide sorting.
 *
 * Created: Wed Jul 15 10:23:15 2015
 *
 * @author <a href="mailto:">Oliver Karch</a>
 * @version 1.0
 */
public class RowComparator implements Comparator {
    private Comparator comparator;
    private Column column;
    private ColumnFormatter renderer;

    public RowComparator( ColumnFormatter sr, Column col, Comparator comp ) {
	this.comparator = comp;
	this.column = col;
	this.renderer = sr;
    }

    /**
     * Get the Comparator value.
     * @return the Comparator value.
     */
    public Comparator getComparator() {
	return comparator;
    }

    /**
     * Set the Comparator value.
     * @param newComparator The new Comparator value.
     */
    public void setComparator(Comparator newComparator) {
	this.comparator = newComparator;
    }

    /**
     * Get the Column value.
     * @return the Column value.
     */
    public Column getColumn() {
	return column;
    }

    /**
     * Set the Column value.
     * @param newColumn The new Column value.
     */
    public void setColumn(Column newColumn) {
	this.column = newColumn;
    }

    protected String getContentValue( Object data ) {
	return renderer.formatColumn( column.getId(), data );
    }

    /**
     * Compares its two arguments for order. 
     * Returns a negative integer, zero, or a positive 
     * integer as the first argument is less than, equal to, 
     * or greater than the second.
     *
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared. 
     *
     * @return a negative integer, zero, or a positive integer as 
     * the first argument is less than, equal to, or greater than the second. 
     */
    public int compare(Object o1, Object o2) {	
	String st1 = getContentValue( o1 );
	String st2 = getContentValue( o2 );
	return comparator.compare( st1, st2 );
    }
   
}
