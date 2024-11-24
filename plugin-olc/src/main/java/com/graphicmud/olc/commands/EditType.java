/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.olc.commands;

import java.util.Locale;

import com.graphicmud.Localization;

public enum EditType {
    ROOM,
    ZONE
	;
	public String getName(Locale loc) {
		return Localization.getString("enum.edittype."+this.name().toLowerCase());
	}
	public static String[] values(Locale loc) {
		String[] translated = new String[EditType.values().length];
		for (int i=0; i<translated.length; i++) {
			translated[i]=EditType.values()[i].getName(loc);
		}
		return translated;
	}
	public static Object valueOf(Locale loc, String val) {
		for (EditType dir : EditType.values()) {
			if (dir.getName(loc).equalsIgnoreCase(val))
				return dir;
		}
		return null;
	}
}
