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
import com.graphicmud.map.GridMap;
import com.graphicmud.symbol.Symbol;
import com.graphicmud.world.Location;
import com.graphicmud.world.NoSuchPositionException;
import com.graphicmud.world.Position;
import com.graphicmud.world.WorldCenter;
import com.graphicmud.world.Zone;
import com.graphicmud.world.text.Direction;
import com.graphicmud.world.text.Door;
import com.graphicmud.world.text.Exit;
import com.graphicmud.world.text.RoomPosition;
import com.graphicmud.world.text.Door.DoorState;
import com.graphicmud.world.tile.GridPosition;

/**
 * Action to walk (or hover) into a direction
 */
public class Step implements CookedAction {
	
	private final static Logger logger = System.getLogger(Step.class.getPackageName());

	private final static String ERROR = "action.tilepos.error";
	private final static String LOOK_INVALID_POSITION = "action.tilepos.look.invalid_position";
	private final static String LOOK_SELF = "action.tilepos.look.self";
	
	private Direction direction;

	//-------------------------------------------------------------------
	public Step(Direction dir) {
		this.direction = dir;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.action.cooked.CookedAction#getId()
	 */
	@Override
	public String getId() {
		return "step";
	}
	
	//-------------------------------------------------------------------
	public static TreeResult selectedDirection(MUDEntity performedBy, Context context) {
		Direction direction = context.get(ParameterType.DIRECTION);
		CookedActionProcessor.perform(new Step(direction), performedBy, context);
		return TreeResult.builder().value(Result.SUCCESS).build();
	}
	
	private static GridPosition getModifiedPosition(GridPosition from, Direction dir, GridMap map) {
		GridPosition gridPos = new GridPosition(from);
		switch (dir) {
		case WEST:
			if (gridPos.getX()>0)
				gridPos.setX(gridPos.getX()-1);
			else return null;
			break;
		case EAST:
			if (gridPos.getX()<map.getWidth())
				gridPos.setX(gridPos.getX()+1);
			else return null;
			break;
		case NORTH:
			if (gridPos.getY()>0)
				gridPos.setY(gridPos.getY()-1);
			else return null;
			break;
		case SOUTH:
			if (gridPos.getY()<map.getHeight())
				gridPos.setY(gridPos.getY()+1);
			else return null;
			break;
		default:
			logger.log(Level.ERROR, "Grid Movement for UP/DOWN not implemented");
			return null;
		}
		return gridPos;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.action.cooked.CookedAction#apply(com.graphicmud.game.MUDEntity, com.graphicmud.behavior.Context)
	 */
	@Override
	public CookedActionResult apply(MUDEntity performedBy, Context context) {
		logger.log(Level.DEBUG, "ENTER: apply");
		/*
		 * Make some checks
		 */
		try {
			CookedActionResult test = RoomHelper.getRoomByPosition(performedBy, context);
			if (!test.isSuccessful()) return test;

			Position oldPos = performedBy.getPosition();

			// Does the zone have a map?
			WorldCenter wCenter = MUD.getInstance().getWorldCenter();
			Zone zone = wCenter.getZone(oldPos.getZoneNumber());
			GridMap map = null;
			if (zone!=null)
				map = zone.getCachedMap();
			else {
				// No map to move on
				return new CookedActionResult("No map in zone "+oldPos.getZoneNumber());
			}
			
			Direction dir = context.get(ParameterType.DIRECTION);
			
			GridPosition gridPos = oldPos.getTilePosition();
			// Determine old room (may be null)
			Location oldRoom = context.get(ParameterType.ROOM_CURRENT);
			
			logger.log(Level.INFO, "Step from {0} ", gridPos);
			switch (dir) {
			case WEST:
				if (gridPos.getX()>0)
					gridPos.setX(gridPos.getX()-1);
				break;
			case EAST:
				if (gridPos.getX()<map.getWidth())
					gridPos.setX(gridPos.getX()+1);
				break;
			case NORTH:
				if (gridPos.getY()>0)
					gridPos.setY(gridPos.getY()-1);
				break;
			case SOUTH:
				if (gridPos.getY()<map.getHeight())
					gridPos.setY(gridPos.getY()+1);
				break;
			}

		} finally {
			logger.log(Level.DEBUG, "LEAVE: apply");
		}
		
		
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
			return new CookedActionResult(Walk.NOT_WHILE_FIGHTING, direction);
		}
		if (exit.isDoor()) {
			Door door = exit.getDoorComponent();
			// Make sure door is open
			if (door.getDoorState()!=DoorState.OPEN) {
				return new CookedActionResult(Walk.DOOR_CLOSED, direction);
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
				raw.add( (new Echo(Walk.LEAVE_SELF, direction))::sendSelf );
				
			// Inform old room
			raw.add( (new Echo(Walk.LEAVE_OTHER, capitalize(performedBy.getName()), direction))::sendOthers);
			raw.add(new FireRoomEvent(oldRoom, new MUDEvent(MUDEvent.Type.LEAVE_ROOM, performedBy, direction)));

			// Move the entity and send new room description to player
			raw.add(new ChangePosition(newRoom.getNr()));
			raw.add(SendSurrounding::sendMapAndText);
			
			// Inform new room
			Exit enterFrom = wCenter.getOppositeExit(performedBy.getPosition(), direction, target);
			if (enterFrom!=null)
				raw.add( (new Echo(Walk.ENTER_OTHER, capitalize(performedBy.getName()), enterFrom.getDirection()))::sendOthers);
			raw.add(new FireRoomEvent(newRoom, new MUDEvent(MUDEvent.Type.ENTER_ROOM, performedBy, enterFrom)));

			return new CookedActionResult("walk",raw, exit.getDirection().name());
		} finally {
			logger.log(Level.DEBUG, "LEAVE: apply with {0} raw actions", raw.size());
		}
	}
	
	public CookedActionResult lookInDirection(MUDEntity performedBy, Context context) {
		logger.log(Level.DEBUG, "ENTER: lookInDirection");
		List<RawAction> raw = new ArrayList<>();
		try {
			Position oldPos = performedBy.getPosition();

			// Does the zone have a map?
			WorldCenter wCenter = MUD.getInstance().getWorldCenter();
			Zone zone = wCenter.getZone(oldPos.getZoneNumber());
			// Make sure we are in a zone at all
			logger.log(Level.DEBUG, "1");
			if (zone==null) 
				return new CookedActionResult("Not in a zone!!? WTF!!", oldPos);
			// Make sure the zone has a tilemap
			GridMap map = zone.getCachedMap();
			logger.log(Level.DEBUG, "2");
			if (map==null) {
				// No map to move on
				return new CookedActionResult("No map in zone "+oldPos.getZoneNumber());
			}
			
			// Determine the target position
			GridPosition targetPos = getModifiedPosition(oldPos.getTilePosition(), direction, map);
			logger.log(Level.DEBUG, "3");
			if (targetPos==null) {
				return new CookedActionResult( (new Echo(LOOK_INVALID_POSITION))::sendSelf );
			}
			
			Symbol symbol = map.getSymbolAt(targetPos);
			logger.log(Level.DEBUG, "4");
			if (symbol==null) {
				return new CookedActionResult( (new Echo(ERROR))::sendSelf );
			}
			
			logger.log(Level.DEBUG, "5");
			raw.add( (new Echo(LOOK_SELF, direction, symbol.getTitle()))::sendSelf);
			return new CookedActionResult("tileLook",raw, direction.name());
		} finally {
			logger.log(Level.DEBUG, "LEAVE: lookInDirection produces {0} raw actions", raw.size());
		}
	}


}
