/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.commands.impl;

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
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MobileEntity;
import com.graphicmud.network.ClientConnection.Priority;
import com.graphicmud.player.PlayerCharacter;
import com.graphicmud.world.Location;
import com.graphicmud.world.NoSuchPositionException;
import com.graphicmud.world.Position;
import com.graphicmud.world.Range;

public class DropCommand extends ACommand {
    public DropCommand() {
        super(CommandGroup.BASIC, "drop", Localization.getI18N());
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
        
        CookedActionProcessor.perform(Inventory::drop, mobile, context);

//        Position position = entity.getPosition();
//        try {
//            Location location = MUD.getInstance().getWorldCenter().getLocation(position.getRoomPosition()
//                    .getRoomNumber());
//            if (itemname.equalsIgnoreCase("all")) {
//                dropAll(entity, location);
//            } else {
//                dropNamedItems(entity, itemname, location);
//            }
//        } catch (NoSuchPositionException e) {
//            logger.log(System.Logger.Level.WARNING, "GetCommand tried to interact with not existing position");
//            entity.sendShortText(Priority.IMMEDIATE, "Error. Position not existing. Have a look in the magic log");
//        }
    }

    
    private void dropAll(MUDEntity entity, Location location) {
        List<MUDEntity> inventory = entity.getAllFromInventory(null);
        if (inventory.isEmpty()) {
            entity.sendShortText(Priority.IMMEDIATE, Localization.getString("command.drop.mess.noitems"));
        } else {
            String allItemNames = inventory.stream().map(MUDEntity::getName).collect(Collectors.joining(", "));
            logger.log(System.Logger.Level.DEBUG, "{0} drops {1} on the ground", entity.getName(), allItemNames);
            inventory.forEach(removeFromInventoryAndAddToRoom(entity, location));
        }
        

    }

    private static Consumer<MUDEntity> removeFromInventoryAndAddToRoom(MUDEntity entity, Location location) {
        return i -> {
            location.addEntity(i);
            entity.removeFromInventory(i);
            entity.sendShortText(Priority.IMMEDIATE, Localization.fillString("command.drop.mess.self", i.getName()));
            sendOther(entity, i.getName());
        };
    }

    private void dropNamedItems(MUDEntity entity, String itemname, Location location) {
        CommandUtil.IndexedEntity indexEntity = CommandUtil.createIndexEntity(itemname);
        List<MUDEntity> items = entity.getAllFromInventory(indexEntity.getItemName());
        if (items.isEmpty() || items.size() -1 < indexEntity.getIndex()) {
            entity.sendShortText(Priority.IMMEDIATE, Localization.fillString("command.drop.mess.nosuchitem", itemname));
        } else {
            MUDEntity itemToDrop = items.get(indexEntity.getIndex());
            String realItemName = itemToDrop.getName();
            logger.log(System.Logger.Level.DEBUG, "{0} drops {1} to the ground", entity.getName(),
                    realItemName);
            location.addEntity(itemToDrop);
            logger.log(System.Logger.Level.DEBUG, "Removed {0} from inventory of {1}", realItemName, 
                    entity.getName());
            entity.removeFromInventory(itemToDrop);
            entity.sendShortText(Priority.IMMEDIATE, Localization.fillString("command.drop.mess.self", realItemName));
            sendOther(entity, itemname);
        }
    }

    private static void sendOther(MUDEntity entity, String itemname) {
        List<PlayerCharacter> players = MUD.getInstance().getWorldCenter()
                .getPlayersInRangeExceptSelf(entity, Range.SURROUNDING);
        players.stream()
                .filter(p -> !p.equals(entity))
                .forEach(p -> send(entity, itemname, p));
    }

    private static void send(MUDEntity entity, String itemname, PlayerCharacter p) {
        p.getConnection().sendShortText(Priority.UNIMPORTANT, Localization.fillString("command.drop.mess.other",
                entity.getName(), itemname));
    }
}
