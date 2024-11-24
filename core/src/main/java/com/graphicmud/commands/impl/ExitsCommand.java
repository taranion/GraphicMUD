/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.commands.impl;

import java.util.List;
import java.util.Map;

import com.graphicmud.Identifier;
import com.graphicmud.Localization;
import com.graphicmud.MUD;
import com.graphicmud.commands.ACommand;
import com.graphicmud.commands.CommandGroup;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.io.text.TextUtil;
import com.graphicmud.network.ClientConnection;
import com.graphicmud.world.NoSuchPositionException;
import com.graphicmud.world.text.Exit;
import com.graphicmud.world.text.RoomComponent;

public class ExitsCommand extends ACommand {
    public ExitsCommand() {
        super(CommandGroup.EXAMINE, "exits", Localization.getI18N());
    }

    @Override
    public void execute(MUDEntity np, Map<String, Object> params) {
        logger.log(System.Logger.Level.DEBUG, "Execute for {0} with {1}", np.getName(), params);
        try {
            RoomComponent roomComponent = MUD.getInstance().getWorldCenter().getLocation(np.getPosition()).getRoomComponent().orElse(null);
            if (roomComponent == null) {
                np.sendShortText(ClientConnection.Priority.IMMEDIATE, "Error. Unable to resolve your room.");
                return;
            }
            StringBuilder builder = new StringBuilder(getString("mess.title"));
            builder.append("<br/>");
            List<Exit> exitList = roomComponent.getExitList();
            for (Exit exit : exitList) {
                builder.append("<span width=\"8\">");
                builder.append(getExitName(np, exit));
                builder.append(": ");
                builder.append("</span>");
                builder.append(getTitle(exit));
                builder.append("<br/>");
            }
            np.sendTextWithMarkup(builder.toString());
        } catch (NoSuchPositionException e) {
            np.sendShortText(ClientConnection.Priority.IMMEDIATE, "Error. Unable to resolve your room.");
        }
    }

    private static String getExitName(MUDEntity np, Exit exit) {
        String name = exit.getDirection().getName(np.getLocale());
        return TextUtil.capitalize((name));
    }

    private static String getTitle(Exit exit) throws NoSuchPositionException {
        String title = exit.getTitle();
        if (title == null || title.isBlank()) {
            Identifier targetRoom = exit.getTargetRoom();
            RoomComponent roomComponent = MUD.getInstance().getWorldCenter().getLocation(targetRoom).getRoomComponent().orElse(null);
            if (roomComponent != null) {
                title = roomComponent.getTitle();
            }
        }
        return title;
    }
}
