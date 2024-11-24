/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.dialog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.text.ParseException;

import org.junit.Test;
import org.prelle.simplepersist.Persister;
import org.prelle.simplepersist.SerializationException;

import com.graphicmud.dialog.DialogueTree;
import com.graphicmud.dialog.EmoteDialogAction;
import com.graphicmud.dialog.SayDialogAction;

/**
 * 
 */
public class DialogTreeParserTest {
	
	static Persister persister = new Persister();

	//-------------------------------------------------------------------
	@Test
	public void testDialog() throws ParseException, SerializationException, IOException {
		String dialog = "<dialog>\r\n"
				+ "  <emote>straits up and looks at you.</emote>\r\n"
				+ "  <say>I sense a soul in search of answers.</say>\r\n"
				+ "  <choice option=\"You said something about cheese ...\">\r\n"
				+ "    <answer>\r\n"
				+ "        <say>Yes. I like Wensleydale, Cheddar and Brie. Do you have some?</say>\r\n"
				+ "        <choice option=\"Unfortunately, No\">\r\n"
				+ "            <answer><say>Too bad</say></answer>\r\n"
				+ "        </choice>\r\n"
				+ "        <choice option=\"Yes\">\r\n"
				+ "            <answer><say>Oh, can I have some?</say></answer>\r\n"
				+ "        </choice>\r\n"
				+ "    </answer>\r\n"
				+ "  </choice>\r\n"
				+ "  <choice option=\"Ph'nglui mglw'nfah\">\r\n"
				+ "    <youSay>Ph'nglui mglw'nfah! Cthulhu R'lyeh wgah'nagl fhtagn!</youSay>\r\n"
				+ "    <answer>\r\n"
				+ "        <say>Ah, I see - you know the parole!</say>\r\n"
				+ "    </answer>\r\n"
				+ "  </choice>\r\n"
				+ "</dialog>";
		DialogueTree tree = persister.read(DialogueTree.class, dialog);
		assertNotNull("Should not return null", tree);
		assertNotNull(tree.getIntroActions());
		assertFalse("Intro actions not read",tree.getIntroActions().isEmpty());
		assertEquals("Intro actions not ALL read",2,tree.getIntroActions().size());
		
		assertEquals(EmoteDialogAction.class, tree.getIntroActions().get(0).getClass());
		assertEquals("straits up and looks at you.", ((EmoteDialogAction)tree.getIntroActions().get(0)).getText());
		assertEquals(SayDialogAction.class, tree.getIntroActions().get(1).getClass());
	}

}
