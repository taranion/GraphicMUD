/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.behavior;

import java.util.List;

import com.graphicmud.Identifier;
import com.graphicmud.MUDException;
import com.graphicmud.ZoneIdentifier;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MUDEntityTemplate;
import com.graphicmud.player.PlayerCharacter;
import com.graphicmud.world.Location;
import com.graphicmud.world.NoSuchPositionException;
import com.graphicmud.world.Position;
import com.graphicmud.world.Range;
import com.graphicmud.world.Surrounding;
import com.graphicmud.world.World;
import com.graphicmud.world.WorldCenter;
import com.graphicmud.world.Zone;

/**
 * 
 */
public class DummyWorldCenter implements WorldCenter {

	//-------------------------------------------------------------------
	/**
	 */
	public DummyWorldCenter() {
		// TODO Auto-generated constructor stub
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.WorldCenter#start()
	 */
	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.WorldCenter#pulse()
	 */
	@Override
	public void pulse() {
		// TODO Auto-generated method stub

	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.WorldCenter#tick()
	 */
	@Override
	public void tick() {
		// TODO Auto-generated method stub

	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.WorldCenter#createLocation(com.graphicmud.world.World, com.graphicmud.world.Zone)
	 */
	@Override
	public <L extends Location> L createLocation(World w, Zone z) {
		// TODO Auto-generated method stub
		return null;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.WorldCenter#createPosition()
	 */
	@Override
	public <P extends Position> P createPosition() {
		// TODO Auto-generated method stub
		return null;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.WorldCenter#createWorld(java.lang.String)
	 */
	@Override
	public World createWorld(String name) throws MUDException {
		// TODO Auto-generated method stub
		return null;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.WorldCenter#getLocation(int)
	 */
	@Override
	public Location getLocation(Identifier nr) throws NoSuchPositionException {
		// TODO Auto-generated method stub
		return null;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.WorldCenter#getLocation(com.graphicmud.world.Position)
	 */
	@Override
	public Location getLocation(Position pos) throws NoSuchPositionException {
		// TODO Auto-generated method stub
		return null;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.WorldCenter#getLocations(com.graphicmud.world.Position)
	 */
	@Override
	public Location[] getLocations(Position pos) {
		// TODO Auto-generated method stub
		return null;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.WorldCenter#generateSurrounding(com.graphicmud.player.PlayerCharacter, com.graphicmud.world.Position)
	 */
	@Override
	public Surrounding generateSurrounding(PlayerCharacter player, Position pos) {
		// TODO Auto-generated method stub
		return null;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.WorldCenter#getLifeformsInRange(com.graphicmud.world.Position, com.graphicmud.world.Range)
	 */
	@Override
	public List<MUDEntity> getLifeformsInRange(Position pos, Range range) throws NoSuchPositionException {
		// TODO Auto-generated method stub
		return null;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.WorldCenter#getLifeformsInRangeExceptSelf(com.graphicmud.game.MUDEntity, com.graphicmud.world.Range)
	 */
	@Override
	public List<MUDEntity> getLifeformsInRangeExceptSelf(MUDEntity player, Range range) {
		// TODO Auto-generated method stub
		return null;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.WorldCenter#getNamedLifeformInRange(java.lang.String, com.graphicmud.world.Position, com.graphicmud.world.Range)
	 */
	@Override
	public MUDEntity getNamedLifeformInRange(String name, Position pos, Range range) throws NoSuchPositionException {
		// TODO Auto-generated method stub
		return null;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.WorldCenter#getZone(int)
	 */
	@Override
	public Zone getZone(ZoneIdentifier nr) {
		// TODO Auto-generated method stub
		return null;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.WorldCenter#saveZone(com.graphicmud.world.Zone)
	 */
	@Override
	public void saveZone(Zone zone) {
		// TODO Auto-generated method stub

	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.WorldCenter#getItemTemplate(java.lang.String)
	 */
	@Override
	public MUDEntityTemplate getItemTemplate(Identifier ref) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MUDEntityTemplate getMobileTemplate(Identifier ref) {
		return null;
	}

}
