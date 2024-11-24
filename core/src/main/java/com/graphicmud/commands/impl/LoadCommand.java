/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.commands.impl;

import java.util.Locale;
import java.util.Map;

import com.graphicmud.Identifier;
import com.graphicmud.Localization;
import com.graphicmud.MUD;
import com.graphicmud.commands.ACommand;
import com.graphicmud.commands.CommandGroup;
import com.graphicmud.commands.CommandUtil;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MUDEntityTemplate;
import com.graphicmud.network.ClientConnection;
import com.graphicmud.world.Location;
import com.graphicmud.world.NoSuchPositionException;

public class LoadCommand extends ACommand {
    public LoadCommand() {
        super(CommandGroup.ADMIN, "load", Localization.getI18N());
    }

    @Override
    public void execute(MUDEntity np, Map<String, Object> params) {
        logger.log(System.Logger.Level.DEBUG, "Execute for {0} with {1}", np.getName(), params);
        LoadType type = (LoadType) params.get("type");
        Identifier entity = new Identifier( (String) params.get("entity"));
        if (type == null) {
            np.sendShortText(ClientConnection.Priority.IMMEDIATE, getString("mess.typemissing"));
            return;
        }
        if (entity == null) {
            np.sendShortText(ClientConnection.Priority.IMMEDIATE, getString("mess.entity"));
            return;
        }
        MUDEntityTemplate template = null;
        switch (type) {
            case ITEM -> {
                template = MUD.getInstance().getWorldCenter().getItemTemplate(entity);
            }
            case MOBILE -> {
                template = MUD.getInstance().getWorldCenter().getMobileTemplate(entity);
            }
        }
        if (template == null) {
            np.sendShortText(ClientConnection.Priority.IMMEDIATE, fillString("mess.notemplate", entity));
            return;
        }
        MUDEntity entityToLoad = MUD.getInstance().getGame().instantiate(template);
        try {
            Location location = MUD.getInstance().getWorldCenter().getLocation(np.getPosition().getRoomPosition()
                    .getRoomNumber());
            location.addEntity(entityToLoad);
            entityToLoad.setPosition(np.getPosition());
            np.sendShortText(ClientConnection.Priority.IMMEDIATE, fillString("mess.self", entityToLoad.getName()));
            CommandUtil.sendOthersWithoutSelf(np, fillString("mess.other", np.getName(), entityToLoad.getName()));
        } catch (NoSuchPositionException e) {
            np.sendShortText(ClientConnection.Priority.IMMEDIATE, "Error. Cannot find your position");
        }
    }
    
   public enum LoadType{
        ITEM,
        MOBILE;

       public String getName(Locale loc) {
           return Localization.getString("enum.loadtype."+this.name().toLowerCase());
       }
       public static String[] values(Locale loc) {
           String[] translated = new String[LoadType.values().length];
           for (int i=0; i<translated.length; i++) {
               translated[i]= LoadType.values()[i].getName(loc);
           }
           return translated;
       }
       public static LoadType valueOf(Locale loc, String val) {
           for (LoadType type : LoadType.values()) {
               if (type.getName(loc).equalsIgnoreCase(val))
                   return type;
           }
           return null;
       }
   }
}
