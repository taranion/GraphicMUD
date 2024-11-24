/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.network.interaction;

import java.util.ArrayList;
import java.util.List;
import java.util.PropertyResourceBundle;

import lombok.Getter;
import lombok.Setter;

/**
 *
 */
@Getter
public class Menu {

	@Setter
	private PropertyResourceBundle i18n;
	@Setter
	private String title;
	@Setter
	private String message;
	private List<MenuItem<?>> items;

	//-------------------------------------------------------------------
	public Menu(String title) {
		this.title = title;
		items = new ArrayList<>();
	}

	//-------------------------------------------------------------------
	public Menu add(MenuItem<?> item) {
		items.add(item);
		return this;
	}

}
