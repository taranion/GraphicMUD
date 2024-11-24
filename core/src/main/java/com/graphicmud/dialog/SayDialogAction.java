/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.dialog;

import org.prelle.simplepersist.CData;

import lombok.Getter;

/**
 * 
 */
public class SayDialogAction extends DialogAction {

	@CData
	@Getter
	private String text;
	
}
