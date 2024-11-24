/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.commands.impl;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Map;

import com.graphicmud.Identifier;
import com.graphicmud.Localization;
import com.graphicmud.MUD;
import com.graphicmud.commands.ACommand;
import com.graphicmud.commands.CommandGroup;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.network.ClientConnection.Priority;
import com.graphicmud.player.PlayerCharacter;
import com.graphicmud.world.Location;
import com.graphicmud.world.NoSuchPositionException;
import com.graphicmud.world.Position;
import com.graphicmud.world.Surrounding;
import com.graphicmud.world.text.RoomComponent;
import com.graphicmud.world.text.RoomPosition;
import com.graphicmud.world.text.TextWorldCenter;
import com.graphicmud.world.tile.GridPosition;
import com.graphicmud.world.tile.TileAreaComponent;

/**
 * @author Stefan Prelle
 *
 */
public class GoToCommand extends ACommand {

	private final static Logger logger = System.getLogger(GoToCommand.class.getPackageName());

	//------------------------------------------------
	public GoToCommand() {
		super(CommandGroup.ADMIN, "goto", Localization.getI18N());
 	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.commands.Command#execute(MUDEntity, Map)
	 */
	@Override
	public void execute(MUDEntity np, Map<String, Object> params) {
		logger.log(Level.DEBUG, "Execute for {0} with {1}", np.getName(), params);
		if (!(np instanceof PlayerCharacter pc)) {
			np.sendShortText(Priority.IMMEDIATE, "GOTO ONLY FOR PLAYERS ATM " + np.getName());
			logger.log(Level.WARNING,"GOTO ONLY FOR PLAYERS ATM " + np.getName() );
			return;
		}
		String roomNr_s = (String)params.get("room");
		if (roomNr_s==null) {
			pc.sendShortText(Priority.IMMEDIATE, super.getI18N("error.missing_room_nr", pc.getLocale()));
			return;
		}
		logger.log(Level.INFO, "Player {1} wants to jump into room {0}", roomNr_s, pc.getName());

		TextWorldCenter wCenter = (TextWorldCenter) MUD.getInstance().getWorldCenter();
		Position target = wCenter.createPosition();
		Identifier roomNr = new Identifier(roomNr_s);
		target.setRoomPosition(new RoomPosition(roomNr));

		// Get current room
		RoomPosition currentPos = pc.getPosition().getRoomPosition();
		Location loc = null;
		try {
			if (currentPos!=null)
				loc = wCenter.getLocation(currentPos.getRoomNumber());
		} catch (NoSuchPositionException e) {
			pc.sendShortText(Priority.IMMEDIATE, fillString("mess.inInvalidRoom",pc.getLocale(),currentPos.getRoomNumber()));
			return;
		}
		// Since the room is obtained by room ID, the current room must have
		// a room component - no need to check
		RoomComponent room = loc.getRoomComponent().get();

		Location newLoc = null;
		try {
			newLoc = wCenter.getLocation(roomNr);
			logger.log(Level.INFO, "Room number {0}/{1} resolves to {2}", roomNr_s, roomNr, newLoc);
			// Copy Grid position, if there is any
			if (newLoc.getTileAreaComponent().isPresent()) {
				TileAreaComponent tile = newLoc.getTileAreaComponent().get();
				GridPosition tileTarget = new GridPosition();
				tileTarget.setX( tile.getCenterX() );
				tileTarget.setY( tile.getCenterY() );
				target.setTilePosition(tileTarget);
			}
			Surrounding room2 = wCenter.generateSurrounding(pc,target);
			// Send messages that the player leaves the room
//			// .. to the player
//			pc.getConnection().sendShortText(fillString("mess.youLeave",exitDirName)+"\r\n");
//			// .. to the other players in the room
//			sendSameRoomExceptSelf(pc, fillString("mess.youLeave",exitDirName)+"\r\n");

			// Change room
			loc.removeEntity(pc);
			newLoc.addEntity(pc);
			pc.setPosition(target);
			pc.getConnection().sendRoom(room2);
			// Inform players in new room of the arrival
			Movement.sendSameRoomExceptSelf(pc, fillString("mess.playerArrives", pc.getName())+"\r\n");
		} catch (Exception e) {
			pc.getConnection().sendShortText(Priority.IMMEDIATE, "Bad things happened!\r\n");
			logger.log(Level.ERROR, "Error while moving",e);
			return;
		}

	}

}
