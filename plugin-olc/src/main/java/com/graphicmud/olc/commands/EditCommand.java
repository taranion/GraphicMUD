/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.olc.commands;

import java.util.Map;

import com.graphicmud.Identifier;
import com.graphicmud.Localization;
import com.graphicmud.MUD;
import com.graphicmud.commands.ACommand;
import com.graphicmud.commands.CommandGroup;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.handler.OLCEditRoomHandler;
import com.graphicmud.network.ClientConnection;
import com.graphicmud.network.ClientConnection.Priority;
import com.graphicmud.player.PlayerCharacter;
import com.graphicmud.world.Location;
import com.graphicmud.world.NoSuchPositionException;

public class EditCommand extends ACommand {
    public EditCommand() {
        super(CommandGroup.ADMIN, "edit", Localization.getI18N());
    }

    @Override
    public void execute(MUDEntity entity, Map<String, Object> params) {
        logger.log(System.Logger.Level.DEBUG, "Execute for {0} with {1}", entity.getName(), params);
        if (!(entity instanceof PlayerCharacter pc)) {
            entity.sendShortText(Priority.IMMEDIATE, "EDITING ATM ONLY FOR PLAYERS");
            logger.log(System.Logger.Level.WARNING, "EDITING ATM ONLY FOR PLAYERS");
            return;
        }
        String numberString = (String)params.get("number");
        Integer number = numberString != null && !numberString.isEmpty() ? Integer.parseInt(numberString) : -1;
        EditType type = (EditType) params.get("type");
        logger.log(System.Logger.Level.DEBUG, "EDIT {0} no {1}", type, number);
        switch (type){
            case ROOM -> {
                Identifier roomnummber = entity.getPosition().getRoomPosition().getRoomNumber();
                if (number > 0)  {
                    roomnummber = Identifier.of(roomnummber, number);
                }
                ClientConnection connection = pc.getConnection();
                try {
                    Location room = MUD.getInstance().getWorldCenter().getLocation(roomnummber);
                    connection.pushConnectionListener(new OLCEditRoomHandler(connection.getClientConnectionListener(), room));
                } catch (NoSuchPositionException e) {
                    connection.sendShortText(Priority.IMMEDIATE, "You cannot edit room " + roomnummber);
                }
            }
            case ZONE -> {
            }
        }

    }
}
