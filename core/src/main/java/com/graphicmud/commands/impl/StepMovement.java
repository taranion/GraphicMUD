/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.commands.impl;

import java.lang.System.Logger.Level;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.graphicmud.Localization;
import com.graphicmud.MUD;
import com.graphicmud.action.ActionUtil;
import com.graphicmud.commands.ACommand;
import com.graphicmud.commands.CommandGroup;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MUDEvent;
import com.graphicmud.game.MobileEntity;
import com.graphicmud.io.text.TextUtil;
import com.graphicmud.map.GridMap;
import com.graphicmud.network.ClientConnection;
import com.graphicmud.network.ClientConnection.Priority;
import com.graphicmud.player.PlayerCharacter;
import com.graphicmud.world.Location;
import com.graphicmud.world.NoSuchPositionException;
import com.graphicmud.world.Position;
import com.graphicmud.world.Range;
import com.graphicmud.world.WorldCenter;
import com.graphicmud.world.Zone;
import com.graphicmud.world.impl.SurroundingImpl;
import com.graphicmud.world.text.Direction;
import com.graphicmud.world.text.RoomPosition;
import com.graphicmud.world.tile.GridPosition;

/**
 *
 */
public class StepMovement extends ACommand {
	
	private final static String DOOR_CLOSED = "action.walk.error.door_closed";
	private final static String LEAVE_SELF  = "action.walk.self";
	private final static String LEAVE_OTHER = "action.walk.leave.other";
	private final static String ENTER_OTHER = "action.walk.enter.other";
	private final static String TARGET_UNKNOWN = "action.walk.error.target_unknown";
	private final static String NOT_WHILE_FIGHTING = "action.walk.error.not_while_fighting";

	//-------------------------------------------------------------------
	/**
	 * @param type
	 * @param name
	 */
	public StepMovement() {
		super(CommandGroup.MOVEMENT, "stepmove", Localization.getI18N());
		// TODO Auto-generated constructor stub
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.commands.Command#execute(MUDEntity, Map)
	 */
	@Override
	public void execute(MUDEntity np, Map<String, Object> params) {
		// TODO Auto-generated method stub

	}

	public static void step(ClientConnection con, PlayerCharacter np, Direction dir) {
		Position oldPos = con.getCharacter().getPosition();
		WorldCenter wCenter = MUD.getInstance().getWorldCenter();
		Zone zone = wCenter.getZone(oldPos.getZoneNumber());
		GridMap map = null;
		if (zone!=null)
			map = zone.getCachedMap();
		

		GridPosition gridPos = oldPos.getTilePosition();
		// Determine old room (may be null)
		Location oldRoom = null;
		for (Location loc : wCenter.getLocations(oldPos)) {
			if (loc.getRoomComponent().isPresent()) {
				oldRoom = loc;
			}
		}

//		GridPosition newGridPos = new GridPosition();
//		newGridPos.setX(gridPos.getX());
//		newGridPos.setY(gridPos.getY());
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

		logger.log(Level.INFO, "Step to {0}", gridPos);

		// Determine room by grid position
		Location newRoom = null;
		for (Location loc : wCenter.getLocations(oldPos)) {
			if (loc.getRoomComponent().isPresent()) {
				newRoom = loc;
			}
		}
		SurroundingImpl surround = null;
		Locale loc = null;
		surround = (SurroundingImpl) wCenter.generateSurrounding(np,oldPos); 
		if (newRoom==null) {
			// The new position is between rooms
			if (oldRoom!=null) {
				logger.log(Level.INFO, "Leaving a room -----------------------");
				informOldRoom(dir, wCenter, np, oldRoom);
				oldPos.setRoomPosition(null);
				surround.setDescription(List.of());
				surround.setTitle(List.of());
				surround.setMoodImage(null);
				surround.setDirections(List.of());
				con.sendRoom(surround);
			} else {
				logger.log(Level.INFO, "Already not in a room -----------------------");
				con.sendMap(surround.getMap());
			}
		} else {
			// The new position is in a room
			if (oldRoom==null) {
				logger.log(Level.INFO, "Entering a room -----------------------");
				oldPos.setRoomPosition(new RoomPosition(newRoom.getNr()));
				surround = (SurroundingImpl) wCenter.generateSurrounding(np,oldPos); 
				con.sendRoom(surround);
				// Inform new room
				Direction incDirection = dir.getOpposite();
				for (MobileEntity other : wCenter.getPlayersInRangeExceptSelf(np, Range.SURROUNDING)) {
					loc = ActionUtil.getLocale(other);
					other.sendShortText(Priority.UNIMPORTANT, Localization.fillString(ENTER_OTHER, loc, TextUtil.capitalize(np.getName()), incDirection.getName(loc)));
				}
				newRoom.fireEvent(new MUDEvent(MUDEvent.Type.ENTER_ROOM, np, incDirection));
			} else if (oldRoom==newRoom) {
				logger.log(Level.INFO, "Still in same room --------------------");
				con.sendMap(surround.getMap());
			} else {
				logger.log(Level.INFO, "Changing the room ---------------------");
				oldRoom.fireEvent(new MUDEvent(MUDEvent.Type.LEAVE_ROOM, np, dir));
				oldPos.setRoomPosition(new RoomPosition(newRoom.getNr()));
				surround = (SurroundingImpl) wCenter.generateSurrounding(np,oldPos); 
				con.sendRoom(surround);
				informNewRoom(dir.getOpposite(), wCenter, np, newRoom);
			}
		}
	}
	
	private static void informNewRoom(Direction dir, WorldCenter wCenter, MobileEntity np, Location newRoom) {
		// Inform new room
		Direction incDirection = dir.getOpposite();
		for (MobileEntity other : wCenter.getPlayersInRangeExceptSelf(np, Range.SURROUNDING)) {
			Locale loc = ActionUtil.getLocale(other);
			other.sendShortText(Priority.UNIMPORTANT, Localization.fillString(ENTER_OTHER, loc, TextUtil.capitalize(np.getName()), incDirection.getName(loc)));
		}
		newRoom.fireEvent(new MUDEvent(MUDEvent.Type.ENTER_ROOM, np, incDirection));
		newRoom.addEntity(np);
	}
	
	private static void informOldRoom(Direction dir, WorldCenter wCenter, MobileEntity np, Location oldRoom) {
		// Inform old room
		for (MobileEntity other : wCenter.getPlayersInRangeExceptSelf(np, Range.SURROUNDING)) {
			Locale loc = ActionUtil.getLocale(other);
			other.sendShortText(Priority.UNIMPORTANT, Localization.fillString(LEAVE_OTHER, loc, TextUtil.capitalize(np.getName()), dir.getName(loc)));
		}
		oldRoom.fireEvent(new MUDEvent(MUDEvent.Type.LEAVE_ROOM, np, dir));
		oldRoom.removeEntity(np);
	}
	
	
}
