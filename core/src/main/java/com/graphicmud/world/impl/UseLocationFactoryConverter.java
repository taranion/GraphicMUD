/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.world.impl;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import org.prelle.simplepersist.XMLElementConverter;
import org.prelle.simplepersist.marshaller.XmlNode;
import org.prelle.simplepersist.unmarshal.XMLTreeItem;

import com.graphicmud.Identifier;
import com.graphicmud.MUD;
import com.graphicmud.world.Location;

/**
 *
 */
public class UseLocationFactoryConverter implements XMLElementConverter<Location> {

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.simplepersist.XMLElementConverter#write(org.prelle.simplepersist.marshaller.XmlNode, java.lang.Object)
	 */
	@Override
	public void write(XmlNode node, Location value) throws Exception {
		System.err.println("UseLocationFactoryConverter.write "+value+" into "+node);
		// TODO Auto-generated method stub
		throw new IOException("Writing not supported yet");
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.simplepersist.XMLElementConverter#read(org.prelle.simplepersist.unmarshal.XMLTreeItem, javax.xml.stream.events.StartElement, javax.xml.stream.XMLEventReader)
	 */
	@Override
	public Location read(XMLTreeItem node, StartElement ev, XMLEventReader evRd) throws Exception {
//		if (MUD.getInstance()==null) return null;
		Location room = MUD.getInstance().getWorldCenter().createLocation(null, null);
		String roomNr = ev.getAttributeByName(QName.valueOf("nr")).getValue();
		room.setNr(new Identifier(roomNr));
		return  room;
	}

}
