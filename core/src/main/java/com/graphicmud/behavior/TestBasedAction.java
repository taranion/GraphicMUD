/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.behavior;

import com.graphicmud.game.MUDEntity;

/**
 * 
 */
public interface TestBasedAction extends MUDAction {
	
	public static interface ITestProbability extends Comparable<ITestProbability> {}
	public static interface ITestResult {}
	
	//-------------------------------------------------------------------
	/**
	 * E.g. if a risk roll is necessary or if Edge needs to be spent.
	 * Write decision in context
	 */
	public void makeTacticalDecision(MUDEntity actor, Context context);
	
	//-------------------------------------------------------------------
	/**
	 * Calculate a probability according to the tactical decision
	 */
	public ITestProbability determineProbability(MUDEntity actor, Context context);

	//-------------------------------------------------------------------
	/**
	 * If the test was successful and the ITestResult is known, decide
	 * how to deal with it (e.g. spending additional points on damage).
	 * The output is expected to be shown here.
	 */
	public void performPOST(MUDEntity actor, Context context);
	
}
