/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.symbol;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.prelle.simplepersist.Attribute;
import org.prelle.simplepersist.Element;
import org.prelle.simplepersist.ElementList;
import org.prelle.simplepersist.Root;

/**
 *
 */
@Root(name="symbolset")
public class SymbolSet {

	@Attribute(name="nr")
	protected int id;
	@Attribute
	protected String title;
	@Element
	protected String imageFile;
	@Attribute(name="tilesize")
	protected int tileSize;
	@Element
	@ElementList(entry="symbol",type = Symbol.class)
	private List<Symbol> symbols;
	private transient Map<Integer,Symbol> symbolMap;
	private transient Path file;

	//-------------------------------------------------------------------
	public SymbolSet() {
		symbols   = new ArrayList<>();
		symbolMap = new HashMap<Integer, Symbol>();
	}
	//-------------------------------------------------------------------
	public SymbolSet(int id) {
		this();
		this.id = id;
	}

	//-------------------------------------------------------------------
	public void addSymbol(Symbol value) {
		symbols.add(value);
		symbolMap.put(value.getId(), value);
	}

	//-------------------------------------------------------------------
	public Symbol getSymbol(int i) {
		if (i<0) return null;
		return symbols.get(i);
	}

	//-------------------------------------------------------------------
	public int size() { return symbols.size(); }

	//-------------------------------------------------------------------
	public List<Symbol> asList() {
		return symbols;
	}

	//-------------------------------------------------------------------
	/**
	 * @return the imageFile
	 */
	public String getImageFile() {
		return imageFile;
	}

	//-------------------------------------------------------------------
	/**
	 * @param imageFile the imageFile to set
	 */
	public void setImageFile(String imageFile) {
		this.imageFile = imageFile;
	}

	//-------------------------------------------------------------------
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	//-------------------------------------------------------------------
	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	//-------------------------------------------------------------------
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	//-------------------------------------------------------------------
	/**
	 * @return the tileSize
	 */
	public int getTileSize() {
		return tileSize;
	}
	//-------------------------------------------------------------------
	/**
	 * @param tileSize the tileSize to set
	 */
	public void setTileSize(int tileSize) {
		this.tileSize = tileSize;
	}
	//-------------------------------------------------------------------
	/**
	 * @return the file
	 */
	public Path getFile() {
		return file;
	}
	//-------------------------------------------------------------------
	/**
	 * @param file the file to set
	 */
	public void setFile(Path file) {
		this.file = file;
	}

}
