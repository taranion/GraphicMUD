/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.network;

/**
 *
 */
public interface ConnectionVariables {

	public final static String VAR_IPADDRESS = "IPAddress";
	public final static String VAR_CLIENT    = "Client";
	public final static String VAR_COLUMNS   = "COLUMNS";
	public final static String VAR_CONNECTION_ID   = "CONNECTION_IDENTIFIER";
	public final static String VAR_GMCP_PACKAGES = "gmcp.supported";
	public final static String VAR_ROWS      = "ROWS";
	public final static String VAR_TERMTYPE  = "TERMTYPE";
	public final static String VAR_TERMSIZE  = "TERMSIZE";
	/** Username provided via Telnet Option */
	public final static String VAR_LOGIN     = "USERNAME";
	public final static String VAR_PASSWORD  = "PASSWORD";
	public final static String VAR_MTT_CAPS  = "MTT_CAPS";
	public final static String VAR_LANG      = "LANG";

}
