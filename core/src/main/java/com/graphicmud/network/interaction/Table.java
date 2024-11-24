/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.network.interaction;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 */
public class Table<E> {

	@Getter
	private String title;
	@Setter
	@Getter
	private List<E> data = new ArrayList<E>();
	@Getter
	private List<TableColumn<E,?>> columns = new ArrayList<TableColumn<E,?>>();
	
	//-------------------------------------------------------------------
	public Table<E> addColumn(TableColumn<E,?> column) {
		columns.add(column);
		return this;
	}

	//-------------------------------------------------------------------
	public Table<E> setTitle(String value) {
		this.title = value;
		return this;
	}
}
