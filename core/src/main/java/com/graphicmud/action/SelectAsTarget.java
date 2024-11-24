/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.action;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;

import com.graphicmud.action.cooked.ParameterType;
import com.graphicmud.behavior.BehaviorRunner;
import com.graphicmud.behavior.Context;
import com.graphicmud.behavior.TreeResult;
import com.graphicmud.behavior.TreeResult.Result;
import com.graphicmud.game.EntityType;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.world.Location;

public class SelectAsTarget {
	
	private final static Logger logger = System.getLogger(SelectAsTarget.class.getPackageName());

//	public static void yawn(MUDEntity performedBy) {		
//	}
	
	//-------------------------------------------------------------------
	public static TreeResult playerInRoom(MUDEntity performedBy, Context context) {
//		logger.log(Level.DEBUG,"Select::playerInRoom");
		// Check if target has already been selected
		if (context.containsKey(ParameterType.MOBILE))
			return new TreeResult(true);
		if (!RoomHelper.getRoomByPosition(performedBy, context).isEmpty())
			return new TreeResult(false);
		
		Location room = context.get(ParameterType.ROOM_CURRENT);
		MUDEntity selected = room.getEntities().stream()
			.filter(ent -> ent.getType()==EntityType.PLAYER)
			.collect(RoomHelper.RANDOM_ENTITY);
		// Was any player selected?
		if (selected==null) {
			return new TreeResult(false);
		}
		logger.log(Level.INFO, "Selected "+selected.getName());
		// Store decision
		context.put(ParameterType.MOBILE, selected);
		context.put(ParameterType.TARGET, selected);
		
		return new TreeResult(Result.SUCCESS);
	}
}
