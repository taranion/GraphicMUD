/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.ecs;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import com.graphicmud.action.ActOnTarget;
import com.graphicmud.action.EventListener;
import com.graphicmud.action.SelectAsTarget;
import com.graphicmud.action.script.OnEventXML;
import com.graphicmud.behavior.BehaviorRunner;
import com.graphicmud.behavior.CompositeNode;
import com.graphicmud.behavior.SequenceNode;
import com.graphicmud.behavior.TreeResult;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MUDEvent.Type;

/**
 * Every pulse decide if to attack someone in the same room
 */
public class BehaviorComponent extends Component {
	
	private final static Logger logger = System.getLogger(BehaviorComponent.class.getName());

	private CompositeNode root;
	
	//-------------------------------------------------------------------
	public BehaviorComponent() {
		root = new SequenceNode()
				.add(SelectAsTarget::playerInRoom)
				.add(ActOnTarget::follow)
//				.add((new EventListener(,Type.LEAVE_ROOM,))::listenInCurrentRoom)
//				.add(OnEvent::execute)
				;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.ecs.Component#pulse(com.graphicmud.game.MUDEntity)
	 */
	@Override
	public void pulse(MUDEntity entity) {
//		logger.log(Level.INFO, "pulse for BT");
//		TreeResult result = BehaviorRunner.heartbeat(root, entity);
//		logger.log(Level.INFO, "  result "+result);
	}

}
