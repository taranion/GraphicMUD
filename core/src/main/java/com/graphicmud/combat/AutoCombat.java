/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.combat;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Map;

import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.Vital;
import com.graphicmud.game.Vital.VitalType;
import com.graphicmud.player.PlayerCharacter;

import lombok.experimental.SuperBuilder;

/**
 * 
 */
@SuperBuilder
public class AutoCombat extends Combat {
	
	private final static Logger logger = System.getLogger(AutoCombat.class.getPackageName());	
	
	//-------------------------------------------------------------------
	/**
	 * Configure what the next action of a specific combatant will be.
	 * @param combatant
	 * @param action
	 */
	public void setNextAction(MUDEntity combatant, Object action) {
	
	}

	@Override
	public void next() {
		// TODO Auto-generated method stub
		logger.log(Level.DEBUG, "next");
		MUDEntity next = listener.getNextActor(this);
		listener.performTurn(this, next);
		if (next instanceof PlayerCharacter) {
			((PlayerCharacter)next).getConnection().sendPromptWithStats( ((PlayerCharacter)next).getVitals());
		}
	}

}
