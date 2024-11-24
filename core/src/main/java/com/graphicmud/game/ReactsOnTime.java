/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.game;

/**
 * 
 */
public interface ReactsOnTime {

	//-------------------------------------------------------------------
	/**
	 * Perform PULSE operations
	 */
	public void pulse();
	
	//-------------------------------------------------------------------
	/**
	 * Perform TICK operations
	 */
	public void tick();
	
}
