/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.world.text;

import org.prelle.simplepersist.AttribConvert;
import org.prelle.simplepersist.Attribute;
import org.prelle.simplepersist.Element;

import com.graphicmud.Identifier;
import com.graphicmud.io.IdentifierConverter;
import com.graphicmud.world.HasRPGData;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 */
@Getter
@Setter
public class Door implements HasRPGData {
	
	public static enum DoorState {
		OPEN,
		CLOSED,
		LOCKED
	}
	
	@Attribute(name="load")
	private DoorState loadState = DoorState.CLOSED;
	@Attribute(name="key")
	@AttribConvert(value = IdentifierConverter.class)
	private Identifier keyReference;
	@Element(name="rpg")
	private String rpgData;
	
	// TODO: Position auf Karte

	protected transient DoorState doorState = DoorState.CLOSED;
	protected transient Object userData;

}
