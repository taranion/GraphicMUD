/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.game;

import com.graphicmud.behavior.Context;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 */
@Setter
@AllArgsConstructor
public class MUDEvent {
	
	public static enum Type {
		COMMAND_ISSUED,
		/** 
		 * The source of the event enters the current room.
		 * Sent to rooms.
		 */
		ENTER_ROOM,
		/** Sent to an entity that is being attacked */
		HOSTILE_ACTION,
		INVENTORY_CHANGED,
		/** The source of the event left the room */
		LEAVE_ROOM,
//		RCV_ATTACKED,
		/** The target of the event was the receiver of a social */
		RCV_SOCIAL,
		/** The source of the event has performed a social */
		SOCIAL_PERFORMED,
		/** A player issues a talk request against an entity. Send against the entity.  */
		TALK_REQUEST,
	}

	@Getter
	private Type type;
	@Getter
	private MUDEntity source;
	private Object data;

	//-------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	public <E> E getData() {
		return (E)data;
	}

	//-------------------------------------------------------------------
	public String toString() {
		return type+":"+data;
	}
	
	
	public static MUDEvent when(MUDEvent ev, Context ctx) {
		return ev;
	}
}
