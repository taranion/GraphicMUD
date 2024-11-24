/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.procgen;

import java.lang.System.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import com.graphicmud.map.GeneratedMap;
import com.graphicmud.map.GeneratedMap.Square;

/**
 * 
 */
public class FindRooms<T> implements Consumer<GeneratedMap<T>> {
	
	enum ShapeTest {
		R1_N ( 0,-1), // 0
		R1_NO( 1,-1), // 1
		R1_O ( 1, 0), // 4
		R1_SO( 1, 1), // 3
		R1_S ( 0, 1), // 4
		R1_SW(-1, 1), // 5
		R1_W (-1, 0), // 6
		R1_NW(-1,-1), //
		
		F08( 0,-2), // 8
		F09( 1,-2), // 9
		F10( 2,-2), 
		F11( 2,-1),
		F12( 2, 0),
		F13( 2, 1),
		F14( 2, 2),
		F15( 1, 2),
		F16( 0, 2),
		F17(-1, 2),
		F18(-2, 2),
		F19(-2, 1),
		F20(-2, 0),
		F21(-2,-1),
		F22(-2,-2),
		F23(-1,-2),

		F24( 0,-3),
		F25( 1,-3),
		F26( 2,-3),
		F27( 3,-3),
		F28( 3,-2),
		F29( 3,-1),
		F30( 3, 0),
		F31( 3, 1),
		F32( 3, 2),
		F33( 3, 3),
		F34( 2, 3),
		F35( 1, 3),
		F36( 0, 3),
		F37(-1, 3),
		F38(-2, 3),
		F39(-3, 3),
		F40(-3, 2),
		F41(-3, 1),
		F42(-3, 0),
		F43(-3,-1),
		F44(-3,-2),
		F45(-3,-3),
		F46(-3,-2),
		F47(-3,-1)
		;
		int modX;
		int modY;
		
		ShapeTest(int modX, int modY) {
			this.modX=modX;
			this.modY=modY;
		}
	}	
	
	//-------------------------------------------------------------------
	/**
	 * @see java.util.function.Consumer#accept(java.lang.Object)
	 */
	//@Override
	public void acceptOld(GeneratedMap<T> map) {
		
		for (int y=1; y<map.getHeight()-1; y++) {
			for (int x=1; x<map.getWidth()-1; x++) {
				List<Integer> free = getFreeList(map,x,y);
				for (RoomShape shape : RoomShape.values()) {
					if (free.containsAll(shape.fieldOrdinals)) {
//						System.out.println("Room "+shape+" at "+x+","+y);
						break;
					}
				}
			}
		}
	}
	
	//-------------------------------------------------------------------
	private List<Integer> getFreeList(GeneratedMap<T> map, int x, int y) {
		List<Integer> free = new ArrayList<Integer>();
		for (ShapeTest field : ShapeTest.values()) {
			if (!map.isWall( x+field.modX, y+field.modY ))
				free.add(field.ordinal());
		}
		return free;
	}
	
	//-------------------------------------------------------------------
	/**
	 * @see java.util.function.Consumer#accept(java.lang.Object)
	 */
	//@Override
	public void accept(GeneratedMap<T> map) {
		for (RoomShape shape : RoomShape.values()) {
			for (int y=1; y<map.getHeight()-1; y++) {
				for (int x=1; x<map.getWidth()-1; x++) {
					Square found = supportsShape(map,x,y,shape);
					if (found!=null) {
						System.out.println("There is a "+shape+" room at "+x+","+y+" with nr. "+found.getId());
					}
				}
			}
//			if (shape==RoomShape.SMALL)
//				return;

		}
		

	}

	//-------------------------------------------------------------------
	private Square supportsShape(GeneratedMap<T> map,int x, int y, RoomShape shape) {
		int fromX = x-shape.width/2;
		int fromY = y-shape.width/2;
		if (fromX<0 || fromY<0) return null;
		int toX = (fromX+shape.width-1);
		int toY = (fromY+shape.height-1);
		if (toX>=map.getWidth() || toY>=map.getHeight()) return null;
		for (int newY=fromY; newY<=toY; newY++) {
			for (int newX=fromX; newX<=toX; newX++) {
				if (map.isWall(newX, newY)) return null;
				if (map.overlapsWithRoomPlusMargin(newX, newY)) return null;
			}
		}
		return map.assignRoom(fromX, toX, fromY, toY);	}

}

