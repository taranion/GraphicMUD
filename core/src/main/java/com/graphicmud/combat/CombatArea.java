/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.combat;

import java.util.List;

import com.graphicmud.game.MUDEntity;

/**
 * 
 */
public interface CombatArea {
	
	public List<MUDEntity> getTargetsInRange(MUDEntity attacker);

}
