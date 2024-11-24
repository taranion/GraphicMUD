/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.dialog;

import org.prelle.simplepersist.Attribute;
import org.prelle.simplepersist.Element;

import lombok.Getter;

/**
 * Reflects a choice option
 */
public class ChoiceNode {
	
	/**
	 * Short text to be displayed as choice
	 */
	@Attribute(required=true)
	@Getter
	private String option;
	@Element
	@Getter
	private String youSay;
	@Attribute(name="return")
	@Getter
	private boolean returnAfterChoice;
	
	@Getter
	@Element
	private ActionNode answer;
	

}
