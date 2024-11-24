/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.action;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collector;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.graphicmud.Localization;
import com.graphicmud.MUD;
import com.graphicmud.action.cooked.CookedActionResult;
import com.graphicmud.action.cooked.ParameterType;
import com.graphicmud.action.raw.Echo;
import com.graphicmud.action.script.EventProcessor;
import com.graphicmud.action.script.IfScriptAction;
import com.graphicmud.action.script.OnEvent;
import com.graphicmud.action.script.OnEventJS;
import com.graphicmud.action.script.OnEventXML;
import com.graphicmud.action.script.XMLScriptAction;
import com.graphicmud.behavior.Context;
import com.graphicmud.behavior.RandomEntityCollector;
import com.graphicmud.behavior.RandomExitCollector;
import com.graphicmud.behavior.TreeResult;
import com.graphicmud.behavior.TreeResult.Result;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MobileEntity;
import com.graphicmud.game.MUDEvent.Type;
import com.graphicmud.world.Location;
import com.graphicmud.world.NoSuchPositionException;
import com.graphicmud.world.Position;
import com.graphicmud.world.WorldCenter;
import com.graphicmud.world.text.Direction;
import com.graphicmud.world.text.Exit;
import com.graphicmud.world.text.RoomPosition;

/**
 * 
 */
public class RoomHelper {
	
	private final static Logger logger = System.getLogger(RoomHelper.class.getPackageName());
	
	private final static Random RANDOM = new Random();
	public static final Collector<MUDEntity, List<MUDEntity>, MUDEntity> RANDOM_ENTITY = new RandomEntityCollector();
	public static final Collector<Exit, List<Exit>, Exit> RANDOM_EXIT = new RandomExitCollector();

	//-------------------------------------------------------------------
	public static CookedActionResult getRoomByPosition(MUDEntity performedBy, Context params) {
		// Is the entity at a known position
		Location room = null;
		try {
			room = MUD.getInstance().getWorldCenter().getLocation(performedBy.getPosition());
		} catch (NoSuchPositionException e) {
			return new CookedActionResult((new Echo("action.error.invalid_position", performedBy.getPosition()))::sendSelf );
		}
		if (room==null) {
			return new CookedActionResult((new Echo("action.error.invalid_position", performedBy.getPosition()))::sendSelf );
		}
		// Was the room found
		// Is the given position a room?
		if (room.getRoomComponent().isEmpty()) {
			return new CookedActionResult((new Echo("action.error.position_is_not_a_room", performedBy.getPosition()))::sendSelf );
		}
		params.put(ParameterType.ROOM_CURRENT, room);
		return new CookedActionResult();
	}

	//-------------------------------------------------------------------
	public static CookedActionResult getRoomAndExitByPosition(MUDEntity performedBy, Context params) {
		CookedActionResult check1 = getRoomByPosition(performedBy, params);
		if (!check1.isSuccessful()) return check1;
		Location room = params.get(ParameterType.ROOM_CURRENT);
		
		// Find the exit for the configured direction
		Direction dir = params.get(ParameterType.DIRECTION);
		if (dir==null) {
			return new CookedActionResult( (new Echo("action.error.no_direction_given", performedBy.getPosition()))::sendSelf );
		}
		Exit exit = room.getRoomComponent().get().getExit(dir);
		if (exit==null) {
			return new CookedActionResult( (new Echo("action.error.no_exit_in_direction", dir))::sendSelf );
		}
		params.put(ParameterType.EXIT, exit);
		return new CookedActionResult();
	}
	
	//-------------------------------------------------------------------
	/**
	 * @param performedBy
	 * @param params
	 * @return
	 */
	public static TreeResult validateTarget(MUDEntity performedBy, Context params) {
		if (performedBy.getPosition()==null) {
			return new TreeResult("Entity without location");
		}
		Locale loc = ActionUtil.getLocale(performedBy);
		try {
			Location room = MUD.getInstance().getWorldCenter().getLocation(performedBy.getPosition());
			MUDEntity target = null;
			if (params.containsKey(ParameterType.TARGET)) {
				target = params.get(ParameterType.TARGET);
				if (!room.getEntities().contains(target)) {
					return TreeResult.builder()
							.value(Result.FAILURE)
							.errorMessage(Localization.fillString("action.error.not_in_same_room", loc, target.getName()))
							.build();
				}
			} else if (params.containsKey(ParameterType.TARGET_NAME)) {
				String name = params.get(ParameterType.TARGET_NAME);
				for (MUDEntity ent : room.getEntities()) {
					if (ent.reactsOnKeyword(name)) {
						target=ent;
					}
				}
				if (target==null) {
					return TreeResult.builder()
							.value(Result.FAILURE)
							.errorMessage(Localization.fillString("action.error.not_in_same_room", loc, name))
							.build();					
				}
			} else {
				return TreeResult.builder()
						.value(Result.FAILURE)
						.errorMessage(Localization.fillString("action.error.target_missing", loc))
						.build();									
			}
			// Target exists and is in same room
			return new TreeResult(true);
		} catch (NoSuchPositionException e) {
			return new TreeResult("Performing entity is at unknown position "+performedBy.getPosition());
		}
	}
	
	//-------------------------------------------------------------------
	/**
	 * @return TRUE to continue execution
	 */
	public static boolean preActionTest(MUDEntity entity, Context context, String actionID, String parameter) {
		logger.log(Level.WARNING, "TODO: check if command is possible\n"+context);
		boolean continueAction = true;
		
		try {
			Location loc = context.get(ParameterType.ROOM_CURRENT);
			if (loc!=null) {
		        for (OnEvent onEv : loc.getEventHandlers()) {
		        	continueAction = EventProcessor.process(onEv, entity, context, actionID, parameter);
		        	if (!continueAction)
		        		break;
		        }
			}
			
			return continueAction;
		} finally {
			logger.log(Level.DEBUG, "LEAVE preActionTest() with {0}", continueAction);
		}
	}

	//-------------------------------------------------------------------
	public static Exit getReverseExit(Position currentPos, Exit exit) {
		WorldCenter wc = MUD.getInstance().getWorldCenter();
		Position targetPos = wc.createPosition();
		targetPos.setRoomPosition(new RoomPosition(exit.getTargetRoom()));
		return wc.getOppositeExit(currentPos, exit.getDirection(), targetPos);
	}
}
