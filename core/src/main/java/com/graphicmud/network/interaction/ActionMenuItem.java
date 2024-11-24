/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.network.interaction;

import java.util.function.BiConsumer;

import com.graphicmud.network.ClientConnectionListener;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 *
 */
@SuperBuilder
@Getter
public class ActionMenuItem<T> extends MenuItem<T> {

	private BiConsumer<ActionMenuItem<T>,T> onActionPerform;
	private ClientConnectionListener finallyGoTo;

}
