/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.olc.commands;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Map;

import com.graphicmud.Localization;
import com.graphicmud.commands.ACommand;
import com.graphicmud.commands.CommandGroup;
import com.graphicmud.game.MUDEntity;

/**
 * Created at 03.07.2004, 12:15:20
 *
 * @author Stefan Prelle
 *
 */
public class OLCCreateCommand extends ACommand {

	private final static Logger logger = System.getLogger(OLCCreateCommand.class.getPackageName());

	//------------------------------------------------
	public OLCCreateCommand() {
		super(CommandGroup.OLC, "create", Localization.getI18N());
 	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.commands.Command#execute(org.prelle.mud.player.PlayerCharacter, java.util.regex.Matcher)
	 */
	@Override
	public void execute(MUDEntity np, Map<String, Object> params) {
		logger.log(Level.DEBUG, "Execute for {0} with {1}", np.getName(), params);
		logger.log(Level.DEBUG, "LEAVE execute()");
	}


	
}
