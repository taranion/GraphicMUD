/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.world;

/**
 * This exception means that a requested position wasn't available.
 *
 * @author Stefan Prelle  <prelle@tzi.de>
 * @version $Id: NoSuchPositionException.java,v 1.1 2004/07/04 08:58:20 prelle Exp $
 */

public class NoSuchPositionException extends Exception {
    
	private static final long serialVersionUID = 2737940486553541150L;
	
	private Position pos;

    //---------------------------------------------------------------
    public NoSuchPositionException(String mess, Position pos) {
	super(mess);
	this.pos = pos;
    }

    //---------------------------------------------------------------
    public Position getPosition() {
	return pos;
    }
    
} // NoSuchPositionException
