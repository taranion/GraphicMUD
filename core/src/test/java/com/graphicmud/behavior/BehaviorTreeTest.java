/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.behavior;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.graphicmud.MUD;
import com.graphicmud.MUD.MUDBuilder;
import com.graphicmud.action.SelectAsTarget;
import com.graphicmud.behavior.BehaviorRunner;
import com.graphicmud.behavior.CompositeNode;
import com.graphicmud.behavior.SelectorNode;
import com.graphicmud.behavior.TreeResult;
import com.graphicmud.behavior.TreeResult.Result;
import com.graphicmud.game.MobileEntity;

/**
 * 
 */
public class BehaviorTreeTest {

	//-------------------------------------------------------------------
	@Test
	public void testTreeParsing() throws Exception {
		DummyWorldCenter wc = new DummyWorldCenter();
		MUD mud = (new MUDBuilder())
				.setWorldCenter(wc)
				.build();
		CompositeNode root = new SelectorNode()
				.add(SelectAsTarget::playerInRoom);
		MobileEntity entity = new MobileEntity(null);
		TreeResult result = BehaviorRunner.heartbeat(root, entity);
		assertNotNull(result);
		//assertEquals(result.getInternalErrorMessage(),Result.SUCCESS, result.getValue());
	}

}
