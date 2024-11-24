/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.commands.impl;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.graphicmud.Localization;
import com.graphicmud.MUD;
import com.graphicmud.commands.ACommand;
import com.graphicmud.commands.CommandCenter;
import com.graphicmud.commands.CommandGroup;
import com.graphicmud.commands.CommandCenter.CommandName;
import com.graphicmud.game.MUDEntity;

/**
 * @author Stefan Prelle
 *
 */
public class CommandsCommand extends ACommand {

	private final static Logger logger = System.getLogger(CommandsCommand.class.getPackageName());

	//------------------------------------------------
	public CommandsCommand() {
		super(CommandGroup.BASIC, "commands", Localization.getI18N());
 	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.commands.Command#execute(org.prelle.mud.player.PlayerCharacter, java.util.regex.Matcher)
	 */
	@Override
	public void execute(MUDEntity np, Map<String, Object> params) {
		logger.log(Level.DEBUG, "Execute for {0} with {1}", np.getName(), params);
		commands(np);
		logger.log(Level.DEBUG, "LEAVE execute()");
	}

	//-------------------------------------------------------------------
	public void commands(MUDEntity np) {
		Map<CommandGroup, List<String>> commandsByGroup = new HashMap<>();
		// Sort all commands by group
		CommandCenter commMgmt = MUD.getInstance().getCommandManager();
		for (CommandName tmp : commMgmt.getAllCommands(np.getLocale())) {
			logger.log(Level.DEBUG, "Check command "+tmp+" with type "+tmp.getType());
			List<String> list = commandsByGroup.getOrDefault(tmp.getType(), new ArrayList<String>());
			if (!list.contains(tmp))
				list.add(tmp.getName());
			commandsByGroup.put(tmp.getType(), list);
		}
		logger.log(Level.INFO ,"commandsByGroup = {0}", commandsByGroup);

		StringBuffer out = new StringBuffer();
		for (CommandGroup grp : CommandGroup.values()) {
			List<String> list = commandsByGroup.get(grp);
			if (list==null || list.isEmpty())
				continue;
			out.append("<b><u>"+grp.name()+"</u></b></br>");
			Collections.sort(list, new Comparator<String>() {
				public int compare(String c0, String c1) {
					return c0.compareTo(c1);
				}
			});
			int num=0;
			for (String c : list) {
				num++;
				out.append(String.format("%15s    ", c));
				if ( (num%4)==4 ) out.append("</br>");
			}
			out.append("</br>");
		}

		np.sendTextWithMarkup(out.toString());
	}

}
