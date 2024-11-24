/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.ecs;

import java.lang.System.Logger;
import java.util.List;
import java.util.Random;

import com.graphicmud.MUD;
import com.graphicmud.game.EntityState;
import com.graphicmud.game.EntityType;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MobileEntity;
import com.graphicmud.game.Pose;
import com.graphicmud.world.Position;
import com.graphicmud.world.Range;
import com.graphicmud.world.WorldCenter;

/**
 * Every pulse decide if to attack someone in the same room
 */
public class AggroComponent extends Component {
	
	private final static Logger logger = System.getLogger(AggroComponent.class.getName());
	private final static Random RANDOM = new Random();

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.ecs.Component#pulse(com.graphicmud.game.MUDEntity)
	 */
	@Override
	public void pulse(MUDEntity entity) {
		Position pos = entity.getPosition();
		if (pos==null) throw new NullPointerException("Entity "+entity.getName()+" has no position");
		WorldCenter wc = MUD.getInstance().getWorldCenter();
		
		if (pos.getRoomPosition()!=null) {
			List<MUDEntity> otherPlayers = wc.getLifeformsInRangeExceptSelf(entity, Range.SURROUNDING).stream()
				.filter(lf -> lf.getType()==EntityType.PLAYER)
				.filter(lf -> ((MobileEntity)lf).getPose()!=Pose.DEAD)
				.toList();
				;
			if (otherPlayers.isEmpty())
				return;
			if (entity.getState()!=EntityState.IDLE)
				return;
			int toAttack = RANDOM.nextInt(otherPlayers.size());
			MUDEntity target = otherPlayers.get(toAttack);
			MUD.getInstance().getCommandManager().parse(entity, "attack "+target.getName());
		}
	}

}
