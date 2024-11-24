/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.world;

import java.util.List;

/**
 * 
 */
public interface IMUDItem {
	
	//-------------------------------------------------------------------
	/**
	 * Return a short name, suitable for the item when appearing in a list
	 * or inventory
	 * @return
	 */
	public String getName();

	public List<String> getAliases();
	
	//-------------------------------------------------------------------
	/**
	 * A name describing the item when it is lying around in a room 
	 * @return
	 */
	public String getLookName();
	
	//-------------------------------------------------------------------
	public String getDescription();
	
	//-------------------------------------------------------------------
	public <E> E getRuleSpecificData();
	
}
