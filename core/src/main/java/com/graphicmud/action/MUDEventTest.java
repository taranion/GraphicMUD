/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.action;

import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MUDEvent;

/**
 * 
 */
@FunctionalInterface
public interface MUDEventTest {
	
	public Boolean test(MUDEntity performer, MUDEvent ev);

}
