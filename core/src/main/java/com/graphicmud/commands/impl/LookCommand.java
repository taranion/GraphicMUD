/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.commands.impl;

import java.lang.System.Logger.Level;
import java.util.Map;

import com.graphicmud.Localization;
import com.graphicmud.action.cooked.CookedActionHelper;
import com.graphicmud.action.cooked.CookedActionProcessor;
import com.graphicmud.action.cooked.Look;
import com.graphicmud.behavior.Context;
import com.graphicmud.commands.ACommand;
import com.graphicmud.commands.CommandGroup;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.network.ClientConnection.Priority;
import com.graphicmud.player.PlayerCharacter;
import com.graphicmud.world.text.Direction;

public class LookCommand extends ACommand {


    public LookCommand() {
        super(CommandGroup.EXAMINE, "look", Localization.getI18N());
    }

    //-------------------------------------------------------------------
    /**
     * @see com.graphicmud.commands.Command#execute(com.graphicmud.game.MUDEntity, java.util.Map)
     */
    @Override
    public void execute(MUDEntity np, Map<String, Object> params) {
        logger.log(System.Logger.Level.DEBUG, "Execute for {0} with {1}", np.getName(), params);
        Context context = new Context();
        if (!(np instanceof PlayerCharacter pc)) {
            logger.log(System.Logger.Level.WARNING, "LOOK ATM ONLY FOR PLAYERS " + np.getName());
            np.sendShortText(Priority.IMMEDIATE, "LOOK ATM ONLY FOR PLAYERS");
            return;
        }
        String target = (String) params.get("target");
        if (target == null || target.isBlank()) {
            logger.log(Level.DEBUG, "call LookAround");
            CookedActionProcessor.perform(Look::lookAround, pc, context);
            CookedActionProcessor.perform(new Look.LookAround(), pc, context);
            return;
        }
        Direction direction = CookedActionHelper.getDirectionByName(np.getLocale(), target);
        if (direction != null) {
            logger.log(Level.DEBUG, "call LookDirection");
            CookedActionProcessor.perform(new Look.LookDirection(direction), pc, context);
            return;
        }
        logger.log(Level.DEBUG, "call LookEntity");
        CookedActionProcessor.perform(new Look.LookEntity(target), pc, context);
    }

}
