/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.shops.commands;

import java.util.Map;

import com.graphicmud.Localization;
import com.graphicmud.commands.CommandGroup;
import com.graphicmud.ecs.Shopkeeper;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MobileEntity;
import com.graphicmud.network.ClientConnection;
import com.graphicmud.player.PlayerCharacter;

public class ShopInspectCommand extends ShopCommand {
    
    public ShopInspectCommand() {
        super(CommandGroup.INTERACT, "shopinspect", Localization.getI18N());
    }

    @Override
    public void execute(MUDEntity np, Map<String, Object> params) {
        logger.log(System.Logger.Level.DEBUG, "Execute for {0} with {1}", np.getName(), params);
        if (!(np instanceof MobileEntity customer)) {
            np.sendShortText(ClientConnection.Priority.UNIMPORTANT, "You cannot do that.");
            return;
        }
       
        Integer item = Integer.parseInt((String) params.get("item"));
        MUDEntity shopkeeperMob = getShopKeeper(np);
        if (shopkeeperMob == null) {
            return;
        }
        Shopkeeper.ShopItem shopItem = getShopItem(item, customer, shopkeeperMob);
        if (shopItem == null) {
            np.sendShortText(ClientConnection.Priority.IMMEDIATE, fillString("mess.nosuchitem", item));
            return;
        }
        String description = "Sorry inspection is not really available yet";
        np.sendShortText(ClientConnection.Priority.IMMEDIATE, fillString("mess.inspectintro", shopkeeperMob.getName(), shopItem.getName()));
        np.sendShortText(ClientConnection.Priority.IMMEDIATE, description);
    }

    private Shopkeeper.ShopItem getShopItem(Integer itemNo, MobileEntity mobile, MUDEntity shopkeeperMob) {
        Shopkeeper shopkeeper = (Shopkeeper) shopkeeperMob.getComponent(Shopkeeper.class);
        Map<Integer, Shopkeeper.ShopItem> productMap = null;
        if (mobile instanceof PlayerCharacter pc) {
            productMap = pc.getConnection()
                    .getVariable("shop:" + shopkeeperMob.getTemplate().getId());
        }
        if (productMap == null || productMap.isEmpty()) {
            productMap = getProductMap(shopkeeper.getAllProducts());
        }
        return productMap.get(itemNo);
    }
}