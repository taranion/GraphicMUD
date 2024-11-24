/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.action.cooked;

import java.util.function.BiFunction;

import com.graphicmud.behavior.Context;
import com.graphicmud.game.MUDEntity;

/**
 * 
 */
@FunctionalInterface
public interface CookedActionTest extends BiFunction<MUDEntity, Context, CookedActionResult> {

	
}
