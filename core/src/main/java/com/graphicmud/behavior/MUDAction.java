/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.behavior;

import java.util.function.BiFunction;

import com.graphicmud.game.MUDEntity;

/**
 * 
 */
public interface MUDAction extends BiFunction<MUDEntity, Context, TreeResult> {

	//-------------------------------------------------------------------
	/**
	 * Check if the situation allows executing this operation
	 */
	public boolean conditionsMet(MUDEntity actor, Context context);
	
}
