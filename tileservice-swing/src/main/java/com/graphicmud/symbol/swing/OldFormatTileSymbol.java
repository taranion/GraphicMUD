package com.graphicmud.symbol.swing;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;

import com.graphicmud.symbol.Symbol;

/**
 *
 */
public class OldFormatTileSymbol extends Symbol {

	private int[][] pixels;

	public OldFormatTileSymbol(int id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

	public void load(InputStream in) throws IOException {
		int width = 32;
		int height= 32;
		int frames= 1;

		int FRAME_SIZE = width*height;
	    byte[] data = new byte[ FRAME_SIZE*2 * frames ];
	    pixels = new int[frames][FRAME_SIZE];
	    in.read(data, 0, data.length);

	    int max = FRAME_SIZE;
	    pixels = new int[frames][max];

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
	  private static int transformColor(byte[] color) {
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
