/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.action.raw;

import com.graphicmud.MUD;
import com.graphicmud.action.cooked.ParameterType;
import com.graphicmud.behavior.Context;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.player.PlayerCharacter;
import com.graphicmud.world.Position;
import com.graphicmud.world.Surrounding;
import com.graphicmud.world.WorldCenter;

/**
 * 
 */
public class SendSurrounding {

	//-------------------------------------------------------------------
	/**
	 */
	public SendSurrounding() {
		// TODO Auto-generated constructor stub
	}

	//-------------------------------------------------------------------
	public static void sendMapAndText(MUDEntity performedBy, Context context) {
		if (performedBy instanceof PlayerCharacter) {
			WorldCenter wc = MUD.getInstance().getWorldCenter();
			Surrounding surr = wc.generateSurrounding(
					(PlayerCharacter) performedBy, 
					(Position)context.get(ParameterType.POSITION_CURRENT));
			((PlayerCharacter)performedBy).getConnection().sendRoom(surr);
		}
	}


}
