/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.network;

import java.io.IOException;

/**
 * Represents e.g. Telnet access or a Discord bot access
 */
public interface MUDConnector {

	public final static String PROTO_TELNET = "telnet";
	public final static String PROTO_DISCORD = "discord";
	public final static String PROTO_TELEGRAM = "telegram";
	public final static String PROTO_WEBSOCKET = "websocket";

	//-------------------------------------------------------------------
	public String getProtocolIdentifier();

	//-------------------------------------------------------------------
	public String getName();

	//-------------------------------------------------------------------
	/**
	 * Start receiving connections
	 */
	public void start(MUDConnectorListener listener) throws IOException;

	//-------------------------------------------------------------------
	/**
	 * Stop receiving connections
	 */
	public void stop();
}
