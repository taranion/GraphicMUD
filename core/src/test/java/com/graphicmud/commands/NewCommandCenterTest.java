/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import com.graphicmud.commands.NewCommandCenter.CommandPath;
import com.graphicmud.commands.impl.GiveCommand;
import com.graphicmud.commands.impl.Movement;
import com.graphicmud.commands.impl.communication.Communication;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.world.text.Direction;

/**
 * 
 */
public class NewCommandCenterTest {
	
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
	public void testMovement() throws ParseException {
		cCenter.registerCommand(new Movement());
		
		List<CommandPath> list = cCenter.getPossiblePathes((MUDEntity)null, "n", Locale.ENGLISH);
		assertNotNull(list);
		assertFalse(list.isEmpty());
		assertEquals(1, list.size());
		CommandPath result = list.getFirst();
		assertNotNull(result);
		System.out.println("Result = "+result);
		assertNotNull(result.getCommand());
		assertTrue( result.getCommand() instanceof Movement);
		assertNotNull(result.getVariables());
		assertTrue(result.getVariables().containsKey("dir"));
		assertEquals(Direction.NORTH,result.getVariables().get("dir"));
	}

	//-------------------------------------------------------------------
	@Test
	public void testCommunication() throws ParseException {
		cCenter.registerCommand(new Communication());
		
		List<CommandPath> list = cCenter.getPossiblePathes((MUDEntity)null, "shout Hello World", Locale.ENGLISH);
		assertNotNull(list);
		assertFalse(list.isEmpty());
		assertEquals(1, list.size());
		CommandPath result = list.getFirst();
		assertNotNull(result);
		System.out.println("Result = "+result);
		assertNotNull(result.getCommand());
		assertTrue( result.getCommand() instanceof Communication);
		assertNotNull(result.getVariables());
		assertTrue(result.getVariables().containsKey("channel"));
		assertEquals(CommunicationChannel.SHOUT,result.getVariables().get("channel"));
		assertEquals("Hello World",result.getVariables().get("msg"));
	}

	//-------------------------------------------------------------------
	@Test
	public void testGet() throws ParseException {
		cCenter.registerCommand(new GiveCommand());
		
		List<CommandPath> list = cCenter.getPossiblePathes((MUDEntity)null, "give longsword Grinner", Locale.ENGLISH);
		assertNotNull(list);
		assertFalse(list.isEmpty());
		assertEquals(1, list.size());
		CommandPath result = list.getFirst();
		assertNotNull(result);
		System.out.println("Result = "+result);
		assertNotNull(result.getCommand());
		assertTrue( result.getCommand() instanceof GiveCommand);
		assertNotNull(result.getVariables());
		assertTrue(result.getVariables().containsKey("item"));	
		assertTrue(result.getVariables().containsKey("target"));
		assertEquals("longsword",result.getVariables().get("item"));
		assertEquals("Grinner",result.getVariables().get("target"));
	}

//	//-------------------------------------------------------------------
//	@Test
//	public void testBuy() throws ParseException {
//		cCenter.registerCommand(new ShopBuyCommand());
//		
//		List<CommandPath> list = cCenter.getPossiblePathes((MUDEntity)null, "buy longsword 5", Locale.ENGLISH);
//		assertNotNull(list);
//		assertFalse(list.isEmpty());
//		assertEquals(1, list.size());
//		CommandPath result = list.getFirst();
//		assertNotNull(result);
//		System.out.println("Result = "+result);
//		assertNotNull(result.getCommand());
//	}

}
