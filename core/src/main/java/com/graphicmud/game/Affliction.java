/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.game;

import org.prelle.simplepersist.Attribute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class Affliction {
	
	public static enum AfflictionType {
		IMMOBILIZED,
		DEAF,
		MUTE,
		BLIND,
		FIGHTING
	}
	
	@Attribute
	private AfflictionType type;
	@Attribute
	private int ticks;

}
