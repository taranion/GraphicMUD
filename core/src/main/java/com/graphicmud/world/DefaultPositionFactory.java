/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.world;

/**
 *
 */
public class DefaultPositionFactory implements PositionFactory<Position> {

	//-------------------------------------------------------------------
	@Override
	public Position get() {
		return new Position();
	}

}
