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
public class Echo {
	
	/** May be a i18n key or otherwise just the text */
	public String i18nOrText;
	public Object[] data;

	//-------------------------------------------------------------------
	public Echo(String i18nOrText, Object...data) {
		if (i18nOrText==null) throw new NullPointerException();
		this.i18nOrText = i18nOrText;
		this.data       = data;
	}
	//-------------------------------------------------------------------
	public String getId() { return "Echo("+i18nOrText+")";}

	//-------------------------------------------------------------------
	public String toString() {
		return "ECHO:"+i18nOrText;
	}

//	//-------------------------------------------------------------------
//	public void accept(MUDEntity performedBy, Context context) {
//		Locale loc = ActionUtil.getLocale(performedBy);
//		String text = Localization.fillString(i18nOrText, loc, data);
//		performedBy.sendShortText(Priority.PERSONAL, text);
//	}

	//-------------------------------------------------------------------
	public void sendSelf(MUDEntity performedBy, Context context) {
		Locale loc = ActionUtil.getLocale(performedBy);
		String text = Localization.fillString(i18nOrText, loc, data);
		performedBy.sendShortText(Priority.PERSONAL, text);
	}

	//-------------------------------------------------------------------
	public void sendTarget(MUDEntity performedBy, Context context) {
		MUDEntity target = context.get(ParameterType.TARGET);
		Locale loc = ActionUtil.getLocale(target);
		String text = Localization.fillString(i18nOrText, loc, data);
		target.sendShortText(Priority.PERSONAL, text);
	}

	//-------------------------------------------------------------------
	public void sendOthers(MUDEntity performedBy, Context context) {
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

	//-------------------------------------------------------------------
	public void sendOthersTargetRoom(MUDEntity performedBy, Context context) {
		Location room = context.get(ParameterType.ROOM_TARGET);
		for (MUDEntity entity : room.getEntities()) {
			if (entity==performedBy) continue;
			if (entity instanceof PlayerCharacter) {
				Locale loc = ActionUtil.getLocale(entity);
				String text = Localization.fillString(i18nOrText, loc, data);
				entity.sendShortText(Priority.UNIMPORTANT, text);
			}
		}
	}

	//-------------------------------------------------------------------
	public void sendOthersExceptTarget(MUDEntity performedBy, Context context) {
		Location room = context.get(ParameterType.ROOM_CURRENT);
		MUDEntity target = context.get(ParameterType.TARGET);
		for (MUDEntity entity : room.getEntities()) {
			if (entity==performedBy) continue;
			if (entity==target) continue;
			if (entity instanceof PlayerCharacter) {
				Locale loc = ActionUtil.getLocale(entity);
				String text = Localization.fillString(i18nOrText, loc, data);
				entity.sendShortText(Priority.UNIMPORTANT, text);
			}
		}
	}

}
