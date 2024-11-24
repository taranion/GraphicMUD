package com.graphicmud.telnet.impl;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.graphicmud.network.MUDClientCapabilities;
import com.graphicmud.symbol.Symbol;
import com.graphicmud.symbol.SymbolManager;
import com.graphicmud.symbol.SymbolManagerSingleton;
import com.graphicmud.symbol.SymbolSet;
import com.graphicmud.symbol.Symbol.Grapheme;
import com.graphicmud.symbol.Symbol.TextColor;

/**
 * Converts tilemaps to ANSI codes
 */
public class ANSIArtMapConverter {

	public static enum UseSymbol {
		ANSI,
		CP437,
		UNICODE
	}

	private final static Logger logger = System.getLogger("mud.symbols");

	private static String CSI = (char)0x1b+"[";

	//-------------------------------------------------------------------
	public static List<String> convertMap(int[][] mapData, UseSymbol mode, MUDClientCapabilities.Color color) {
		List<String> ret = new ArrayList<>();
		SymbolManager manager = SymbolManagerSingleton.getInstance();
		for (SymbolSet set : manager.getSymbolSets()) {
			logger.log(Level.INFO, "SymbolSet {0} has size {1}", set.getId(), set.size());
		}
		SymbolSet set = manager.getSymbolSet(4);
		if (set==null) {
			logger.log(Level.ERROR, "No symbol set found");
			return ret;
		}

//		SymbolSet set = MUD.getInstance().getSymbolManager().getSymbolSet(2);
		logger.log(Level.INFO, "Symbol set "+set.getFile());
		// Remote party does not support
		int lastFore = -1;
		int lastBack = -1;
		for (int[] line : mapData) {
			StringBuffer out = new StringBuffer();
			logger.log(Level.TRACE, Arrays.toString(line));
			for (int symCode : line) {
				Symbol symbol = (symCode>-1)?set.getSymbol(symCode):null;
				if (symbol==null) {
					out.append("?");
				} else {
					TextColor fore= symbol.getFore();
					TextColor back= symbol.getBack();
					int fg = (fore.getAnsi()!=null)?fore.getAnsi():-1;
					int bg = (back.getAnsi()!=null)?back.getAnsi():-1;
					if (fg!=lastFore) {
						if (fg<0) {
							out.append(CSI+"39m");							
						} else {
							int foo = (fg<8)?(30+fg):(82+fg);
							out.append(CSI+foo+"m");
						}
					}
					lastFore = fg;
					if (bg!=lastBack) {
						if (bg<0) {
							out.append(CSI+"49m");							
						} else {
							int foo = (bg<8)?(40+bg):(92+bg);
							out.append(CSI+foo+"m");
						}
					}
					lastBack = bg;
					
					// Choose glyph
					Grapheme glyph = symbol.getText();
					switch (mode) {
					case UNICODE:
						if (glyph.getUtf8()!=null) {
							out.append(glyph.getUtf8());
							break;
						}
					case CP437:
						if (glyph.getCp437()!=null) {
							out.append(glyph.getCp437());
							break;
						}
					default:
						out.append(glyph.getAscii());
					}
				}
			}
			out.append(CSI+"39;49;0m");
			lastBack=-1;
			lastFore=-1;
			ret.add(out.toString());
//			out.append("\r\n");
//			System.out.print("\n");
		}
//		out.append(CSI+"0m");
//		System.out.println(out.toString());
//
//		return out.toString();
		return ret;
	}

	//-------------------------------------------------------------------
	public static String convertMapToString(int[][] mapData, UseSymbol mode, MUDClientCapabilities.Color color) {
		StringBuffer out = new StringBuffer();
		for (String line : convertMap(mapData, mode, color)) {
			out.append(line+"\r\n");
		}
		out.append(CSI+"39;49m");
		out.append(CSI+"0m");
		System.out.println(out.toString());
		return out.toString();
	}
}
