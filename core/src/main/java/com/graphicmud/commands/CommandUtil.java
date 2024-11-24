/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.commands;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;

import com.graphicmud.Localization;
import com.graphicmud.MUD;
import com.graphicmud.game.EntityType;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.network.ClientConnection.Priority;
import com.graphicmud.world.Location;
import com.graphicmud.world.NoSuchPositionException;
import com.graphicmud.world.Position;
import com.graphicmud.world.Range;

public class CommandUtil {

    public static MUDEntity getMobileFromPosition(String entityName, Position position) throws NoSuchPositionException {
        Location location;
        location = MUD.getInstance()
                .getWorldCenter()
                .getLocation(position.getRoomPosition()
                .getRoomNumber());
        Optional<MUDEntity> first = location.getEntities()
                .stream()
                .filter(e ->( e.getName().equalsIgnoreCase(entityName) || e.reactsOnKeyword(entityName)) && e.getType() != EntityType.ITEM)
                .findFirst();
        return first.orElse(null);
    }

    public static MUDEntity getItemFromPosition(String entityName, Position position) throws NoSuchPositionException {
        Location location;
        location = MUD.getInstance()
                .getWorldCenter()
                .getLocation(position.getRoomPosition()
                        .getRoomNumber());
        Optional<MUDEntity> first = location.getEntities()
                .stream()
                .filter(e ->( e.getName().equalsIgnoreCase(entityName) || e.reactsOnKeyword(entityName)) && e.getType() == EntityType.ITEM)
                .findFirst();
        return first.orElse(null);
    }

    public static List<MUDEntity> getAllEntitiesFromPosition(String entityName, Position position) throws NoSuchPositionException {
        Location location;
        location = MUD.getInstance()
                .getWorldCenter()
                .getLocation(position.getRoomPosition()
                        .getRoomNumber());
        return new LinkedList<>(location.getEntities()
                .stream()
                .filter(e -> (e.reactsOnKeyword(entityName)))
                .toList());
    }
    
    public static List<MUDEntity> getAllItemsFromPosition(String entityName, Position position) throws NoSuchPositionException {
        Location location;
        location = MUD.getInstance()
                .getWorldCenter()
                .getLocation(position.getRoomPosition()
                        .getRoomNumber());
        return new LinkedList<>(location.getEntities()
                .stream()
                .filter(e -> (e.getName().equalsIgnoreCase(entityName) || e.reactsOnKeyword(entityName)) && e.getType() == EntityType.ITEM)
                .toList());
    }

    public static MUDEntity getEntityFromPosition(String entityName, Position position) throws NoSuchPositionException {
        Location location;
        location = MUD.getInstance()
                .getWorldCenter()
                .getLocation(position.getRoomPosition()
                        .getRoomNumber());
        Optional<MUDEntity> first = location.getEntities()
                .stream()
                .filter(e ->( e.getName().equalsIgnoreCase(entityName) || e.reactsOnKeyword(entityName))).findFirst();
        return first.orElse(null);
    }

    public static void sendOthersWithoutSelfAndTarget(MUDEntity source, MUDEntity target, String message) {
        List<MUDEntity> players = MUD.getInstance().getWorldCenter()
                .getLifeformsInRangeExceptSelf(source, Range.SURROUNDING);
        players.stream()
                .filter(p -> !p.equals(target))
                .forEach(p -> p.sendShortText(Priority.UNIMPORTANT, message));
    }

    public static void sendOthersWithoutSelf(MUDEntity source, String message) {
        List<MUDEntity> players = MUD.getInstance().getWorldCenter()
                .getLifeformsInRangeExceptSelf(source, Range.SURROUNDING);
        players.forEach(p -> p.sendShortText(Priority.UNIMPORTANT, message));
    }

    public static IndexedEntity createIndexEntity(String itemName) {
        StringTokenizer st = new StringTokenizer(itemName, ".");
        if (st.countTokens() != 2) {
            return new IndexedEntity(0, itemName);
        }
        try {
            int i = Integer.parseInt(st.nextToken()) - 1;
            return new IndexedEntity(i, st.nextToken());
        } catch (NumberFormatException e) {
            return new IndexedEntity(0, itemName);
        }
    }

    public static boolean isAll(String itemname) {
        return Localization.getString("command.get.all").equalsIgnoreCase(itemname);
    }

    public static boolean isIndexAll(String itemname) {
        String all = Localization.getString("command.get.all") + ".";
        return itemname.startsWith(all);
    }

    public static String extractItemnameFromIndexedAll(String itemname) {
        return itemname.replace(Localization.getString("command.get.all") + ".", "");
    }

    @Data
    public static class IndexedEntity {
        private final int index;
        private final String itemName;
    }
    
    
    
    
}
