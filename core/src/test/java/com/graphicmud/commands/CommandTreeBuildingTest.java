/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.commands;

import org.junit.Before;
import org.junit.Test;

import com.graphicmud.commands.CommandSyntaxParser;
import com.graphicmud.commands.CommunicationChannel;
import com.graphicmud.commands.NewCommandCenter;
import com.graphicmud.commands.NewCommandCenter.CommandNode;
import com.graphicmud.commands.impl.GiveCommand;
import com.graphicmud.commands.impl.Movement;
import com.graphicmud.commands.impl.QuitCommand;
import com.graphicmud.commands.impl.ShowClientCommand;
import com.graphicmud.commands.impl.communication.Communication;
import com.graphicmud.world.text.Direction;

import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * 
 */
public class CommandTreeBuildingTest {
	
	private NewCommandCenter cCenter;
	
	@Before
	public void setupTest() {
		cCenter.unitTestReset();
		cCenter = new NewCommandCenter();
		CommandSyntaxParser.registerLocalizedOptionProvider("DIRECTION", 
				(loc)     -> Direction.values(loc), 
				(loc,val) -> Direction.valueOf(loc,val)
				);
		CommandSyntaxParser.registerLocalizedOptionProvider("CHANNEL", 
				(loc)     -> CommunicationChannel.values(loc), 
				(loc,val) -> CommunicationChannel.valueOf(loc,val)
				);
	}

	//-------------------------------------------------------------------
	@Test
	public void testOneWord()  {
//		PropertyResourceBundle.getBundle(Localization.class.getName());
//		Localization.getI18N();
		QuitCommand com = new QuitCommand();
		cCenter.registerCommand(com);
		
		CommandNode node = cCenter.getTreeRoot(Locale.ENGLISH);
		assertNotNull(node);
		assertFalse(node.children.isEmpty());
		assertEquals(1, node.children.size());
	}

	//-------------------------------------------------------------------
	@Test
	public void testTwoWords()  {
		ShowClientCommand com = new ShowClientCommand();
		cCenter.registerCommand(com);
		
		CommandNode node = cCenter.getTreeRoot(Locale.ENGLISH);
		assertNotNull(node);
		assertFalse(node.children.isEmpty());
		assertEquals(1, node.children.size());
		CommandNode child = node.children.getFirst();
		assertFalse(child.children.isEmpty());
		assertEquals(1, child.children.size());
	}

	//-------------------------------------------------------------------
	@Test
	public void testTwoCommands()  {
		cCenter.registerCommand(new ShowClientCommand());
		cCenter.registerCommand(new QuitCommand());
		
		CommandNode node = cCenter.getTreeRoot(Locale.ENGLISH);
		assertNotNull(node);
		assertFalse(node.children.isEmpty());
		assertEquals(2, node.children.size());
		CommandNode child = node.children.getFirst();
		assertFalse(child.children.isEmpty());
		assertEquals(1, child.children.size());
	}

	//-------------------------------------------------------------------
	@Test
	public void testEnum()  {
		cCenter.registerCommand(new Movement());
		
		CommandNode node = cCenter.getTreeRoot(Locale.ENGLISH);
		assertNotNull(node);
		System.out.println(node.dump());
		assertFalse(node.children.isEmpty());
		assertEquals(7, node.children.size());
	}

	//-------------------------------------------------------------------
	@Test
	public void testGive()  {
		cCenter.registerCommand(new GiveCommand());
		
		CommandNode node = cCenter.getTreeRoot(Locale.ENGLISH);
		assertNotNull(node);
//		System.out.println(node.dump());
		assertFalse(node.children.isEmpty());
		assertEquals(1, node.children.size());
	}

	//-------------------------------------------------------------------
	@Test
	public void testCommunication()  {
		cCenter.registerCommand(new Communication());
		
		CommandNode node = cCenter.getTreeRoot(Locale.ENGLISH);
		assertNotNull(node);
		System.out.println(node.dump());
		assertFalse(node.children.isEmpty());
		assertEquals(10, node.children.size());
		// Must contain command
		assertNotNull(node.children.get(0).belongsTo);
	}

//	//-------------------------------------------------------------------
//	@Test
//	public void testBuy()  {
//		cCenter.registerCommand(new ShopBuyCommand());
//		
//		CommandNode node = cCenter.getTreeRoot(Locale.ENGLISH);
//		assertNotNull(node);
//		System.out.println(node.dump());
//		assertFalse(node.children.isEmpty());
//		assertEquals(1, node.children.size());
//		// Must contain command
//		assertNotNull(node.children.get(0).belongsTo);
//		// Grandchildren
//		node = node.children.get(0);
//		assertEquals(1, node.children.size());
//		assertNotNull(node.children.get(0).belongsTo);
//		// Grandchildren
//		node = node.children.get(0);
//		assertEquals(1, node.children.size());
//		assertNotNull(node.children.get(0).belongsTo);
//	}

}
