/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.world.tile;

import org.prelle.simplepersist.Attribute;

/**
 *
 */
public class GridPosition {

	@Attribute
	private int x;
	@Attribute
	private int y;

	//-------------------------------------------------------------------
	public GridPosition() {
	}
	//-------------------------------------------------------------------
	public GridPosition(GridPosition copyFrom) {
		this.x = copyFrom.x;
		this.y = copyFrom.y;
	}
	//-------------------------------------------------------------------
	public int getX() { return x; }
	public GridPosition setX(int value) { this.x = value; return this;}

	//-------------------------------------------------------------------
	public int getY() { return y; }
	public GridPosition setY(int value) { this.y = value; return this;}

	//-------------------------------------------------------------------
	public String toString() {
		return String.format("%d,%d", x,y);
	}
	public int distance(GridPosition other) {
		int xd = (x>other.x)?(x-other.x):(other.x-x);
		int yd = (y>other.y)?(y-other.y):(other.y-y);
		return (int)Math.ceil( Math.sqrt( xd*xd + yd*yd ) );
	}

}
