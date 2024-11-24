/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.map;

import java.util.List;

import com.graphicmud.symbol.Symbol;
import com.graphicmud.world.tile.GridPosition;

public interface GridMap {

	public static record Layer(int nr, String name) {}

	public int getWidth();
	public int getHeight();
	public List<Layer> getLayer();
	public int[] getRawLayerData(Layer layer);

	public ViewportMap<Symbol> getArea(int centerX, int centerY, int layer, int rangeX, int rangeY, int outside);
	public Symbol getSymbolAt(GridPosition position);

}