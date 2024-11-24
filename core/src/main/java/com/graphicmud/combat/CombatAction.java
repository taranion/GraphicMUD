/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.combat;

import com.graphicmud.behavior.Context;
import com.graphicmud.behavior.MUDAction;
import com.graphicmud.behavior.TreeResult;
import com.graphicmud.game.MUDEntity;

/**
 * 
 */
public interface CombatAction extends MUDAction {

	public int getSuccessProbability(MUDEntity target);

}
