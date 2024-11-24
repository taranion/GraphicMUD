/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.action.raw;

import com.graphicmud.behavior.Context;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MUDEvent;

/**
 * 
 */
public class FireEntityEvent implements RawAction {
	
	private MUDEntity target;
	private MUDEvent event;

	//-------------------------------------------------------------------
	/**
	 */
	public FireEntityEvent(MUDEntity entity, MUDEvent event) {
		this.target = entity;
		this.event= event;
	}

	//-------------------------------------------------------------------
	public String toString() {
		return "FireEntityEvent:"+event+" for "+target;
	}

	//-------------------------------------------------------------------
	/**
	 * @see java.util.function.BiConsumer#accept(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void accept(MUDEntity performedBy, Context context) {
		target.fireEvent(event);
	}

}
