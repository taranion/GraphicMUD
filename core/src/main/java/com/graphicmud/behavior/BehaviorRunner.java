/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.behavior;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;
import java.util.function.BiFunction;

import com.graphicmud.behavior.TreeResult.Result;
import com.graphicmud.game.MUDEntity;

/**
 * 
 */
public class BehaviorRunner {
	
	private final static Logger logger = System.getLogger(BehaviorRunner.class.getPackageName());

	//-------------------------------------------------------------------
	public static TreeResult heartbeat(CompositeNode root, MUDEntity entity) {
		Context context = new Context();
		
		TreeResult result = root.apply(entity, context);
//		logger.log(Level.INFO, "Result is {0}", result);
		return result;
	}

	//-------------------------------------------------------------------
	public static TreeResult performTests(List<BiFunction<MUDEntity,Context,TreeResult>> tests, MUDEntity performer, Context context) {
		for (BiFunction<MUDEntity,Context,TreeResult> test : tests) {
			TreeResult result = test.apply(performer, context);
			if (result.getValue()!=Result.SUCCESS)
				return result;
		}
		return new TreeResult(true);
	}
}
