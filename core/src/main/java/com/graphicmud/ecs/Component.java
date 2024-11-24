/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.ecs;

import java.nio.file.Path;

import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MUDEvent;

/**
 * 
 */
public class Component {

	public void prepare(Path zoneDir) { }
	
	public void handleEvent(MUDEntity you, MUDEvent event) {}
	
	public void pulse(MUDEntity you) {}
	
	public void tick(MUDEntity you) {}

}
