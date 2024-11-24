/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.map;

import java.util.List;

import com.graphicmud.procgen.RoomShape;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 */
public interface GeneratedMap<T> extends Map2D<T> {

	@AllArgsConstructor
	@Getter
	public class Square {
		@Setter
		int id;
		@Setter
		RoomShape shape;
		int fromX;
		int toX;
		int fromY;
		int toY;
		public Square(RoomShape shape,int fX, int tX, int fY, int tY) {
			fromX=fX; fromY=fY;
			toX=tX;   toY=tY;
			this.shape=shape;
		}
	};

	
	public boolean isWall(int x, int y);
	void replaceWith(T[][] newMap);
	
	//-------------------------------------------------------------------
	/**
	 * @return Room identifier
	 */
	public Square assignRoom(int fromX,int toX, int fromY, int toY);
	public List<Square> getRooms();
	public default boolean overlapsWithRoomPlusMargin(int x, int y) {
		for (Square tmp : getRooms()) {
			if (x>=(tmp.fromX-1) && x<=(tmp.toX+1) && y>=(tmp.fromY-1) && y<=(tmp.toY+1)) return true;
		}
		return false;
	}
	public default boolean overlapsWithRoom(int x, int y) {
		for (Square tmp : getRooms()) {
			if (x>=tmp.fromX && x<=tmp.toX && y>=tmp.fromY && y<=tmp.toY) return true;
		}
		return false;
	}

	//-------------------------------------------------------------------
	public default int numberOfWalls(int x, int y, int range) {
        int walls = 0;
        for(int mapX = x - range; mapX <= x + range; mapX++) {
        	for(int mapY = y - range; mapY <= y + range; mapY++)  {
                // Ignore outside range
                if (mapX < 0 || mapY < 0 || mapX >= getWidth() || mapY >= getHeight()) {
                	walls++;
                    continue;
                }
                if (mapY==y && mapX==x)
                	continue;

                if (isWall(mapX,mapY))
                    walls++;
            }
        }
        return walls;
    }

//	//-------------------------------------------------------------------
//	public default int numberOfAdjacentWalls(int x, int y) {
//		int walls = 0;
//		for (Direction dir : Direction.values()) {
//			int newX = x+dir.x;
//			int newY = y+dir.y;
//			if (newX<0 || newX>=getWidth()) { walls++; continue;}
//			if (newY<0 || newY>=getHeight()) { walls++; continue;}
//			if (isWall(newX,newY)) walls++;
//		}
//		return walls;
//	}

	//-------------------------------------------------------------------
	public default int numberOfNearbyWalls( int x, int y) {
        var walls = 0;
         for(var mapX = x - 2; mapX <= x + 2; mapX++) {
            for(var mapY = y - 2; mapY <= y + 2; mapY++)  {
                if (Math.abs(mapX - x) == 2 && Math.abs(mapY - y) == 2)
                    continue;

                if (mapX < 0 || mapY < 0 || mapX >= getWidth() || mapY >= getHeight())
                    continue;

                if (isWall(mapX,mapY))
                    walls++;
            }
        }
        return walls;
    }

}
