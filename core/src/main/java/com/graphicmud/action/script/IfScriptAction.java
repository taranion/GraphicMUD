/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.action.script;

import org.prelle.simplepersist.Attribute;

import lombok.Getter;

/**
 * 
 */
@Getter
public class IfScriptAction extends HasSubActionsNode implements XMLScriptAction {
	
	public static enum What {
		INVENTORY,
		EQUIPMENT
	}
	
	public static enum Operation {
		IS_FREE,
		IS_OCCUPIED,
		CONTAINS,
		EQUALS,
		LESS_THAN,
		GREATER_THAN
	}
	
	@Attribute
	private What what;
	@Attribute
	private Operation op;
	@Attribute(name="val")
	private String value;
	

	//-------------------------------------------------------------------
	/**
	 */
	public IfScriptAction() {
		// TODO Auto-generated constructor stub
	}

}
