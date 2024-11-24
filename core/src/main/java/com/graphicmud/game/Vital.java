/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.game;

/**
 * 
 */
public class Vital {
	
	public static enum VitalType {
		/** Usually Hitpoints or something else describing life */
		VITAL1,
		/** Usually Mana or some other energy */
		VITAL2,
		/** Usually Movement points of some kind */
		VITAL3,
	}
	
	/** Unbuffed healthy maximum of a lifeform */
	public int max;
	/** Amount the maximum is reduced by status effects like illness */
	public int blocked;
	/** Light damage, easily recovered */
	public int exhausted;
	/** Severe damage, needs heal or longer recovery */
	public int damage;
	/** Temporary points thanks to a buff */
	public int temporary;

	//-------------------------------------------------------------------
	public int getCurrent() {
		return max - blocked - damage - exhausted + temporary;
	}
}
