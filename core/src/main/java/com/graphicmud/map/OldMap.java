/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.map;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

/**
 * Map.java
 *
 * @author Stefan Prelle  <prelle@tzi.de>
 * @version $Id: Map.java,v 1.1 2004/07/04 08:58:20 prelle Exp $
 */

public class OldMap extends Object implements Serializable {

	private final static Logger logger = System.getLogger("mud.map");

	class Dimension {
		int width, height;
		public Dimension(int width, int height) {
			this.width = width;
			this.height= height;
		}
	}

	private static final long serialVersionUID = 1L;

	public final static int TYPE_OUTSIDE   = 0;
	public final static int TYPE_TOWN      = 1;
	public final static int TYPE_DUNGEON   = 2;
	public final static int TYPE_UNDERWATER= 3;
	public final static int TYPE_AIR       = 4;


	protected Dimension size;
	protected ElementInterpreter interpretation;
	protected int       nr;
	protected String    name;
	protected byte      type;
	protected byte      modMagic;
	protected byte      modMiracles;
	protected int       aura;
	protected boolean   closed;
	protected byte      charset;

	protected transient int[] data;

	//---------------------------------------------------------------
	/**
	 * Creates a new map with a given size and interpretation.
	 *
	 * @param n Number of the map
	 * @param x Size along the X-axis
	 * @param y Size along the Y-axis
	 * @param inter Interpretation
	 */
	public OldMap(int n, int x, int y, ElementInterpreter inter) {
		size = new Dimension(x,y);
		interpretation = inter;
		data = new int[x*y*inter.getBytesPerElement()];
		for (int i=0; i<data.length; i++)
			data[i] = 0;

		nr = n;
		type = TYPE_OUTSIDE;
	}


	//---------------------------------------------------------------
	/**
	 * Sets a new interpretation for the map. The whole bytebuffer
	 * is modified so it may take some time.
	 *
	 * @param inter New interpretation
	 */
	public void transform(ElementInterpreter inter) {
		int bpe = interpretation.getBytesPerElement();
		int bpe2= inter.getBytesPerElement();
		int max = size.width*size.height;
		int[] elem = new int[bpe];
		int[] newData = new int[max*bpe2];
		// Begin transforming-loop
		for (int pos=0; pos<max; pos++) {
			for (int in=0; in<bpe; in++)
				elem[in] = data[pos*bpe+in];
			elem = interpretation.transform(elem, inter);
			for (int in=0; in<bpe2; in++)
				newData[pos+in] = elem[in];
		}
		// New data built - copy it to old data
		data = newData;
		interpretation = inter;
	}

	//---------------------------------------------------------------
	/**
	 * Returns the used interpretation
	 *
	 * @return Interpretation for the mapelements
	 */
	public ElementInterpreter getInterpreter() {
		return interpretation;
	}

	//---------------------------------------------------------------
	/**
	 * Returns the data of an element at a special position.
	 * WARNING! Because of performance-reasons there won't be a
	 * check if the coordinates are valid.
	 *
	 * @param posX X-Position on the map
	 * @param posY Y-Position on the map
	 * @return Bytebuffer for the symbol
	 */
	protected int[] getSymbolDataAt(int posX, int posY) {
		int[] ret = new int[interpretation.getBytesPerElement()];
		int start = (posY*size.width + posX) * ret.length;
		for (int i=0; i<ret.length; i++)
			ret[i] = data[start + i];
		return ret;
	}

