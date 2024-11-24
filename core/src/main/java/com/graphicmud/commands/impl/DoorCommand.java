/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.commands.impl;

import java.lang.System.Logger.Level;
import java.util.Locale;
import java.util.Map;

import com.graphicmud.Localization;
import com.graphicmud.MUD;
import com.graphicmud.action.ActionUtil;
import com.graphicmud.action.cooked.CookedAction;
import com.graphicmud.action.cooked.CookedActionProcessor;
import com.graphicmud.action.cooked.DoorsAction;
import com.graphicmud.action.cooked.ParameterType;
import com.graphicmud.behavior.Context;
import com.graphicmud.commands.ACommand;
import com.graphicmud.commands.CommandGroup;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.network.ClientConnection.Priority;
import com.graphicmud.world.Location;
import com.graphicmud.world.NoSuchPositionException;
import com.graphicmud.world.WorldCenter;
import com.graphicmud.world.text.Direction;
import com.graphicmud.world.text.Exit;

public class DoorCommand extends ACommand {
	
	public static enum DoorAction {
		OPEN,
		CLOSE,
		LOCK,
		UNLOCK
		;
		public String getName(Locale loc) {
			return Localization.getString("enum.door_action."+this.name().toLowerCase());
		}
		public static String[] values(Locale loc) {
			String[] translated = new String[DoorAction.values().length];
			for (int i=0; i<translated.length; i++) {
				translated[i]=DoorAction.values()[i].getName(loc);
			}
			return translated;
		}
		public static DoorAction valueOf(Locale loc, String val) {
			for (DoorAction dir : DoorAction.values()) {
				if (dir.getName(loc).equalsIgnoreCase(val))
					return dir;
			}
			return null;
		}
	}

    public DoorCommand() {
        super(CommandGroup.INTERACT, "door", Localization.getI18N());
    }

    //-------------------------------------------------------------------
    public static Direction getFirstDoor(MUDEntity actor) {
    	WorldCenter wc = MUD.getInstance().getWorldCenter();
    	try {
			Location loc = wc.getLocation(actor.getPosition());
			if (loc==null) return null;
			if (loc.getRoomComponent().isEmpty()) return null;
			for (Exit exit : loc.getRoomComponent().get().getExitList()) {
				if (exit.isDoor()) return exit.getDirection();
			}
		} catch (NoSuchPositionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return null;
    }

    //-------------------------------------------------------------------
    /**
     * @see com.graphicmud.commands.Command#execute(com.graphicmud.game.MUDEntity, java.util.Map)
     */
    @Override
    public void execute(MUDEntity actor, Map<String, Object> params) {
        logger.log(Level.INFO, "Execute for {0} with {1}", actor.getName(), params);
        DoorAction type = (DoorAction) params.get("action");
        Direction dir   = (Direction ) params.get("dir");
        logger.log(Level.DEBUG, "Action = {0}",type);
        logger.log(Level.DEBUG, "Direct = {0}",dir);
        
        // If there is no direction given, use the first exit that is a door
        if (dir==null) {
        	dir = DoorCommand.getFirstDoor(actor);
        	if (dir==null) {
        		actor.sendShortText(Priority.IMMEDIATE, this.getI18N("command.door.error.no_door_found", ActionUtil.getLocale(actor)));
        		return;
        	}
        }
        
        CookedAction action = switch (type) {
        case OPEN  -> DoorsAction::open;
        case CLOSE -> DoorsAction::close; 
        case LOCK  -> DoorsAction::lock;
        case UNLOCK-> DoorsAction::unlock;
        };
        Context context = new Context();
        if (dir!=null)
        	context.put(ParameterType.DIRECTION, dir);
        CookedActionProcessor.perform(action, actor, context);
    }
}
