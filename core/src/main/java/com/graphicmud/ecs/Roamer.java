/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.ecs;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;
import java.util.Random;

import org.prelle.simplepersist.Attribute;

import com.graphicmud.MUD;
import com.graphicmud.action.SelectDirection;
import com.graphicmud.action.cooked.Walk;
import com.graphicmud.behavior.BehaviorRunner;
import com.graphicmud.behavior.CompositeNode;
import com.graphicmud.behavior.SequenceNode;
import com.graphicmud.behavior.TreeResult;
import com.graphicmud.commands.impl.Movement;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.world.Location;
import com.graphicmud.world.NoSuchPositionException;
import com.graphicmud.world.Position;
import com.graphicmud.world.WorldCenter;
import com.graphicmud.world.text.Exit;
import com.graphicmud.world.text.RoomPosition;

/**
 * Every pulse decide if to move or stay at the same spot.
 */
public class Roamer extends Component {
	
	private final static Logger logger = System.getLogger(Roamer.class.getName());
	
	private final static String WAIT = "WAIT";
	
	private static Random random = new Random();
	
	@Attribute(name="prob")
	private int probability = 10;
	@Attribute(name="wait")
	private int waitBetween  = 50;

	private CompositeNode root;
	
	//-------------------------------------------------------------------
	public Roamer() {
		root = new SequenceNode()
				.add(SelectDirection::randomExit)
				.add(Walk::selectedDirection)
				;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.ecs.Component#pulse(com.graphicmud.game.MUDEntity)
	 */
	@Override
	public void pulse(MUDEntity entity) {
		Integer needWait = entity.getComponentData(this, WAIT);
		if (needWait!=null && needWait>0) {
			entity.storeComponentData(this, WAIT, needWait-1);
			return;
		}
		
		int r = random.nextInt(100);
		if (r>probability)
			return;
		
		logger.log(Level.INFO, "pulse for BT");
		TreeResult result = BehaviorRunner.heartbeat(root, entity);
		logger.log(Level.INFO, "  result "+result);
		
		entity.storeComponentData(this, WAIT, waitBetween);
	}

}
