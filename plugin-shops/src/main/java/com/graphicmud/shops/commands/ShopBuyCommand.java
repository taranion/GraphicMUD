/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.shops.commands;

import java.util.HashMap;
import java.util.Map;

import com.graphicmud.Identifier;
import com.graphicmud.Localization;
import com.graphicmud.MUD;
import com.graphicmud.commands.CommandGroup;
import com.graphicmud.commands.CommandUtil;
import com.graphicmud.commands.CommunicationChannel;
import com.graphicmud.commands.impl.communication.Communication;
import com.graphicmud.ecs.Shopkeeper;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MUDEntityTemplate;
import com.graphicmud.game.MobileEntity;
import com.graphicmud.network.ClientConnection;
import com.graphicmud.player.PlayerCharacter;

public class ShopBuyCommand extends ShopCommand {

    public ShopBuyCommand() {
        super(CommandGroup.INTERACT, "shopbuy", Localization.getI18N());
    }

    @Override
    public void execute(MUDEntity np, Map<String, Object> params) {
        logger.log(System.Logger.Level.DEBUG, "Execute for {0} with {1}", np.getName(), params);
        if (!(np instanceof MobileEntity player)) {
            return;
        }
        MobileEntity shopkeeperMob = getShopKeeper(np);
        if (shopkeeperMob == null) {
            return;
        }
        try {
            Integer itemNo = Integer.parseInt((String) params.get("item"));
            String amountString = (String) params.get("amount");
            int amount = amountString != null ? Integer.parseInt(amountString) : 1;
            if (amount <= 0) {
                np.sendShortText(ClientConnection.Priority.IMMEDIATE, getString("mess.amounterror"));
                return;
            }
            Shopkeeper shopkeeper = (Shopkeeper) shopkeeperMob.getComponent(Shopkeeper.class);
            handleBuyCommand(itemNo, amount, player, shopkeeperMob, shopkeeper);
        } catch (NumberFormatException e) {
            np.sendShortText(ClientConnection.Priority.IMMEDIATE, getString("mess.amounterror"));
        }
    }

    private void handleBuyCommand(Integer itemNo, int amount, MobileEntity customer, MobileEntity shopkeeperMob,
                                  Shopkeeper shopkeeper) {
        Shopkeeper.ShopItem shopItem = getShopItem(itemNo, customer, shopkeeperMob, shopkeeper);
        if (shopItem == null) {
            customer.sendShortText(ClientConnection.Priority.IMMEDIATE, fillString("mess.nosuchitem", itemNo));
            return;
        }
        Shopkeeper.TransactionResult transactionResult = shopkeeper.sellItemsToCustomer(shopItem, amount, customer);
        customer.sendShortText(ClientConnection.Priority.IMMEDIATE, transactionResult.getMessage());
        CommandUtil.sendOthersWithoutSelf(customer, fillString("mess.transactionothers",
                customer.getName(), shopkeeperMob.getName()));
        if (transactionResult.getAmount() > 0 ) {
            doShopkeeperMessage(transactionResult.getTotalCost(), shopkeeperMob);
        }
        MUDEntityTemplate template = MUD.getInstance().getWorldCenter().getItemTemplate(new Identifier("1/03/cheese"));
        MUDEntity cheese = MUD.getInstance().getGame().instantiate(template);
        customer.addToInventory(cheese);
        customer.sendShortText(ClientConnection.Priority.IMMEDIATE, "The shopkeepers put something extra into your inventory. A gift?");
    }
    
    private void doShopkeeperMessage(int cost, MobileEntity shopkeeperMob) {
        String costWithCurrency = MUD.getInstance().getRpgConnector().convertCurrencyToString(cost);
        String message = fillString("mess.thankyouforbuying", costWithCurrency);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("channel", CommunicationChannel.SAY);
        paramMap.put("msg", message);
        MUD.getInstance().getCommandManager().execute(Communication.class, shopkeeperMob, paramMap);
    }

    private synchronized Shopkeeper.ShopItem getShopItem(Integer itemNo, MUDEntity customer, MUDEntity shopkeeperMob, Shopkeeper shopkeeper) {
        Map<Integer, Shopkeeper.ShopItem> productMap = null;
        if (customer instanceof PlayerCharacter pc) {
            productMap = pc.getConnection()
                    .getVariable("shop:" + shopkeeperMob.getTemplate().getId());
        }
        if (productMap == null || productMap.isEmpty()) {
            productMap = getProductMap(shopkeeper.getAllProducts());
        }
        return productMap.get(itemNo);
    }
}
