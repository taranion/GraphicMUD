/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud;

import com.graphicmud.world.World;
import com.graphicmud.world.Zone;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode
@Getter	
@Setter	
public class ZoneIdentifier {
	
	protected Integer world;
	protected Integer zone;	

	//-------------------------------------------------------------------
	public ZoneIdentifier(int world, int zone) {
		this.world = world;
		this.zone  = zone;
	}

	//-------------------------------------------------------------------
	public ZoneIdentifier(ZoneIdentifier copy) {
		this.world = copy.world;
		this.zone  = copy.zone;
	}

	//-------------------------------------------------------------------
	public void setWorld(World world) {
		this.world = world.getID();
	}

	//-------------------------------------------------------------------
	public void setZone(Zone zone) {
		this.zone = zone.getNr();
	}

	//-------------------------------------------------------------------
	public String toString() {
		StringBuilder ret = new StringBuilder(String.valueOf(world));
		if (zone!=null)
			ret.append("/"+zone);
		return ret.toString();
	}

}
