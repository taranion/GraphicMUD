/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.io;

import org.prelle.simplepersist.StringValueConverter;

import com.graphicmud.Identifier;

/**
 * @author prelle
 *
 */
public class IdentifierConverter implements StringValueConverter<Identifier> {

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.simplepersist.StringValueConverter#write(org.prelle.simplepersist.XmlNode, java.lang.Object)
	 */
	@Override
	public String write(Identifier value) throws Exception {
		return value.toString();
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.simplepersist.StringValueConverter#read(org.prelle.simplepersist.Persister.ParseNode, javax.xml.stream.events.StartElement)
	 */
	@Override
	public Identifier read(String idref) throws Exception {
		return new Identifier(idref);
	}

}
