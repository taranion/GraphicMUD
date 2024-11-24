/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.action.script;

import javax.script.CompiledScript;

import org.prelle.simplepersist.Attribute;
import org.prelle.simplepersist.CData;

import com.graphicmud.game.MUDEvent;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 */
public class OnEventJS implements OnEvent {
	
	@Getter
	@Attribute(required = true)
	private MUDEvent.Type type;
	@Getter
	@Attribute(required = true)
	private String value;
	@Getter
	@CData
	private String scriptContent;
	
	@Getter
	@Setter
	private transient CompiledScript compiled;
}
