/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.dialog;

import java.util.ArrayList;

import org.prelle.simplepersist.ElementList;
import org.prelle.simplepersist.ElementListUnion;

@ElementListUnion({
	@ElementList(entry = "emote", type=EmoteDialogAction.class, inline=true),
	@ElementList(entry = "say", type=SayDialogAction.class, inline=true)
})
public class DialogActionList extends ArrayList<DialogAction> {

}
