/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.ecs;

import static com.graphicmud.Localization.fillString;

import java.util.ArrayList;
import java.util.List;

import org.prelle.simplepersist.AttribConvert;
import org.prelle.simplepersist.Attribute;
import org.prelle.simplepersist.ElementList;

import com.graphicmud.Identifier;
import com.graphicmud.Localization;
import com.graphicmud.MUD;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MUDEntityTemplate;
import com.graphicmud.game.MobileEntity;
import com.graphicmud.io.IdentifierConverter;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@SuppressWarnings("unused")
public class Shopkeeper extends Component {

    @ElementList(entry = "item", type = ShopLoadItem.class, inline = true)
    private List<ShopLoadItem> goods = new ArrayList<>();

    @Getter
    @Attribute
    private String shopname;
    @Attribute
    @Getter
    @Setter
    private Integer money;
    @Attribute
    @Getter
    private Integer salesrate;
    @Attribute
    @Getter
    private Integer acquisitionrate;

    private final Shop shop;

    public Shopkeeper() {
        shop = new Shop(this);
    }


    private ShopItem getShopItemForEntity(MUDEntity item) {
        shop.ensureInitialization(goods);
        return shop.getAllProducts().stream()
                .filter(s -> s.item instanceof MUDEntityTemplate template && template.equals(item.getTemplate()))
                .findFirst()
                .orElse(null);
    }

    public List<ShopItem> getAllProducts() {
        shop.ensureInitialization(goods);
        return shop.getAllProducts();
    }

    public TransactionResult sellItemsToCustomer(ShopItem item, int amountToBuy, MobileEntity customer) {
        synchronized (this) {
            List<Shopkeeper.ShopItem> allProducts = shop.getAllProducts();
            int storedAmount = item.getAmount();
            if (!hasEnoughMoney(item, amountToBuy, customer)) {
                return new TransactionResult(0, fillString("command.shopbuy.mess.notenoughmoney",
                        item.getName()));
            }
            TransactionResult result = shop.buy(item, amountToBuy);
            // inv player
            int soldAmount = result.getAmount();
            int cost = item.getPriceSale() * soldAmount;
            money += cost;
            customer.setMoney(customer.getMoney() - cost);
            result.setTotalCost(cost);

            if (item.getItem() instanceof MUDEntityTemplate template) {
                for (int i = 0; i < soldAmount; i++) {
                    MUDEntity itemToBuy = MUD.getInstance().getGame().instantiate(template);
                    customer.addToInventory(itemToBuy);
                }
            } else {
                customer.addToInventory((MUDEntity) item.getItem());
            }
            return result;
        }
    }

    public TransactionResult valueItem(MUDEntity item) {
        int cost;
        if (!isModified(item)) {
            Shopkeeper.ShopItem shopItemForEntity = getShopItemForEntity(item);
            int price = 0;
            if (shopItemForEntity != null) {
                cost = shopItemForEntity.getPriceAcquisition();
            } else {
                Integer acquisitionrate = getAcquisitionrate();
                int monetaryValue = MUD.getInstance().getRpgConnector().getMonetaryValue(item);
                cost = Math.max((monetaryValue * acquisitionrate) / 100, 1);
            }
        } else {
            int monetaryValue = MUD.getInstance().getRpgConnector().getMonetaryValue(item);
            cost = Math.max((monetaryValue * getAcquisitionrate()) / 100, 1);
        }

        String priceString = MUD.getInstance().getRpgConnector().convertCurrencyToString(cost);
        String message = fillString("command.shopvalue.mess.valueitem", priceString);
        return new TransactionResult(1, message);
    }

    public synchronized TransactionResult buyProductsFromCustomer(List<MUDEntity> itemsFromInventory, MobileEntity customer) {
        MUDEntity firstItem = itemsFromInventory.getFirst();
        if (!isBuying(customer, firstItem)) {
            return new TransactionResult(0, fillString("command.shopsell.mess.shopkeeperwontbuy",
                    firstItem.getName()));
        }
        TransactionResult result;
        if (!isModified(firstItem)) {
            Shopkeeper.ShopItem shopItemForEntity = getShopItemForEntity(firstItem);
            if (shopItemForEntity == null) {
                shopItemForEntity = Shopkeeper.ShopItem.createShopItem(firstItem.getTemplate(), 1, this);
            }
            result= handleBuyFromCustomer(shopItemForEntity,itemsFromInventory.size(), customer);
        } else {
            Shopkeeper.ShopItem shopItem = Shopkeeper.ShopItem.createShopItem(firstItem, 1, this);
            result = handleBuyFromCustomer(shopItem, 1, customer);
        }
        if (result.getAmount() == itemsFromInventory.size()) {
            itemsFromInventory.forEach(customer::removeFromInventory);
            customer.setMoney(customer.getMoney() + result.getTotalCost());
            this.setMoney(this.getMoney() - result.totalCost);
        }
        return result;
    }

