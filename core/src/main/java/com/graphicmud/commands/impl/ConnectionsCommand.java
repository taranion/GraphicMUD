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
import com.graphicmud.network.ClientConnection;
import com.graphicmud.network.ClientConnection.Priority;
import com.graphicmud.network.impl.ConnectionManager;
import com.graphicmud.player.PlayerCharacter;

/**
 * Created at 03.07.2004, 12:15:20
 *
 * @author Stefan Prelle
 *
 */
public class ConnectionsCommand extends ACommand {

	private final static Logger logger = System.getLogger(ConnectionsCommand.class.getPackageName());

	//------------------------------------------------
	public ConnectionsCommand() {
		super(CommandGroup.BASIC, "connections", Localization.getI18N());
 	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.commands.Command#execute(MUDEntity, Map)
	 */
	@Override
	public void execute(MUDEntity np, Map<String, Object> params) {
		logger.log(Level.DEBUG, "ENTER execute()");
		if (!(np instanceof PlayerCharacter pc)) {
			np.sendShortText(Priority.IMMEDIATE, "CONNECTIONS-CMD ONLY FOR PLAYERS ATM " + np.getName());
			logger.log(Level.WARNING,"CONNECTIONS-CMD ONLY FOR PLAYERS ATM " + np.getName() );
			return;
		}
		ConnectionManager conMan = MUD.getInstance().getConnectionManager();
		StringBuffer mess = new StringBuffer();

		mess.append(String.format("%15s %6s %15s %14s %11s %s\r\n",
				"Account",
				"Proto",
				"IP-Address",
				"Client",
				"State",
				"Capabilities"
				));
		mess.append("===================================================================================\r\n");
		for (ClientConnection tmp : conMan.getConnections()) {
			mess.append(String.format("%15s %6s %15s %14s %11s %s\r\n",
					tmp.getAccount(),
					tmp.getConnector().getName(),
					tmp.getNetworkId(),
					tmp.getClient(),
					tmp.getState(),
					tmp.getCapabilityString()
					));
		}

		pc.getConnection().sendScreen(mess.toString());
		logger.log(Level.DEBUG, "LEAVE execute()");
	}

}
