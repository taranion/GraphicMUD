/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.network.interaction;

import com.graphicmud.network.ClientConnection;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 *
 */
@Getter
//@SuperBuilder(builderMethodName = "hiddenBuilder")
@SuperBuilder
public class ToggleMenuItem<T> extends MenuItem<T> {

	private TriConsumer<ClientConnection, ToggleMenuItem<T>,T> onSelectPerform;
	private TriConsumer<ClientConnection, ToggleMenuItem,?> onDeselectPerform;

//	public static ToggleMenuItemBuilder builder(String identifier, String label) {
//        return hiddenBuilder().identifier(identifier).label(label);
//    }

}
