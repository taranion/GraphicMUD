/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.action.cooked;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import com.graphicmud.MUD;
import com.graphicmud.action.RoomHelper;
import com.graphicmud.action.raw.Echo;
import com.graphicmud.action.raw.RawAction;
import com.graphicmud.action.raw.SetDoorState;
import com.graphicmud.behavior.Context;
import com.graphicmud.behavior.TreeResult;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MUDEntityTemplate;
import com.graphicmud.world.Location;
import com.graphicmud.world.NoSuchPositionException;
import com.graphicmud.world.Position;
import com.graphicmud.world.WorldCenter;
import com.graphicmud.world.text.Direction;
import com.graphicmud.world.text.Door;
import com.graphicmud.world.text.Exit;
import com.graphicmud.world.text.RoomPosition;
import com.graphicmud.world.text.Door.DoorState;

public class DoorsAction {
	
	private final static Logger logger = System.getLogger(DoorsAction.class.getPackageName());
	private final static String ALREADY_LOCKED   = "action.door.error.already_locked";
	private final static String ALREADY_UNLOCKED = "action.door.error.already_unlocked";
	private final static String ALREADY_OPEN     = "action.door.error.already_open";
	private final static String ALREADY_CLOSED   = "action.door.error.already_closed";
	private final static String NOT_CLOSED       = "action.door.error.not_closed";
	private final static String MISSING_KEY      = "action.door.error.missing_key";
	private final static String LOCKED           = "action.door.error.locked";
	private final static String LOCK_SELF        = "action.door.lock.self";
	private final static String LOCK_OTHER       = "action.door.lock.other";
	private final static String UNLOCK_SELF      = "action.door.unlock.self";
	private final static String UNLOCK_OTHER     = "action.door.unlock.other";
	private final static String OPEN_SELF        = "action.door.open.self";
	private final static String OPEN_OTHER       = "action.door.open.other";
	private final static String CLOSE_SELF       = "action.door.close.self";
	private final static String CLOSE_OTHER      = "action.door.close.other";
	private final static String OTHER_SIDE_PREFIX= "action.door.other_side.";
	
	//-------------------------------------------------------------------
	public static CookedActionResult genericDoorCheck(MUDEntity performedBy, Context context, Direction dir) {
		if (dir==null)
			return genericDoorCheck(performedBy, context);
		
		// Ensure that we know the room we are in and that a room component exists
		CookedActionResult ret = RoomHelper.getRoomByPosition(performedBy, context);
		if (!ret.isEmpty())
			return ret;
		Location room = context.get(ParameterType.ROOM_CURRENT);
		// Find the exit for the configured direction
		Exit exit = room.getRoomComponent().get().getExit(dir);
		if (exit==null) {
			return new CookedActionResult( (new Echo("action.error.no_exit_in_direction", dir))::sendSelf );
		}
		// Make sure exit is a door
		if (!exit.isDoor()) {
			return new CookedActionResult( (new Echo("action.error.not_a_door", dir))::sendSelf );
		}
		context.put(ParameterType.EXIT, exit);
		return new CookedActionResult();
	}
	
