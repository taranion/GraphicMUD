/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.commands.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.graphicmud.Localization;
import com.graphicmud.commands.ACommand;
import com.graphicmud.commands.CommandGroup;
import com.graphicmud.commands.CommandUtil;
import com.graphicmud.game.EntityType;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MUDEntityTemplate;
import com.graphicmud.game.MobileEntity;
import com.graphicmud.network.ClientConnection;
import com.graphicmud.world.NoSuchPositionException;

public class ExamineCommand extends ACommand {
    public ExamineCommand() {
        super(CommandGroup.EXAMINE, "examine", Localization.getI18N());
    }

    @Override
    public void execute(MUDEntity np, Map<String, Object> params) {
        logger.log(System.Logger.Level.DEBUG, "Execute for {0} with {1}", np.getName(), params);
        if (! (np instanceof MobileEntity mobile)) {
            return;
        }
        String target = (String) params.get("target");
        if (target == null || target.isBlank()) {
            np.sendShortText(ClientConnection.Priority.IMMEDIATE, getString("mess.notarget"));
            return;
        }
        CommandUtil.IndexedEntity indexedItem = CommandUtil.createIndexEntity(target);
        // Im Inventory anschauen
        List<MUDEntity> bestMatchingEntities = new LinkedList<>(mobile.getAllFromCompleteInventory(indexedItem.getItemName()));
        try {
            bestMatchingEntities.addAll(CommandUtil.getAllEntitiesFromPosition(indexedItem.getItemName(), np.getPosition()));
        } catch (NoSuchPositionException e) {
            // nothing to do
        }
        if (bestMatchingEntities.isEmpty() || bestMatchingEntities.size() -1 <indexedItem.getIndex()) {
            np.sendShortText(ClientConnection.Priority.IMMEDIATE, fillString("mess.nosuchtarget", target));
            return;
        }
        MUDEntity bestMatchingEntity = bestMatchingEntities.get(indexedItem.getIndex());
        if (bestMatchingEntity == np) {
            np.sendShortText(ClientConnection.Priority.IMMEDIATE, getString("mess.selftargeting"));
            return;
        }
        np.sendShortText(ClientConnection.Priority.IMMEDIATE, fillString("mess.self", bestMatchingEntity.getName()));
        np.sendTextWithMarkup(getExaminationTable(bestMatchingEntity));
        CommandUtil.sendOthersWithoutSelfAndTarget(np, bestMatchingEntity, fillString("mess.other", np.getName(), bestMatchingEntity.getName()));
        bestMatchingEntity.sendShortText(ClientConnection.Priority.IMMEDIATE, fillString("mess.target", np.getName()));
    }

    private String getExaminationTable(MUDEntity entity) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(entity.getDescription());
        buffer.append("</br>");
        if (entity instanceof MobileEntity) {
            buffer.append("Height and vital description here");
            buffer.append("</br>");
            buffer.append("Equipped items here");
            buffer.append("</br>");
        }
        buffer.append("<u>");
        String inv = getString("label.inventory");
        if (entity.getType() == EntityType.ITEM) {
            inv = getString("label.inside");
        }
        buffer.append(inv);
        buffer.append("</u></br>");
        buffer.append(getInventoryText(entity));
        if (entity.getTemplate()!=null) {
        	buffer.append("Flags:");
        	buffer.append(entity.getTemplate().getFlags().stream().map(Enum::name).collect(Collectors.joining(",")));
        	buffer.append("<br/>");
        }
        return buffer.toString();
    }

    private static String getInventoryText(MUDEntity character) {
        Map<MUDEntityTemplate, List<MUDEntity>> inventoryMap = getInventoryMap(character.getAllFromInventory(null));
        StringBuilder inventoryText = new StringBuilder();
        for (MUDEntityTemplate mudEntityTemplate : inventoryMap.keySet()) {
            inventoryText.append(getItemText(mudEntityTemplate, inventoryMap.get(mudEntityTemplate).size()));
        }
        return inventoryText.toString();
    }

    private static String getItemText(MUDEntityTemplate mudEntityTemplate, int size) {
        String itemText = mudEntityTemplate.getName();
        if (size > 1) {
            itemText += " (" + size + ")<br/>";
        } else {
            itemText += "<br/>";
        }
        return itemText;
    }

    private static Map<MUDEntityTemplate, List<MUDEntity>> getInventoryMap(List<MUDEntity> items) {
        HashMap<MUDEntityTemplate, List<MUDEntity>> map = new HashMap<>();
        for (MUDEntity item : items) {
            MUDEntityTemplate key = item.getTemplate();
            map.computeIfAbsent(key, k -> new ArrayList<>());
            map.get(key).add(item);
        }
        return map;
    }

}
