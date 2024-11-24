/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.behavior;

import java.lang.System.Logger.Level;
import java.util.function.BiFunction;

import com.graphicmud.behavior.TreeResult.Result;
import com.graphicmud.game.MUDEntity;

/**
 * Executes children in order until one succeeds
 */
public class SelectorNode extends CompositeNode {
	
	public String toString() { return "SELECTOR("+id+")"; }

	//-------------------------------------------------------------------
	/**
	 */
	public SelectorNode() {
		// TODO Auto-generated constructor stub
	}

	//-------------------------------------------------------------------
	/**
	 * @see java.util.function.Function#apply(java.lang.Object)
	 */
	@Override
	public TreeResult apply(MUDEntity performer, Context context) {
		logger.log(Level.DEBUG, "ENTER({0})", this);
		TreeResult result = null;
		try {
			for (BiFunction<MUDEntity, Context, TreeResult> child : children) {
				result = child.apply(performer, context);
				if (result==null) {
					logger.log(Level.WARNING, "Child {0} returned NULL", child);
					throw new NullPointerException("Child "+child+" of parent "+this+" returned NULL");
				}
				if (result.getValue()!=Result.FAILURE)
					return result;
			}
			return result;
		} finally {
			logger.log(Level.DEBUG, "LEAVE({0}) with {1}", this, result);
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.behavior.MUDAction#conditionsMet(com.graphicmud.game.MUDEntity, com.graphicmud.behavior.Context)
	 */
	@Override
	public boolean conditionsMet(MUDEntity actor, Context context) {
		// TODO Auto-generated method stub
		return true;
	}

}
