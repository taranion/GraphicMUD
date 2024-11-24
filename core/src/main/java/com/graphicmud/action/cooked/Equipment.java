/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.action.cooked;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;

import com.graphicmud.action.raw.ChangeItemLocation;
import com.graphicmud.action.raw.Echo;
import com.graphicmud.behavior.Context;
import com.graphicmud.character.EquipmentPosition;
import com.graphicmud.character.EquippedGear;
import com.graphicmud.character.EquippedGear.WearResult;
import com.graphicmud.commands.CommandUtil;
import com.graphicmud.game.EntityFlag;
import com.graphicmud.game.ItemEntity;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MobileEntity;
import com.graphicmud.world.Location;

/**
 * 
 */
public class Equipment {

	private final static Logger logger = System.getLogger(Equipment.class.getPackageName());
	
	private final static String EQUIP_NO_TARGET      = "command.equipment.mess.no_target";
	private final static String EQUIP_NO_SUCH_ITEM   = "command.equipment.mess.nosuchitem";
	private final static String EQUIP_SELF           = "command.equipment.mess.self";
	private final static String EQUIP_OTHER          = "command.equipment.mess.other";
	
	public final static String EQUIP_CANNOT_WEAR     = "equippedgear.mess.cannotwear";
	public final static String EQUIP_ALREADY_EQUIPPED= "equippedgear.mess.alreadyEquipped";
	public final static String EQUIP_SLOT_USED       = "equippedgear.mess.slotused";
	private final static String REMOVE_NO_SUCH_ITEM  = "command.remove.mess.nosuchitem";
	private final static String REMOVE_SELF          = "command.remove.mess.self";
	private final static String REMOVE_OTHER         = "command.remove.mess.other";
	
	//-------------------------------------------------------------------
	private static List<ItemEntity> convertToItemEntityList(List<ItemEntity> toSelectFrom, String itemName) {
		if (CommandUtil.isAll(itemName))
			return toSelectFrom;
		boolean getAll = CommandUtil.isIndexAll(itemName);
		int index = 0;
		if (getAll) {
			// Strip the "all." from the item
			itemName = CommandUtil.extractItemnameFromIndexedAll(itemName);
		} else {
            CommandUtil.IndexedEntity indexedItem = CommandUtil.createIndexEntity(itemName);
            index    = indexedItem.getIndex();
            itemName = indexedItem.getItemName();
		}
		final String key = itemName;
        List<ItemEntity> list = toSelectFrom.stream().filter(e -> e.reactsOnKeyword(key)).toList();
        return (getAll)?list:(list.isEmpty()?List.of():List.of(list.get(index)));
	}
	
	//-------------------------------------------------------------------
	private static List<ItemEntity> convertToItemEntityList(Location room, String itemName) {
		return convertToItemEntityList(room.getEntities().stream()
				.filter(mi -> mi instanceof ItemEntity)
				.map(mi -> (ItemEntity)mi)
				.toList()
				, itemName);
	}
	
	//-------------------------------------------------------------------
	private static List<ItemEntity> convertToItemEntityList(MUDEntity container, String itemName) {
		logger.log(Level.WARNING, "Inventory of "+container+ "is "+container.getInventory());
		return convertToItemEntityList(container.getInventory().stream()
				.filter(mi -> mi instanceof ItemEntity)
				.map(mi -> (ItemEntity)mi)
				.toList()
				, itemName);
	}
	
