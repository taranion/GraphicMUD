/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.action.raw;

import com.graphicmud.behavior.Context;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.world.text.Door;
import com.graphicmud.world.text.Door.DoorState;

/**
 * 
 */
public class SetDoorState {
	
	public Door door;

	//-------------------------------------------------------------------
	public SetDoorState() {
	}

	//-------------------------------------------------------------------
	public SetDoorState(Door data) {
		this.door = data;
	}

	//-------------------------------------------------------------------
	public String toString() {
		return "DOOR:"+door;
	}

	//-------------------------------------------------------------------
	public void open(MUDEntity performedBy, Context context) {
		door.setDoorState(DoorState.OPEN);
	}

	//-------------------------------------------------------------------
	public void closed(MUDEntity performedBy, Context context) {
		door.setDoorState(DoorState.CLOSED);
	}

	//-------------------------------------------------------------------
	public void locked(MUDEntity performedBy, Context context) {
		door.setDoorState(DoorState.LOCKED);
	}

}
