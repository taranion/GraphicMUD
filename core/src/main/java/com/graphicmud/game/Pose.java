/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.game;

import java.util.Locale;

import com.graphicmud.Localization;
import com.graphicmud.MUD;

public enum Pose {
	
	STANDING,
	SITTING,
	LYING,
	FLYING,
	DEAD
	;
	
	//-------------------------------------------------------------------
	/**
	 * Returns a message format string like "{0} is sitting here"
	 */
	public String getPosePattern(Locale loc) {
		return Localization.getString("enum.pose."+this.name().toLowerCase(), loc);
	}
	
	//-------------------------------------------------------------------
	/**
	 * Returns a message format string like "{0} is sitting here"
	 */
	public String getPosePattern() {
		return getPosePattern(Locale.getDefault());
	}

}
