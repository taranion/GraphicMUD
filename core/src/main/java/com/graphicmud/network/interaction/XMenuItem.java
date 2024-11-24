/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.network.interaction;

import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.graphicmud.network.ClientConnection;
import com.graphicmud.network.ClientConnectionListener;

/**
 *
 */
public class XMenuItem {

	/** Used to handle responses */
	private String identifier;
	/** Label to display to user */
	private String label;
	/** Optional emoji to add */
	private String emoji;
	private Optional<URI> detailsURI;
	private Optional<Path> thumbnail;

	/**
	 * Optional class that should be used and initialized once this menu item
	 * has been selected.
	 */
	private ClientConnectionListener chainedHandler;
	private BiConsumer<XMenuItem, ClientConnection> executeOnSelect;

	//-------------------------------------------------------------------
	public XMenuItem(String identifier, String label) {
		this.identifier = identifier;
		this.label      = label;
	}

	//-------------------------------------------------------------------
	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

	//-------------------------------------------------------------------
	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	//-------------------------------------------------------------------
	/**
	 * @return the emoji
	 */
	public String getEmoji() {
		return emoji;
	}

	//-------------------------------------------------------------------
	/**
	 * @return the chainedHandler
	 */
	public ClientConnectionListener getChainedHandler() {
		return chainedHandler;
	}

	//-------------------------------------------------------------------
	/**
	 * @param chainedHandler the chainedHandler to set
	 */
	public XMenuItem setChainedHandler(ClientConnectionListener chainedHandler) {
		this.chainedHandler = chainedHandler;
		return this;
	}

	//-------------------------------------------------------------------
	/**
	 * @return the executeObSelect
	 */
	public BiConsumer<XMenuItem, ClientConnection> getExecuteOnSelect() {
		return executeOnSelect;
	}

	//-------------------------------------------------------------------
	/**
	 * @param executeObSelect the executeObSelect to set
	 */
	public XMenuItem setExecuteOnSelect(BiConsumer<XMenuItem, ClientConnection> value) {
		this.executeOnSelect = value;
		return this;
	}

}
