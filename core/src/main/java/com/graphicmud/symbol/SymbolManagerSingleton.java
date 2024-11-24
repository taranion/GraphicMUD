/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.symbol;

/**
 *
 */
public class SymbolManagerSingleton {

	private static SymbolManager instance;

	//-------------------------------------------------------------------
	public static SymbolManager getInstance() { return instance; }
	public static void setInstance(SymbolManager value) { instance = value; }

}
