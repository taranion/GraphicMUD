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
import com.graphicmud.commands.CommandUtil;
import com.graphicmud.game.EntityFlag;
import com.graphicmud.game.ItemEntity;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.world.Location;

/**
 * 
 */
public class Inventory {

	private final static Logger logger = System.getLogger(Inventory.class.getPackageName());
	
	private final static String GET_NO_TARGET      = "action.error.get.no_target";
	private final static String GET_NO_SUCH_ITEM   = "command.get.mess.nosuchitem";
	private final static String GET_NO_SUCH_ITEM_INSIDE = "command.get.mess.nosuchiteminside";
	private final static String GET_SELF           = "command.get.mess.self";
	private final static String GET_OTHER          = "command.get.mess.other";
	private final static String GET_FROM_SELF      = "command.get.mess.get.from.self";
	private final static String GET_FROM_OTHER     = "command.get.mess.get.from.other";
	private final static String DROP_SELF          = "command.drop.mess.self";
	private final static String DROP_OTHER         = "command.drop.mess.other";
	private final static String DROP_NO_TARGET     = "command.drop.mess.notarget";
	private final static String DROP_NO_SUCH_ITEM  = "command.drop.mess.nosuchitem";
	private final static String PUT_NO_TARGET      = "action.error.put.no_target";
	private final static String PUT_SELF           = "command.put.mess.self";
	private final static String PUT_OTHER          = "command.put.mess.other";
	private final static String PUT_NO_SUCH_TARGET = "command.put.mess.nosuchtarget";
	private final static String PUT_NOT_A_CONTAINER= "command.put.mess.notacontainer";
	private final static String JUNK_SELF          = "command.junk.mess.self";
	private final static String JUNK_OTHER         = "command.junk.mess.other";
	
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
	public static CookedActionResult get(MUDEntity actor, Context context) {
		// Expect a TARGET_NAME parameter describing an item in the current room
		String itemName = context.get(ParameterType.TARGET_NAME);
		if (itemName==null) {
			return new CookedActionResult(GET_NO_TARGET);
		}
		String containerName = context.get(ParameterType.CONTAINER_NAME);
		Location room = context.get(ParameterType.ROOM_CURRENT);
    	logger.log(Level.INFO, "Itemname  {0}", itemName);
    	logger.log(Level.INFO, "Container {0}", containerName);
		
		// With or without container?
		CookedActionResult raw = new CookedActionResult(true,"get", itemName);
        if (containerName == null || containerName.isBlank()) {
        	// Without container - get from room
        	List<ItemEntity> toGet = convertToItemEntityList(room, itemName);
        	logger.log(Level.INFO, "Convert ''{0}'' to {1} items", itemName, toGet.size());
        	if (toGet.isEmpty()) {
        		String name = CommandUtil.isIndexAll(itemName)?CommandUtil.extractItemnameFromIndexedAll(itemName):itemName;
    			return new CookedActionResult(GET_NO_SUCH_ITEM, name);
        	}
        	
        	for (ItemEntity tmp : toGet) {
        		raw.add( (new ChangeItemLocation(tmp))::roomToInventory);
        		raw.add( (new Echo(GET_SELF , tmp.getName()))::sendSelf);
        		raw.add( (new Echo(GET_OTHER, actor.getName(), tmp.getName()))::sendOthers);
        	}
        } else {
        	// With container - get list of containers
        	List<ItemEntity> toGetFrom = convertToItemEntityList(room, containerName);
        	logger.log(Level.INFO, "Convert ''{0}'' to {1} container items", containerName, toGetFrom.size());
        	if (toGetFrom.isEmpty()) {
        		String name = CommandUtil.isIndexAll(containerName)?CommandUtil.extractItemnameFromIndexedAll(containerName):containerName;
    			return new CookedActionResult(GET_NO_SUCH_ITEM, name);
        	}
        	logger.log(Level.INFO, "Containers to inspect: {0}", toGetFrom);
       	// Iterate over containers
        	for (ItemEntity container : toGetFrom) {
        		// Check for matching items
        		List<ItemEntity> getFromContainer = convertToItemEntityList(container, itemName);
               	logger.log(Level.INFO, "Convert ''{0}'' to {1} content items in {2}", itemName, getFromContainer.size(), container);
            	if (getFromContainer.isEmpty()) {
            		String name = CommandUtil.isIndexAll(containerName)?CommandUtil.extractItemnameFromIndexedAll(containerName):containerName;
        			return new CookedActionResult(GET_NO_SUCH_ITEM_INSIDE, itemName, container.getName());
            	}
            	for (ItemEntity tmp : getFromContainer) {
            		// and move them
            		raw.add( (new ChangeItemLocation(container,tmp))::containerToInventory);
            		raw.add( (new Echo(GET_FROM_SELF, tmp.getName(), container.getName()))::sendSelf);
            		raw.add( (new Echo(GET_FROM_OTHER, actor.getName(), tmp.getName(), container.getName()))::sendOthers);
            	}
        	}
       }
		
		return raw;
	}
	
