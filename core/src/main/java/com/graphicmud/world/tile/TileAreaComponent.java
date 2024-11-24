/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.world.tile;

import org.prelle.simplepersist.Attribute;

/**
 *
 */
public class TileAreaComponent {

	@Attribute
	public int x;
	@Attribute
	public int y;
	@Attribute
	public int w;
	@Attribute
	public int h;

	//-------------------------------------------------------------------
	public TileAreaComponent(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	//-------------------------------------------------------------------
	public int getStartX() {
		return x;
	}

	//-------------------------------------------------------------------
	public int getStartY() {
		return y;
	}

	//-------------------------------------------------------------------
	public int getWidth() {
		return w;
	}

	//-------------------------------------------------------------------
	public int getHeight() {
		return h;
	}

	//-------------------------------------------------------------------
	public int getCenterX() { return x + w/2; }
	public int getCenterY() { return y + h/2; }

	public int getEndX() { return x + w -1; }
	public int getEndY() { return y + h -1; }

}
