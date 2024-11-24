/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud;

/**
 *
 */
public class MUDException extends Exception {

	public static enum Reason {
		NO_FREE_IDENTIFIER
	}

	private Reason reason;
	private Object value;

	//-------------------------------------------------------------------
	public MUDException(Reason reason) {
		this.reason = reason;
	}

	//-------------------------------------------------------------------
	/**
	 * @return the reason
	 */
	public Reason getReason() {
		return reason;
	}

	//-------------------------------------------------------------------
	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

}
