/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.dialog;

import org.prelle.ansi.ANSIOutputStream;

public class DialogMenuTest {

	public static void main(String[] args) {
		ANSIOutputStream out = new ANSIOutputStream(System.out);
		StringBuffer buf = new StringBuffer();
		buf.append('┌');
		buf.repeat('─', 20);
		buf.append('┬');
		buf.repeat('─', 40);
		buf.append('┐');
		buf.append("\r\n");
		buf.append('│');
		buf.repeat(' ', 20);
		buf.append('│');
		System.out.println(buf.toString());

	}

}
