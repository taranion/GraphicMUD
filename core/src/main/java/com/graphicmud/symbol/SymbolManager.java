/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.symbol;

import java.util.Collection;

/**
 *
 */
public interface SymbolManager {

	//-------------------------------------------------------------------
	public TileGraphicService getTileGraphicService();

	//-------------------------------------------------------------------
	/**
	 * Creates a new symbol set
	 */
	public SymbolSet createSymbolSet();

	//-------------------------------------------------------------------
	/**
	 * Creates a symbol in a given symbol set
	 */
	public Symbol createSymbol(SymbolSet set);

	//-------------------------------------------------------------------
	public Collection<SymbolSet> getSymbolSets();

	//-------------------------------------------------------------------
	public SymbolSet getSymbolSet(int id);

	//-------------------------------------------------------------------
	public void updateSymbolSet(SymbolSet value);

}
