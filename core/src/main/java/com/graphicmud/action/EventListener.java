/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.action;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.graphicmud.action.cooked.ParameterType;
import com.graphicmud.behavior.Context;
import com.graphicmud.behavior.TreeResult;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MUDEvent;
import com.graphicmud.world.Location;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class EventListener {
	
	private MUDEntity onSource;
	private MUDEvent.Type onType;
	private List<BiFunction<MUDEntity, Context, TreeResult>> putOnStack;
	
	
	
	public TreeResult listenInCurrentRoom(MUDEntity performer, Context blackboard) {
		Location loc = blackboard.get(ParameterType.ROOM_CURRENT);
		if (loc==null) {
			return new TreeResult("No Room in context");
		}
		Consumer<MUDEvent> callback = (ev) -> {
			//loc.unregisterEventListener(callback);
		};
		
		//loc.registerEventListener( (ev) -> callback);
		return new TreeResult(true);
	}

}
