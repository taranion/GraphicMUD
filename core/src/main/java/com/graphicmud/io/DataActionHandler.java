/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.io;

import java.util.List;


/**
 * Plugins that add new data types should implement this interface
 * to provide means of load and save this data.
 * 
 * Created at 06.06.2007, 20:13:45
 *
 * @author Stefan Prelle
 * @version $Id$
 *
 */
public interface DataActionHandler {

    //----------------------------------------------------
	public List<Integer> getSupportedOperations(String type);

    //----------------------------------------------------
    /**
     * Called to perform a plugin specific data action
     */
    public boolean performDataAction(DataAction action);
}
