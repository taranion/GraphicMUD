/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.behavior;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.text.ParseException;

import org.junit.Test;
import org.prelle.simplepersist.Persister;
import org.prelle.simplepersist.SerializationException;

import com.graphicmud.behavior.CompositeNode;
import com.graphicmud.behavior.RandomNode;
import com.graphicmud.behavior.TreeLoader;

/**
 * 
 */
public class BehaviorTreeParserTest {
	
	static Persister persister = new Persister();

	//-------------------------------------------------------------------
	@Test
	public void testTreeParsing() throws Exception {
		String data = "<random id=\"root\">\r\n"
//				+ "  <social method=\"yawn\"/>\r\n"
				+ "  <communicate method=\"say\">\r\n"
				+ "    <text>Hello</text>\r\n"
				+ "  </communicate>\r\n"
				+ "  <sequence id=\"goToBed\">\r\n"
				+ "    <moveToRoom method=\"find\" param=\"10302\"/>\r\n"
				+ "    <doors method=\"lockAll\"/>\r\n"
				+ "    <moveInRoom method=\"find\" param=\"15\"/>\r\n"
				+ "    <rest method=\"sleep\"/>\r\n"
				+ "  </sequence>\r\n"
				+ "  <sequence id=\"follow\">\r\n"
				+ "    <selectAsTarget method=\"playerInRoom\"/>\r\n"
				+ "    <actOnTarget method=\"follow\"/> <!-- Registriert Event Listener -->\r\n"
				+ "  </sequence>\r\n"
				+ "</random>";
		CompositeNode node = TreeLoader.read(data);
		assertNotNull(node);
		assertEquals(RandomNode.class, node.getClass());
	}

}
