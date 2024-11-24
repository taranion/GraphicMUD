/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.action.script;

import java.util.ArrayList;
import java.util.List;

import org.prelle.simplepersist.ElementList;
import org.prelle.simplepersist.ElementListUnion;

import lombok.Getter;

@Getter
public abstract class HasSubActionsNode {
	
	@ElementListUnion(value = { 
			@ElementList(entry = "echo", type = Echo.class),
			@ElementList(entry = "if", type = IfScriptAction.class),
			@ElementList(entry = "noExecute", type = NoExecute.class),
		})
		@Getter
		private List<XMLScriptAction> eventActions = new ArrayList<XMLScriptAction>();

	protected HasSubActionsNode() {
		// TODO Auto-generated constructor stub
	}

}
