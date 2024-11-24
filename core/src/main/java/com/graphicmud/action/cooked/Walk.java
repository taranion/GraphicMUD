/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.action.cooked;

import static com.graphicmud.io.text.TextUtil.capitalize;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import com.graphicmud.MUD;
import com.graphicmud.action.RoomHelper;
import com.graphicmud.action.raw.ChangePosition;
import com.graphicmud.action.raw.Echo;
import com.graphicmud.action.raw.FireRoomEvent;
import com.graphicmud.action.raw.RawAction;
import com.graphicmud.action.raw.SendSurrounding;
import com.graphicmud.behavior.Context;
import com.graphicmud.behavior.TreeResult;
import com.graphicmud.behavior.TreeResult.Result;
import com.graphicmud.game.EntityState;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MUDEvent;
import com.graphicmud.world.Location;
import com.graphicmud.world.NoSuchPositionException;
import com.graphicmud.world.Position;
import com.graphicmud.world.WorldCenter;
import com.graphicmud.world.text.Direction;
import com.graphicmud.world.text.Door;
import com.graphicmud.world.text.Exit;
import com.graphicmud.world.text.RoomPosition;
import com.graphicmud.world.text.Door.DoorState;

/**
 * Action to walk (or hover) into a direction
 */
public class Walk implements CookedAction {
	
	private final static Logger logger = System.getLogger(Walk.class.getPackageName());

	public final static String DOOR_CLOSED = "action.walk.error.door_closed";
	public final static String LEAVE_SELF  = "action.walk.self";
	public final static String LEAVE_OTHER = "action.walk.leave.other";
	public final static String ENTER_OTHER = "action.walk.enter.other";
	public final static String NOT_WHILE_FIGHTING = "action.walk.error.not_while_fighting";
	
	private Direction direction;

	//-------------------------------------------------------------------
	public Walk(Direction dir) {
		this.direction = dir;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.action.cooked.CookedAction#getId()
	 */
	@Override
	public String getId() {
		return "walk";
	}
	
	//-------------------------------------------------------------------
	public static TreeResult selectedDirection(MUDEntity performedBy, Context context) {
		Direction direction = context.get(ParameterType.DIRECTION);
		CookedActionProcessor.perform(new Walk(direction), performedBy, context);
		return TreeResult.builder().value(Result.SUCCESS).build();
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.action.cooked.CookedAction#apply(com.graphicmud.game.MUDEntity, com.graphicmud.behavior.Context)
	 */
	@Override
	public CookedActionResult apply(MUDEntity performedBy, Context context) {
		logger.log(Level.DEBUG, "ENTER: apply");
		CookedActionResult test = RoomHelper.getRoomAndExitByPosition(performedBy, context);
		if (!test.isSuccessful()) return test;
		logger.log(Level.DEBUG, "context="+context);

		List<RawAction> raw = new ArrayList<>();
		Exit  exit = context.get(ParameterType.EXIT);
		Location oldRoom = context.get(ParameterType.ROOM_CURRENT);
		Location newRoom = null;
		// Check ROOM_TARGET
		// Check target room
		WorldCenter wCenter = MUD.getInstance().getWorldCenter();
		Position target = wCenter.createPosition();
		target.setRoomPosition(new RoomPosition(exit.getTargetRoom()));
		context.put(ParameterType.POSITION_TARGET, target);
		try {
			newRoom = wCenter.getLocation(exit.getTargetRoom()); 
			if (newRoom==null) {
				logger.log(Level.ERROR, "Room {0}, exit {1} leads into unknown room {2}", oldRoom.getNr(), exit.getDirection(), exit.getTargetRoom());
				return new CookedActionResult("Zone error - this leads (in)to nothing.");
			}
			context.put(ParameterType.ROOM_TARGET, newRoom);
			context.put(ParameterType.IDENTIFIER, newRoom.getNr());
			context.put(ParameterType.POSITION_TARGET, target);
		} catch (NoSuchPositionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Make sure our state allows walking away
		if (performedBy.getState()==EntityState.FIGHTING) {
			return new CookedActionResult(NOT_WHILE_FIGHTING, direction);
		}
		if (exit.isDoor()) {
			Door door = exit.getDoorComponent();
			// Make sure door is open
			if (door.getDoorState()!=DoorState.OPEN) {
				return new CookedActionResult(DOOR_CLOSED, direction);
			}
		}

		/* 
		 * All checks have been done - now prepare the raw actions to execute
		 */		
		try {
			logger.log(Level.INFO, "{0} moves from {1} to {2}", performedBy.getName(), oldRoom.getNr(), newRoom.getNr());
		
			// Inform the player
			if (exit.getPassDescription()!=null) {
				raw.add( (new Echo(exit.getPassDescription()))::sendSelf );
			} else
				raw.add( (new Echo(LEAVE_SELF, direction))::sendSelf );
				
			// Inform old room
			raw.add( (new Echo(LEAVE_OTHER, capitalize(performedBy.getName()), direction))::sendOthers);
			raw.add(new FireRoomEvent(oldRoom, new MUDEvent(MUDEvent.Type.LEAVE_ROOM, performedBy, direction)));

			// Move the entity and send new room description to player
			raw.add(new ChangePosition(newRoom.getNr()));
			raw.add(SendSurrounding::sendMapAndText);
			
			// Inform new room
			Exit enterFrom = wCenter.getOppositeExit(performedBy.getPosition(), direction, target);
			if (enterFrom!=null)
				raw.add( (new Echo(ENTER_OTHER, capitalize(performedBy.getName()), enterFrom.getDirection()))::sendOthers);
			raw.add(new FireRoomEvent(newRoom, new MUDEvent(MUDEvent.Type.ENTER_ROOM, performedBy, enterFrom)));

			return new CookedActionResult("walk",raw, exit.getDirection().name());
		} finally {
			logger.log(Level.DEBUG, "LEAVE: apply with {0} raw actions", raw.size());
		}
	}

}
