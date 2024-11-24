/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud;

import java.util.Collection;

import com.graphicmud.commands.Command;

/**
 *
 */
public interface MUDPlugin {

	public Collection<Command> getCommands();

}
