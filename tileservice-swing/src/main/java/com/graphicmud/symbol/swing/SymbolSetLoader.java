package com.graphicmud.symbol.swing;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import com.graphicmud.symbol.SymbolSet;

/**
 *
 */
public abstract class SymbolSetLoader {

	protected int symbolWidth;
	protected int symbolHeight;

	public abstract void load(SymbolSet set, InputStream in, int startID, Function<Integer,Integer> getFramesPerSymbol) throws IOException ;
}