	//-------------------------------------------------------------------
	public static CookedActionResult drop(MUDEntity actor, Context context) {
		// Expect a TARGET_NAME parameter describing an item in the current room
		String itemName = context.get(ParameterType.TARGET_NAME);
		if (itemName==null) {
			return new CookedActionResult(DROP_NO_TARGET);
		}
		
		CookedActionResult raw = new CookedActionResult(true,"drop", itemName);
    	List<ItemEntity> toDrop = convertToItemEntityList(actor, itemName);
    	logger.log(Level.INFO, "Convert ''{0}'' to {1} items", itemName, toDrop.size());
    	if (toDrop.isEmpty()) {
    		String name = CommandUtil.isIndexAll(itemName)?CommandUtil.extractItemnameFromIndexedAll(itemName):itemName;
			return new CookedActionResult(DROP_NO_SUCH_ITEM, name);
    	}
    	
    	for (ItemEntity tmp : toDrop) {
    		raw.add( (new ChangeItemLocation(tmp))::inventoryToContainer);
    		raw.add( (new Echo(DROP_SELF, tmp.getName()))::sendSelf);
    		raw.add( (new Echo(DROP_OTHER, actor.getName(), tmp.getName()))::sendOthers);
    	}
		
		return raw;
	}
	
	//-------------------------------------------------------------------
	public static CookedActionResult put(MUDEntity actor, Context context) {
		// Expect a TARGET_NAME parameter describing an item in the current room
		String itemName = context.get(ParameterType.TARGET_NAME);
		if (itemName==null) {
			return new CookedActionResult(PUT_NO_TARGET);
		}
		String containerName = context.get(ParameterType.CONTAINER_NAME);
		Location room = context.get(ParameterType.ROOM_CURRENT);
		
		CookedActionResult raw = new CookedActionResult(true,"put", itemName);
		// Get a list of items matching the itemname
    	List<ItemEntity> toPut = convertToItemEntityList(actor, itemName);
    	logger.log(Level.INFO, "Convert ''{0}'' to {1} items", itemName, toPut.size());
    	if (toPut.isEmpty()) {
    		String name = CommandUtil.isIndexAll(itemName)?CommandUtil.extractItemnameFromIndexedAll(itemName):itemName;
			return new CookedActionResult(DROP_NO_SUCH_ITEM, name);
    	}

    	// Get all items that match that name
    	List<ItemEntity> toPutIntoList = convertToItemEntityList(room, containerName);
    	logger.log(Level.INFO, "Convert ''{0}'' to {1} items", containerName, toPutIntoList.size());
    	if (toPutIntoList.isEmpty()) {
    		String name = CommandUtil.isIndexAll(containerName)?CommandUtil.extractItemnameFromIndexedAll(containerName):containerName;
			return new CookedActionResult(PUT_NO_SUCH_TARGET, name);
    	}
    	// Find the first one that is a container
    	ItemEntity container = toPutIntoList.stream().filter(it -> it.hasFlags(EntityFlag.CONTAINER)).findFirst().orElse(null);
    	logger.log(Level.INFO, "Put {0} into {1}", toPut, container);
    	if (container==null) {
    		String name = CommandUtil.isIndexAll(itemName)?CommandUtil.extractItemnameFromIndexedAll(itemName):itemName;
			return new CookedActionResult(PUT_NOT_A_CONTAINER, name);
    	}
    	
    	for (ItemEntity tmp : toPut) {
    		raw.add( (new ChangeItemLocation(container,tmp))::inventoryToContainer);
    		raw.add( (new Echo(PUT_SELF, tmp.getName(), container.getName()))::sendSelf);
    		raw.add( (new Echo(PUT_OTHER, actor.getName(), tmp.getName(), container.getName()))::sendOthers);
    	}
 		
		return raw;
	}
	
	//-------------------------------------------------------------------
	public static CookedActionResult junk(MUDEntity actor, Context context) {
		// Expect a TARGET_NAME parameter describing an item in the current room
		String itemName = context.get(ParameterType.TARGET_NAME);
		if (itemName==null) {
			return new CookedActionResult(DROP_NO_TARGET);
		}
		
		CookedActionResult raw = new CookedActionResult(true,"junk", itemName);
    	List<ItemEntity> toDrop = convertToItemEntityList(actor, itemName);
    	logger.log(Level.INFO, "Convert ''{0}'' to {1} items", itemName, toDrop.size());
    	if (toDrop.isEmpty()) {
    		String name = CommandUtil.isIndexAll(itemName)?CommandUtil.extractItemnameFromIndexedAll(itemName):itemName;
			return new CookedActionResult(DROP_NO_SUCH_ITEM, name);
    	}
    	
    	for (ItemEntity tmp : toDrop) {
    		raw.add( (new ChangeItemLocation(tmp))::removeFromInventory);
    		raw.add( (new Echo(JUNK_SELF, tmp.getName()))::sendSelf);
    		raw.add( (new Echo(JUNK_OTHER, actor.getName(), tmp.getName()))::sendOthers);
    	}
		
		return raw;
	}

}
