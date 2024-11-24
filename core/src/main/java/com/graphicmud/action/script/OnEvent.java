/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.action.script;

import com.graphicmud.game.MUDEvent;

public interface OnEvent {

	MUDEvent.Type getType();

	String getValue();

}