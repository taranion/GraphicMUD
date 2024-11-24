/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.commands.impl.communication;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.graphicmud.Localization;
import com.graphicmud.MUD;
import com.graphicmud.commands.ACommand;
import com.graphicmud.commands.CommandGroup;
import com.graphicmud.commands.CommunicationChannel;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.network.ClientConnection.Priority;

public class Communication extends ACommand {
    
    private final static System.Logger logger = System.getLogger(Communication.class.getPackageName());
    
    public Communication() {
        super(CommandGroup.COMMUNIC, "communication", Localization.getI18N());
    }

    @Override
    public void execute(MUDEntity np, Map<String, Object> params) {
        logger.log(System.Logger.Level.DEBUG, "Execute for {0} with {1}", np.getName(), params);
        String msg = (String) params.get("msg");
        CommunicationChannel channel = (CommunicationChannel) params.get("channel");
        if (channel.isAllowedToUseChannel()) {
            sendSelf(np, msg, channel);
            sendAll(np, msg, channel);
        } else {
            sendNotAllowed(np, channel);
        }
    }

    private void sendNotAllowed(MUDEntity entity, CommunicationChannel channel) {
        entity.sendShortText(Priority.IMMEDIATE, "You are not allowed to use the channel: " + channel.getName().toLowerCase());
    }

    private void sendAll(MUDEntity entity, String message, CommunicationChannel channel) {
            List<MUDEntity> players = MUD.getInstance().getWorldCenter()
                    .getLifeformsInRangeExceptSelf(entity, channel.getRangeForChannel());
            players.stream().filter(channel.filterPlayers(entity)).forEach(sendPlayer(message,
                    entity, channel));
    }


    private Consumer<MUDEntity> sendPlayer(String message, MUDEntity np, CommunicationChannel channel) {
        return p -> {
            String prefixOthers = getString("enum." + channel.name().toLowerCase()+ ".prefixOthers");
            p.sendOnChannel(channel, np.getName()
                        + " " + prefixOthers + " \"" + message + "\"");
        };
    }

    private void sendSelf(MUDEntity np, String message, CommunicationChannel channel) {
        String prefix = getString("enum." + channel.name().toLowerCase() + ".prefixSelf");
        np.sendOnChannel(channel, MessageFormat.format("{0}, \"{1}\"", prefix, message));
    }
}
