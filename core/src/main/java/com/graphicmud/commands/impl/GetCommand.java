/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.commands.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.graphicmud.Localization;
import com.graphicmud.MUD;
import com.graphicmud.action.cooked.CookedActionProcessor;
import com.graphicmud.action.cooked.Inventory;
import com.graphicmud.action.cooked.ParameterType;
import com.graphicmud.behavior.Context;
import com.graphicmud.commands.ACommand;
import com.graphicmud.commands.CommandGroup;
import com.graphicmud.commands.CommandUtil;
import com.graphicmud.game.EntityFlag;
import com.graphicmud.game.EntityType;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MUDEvent;
import com.graphicmud.game.MobileEntity;
import com.graphicmud.network.ClientConnection.Priority;
import com.graphicmud.world.Location;
import com.graphicmud.world.NoSuchPositionException;
import com.graphicmud.world.Position;
import com.graphicmud.world.Range;

public class GetCommand extends ACommand {
    public GetCommand() {
        super(CommandGroup.INTERACT, "get", Localization.getI18N());
    }

    @Override
    public void execute(MUDEntity actor, Map<String, Object> params) {
        logger.log(System.Logger.Level.DEBUG, "Execute for {0} with {1}", actor.getName(), params);
        if (!(actor instanceof MobileEntity mobile)) {
            return;
        }
        
        Context context = new Context();
        // Make sure an item to get has been given
        String itemname = (String) params.get("item");
        if (itemname == null) {
            actor.sendShortText(Priority.IMMEDIATE, getString("mess.notarget"));
            return;
        }
        context.put(ParameterType.TARGET_NAME, itemname);
        
        // Check if an optional container name has been given
        String containerName = (String) params.get("container");
        if (containerName!=null) {
        	context.put(ParameterType.CONTAINER_NAME, containerName);
        }
        
        CookedActionProcessor.perform(Inventory::get, mobile, context);
        
//        if (containerName == null || containerName.isBlank()) {
//            if (CommandUtil.isIndexAll(itemname)) {
//                String wantedItemsName = CommandUtil.extractItemnameFromIndexedAll(itemname);
//                handleGetAllOfSameName(actor, wantedItemsName);
//                return;
//            }
//            CommandUtil.IndexedEntity indexedItem = CommandUtil.createIndexEntity(itemname);
//            handleGetFromRoom(actor, indexedItem);
//        } else {
//            CommandUtil.IndexedEntity indexContainer = CommandUtil.createIndexEntity(containerName);
//            if (CommandUtil.isIndexAll(itemname)) {
//                String wantedItemsName = CommandUtil.extractItemnameFromIndexedAll(itemname);
//                handleGetAllOfSameNameFromContainer(mobile, wantedItemsName, indexContainer);
//                return;
//            } else {
//                CommandUtil.IndexedEntity indexedItem = CommandUtil.createIndexEntity(itemname);
//                handleGetFromContainer(mobile, indexedItem, indexContainer);
//            }
//        }
    }

//    private void handleGetAllOfSameNameFromContainer(MobileEntity actor, String wantedItemsName, CommandUtil.IndexedEntity idContainer) {
//        try {
//            MUDEntity container = getContainer(actor, idContainer);
//            if (container == null) return;
//
//            List<MUDEntity> itemsFromContainer = container.getAllFromInventory(wantedItemsName);
//            if (itemsFromContainer.isEmpty()) {
//                actor.sendShortText(Priority.IMMEDIATE, fillString("mess.nocsuchitem", wantedItemsName, container.getName()));
//                return;
//            }
//            for (MUDEntity itemFromContainer : itemsFromContainer) {
//                actor.addToInventory(itemFromContainer);
//                container.removeFromInventory(itemFromContainer);
//                actor.sendShortText(Priority.IMMEDIATE, fillString("mess.get.from.self", itemFromContainer.getName(), container.getName()));
//                CommandUtil.sendOthersWithoutSelf(actor, fillString("mess.get.from.other", actor.getName(), itemFromContainer.getName(), container.getName()));
//            }
//        } catch (NoSuchPositionException e) {
//            logger.log(System.Logger.Level.WARNING, "GetCommand tried to interact with not existing position");
//            actor.sendShortText(Priority.IMMEDIATE, "Error. Position not existing. Have a look in the magic log");
//        }
//    }
//
//    private MUDEntity getContainer(MobileEntity actor, CommandUtil.IndexedEntity idContainer) throws NoSuchPositionException {
//        List<MUDEntity> foundContainerItems = new LinkedList<>(actor.getAllFromCompleteInventory(idContainer.getItemName()));
//        List<MUDEntity> containersFromRoom = CommandUtil.getAllItemsFromPosition(idContainer.getItemName(), actor.getPosition());
//        foundContainerItems.addAll(containersFromRoom);
//
//        if (foundContainerItems.isEmpty() || foundContainerItems.size()-1 < idContainer.getIndex()) {
//            actor.sendShortText(Priority.IMMEDIATE, fillString("mess.nocontainerfound", idContainer.getItemName()));
//            return null;
//        }
//
//        MUDEntity container = foundContainerItems.get(idContainer.getIndex());
//        if (isNotAContainer(container)) {
//            actor.sendShortText(Priority.IMMEDIATE, fillString("mess.notacontainer", container.getName()));
//            return null;
//        }
//        return container;
//    }
//
//    private void handleGetAllOfSameName(MUDEntity actor, String wantedItemsName) {
//        Position position = actor.getPosition();
//        try {
//            Location location = MUD.getInstance().getWorldCenter().getLocation(position.getRoomPosition()
//                    .getRoomNumber());
//
//            List<MUDEntity> entitiesInRoom = location.getEntities();
//            List<MUDEntity> list = entitiesInRoom.stream().filter(e -> e.reactsOnKeyword(wantedItemsName)).toList();
//            getAll(actor, list, location);
//        } catch (NoSuchPositionException e) {
//            logger.log(System.Logger.Level.WARNING, "GetCommand tried to interact with not existing position");
//            actor.sendShortText(Priority.IMMEDIATE, "Error. Position not existing. Have a look in the magic log");
//        }
//    }
//
//    private void handleGetFromRoom(MUDEntity entity, CommandUtil.IndexedEntity item) {
//        Position position = entity.getPosition();
//        try {
//            Location location = MUD.getInstance().getWorldCenter().getLocation(position.getRoomPosition()
//                    .getRoomNumber());
//
//            List<MUDEntity> entitiesInRoom = location.getEntities();
//            if (item.getItemName().equalsIgnoreCase("all")) {
//                getAll(entity, entitiesInRoom, location);
//            } else {
//                getNamedItems(entity, entitiesInRoom, item, location);
//            }
//        } catch (NoSuchPositionException e) {
//            logger.log(System.Logger.Level.WARNING, "GetCommand tried to interact with not existing position");
//            entity.sendShortText(Priority.IMMEDIATE, "Error. Position not existing. Have a look in the magic log");
//        }
//    }
//
//    private void handleGetFromContainer(MobileEntity actor, CommandUtil.IndexedEntity idItem, CommandUtil.IndexedEntity idContainer) {
//        try {
//            MUDEntity container = getContainer(actor, idContainer);
//            if (container == null) return;
//
//            List<MUDEntity> itemsFromContainer = container.getAllFromInventory(idItem.getItemName());
//            if (itemsFromContainer.isEmpty() || itemsFromContainer.size()-1 < idItem.getIndex()) {
//                actor.sendShortText(Priority.IMMEDIATE, fillString("mess.nocsuchitem", idItem.getItemName(), container.getName()));
//                return;
//            }
//            MUDEntity itemFromContainer = itemsFromContainer.get(idItem.getIndex());
//            actor.addToInventory(itemFromContainer);
//            container.removeFromInventory(itemFromContainer);
//            actor.sendShortText(Priority.IMMEDIATE, fillString("mess.get.from.self", itemFromContainer.getName(), container.getName()));
//            CommandUtil.sendOthersWithoutSelf(actor, fillString("mess.get.from.other", actor.getName(), itemFromContainer.getName(), container.getName()));
//            
//        } catch (NoSuchPositionException e) {
//            logger.log(System.Logger.Level.WARNING, "GetCommand tried to interact with not existing position");
//            actor.sendShortText(Priority.IMMEDIATE, "Error. Position not existing. Have a look in the magic log");
//        }
//    }
//
//    private static boolean isNotAContainer(MUDEntity entity) {
//        return !entity.getFlags().contains(EntityFlag.CONTAINER);
//    }
//
//    private void getAll(MUDEntity entity, List<MUDEntity> entitiesInRoom, Location location) {
//        List<MUDEntity> itemsInRoom = entitiesInRoom.stream()
//                .filter(i -> i.getType() == EntityType.ITEM)
//                .toList();
//        if (itemsInRoom.isEmpty()) {
//            entity.sendShortText(Priority.IMMEDIATE, Localization.getString("command.get.mess.noitems"));
//        } else {
//            String allItemNames = itemsInRoom.stream().map(MUDEntity::getName).collect(Collectors.joining(", "));
//            logger.log(System.Logger.Level.DEBUG, "{0} puts {1} in inventory", entity.getName(), allItemNames);
//            itemsInRoom.forEach(removeFromRoomAndAddToInventory(entity, location));
//            logger.log(System.Logger.Level.DEBUG, "Removed {0} from room {0}", allItemNames, location.getNr());
//        }
//    }
//
//    private static Consumer<MUDEntity> removeFromRoomAndAddToInventory(MUDEntity entity, Location location) {
//        return i -> {
//            location.removeEntity(i);
//            entity.addToInventory(i);
//            entity.sendShortText(Priority.IMMEDIATE, Localization.fillString("command.get.mess.self", i.getName()));
//            sendOther(entity, i.getName());
//            entity.fireEvent(new MUDEvent(MUDEvent.Type.INVENTORY_CHANGED, entity, i));
//        };
//    }
//
//    private void getNamedItems(MUDEntity np, List<MUDEntity> entitiesInRoom, CommandUtil.IndexedEntity item, Location location) {
//        List<MUDEntity> itemsToGet = entitiesInRoom.stream()
//                .filter(i -> i.getType() == EntityType.ITEM && i.reactsOnKeyword(item.getItemName())).toList();
//                
//        if (itemsToGet.isEmpty()) {
//            np.sendShortText(Priority.IMMEDIATE, Localization.fillString("command.get.mess.nosuchitem", item.getItemName()));
//        } else if (itemsToGet.size() -1 < item.getIndex()) {
//            np.sendShortText(Priority.IMMEDIATE, getString("mess.nosuchindex"));
//        } else {
//            MUDEntity itemToGet = itemsToGet.get(item.getIndex());
//            String realItemName = itemToGet.getName();
//            logger.log(System.Logger.Level.DEBUG, "{0} puts {1} in inventory", np.getName(), realItemName);
//            location.removeEntity(itemToGet);
//            logger.log(System.Logger.Level.DEBUG, "Removed {0} from room {0}", realItemName, location.getNr());
//            np.addToInventory(itemToGet);
//            np.sendShortText(Priority.IMMEDIATE, Localization.fillString("command.get.mess.self", realItemName));
//            sendOther(np, realItemName);
//        }
//    }
//
//    private static void sendOther(MUDEntity entity, String itemname) {
//        List<MUDEntity> players = MUD.getInstance().getWorldCenter()
//                .getLifeformsInRangeExceptSelf(entity, Range.SURROUNDING);
//        players.stream()
//                .filter(p -> !p.equals(entity))
//                .forEach(p -> send(entity, itemname, p));
//    }
//
//    private static void send(MUDEntity entity, String itemname, MUDEntity p) {
//        p.sendShortText(Priority.UNIMPORTANT, Localization.fillString("command.get.mess.other",
//                entity.getName(), itemname));
//    }
}
