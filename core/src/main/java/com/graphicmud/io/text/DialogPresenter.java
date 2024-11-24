/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.io.text;

import com.graphicmud.network.ClientConnectionListener;

/**
 * 
 */
public interface DialogPresenter {

	public ClientConnectionListener getInputHandler();
	
}
