/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.map;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.graphicmud.MUD;
import com.graphicmud.symbol.Symbol;
import com.graphicmud.symbol.SymbolSet;
import com.graphicmud.world.tile.GridPosition;

/**
 *
 */
public class InMemoryMap implements GridMap {
	
	private final static Logger logger = System.getLogger(InMemoryMap.class.getPackageName());

//	public static record Chunk(int width, int height, int x, int y, int[] data) {
//	}
//
//	public class Layer {
//		@Attribute
//		private Integer id;
//		@Attribute
//		private String name;
//		@Attribute
//		private Integer width;
//		@Attribute
//		private Integer height;
//	}

//	private List<Chunk> data;
	private int width;
	private int height;

	private Map<Layer, int[]> layers = new HashMap<>();

	//-------------------------------------------------------------------
	public InMemoryMap(int width, int height) {
//		data = new ArrayList<>();
		this.width = width;
		this.height = height;
	}

//	public void addChunk(Chunk value) {
//		data.add(value);
//	}

	//---------------------------------------------------------------
	/**
	 * Returns a part of the map. The coordinates define the center
	 * of the of the viewport while the range means how many elements
	 * wide the viewport is in each direction (like a radius).
	 *
	 * @param posX The X-Position of the centre
	 * @param posY The Y-Position of the centre
	 * @param range The radius
	 */
	@Override
	public ViewportMap<Symbol> getArea(int posX, int posY, int layerNr, int rangeX, int rangeY, int outside) {
		if (layerNr==0)
			layerNr = layers.keySet().iterator().next().nr();
		final int fixLayer = layerNr;
		Optional<Layer> layer = layers.keySet().stream().filter(l -> l.nr()==fixLayer).findFirst();
		if (!layer.isPresent())
			throw new IllegalArgumentException("No such layer "+layerNr);
		if (posX>=width || posX<0)
			throw new IllegalArgumentException("X-Coordinate out of range. (0.."+(width-1)+")");
		if (posY>=height || posY<0)
			throw new IllegalArgumentException("Y-Coordinate out of range. (0.."+(height-1)+")");
		if (rangeX<0 || rangeY<0)
			throw new IllegalArgumentException("Range must be positive");

		int line = 2*rangeX + 1;
		int column = 2*rangeY + 1;
		int[][] ret = new int[column][line];
		int[] data = layers.get(layer.get());
		
		// Theoretical viewport
		int viewX1 = posX - rangeX;
		int viewY1 = posY - rangeY;
		int viewX2   = posX + rangeX+1;
		int viewY2   = posY + rangeY+1;
		logger.log(Level.INFO, "Theoretical viewport: {0},{1} - {2},{3}", viewX1,viewY1, viewX2,viewY2);
		// Corrected viewport
		int realX1 = (viewX1<0)?0:viewX1;
		int realY1 = (viewY1<0)?0:viewY1;
		int realX2 = (viewX2>=width)?(width-1):viewX2;
		int realY2 = (viewY2>=height)?(height-1):viewY2;
		logger.log(Level.INFO, "Corrected viewport  : {0},{1} - {2},{3}", realX1,realY1, realX2,realY2);

		// Move start line if outside the map
		int offsetX = realX1 - viewX1;
		int offsetY = realY1 - viewY1;
		int realW   = realX2 - realX1;
		int realH   = realY2 - realY1;
		// Blank 
		for (int y=0; y<column; y++) {
			Arrays.fill(ret[y], outside);
		}
		
		// Start outer loop
		for (int y=realY1; y<realY2; y++) {
			int length = realW;
			int copyStart = y*width + realX1;
			System.arraycopy(data, copyStart, ret[offsetY++], offsetX, length);
					//System.out.println("Read "+Arrays.toString(ret[y]));
		} // Y
		
		SymbolSet symbolSet = MUD.getInstance().getSymbolManager().getSymbolSet(4);
		SymbolMap map = new SymbolMap(ret, symbolSet);
		return map;
	}

//	//-------------------------------------------------------------------
//	/**
//	 * @return the data
//	 */
//	public List<Chunk> getData() {
//		return data;
//	}

	//-------------------------------------------------------------------
	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	//-------------------------------------------------------------------
	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	//-------------------------------------------------------------------
	public void addLayer(Layer gLayer, int[] data) {
		layers.put(gLayer, data);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.map.GridMap#getLayer()
	 */
	@Override
	public List<Layer> getLayer() {
		return new ArrayList<>(layers.keySet());
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.map.GridMap#getRawLayerData(com.graphicmud.map.GridMap.Layer)
	 */
	@Override
	public int[] getRawLayerData(Layer layer) {
		return layers.get(layer);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.map.GridMap#getSymbolAt(com.graphicmud.world.tile.GridPosition)
	 */
	@Override
	public Symbol getSymbolAt(GridPosition position) {
		int index = position.getY()*width + position.getX();
		SymbolSet symbolSet = MUD.getInstance().getSymbolManager().getSymbolSet(4);
		int sym = layers.values().iterator().next()[index];
		return symbolSet.getSymbol(sym);
	}

}
