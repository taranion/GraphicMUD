/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.io;

public interface DataAction {

	//------------------------------------------------
	/**
	 * @see DataOperations
	 * @return Returns the action.
	 */
	public int getAction();

	//------------------------------------------------
	/**
	 * @see DataTypes
	 * @return Returns the type.
	 */
	public String getType();

	//------------------------------------------------
	/**
	 * @return Returns the result.
	 */
	public Object getResult();

	//------------------------------------------------
	/**
	 * Set the command parameter
	 * @param result The result to set.
	 */
	public void setParameter(Object data);

	//------------------------------------------------
	/**
	 * Get the command parameter
	 */
	public Object getParameter();

	//------------------------------------------------
	/**
	 * @param result The result to set.
	 */
	public void setResult(Object result);

}