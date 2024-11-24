/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.shops.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

public class ShopSellCommand extends ShopCommand {

    public ShopSellCommand() {
        super(CommandGroup.INTERACT, "shopsell", Localization.getI18N());
    }

    @Override
    public void execute(MUDEntity np, Map<String, Object> params) {
        logger.log(System.Logger.Level.DEBUG, "Execute for {0} with {1}", np.getName(), params);
        if (!(np instanceof MobileEntity customer)) {
            np.sendShortText(ClientConnection.Priority.UNIMPORTANT, "You cannot sell your stuff. You are not a player.");
            return;
        }
        MUDEntity shopkeeperMob = getShopKeeper(customer);
        if (shopkeeperMob == null) {
            return;
        }
        
        try {
            String itemName = (String) params.get("item");
            String amountString = (String) params.get("amount");
            int amount = amountString != null ? Integer.parseInt(amountString) : 1;
            if (amount <= 0) {
                np.sendShortText(ClientConnection.Priority.IMMEDIATE, getString("mess.amounterror"));
                return;
            }
            handleSellCommand(itemName, amount, customer, shopkeeperMob);
        } catch (NumberFormatException e) {
            np.sendShortText(ClientConnection.Priority.IMMEDIATE, getString("mess.amounterror"));
        }
    }

    private void handleSellCommand(String itemName, int amount, MobileEntity customer, MUDEntity shopkeeperMob) {
        Shopkeeper shopkeeper = (Shopkeeper) shopkeeperMob.getComponent(Shopkeeper.class);
        List<MUDEntity> itemsFromInventory = getItemsFromInventory(itemName, amount, customer);
        if (itemsFromInventory.isEmpty()) {
            customer.sendShortText(ClientConnection.Priority.IMMEDIATE, fillString("mess.nosuchitem", itemName));
            return;
        }
        
        MUDEntity first = itemsFromInventory.getFirst();
        if (itemsFromInventory.size() < amount) {
            customer.sendShortText(ClientConnection.Priority.IMMEDIATE, fillString("mess.wrongamount",
                    first.getName(), itemsFromInventory.size(), amount));
            return;
        }
        Shopkeeper.TransactionResult transactionResult = shopkeeper.buyProductsFromCustomer(itemsFromInventory, customer);
        
        if (transactionResult.getAmount() == itemsFromInventory.size()) {
            CommandUtil.sendOthersWithoutSelfAndTarget(customer, shopkeeperMob,
                    fillString("mess.other", customer.getName(), shopkeeperMob.getName()));
            customer.sendShortText(ClientConnection.Priority.IMMEDIATE, transactionResult.getMessage());
        } else {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("channel", CommunicationChannel.SAY);
            paramMap.put("msg", transactionResult.getMessage());
            MUD.getInstance().getCommandManager().execute(Communication.class, shopkeeperMob, paramMap);
        }
    }
    
    private List<MUDEntity> getItemsFromInventory(String itemName, int amount, MobileEntity player) {
        List<MUDEntity> allFromInventory = player.getAllFromInventory(itemName);
        MUDEntity first = !allFromInventory.isEmpty() ? allFromInventory.getFirst() : null;
        if (first == null) {
            return new ArrayList<>();
        }
        List<MUDEntity> list = allFromInventory.stream().filter(i -> i.getTemplate().equals(first.getTemplate())).toList();
        return new ArrayList<>(list.subList(0, Math.min(amount, list.size())));
    }
}
