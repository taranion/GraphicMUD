/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.shops.commands;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.graphicmud.Localization;
import com.graphicmud.MUD;
import com.graphicmud.ecs.Shopkeeper;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.player.PlayerCharacter;

public class SimpleShopListFormat implements ShopListFormat {

    public static final int MAX_LENGTH_PRODUCTNAME = 55;
    
    public String getFormattedOutput(MUDEntity np, MUDEntity shopkeeperMob) {
        Shopkeeper shopkeeper = (Shopkeeper) shopkeeperMob.getComponent(Shopkeeper.class);
        List<Shopkeeper.ShopItem> products = shopkeeper.getAllProducts();

        int productNameLength = getMaxLength(products);
        if (productNameLength > MAX_LENGTH_PRODUCTNAME) {
            productNameLength = MAX_LENGTH_PRODUCTNAME;
        } else if (productNameLength < 40) {
            productNameLength = 40;
        }
        int length = 28 + productNameLength;
        Map<Integer, Shopkeeper.ShopItem> productMap = getProductMap(products);
        StringBuilder buffer = new StringBuilder(getShopName(shopkeeper.getShopname(), shopkeeperMob));
        buffer.append("-".repeat(length));
        buffer.append("<br/>");
        buffer.append("|<span width=\"3\">No.</span>|");
        buffer.append("<span width=\"");
        buffer.append(productNameLength);
        buffer.append("\">Name</span>|");
        buffer.append("<span width=\"8\">Amount</span>|");
        buffer.append("<span width=\"12\">$</span>|<br/>");
        if (np instanceof PlayerCharacter pc) {
            pc.getConnection().setVariable("shop:" + shopkeeperMob.getTemplate().getId(), productMap);
        }
        fillBuffer(productMap, buffer, productNameLength);
        buffer.append("-".repeat(length));
        buffer.append("<br/>");
        return buffer.toString();
    }

    private static int getMaxLength(List<Shopkeeper.ShopItem> products) {
        return products.stream()
                .mapToInt(obj -> getProductName(obj).length())  // Mappe jedes Objekt zur Länge des Namens
                .max()                                   // Finde das Maximum
                .orElse(0);

    }

    private static void fillBuffer(Map<Integer, Shopkeeper.ShopItem> productMap, StringBuilder buffer, int maxLength) {
        for (Map.Entry<Integer, Shopkeeper.ShopItem> itemEntry : productMap.entrySet()) {
            buffer.append("|");
            Shopkeeper.ShopItem product = itemEntry.getValue();
            buffer.append("<span width=\"3\">");
            buffer.append(itemEntry.getKey());
            buffer.append("</span>");
            buffer.append("|");
            buffer.append("<span width=\"");
            buffer.append(maxLength);
            buffer.append("\">");
            buffer.append(getProductName(product));
            buffer.append("</span>");
            buffer.append("|");
            buffer.append("<span width=\"8\">");
            buffer.append(getAmount(product));
            buffer.append("</span>");
            buffer.append("|");
            buffer.append("<span width=\"12\">");
            Integer priceSale = product.getPriceSale();
            String priceWithCurrency = MUD.getInstance().getRpgConnector().convertCurrencyToString(priceSale);
            buffer.append(priceWithCurrency);
            buffer.append("</span>|<br/>");
        }
    }

    private static String getAmount(Shopkeeper.ShopItem product) {
        if (product.getAmount() == -1) {
            return "unlim.";
        }
        return String.valueOf(product.getAmount());
    }

    private static String getProductName(Shopkeeper.ShopItem product) {
        if (product.getName().length() < 56) {
            return product.getName();
        } else {
            return product.getName().substring(0, MAX_LENGTH_PRODUCTNAME);
        }
    }

    private static String getShopName(String shopName, MUDEntity shopkeeperMob) {
        Shopkeeper component = (Shopkeeper) shopkeeperMob.getComponent(Shopkeeper.class);
        String money = MUD.getInstance().getRpgConnector().convertCurrencyToString(component.getMoney());
        String name = "";
        if (shopName != null && !shopName.isBlank()) {
            name = shopName;
        } else {
            name = Localization.fillString("command.shop.label.shopname", shopkeeperMob.getName());
        }
        return "<u><pre>" + name + " (" + money + ")</pre></u><br/>";
    }

    private static Map<Integer, Shopkeeper.ShopItem> getProductMap(List<Shopkeeper.ShopItem> products) {
        return IntStream.range(0, products.size())
                .boxed()
                .collect(Collectors.toMap(i -> ++i, products::get));
    }
}