	//-------------------------------------------------------------------
	public static CookedActionResult equip(MUDEntity actor, Context context) {
		// Expect a TARGET_NAME parameter describing an item in the current room
		String itemName = context.get(ParameterType.TARGET_NAME);
		if (itemName==null) {
			return new CookedActionResult(EQUIP_NO_TARGET);
		}
		
		// With or without container?
		CookedActionResult raw = new CookedActionResult(true,"equip", itemName);
    	// Get all matching items from the inventor<
    	List<ItemEntity> toEquip = convertToItemEntityList(actor, itemName);
    	logger.log(Level.INFO, "Convert ''{0}'' to {1} items", itemName, toEquip.size());
    	if (toEquip.isEmpty()) {
    		String name = CommandUtil.isIndexAll(itemName)?CommandUtil.extractItemnameFromIndexedAll(itemName):itemName;
			return new CookedActionResult(EQUIP_NO_SUCH_ITEM, name);
    	}
    	
    	// Try to equip all matching items
    	for (ItemEntity tmp : toEquip) {
    		// Get the allowed equipment slot
    		if (tmp.getTemplate()==null) {
    			continue;
    		}
    		EquipmentPosition slot = tmp.getTemplate().getEquipmentSlot();
    		EquippedGear equipped = ((MobileEntity)actor).getEquippedGear();
    		// Check if this item is wearable
    		if (slot==null || slot==EquipmentPosition.NONE) {
    			raw.add((new Echo(EQUIP_CANNOT_WEAR , tmp.getName(), slot))::sendSelf);
    		} else if (equipped.contains(tmp)) {
    			tmp.setEquippedAt(slot); // Just to be sure
    			raw.add((new Echo(EQUIP_ALREADY_EQUIPPED , tmp.getName(), slot))::sendSelf);
    		} else if (equipped.getNumEmptySlots(slot)<1) {
        		// Check if position is free
    			raw.add((new Echo(EQUIP_SLOT_USED , tmp.getName(), slot))::sendSelf);
    		} else {    		
    			int slotNum = (int) equipped.stream().filter(item -> item.getEquippedAt()==slot).count();
    		    raw.add( (new ChangeItemLocation(slot, tmp))::inventoryToEquipment);
    		    String i18nKey = "equippedgear.mess." + slot.name().toLowerCase();
//    	        return new WearResult(true,equipmentSlot, "equippedgear.mess." + equipmentSlot.name().toLowerCase(), itemToWear.getName(), getSlotName(equipmentSlot, slotNum));
    			raw.add( (new Echo(i18nKey , tmp.getName(), EquippedGear.getSlotName(slot, slotNum)))::sendSelf);
    			raw.add( (new Echo(EQUIP_OTHER, actor.getName(), tmp.getName()))::sendOthers);
    		}
    	}
		
		return raw;
	}
	
	//-------------------------------------------------------------------
	public static CookedActionResult remove(MUDEntity actor, Context context) {
		// Expect a TARGET_NAME parameter describing an item in the current room
		String itemName = context.get(ParameterType.TARGET_NAME);
		if (itemName==null) {
			return new CookedActionResult(EQUIP_NO_TARGET);
		}
		
		// With or without container?
		CookedActionResult raw = new CookedActionResult(true,"remove", itemName);
    	// Get all matching items from the equipped gear
		EquippedGear equipped = ((MobileEntity)actor).getEquippedGear();
		List<ItemEntity> toRemove = convertToItemEntityList(equipped, itemName);
    	logger.log(Level.INFO, "Convert ''{0}'' to {1} items", itemName, toRemove.size());
    	if (toRemove.isEmpty()) {
    		String name = CommandUtil.isIndexAll(itemName)?CommandUtil.extractItemnameFromIndexedAll(itemName):itemName;
			return new CookedActionResult(REMOVE_NO_SUCH_ITEM, name);
    	}
    	
    	// Try to unequip all matching items
    	for (ItemEntity tmp : toRemove) {
   		    raw.add( (new ChangeItemLocation(tmp))::equipmentToInventory);
		    //String i18nKey = "equippedgear.mess." + slot.name().toLowerCase();
   			raw.add( (new Echo(REMOVE_SELF , tmp.getName()))::sendSelf);
   			raw.add( (new Echo(REMOVE_OTHER, actor.getName(), tmp.getName()))::sendOthers);
    	}
		return raw;
	}
}
