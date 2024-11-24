/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.map;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

import org.junit.Test;

import com.graphicmud.map.BinaryMap;
import com.graphicmud.map.CaveCreator;
import com.graphicmud.procgen.FindRooms;

/**
 * 
 */
public class CaveCreatorTest {

	//-------------------------------------------------------------------
	@Test
	public void testCave1()  {
		Supplier<BinaryMap> creator = new CaveCreator(500, 55, 60, 30);
		BinaryMap map = creator.get();
		System.out.println( map.printMap() );
		Instant from = Instant.now();
		(new FindRooms()).accept(map);
		Duration dur1 = Duration.between(from, Instant.now());
		System.out.println("Rooms = "+dur1.toMillis());
		System.out.println( map.printMap() );
		Duration dur2 = Duration.between(from, Instant.now());
		System.out.println("Total= "+dur2.toMillis());
	}

}
