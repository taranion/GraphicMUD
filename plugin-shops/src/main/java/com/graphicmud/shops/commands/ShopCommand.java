/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.shops.commands;

import de.rpgframework.MultiLanguageResourceBundle;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.graphicmud.MUD;
import com.graphicmud.commands.ACommand;
import com.graphicmud.commands.CommandGroup;
import com.graphicmud.ecs.Shopkeeper;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MobileEntity;
import com.graphicmud.network.ClientConnection;
import com.graphicmud.world.Range;

public abstract class ShopCommand extends ACommand {
    
    public ShopCommand(CommandGroup type, String name, MultiLanguageResourceBundle i18n) {
        super(type, name, i18n);
    }

    public static Map<Integer, Shopkeeper.ShopItem> getProductMap(List<Shopkeeper.ShopItem> products) {
        return IntStream.range(0, products.size())
                .boxed()
                .collect(Collectors.toMap(i -> ++i, products::get));
    }

    public MobileEntity getShopKeeper(MUDEntity np) {
        List<MUDEntity> lifeformsInRangeExceptSelf = MUD.getInstance()
                .getWorldCenter()
                .getLifeformsInRangeExceptSelf(np, Range.SURROUNDING);
        Optional<MUDEntity> first = lifeformsInRangeExceptSelf.stream().filter(l -> l.getComponent(Shopkeeper.class) != null).findFirst();
        if (first.isPresent() && first.get() instanceof MobileEntity) {
            return (MobileEntity) first.get();
        } else {
            np.sendShortText(ClientConnection.Priority.IMMEDIATE, getI18N("command.shop.mess.noshophere"));
        }
        return null;
    }
}
