/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud;

import com.graphicmud.Identifier;

import com.graphicmud.world.World;
import com.graphicmud.world.Zone;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 
 */
@EqualsAndHashCode
public class Identifier {
	
	@Getter	
	private Integer worldId;
	@Getter	
	private Integer zoneId;	
	@Getter	
	private String localId;

	//-------------------------------------------------------------------
	public Identifier(String wrapped) {
		String[] elements = wrapped.split("/");
		switch (elements.length) {
		case 1: 
			localId = elements[0];
			break;
		case 2:
			zoneId  = Integer.parseInt(elements[0]);
			localId = elements[1];
			break;
		case 3:
			worldId = Integer.parseInt(elements[0]);
			zoneId  = Integer.parseInt(elements[1]);
			localId = elements[2];
			break;
		}
	}

	//-------------------------------------------------------------------
	public Identifier(Identifier copy) {
		this.worldId = copy.worldId;
		this.zoneId  = copy.zoneId;
		this.localId = copy.localId;
	}

	//-------------------------------------------------------------------
	public void setWorld(World world) {
		this.worldId = world.getID();
	}

	//-------------------------------------------------------------------
	public void setZone(Zone zone) {
		this.zoneId = zone.getNr();
	}

	//-------------------------------------------------------------------
	public void setLocal(int num) {
		this.localId = String.valueOf(num);
	}

	//-------------------------------------------------------------------
	public void setLocal(String val) {
		this.localId = val;
	}

	//-------------------------------------------------------------------
	public String toString() {
		StringBuilder ret = new StringBuilder();
		if (worldId!=null)
			ret.append(worldId+"/");
		if (zoneId!=null)
			ret.append(zoneId+"/");
		ret.append(localId);
		return ret.toString();
	}

	//-------------------------------------------------------------------
	public boolean isZoneLocal() {
		return zoneId==null;
	}

	//-------------------------------------------------------------------
	public boolean isWorldLocal() {
		return worldId==null;
	}

	//-------------------------------------------------------------------
	public boolean isGlobal() {
		return worldId!=null && zoneId!=null;
	}

	//-------------------------------------------------------------------
	public Identifier asGlobalId(World world, Zone zone) {
		Identifier ret = new Identifier(this);
		// Fix world
		if (ret.worldId==null) {
			ret.setWorld(world);
		} else if (ret.worldId!=world.getID()) {
			throw new IllegalArgumentException("Identifier cannot be set to world "+world.getID()+", because it already is in "+worldId);
		}
		// Fix zone
		if (ret.zoneId==null) {
			ret.setZone(zone);
		} else if (ret.zoneId!=zone.getNr()) {
			throw new IllegalArgumentException("Identifier cannot be set to world "+zone.getNr()+", because it already is in "+zoneId);
		}
		return ret;
	}

	//-------------------------------------------------------------------
	public String toZoneLocalId() {
		return localId;
	}

//	//-------------------------------------------------------------------
//	public String toWorldLocalId() {
//		int first = wrapped.indexOf('/');
//		if (first==-1) {
//			throw new IllegalArgumentException("Id is local and cannot be downgraded to World-local");
//		} 
//		int last = wrapped.indexOf('/', first+1);
//		if (last==-1) {
//			// No second / - already world local
//			return wrapped;
//		} else {
//			return wrapped.substring(first+1);
//		}
//	}
//
//	//-------------------------------------------------------------------
//	public String asGlobalId() {
//		int first = wrapped.indexOf('/');
//		if (first==-1) {
//			throw new IllegalArgumentException("Id is local and cannot be upgraded to local");
//		} 
//		int last = wrapped.indexOf('/', first+1);
//		if (last==-1) {
//			// No second / - already world local
//			return wrapped;
//		} else {
//			return wrapped.substring(first+1);
//		}
//	}
	
	//-------------------------------------------------------------------
	public Identifier makeGlobal(World world, Zone zone) {
		if (isZoneLocal()) {
			setWorld(world);
			setZone(zone);
		} else if (isWorldLocal()) {
			setWorld(world);
		}
		return this;
	}

	//-------------------------------------------------------------------
	public static Identifier of(Identifier roomnummber, int number) {
		Identifier id = new Identifier(roomnummber);
		id.setLocal(number);
		return id;
	}

	//-------------------------------------------------------------------
	public int getLocalAsNumber() {
		return Integer.parseInt(localId);
	}

}
