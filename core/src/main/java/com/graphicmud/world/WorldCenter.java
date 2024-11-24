/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.world;

import java.util.List;

import com.graphicmud.Identifier;
import com.graphicmud.MUDException;
import com.graphicmud.ZoneIdentifier;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MUDEntityTemplate;
import com.graphicmud.player.PlayerCharacter;
import com.graphicmud.world.text.Direction;
import com.graphicmud.world.text.Exit;

/**
 * WorldCenter.java
 *
 * @author Stefan Prelle  <prelle@tzi.de>
 * @version $Id: WorldCenter.java,v 1.2 2004/07/04 19:45:19 prelle Exp $
 */

public interface WorldCenter {

	//---------------------------------------------------------------
	/**
	 * Start loading data
	 */
	public void start();

	//---------------------------------------------------------------
	public void pulse();

	//---------------------------------------------------------------
	public void tick();

	//---------------------------------------------------------------
	public <L extends Location> L createLocation(World w, Zone z);

	//---------------------------------------------------------------
	public <P extends Position> P createPosition();

	//---------------------------------------------------------------
	public World createWorld(String name) throws MUDException;

	//---------------------------------------------------------------
	public Location getLocation(Identifier nr) throws NoSuchPositionException;

	//---------------------------------------------------------------
	public Location getLocation(Position pos) throws NoSuchPositionException;

	//---------------------------------------------------------------
	/**
	 * Returns all special locations that cover the given position.
	 *
	 * @param pos Position to return the location for
	 * @return Covering Locations, may be empty
	 * @exception NoSuchPositionException The requested position does
	 *            not exist.
	 */
	public Location[] getLocations(Position pos) ;

	//---------------------------------------------------------------
	/**
	 * Returns the surrounding of a player that can be send to him.
	 *
	 * @param player Player that requests the surrounding
	 * @param pos Position to return the surrounding for
	 * @return Surrounding.
	 * @exception NoSuchPositionException The requested position does
	 *            not exist.
	 */
	public Surrounding generateSurrounding(PlayerCharacter player, Position pos) ;

	//---------------------------------------------------------------
	/**
	 * Returns all lifeforms that are in the given range of the
	 * given position.</BR>
	 * NOTE: This may also include the person requesting this
	 * list.
	 *
	 * @param pos Position to use as the mid.
	 * @param range Range to look in.
	 * @exception NoSuchPositionException The requested position does
	 *            not exist.
	 */
	public List<MUDEntity> getLifeformsInRange(Position pos, Range range) throws NoSuchPositionException;

    //---------------------------------------------------------------
    /**
     * Returns all lifeforms that are in the given range of the
     * given position, excluding the player requesting.<BR>
     *
     * @param pos Position to use as the mid.
     * @param range Range to look in.
     * @exception NoSuchPositionException The requested position does
     *            not exist.
     */
    public List<MUDEntity> getLifeformsInRangeExceptSelf(MUDEntity player, Range range) ;

    //---------------------------------------------------------------
	public default List<PlayerCharacter> getPlayersInRangeExceptSelf(MUDEntity self, Range surrounding) {
		return getLifeformsInRangeExceptSelf(self, surrounding)
				.stream()
				.filter(lf -> lf instanceof PlayerCharacter)
				.map(lf -> (PlayerCharacter)lf)
				.toList();
	}

    //---------------------------------------------------------------
    /**
     * Returns the lifeform identified by a given name. A range is given
     * to limit the search.
     *
     * @param name Name of the lifeform to search
     * @param pos Position to use as the mid.
     * @param range Range to look in.
     * @exception NoSuchPositionException The requested position does
     *            not exist.
     */
    public MUDEntity getNamedLifeformInRange(String name, Position pos, Range range) throws NoSuchPositionException;

    //---------------------------------------------------------------
	public Zone getZone(ZoneIdentifier nr);

	//---------------------------------------------------------------
	public void saveZone(Zone zone);

	public MUDEntityTemplate getItemTemplate(Identifier ref);
	
	MUDEntityTemplate getMobileTemplate(Identifier ref);
	
	//---------------------------------------------------------------
	public default Exit getOppositeExit(Position currentPos, Direction dir, Position targetPos) {
		try {
			Location targetLoc = getLocation(targetPos);
			for (Exit exit : targetLoc.getRoomComponent().get().getExitList()) {
				if (exit.getTargetRoom().equals(currentPos.getRoomPosition().getRoomNumber()))
					return exit;
			}
		} catch (NoSuchPositionException e) {			
			e.printStackTrace();
		}
		return null;
	}

} // WorldCenter
