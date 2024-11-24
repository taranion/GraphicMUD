/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.combat;

import com.graphicmud.game.MUDEntity;

/**
 * 
 */
public interface CombatListener {
	
	//-------------------------------------------------------------------
	/**
	 * E.g. Roll initiatives
	 * @param combat
	 */
	public void prepareCombatants(Combat combat);

	//-------------------------------------------------------------------
	public MUDEntity getNextActor(Combat combat);

	//-------------------------------------------------------------------
	public void performTurn(Combat combat, MUDEntity turn);
	
}
