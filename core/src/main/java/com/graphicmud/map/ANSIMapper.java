/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.map;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.function.Function;

import com.graphicmud.symbol.Symbol;
import com.graphicmud.symbol.Symbol.Grapheme;

/**
 * 
 */
public class ANSIMapper implements Function<ViewportMap<Symbol>, ViewportMap<String>> {

	private Logger logger = System.getLogger(ANSIMapper.class.getPackageName());
	
	public static enum GraphemeMapping {
		ASCII,
		CP437,
		UNICODE
	}
	
	public static enum ColorMapping {
		COL16,
		COL256,
		COL16M
	}
	
	private GraphemeMapping symbolMap;
	private ColorMapping    colorMap;

	//-------------------------------------------------------------------
	public ANSIMapper(GraphemeMapping symbols, ColorMapping colors) {
		this.symbolMap = symbols;
		this.colorMap  = colors;
	}

	//-------------------------------------------------------------------
	/**
	 * @see java.util.function.Function#apply(java.lang.Object)
	 */
	@Override
	public ViewportMap<String> apply(ViewportMap<Symbol> source) {
		logger.log(Level.WARNING, "apply(symbolMap={0}, colorMap={1} )", symbolMap, colorMap);
		String[][] mapped = new String[source.getHeight()][source.getWidth()];
		for (int y=0; y<source.getHeight(); y++) {
			String lastTrueFore;
			String lastTrueBack;
			int lastFore =-1;
			int lastBack =-1;
			for (int x=0; x<source.getWidth(); x++) {
				Symbol symbol = source.get(x, y);
				StringBuilder buf = new StringBuilder();
				if (symbol==null) {
					mapped[y][x] = "\u001b[40m ";
					lastFore=-1;
					lastBack=-1;
					continue;
				}
				lastFore=-1;
				lastBack=-1;
				// Foreground color
				switch (colorMap) {
				case COL16M:
				case COL256:
					boolean did256=false;
					if (symbol.getC256Foreground()!=null) {
						if (symbol.getC256Foreground()!=lastFore) {
							buf.append("\u001b[38;5;"+symbol.getC256Foreground()+"m");
							lastFore = symbol.getC256Foreground();
							did256 = true;
						}
						if (did256)
							break;
					}
				default:
					if (symbol.getANSIForeground()==null) {
						buf.append("\u001b[0m");
						lastFore=-1;
					} else if (symbol.getANSIForeground()!=lastFore) {
						if (symbol.getANSIForeground()<8)
							buf.append("\u001b[3"+symbol.getANSIForeground()+"m");
						else
							buf.append("\u001b[9"+(symbol.getANSIForeground()-8)+"m");
						lastFore = symbol.getANSIForeground();
					}
				}
				// Background color
				switch (colorMap) {
				case COL16M:
				case COL256:
					boolean did256=false;
					if (symbol.getC256Background()!=null) {
						if (symbol.getC256Background()!=lastFore) {
							buf.append("\u001b[48;5;"+symbol.getC256Background()+"m");
							lastBack = symbol.getC256Background();
							did256 = true;
						}
						if (did256)
							break;
					}
				default:
					if (symbol.getANSIBackground()==null) {
						buf.append("\u001b[0m");
						lastBack=-1;
					} else if (symbol.getANSIBackground()!=lastBack) {
						if (symbol.getANSIBackground()<8)
							buf.append("\u001b[4"+symbol.getANSIBackground()+"m");
						else
							buf.append("\u001b[10"+(symbol.getANSIBackground()-8)+"m");
						// For zMUD with only 8 background colors
//							buf.append("\u001b[4"+(symbol.getANSIBackground()-8)+"m");
						lastBack = symbol.getANSIBackground();
					}
				}
				// Grapheme
				Grapheme glyph = symbol.getText();
				switch (symbolMap) {
				case UNICODE:
					if (glyph.getUtf8()!=null) {
						buf.append(glyph.getUtf8());
						break;
					}
				case CP437:
					if (glyph.getCp437()!=null) {
						buf.append(glyph.getCp437());
						break;
					}
				default:
					buf.append(glyph.getAscii());
				}
				// Store
				mapped[y][x] = buf.toString();
			}
		}
		ANSIMap ret = new ANSIMap(mapped);
		ret.setPositionSelf(ret.getPositionSelf()[0], ret.getPositionSelf()[1]);
		return ret;
	}


}
