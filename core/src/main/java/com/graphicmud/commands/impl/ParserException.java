/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.commands.impl;

/**
 * This exception is thrown from MUD.Basics.Parser
 *
 * @version 1.0, 16.02.1998
 * @author  Stefan Prelle   <prelle@informatik.uni-bremen.de>
 * @since   V1.0
 */

public class ParserException extends Exception {
	
	private static final long serialVersionUID = 700578313780003985L;
	
	int position;
	String param;
	
	//------------------------------------------------
	public ParserException(String txt, int pos, String par) {
		super(txt);
		position = pos;
		param = par;
	}
	
	//------------------------------------------------
	public int getPosition() {
		return position;
	}
	
	//------------------------------------------------
	public String getParameter() {
		return param;
	}
	
}
