/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.network.interaction;

import java.util.function.BiFunction;
import java.util.function.Function;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 */
@Getter
@Setter
public class TableColumn<E,T> {
	
	private String name;
	private BiFunction<Table<E>,E,T> valueProvider;
	private Function<Object,String> renderer;
	private int maxWidth;
	private Function<E,String> mxpLinkProvider;
	
	public TableColumn(String name) {
		this.name = name;
	}
	
	public TableColumn<E, T> setValueProvider(BiFunction<Table<E>,E,T> provider) {
		this.valueProvider=provider;
		return this;	
	}
	
	public TableColumn<E, T> setMxpLinkProvider(Function<E,String> provider) {
		this.mxpLinkProvider=provider;
		return this;	
	}
	
	public TableColumn<E, T> setRenderer(Function<?,String> renderer) {
		this.renderer=(Function<Object, String>) renderer;
		return this;	
	}
	
	public TableColumn<E, T> setMaxWidth(int value) {
		this.maxWidth = value;
		return this;	
	}

}
