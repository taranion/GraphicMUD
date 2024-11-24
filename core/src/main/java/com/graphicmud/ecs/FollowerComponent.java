/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.ecs;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.graphicmud.MUD;
import com.graphicmud.commands.impl.Movement;
import com.graphicmud.game.EntityState;
import com.graphicmud.game.EntityType;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MUDEvent;
import com.graphicmud.game.MUDEvent.Type;
import com.graphicmud.world.Position;
import com.graphicmud.world.Range;
import com.graphicmud.world.text.Direction;

/**
 * Every pulse decide if to move or stay at the same spot.
 */
public class FollowerComponent extends Component {
	
	private final static Logger logger = System.getLogger(FollowerComponent.class.getName());
	private final static Random RANDOM = new Random();
	
	private final static String VAR_TARGET = "TARGET";
	private final static String VAR_NEXT_DIRECTION = "NEXT_DIRECTION";

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.ecs.Component#pulse(com.graphicmud.game.MUDEntity)
	 */
	@Override
	public void pulse(MUDEntity entity) {
		// If we know where to go, let us do that
		Direction direction = entity.getComponentData(this, VAR_NEXT_DIRECTION);
		if (direction!=null) {
			entity.clearComponentData(this, VAR_NEXT_DIRECTION);
			MUD.getInstance().getCommandManager().execute(Movement.class, entity, 
					Map.of("dir",direction));
		}

		MUDEntity target = entity.getComponentData(this, VAR_TARGET);
		// If we don't have a target, pick one
		if (target==null) {
			Position pos = entity.getPosition();
			if (pos.getRoomPosition()!=null) {
				List<MUDEntity> otherPlayers = MUD.getInstance().getWorldCenter().getLifeformsInRangeExceptSelf(entity, Range.SURROUNDING).stream()
					.filter(lf -> lf.getType()==EntityType.PLAYER)
					.toList();
					;
				if (otherPlayers.isEmpty())
					return;
				if (entity.getState()!=EntityState.IDLE)
					return;
				int toAttack = RANDOM.nextInt(otherPlayers.size());
				target = otherPlayers.get(toAttack);
				entity.storeComponentData(this, VAR_TARGET, target);
//				MUD.getInstance().getCommandManager().execute(
//						SocialCommand.class,entity, 
//						Map.of("type", SocialType.GIGGLE,
//								"target", target.getName())
//						);
			}			
		}

	}
	
	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.ecs.Component#handleEvent(com.graphicmud.game.MUDEntity, com.graphicmud.game.MUDEvent)
	 */
	public void handleEvent(MUDEntity you, MUDEvent event) {
		if (event.getType()==Type.LEAVE_ROOM) {
			MUDEntity whoLeft = event.getSource();
			Direction where  = event.getData();
			MUDEntity toFollow = you.getComponentData(this, VAR_TARGET);
			if (whoLeft==toFollow) {
				you.storeComponentData(this, VAR_NEXT_DIRECTION, where);
				logger.log(Level.INFO, "Go "+where+" next pulse");
			}
		}
	}

}
