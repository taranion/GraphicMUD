/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.commands.impl;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Locale;
import java.util.Map;

import com.graphicmud.Localization;
import com.graphicmud.MUD;
import com.graphicmud.action.cooked.CookedActionProcessor;
import com.graphicmud.action.cooked.ParameterType;
import com.graphicmud.action.cooked.Walk;
import com.graphicmud.behavior.Context;
import com.graphicmud.behavior.TreeResult;
import com.graphicmud.behavior.TreeResult.Result;
import com.graphicmud.commands.ACommand;
import com.graphicmud.commands.CommandGroup;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MUDEvent;
import com.graphicmud.network.ClientConnection.Priority;
import com.graphicmud.player.PlayerCharacter;
import com.graphicmud.world.Location;
import com.graphicmud.world.NoSuchPositionException;
import com.graphicmud.world.Position;
import com.graphicmud.world.Range;
import com.graphicmud.world.Surrounding;
import com.graphicmud.world.text.Direction;
import com.graphicmud.world.text.Exit;
import com.graphicmud.world.text.RoomComponent;
import com.graphicmud.world.text.RoomPosition;
import com.graphicmud.world.text.TextWorldCenter;
import com.graphicmud.world.text.Door.DoorState;
import com.graphicmud.world.tile.GridPosition;
import com.graphicmud.world.tile.TileAreaComponent;

/**
 * Created at 03.07.2004, 12:15:20
 *
 * @author Stefan Prelle
 *
 */
public class Movement extends ACommand {

	private final static Logger logger = System.getLogger(Movement.class.getPackageName());

	//------------------------------------------------
	public Movement() {
//        List<String> variantIDs = new ArrayList<String>(List.of(Variant.values()).stream().map(v -> v.name().toLowerCase()).toList());
		super(CommandGroup.MOVEMENT, "movement", Localization.getI18N());
 	}

	//---------------------------------------------------------------
	static void sendSameRoomExceptSelf(MUDEntity self, String mess) {
		for (PlayerCharacter life : MUD.getInstance().getWorldCenter().getPlayersInRangeExceptSelf(self, Range.SURROUNDING)) {
			((PlayerCharacter)life).getConnection().sendShortText(Priority.UNIMPORTANT, mess);
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.commands.Command#execute(MUDEntity, Map)
	 */
	@Override
	public void execute(MUDEntity np, Map<String, Object> params) {
		logger.log(Level.INFO, "Execute for {0} with {1}", np.getName(), params);
		Direction direction = (Direction)params.get("dir");
		if (direction==null) {
			logger.log(Level.ERROR, "Movement command called without direction parameter");
			np.sendShortText(Priority.IMMEDIATE, "Parsing error - missing direction");
			return;
		}
		logger.log(Level.INFO, "Player {1} wants to go to next room in direction {0}", direction, np.getName());

		Context context = new Context();
		context.put(ParameterType.DIRECTION, direction);
		CookedActionProcessor.perform(new Walk(direction), np, context);
	}

}
