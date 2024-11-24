/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.commands.impl;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.graphicmud.Localization;
import com.graphicmud.commands.ACommand;
import com.graphicmud.commands.CommandGroup;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.network.ClientConnection;
import com.graphicmud.network.ClientConnection.Priority;
import com.graphicmud.player.PlayerCharacter;

/**
 * Created at 03.07.2004, 12:15:20
 *
 * @author Stefan Prelle
 *
 */
public class ShowClientCommand extends ACommand {

	private final static Logger logger = System.getLogger(ShowClientCommand.class.getPackageName());

	//------------------------------------------------
	public ShowClientCommand() {
		super(CommandGroup.BASIC, "showclient", Localization.getI18N());
 	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.commands.Command#execute(com.graphicmud.player.PlayerCharacter, java.util.regex.Matcher)
	 */
	@Override
	public void execute(MUDEntity np, Map<String, Object> params) {
		logger.log(Level.DEBUG, "Execute for {0} with {1}", np.getName(), params);
		if (!(np instanceof PlayerCharacter pc)) {
			np.sendShortText(Priority.IMMEDIATE, "SHOWCLIENT ONLY FOR PLAYERS ATM " + np.getName());
			logger.log(Level.WARNING,"SHOWCLIENT ONLY FOR PLAYERS ATM " + np.getName() );
			return;
		}
		showClient(pc.getConnection(),pc);
		logger.log(Level.DEBUG, "LEAVE execute()");
	}

	//-------------------------------------------------------------------
	public void showClient(ClientConnection con, PlayerCharacter np) {
		StringBuffer out = new StringBuffer();
		out.append("<b><u>MUD Protocols</u></b><br/>");
		for (Entry<String,List<String>> entry : con.getProtocolCapabilities().entrySet()) {
			String feature = entry.getKey();
			boolean present = ! (feature.startsWith("-"));
			if (feature.startsWith("-") || feature.startsWith("+")) feature=feature.substring(1);
			String helpKey = "feature."+feature.toLowerCase();
			String helpTxt = getString(helpKey);
			if (helpTxt.equals(helpKey))
				helpTxt=null;

			if (present) {
			  out.append("<green>[*] "+feature.toUpperCase()+"</green>");
			  if (helpTxt!=null)
				  out.append(" "+helpTxt);
			  out.append("<br/>");
			  // Output features, if there are any
			  if (entry.getValue()!=null && !entry.getValue().isEmpty()) {
				  out.append("&nbsp;&nbsp;&nbsp;&nbsp;"+entry.getValue());
				  out.append("<br/>");
			  }
			} else {
				  out.append("<red>[ ] "+feature.toUpperCase()+"</red>");
				  if (helpTxt!=null)
					  out.append(" "+helpTxt);
				  out.append("<br/>");
			}
		}
		con.sendTextWithMarkup(out.toString());
	}

}
