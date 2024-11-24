/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.map;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.graphicmud.symbol.Symbol;
import com.graphicmud.world.tile.GridPosition;

/**
 *
 */
public class TempFileMap implements GridMap {

	private int width;
	private int height;
	private Path tempFile;
	private FileChannel readChannel;

	private transient Map<Layer, FileChannel> filePerLayer = new HashMap<>();

	//-------------------------------------------------------------------
	public TempFileMap(int width, int height, int chunkSize, Path tempFile) {
		// TODO Auto-generated constructor stub
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.map.GridMap#getArea(int, int, int)
	 */
	@Override
	public ViewportMap<Symbol> getArea(int centerX, int centerY, int layer, int rangeX, int rangeY, int outside) {
		// TODO Auto-generated method stub
		return null;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.map.GridMap#getWidth()
	 */
	@Override
	public int getWidth() {
		return width;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.map.GridMap#getHeight()
	 */
	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public List<Layer> getLayer() {
		return null;
	}

	//-------------------------------------------------------------------
	/**
	 * @param layerNr
	 * @param data
	 */
	public void writeLayer(int layerNr, ByteBuffer data) throws IOException {
		// Find layer and write data
		Optional<Layer> layer = filePerLayer.keySet().stream().filter(l -> l.nr()==layerNr).findFirst();
		if (layer.isPresent()) {
			filePerLayer.get(layer.get()).write(data);
		} else
			throw new IllegalArgumentException("Invalid layer "+layerNr+" - valid are "+filePerLayer.keySet());
	}

	@Override
	public int[] getRawLayerData(Layer layer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Symbol getSymbolAt(GridPosition position) {
		// TODO Auto-generated method stub
		return null;
	}

}
