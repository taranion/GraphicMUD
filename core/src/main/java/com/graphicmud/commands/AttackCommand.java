/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.commands;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.graphicmud.Localization;
import com.graphicmud.MUD;
import com.graphicmud.combat.AutoCombat;
import com.graphicmud.game.MUDClock;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.network.ClientConnection.Priority;
import com.graphicmud.world.Location;
import com.graphicmud.world.NoSuchPositionException;
import com.graphicmud.world.Position;

/**
 *
 */
public class AttackCommand extends ACommand {

	private final static Logger logger = System.getLogger(AttackCommand.class.getPackageName());

	//-------------------------------------------------------------------
	public AttackCommand() {
		super(CommandGroup.COMBAT, "attack", Localization.getI18N());
	}

	//-------------------------------------------------------------------
	/**
	 * @see Command#execute(MUDEntity, Map)
	 */
	@Override
	public void execute(MUDEntity np, Map<String, Object> params) {
		logger.log(Level.INFO, "Execute for {0} with {1}", np.getName(), params);
		String targetName = (String) params.get("target");
	    Position position = np.getPosition();
	    MUDEntity target = null;
	    Location room = null;
	    try {
	    	target = CommandUtil.getMobileFromPosition(targetName, position);
	        if (target == np) {
	            np.sendShortText(Priority.IMMEDIATE, Localization.getString("command.attack.mess.targetIsSelf"));
	            return;
	        }
	        if (target == null) {
	            np.sendShortText(Priority.IMMEDIATE, Localization.fillString("command.attack.mess.nosuchplayer", targetName));
	            return;
	        }
	        room = MUD.getInstance().getWorldCenter().getLocation(position.getRoomPosition().getRoomNumber());
        } catch (NoSuchPositionException e) {
            logger.log(System.Logger.Level.WARNING, "GetCommand tried to interact with not existing position");
            np.sendShortText(Priority.IMMEDIATE, "Error. Position not existing. Have a look in the magic log");
        }
	    
	    AutoCombat combat = AutoCombat.builder()
	    		.withListener(MUD.getInstance().getRpgConnector().createNewCombatHandler())
	    		.withCombatantsSideA(List.of(np))
	    		.withCombatantsSideB(List.of(target))
	    		.withLocation(room)
	    		.build();
	    combat.prepare();
	    target.sendShortText(Priority.IMMEDIATE, fillString("mess.target", Locale.getDefault(), np.getName()));
	    MUDClock.registerCombat(combat);
	    
		logger.log(Level.DEBUG, "LEAVE execute()");
	}

}
