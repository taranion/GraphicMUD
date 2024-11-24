/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.world.impl;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.prelle.mudansi.MarkupElement;

import com.graphicmud.game.MUDEntity;
import com.graphicmud.map.ViewportMap;
import com.graphicmud.symbol.Symbol;
import com.graphicmud.world.IMUDItem;
import com.graphicmud.world.Location;
import com.graphicmud.world.Surrounding;
import com.graphicmud.world.text.Direction;

import lombok.Getter;
import lombok.Setter;

/**
 *
 */
public class SurroundingImpl implements Surrounding {

	@Setter
	@Getter
	private Location location;
	@Setter
	@Getter
	private List<MarkupElement> title;
	@Setter
	@Getter
	private Path moodImage;
	@Setter
	@Getter
	private List<Direction> directions = new ArrayList<Direction>();
	@Setter
	private List<MarkupElement> description = new ArrayList<MarkupElement>();
	@Setter
	private ViewportMap<Symbol> map;
	@Setter
	@Getter
	private List<MUDEntity> lifeforms = new ArrayList<MUDEntity>();
	@Setter
	@Getter
	private List<IMUDItem> items = new ArrayList<IMUDItem>();

	@Setter
	@Getter
	private List<ColorLine> playerCharacterLines = new ArrayList<ColorLine>();

	@Setter
	@Getter
	private List<ColorLine> otherMobileCharacterLines = new ArrayList<ColorLine>();

	@Setter
	@Getter
	private List<ColorLine> itemLines = new ArrayList<ColorLine>();


	//-------------------------------------------------------------------
	public SurroundingImpl() {
	}

	//-------------------------------------------------------------------
	public SurroundingImpl(List<MarkupElement> descr) {
		this.description = descr;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.Surrounding#getDescription()
	 */
	@Override
	public List<MarkupElement> getDescription() {
		return description;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.Surrounding#getMap()
	 */
	@Override
	public ViewportMap<Symbol> getMap() {
		return map;
	}

	//-------------------------------------------------------------------
	/**
	 * @param value
	 */
	public void addItem(IMUDItem value) {
		if (!items.contains(value))
			this.items.add(value);
	}

	//-------------------------------------------------------------------
	/**
	 * @param value
	 * @return
	 */
	public boolean removeItem(IMUDItem value) {
		return this.items.remove(value);
	}

	//-------------------------------------------------------------------
	@Override
	public void addLifeform(MUDEntity value) {
		if (!lifeforms.contains(value))
			this.lifeforms.add(value);
	}
}
