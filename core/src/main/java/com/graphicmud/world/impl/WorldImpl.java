/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.world.impl;

import java.util.ArrayList;
import java.util.List;

import org.prelle.simplepersist.Attribute;
import org.prelle.simplepersist.ElementList;
import org.prelle.simplepersist.Root;

import com.graphicmud.world.World;

/**
 *
 */
@Root(name="world")
public class WorldImpl implements World {

	public static class ZoneLoad {
		@Attribute
		private int id;
		// Eventually add parameter for late loading

		public int getZoneId() {
			return id;
		}
	}


	@Attribute
	private int id;
	@Attribute
	private String title;
	@ElementList(entry = "loadZone", type = ZoneLoad.class)
	private List<ZoneLoad> zones;

	//-------------------------------------------------------------------
	// For persistence loading
	public WorldImpl() {
		zones = new ArrayList<>();
	}

	//-------------------------------------------------------------------
	public WorldImpl(int id, String title) {
		this.id = id;
		this.title = title;
	}

	//-------------------------------------------------------------------
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(World other) {
		return Integer.compare(id, other.getID());
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.World#getID()
	 */
	@Override
	public int getID() {
		return id;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.World#getTitle()
	 */
	@Override
	public String getTitle() {
		return title;
	}

	//-------------------------------------------------------------------
	/**
	 * @return the zones
	 */
	public List<ZoneLoad> getZonesToLoad() {
		return zones;
	}

	@Override
	public void pulse() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tick() {
		// TODO Auto-generated method stub
		
	}

}
