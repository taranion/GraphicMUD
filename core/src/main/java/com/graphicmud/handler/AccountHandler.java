/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.handler;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import com.graphicmud.Localization;
import com.graphicmud.network.ClientConnection;
import com.graphicmud.network.ClientConnectionListener;
import com.graphicmud.network.MUDClientCapabilities;
import com.graphicmud.network.ClientConnection.Priority;
import com.graphicmud.network.interaction.ActionMenuItem;
import com.graphicmud.network.interaction.Menu;
import com.graphicmud.network.interaction.MenuHandler;

/**
 *
 */
public class AccountHandler extends MenuHandler implements ClientConnectionListener {

	private final static Logger logger = System.getLogger(AccountHandler.class.getPackageName());


	//-------------------------------------------------------------------
	/**
	 */
	public AccountHandler() {
		super(null,null);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.interaction.MenuHandler#enter(com.graphicmud.network.ClientConnection)
	 */
	@Override
	public void enter(ClientConnection con) {
		logger.log(Level.INFO, "ENTER: initialize");

		menu = new Menu(Localization.fillString("menu.account.title", con.getPlayer().getName()));
		menu.setMessage(Localization.getString("menu.account.mess"));
		// Play option
		menu.add( ActionMenuItem.builder()
				.identifier("play")
				.label(Localization.getString("menu.account.play"))
				.emoji("🎲")
				.finallyGoTo(new CharacterSelectorHandler(this))
				.build()
		);
		// Email option
		menu.add( ActionMenuItem.builder()
				.identifier("email")
				.label(Localization.getString("menu.account.email"))
				.emoji("📧")
				.checkIfSelectable(data -> false)
				.build()
		);
		// Interface language
		menu.add( ActionMenuItem.builder()
				.identifier("lang")
				.label(Localization.getString("menu.account.language"))
				.emoji("🇬🇧")
				.checkIfSelectable(data -> false)
				.build()
		);
		// Show client compatibility
		menu.add( ActionMenuItem.builder()
				.identifier("client")
				.label(Localization.getString("menu.account.client"))
				.emoji("🖥️")
				.onActionPerform( (o,v) -> reportClient(con))
				.build()
		);
		// Exit MUD
		menu.add( ActionMenuItem.builder()
				.identifier("leave")
				.label(Localization.getString("menu.account.leave"))
				.emoji("👋")
				.onActionPerform( (o,v) -> closeConnection(con))
				.isExit(true)
				.build()
		);

		super.visualMenu = con.presentMenu(menu);
		logger.log(Level.INFO, "LEAVE: initialize");
	}

	//-------------------------------------------------------------------
	private void closeConnection(ClientConnection con) {
		logger.log(Level.INFO, "Player {0} logging off", con.getPlayer().getName());

		con.sendShortText(Priority.IMMEDIATE, Localization.getString("menu.account.leave.bye")+"\r\n");

		con.logOut();
	}

	//-------------------------------------------------------------------
	private void reportClient(ClientConnection con) {
		MUDClientCapabilities caps = con.getCapabilities();

		int width = 80;
		con.sendTextWithMarkup(caps.toString(con.getLocale(),width)+"<br/>");
		con.presentMenu(menu);
	}

}
