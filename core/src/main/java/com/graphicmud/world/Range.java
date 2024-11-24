/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.world;

/**
 *
 */
public enum Range {

	/** Indicates the range of a normal conversation */
	TALK,
	/** Indicates the near surrounding of a player which means
	 that it is all he can see from his position. */
	SURROUNDING,
	/** Indicates the range of a loud call */
	SHOUT,
	/** A technical value representing a map for a Tile-based MUD
	 or an area (a collection of zones) from text-MUD. */
	MAP,
	/** Every map or zone should receive it. */
	EVERYWHERE
	;

}
