/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.commands.impl;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.graphicmud.Localization;
import com.graphicmud.commands.ACommand;
import com.graphicmud.commands.CommandGroup;
import com.graphicmud.commands.CommandUtil;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.network.ClientConnection.Priority;
import com.graphicmud.player.PlayerCharacter;
import com.graphicmud.world.NoSuchPositionException;
import com.graphicmud.world.Position;

public class GiveCommand extends ACommand {
    public GiveCommand() {
        super(CommandGroup.INTERACT, "give", Localization.getI18N());
    }

    @Override
    public void execute(MUDEntity np, Map<String, Object> params) {
        logger.log(System.Logger.Level.DEBUG, "Execute for {0} with {1}", np.getName(), params);
        String itemName = (String) params.get("item");
        String targetName = (String) params.get("target");
        Position position = np.getPosition();
        try {
            MUDEntity target = CommandUtil.getMobileFromPosition(targetName, position);
            if (target == np) {
                np.sendShortText(Priority.IMMEDIATE, Localization.getString("command.give.mess.targetIsSelf"));
            }
            if (target == null) {
                np.sendShortText(Priority.IMMEDIATE, Localization.fillString("command.give.mess.nosuchplayer", targetName));
                return;
            }
            if (itemName.equalsIgnoreCase("all")) {
                giveAll(np, target);
            } else {
                giveNamedItems(np, itemName, target);
            }
        } catch (NoSuchPositionException e) {
            logger.log(System.Logger.Level.WARNING, "GetCommand tried to interact with not existing position");
            np.sendShortText(Priority.IMMEDIATE, "Error. Position not existing. Have a look in the magic log");
        }
    }

    private void giveAll(MUDEntity source, MUDEntity target) {
        List<MUDEntity> itemsToGive = source.getAllFromInventory(null);
        if (itemsToGive.isEmpty()) {
            source.sendShortText(Priority.IMMEDIATE, Localization.fillString("command.give.mess.noitems"));
        } else {
            String allItemNames = itemsToGive.stream().map(MUDEntity::getName).collect(Collectors.joining(", "));
            logger.log(System.Logger.Level.DEBUG, "{0} gives {1} to {2}", source.getName(),
                    allItemNames, target.getName());
            itemsToGive.forEach(removeFromInventoryAndAddToTargetInventory(source, target));
            logger.log(System.Logger.Level.DEBUG, "Removed {0} from inventory of {0} and give to {2}",
                    allItemNames, source.getName(), target.getName());
        }
    }

    private static Consumer<MUDEntity> removeFromInventoryAndAddToTargetInventory(MUDEntity source, MUDEntity target) {
        return i -> {
            source.removeFromInventory(i);
            target.addToInventory(i);
            source.sendShortText(Priority.IMMEDIATE, Localization.fillString("command.give.mess.self", i.getName()));
            if (target instanceof PlayerCharacter player) {
                player.getConnection().sendShortText(Priority.IMMEDIATE, Localization.fillString("command.give.mess.target",
                        source.getName(), i.getName(), target.getName()));
            }
            CommandUtil.sendOthersWithoutSelfAndTarget(source, target, Localization.fillString("command.give.mess.other",
                    source.getName(), i.getName(), target.getName()));
        };
    }

    private void giveNamedItems(MUDEntity lifeForm, String itemName, MUDEntity target) {
        //TODO Umstellen auf mehrere und Keyword-Prüfung
        MUDEntity itemToGive = lifeForm.getFromInventory(itemName);
        if (itemToGive == null) {
            lifeForm.sendShortText(Priority.IMMEDIATE, Localization.fillString("command.give.mess.nosuchitem", itemName, target.getName()));
        } else {
            String realItemName = itemToGive.getName();
            logger.log(System.Logger.Level.DEBUG, "{0} gives {1} to {2}", lifeForm.getName(), realItemName, target.getName());
            lifeForm.removeFromInventory(itemToGive);
            logger.log(System.Logger.Level.DEBUG, "Removed {0} from inventory", realItemName);
            target.addToInventory(itemToGive);
            lifeForm.sendShortText(Priority.IMMEDIATE, Localization.fillString("command.give.mess.self", realItemName, target.getName()));
            CommandUtil.sendOthersWithoutSelfAndTarget(lifeForm, target, Localization.fillString("command.give.mess.other",
                    lifeForm.getName(), realItemName, target.getName()));
            target.sendShortText(Priority.PERSONAL, Localization.fillString("command.give.mess.target",
                    lifeForm.getName(), realItemName));
        }
    }
}
