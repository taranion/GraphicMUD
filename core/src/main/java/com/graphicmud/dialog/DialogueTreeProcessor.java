/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.dialog;

import java.lang.System.Logger;
import java.util.List;

/**
 * 
 */
public class DialogueTreeProcessor {
	
	private final static Logger logger = System.getLogger(DialogueTreeProcessor.class.getPackageName());
	
	public static ActionNode startDialogue(DialogueTree data, List<String> speaker) {
		/*
		 * Position: Wo im Dialogbaum
		 * Getroffene Wahl
		 * -> Dinge die man selbst sagt
		 * -> Antwort vom Dialogpartner
		 */
		return data;
	}

}
