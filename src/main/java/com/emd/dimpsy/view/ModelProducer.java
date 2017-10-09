package com.emd.dimpsy.view;

import java.util.Map;

import org.zkoss.zk.ui.Component;

import org.zkoss.zul.Window;


/**
 * <code>ModelProducer</code> specifies an interface for mechanisms 
 * to produce models used in zk ui components.
 *
 * Created: Sat Mar 28 08:17:18 2015
 *
 * @author <a href="mailto:">Oliver</a>
 * @version 1.0
 */
public interface ModelProducer {

    /**
     * Initializes the model producer.
     *
     * @param wnd the application window.
     * @param context the execution context.
     */
    public void initModel( Window wnd, Map context );

    /**
     * Updates the model producer.
     *
     * @param wnd the application window.
     * @param context the execution context.
     */
    public void updateModel( Window wnd, Map context );

    /**
     * Creates a new list model.
     *
     * @param cmp the component which gets the model assgned.
     * @param context the execution context which includes the current result etc.
     */
    public void assignModel( Component cmp, Map context );
}
