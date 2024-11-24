/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.procgen;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public enum RoomShape {
	GIANT(7,7, IntStream.rangeClosed(0, 47)),
	VERY_LARGE(6,6, IntStream.rangeClosed(0, 23), IntStream.rangeClosed(27, 38)),
	VERY_LARGE_VERTICAL  (5,6, IntStream.rangeClosed(0, 23), IntStream.rangeClosed(34, 38)),
	VERY_LARGE_HORIZONTAL(6,5, IntStream.rangeClosed(0, 23), IntStream.rangeClosed(27, 33)),
	LARGE            (5,5, IntStream.rangeClosed(0, 23)),
	LARGE_VERTICAL   (4,5, IntStream.rangeClosed(0, 17), IntStream.rangeClosed(23, 23)),
	LARGE_HORIZONTAL (5,4, IntStream.rangeClosed(0, 7), IntStream.rangeClosed(11, 21)),
	MEDIUM           (4,4, IntStream.rangeClosed(0, 7), IntStream.rangeClosed(11, 17)),
	MEDIUM_VERTICAL  (3,4, IntStream.rangeClosed(0, 7), IntStream.rangeClosed(15, 17)),
	MEDIUM_HORIZONTAL(4,3, IntStream.rangeClosed(0, 7), IntStream.rangeClosed(11, 13)),
	SMALL(3,3, IntStream.rangeClosed(0, 7)),
//	SMALL_VERTICAL  (2,3, List.of(0,1,2,3,4)),
//	SMALL_HORIZONTAL(3,2, List.of(2,3,4,5,6)),
//	TINY(2,2, List.of(2,3,4)),
//	CORRIDOR_VERTICAL(1,3, List.of(2,6)),
//	CORRIDOR_HORIZONTAL(3,1, List.of(0,4)),
	;
	int width;
	int height;
	List<Integer> fieldOrdinals;
	RoomShape(int w, int h, int...fields) {
		width=w;
		height=h;		
		fieldOrdinals = new ArrayList<Integer>();
		for (int f : fields) fieldOrdinals.add(f);
	}
	RoomShape(int w, int h, List<Integer> fields) {
		width=w;
		height=h;
		fieldOrdinals = fields;
	}
	RoomShape(int w, int h, IntStream...streams) {
		width=w;
		height=h;
		// Merge into one list
		fieldOrdinals = new ArrayList<Integer>();
		for (IntStream s : streams) {
			fieldOrdinals.addAll(s.boxed().toList());
		}
	}
}