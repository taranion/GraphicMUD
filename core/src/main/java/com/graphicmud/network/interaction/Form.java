/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.network.interaction;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Form {

	private String id;
	private String title;
	private List<TextField> items;

	//-------------------------------------------------------------------
	public Form(String id, String title) {
		this.id = id;
		this.title = title;
		items = new ArrayList<>();
	}

	//-------------------------------------------------------------------
	public Form add(TextField item) {
		items.add(item);
		return this;
	}
	//-------------------------------------------------------------------
	public Form add(TextField... data) {
		for (TextField tmp : data)
			items.add(tmp);
		return this;
	}

	//-------------------------------------------------------------------
	public List<TextField> getItems() {
		return items;
	}

	//-------------------------------------------------------------------
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	//-------------------------------------------------------------------
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
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

}
