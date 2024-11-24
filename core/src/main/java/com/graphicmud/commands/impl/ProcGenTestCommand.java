/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.commands.impl;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Map;
import java.util.function.Supplier;

import com.graphicmud.Localization;
import com.graphicmud.commands.ACommand;
import com.graphicmud.commands.CommandGroup;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.map.GridMap;
import com.graphicmud.map.ViewportMap;
import com.graphicmud.network.ClientConnection.Priority;
import com.graphicmud.player.PlayerCharacter;
import com.graphicmud.symbol.Symbol;
import com.graphicmud.world.impl.ProcGen;

/**
 * Created at 03.07.2004, 12:15:20
 *
 * @author Stefan Prelle
 *
 */
public class ProcGenTestCommand extends ACommand {

	private final static Logger logger = System.getLogger(ProcGenTestCommand.class.getPackageName());

	//------------------------------------------------
	public ProcGenTestCommand() {
		super(CommandGroup.INTERACT, "procgen", Localization.getI18N());
 	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.commands.Command#execute(com.graphicmud.player.PlayerCharacter, java.util.regex.Matcher)
	 */
	@Override
	public void execute(MUDEntity np, Map<String, Object> params) {
		logger.log(Level.DEBUG, "Execute for {0} with {1}", np.getName(), params);
		if (!(np instanceof PlayerCharacter pc)) {
			np.sendShortText(Priority.IMMEDIATE, "PROCGENTEST ONLY FOR PLAYERS ATM " + np.getName());
			logger.log(Level.WARNING,"PROCGENTEST ONLY FOR PLAYERS ATM " + np.getName() );
			return;
		}
		
		Supplier<GridMap> creator = new ProcGen(np.hashCode(), 55, 30, 60, 11, 77);
		GridMap baseGrid = creator.get();
		
		ViewportMap<Symbol> map = baseGrid.getArea(baseGrid.getWidth()/2, baseGrid.getHeight()/2, 1, 10,10, 1);
		pc.getConnection().sendMap(map);
		logger.log(Level.DEBUG, "LEAVE execute()");
	}


	
}
