/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.combat;

import lombok.experimental.SuperBuilder;

/**
 * Interactive combat that waits (some time) for a player decision
 */
@SuperBuilder
public class TurnBasedCombat extends Combat {

	public void setListener(CombatListener listener) {
		
	}

	@Override
	public void next() {
		// TODO Auto-generated method stub
		
	}
	
}
