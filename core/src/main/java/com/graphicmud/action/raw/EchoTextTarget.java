/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.action.raw;

import java.util.Locale;

import com.graphicmud.Localization;
import com.graphicmud.action.ActionUtil;
import com.graphicmud.behavior.Context;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.network.ClientConnection.Priority;

/**
 * 
 */
public class EchoTextTarget implements RawAction {
	
	/** May be a i18n key or otherwise just the text */
	public String i18nOrText;
	public Object[] data;

	//-------------------------------------------------------------------
	public EchoTextTarget() {
	}

	//-------------------------------------------------------------------
	public EchoTextTarget(String i18nOrText, Object...data) {
		this.i18nOrText = i18nOrText;
		this.data       = data;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.action.raw.RawAction#execute(com.graphicmud.game.MUDEntity, com.graphicmud.behavior.Context)
	 */
	@Override
	public void accept(MUDEntity performedBy, Context context) {
		Locale loc = ActionUtil.getLocale(performedBy);
		String text = Localization.fillString(i18nOrText, loc, data);
		performedBy.sendShortText(Priority.PERSONAL, text);
	}

}
