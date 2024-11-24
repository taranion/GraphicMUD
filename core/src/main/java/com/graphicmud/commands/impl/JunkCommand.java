/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.commands.impl;

import java.util.Map;

import com.graphicmud.Localization;
import com.graphicmud.action.cooked.CookedActionProcessor;
import com.graphicmud.action.cooked.Inventory;
import com.graphicmud.action.cooked.ParameterType;
import com.graphicmud.behavior.Context;
import com.graphicmud.commands.ACommand;
import com.graphicmud.commands.CommandGroup;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.network.ClientConnection;

public class JunkCommand extends ACommand {
	
    //-------------------------------------------------------------------
    public JunkCommand() {
        super(CommandGroup.INTERACT, "junk", Localization.getI18N());
    }

    //-------------------------------------------------------------------
    /**
     * @see com.graphicmud.commands.Command#execute(com.graphicmud.game.MUDEntity, java.util.Map)
     */
    @Override
    public void execute(MUDEntity np, Map<String, Object> params) {
        logger.log(System.Logger.Level.DEBUG, "Execute for {0} with {1}", np.getName(), params);
        String item = (String) params.get("item");
        if (item == null || item.isBlank()) {
            np.sendShortText(ClientConnection.Priority.IMMEDIATE, "mess.noitem");
            return;
        }
        Context context = new Context();
        context.put(ParameterType.TARGET_NAME, item);
       CookedActionProcessor.perform(Inventory::junk, np, context);
//        CommandUtil.IndexedEntity indexEntity = CommandUtil.createIndexEntity(item);
//        List<MUDEntity> allFromInventory = np.getAllFromInventory(indexEntity.getItemName());
//        if (allFromInventory.isEmpty() || allFromInventory.size() - 1 < indexEntity.getIndex()) {
//            np.sendShortText((ClientConnection.Priority.IMMEDIATE), fillString("mess.nosuchitem", item));
//            return;
//        }
//        MUDEntity itemToJunk = allFromInventory.get(indexEntity.getIndex());
//        np.removeFromInventory(itemToJunk);
//        np.sendShortText(ClientConnection.Priority.IMMEDIATE, fillString("mess.self", itemToJunk.getName()));
//        CommandUtil.sendOthersWithoutSelf(np, fillString("mess.other", np.getName(), itemToJunk.getName()));
    }
}
