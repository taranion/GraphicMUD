/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.map;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 
 */
public interface ViewportMap<T> extends Map2D<T> {

	public default <R> ViewportMap<R> convert(Function<ViewportMap<T>, ViewportMap<R>> mapper) {
		return (ViewportMap<R>) mapper.apply(this);
	}
	
	public default ViewportMap<T> apply(Consumer<ViewportMap<T>> consumer) {
		consumer.accept(this);
		return this;
	}
	
	//-------------------------------------------------------------------
	/**
	 * Reduce the map size to the given values
	 * @param radiusX
	 * @param radiusY
	 */
	public void reduceSize(int radiusX, int radiusY);
	
	public int[] getPositionSelf();
	public ViewportMap<T> setPositionSelf(int x, int y);
	
	public <X> void addLayer(String name, X[][] flood);
}
