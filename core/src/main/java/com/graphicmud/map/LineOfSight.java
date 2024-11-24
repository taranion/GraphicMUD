/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.map;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import com.graphicmud.symbol.Symbol;
import com.graphicmud.symbol.SymbolFlags;

/**
 * 
 */
public class LineOfSight {
	
	private final static Logger logger = System.getLogger(LineOfSight.class.getPackageName());
	private final static int NOT_SEE_NOT_WALK = -2;
	private final static int SEE_BUT_NOT_WALK = -1;
	
	private static record Next(int x, int y) {
		public String toString() { return "("+x+","+y+")"; }
	}
	
	private static Predicate<Symbol> WALK_CHECK  = (sym) -> sym!=null && !sym.hasFlag(SymbolFlags.BLOCK_MOVEMENT);
	private static Predicate<Symbol> SIGHT_CHECK = (sym) -> sym!=null && !sym.hasFlag(SymbolFlags.BLOCK_SIGHT);
	
	private static Predicate<Symbol> check = SIGHT_CHECK;

	//-------------------------------------------------------------------
	public static void floodFill(ViewportMap<Symbol> map) {
		int startX = map.getPositionSelf()[0];
		int startY = map.getPositionSelf()[1];
		
		// Prepare array
		Integer[][] flood = new Integer[map.getHeight()][map.getWidth()];
		for (int i=0; i<map.getHeight(); i++) {
			Arrays.fill(flood[i], NOT_SEE_NOT_WALK);
		}
		
		int depth = 1;
		List<Next> tested = new ArrayList<>();
		List<Next> nextDepth = floodAt(map,flood, startX, startY,depth);
		logger.log(Level.INFO, "for depth {0}: {1}",depth,nextDepth);
		recurse(map,flood,depth+1, tested, nextDepth);
		
		// Remove detail for invisible
		for (int y=0; y<map.getHeight(); y++) {
			for (int x=0; x<map.getWidth(); x++) {
				if (flood[y][x]==NOT_SEE_NOT_WALK)
					map.set(x, y, null);
			}
		}
		map.addLayer("flood", flood);
	}

	//-------------------------------------------------------------------
	private static void recurse(ViewportMap<Symbol> map, Integer[][] flood, int depth, List<Next> tested, List<Next> toTest) {
		List<Next> nextDepth = new ArrayList<>();
		for (Next tmp : toTest) {
			if (!tested.contains(tmp)) {
				tested.add(tmp);
				List<Next> checkNext = floodAt(map,flood, tmp.x, tmp.y,depth);
				checkNext.stream()
					.filter(t -> !tested.contains(t))
					.filter(t -> !toTest.contains(t))
					.filter(t -> !nextDepth.contains(t))
					.forEach(t -> {
						nextDepth.add(t);
						});
			}
		}
		logger.log(Level.TRACE, "for depth {0}: {1}",depth,nextDepth);
		if (!nextDepth.isEmpty()) {
			recurse(map,flood,depth+1,tested,nextDepth);
		}
	}

	//-------------------------------------------------------------------
	private static List<Next> floodAt(ViewportMap<Symbol> map, Integer[][] flood, int posX, int posY, int value) {
		flood[posY][posX]=value;
		value++;
		List<Next> next = new ArrayList<>(); 
		for (Direction dir : Direction.values()) {
			int newX = posX+dir.x;
			int newY = posY+dir.y;
			if (newX<0 || newX>=map.getWidth())
				continue;
			if (newY<0 || newY>=map.getHeight())
				continue;
			// If not processed, set distance
			if (flood[newY][newX]<0) {
				// The field is at least visible
				flood[newY][newX] = SEE_BUT_NOT_WALK;
				// Can I go into that field
				if (check.test(map.get(newX, newY))) {
					flood[newY][newX] = value;
					Next tmp = new Next(newX, newY);
					if (!next.contains(tmp))
						next.add(tmp);
				}
			}
		}
		return next;
		
	}
}
