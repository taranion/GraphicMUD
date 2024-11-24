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
import com.graphicmud.game.MUDEntity;
import com.graphicmud.network.ClientConnection.Priority;
import com.graphicmud.player.PlayerCharacter;

/**
 * Created at 03.07.2004, 12:15:20
 *
 * @author Stefan Prelle
 *
 */
public class QuitCommand extends ACommand {

	private final static Logger logger = System.getLogger(QuitCommand.class.getPackageName());

	//------------------------------------------------
	public QuitCommand() {
		super(CommandGroup.BASIC, "quit", Localization.getI18N());
 	}

	//-------------------------------------------------------------------
	/**
	 */
	@Override
	public void execute(MUDEntity entity, Map<String, Object> params) {
		logger.log(Level.DEBUG, "ENTER execute()");
		if (!(entity instanceof PlayerCharacter np)) {
			entity.sendShortText(Priority.IMMEDIATE, "QUIT IS ONLY FOR PLAYERS");
			logger.log(Level.WARNING, "QUIT IS ONLY FOR PLAYER CHARS " + entity.getName());
			return;
		}
		PlayerCharacter model = (PlayerCharacter) entity;
		MUD.getInstance().getPlayerDatabase().saveCharacter(model.getConnection().getPlayer(), model);
        MUD.getInstance().getGame().removePlayer(np);
		np.getConnection().logOut();
		logger.log(Level.DEBUG, "LEAVE execute()");
	}

}
