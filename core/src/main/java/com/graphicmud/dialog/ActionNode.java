/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.dialog;

import java.util.ArrayList;
import java.util.List;

import org.prelle.simplepersist.Element;
import org.prelle.simplepersist.ElementList;
import org.prelle.simplepersist.ElementListUnion;

import lombok.Getter;

/**
 * Describes what is happening after a user choose something
 */
public class ActionNode {

	@ElementListUnion({
		@ElementList(entry = "emote", type=EmoteDialogAction.class, inline=true),
		@ElementList(entry = "say", type=SayDialogAction.class, inline=true)
	})
	@Getter
	private List<DialogAction> introActions = new ArrayList<DialogAction>();
	@ElementList(entry = "choice", type=ChoiceNode.class, inline=true)
	@Getter
	private List<ChoiceNode> choices = new ArrayList<ChoiceNode>();
	@Element(name="outro")
	@Getter
	private DialogActionList outroActions = new DialogActionList();	
	
}
