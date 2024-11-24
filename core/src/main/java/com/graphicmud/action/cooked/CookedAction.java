/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.action.cooked;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.function.BiFunction;

import com.graphicmud.behavior.Context;
import com.graphicmud.game.MUDEntity;

/**
 * 
 */
@FunctionalInterface
public interface CookedAction extends BiFunction<MUDEntity, Context, CookedActionResult>, Serializable {
	
	public default String getId() {
		return getClass().getSimpleName();
	}
	
	public default String getNameExpensive() {
        try {
            Method writeReplace = this.getClass().getDeclaredMethod("writeReplace");
            writeReplace.setAccessible(true);
            SerializedLambda sl = (SerializedLambda) writeReplace.invoke(this);
            return sl.getImplClass() + "::" + sl.getImplMethodName();
        } catch (Exception e) {
            return toString();
        }
    }

}
