/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.commands;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.graphicmud.Localization;
import com.graphicmud.MUD;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.player.PlayerCharacter;

/**
 *
 */
public class WhoCommand extends ACommand {

	private final static Logger logger = System.getLogger(WhoCommand.class.getPackageName());

	//-------------------------------------------------------------------
	public WhoCommand() {
		super(CommandGroup.BASIC, "who", Localization.getI18N());
	}

	//-------------------------------------------------------------------
	/**
	 * @see Command#execute(MUDEntity, Map)
	 */
	@Override
	public void execute(MUDEntity np, Map<String, Object> params) {
		logger.log(Level.DEBUG, "Execute for {0} with {1}", np.getName(), params);
		StringBuffer buf = new StringBuffer("<b><u>Players online</b></u><br/>");
		for (PlayerCharacter tmp : getVisiblePlayers(np)) {
			buf.append("* "+tmp.getName()+"<br/>");
		}
		np.sendTextWithMarkup(buf.toString());
		logger.log(Level.DEBUG, "LEAVE execute()");
	}

	//-------------------------------------------------------------------
	public static List<PlayerCharacter> getVisiblePlayers(MUDEntity entity) {
		List<PlayerCharacter> visiblePlayers = new ArrayList<>();
		for (PlayerCharacter tmp : MUD.getInstance().getGame().getPlayers()) {
			// Check visibility
			visiblePlayers.add(tmp);
		}
		return visiblePlayers;
	}

}
