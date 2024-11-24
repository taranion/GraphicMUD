/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.behavior;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.graphicmud.action.cooked.ParameterType;

/**
 * 
 */
public class Context {
	
	private Map<ParameterType, Object> wrapped = new HashMap<>();

	//-------------------------------------------------------------------
	public void put(ParameterType key, Object value) {
		if (!(key.getExpectedClass().isAssignableFrom(value.getClass()))) {
			throw new IllegalArgumentException(key+" must be of type "+key.getExpectedClass());
		}
		wrapped.put(key, value);
	}

	//-------------------------------------------------------------------
	public Set<ParameterType> keySet() {
		return wrapped.keySet();
	}

	//-------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	public <E> E get(ParameterType key) {
		return (E)wrapped.get(key);
	}

	//-------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	public <E> E remove(ParameterType key) {
		return (E) wrapped.remove(key);
	}
	
	//-------------------------------------------------------------------
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return wrapped.toString();
	}

	//-------------------------------------------------------------------
	public boolean containsKey(ParameterType key) {
		return wrapped.containsKey(key);
	}

}
