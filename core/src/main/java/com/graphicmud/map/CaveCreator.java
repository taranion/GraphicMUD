/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.map;

import java.util.Random;
import java.util.function.Supplier;

/**
 * 
 */
public class CaveCreator implements Supplier<BinaryMap> {

	private int wallProb;
	private int width;
	private int height;
	private Random random;

	//-------------------------------------------------------------------
	public CaveCreator(long seed, int percentWalls, int width, int height) {
		random = new Random(seed);
		this.wallProb = percentWalls;
		this.width    = width;
		this.height   = height;
	}
	//-------------------------------------------------------------------
	/**
	 * @see java.util.function.Supplier#get()
	 */
	@Override
	public BinaryMap get() {
		BinaryMap map = new BinaryMap(width, height);
		initialFill(map);
		for (int i=0; i<5; i++) {
			iterate(map);
		}
		return map;
	}

	//-------------------------------------------------------------------
	private void initialFill(BinaryMap map) {
		var randomColumn = random.nextInt(width-8) +4;
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
                    map.set(x,y, true);
				} else if (x!=randomColumn) {
					int i = random.nextInt(100);
					map.set(x,y, (i<wallProb));
				}
			}
		}
	}

	//-------------------------------------------------------------------
	private void iterate(BinaryMap map) {
		Boolean[][] newMap = new Boolean[height][width];
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				newMap[y][x]=false;
				if(x == 0 || y == 0 || x == width - 1 || y == height - 1)
					newMap[y][x] = true;
	            else
	            	newMap[y][x] = placeWall(map, x, y);
			}
		}

		map.replaceWith(newMap);
	}

	//-------------------------------------------------------------------
	private static boolean placeWall(BinaryMap map, int x, int y) {
		return map.numberOfWalls(x, y,1)>=5 ||
				map.numberOfNearbyWalls(x, y)<=2;
	}

}
