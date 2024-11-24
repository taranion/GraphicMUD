/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.ecs;

import org.prelle.simplepersist.Attribute;
import org.prelle.simplepersist.Element;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 */
@Getter
@Setter
public class KnowledgeComponent {
	
	public static enum KnowledgeType {
		INTRODUCTION,
		SKILL_VALUE,
		QUEST_FLAG
	}
	
	@Attribute(required=true)
	private KnowledgeType type;
	@Attribute(name = "ref")
	private String reference;
	@Attribute
	private int value;
	@Element
	private String nameWhenKnown;
	
	
}
