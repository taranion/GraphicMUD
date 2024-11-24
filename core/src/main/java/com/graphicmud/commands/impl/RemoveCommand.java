/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.commands.impl;

import java.lang.System.Logger.Level;
import java.util.Map;

import com.graphicmud.Localization;
import com.graphicmud.MUD;
import com.graphicmud.action.cooked.CookedActionProcessor;
import com.graphicmud.action.cooked.Equipment;
import com.graphicmud.action.cooked.ParameterType;
import com.graphicmud.behavior.Context;
import com.graphicmud.character.EquippedGear;
import com.graphicmud.commands.ACommand;
import com.graphicmud.commands.CommandGroup;
import com.graphicmud.game.ItemEntity;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MUDEvent;
import com.graphicmud.game.MobileEntity;
import com.graphicmud.game.MUDEvent.Type;
import com.graphicmud.network.ClientConnection;

public class RemoveCommand extends ACommand {
    public RemoveCommand() {
        super(CommandGroup.INTERACT, "remove", Localization.getI18N());
    }

    @Override
    public void execute(MUDEntity np, Map<String, Object> params) {
        logger.log(System.Logger.Level.DEBUG, "Execute for {0} with {1}", np.getName(), params);
        if (!(np instanceof MobileEntity mobile)) {
            return;
        }
        String itemName = (String) params.get("item");
        if (itemName != null && !itemName.isBlank()) {
            remove(mobile, itemName);
        } else {
            mobile.sendShortText(ClientConnection.Priority.IMMEDIATE, fillString("mess.notarget"));
        }
    }

    //-------------------------------------------------------------------
    private static void remove(MobileEntity mobile, String itemName) {
        Context context = new Context();
        context.put(ParameterType.TARGET_NAME, itemName);
        CookedActionProcessor.perform(Equipment::remove, mobile, context);

        
//        EquippedGear equippedGear = mobile.getEquippedGear();
//        EquippedGear.RemoveResult result = equippedGear.unequip(itemName);
//        if (result.isOk()) {
//        	// Let the RPG try the same
//            String error = MUD.getInstance().getRpgConnector().unequip(mobile, (ItemEntity) result.getItemToRemove());
//            if (error!=null) {
//         	   logger.log(Level.WARNING, "MUD was fine, but RPG rejected equipping: "+error);
//         	   equippedGear.wear(result.getItemToRemove());
//         	   mobile.sendShortText(ClientConnection.Priority.IMMEDIATE, error);
//         	   return;
//            }
//            mobile.addToInventory(result.getItemToRemove());
//            mobile.sendShortText(ClientConnection.Priority.IMMEDIATE, result.getMsg());
//            // Fire an inventory change
//            mobile.fireEvent(new MUDEvent(Type.INVENTORY_CHANGED, mobile, result.getItemToRemove()));
//        } else {
//            mobile.sendShortText(ClientConnection.Priority.IMMEDIATE, result.getMsg());
//        }
    }

  
}
