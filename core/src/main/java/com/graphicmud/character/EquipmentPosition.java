/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.character;

import java.util.Locale;

import com.graphicmud.Localization;
import com.graphicmud.world.text.Direction;

public enum EquipmentPosition {
    HEAD,
    NECK,
    SHOULDERS,
    BACK,
    ARMS,
    WRIST,
    WAIST,
    FINGER,
    BODY,
    LEGS,
    FEET,
    WEAPON_PRIMARY,
    WEAPON_SECONDARY,
    SHIELD,
    HOLD,
    NONE
    ;
	public String getName(Locale loc) {
		return Localization.getString("enum.equipmentposition."+this.name().toLowerCase(), loc);
	}
	public static String[] values(Locale loc) {
		String[] translated = new String[EquipmentPosition.values().length];
		for (int i=0; i<translated.length; i++) {
			translated[i]=EquipmentPosition.values()[i].getName(loc);
		}
		return translated;
	}
	public static EquipmentPosition valueOf(Locale loc, String val) {
		for (EquipmentPosition dir : EquipmentPosition.values()) {
			if (dir.getName(loc).equalsIgnoreCase(val))
				return dir;
		}
		return null;
	}
	
}
