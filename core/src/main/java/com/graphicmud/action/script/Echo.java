/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.action.script;

import org.prelle.simplepersist.Attribute;
import org.prelle.simplepersist.CData;

import com.graphicmud.action.raw.RawAction;
import com.graphicmud.behavior.Context;
import com.graphicmud.game.MUDEntity;

import lombok.Getter;

/**
 * 
 */
@Getter
public class Echo implements XMLScriptAction {
	
	@Attribute
	private String to;
	@CData
	private String content;

	//-------------------------------------------------------------------
	/**
	 */
	public Echo() {
		// TODO Auto-generated constructor stub
	}

	//-------------------------------------------------------------------
	public void execute(MUDEntity actor, Context context) {
		RawAction action = switch(to) {
		case "self" -> (new com.graphicmud.action.raw.Echo(content))::sendSelf;
		case "other" -> (new com.graphicmud.action.raw.Echo(content))::sendOthersTargetRoom;
		default ->  throw new RuntimeException("Don't know how to echo to '"+to+"'");
		};
		action.accept(actor, context);
	}
}
