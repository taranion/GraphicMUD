/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.web;

import java.io.IOException;

/**
 *
 */
public interface WebServer {

	public void start() throws IOException;

	public void stop();

	public String getBaseURL();

}
