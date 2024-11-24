/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.action.cooked;

import com.graphicmud.Identifier;
import com.graphicmud.character.EquipmentPosition;
import com.graphicmud.combat.Combat;
import com.graphicmud.game.ItemEntity;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MobileEntity;
import com.graphicmud.world.Location;
import com.graphicmud.world.Position;
import com.graphicmud.world.text.Direction;
import com.graphicmud.world.text.Exit;

public enum ParameterType {
	ANY(Object.class),
	COMBAT(Combat.class),
	CONTAINER_NAME(String.class),
	COOKED_ACTION(CookedAction.class),
	/** Not a real item, but something hardcoded by the RPG */
	CREATURE_WEAPON(Object.class),
	DIRECTION(Direction.class),
	EQUIP_POSITION(EquipmentPosition.class),
	EXIT(Exit.class),
	EXIT_TARGET(Exit.class),
	IDENTIFIER(Identifier.class),
	ITEM(ItemEntity.class),
	LOCAL_IDENTIFIER(Identifier.class),
	MOBILE(MobileEntity.class),
	POSITION_CURRENT(Position.class),
	POSITION_TARGET(Position.class),
	ROOM_CURRENT(Location.class),
	ROOM_TARGET(Location.class),
	RPG_WEAPON(Object.class),
	PERFORMED_BY(MUDEntity.class),
//	SOCIAL(SocialType.class),
	SOURCE(MUDEntity.class),
	TARGET(MUDEntity.class),
	/** Name of an MUDEntity at the same location */
	TARGET_NAME(String.class),
	TEXT(String.class),
	TEXT_DATA(Object[].class),
 	;
	Class<?> clazz;
	ParameterType(Class<?> cls) {
		clazz=cls;
	}
	public Class<?> getExpectedClass() {
		return clazz;
	}
}