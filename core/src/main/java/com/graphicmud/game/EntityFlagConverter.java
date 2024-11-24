/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.game;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.prelle.simplepersist.SerializationException;
import org.prelle.simplepersist.XMLElementConverter;
import org.prelle.simplepersist.marshaller.XmlNode;
import org.prelle.simplepersist.unmarshal.XMLTreeItem;

/**
 * 
 */
public class EntityFlagConverter implements XMLElementConverter<EntityFlag> {

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.simplepersist.XMLElementConverter#write(org.prelle.simplepersist.marshaller.XmlNode, java.lang.Object)
	 */
	@Override
	public void write(XmlNode node, EntityFlag value) throws Exception {
		// TODO Auto-generated method stub
		System.err.println("EntityFlagConverter.write() not implemented");
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.simplepersist.XMLElementConverter#read(org.prelle.simplepersist.unmarshal.XMLTreeItem, javax.xml.stream.events.StartElement, javax.xml.stream.XMLEventReader)
	 */
	@Override
	public EntityFlag read(XMLTreeItem node, StartElement ev, XMLEventReader evRd) throws Exception {
		// Expect a text child
		XMLEvent nextEv = evRd.nextEvent();
		if (nextEv instanceof Characters) {
			String text = nextEv.asCharacters().getData();
			return EntityFlag.valueOf(text);
		}
		throw new SerializationException("<flags> expects characters but got "+nextEv);
	}

}
