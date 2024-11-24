/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.commands.impl;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Map;

import com.graphicmud.Localization;
import com.graphicmud.MUD;
import com.graphicmud.commands.ACommand;
import com.graphicmud.commands.CommandGroup;
import com.graphicmud.commands.CommandUtil;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MUDEvent;
import com.graphicmud.game.MUDEvent.Type;
import com.graphicmud.network.ClientConnection.Priority;
import com.graphicmud.world.Location;
import com.graphicmud.world.NoSuchPositionException;
import com.graphicmud.world.text.RoomPosition;
import com.graphicmud.world.text.TextWorldCenter;

/**
 * Talk to a mobile (or other MUDEntity)
 * @author Stefan Prelle
 *
 */
public class TalkCommand extends ACommand {

	private final static Logger logger = System.getLogger(TalkCommand.class.getPackageName());

	//------------------------------------------------
	public TalkCommand() {
		super(CommandGroup.INTERACT, "talk", Localization.getI18N());
 	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.commands.Command#execute(MUDEntity, Map)
	 */
	@Override
	public void execute(MUDEntity np, Map<String, Object> params) {
		logger.log(Level.DEBUG, "Execute for {0} with {1}", np.getName(), params);
		String targetName = (String)params.get("target");
		logger.log(Level.DEBUG, "target is "+targetName);
		if (targetName==null) {
			np.sendShortText(Priority.IMMEDIATE, getString("mess.noTarget"));
			return;
		}

		TextWorldCenter wCenter = (TextWorldCenter) MUD.getInstance().getWorldCenter();

		// Get current room
		RoomPosition currentPos = np.getPosition().getRoomPosition();
		Location loc = null;
		MUDEntity target = null;
		try {
			loc = wCenter.getLocation(currentPos.getRoomNumber());
			target = CommandUtil.getMobileFromPosition(targetName, np.getPosition());
		} catch (NoSuchPositionException e) {
			np.sendShortText(Priority.IMMEDIATE, fillString("mess.inInvalidRoom",np.getLocale(),currentPos.getRoomNumber()));
			return;
		}
		if (target==null) {
			np.sendShortText(Priority.IMMEDIATE, fillString("mess.invalidTarget",np.getLocale(),targetName));
			return;			
		}
		logger.log(Level.DEBUG, "Send TALK_REQUEST event");
		target.receiveEvent(new MUDEvent(Type.TALK_REQUEST, np, null));
	}

}
