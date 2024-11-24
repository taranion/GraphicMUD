/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.network.interaction;

/**
 *
 */
public class TextField {

	private String id;
	private String name;
	private String placeholder;
	private int minLength;
	private int maxLength;

	//-------------------------------------------------------------------
	public TextField(String id, String name) {
		this.id = id;
		this.name = name;
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
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	//-------------------------------------------------------------------
	/**
	 * @return the placeholder
	 */
	public String getPlaceholder() {
		return placeholder;
	}

	//-------------------------------------------------------------------
	/**
	 * @param placeholder the placeholder to set
	 */
	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
	}

	//-------------------------------------------------------------------
	/**
	 * @return the minLength
	 */
	public int getMinLength() {
		return minLength;
	}

	//-------------------------------------------------------------------
	/**
	 * @param minLength the minLength to set
	 */
	public void setMinLength(int minLength) {
		this.minLength = minLength;
	}

	//-------------------------------------------------------------------
	/**
	 * @return the maxLength
	 */
	public int getMaxLength() {
		return maxLength;
	}

	//-------------------------------------------------------------------
	/**
	 * @param maxLength the maxLength to set
	 */
	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

}
