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
 * ALL child nodes must be processed in order
 */
public class SequenceNode extends CompositeNode {
	
	public String toString() { return "SEQUENCE("+id+")"; }

	//-------------------------------------------------------------------
	/**
	 * @see java.util.function.Function#apply(java.lang.Object)
	 */
	@Override
	public TreeResult apply(MUDEntity performer, Context context) {
		logger.log(Level.TRACE, "ENTER({0})", this);
		TreeResult result = null;
		try {
			for (BiFunction<MUDEntity, Context, TreeResult> child : children) {
				result = child.apply(performer, context);
				if (result==null || result.getValue()!=Result.SUCCESS)
					return result;
			}
			return result;
		} finally {
			logger.log(Level.TRACE, "LEAVE({0}) with {1}", this, result);
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

//	//-------------------------------------------------------------------
//	/**
//	 * @see org.prelle.mud.behavior.BehaviorTreeNode#add(java.util.function.BiConsumer)
//	 */
//	@Override
//	public <E extends BehaviorTreeNode<T>> E add(BiConsumer<MUDEntity, Context<T>> cons) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void accept(MUDEntity t, Context<T> u) {
//		// TODO Auto-generated method stub
//		
//	}

}
