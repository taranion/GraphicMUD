/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.world;

/**
 *
 */
public class DefaultRoomFactory implements LocationFactory<Location> {

	//-------------------------------------------------------------------
	public DefaultRoomFactory() {
		// TODO Auto-generated constructor stub
	}

	//-------------------------------------------------------------------
	/**
	 * @see java.util.function.BiFunction#apply(java.lang.Object, java.lang.Object)
	 */
	@Override
	public Location apply(World world, Zone zone) {
		return new Location();
	}

}
