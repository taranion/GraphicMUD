/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.channels;

import java.io.*;

/**
 * Somehow this is a general channel exception.
 *
 * @author Stefan Prelle  <prelle@tzi.de>
 * @version $Id: ChannelException.java,v 1.1 2004/07/04 08:58:20 prelle Exp $
 */

public class ChannelException extends IOException {

	private static final long serialVersionUID = 5817125624945162267L;
	
	public final static int CHANNEL_IS_CLOSED        = 0;
	public final static int CHANNEL_IS_NOT_AVAILABLE = 1;
	public final static int CHANNEL_MUTED            = 2;
	public final static int EXCLUDED_FROM_CHANNEL    = 3;
	public final static int DONT_HAVE_THAT_CHANNEL   = 4;


	private int channelID;
	private int exceptionType;

	//---------------------------------------------------------------
	public ChannelException(int id, int type) {
		channelID = id;
		exceptionType = type;
	}

	//---------------------------------------------------------------
	public int getChannelID() {
		return channelID;
	}

	//---------------------------------------------------------------
	public int getType() {
		return exceptionType;
	}

} // ChannelStateException
