/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.action.raw;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import com.graphicmud.action.cooked.ParameterType;
import com.graphicmud.behavior.Context;
import com.graphicmud.character.EquipmentPosition;
import com.graphicmud.game.ItemEntity;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MobileEntity;
import com.graphicmud.world.Location;

/**
 * 
 */
public class ChangeItemLocation {
	
	private final static Logger logger = System.getLogger(ChangeItemLocation.class.getPackageName());
	
	private ItemEntity item;
	private ItemEntity container;
	private EquipmentPosition position;

	//-------------------------------------------------------------------
	public ChangeItemLocation(ItemEntity item) {
		this.item = item;
	}

	//-------------------------------------------------------------------
	public ChangeItemLocation(ItemEntity container, ItemEntity item) {
		this.container = container;
		this.item = item;
	}

	//-------------------------------------------------------------------
	public ChangeItemLocation(EquipmentPosition position, ItemEntity item) {
		this.position = position;
		this.item = item;
	}

	//-------------------------------------------------------------------
	public void roomToInventory(MUDEntity performer, Context context) {
		Location room = context.get(ParameterType.ROOM_CURRENT);
		// Change position
		performer.addToInventory(item);
		room.removeEntity(item);
		// Remove position
		item.setPosition(null);
	}

	//-------------------------------------------------------------------
	public void inventoryToRoom(MUDEntity performer, Context context) {
		Location room = context.get(ParameterType.ROOM_CURRENT);
		// Change position
		performer.removeFromInventory(item);
		room.addEntity(item);
		// Add position
		item.setPosition(performer.getPosition());
	}

	//-------------------------------------------------------------------
	public void containerToInventory(MUDEntity performer, Context context) {
		logger.log(Level.DEBUG, "containerToInventory({0} from {1}", item, container);
		performer.addToInventory(item);
		container.removeFromInventory(item);
	}

	//-------------------------------------------------------------------
	public void inventoryToContainer(MUDEntity performer, Context context) {
		logger.log(Level.INFO, "inventoryToContainer({0} to {1}", item, container);
		performer.removeFromInventory(item);
		container.addToInventory(item);
	}

	//-------------------------------------------------------------------
	public void removeFromInventory(MUDEntity performer, Context context) {
		logger.log(Level.INFO, "removeFromInventory({0}", item);
		performer.removeFromInventory(item);
	}

	//-------------------------------------------------------------------
	public void inventoryToEquipment(MUDEntity performer, Context context) {
	    int slotNum = (int) ((MobileEntity)performer).getEquippedGear().stream().filter(item -> item.getEquippedAt()==position).count();
		performer.removeFromInventory(item);
		item.setEquippedAt(position);
		((MobileEntity)performer).getEquippedGear().add(item);
	}

	//-------------------------------------------------------------------
	public void equipmentToInventory(MUDEntity performer, Context context) {
		item.setEquippedAt(null);
		((MobileEntity)performer).getEquippedGear().remove(item);
		performer.addToInventory(item);
	}

}
