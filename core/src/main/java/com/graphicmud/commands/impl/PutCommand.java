/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.commands.impl;

import static com.graphicmud.commands.CommandUtil.extractItemnameFromIndexedAll;
import static com.graphicmud.commands.CommandUtil.isIndexAll;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.graphicmud.Localization;
import com.graphicmud.action.cooked.CookedActionProcessor;
import com.graphicmud.action.cooked.Inventory;
import com.graphicmud.action.cooked.ParameterType;
import com.graphicmud.behavior.Context;
import com.graphicmud.commands.ACommand;
import com.graphicmud.commands.CommandGroup;
import com.graphicmud.commands.CommandUtil;
import com.graphicmud.game.EntityFlag;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MobileEntity;
import com.graphicmud.network.ClientConnection;
import com.graphicmud.network.ClientConnection.Priority;
import com.graphicmud.world.NoSuchPositionException;

public class PutCommand extends ACommand {
    public PutCommand() {
        super(CommandGroup.INTERACT, "put", Localization.getI18N());
    }

    @Override
    public void execute(MUDEntity np, Map<String, Object> params) {
        logger.log(System.Logger.Level.DEBUG, "Execute for {0} with {1}", np.getName(), params);
        if (!(np instanceof MobileEntity mobile)) {
            return;
        }
        Context context = new Context();
        // Make sure an item to get has been given
        String itemname = (String) params.get("item");
        if (itemname == null) {
        	np.sendShortText(Priority.IMMEDIATE, getString("mess.notarget"));
            return;
        }
        context.put(ParameterType.TARGET_NAME, itemname);
        
        // Check if the mandatory container target name has been given
        String containerName = (String) params.get("target");
        if (containerName==null) {
            np.sendShortText(ClientConnection.Priority.IMMEDIATE, getString("mess.missingvalues"));
            return;
        }
    	context.put(ParameterType.CONTAINER_NAME, containerName);

        CookedActionProcessor.perform(Inventory::put, mobile, context);
    	
    	
//        int index;
//        List<MUDEntity> itemsFromInventory;
//        if (isIndexAll(item)) {
//            String wantedItemsName = extractItemnameFromIndexedAll(item);
//            itemsFromInventory = mobile.getAllFromInventory(wantedItemsName);
//            if (itemsFromInventory.isEmpty()) {
//                np.sendShortText(ClientConnection.Priority.IMMEDIATE, fillString("mess.nosuchitem", item, target));
//                return;
//            }
//            index = -1;
//        } else {
//            CommandUtil.IndexedEntity indexedItem = CommandUtil.createIndexEntity(item);
//            itemsFromInventory = mobile.getAllFromInventory(indexedItem.getItemName());
//            if (itemsFromInventory.isEmpty() || itemsFromInventory.size() -1 < indexedItem.getIndex()) {
//                np.sendShortText(ClientConnection.Priority.IMMEDIATE, fillString("mess.nosuchitem", item, target));
//                return;
//            }
//            index = indexedItem.getIndex();
//        }
//        
//        CommandUtil.IndexedEntity indexedContainer = CommandUtil.createIndexEntity(target);
//        List<MUDEntity> allContainerItems = new LinkedList<>(mobile.getAllFromInventory(indexedContainer.getItemName()));
//            try {
//                allContainerItems.addAll(CommandUtil.getAllItemsFromPosition(indexedContainer.getItemName(), np.getPosition()));
//            } catch (NoSuchPositionException e) {
//                // nothting to do
//            }
//        if (allContainerItems.isEmpty() || allContainerItems.size()-1 < indexedContainer.getIndex() ) {
//            np.sendShortText(ClientConnection.Priority.IMMEDIATE, fillString("mess.nosuchtarget", target));
//            return;
//        }
//        MUDEntity targetContainer = allContainerItems.get(indexedContainer.getIndex());
//        if (isNotAContainer(targetContainer)) {
//            np.sendShortText(ClientConnection.Priority.IMMEDIATE, fillString("mess.notacontainer", targetContainer.getName()));
//            return;
//        }
//        if (index > 0) {
//            MUDEntity itemToPut = itemsFromInventory.get(index);
//            np.removeFromInventory(itemToPut);
//            targetContainer.addToInventory(itemToPut);
//            np.sendShortText(ClientConnection.Priority.IMMEDIATE, fillString("mess.self", itemToPut.getName(), targetContainer.getName()));
//            CommandUtil.sendOthersWithoutSelf(np, fillString("mess.other", np.getName(), itemToPut.getName(), targetContainer.getName()));
//        } else {
//            for (MUDEntity itemToPut : itemsFromInventory) {
//                np.removeFromInventory(itemToPut);
//                targetContainer.addToInventory(itemToPut);
//                np.sendShortText(ClientConnection.Priority.IMMEDIATE, fillString("mess.self", itemToPut.getName(), targetContainer.getName()));
//                CommandUtil.sendOthersWithoutSelf(np, fillString("mess.other", np.getName(), itemToPut.getName(), targetContainer.getName()));
//            }
//        }
    }

//    private static boolean isNotAContainer(MUDEntity entity) {
//        return !entity.getFlags().contains(EntityFlag.CONTAINER);
//    }
}
