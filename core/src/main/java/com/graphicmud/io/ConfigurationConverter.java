/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.io;

import java.util.Map;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import org.prelle.simplepersist.XMLElementConverter;
import org.prelle.simplepersist.marshaller.XmlNode;
import org.prelle.simplepersist.unmarshal.XMLTreeItem;

import com.graphicmud.player.ConfigOption;

/**
 * 
 */
public class ConfigurationConverter implements XMLElementConverter<Map<ConfigOption, String>> {

	//-------------------------------------------------------------------
	/**
	 */
	public ConfigurationConverter() {
		// TODO Auto-generated constructor stub
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.simplepersist.XMLElementConverter#write(org.prelle.simplepersist.marshaller.XmlNode, java.lang.Object)
	 */
	@Override
	public void write(XmlNode node, Map<ConfigOption, String> value) throws Exception {
		// TODO Auto-generated method stub

	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.simplepersist.XMLElementConverter#read(org.prelle.simplepersist.unmarshal.XMLTreeItem, javax.xml.stream.events.StartElement, javax.xml.stream.XMLEventReader)
	 */
	@Override
	public Map<ConfigOption, String> read(XMLTreeItem node, StartElement ev, XMLEventReader evRd) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
