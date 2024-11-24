/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.network.interaction;

import java.io.IOException;
import java.util.List;

/**
 * Used by a client connection handler to control what elements of the menu are visible
 */
public interface VisualizedMenu {
	
	public void updateImage(byte[] data, String filename, int width, int height);
	
	public void writeScrollArea(String markup, boolean decorated);
	
	public void updateChoices(Menu menu, List<MenuItem<?>> choices) throws IOException;
	
	public void close();
	
	public MenuItem getMenuItemForInput(String input);

}
