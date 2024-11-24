/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.world.text;

import java.util.Locale;

import org.prelle.simplepersist.EnumValue;

import com.graphicmud.Localizable;
import com.graphicmud.Localization;

public enum Direction implements Localizable {
	@EnumValue(value = "n") NORTH,
	@EnumValue(value = "e") EAST,
	@EnumValue(value = "s") SOUTH,
	@EnumValue(value = "w") WEST,
	@EnumValue(value = "u") UP,
	@EnumValue(value = "d") DOWN,
	;
	public String getName(Locale loc) {
		return Localization.getString("enum.direction."+this.name().toLowerCase(), loc);
	}
	public static String[] values(Locale loc) {
		String[] translated = new String[Direction.values().length];
		for (int i=0; i<translated.length; i++) {
			translated[i]=Direction.values()[i].getName(loc);
		}
		return translated;
	}
	public static Direction valueOf(Locale loc, String val) {
		for (Direction dir : Direction.values()) {
			if (dir.getName(loc).equalsIgnoreCase(val))
				return dir;
		}
		return null;
	}
	
	public Direction getOpposite() {
		switch (this) {
		case NORTH : return SOUTH;
		case SOUTH : return NORTH;
		case EAST  : return WEST;
		case WEST  : return EAST;
		case UP    : return DOWN;
		case DOWN  : return UP;
		}
		return null;
	}
}