/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.network;

/**
 *
 */
public interface MUDConnectorListener {

	//-------------------------------------------------------------------
	/**
	 * A new connection has been established
	 * @param con
	 */
	public void incomingConnection(ClientConnection con);

	//-------------------------------------------------------------------
	public void closedConnection(ClientConnection com);

}
