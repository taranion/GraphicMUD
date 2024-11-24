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
import com.graphicmud.player.PlayerCharacter;
import com.graphicmud.world.Location;
import com.graphicmud.world.NoSuchPositionException;
import com.graphicmud.world.Position;

public class WhereCommand extends ACommand {
    private final static System.Logger logger = System.getLogger(WhereCommand.class.getPackageName());
    
    public WhereCommand() {
        super(CommandGroup.ADMIN, "where", Localization.getI18N());
    }

    @Override
    public void execute(MUDEntity np, Map<String, Object> params) {
        List<PlayerCharacter> players = MUD.getInstance().getGame().getPlayers();
        StringBuilder message = new StringBuilder("<u>Player locations</u><br/>");
        for (PlayerCharacter player : players) {
            Position position = player.getPosition();
            Identifier roomNumber = position.getRoomPosition().getRoomNumber();
            String title = "unknown/not existing";
            try {
                Location location = MUD.getInstance().getWorldCenter().getLocation(roomNumber);
                if (location.getRoomComponent().isPresent()) {
                    title = location.getRoomComponent().get().getTitle();
                }
            } catch (NoSuchPositionException e) {
            }
            message.append("<b>" + player.getName() + "</b> - ");
            message.append(roomNumber + ": " + title);
            message.append("<br/>");
        }
        np.sendTextWithMarkup(message.toString());
    }
}
