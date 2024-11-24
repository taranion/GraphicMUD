/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.io;




/**
 * This interface provides a generic mean to load and store MUD related
 * data, like zones, players, etc.
 * 
 * Created at 06.06.2007, 19:33:15
 *
 * @author Stefan Prelle
 * @version $Id$
 *
 */
public interface DataStorage {
    
    //-------------------------------------------------------------------------
    /**
     * Register a DataActionHandler for a specific data type.
     * 
     * @param type Identifier of data type to take care of
     * @param ruleSpecific Handler for generic (FALSE) or rule specific (TRUE) data
     * @param handler Class to call for actions
     */
    public boolean registerDataActionHandler(String type, boolean ruleSpecific, DataActionHandler handler);
    
    //-------------------------------------------------------------------------
    /**
     * Tell the storage class which class to use to save rule specific data
     */
//    public void attachStorageHook(String type, RuleSpecificDataActionHook hook);
    
    //-------------------------------------------------------------------------
    public boolean performDataAction(DataAction action);

}
