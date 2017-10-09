package com.emd.dimpsy.view;

/**
 * <code>ColumnFormatter</code> supports column formatting used by a row renderer.
 *
 * Created: Wed Jul 15 20:51:19 2015
 *
 * @author <a href="mailto:okarch@cuba.site">Oliver Karch</a>
 * @version 1.0
 */
public interface ColumnFormatter {

    /**
     * Formats a column's content.
     *
     * @param colName the column name.
     * @param data the data object.
     * @return a string representing the content.
     */
    public String formatColumn( String colName, Object data );

}