	//---------------------------------------------------------------
	/**
	 * Returns a part of the map. The coordinates define the center
	 * of the of the viewport while the range means how many elements
	 * wide the viewport is in each direction (like a radius).
	 *
	 * @param posX The X-Position of the centre
	 * @param posY The Y-Position of the centre
	 * @param range The radius
	 */
	public int[] getArea(int posX, int posY, int range) {
		if (posX>=size.width || posX<0)
			throw new IllegalArgumentException("X-Coordinate out of range. (0.."+(size.width-1)+")");
		if (posY>=size.height || posY<0)
			throw new IllegalArgumentException("Y-Coordinate out of range. (0.."+(size.height-1)+")");
		if (range<0)
			throw new IllegalArgumentException("Range must be positive");

		int bpe = interpretation.getBytesPerElement();
		int line = 2*range + 1;
		int[] ret = new int[(line*line)*bpe];

		// Start outer loop
		for (int y=-range; y<=range; y++) {
			int oldY = posY + y;
			int newY = y + range;
			for (int x=-range; x<=range; x++) {
				int oldX = posX + x;
				int newX = x + range;
				int[] old;

				// Is the actual element on the map?
				if (oldX<0 || oldX>=size.width || oldY<0 || oldY>=size.height)
					// No, just use symbol 0
					old = interpretation.buildElement(1,0);
				else
					// Yes
					old = getSymbolDataAt(oldX, oldY);

				// Copy the elementdata to the return-buffer
				for (int i=0; i<bpe; i++)
					ret[(newY*line+newX)*bpe+i] = old[i];

			} // X
		} // Y
		return ret;
	}

	//---------------------------------------------------------------
	/**
	 * Returns a part of the map. The coordinates define the center
	 * of the of the viewport while the range means how many elements
	 * wide the viewport is in each direction (like a radius).<BR>
	 * Unlike <tt>getArea</tt> this method returns just the numbers
	 * if the symbols combined with the selected charsetnumber.
	 *
	 * @param posX The X-Position of the centre
	 * @param posY The Y-Position of the centre
	 * @param range The radius
	 */
	public int[] getViewport(int posX, int posY, int range) {
//		System.out.println("getViewport("+posX+","+posY+","+range+")");
		int[] ret = getArea(posX, posY, range);
		int bpe = interpretation.getBytesPerElement();
//		int line = 2*range + 1;

		int i=0;
		int[] tmp = new int[bpe];
		while (i<ret.length) {
			for (int j=0; j<bpe; j++)
				tmp[j] = ret[j+i];
			interpretation.includeCharset(tmp,charset);
			for (int j=0; j<bpe; j++)
				ret[j+i] = tmp[j];
			i++;
		}
		return ret;
	}

	//---------------------------------------------------------------
	/**
	 * Writes the raw data to disk. This does not include the
	 * size-information or the ElementInterpreter that must be saved
	 * via the Serializable-Interface.
	 *
	 * @param out OutputStream to write the data in
	 * @exception java.io.IOException Error while saving
	 */
	public void save(OutputStream out) throws IOException {
//		BufferedOutputStream bout = new BufferedOutputStream(out);
//		bout.write(data,0,data.length);
//		bout.close();
	}

	//---------------------------------------------------------------
	/**
	 * Reads the raw data from an inputstream. This does not include
	 * the mapsize and ElementInterpreter which must be set to this
	 * time.
	 *
	 * @param in InputStream to get data from
	 * @exception java.io.IOException Error while reading
	 */
	public void load(InputStream in) throws IOException {
		BufferedInputStream bin = new BufferedInputStream(in);
		if (in.available()!=data.length)
			throw new IOException("Expected "+data.length+" bytes but in the stream are "+in.available());
		logger.log(Level.INFO,"Now read "+in.available()+" Bytes.");
		byte[] tmp = new byte[data.length];
		bin.read(tmp,0,data.length);
		for (int i=0; i<data.length; i++) {
			data[i] = U5alikeInterpreter.byteToInt(tmp[i]);
		}
		bin.close();
	}

	//---------------------------------------------------------------
	public String toString() {
		StringBuffer ret = new StringBuffer("Map ");
		ret.append(nr+"  ");
		ret.append(name);
		ret.append("  Size="+size.width+"x"+size.height);
		ret.append("  "+interpretation.getBytesPerElement());
		ret.append(" BpE");
		return ret.toString();
	}

} // Map
