/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class EventBus {

	private final static Logger logger = System.getLogger("mud.events");

	public static interface MUDEventListener {
		public void handle();
	}

	private static List<MUDEventListener> listener = new ArrayList<MUDEventListener>();

	//-------------------------------------------------------------------
	public void addListener(MUDEventListener callback) {
		if (!listener.contains(callback)) {
			listener.add(callback);
		}
	}

	//-------------------------------------------------------------------
	public void fireEvent(Object x) {
		listener.forEach(callback -> {
			try {
				callback.handle();
			} catch (Exception e) {
				logger.log(Level.ERROR, "Error processing event "+x+" in "+callback.getClass().getSimpleName(),e);
			}
		});
	}

}
