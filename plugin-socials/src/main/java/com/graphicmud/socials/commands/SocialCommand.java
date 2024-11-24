/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.socials.commands;

import java.util.Map;

import com.graphicmud.Localization;
import com.graphicmud.action.RoomHelper;
import com.graphicmud.action.cooked.CookedActionProcessor;
import com.graphicmud.action.cooked.ParameterType;
import com.graphicmud.behavior.Context;
import com.graphicmud.commands.ACommand;
import com.graphicmud.commands.CommandGroup;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.socials.action.cooked.Social;

public class SocialCommand extends ACommand {

    //-------------------------------------------------------------------
    public SocialCommand() {
        super(CommandGroup.SOCIAL, "social", Localization.getI18N());
    }

    //-------------------------------------------------------------------
    /**
     * @see com.graphicmud.commands.Command#execute(com.graphicmud.game.MUDEntity, java.util.Map)
     */
    @Override
    public void execute(MUDEntity np, Map<String, Object> params) {
        logger.log(System.Logger.Level.DEBUG, "Execute for {0} with {1}", np.getName(), params);
        SocialType type = (SocialType) params.get("type");
        String targetName = (String) params.get("target");
		Context context = new Context();
		RoomHelper.getRoomByPosition(np, context);
		if (targetName!=null)
			context.put(ParameterType.TARGET_NAME, targetName);
		CookedActionProcessor.perform(new Social(type), np, context);
    }
}
