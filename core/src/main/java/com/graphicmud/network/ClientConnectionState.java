/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.network;

/**
 *
 */
public enum ClientConnectionState {

	/**
	 * A user from a network transmitting a reliable identifier has connected
	 * but there is no connection to an account yet.
	 */
	NEW_OR_OLD,
	CREATE_LOGIN,
	CREATE_PASSWORD,
	ENTER_LOGIN,
	UNTRUSTED_ENTER_PASSWORD,
	NEW_ACCOUNT_PASSWORD,
	CREATE_VERIFY_PASSWORD,
	LOGGED_IN,
	DISCONNECTED,
	IN_MENU

}
