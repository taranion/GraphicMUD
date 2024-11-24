/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.action;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import com.graphicmud.action.cooked.ParameterType;
import com.graphicmud.behavior.Context;
import com.graphicmud.behavior.TreeResult;
import com.graphicmud.behavior.TreeResult.Result;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MobileEntity;
import com.graphicmud.game.MUDEvent.Type;
import com.graphicmud.world.Location;
import com.graphicmud.world.text.Direction;

public class ActOnTarget {
	
	private final static Logger logger = System.getLogger(ActOnTarget.class.getPackageName());

//	public static void yawn(MUDEntity performedBy) {		
//	}
	
	public static TreeResult follow(MUDEntity performedBy, Context context) {
		logger.log(Level.INFO,"ActOnTarget::follow");
		MobileEntity whoToFollow = context.get(ParameterType.MOBILE);
		logger.log(Level.INFO, "I should follow "+whoToFollow);
		if (whoToFollow==null) {
			// Noone selected to follow
			return new TreeResult(false);
		}
		
		// Is the target still in this room
		Location loc = context.get(ParameterType.ROOM_CURRENT);
		if (loc.getEntities().contains(whoToFollow)) {
			// Yes, no need to follow
			return new TreeResult(Result.FAILURE);
		}
		// No, DO FOLLOW
		// TODO
		//MUDEvent ev = new MUDEvent();
		MUDEventTest test = (actor,ev) -> {
			return ev.getType()==Type.LEAVE_ROOM 
				&&
				ev.getSource()==whoToFollow;
		};
		loc.registerEventListener( (ev) -> {
			if (test.test(performedBy, ev)) {
				Direction dir = ev.getData();
				logger.log(Level.INFO, "Target to follow moved {0}", dir);
//				performedBy.addStackOptions(
//						(new MoveRoomAction(dir))::direction
//						);
			}
		});
		
		
		return new TreeResult(Result.RUNNING);
	}
}
