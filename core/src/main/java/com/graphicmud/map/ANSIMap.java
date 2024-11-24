/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 */
public class ANSIMap implements ViewportMap<String> {
	
	private int selfX;
	private int selfY;
	private int width;
	private int height;
	private String[][] data;

	//-------------------------------------------------------------------
	public ANSIMap(String[][] data) {
		this.data = data;
		this.width = data[0].length;
		this.height= data.length;
		selfX = (width+1)/2;
		selfY = (height+1)/2;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.map.Map2D#getWidth()
	 */
	@Override
	public int getWidth() {
		return width;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.map.Map2D#getHeight()
	 */
	@Override
	public int getHeight() {
		return height;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.map.Map2D#get(int, int)
	 */
	@Override
	public String get(int x, int y) {
		return data[y][x];
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.map.Map2D#set(int, int, java.lang.Object)
	 */
	@Override
	public void set(int x, int y, String val) {
		data[y][x] = val;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.map.ViewportMap#getPositionSelf()
	 */
	@Override
	public int[] getPositionSelf() {
		return new int[] {selfX, selfY};
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.map.ViewportMap#setPositionSelf(int, int)
	 */
	@Override
	public ViewportMap<String> setPositionSelf(int x, int y) {
		this.selfX = x;
		this.selfY = y;
		return this;
	}

	//-------------------------------------------------------------------
	public String dump() {
		StringBuilder ret = new StringBuilder();
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				ret.append(data[y][x]);				
			}
			ret.append("\u001b[0m\r\n");
		}
		return ret.toString();
	}

	//-------------------------------------------------------------------
	public List<String> getAsLines() {
		List<String> lines = new ArrayList<>();
		for (int y=0; y<height; y++) {
			StringBuilder ret = new StringBuilder();
			for (int x=0; x<width; x++) {
				ret.append(data[y][x]);				
			}
			//ret.append("\u001b[0m\r\n");
			lines.add(ret.toString());
		}
		return lines;
	}

	@Override
	public <X> void addLayer(String name, X[][] flood) {
		// TODO Auto-generated method stub
		
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.map.ViewportMap#reduceSize(int, int)
	 */
	@Override
	public void reduceSize(int radiusX, int radiusY) {
		// Validate
		int newWidth = radiusX*2 +1;
		int newHeight= radiusY*2 +1;
		if (newWidth > width) throw new IllegalArgumentException("X-Radius too large - max. "+((width-1)/2));
		if (newHeight>height) throw new IllegalArgumentException("Y-Radius too large - max. "+((height-1)/2));
		// Calculate Width-Shrinking
		if (newWidth<width) {
			int start = (width-newWidth)/2;
			for (int y=0; y<height; y++) {
				// Terrain Layer
				data[y] = Arrays.copyOfRange(data[y], start, start+newWidth-1);
			}
			width = newWidth;
		}
		// Calculate Height-Shrinking
		if (newHeight<height) {
			int start = (height-newHeight)/2;
			data = Arrays.copyOfRange(data, start, start+newHeight-1);
			height = newHeight;
		}
		selfX = (width+1)/2;
		selfY = (height+1)/2;
	}

}
