/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.map;

/**
 * This interpreter handles data like in "Ultima 5", where there was
 * one Byte per element with no flag-data stored.<br>
 * This interpreter is usually no good idea for a complex mud - but may 
 * be useful for testing purpose. 
 *
 * @author Stefan Prelle  <prelle@tzi.de>
 * @version $Id: U5alikeInterpreter.java,v 1.2 2005/03/29 21:48:12 prelle Exp $
 */

public class U5alikeInterpreter implements ElementInterpreter {
  
  //---------------------------------------------------------------
  public static int byteToInt(byte b) {
    if (b<0)
      return 256+b;
    else
      return b;
  }
  
  //---------------------------------------------------------------
  /**
   * This method returns the amount of bytes that are needed to 
   * store the data of one single map-element. This interpreter
   * uses 1 byte for each element.
   *
   * @return Bytes per element
   */
  public int getBytesPerElement() {
    return 1;
  }
  
  //---------------------------------------------------------------
  /**
   * Filters the sumbolnumber out of the given bytes of a single
   * element.
   *
   * @param  data Bytes for the element
   * @return Number of the graphical symbol to use
   */
  @Override
  public int getSymbolNumber(int[] data) {
    return data[0]; // byteToInt(data[0]);
  }
  
  
  //---------------------------------------------------------------
  /**
   * Returns always false because there can't be any flags for an
   * element.
   *
   * @param  data Bytes for the element
   * @param  flag Number of the flag
   * @return False
   */
  @Override
  public boolean isFlagSet(int[] data, int flag) {
    return false;
  }
  
  //---------------------------------------------------------------
  /**
   * Returns always false because there can't be any height for an
   * element.
   *
   * @param  data Bytes for the element
   * @return 0.
   */
  @Override
  public int getHeight(int[] data) {
    return 0;
  }
  
  //---------------------------------------------------------------
  /**
   * Creates the data for an element with a given symbolnumber. The 
   * given height is ignored, flags are not allowed.
   *
   * @param symbol Number of the graphical symbol
   * @param level  IGNORED
   * @return Bytedata describig this element
   */
  @Override
  public int[] buildElement(int symbol, int level) {
    int[] ret = new int[1];
    ret[0] = symbol;
    return ret;
  }
  
  //---------------------------------------------------------------
  /**
   * Normally sets or clears a flag in the map-element described 
   * via the bytebuffer. Useless because this interpreter doesn't
   * know flags.
   *
   * @param data Databytes of the mapelement
   * @param flag Number of the flag to clear or set
   * @param state New state of the flag
   * @return Modified elementdata
   */
  @Override
  public int[] setFlag(int[] data, int flag, boolean state) {
    return data;
  }
  
  //---------------------------------------------------------------
  /**
   * Transforms the element-data from this interpretation to a given
   * interpretation.
   *
   * @param data  The elementdata to transform
   * @param inter The new interpretation
   * @return The data in the new interpretation
   */
  @Override
  public int[] transform(int[] data, ElementInterpreter inter) {
    int[] ret = inter.buildElement(
    		getSymbolNumber(data),
    		getHeight(data)
    	);
    return ret;
  }
  
  //---------------------------------------------------------------
  /**
   * Transforms the bytebuffer of elementdata into a buffer containing
   * the symbolnumber and charsetnumber. 
   *
   * @param data DataBuffer
   * @param charset Charset to set
   * @return Bytedata describig this element
   */
  @Override
  public int[] includeCharset(int[] data, byte charset) {
    return data;
  }
  
} // U5alikeInterpreter
