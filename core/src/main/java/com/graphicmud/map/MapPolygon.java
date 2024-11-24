/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.map;

import java.util.List;
import java.util.function.Consumer;

/**
 * 
 */
public interface MapPolygon {

	public List<Object> getSites();
	
	public List<Object> getNeighbors(Object site);
	
	default public MapPolygon perform(Consumer<MapPolygon> toPerform) {
		toPerform.accept(this);
		return this;
	}
}