	//-------------------------------------------------------------------
	public static CookedActionResult genericDoorCheck(MUDEntity performedBy, Context context) {
		// Ensure that we know the room we are in and that a room component exists
		CookedActionResult ret = RoomHelper.getRoomByPosition(performedBy, context);
		if (!ret.isEmpty())
			return ret;
		Location room = context.get(ParameterType.ROOM_CURRENT);
		Direction dir = context.get(ParameterType.DIRECTION);
		// Find the exit for the configured direction
		Exit exit = room.getRoomComponent().get().getExit(dir);
		if (exit==null) {
			return new CookedActionResult( (new Echo("action.error.no_exit_in_direction", dir))::sendSelf );
		}
		// Make sure exit is a door
		if (!exit.isDoor()) {
			return new CookedActionResult( (new Echo("action.error.not_a_door", dir))::sendSelf );
		}
		context.put(ParameterType.EXIT, exit);
		
		// Reverse direction
		WorldCenter wc = MUD.getInstance().getWorldCenter();
		Position targetPos = wc.createPosition();
		targetPos.setRoomPosition(new RoomPosition(exit.getTargetRoom()));
		try {
			context.put(ParameterType.ROOM_TARGET, wc.getLocation(targetPos));
		} catch (NoSuchPositionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Exit reverseExit = wc.getOppositeExit(performedBy.getPosition(), dir, targetPos);
		context.put(ParameterType.EXIT_TARGET, reverseExit);
		
		return new CookedActionResult((String)null);
	}
	
	//-------------------------------------------------------------------
	public static CookedActionResult lock(MUDEntity performedBy, Context context) {
		logger.log(Level.DEBUG, "ENTER: lock");
		try {
			Direction dir = context.get(ParameterType.DIRECTION);
			// Ensure that we know the room we are in and that a room component exists
			CookedActionResult ret = genericDoorCheck(performedBy, context, dir);
			if (!ret.isEmpty())
				return ret;
			// Find the exit for the configured direction
			Exit exit = context.get(ParameterType.EXIT);
			Door door = exit.getDoorComponent();
			// Make sure door is not locked yet
			if (door.getDoorState()==DoorState.LOCKED) {
				return new CookedActionResult( (new Echo(ALREADY_LOCKED))::sendSelf );
			}
			// Make sure door is closed
			if (door.getDoorState()==DoorState.OPEN) {
				return new CookedActionResult( (new Echo(NOT_CLOSED))::sendSelf );
			}

			WorldCenter wc = MUD.getInstance().getWorldCenter();
			// Make sure we have the key
			MUDEntityTemplate keyTemplate = wc.getItemTemplate(door.getKeyReference());
			if (!performedBy.isInInventory(keyTemplate)) {
				return new CookedActionResult( (new Echo(MISSING_KEY))::sendSelf );
			}
			
			/*
			 * All checks done - no create actions
			 */
			List<RawAction> raw = new ArrayList<>();
			Exit reverseExit = context.get(ParameterType.EXIT_TARGET);
			Door reverseDoor = (reverseExit!=null)?reverseExit.getDoorComponent():null;

			raw.add( (new SetDoorState(door))::locked );
			raw.add( (new Echo(LOCK_SELF))::sendSelf );
			raw.add( (new Echo(LOCK_OTHER, performedBy.getName(), dir))::sendOthers );
			if (reverseDoor!=null) 
				raw.add( (new SetDoorState(reverseDoor))::locked );
			if (reverseExit!=null) 
				raw.add( (new Echo(OTHER_SIDE_PREFIX+".lock", reverseExit.getDirection()))::sendOthers );
			return new CookedActionResult("lock",raw, dir.name());
		} finally {
			logger.log(Level.DEBUG, "LEAVE: lock");
		}
	}
	
	//-------------------------------------------------------------------
	public static CookedActionResult unlock(MUDEntity performedBy, Context context) {
		logger.log(Level.DEBUG, "ENTER: unlock");
		try {
			Direction dir = context.get(ParameterType.DIRECTION);
			// Ensure that we know the room we are in and that a room component exists
			CookedActionResult ret = genericDoorCheck(performedBy, context, dir);
			if (!ret.isEmpty())
				return ret;
			// Find the exit for the configured direction
			Exit exit = context.get(ParameterType.EXIT);
			Door door = exit.getDoorComponent();
			// Make sure door is not unlocked yet
			if (door.getDoorState()!=DoorState.LOCKED) {
				return new CookedActionResult( (new Echo(ALREADY_UNLOCKED))::sendSelf );
			}

			WorldCenter wc = MUD.getInstance().getWorldCenter();
			// Make sure we have the key
			MUDEntityTemplate keyTemplate = wc.getItemTemplate(door.getKeyReference());
			if (!performedBy.isInInventory(keyTemplate)) {
				return new CookedActionResult( (new Echo(MISSING_KEY))::sendSelf );
			}
			
			/*
			 * All checks done - no create actions
			 */
			List<RawAction> raw = new ArrayList<>();
			Exit reverseExit = context.get(ParameterType.EXIT_TARGET);
			Door reverseDoor = (reverseExit!=null)?reverseExit.getDoorComponent():null;

			raw.add( (new SetDoorState(door))::closed );
			raw.add( (new Echo(UNLOCK_SELF))::sendSelf );
			raw.add( (new Echo(UNLOCK_OTHER, performedBy.getName(), dir))::sendOthers );
			if (reverseDoor!=null) 
				raw.add( (new SetDoorState(reverseDoor))::closed );
			if (reverseExit!=null) 
				raw.add( (new Echo(OTHER_SIDE_PREFIX+".unlock", reverseExit.getDirection()))::sendOthers );
			return new CookedActionResult("unlock",raw, dir.name());
		} finally {
			logger.log(Level.DEBUG, "LEAVE: lock");
		}
	}
	
	//-------------------------------------------------------------------
	public static CookedActionResult close(MUDEntity performedBy, Context context) {
		logger.log(Level.DEBUG, "ENTER: close");
		try {
			Direction dir = context.get(ParameterType.DIRECTION);
			// Ensure that we know the room we are in and that a room component exists
			CookedActionResult ret = genericDoorCheck(performedBy, context, dir);
			if (!ret.isEmpty())
				return ret;
			// Find the exit for the configured direction
			Exit exit = context.get(ParameterType.EXIT);
			Door door = exit.getDoorComponent();
			// Make sure door is not closed yet
			if (door.getDoorState()!=DoorState.OPEN) {
				return new CookedActionResult( (new Echo(ALREADY_CLOSED))::sendSelf );
			}
			
			/*
			 * All checks done - no create actions
			 */
			List<RawAction> raw = new ArrayList<>();
			Exit reverseExit = context.get(ParameterType.EXIT_TARGET);
			Door reverseDoor = (reverseExit!=null)?reverseExit.getDoorComponent():null;

			raw.add( (new SetDoorState(door))::closed );
			raw.add( (new Echo(CLOSE_SELF))::sendSelf );
			raw.add( (new Echo(CLOSE_OTHER, performedBy.getName(), dir))::sendOthers );
			if (reverseDoor!=null) 
				raw.add( (new SetDoorState(reverseDoor))::closed );
			if (reverseExit!=null) 
				raw.add( (new Echo(OTHER_SIDE_PREFIX+".close", reverseExit.getDirection()))::sendOthers );
			return new CookedActionResult("close",raw, dir.name());
		} finally {
			logger.log(Level.DEBUG, "LEAVE: close");
		}
	}
	
	//-------------------------------------------------------------------
	public static CookedActionResult open(MUDEntity performedBy, Context context) {
		logger.log(Level.DEBUG, "ENTER open");
		try {
			Direction dir = context.get(ParameterType.DIRECTION);
			// Ensure that we know the room we are in and that a room component exists
			CookedActionResult ret = genericDoorCheck(performedBy, context, dir);
			if (!ret.isEmpty())
				return ret;
			// Find the exit for the configured direction
			Exit exit = context.get(ParameterType.EXIT);
			Door door = exit.getDoorComponent();
			// Make sure door is not locked 
			if (door.getDoorState()==DoorState.LOCKED) {
				return new CookedActionResult( (new Echo(LOCKED))::sendSelf );
			// Make sure door is closed
			}
			// Make sure door is not open yet
			if (door.getDoorState()==DoorState.OPEN) {
				return new CookedActionResult( (new Echo(ALREADY_OPEN))::sendSelf );
			}
			
			/*
			 * All checks done - no create actions
			 */
			List<RawAction> raw = new ArrayList<>();
			Exit reverseExit = context.get(ParameterType.EXIT_TARGET);
			Door reverseDoor = (reverseExit!=null)?reverseExit.getDoorComponent():null;

			raw.add( (new SetDoorState(door))::open );
			raw.add( (new Echo(OPEN_SELF))::sendSelf );
			raw.add( (new Echo(OPEN_OTHER, performedBy.getName(), dir))::sendOthers );
			if (reverseDoor!=null) 
				raw.add( (new SetDoorState(reverseDoor))::locked );
			if (reverseExit!=null) 
				raw.add( (new Echo(OTHER_SIDE_PREFIX+".open", reverseExit.getDirection()))::sendOthers );
			return new CookedActionResult("open",raw, dir.name());
		} finally {
			logger.log(Level.DEBUG, "LEAVE: open");
		}
	}
	
	//-------------------------------------------------------------------
	public static TreeResult lockAll(MUDEntity performedBy, Context context) {
		return null;
	}
	
}
