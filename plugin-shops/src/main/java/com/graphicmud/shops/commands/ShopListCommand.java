/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.shops.commands;

import java.util.List;
import java.util.Map;

import com.graphicmud.Localization;
import com.graphicmud.MUD;
import com.graphicmud.commands.CommandGroup;
import com.graphicmud.ecs.Shopkeeper;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.network.interaction.Table;
import com.graphicmud.network.interaction.TableColumn;
import com.graphicmud.player.PlayerCharacter;

public class ShopListCommand extends ShopCommand {

    private final ShopListFormat listFormat;

    public ShopListCommand(ShopListFormat listFormat) {
        super(CommandGroup.INTERACT, "shoplist", Localization.getI18N());
        this.listFormat = listFormat;
    }

    @Override
    public void execute(MUDEntity np, Map<String, Object> params) {
        logger.log(System.Logger.Level.DEBUG, "Execute for {0} with {1}", np.getName(), params);
        MUDEntity shopKeeper = getShopKeeper(np);
        if (shopKeeper == null) {
            return;
        }
        handleListCommand(np, shopKeeper);
    }

    private synchronized void handleListCommand(MUDEntity np, MUDEntity shopkeeperMob) {
        Shopkeeper shopkeeper = (Shopkeeper) shopkeeperMob.getComponent(Shopkeeper.class);
        List<Shopkeeper.ShopItem> products = shopkeeper.getAllProducts();

        Table<Shopkeeper.ShopItem> table = new Table<>();
        table.setTitle(getShopName(shopkeeper.getShopname(), shopkeeperMob));
        table.addColumn(new TableColumn<Shopkeeper.ShopItem, Integer>("No.")
                .setValueProvider((t, item) -> t.getData().indexOf(item) + 1)
        );
        table.addColumn(new TableColumn<Shopkeeper.ShopItem, String>("Name")
                .setValueProvider((t, item) -> item.getName())
                .setMxpLinkProvider( item -> "buy "+(products.indexOf(item) + 1))
                .setMaxWidth(55)
        );
        table.addColumn(new TableColumn<Shopkeeper.ShopItem, Integer>("Amount")
                .setValueProvider((t, item) -> item.getAmount())
                .setRenderer( amount -> ( (Integer)amount<0)?"Unlimited":String.valueOf(amount))
        );
        table.addColumn(new TableColumn<Shopkeeper.ShopItem, Integer>("Price")
                .setValueProvider((t, item) -> item.getPriceSale())
                .setRenderer((price) -> MUD.getInstance().getRpgConnector().convertCurrencyToString((int) price))
        );
        table.setData(products);
        ((PlayerCharacter) np).getConnection().presentTable(table);
        // Alte Version
        String buffer = listFormat.getFormattedOutput(np, shopkeeperMob);
        np.sendTextWithMarkup(buffer);
    }

    //-------------------------------------------------------------------
    /*
     * Gedoppelt aus SimpleShopFormat, da wir das evtl. ersetzen
     */
    private static String getShopName(String shopName, MUDEntity shopkeeperMob) {
        Shopkeeper component = (Shopkeeper) shopkeeperMob.getComponent(Shopkeeper.class);
        String money = MUD.getInstance().getRpgConnector().convertCurrencyToString(component.getMoney());
        String name = "";
        if (shopName != null && !shopName.isBlank()) {
            name = shopName;
        } else {
            name = Localization.fillString("command.shop.label.shopname", shopkeeperMob.getName());
        }
        return name + "  (" + money + ")";
    }

}
