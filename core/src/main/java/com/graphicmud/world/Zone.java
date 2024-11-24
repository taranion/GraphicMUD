/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.world;

import java.util.List;

import com.graphicmud.game.Customizable;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.ReactsOnTime;
import com.graphicmud.map.GridMap;

/**
 * A zone is a collection of up to 100 rooms (in a text MUD) 
 * or a map with up to 100 locations/rooms (in a tile MUD). 
 * 
 */
public interface Zone extends ReactsOnTime, Customizable<Zone> {

	//------------------------------------------------
	/**
	 * @return Returns the description.
	 */
	public String getDescription();

	//------------------------------------------------
	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description);

	//------------------------------------------------
	public String getTitle();

	//------------------------------------------------
	public void setTitle(String title);

	//------------------------------------------------
	/**
	 * @return Returns the zone number.
	 */
	public int getNr();
	
	//------------------------------------------------
	public World getWorld();

	//------------------------------------------------
	public String getMapFile();

	//------------------------------------------------
	public void setMapFile(String filename);

	//------------------------------------------------
	/**
	 * @return TRUE, if room has been added successfully.
	 */
	public boolean addRoom(Location room);

	//------------------------------------------------
	public Location getRoom(int nr);

	//------------------------------------------------
	public List<Location> getRooms();

	//------------------------------------------------
	/**
	 * Store a loaded map instance
	 */
	public void setCachedMap(GridMap imported);

	//------------------------------------------------
	public GridMap getCachedMap();

//	//--------------------------------------------------
//	/**
//	 * TODO: Comment method
//	 *
//	 * @param items
//	 */
//	public void addItems(List<Item> itemList);
//
//	//------------------------------------------------
//	public Item getItem(int nr);
//
//	//------------------------------------------------
//	public List<Item> getItems();

//	//--------------------------------------------------
//	/**
//	 * TODO: Comment method
//	 *
//	 * @param items
//	 */
//	public void addMobs(List<MUDLifeform> mobList);
//
//	//------------------------------------------------
//	public MUDLifeform getMob(int nr);

	//------------------------------------------------
	public List<MUDEntity> getMobs();

}