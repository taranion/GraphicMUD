/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.procgen;

import java.lang.System.Logger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Random;

import com.graphicmud.map.GridMap;
import com.graphicmud.map.InMemoryMap;
import com.graphicmud.map.MapGenerator;

/**
 *
 */
public class ProcGen implements MapGenerator {
	
	private final static Logger logger = System.getLogger(ProcGen.class.getPackageName());

	private enum Direction {
		N(0,-1),
		NE(1,-1),
		E(1,0),
		SE(1,1),
		S(0,1),
		SW(-1,1),
		W(-1,0),
		NW(-1,-1)
		;
		int x,y;
		Direction(int xv, int yv) {
			x=xv;
			y=yv;
		}
	}



	public static void main(String[] args) {
		long seed = 500;
		ProcGen gen = new ProcGen(seed, 55, 30, 60, 0,0);
		gen.get();
	}

	private Random random;

	private int percentWalls;
	private int wallSymbol;
	private int floorSymbol;
	
	private boolean[][] map;
	private InMemoryMap grid;

	//-------------------------------------------------------------------
	public ProcGen(long seed, int percentWalls, int width, int height, int floorSymbol, int wallSymbol) {
		random = new Random(seed);
		this.percentWalls = percentWalls;
		this.floorSymbol  = floorSymbol;
		this.wallSymbol   = wallSymbol;

		map = new boolean[width][height];
		grid = new InMemoryMap(width, height);
	}

	//-------------------------------------------------------------------
	private static void initialFill(Random random, int wallProb, boolean[][] map) {
		int height = map.length;
		int width  = map[0].length;
		var randomColumn = random.nextInt(width-8) +4;
		for (int y=0; y<map.length; y++) {
			for (int x=0; x<map[y].length; x++) {
				if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
                    map[y][x] = true;
				} else if (x!=randomColumn) {
					int i = random.nextInt(100);
					map[y][x] = (i<wallProb);
				}
			}
		}
	}

	//-------------------------------------------------------------------
	private static boolean[][] iterate(Random random, boolean[][] map) {
		boolean[][] newMap = new boolean[map.length][map[0].length];
		int height = map.length;
		int width  = map[0].length;
		for (int y=0; y<map.length; y++) {
			for (int x=0; x<map[y].length; x++) {
				 if(x == 0 || y == 0 || x == width - 1 || y == height - 1)
					 newMap[y][x] = true;
	             else
	            	 newMap[y][x] = placeWall(map, x, y);
			}
		}

		return newMap;
	}

	//-------------------------------------------------------------------
	private static boolean placeWall(boolean[][] map, int x, int y) {
		return numberOfAdjacentWalls(map, x, y)>=5 ||
				numberOfNearbyWalls(map, x, y)<=2;
	}

	//-------------------------------------------------------------------
	private static int numberOfAdjacentWalls(boolean[][] map, int x, int y) {
		int walls = 0;
		for (Direction dir : Direction.values()) {
			int newX = x+dir.x;
			int newY = y+dir.y;
			if (newX<0 || newX>=map[0].length) { walls++; continue;}
			if (newY<0 || newY>=map.length) { walls++; continue;}
			if (map[newY][newX]) walls++;
		}
		return walls;
	}

	//-------------------------------------------------------------------
	private static int numberOfNearbyWalls(boolean[][] map, int x, int y) {
        var walls = 0;
         for(var mapX = x - 2; mapX <= x + 2; mapX++) {
            for(var mapY = y - 2; mapY <= y + 2; mapY++)  {
                if (Math.abs(mapX - x) == 2 && Math.abs(mapY - y) == 2)
                    continue;

                if (mapX < 0 || mapY < 0 || mapX >= map[0].length || mapY >= map.length)
                    continue;

                if (map[mapY][mapX])
                    walls++;
            }
        }
        return walls;
    }



	//-------------------------------------------------------------------
	private static void printMap(boolean[][] map) {
		for (int y=0; y<map.length; y++) {
			for (int x=0; x<map[y].length; x++) {
				System.out.print( map[y][x]?"#":".");
			}
			System.out.println("");
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see java.util.function.Supplier#get()
	 */
	@Override
	public GridMap get() {
		initialFill(random, percentWalls, map);
		for (int i=0; i<5; i++) map = iterate(random, map);
		printMap(map);

		byte[] raw = new byte[grid.getWidth()*grid.getHeight()*4];
		ByteBuffer buf = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
		IntBuffer iBuf = buf.asIntBuffer();

		for (int y=0; y<grid.getHeight(); y++) {
			for (int x=0; x<grid.getWidth(); x++) {
				int pos = y*grid.getWidth()+x;
				iBuf.put(pos, map[x][y]?wallSymbol:floorSymbol);				
			}			
		}
		GridMap.Layer layer = new GridMap.Layer(1,"Floor");
		grid.addLayer(layer, iBuf.array());
		
		return grid;
	}

}
