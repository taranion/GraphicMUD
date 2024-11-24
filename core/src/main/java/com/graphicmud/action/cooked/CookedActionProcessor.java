/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.action.cooked;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import com.graphicmud.Identifier;
import com.graphicmud.MUD;
import com.graphicmud.action.RoomHelper;
import com.graphicmud.action.raw.RawAction;
import com.graphicmud.behavior.Context;
import com.graphicmud.behavior.TreeResult;
import com.graphicmud.behavior.TreeResult.Result;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.world.Location;
import com.graphicmud.world.NoSuchPositionException;
import com.graphicmud.world.Position;
import com.graphicmud.world.WorldCenter;

/**
 * 
 */
public class CookedActionProcessor {

	private final static Logger logger = System.getLogger(CookedActionProcessor.class.getPackageName());
	
	//-------------------------------------------------------------------
	public static TreeResult perform(CookedAction cooked, MUDEntity performer, Context context) {
		logger.log(Level.DEBUG, "ENTER: perform({0})", cooked);
		WorldCenter wc = MUD.getInstance().getWorldCenter();
		Position pos = performer.getPosition();
		context.put(ParameterType.COOKED_ACTION, cooked);
		context.put(ParameterType.POSITION_CURRENT, pos);
		if (pos.getRoomPosition()!=null) {
			Identifier currentRoomId = pos.getRoomPosition().getRoomNumber();
			context.put(ParameterType.LOCAL_IDENTIFIER, currentRoomId);
			try {
				Location loc = wc.getLocation(currentRoomId);
				if (loc!=null) {
					context.put(ParameterType.ROOM_CURRENT, loc);
				}
			} catch (NoSuchPositionException e) {
				logger.log(Level.WARNING, "Unknown location",e);
			}
		}
		
		CookedActionResult toExecute = cooked.apply(performer, context);
		logger.log(Level.DEBUG, "Going to execute {0}:{1}", toExecute.getActionID(), toExecute.getParameter());
		boolean mayContinue = RoomHelper.preActionTest(performer, context, toExecute.getActionID(), toExecute.getParameter());
		logger.log(Level.INFO, "may continue = "+mayContinue);
		if (!mayContinue) {
			// Script prevents further execution
			return new TreeResult(Result.FAILURE);
		}
		
		for (RawAction act : toExecute) {
			if (logger.isLoggable(Level.DEBUG)) {
				logger.log(Level.DEBUG, "raw action: {0}", act.getNameExpensive());
			}
			try {
				act.accept(performer, context);
			} catch (Exception e) {
				logger.log(Level.ERROR, "Error performing "+act.getClass().getSimpleName(),e);
			}
		}

		logger.log(Level.DEBUG, "LEAVE: perform({0})", cooked);
		if (toExecute.isSuccessful()) {
			return new TreeResult(Result.SUCCESS);
		} else {
			return new TreeResult(Result.FAILURE);
		}
	}

	//-------------------------------------------------------------------
	public static TreeResult performAsTreeResult(CookedActionResult checkedActions, MUDEntity performer, Context context) {
		logger.log(Level.DEBUG, "ENTER: performAsTreeResult");
		for (RawAction act : checkedActions) {
			if (logger.isLoggable(Level.DEBUG)) {
				logger.log(Level.DEBUG, "raw action: {0}", act.getNameExpensive());
			}
			try {
				act.accept(performer, context);
			} catch (Exception e) {
				logger.log(Level.ERROR, "Error performing "+act.getClass().getSimpleName(),e);
			}
		}

		logger.log(Level.DEBUG, "LEAVE: performAsTreeResult");
		if (checkedActions.isSuccessful()) {
			return new TreeResult(Result.SUCCESS);
		} else {
			return new TreeResult(Result.FAILURE);
		}
	}
	
//	//-------------------------------------------------------------------
//	private static List<RawAction> convertToActions(CookedAction action, List<CookedActionTest> tests, MUDEntity entity, Context context) {
//		for (CookedActionTest test : tests) {
//			try {
//				CookedActionResult res = test.apply(entity, context);
//				if (!res.isSuccessful()) {
//					logger.log(Level.DEBUG, "CookedAction test failed for {0} executing {1} with {2}", entity, action, res.getErrorMessage());
//					String msg = (res.getErrorMessage()!=null)?res.getErrorMessage():"Unknown error in CookedActionProcessor "+test;
//					return List.of((new Echo(msg))::sendSelf);
//				}
//			} catch (Exception e) {
//				logger.log(Level.ERROR, "Exception in CookedAction "+test,e);
//				return List.of((new Echo("Internal error"))::sendSelf);
//			}
//		}
//		return action.apply(entity, context);
//	}

}
