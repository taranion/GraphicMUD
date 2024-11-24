package com.graphicmud.symbol.swing;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import com.graphicmud.symbol.SymbolSet;

import lombok.Builder;

/**
 *
 */
@Builder
public class OldFormatSymbolSetLoader extends SymbolSetLoader {

	//-------------------------------------------------------------------
	/**
	 * @param in
	 * @param startID Numeric ID to start with
	 * @param framesGetter Used to determine number of frames per symbol
	 */
	public void load(SymbolSet set, InputStream in, int startID, Function<Integer,Integer> framesGetter) throws IOException {
		byte[] data = in.readAllBytes();

	    int max = super.symbolWidth * symbolHeight;
	    int frames = 1;
	    int[][] pixels = new int[frames][max];

	    for (int fr=0; fr<frames; fr++) {
	      int offset = fr * max;
	      byte[] tmp = new byte[2];

	      for (int i=0; i<max; i++) {
	        tmp[0] = data[offset+2*i];
	        tmp[1] = data[offset+2*i+1];
	        pixels[fr][i] = transformColor(tmp);
	      }
	    }

	}

	  //---------------------------------------------------------------
	  /**
	   * Converts a two-byte color-value into an integer colorvalue
	   * as obtained through <tt>java.awt.Color.getRGB()</tt>.
	   *
	   * @param color Two-Byte-Colorvalue
	   * @return Integervalue for the color
	   * @see java.awt.Color
	   */
	  protected static int transformColor(byte[] color) {
	    int ret = 0;
	    for (int i=0; i<2; i++) {
	      ret *= 256;
	      if (color[i]<0)
	        ret += 256+color[i];
	      else
	        ret += color[i];
	    }

	    Color col = new Color( ((ret>>10)&31)<<3,
	        ((ret>> 5)&31)<<3,
	        ( ret     &31)<<3);
	    return col.getRGB();
	  }
}
