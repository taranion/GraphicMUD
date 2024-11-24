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
import com.graphicmud.world.Location;
import com.graphicmud.world.text.Exit;

public class SelectDirection {
	
	private final static Logger logger = System.getLogger(SelectDirection.class.getPackageName());
	
	//-------------------------------------------------------------------
	/**
	 * ECS action that randomly selects an exit to take
	 */
	public static TreeResult randomExit(MUDEntity performedBy, Context context) {
//		logger.log(Level.DEBUG,"Select::playerInRoom");
		// Check if target has already been selected
		if (context.containsKey(ParameterType.DIRECTION))
			return new TreeResult(true);
		
		if (!RoomHelper.getRoomByPosition(performedBy, context).isEmpty())
			return new TreeResult(false);
		
		Location room = context.get(ParameterType.ROOM_CURRENT);
		Exit selected = room.getRoomComponent().get().getExitList().stream()
				.collect(RoomHelper.RANDOM_EXIT);
		if (selected==null) {
			return TreeResult.builder()
					.value(Result.FAILURE)
					.internalErrorMessage("No exits in room "+room)
					.build();
		}
		// Store decision
		context.put(ParameterType.EXIT, selected);
		context.put(ParameterType.DIRECTION, selected.getDirection());
		
		logger.log(Level.DEBUG, "Decided to go "+selected.getDirection()+" to "+selected.getTargetRoom());
		return new TreeResult(Result.SUCCESS);
	}
}
