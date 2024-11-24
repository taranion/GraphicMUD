/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.List;

import org.junit.Test;

import com.graphicmud.commands.CommandSyntaxParser;
import com.graphicmud.commands.CommandSyntaxParser.CommandElement;
import com.graphicmud.commands.CommandSyntaxParser.FixWord;
import com.graphicmud.commands.CommandSyntaxParser.Variable;

/**
 * 
 */
public class CommandSyntaxParserTest {

	//-------------------------------------------------------------------
	@Test
	public void testSingleFixed() throws ParseException {
		String text = "score";
		List<CommandElement> elements = CommandSyntaxParser.parse(text);
		assertNotNull("Should not return null", elements);
		assertEquals(1, elements.size());
		assertTrue(elements.get(0) instanceof FixWord);
		FixWord fixed = (FixWord) elements.get(0);
		assertEquals("score", fixed.getWord());
		assertFalse(fixed.isOptional());
	}

	//-------------------------------------------------------------------
	@Test
	public void testSingleFixedOptional() throws ParseException {
		String text = "[score]";
		List<CommandElement> elements = CommandSyntaxParser.parse(text);
		assertNotNull("Should not return null", elements);
		assertEquals(1, elements.size());
		assertTrue(elements.get(0) instanceof FixWord);
		FixWord fixed = (FixWord) elements.get(0);
		assertEquals("score", fixed.getWord());
		assertTrue( fixed.isOptional());
	}

	//-------------------------------------------------------------------
	@Test
	public void testTwoFixedWords() throws ParseException {
		String text = "health score";
		List<CommandElement> elements = CommandSyntaxParser.parse(text);
		assertNotNull("Should not return null", elements);
		assertEquals(2, elements.size());
		assertTrue(elements.get(0) instanceof FixWord);
		assertTrue(elements.get(1) instanceof FixWord);
	}

	//-------------------------------------------------------------------
	@Test
	public void testVariable() throws ParseException {
		String text = "{$some_variable}";
		List<CommandElement> elements = CommandSyntaxParser.parse(text);
		assertNotNull("Should not return null", elements);
		assertEquals(1, elements.size());
		assertTrue(elements.get(0) instanceof Variable);
		Variable var = (Variable) elements.get(0);
		assertFalse(var.isOptional());
		assertEquals("some_variable", var.getName());
		assertNull(var.getOptions());
		assertNull(var.getSelector());
	}

	//-------------------------------------------------------------------
	@Test
	public void testEnum() throws ParseException {
		String text = "{$dir,DIRECTION}";
		List<CommandElement> elements = CommandSyntaxParser.parse(text);
		assertNotNull("Should not return null", elements);
		assertEquals(1, elements.size());
		assertTrue(elements.get(0) instanceof Variable);
		Variable var = (Variable) elements.get(0);
		assertFalse(var.isOptional());
		assertEquals("dir", var.getName());
		assertNotNull(var.getOptions());
		assertNull(var.getSelector());
	}

	//-------------------------------------------------------------------
	@Test
	public void testBuy() throws ParseException {
		String text = "buy {$what} [{$amount}]";
		List<CommandElement> elements = CommandSyntaxParser.parse(text);
		assertNotNull("Should not return null", elements);
		assertEquals(3, elements.size());
		assertTrue(elements.get(0) instanceof FixWord);
		assertTrue(elements.get(1) instanceof Variable);
		assertTrue(elements.get(2) instanceof Variable);
		FixWord word = (FixWord) elements.get(0);
		assertFalse(word.isOptional());
		Variable var = (Variable) elements.get(1);
		assertFalse(var.isOptional());
		assertEquals("what", var.getName());
		var = (Variable) elements.get(2);
		assertTrue(var.isOptional());
		assertEquals("amount", var.getName());
	}

}
