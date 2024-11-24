/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.action.script;

import org.prelle.simplepersist.Attribute;

import com.graphicmud.game.MUDEvent;

import lombok.Getter;

/**
 * 
 */
public class OnEventXML extends HasSubActionsNode implements OnEvent {
	
	@Getter
	@Attribute(required = true)
	private MUDEvent.Type type;
	@Getter
	@Attribute
	private String value;
	@Getter
	@Attribute
	private String param;
	
}
