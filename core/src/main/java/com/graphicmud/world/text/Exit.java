/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.world.text;

import java.util.Optional;

import org.prelle.simplepersist.AttribConvert;
import org.prelle.simplepersist.Attribute;
import org.prelle.simplepersist.Element;

import com.graphicmud.Identifier;
import com.graphicmud.io.IdentifierConverter;
import com.graphicmud.world.text.Door.DoorState;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Exit {

	/** The number of the target room. */
	@Attribute(name="target")
	@AttribConvert(value = IdentifierConverter.class)
	protected Identifier targetRoom;
	/** The direction (see <code>Room</code>) of the exit */
	@Attribute(name="dir")
	protected Direction direction;
	/** The short name of the exit when shown in lists */
	@Attribute
	protected String title;

	/**
	 * The descriptive text when looking in the exits direction.
	 * May be omitted.
	 */
	@Element(name="descr")
	protected String description;
	/**
	 * The message that the player shall receive when passing
	 * through the exit. May be empty.
	 */
	@Element(name="passdesc")
	protected String passDescription;

	@Element(name="door")
	protected Door doorComponent;
	
	protected transient Optional<DoorState> doorState;
	protected transient Object userData;
	
	
	//-------------------------------------------------------------------
	public boolean isDoor() {
		return doorComponent!=null;
	}
}