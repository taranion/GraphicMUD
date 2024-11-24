/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.map;

import static org.junit.Assert.assertNotNull;

import java.nio.file.Paths;

import org.junit.BeforeClass;
import org.junit.Test;

import com.graphicmud.map.ANSIMap;
import com.graphicmud.map.ANSIMapper;
import com.graphicmud.map.LineOfSight;
import com.graphicmud.map.SymbolMap;
import com.graphicmud.map.ViewportMap;
import com.graphicmud.map.ANSIMapper.ColorMapping;
import com.graphicmud.map.ANSIMapper.GraphemeMapping;
import com.graphicmud.symbol.DefaultSymbolManager;
import com.graphicmud.symbol.Symbol;
import com.graphicmud.symbol.SymbolManager;
import com.graphicmud.symbol.SymbolSet;

/**
 * 
 */
public class MapAlgorithmsTest {

	private final static int[][] MAPU5 = new int[][] {
		{ 5, 5, 5, 5, 5, 5, 8,46, 5, 5},
		{ 5,79,79,74,79,79, 8, 8, 6, 5},
		{ 5,79,68,68,68,79,79,79,79,79},
		{ 5,74,68,68,68,79,164,164,79,68},
		{ 5,79,68,68,198,79,164,68,182,68},
		{ 5,79,79,79,79,79,79,79,79,169},
		{ 6, 8,79,169,170,68,169,170,79,68},
		{ 8, 8,79,68,68,68,68,68,79,92},
		{ 6,46,79,169,170,68,169,170,79,79},
		{ 5, 8,79,68,68,68,68,68,79, 6},
		{ 5, 6,79,169,170,68,169,170,79, 6}
	};
	
	private static SymbolManager manager;
	private static SymbolSet symbolSet;

	@BeforeClass
	public static void initializeSymbols() {
		manager = new DefaultSymbolManager(Paths.get("src/test/resources"), null);
		symbolSet = manager.getSymbolSet(4);
	}
	

	//-------------------------------------------------------------------
	@Test
	public void testCave1() {
		assertNotNull(symbolSet);
		ViewportMap<Symbol> map = new SymbolMap(MAPU5, symbolSet)
				.setPositionSelf(4, 4)
				.apply(LineOfSight::floodFill);
		ANSIMap out = (ANSIMap) map.convert(new ANSIMapper(GraphemeMapping.ASCII, ColorMapping.COL16));
		System.out.println(out.dump());
	}

}
