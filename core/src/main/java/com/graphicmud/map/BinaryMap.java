/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.map;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class BinaryMap implements GeneratedMap<Boolean> {
	
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";
	
	private int width;
	private int height;
	
	private boolean[][] data;
	private List<Square> rooms;

	//-------------------------------------------------------------------
	BinaryMap(int w, int h) {
		this.width=w;
		this.height=h;
		data = new boolean[h][w];
		rooms = new ArrayList<GeneratedMap.Square>();
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.map.GeneratedMap#getWidth()
	 */
	@Override
	public int getWidth() {
		return width;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.map.GeneratedMap#getHeight()
	 */
	@Override
	public int getHeight() {
		return height;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.map.GeneratedMap#get(int, int)
	 */
	@Override
	public Boolean get(int x, int y) {
		if (x<0 || x>=width) return true;
		if (y<0 || y>=height) return true;
		return data[y][x];
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.map.GeneratedMap#set(int, int, java.lang.Object)
	 */
	@Override
	public void set(int x, int y, Boolean val) {
		data[y][x]=val;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.map.GeneratedMap#isWall(int, int)
	 */
	@Override
	public boolean isWall(int x, int y) {
		return get(x,y);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.map.GeneratedMap#replaceWith(java.lang.Object[][])
	 */
	@Override
	public void replaceWith(Boolean[][] newMap) {
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				data[y][x] = newMap[y][x];
			}
		}
	}

	//-------------------------------------------------------------------
	public String printMap() {
		StringBuilder builder = new StringBuilder();
		builder.append("     012345678901234567890123456789012345678901234567890123456789\n");
		for (int y=0; y<height; y++) {
			builder.append(String.format("%03d  ", y));
			for (int x=0; x<width; x++) {
				if (overlapsWithRoom(x, y)) {
					builder.append(ANSI_RED);
				} else {
					builder.append(ANSI_RESET);
				}
				builder.append( data[y][x]?'#':'.');
			}
			builder.append(ANSI_RESET+"\r\n");
		}
		return builder.toString();
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.map.GeneratedMap#assignRoom(int, int, int, int)
	 */
	@Override
	public Square assignRoom(int fromX, int toX, int fromY, int toY) {
		Square room = new Square(rooms.size()+1,null,fromX, toX, fromY, toY);
		rooms.add(room);
		return room;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.map.GeneratedMap#getRooms()
	 */
	@Override
	public List<Square> getRooms() {
		return rooms;
	}

}
