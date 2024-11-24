/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.dialog;

/**
 * 
 */
public class DialogAction {
	
	/** Refers to the list of NPCs you are talking to, where 0 is the person the dialog was started with */
	protected int speaker;

	//-------------------------------------------------------------------
	/**
	 *  Refers to the list of NPCs you are talking to, where 0 is the person the dialog was started with
	 * @return
	 */
	public int getSpeaker() {
		return speaker;
	}
}
