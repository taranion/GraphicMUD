/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.map;

/**
 * This class models the interpretation of the data stored in the map.
 * It is assumed that each map-element can be seperated in a symbol-
 * number and some flags.
 *
 *
 * @author Stefan Prelle
 * @version $Id: ElementInterpreter.java,v 1.1 2004/07/04 08:58:20 prelle Exp $
 */

public interface ElementInterpreter  {
    
    //---------------------------------------------------------------
    /**
     * This method returns the amount of bytes that are needed to 
     * store the data of one single map-element.
     *
     * @return Bytes per element
     */
    public abstract int getBytesPerElement();
    
    //---------------------------------------------------------------
    /**
     * Filters the sumbolnumber out of the given bytes of a single
     * element.
     *
     * @param  data Bytes for the element
     * @return Number of the graphical symbol to use
     */
    public abstract int getSymbolNumber(int[] data);
    
    //---------------------------------------------------------------
    /**
     * Filters the flags out of the given bytes of a single element
     * and tests if a special flag is set.
     *
     * @param  data Bytes for the element
     * @param  flag Number of the flag
     * @return boolean True, if the flag was set
     */
    public abstract boolean isFlagSet(int[] data, int flag);
    
    //---------------------------------------------------------------
    /**
     * Filters the (height-)level out of the given bytes of a single
     * map element.
     *
     * @param  data Bytes for the element
     * @return Level of height for the element.
     */
    public abstract int getHeight(int[] data);
    
    //---------------------------------------------------------------
    /**
     * Creates the data for an element with a given symbolnumber and
     * the level of height. The flags have to be set extra with the
     * method <tt>setFlag()</tt>.
     *
     * @param symbol Number of the graphical symbol
     * @param level  Level of height relative to Groundlevel
     * @return Bytedata describig this element
     */
    public abstract int[] buildElement(int symbol, int level);
    
    //---------------------------------------------------------------
    /**
     * Sets or clears a flag in the map-element described via the
     * bytebuffer.
     *
     * @param data Databytes of the mapelement
     * @param flag Number of the flag to clear or set
     * @param state New state of the flag
     * @return Modified elementdata
     */
    public abstract int[] setFlag(int[] data, int flag, boolean state);
    
    //---------------------------------------------------------------
    /**
     * Transforms the element-data from this interpretation to a given
     * interpretation.
     *
     * @param data  The elementdata to transform
     * @param inter The new interpretation
     * @return The data in the new interpretation
     */
    public abstract int[] transform(int[] data, ElementInterpreter inter);
    
    //---------------------------------------------------------------
    /**
     * Transforms the bytebuffer of elementdata into a buffer containing
     * the symbolnumber and charsetnumber. 
     *
     * @param data DataBuffer
     * @param charset Charset to set
     * @return Bytedata describig this element
     */
    public abstract int[] includeCharset(int[] data, byte charset);
    

} // ElementInterpreter
