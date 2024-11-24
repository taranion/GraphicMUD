/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.shops.commands;

import java.util.HashMap;
import java.util.Map;

import com.graphicmud.Localization;
import com.graphicmud.MUD;
import com.graphicmud.commands.CommandGroup;
import com.graphicmud.commands.CommandUtil;
import com.graphicmud.commands.CommunicationChannel;
import com.graphicmud.commands.impl.communication.Communication;
import com.graphicmud.ecs.Shopkeeper;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MobileEntity;
import com.graphicmud.network.ClientConnection;

public class ShopValueCommand extends ShopCommand {

    public ShopValueCommand() {
        super(CommandGroup.INTERACT, "shopvalue", Localization.getI18N());
    }

    @Override
    public void execute(MUDEntity np, Map<String, Object> params) {
        logger.log(System.Logger.Level.DEBUG, "Execute for {0} with {1}", np.getName(), params);
        if (!(np instanceof MobileEntity customer)) {
            np.sendShortText(ClientConnection.Priority.UNIMPORTANT, "You cannot do this. You are not a mobile.");
            return;
        }
        MobileEntity shopkeeperMob = getShopKeeper(np);
        if (shopkeeperMob == null) {
            return;
        }
        String itemName = (String) params.get("item");
        handleValueCommand(itemName, customer, shopkeeperMob);
    }

    private void handleValueCommand(String itemName, MobileEntity customer, MobileEntity shopkeeperMob) {
        Shopkeeper shopkeeper = (Shopkeeper) shopkeeperMob.getComponent(Shopkeeper.class);
        MUDEntity itemFromInventory = customer.getFromInventory(itemName);
        if (itemFromInventory == null) {
            customer.sendShortText(ClientConnection.Priority.IMMEDIATE, fillString("mess.nosuchitem", itemName));
            return;
        }
        Shopkeeper.TransactionResult transactionResult = shopkeeper.valueItem(itemFromInventory);
        if (transactionResult.getAmount() > 0) {
            CommandUtil.sendOthersWithoutSelfAndTarget(customer, shopkeeperMob,
                    fillString("mess.other", customer.getName(), shopkeeperMob.getName()));
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("channel", CommunicationChannel.SAY);
            paramMap.put("msg", transactionResult.getMessage());
            MUD.getInstance().getCommandManager().execute(Communication.class, shopkeeperMob, paramMap);
        }
    }
}
