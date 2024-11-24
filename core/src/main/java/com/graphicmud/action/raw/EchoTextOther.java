/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.action.raw;

import java.util.Locale;

import com.graphicmud.Localization;
import com.graphicmud.action.ActionUtil;
import com.graphicmud.action.cooked.ParameterType;
import com.graphicmud.behavior.Context;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.network.ClientConnection.Priority;
import com.graphicmud.player.PlayerCharacter;
import com.graphicmud.world.Location;

/**
 * 
 */
public class EchoTextOther implements RawAction {
	
	/** May be a i18n key or otherwise just the text */
	public String i18nOrText;
	public Object[] data;

	//-------------------------------------------------------------------
	public EchoTextOther(String i18nOrText, Object...data) {
		this.i18nOrText = i18nOrText;
		this.data       = data;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.action.raw.RawAction#execute(com.graphicmud.game.MUDEntity, com.graphicmud.behavior.Context)
	 */
	@Override
	public void accept(MUDEntity performedBy, Context context) {
		Location room = context.get(ParameterType.ROOM_CURRENT);
		for (MUDEntity entity : room.getEntities()) {
			if (entity==performedBy) continue;
			if (entity instanceof PlayerCharacter) {
				Locale loc = ActionUtil.getLocale(entity);
				String text = Localization.fillString(i18nOrText, loc, data);
				entity.sendShortText(Priority.UNIMPORTANT, text);
			}
		}
	}

}
