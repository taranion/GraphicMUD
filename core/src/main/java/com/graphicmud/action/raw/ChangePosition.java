/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.action.raw;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import com.graphicmud.Identifier;
import com.graphicmud.MUD;
import com.graphicmud.action.cooked.ParameterType;
import com.graphicmud.behavior.Context;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.world.Location;
import com.graphicmud.world.NoSuchPositionException;
import com.graphicmud.world.Position;
import com.graphicmud.world.WorldCenter;
import com.graphicmud.world.text.RoomPosition;
import com.graphicmud.world.tile.GridPosition;
import com.graphicmud.world.tile.TileAreaComponent;

/**
 * 
 */
public class ChangePosition implements RawAction {
	
	private final static Logger logger = System.getLogger(ChangePosition.class.getPackageName());
	
	public int roomNr;
	public Identifier roomId;

	//-------------------------------------------------------------------
	public ChangePosition(int roomNr) {
		this.roomNr = roomNr;		
	}

	//-------------------------------------------------------------------
	public ChangePosition(Identifier roomId) {
		this.roomId = roomId;		
	}

	//-------------------------------------------------------------------
	/**
	 * @see java.util.function.BiConsumer#accept(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void accept(MUDEntity performer, Context context) {
		WorldCenter wc = MUD.getInstance().getWorldCenter();
		// Remove from old room
		if (performer.getPosition()!=null && performer.getPosition().getRoomPosition()!=null) {
			Identifier oldRoom = performer.getPosition().getRoomPosition().getRoomNumber();
			try {
				Location oldLoc = wc.getLocation(oldRoom);
				oldLoc.removeEntity(performer);
			} catch (NoSuchPositionException e) {
				logger.log(Level.ERROR, "Unknown position to remove from: {0}",oldRoom);
			}
		}
		Identifier targetRoomId = context.get(ParameterType.IDENTIFIER);
//		if (targetRoomId==null) {
//			if (performer.getPosition()!=null && performer.getPosition().getRoomPosition()!=null) {
//				targetRoomId = new Identifier( (Identifier)context.get(ParameterType.LOCAL_IDENTIFIER));
//				targetRoomId.setLocal(performer.getPosition().getRoomPosition().getRoomNumber());
//				targetRoomId = performer.getPosition().getRoomPosition().getRoomNumber();
//				String targetRoomNr = context.get(ParameterType.IDENTIFIER);
//				targetRoomId.setLocal(targetRoomNr);
//			}
//		}
		
		// Change position
		performer.getPosition().getRoomPosition().setRoomNumber(targetRoomId);
		Position target = wc.createPosition();
		target.setRoomPosition(new RoomPosition(targetRoomId));
		performer.setPosition(target);
		context.put(ParameterType.POSITION_CURRENT, target);
		
		// Add to room
		try {
			Location newLoc = wc.getLocation(targetRoomId);
			newLoc.addEntity(performer);
			context.put(ParameterType.ROOM_CURRENT, newLoc);
			// Copy Grid position, if there is any
			if (newLoc.getTileAreaComponent().isPresent()) {
				TileAreaComponent tile = newLoc.getTileAreaComponent().get();
				GridPosition tileTarget = new GridPosition();
				tileTarget.setX( tile.getCenterX() );
				tileTarget.setY( tile.getCenterY() );
				target.setTilePosition(tileTarget);
			}
		} catch (NoSuchPositionException e) {
			logger.log(Level.ERROR, "Unknown position to add to: {0}",targetRoomId);
		}
	}

}
