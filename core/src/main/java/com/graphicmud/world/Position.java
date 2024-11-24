/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.world;

import org.prelle.simplepersist.Attribute;

import com.graphicmud.ZoneIdentifier;
import com.graphicmud.world.text.RoomPosition;
import com.graphicmud.world.tile.GridPosition;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 */
@EqualsAndHashCode
@Getter	
public class Position {

	@Attribute(name="world")
	private int worldNumber;
	@Attribute(name="zone")
	private int zoneNumber;

	private RoomPosition roomPosition;
	private GridPosition tilePosition;

	//-------------------------------------------------------------------
	public String toString() {
		return "Pos("+worldNumber+","+zoneNumber+", room="+roomPosition+", grid="+tilePosition+")";
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.Position#getWorldNumber()
	 */
	public int getWorldNumber() {
		return worldNumber;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.Position#setWorldNumber(int)
	 */
	public Position setWorldNumber(int value) {
		this.worldNumber = value;
		return this;
	}

	//-------------------------------------------------------------------
	public ZoneIdentifier getZoneNumber() {
		return new ZoneIdentifier(worldNumber, zoneNumber);
	}

	//-------------------------------------------------------------------
	public Position setZoneNumber(int world, int value) {
		this.worldNumber= world;
		this.zoneNumber = value;
		return this;
	}

	//-------------------------------------------------------------------
	/**
	 * @return the roomPosition
	 */
	public RoomPosition getRoomPosition() {
		return roomPosition;
	}

	//-------------------------------------------------------------------
	/**
	 * @param roomPosition the roomPosition to set
	 */
	public void setRoomPosition(RoomPosition roomPosition) {
		this.roomPosition = roomPosition;
		if (roomPosition!=null) {
			zoneNumber = (roomPosition.getRoomNumber().getZoneId()!=null)?roomPosition.getRoomNumber().getZoneId():103;
			worldNumber = (roomPosition.getRoomNumber().getWorldId()!=null)?roomPosition.getRoomNumber().getWorldId():1;
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @return the tilePosition
	 */
	public GridPosition getTilePosition() {
		return tilePosition;
	}

	//-------------------------------------------------------------------
	/**
	 * @param tilePosition the tilePosition to set
	 */
	public void setTilePosition(GridPosition tilePosition) {
		this.tilePosition = tilePosition;
	}

} // Position
