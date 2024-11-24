/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.action;

import java.util.Locale;

import com.graphicmud.behavior.TreeResult;
import com.graphicmud.behavior.TreeResult.Result;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.player.PlayerCharacter;

/**
 * 
 */
public class ActionUtil {

	//-------------------------------------------------------------------
	public static Locale getLocale(MUDEntity performedBy) {
		return (performedBy instanceof PlayerCharacter)?((PlayerCharacter)performedBy).getLocale():Locale.getDefault();
	}

	//-------------------------------------------------------------------
	public static TreeResult error(String mess) {
		return TreeResult.builder()
				.value(Result.FAILURE)
				.errorMessage(mess)
				.build();	
	}
}
