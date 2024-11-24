/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.ecs;

import java.util.ArrayList;
import java.util.List;

import com.graphicmud.Localization;

public class Shop {

    private final List<Shopkeeper.ShopItem> allProducts = new ArrayList<>();
    private final Shopkeeper shopkeeper;

    public Shop(Shopkeeper shopkeeper) {
        this.shopkeeper = shopkeeper;
    }

    public synchronized void ensureInitialization(List<Shopkeeper.ShopLoadItem> goods) {
        if (!allProducts.isEmpty()) {
            return;
        }
        goods.forEach(i -> {
            int initial = i.getInitial() != null ? i.getInitial() : -1;
            allProducts.add(Shopkeeper.ShopItem.createShopItem(i, initial, shopkeeper));
        });
    }

    public synchronized List<Shopkeeper.ShopItem> getAllProducts() {
        return new ArrayList<>(allProducts);
    }

    public Shopkeeper.TransactionResult buy(Shopkeeper.ShopItem item, int amountToBuy) {
        synchronized (this) {
            if (!allProducts.contains(item)) {
                return new Shopkeeper.TransactionResult(0, Localization.fillString("command.shopbuy.mess.itemoutofstock",
                        item.getName()));
            }
            int storedAmount = item.getAmount();
            if (storedAmount == -1) {
                return getResultOK(item, amountToBuy);
            } else if (amountToBuy < storedAmount) {
                item.setAmount(item.getAmount() - amountToBuy);
                return getResultOK(item, amountToBuy);
            } else if (amountToBuy == storedAmount) {
                item.setAmount(0);
                allProducts.remove(item);
                return getResultOK(item, amountToBuy);
            } else {
                item.setAmount(0);
                allProducts.remove(item);
                return new Shopkeeper.TransactionResult(storedAmount, Localization.fillString("mess.lessthanordered", storedAmount, item.getName()));
            }
        }
    }

    private static Shopkeeper.TransactionResult getResultOK(Shopkeeper.ShopItem item, int amountToBuy) {
        return new Shopkeeper.TransactionResult(amountToBuy, Localization.fillString("command.shopbuy.mess.buyok", item.getName(), amountToBuy));
    }

    public void sell(Shopkeeper.ShopItem item, int amount) {
        synchronized (this) {
            if (!allProducts.contains(item)) {
                item.setAmount(amount);
                allProducts.add(item);
                return;
            }
            if (item.getAmount() != -1) {
                item.setAmount(amount + amount);
            }
        }
    }
}
