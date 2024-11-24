/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.network.impl;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import com.graphicmud.handler.LoginHandler;
import com.graphicmud.network.ClientConnection;
import com.graphicmud.network.MUDConnectorListener;

import lombok.Getter;

/**
 *
 */
public class ConnectionManager implements MUDConnectorListener {

	private final static Logger logger = System.getLogger("mud.network");

	@Getter
	private List<ClientConnection> connections;
	private LoginHandler loginHandler;

	//-------------------------------------------------------------------
	public ConnectionManager(LoginHandler loginHandler) {
		this.loginHandler = loginHandler;
		connections = new ArrayList<ClientConnection>();
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.MUDConnectorListener#incomingConnection(com.graphicmud.network.ClientConnection)
	 */
	@Override
	public void incomingConnection(ClientConnection con) {
		logger.log(Level.INFO, "new connection received: "+con);
		connections.add(con);

//		con.pushConnectionListener(loginHandler);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.MUDConnectorListener#closedConnection(com.graphicmud.network.ClientConnection)
	 */
	@Override
	public void closedConnection(ClientConnection con) {
		logger.log(Level.INFO, "closed connection: "+con);
		connections.remove(con);
	}

}