    private TransactionResult handleBuyFromCustomer(ShopItem item, int amount, MobileEntity player) {
        int price = item.getPriceAcquisition() * amount;
        if (price > money) {
            return new TransactionResult(0, fillString("command.shopsell.mess.shopkeeperbroke"));
        }
        shop.sell(item, amount);
        String priceString = MUD.getInstance().getRpgConnector().convertCurrencyToString(price);
        TransactionResult result = new TransactionResult(amount, Localization.fillString("command.shopsell.mess.soldplayer",
                item.getName(), priceString));
        result.setTotalCost(price);
        return result;
    }

    private boolean isModified(MUDEntity item) {
        return false;
    }

    private boolean isBuying(MUDEntity first, MUDEntity item) {
        return true;
    }


    private boolean hasEnoughMoney(Shopkeeper.ShopItem item, int amount, MobileEntity customer) {
        int money = customer.getMoney();
        int costPerItem = item.getPriceSale();
        return money >= costPerItem * amount;
    }

    public static class ShopLoadItem {
        @Attribute(required = true)
    	@AttribConvert(value = IdentifierConverter.class)
        private Identifier ref;
        @Attribute
        private Integer price;
        @Attribute
        @Getter
        private Integer initial;
    }

    @Data
    public static class ShopItem {
        @NonNull
        String name;

        /**
         * Amount of money the shopkeeper offers the customer in case the customer wants to sell something
         */
        @NonNull
        Integer priceAcquisition;

        /**
         * Amount of money the shopkeeper wants from the customer
         */
        @NonNull
        Integer priceSale;

        @NonNull
        Integer amount;

        @NonNull
        Object item;


        public static ShopItem createShopItem(Object item, int amount, Shopkeeper shopkeeper) {
            if (item instanceof ShopLoadItem shopLoadItem) {
                MUDEntityTemplate template = MUD.getInstance().getWorldCenter().getItemTemplate(shopLoadItem.ref);
                int priceSale = getRateAdjustedPrice(shopLoadItem, shopkeeper.getSalesrate());
                int priceAcq = getRateAdjustedPrice(shopLoadItem, shopkeeper.getAcquisitionrate());
                amount = shopLoadItem.initial != null && shopLoadItem.initial > 0 ? shopLoadItem.initial : -1;
                return new ShopItem(template.getName(), priceAcq, priceSale, amount, template);
            } else if (item instanceof MUDEntityTemplate template) {
                int priceSale = getRateAdjustedPrice(template, shopkeeper.getSalesrate());
                int priceAcq = getRateAdjustedPrice(template, shopkeeper.getAcquisitionrate());
                return new ShopItem(template.getName(), priceAcq, priceSale, amount, template);
            } else if (item instanceof MUDEntity entity) {
                MUDEntityTemplate template = entity.getTemplate();
                int priceSale = getRateAdjustedPrice(template, shopkeeper.getSalesrate());
                int priceAcq = getRateAdjustedPrice(template, shopkeeper.getAcquisitionrate());
                return new ShopItem(template.getName(), priceAcq, priceSale, amount, entity);
            }
            return null;
        }

        private static int getRateAdjustedPrice(ShopLoadItem shopLoadItem, Integer rate) {
            int price = getItemPrice(shopLoadItem);
            int salesRate = rate != null ? rate : 100;
            return Math.max((price * salesRate) / 100, 1);
        }

        private static int getRateAdjustedPrice(MUDEntityTemplate template, Integer rate) {
            int price = getTemplatePrice(template);
            int salesRate = rate != null ? rate : 100;
            return Math.max((price * salesRate) / 100, 1);
        }

        private static Integer getItemPrice(ShopLoadItem shopLoadItem) {
            int price;
            if (shopLoadItem.price != null && !(shopLoadItem.price < 0)) {
                price = shopLoadItem.price;
            } else {
                MUDEntityTemplate template = MUD.getInstance().getWorldCenter().getItemTemplate(shopLoadItem.ref);
                price = getTemplatePrice(template);
            }
            return price;
        }

        private static int getTemplatePrice(MUDEntityTemplate template) {
            return Math.max(Math.toIntExact(MUD.getInstance().getRpgConnector().getMonetaryValue(template)), 0);
        }
    }

    @Data
    public static class TransactionResult {
        final int amount;
        final String message;
        int totalCost;
    }
}
