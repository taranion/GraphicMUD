/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.symbol;

import java.io.IOException;

import com.graphicmud.map.ViewportMap;

/**
 *
 */
public interface TileGraphicService {

	//-------------------------------------------------------------------
	public void loadSymbolImages(SymbolSet set) throws IOException;

	public byte[] renderMap(ViewportMap<Symbol> map, SymbolSet set);

}
