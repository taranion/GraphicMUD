/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.channels;

import java.io.*;

/**
 * This exception is thrown when you try to access a undefined 
 * or unknown channel.
 *
 * @author Stefan Prelle  <Stefan.Prelle@epost.de>
 * @version $Id: NoSuchChannelException.java,v 1.1 2004/07/04 08:58:20 prelle Exp $
 */

public class NoSuchChannelException extends IOException {

	private static final long serialVersionUID = 3099851224554413420L;

	private int channelID;

	//---------------------------------------------------------------
	public NoSuchChannelException(int id) {
		super("Unknown channel: "+id);
		channelID = id;
	}

	//---------------------------------------------------------------
	public NoSuchChannelException(int id, String mess) {
		super(mess);
		channelID = id;
	}

	//---------------------------------------------------------------
	public int getChannelID() {
		return channelID;
	}

} // NoSuchChannelException
