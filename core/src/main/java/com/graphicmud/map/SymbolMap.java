/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.map;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.graphicmud.symbol.Symbol;
import com.graphicmud.symbol.SymbolSet;

/**
 * 
 */
public class SymbolMap implements ViewportMap<Symbol> {
	
	private int width;
	private int height;
	private int selfX;
	private int selfY;
	private Symbol[][] terrain;
	private Symbol[][] scenery;
	private Symbol[][] mobiles;
	private Color[][] lights;
	
	private Map<String, Object[][]> namedLayers;

	//-------------------------------------------------------------------
	public SymbolMap(int width, int height) {
		this.width = width;
		this.height= height;
		terrain = new Symbol[height][width];
		selfX = (width+1)/2;
		selfY = (height+1)/2;
		this.namedLayers = new HashMap<>();
	}

	//-------------------------------------------------------------------
	public SymbolMap(int[][] terrain, SymbolSet set) {
		this(terrain[0].length, terrain.length);
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				this.terrain[y][x] = set.getSymbol(terrain[y][x]);
			}
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.map.GeneratedMap#getWidth()
	 */
	@Override
	public int getWidth() {
		return width;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.map.GeneratedMap#getHeight()
	 */
	@Override
	public int getHeight() {
		return height;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.map.GeneratedMap#get(int, int)
	 */
	@Override
	public Symbol get(int x, int y) {
		return terrain[y][x];
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.map.GeneratedMap#set(int, int, java.lang.Object)
	 */
	@Override
	public void set(int x, int y, Symbol val) {
		terrain[y][x] = val;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.map.ViewportMap#getPositionSelf()
	 */
	@Override
	public int[] getPositionSelf() {
		return new int[] {selfX, selfY};
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.map.ViewportMap#setPositionSelf(int, int)
	 */
	@Override
	public ViewportMap<Symbol> setPositionSelf(int x, int y) {
		this.selfX = x;
		this.selfY = y;
		return this;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.map.ViewportMap#addLayer(java.lang.String, java.lang.Object[][])
	 */
	@Override
	public <X> void addLayer(String name, X[][] data) {
		namedLayers.put(name, data);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.map.ViewportMap#reduceSize(int, int)
	 */
	@Override
	public void reduceSize(int radiusX, int radiusY) {
		// Validate
		int newWidth = radiusX*2 +1;
		int newHeight= radiusY*2 +1;
		if (newWidth > width) throw new IllegalArgumentException("X-Radius too large - max. "+((width-1)/2));
		if (newHeight>height) throw new IllegalArgumentException("Y-Radius too large - max. "+((height-1)/2));
		// Calculate Width-Shrinking
		if (newWidth<width) {
			int start = (width-newWidth)/2;
			for (int y=0; y<height; y++) {
				// Terrain Layer
				terrain[y] = Arrays.copyOfRange(terrain[y], start, start+newWidth);
//				scenery[y] = Arrays.copyOfRange(scenery[y], diff, diff+newWidth-1);
			}
			width = newWidth;
		}
		// Calculate Height-Shrinking
		if (newHeight<height) {
			int start = (height-newHeight)/2;
			terrain = Arrays.copyOfRange(terrain, start, start+newHeight);
			height = newHeight;
		}
		selfX = (width+1)/2;
		selfY = (height+1)/2;
	}

	
}
