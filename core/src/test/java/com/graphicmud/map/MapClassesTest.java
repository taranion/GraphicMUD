/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.map;

import static org.junit.Assert.assertNotNull;

import java.nio.file.Paths;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.graphicmud.map.MapPolygon;
import com.graphicmud.map.ANSIMapper.ColorMapping;
import com.graphicmud.map.ANSIMapper.GraphemeMapping;
import com.graphicmud.symbol.DefaultSymbolManager;
import com.graphicmud.symbol.Symbol;
import com.graphicmud.symbol.SymbolManager;
import com.graphicmud.symbol.SymbolSet;

/**
 * 
 */
public class MapClassesTest {

	//-------------------------------------------------------------------
	@Test
	public void testCave1() {
		MapPolygon map = new MapPolygon() {
			
			@Override
			public List<Object> getSites() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public List<Object> getNeighbors(Object site) {
				// TODO Auto-generated method stub
				return null;
			}
		};
		
//		map
//			.perform(LineOfSight::floodFill)
//			.foo();
	}

}
