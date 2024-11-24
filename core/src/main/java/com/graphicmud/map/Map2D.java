/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.map;

import java.util.ArrayList;
import java.util.List;

public interface Map2D<T> extends MapPolygon {
	
	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.map.MapPolygon#getSites()
	 */
	default public List<Object> getSites() {
		List<Object> ret = new ArrayList<>();
		for (int y=0; y<getHeight(); y++) {
			for (int x=0; x<getWidth(); x++) {
				ret.add(get(x,y));
			}
		}
		return ret;
	}
	
	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.map.MapPolygon#getNeighbors(java.lang.Object)
	 */
	default public List<Object> getNeighbors(Object site) {
		List<Object> ret = new ArrayList<>();
		return ret;
		
	}

	int getWidth();

	int getHeight();

	T get(int x, int y);

	void set(int x, int y, T val);

}