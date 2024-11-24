/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.map;

public enum Direction {
	N(0,-1),
	NE(1,-1),
	E(1,0),
	SE(1,1),
	S(0,1),
	SW(-1,1),
	W(-1,0),
	NW(-1,-1)
	;
	int x,y;
	Direction(int xv, int yv) {
		x=xv;
		y=yv;
	}
}