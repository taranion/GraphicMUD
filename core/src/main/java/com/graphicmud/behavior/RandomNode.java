/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.behavior;

import com.graphicmud.game.MUDEntity;

/**
 * 
 */
public class RandomNode extends CompositeNode {
	
	public String toString() { return "RANDOM("+id+")"; }

	//-------------------------------------------------------------------
	/**
	 * @see java.util.function.Function#apply(java.lang.Object)
	 */
	@Override
	public TreeResult apply(MUDEntity performer, Context t) {
		// TODO Auto-generated method stub
		return null;
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
