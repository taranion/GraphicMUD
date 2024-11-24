/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.action.raw;

import com.graphicmud.behavior.Context;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MUDEvent;
import com.graphicmud.world.Location;

/**
 * 
 */
public class FireRoomEvent implements RawAction {
	
	private Location room;
	private MUDEvent event;

	//-------------------------------------------------------------------
	/**
	 */
	public FireRoomEvent(Location room, MUDEvent event) {
		this.room = room;
		this.event= event;
	}

	//-------------------------------------------------------------------
	public String toString() {
		return "FireRoomEvent:"+event+" to "+room.getNr();
	}

	//-------------------------------------------------------------------
	/**
	 * @see java.util.function.BiConsumer#accept(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void accept(MUDEntity performedBy, Context context) {
		room.fireEvent(event);
	}

}
