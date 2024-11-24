/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.action;

import com.graphicmud.behavior.Context;
import com.graphicmud.behavior.MUDAction;
import com.graphicmud.behavior.TreeResult;
import com.graphicmud.game.MUDEntity;

/**
 * Action to
 */
public class MoveToRoom {
	
	private int targetRoomNr;

	public MoveToRoom(int target) {
		this.targetRoomNr = target;
	}
		
	
	public TreeResult find(MUDEntity performedBy, Context context) {
		return null;
	}

}
