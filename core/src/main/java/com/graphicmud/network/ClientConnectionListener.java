/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.network;

import java.util.List;
import java.util.Map;

import com.graphicmud.network.interaction.Form;

/**
 *
 */
public interface ClientConnectionListener {

	//-------------------------------------------------------------------
	/**
	 * Called when the handler is called for the first time
	 * @param con
	 */
	public default void enter(ClientConnection con) {
		con.pushConnectionListener(this);
	}

	//-------------------------------------------------------------------
	/**
	 * Called when the handler is called for the first time
	 * @param con
	 */
	public default void leave(ClientConnection con) {
		con.popConnectionListener(null);
	}

	//-------------------------------------------------------------------
	/**
	 * The connector detected major changes in the connection that require
	 * the screen content to be redrawn (e.g. UI, room description, map ...
	 */
	public void performScreenRefresh(ClientConnection con);

	//-------------------------------------------------------------------
	public void reenter(ClientConnection con, Object result);

	//-------------------------------------------------------------------
	public default void onVariableChange(ClientConnection con, String variable) {

	}

	//-------------------------------------------------------------------
	/**
	 * A player typed some input via the client
	 * @param con  Client connection
	 * @param input Typed input
	 */
	public void receivedInput(ClientConnection con, String input);

	//-------------------------------------------------------------------
	public void receivedKeyCode(ClientConnection con, int code, List<Integer> arguments);

	//-------------------------------------------------------------------
	public void receivedFormResponse(ClientConnection con, Form form, Map<String,String> answers);

	//-------------------------------------------------------------------
	/**
	 * The connection to the client has been lost
	 */
	public default void connectionLost(ClientConnection con) {}

}
