/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.world.text;

import com.graphicmud.Identifier;

/**
 * Created at 01.07.2004, 22:36:16
 *
 * @author Stefan Prelle
 *
 */
public class RoomPosition {

	private Identifier roomNumber;

	//------------------------------------------------
	public RoomPosition(Identifier nr) { this.roomNumber=nr; }

	//------------------------------------------------
	public void setRoomNumber(Identifier nr) { this.roomNumber=nr; }

	//------------------------------------------------
	public Identifier getRoomNumber() { return this.roomNumber; }

	//------------------------------------------------
	public String toString() { return String.valueOf(roomNumber); }


}
