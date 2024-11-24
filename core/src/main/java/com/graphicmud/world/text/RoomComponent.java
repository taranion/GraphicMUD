/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.world.text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.prelle.simplepersist.Element;
import org.prelle.simplepersist.ElementList;

/**
 * Created at 03.07.2004, 11:05:09
 *
 * @author Stefan Prelle
 *
 */
public class RoomComponent {

	/** Name of the room */
	@Element
	protected String title;

	/** Exits indexed using direction */
	@ElementList(type=Exit.class, entry="exit")
	protected List<Exit> exitlist;

	//---------------------------------------------------------------
	/**
	 * Returns the title of the room.
	 *
	 * @return String containing title
	 */
	public String getTitle() { return title; }

	//---------------------------------------------------------------
	public void setTitle(String title) { this.title = title; }

//	//---------------------------------------------------------------
//	/**
//	 * Return all lifeforms (mobiles and player) in this room
//	 *
//	 * @return A set of <code>Lifeform</code>s
//	 */
//	public List<MUDLifeform> getLifeforms();
//
//	//---------------------------------------------------------------
//	/**
//	 * Add a mobile or player to this room
//	 *
//	 * @param life A lifeform to add
//	 */
//	public void addLifeform(MUDLifeform life);
//
//	//---------------------------------------------------------------
//	/**
//	 * Removes a mobile or player from this room. No action is taken
//	 * when the lifeform isn't present
//	 *
//	 * @param life A lifeform to remove
//	 */
//	public void removeLifeform(MUDLifeform life);
//
//	//---------------------------------------------------------------
//	/**
//	 * Add a mobile or player to this room
//	 *
//	 * @param life A lifeform to add
//	 */
//	public void addItem(Item item);
//
//	//---------------------------------------------------------------
//	/**
//	 * Removes a mobile or player from this room. No action is taken
//	 * when the lifeform isn't present
//	 *
//	 * @param life A lifeform to remove
//	 */
//	public void removeItem(Item item);
//
//	//---------------------------------------------------------------
//	/**
//	 * @return
//	 */
//	public List<Item> getItems();
//
//	//---------------------------------------------------------------
//	/**
//	 * Return the lifeform reacting to a specific keyword.
//	 * If there is more than one lifeform reacting to this keyword
//	 * the optional <code>pos</code> parameter can be used to
//	 * address another than the first one.
//	 *
//	 * @param keyword Name or alias
//	 * @param pos Which one exactly - from 1 .. *
//	 */
//	public MUDLifeform getLifeform(String keyword, int pos);
//
//	//---------------------------------------------------------------
//	/**
//	 * Return the item reacting to a specific keyword.
//	 * If there is more than one item reacting to this keyword
//	 * the optional <code>pos</code> parameter can be used to
//	 * address another than the first one.
//	 *
//	 * @param keyword Name or alias
//	 * @param pos Which one exactly - from 1 .. *
//	 */
//	public Item getItem(String keyword, int pos);

	//---------------------------------------------------------------
	/**
	 * Returns the exit for the given direction.
	 *
	 * @see RoomComponent.world.Room#getExit(int)
	 * @param dir Direction to look for
	 * @return Exit object or <code>null</code> if no exit in
	 *         the given direction exists
	 */
	public Exit getExit(Direction dir) {
		Optional<Exit> ret = exitlist.stream().filter(e -> e.getDirection()==dir).findFirst();
		return ret.isPresent()?ret.get():null;
	}

	//---------------------------------------------------------------
	public List<Exit> getExitList() {
		List<Exit> ret = new ArrayList<Exit>();
		for (Exit exit : exitlist)
			if (exit!=null)
				ret.add(exit);
		return ret;
	}

	//---------------------------------------------------------------
	public void setExitList(Collection<Exit> toSet) {
		// Clear old data
		exitlist.clear();
		toSet.forEach(e -> exitlist.add(e));
	}

	//---------------------------------------------------------------
	/**
	 * Set the exit for the given direction.
	 *
	 * @param dir Direction to look for
	 * @param exit Exit object to set or <code>null</code> to remove
	 *             the exit
	 */
	public void setExit(Exit exit) {
		exitlist.removeIf(e -> e.getDirection()==exit.getDirection());
		exitlist.add(exit);
	}

	//---------------------------------------------------------------
	public void setExit(Direction dir, Exit exit) {
		exitlist.removeIf(e -> e.getDirection()==dir);
		exitlist.add(exit);
	}

}
