/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.commands.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.graphicmud.Localization;
import com.graphicmud.commands.ACommand;
import com.graphicmud.commands.CommandGroup;
import com.graphicmud.game.EntityFlag;
import com.graphicmud.game.MUDEntity;


public class InventoryCommand extends ACommand {

    public InventoryCommand() {
        super(CommandGroup.BASIC, "inventory", Localization.getI18N());
    }

    @Override
    public void execute(MUDEntity np, Map<String, Object> params) {
        logger.log(System.Logger.Level.DEBUG, "Execute for {0} with {1}", np.getName(), params);
        String inventory = getInventoryText(np);
        np.sendTextWithMarkup(inventory);
    }

    private String getInventoryText(MUDEntity character) {
        Map<MUDEntity, List<MUDEntity>> inventoryMap = getInventoryMap(character.getAllFromInventory(null));
        StringBuilder inventoryText = new StringBuilder("<u>");
        inventoryText.append(Localization.getString("command.inventory.mess.headline"));
        inventoryText.append("</u><br/>");
        printMapWithIndentation(inventoryMap, inventoryText);
        return inventoryText.toString();
    }
  
    public static void printMapWithIndentation(Map<MUDEntity, List<MUDEntity>> map, StringBuilder output) {
        for (MUDEntity entity : map.keySet()) {
            if (isContainer(entity)) {
                List<MUDEntity> objekte = map.get(entity);
                for (MUDEntity objekt : objekte) {
                    output.append(objekt.getName() + ":<br/>");
                    printObjektWithIndentation(objekt, 1, output);  // 1 ist die Starttiefe
                }
            } else {
                output.append(getItemText(entity, map.get(entity).size()));
            }
        }
    }

    public static void printObjektWithIndentation(MUDEntity objekt, int depth, StringBuilder output) {

        // Rekursion für die Kinder dieses Objekts
        for (MUDEntity child : objekt.getAllFromInventory(null)) {
            if (child.getAllFromInventory(null).isEmpty()) {
                output.append("-".repeat(depth) + child.getName() + "<br/>");
            }  else {
                output.append("-".repeat(depth) + child.getName() + ":<br/>");
                printObjektWithIndentation(child, depth + 1, output);  // Tiefe wird um 1 erhöht
            }
        }
    }

    private static String getItemText(MUDEntity mudEntity, int size) {
         String itemText = mudEntity.getName();
        if (size > 1) {
            itemText += " (" + size + ")<br/>";
        } else {
            itemText += "<br/>";
        }
        return itemText;
    }
    
    private static Map<MUDEntity, List<MUDEntity>> getInventoryMap(List<MUDEntity> items) {
        HashMap<MUDEntity, List<MUDEntity>> map = new HashMap<>();
        for (MUDEntity item : items) {
            map.computeIfAbsent(item, k -> new ArrayList<>());
            map.get(item).add(item);
        }
        return map;
    }
    
    private static boolean isContainer(MUDEntity template) {
    	if (template==null) return false;
    	if (template.getTemplate()==null) return false;
        return template.getTemplate().getFlags().contains(EntityFlag.CONTAINER);
    }
}
