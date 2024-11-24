/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.game;

import java.util.function.Consumer;

/**
 * 
 */
public interface Customizable<Z> {
	
	//-------------------------------------------------------------------
	/**
	 * Add code that gets executed on every pulse.
	 * @param hook
	 */
	public void addPulseHook(Consumer<Z> hook);
	
	//-------------------------------------------------------------------
	/**
	 * Add code that gets executed on every tick.
	 * @param hook
	 */
	public void addTickHook(Consumer<Z> hook);
	
}
