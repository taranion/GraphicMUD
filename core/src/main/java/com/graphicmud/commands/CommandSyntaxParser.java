/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.graphicmud.commands.impl.DoorCommand.DoorAction;
import com.graphicmud.commands.impl.LoadCommand;
import com.graphicmud.player.ConfigOption;
import com.graphicmud.world.Location;
import com.graphicmud.world.text.Direction;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 
 */
public class CommandSyntaxParser {
	
	public static class CommandElement {
		@Getter
		protected boolean optional;		
	}
	
	@AllArgsConstructor
	public static class FixWord extends CommandElement {
		@Getter
		private String word;
		public String toString() { return "WORD:"+word+(optional?"(O)":""); }
	}
	
	@AllArgsConstructor
	public static class Variable extends CommandElement {
		@Getter
		private String name;
		@Getter
		private Function<Locale,String[]> options;
		@Getter
		private BiFunction<Locale,String,Object> optionBackResolver;
		@Getter
		private Function<Location,String[]> selector;
		
		public Variable (String name) {
			this(name,null,null,null);
		}
		public String toString() { return "VAR:"+name; }
	}
	public static class RestOfLine extends Variable {
		public RestOfLine(String name) {
			super(name);
		}
		public String toString() { return "REST_OF_LINE as "+super.toString();}
	}

	
	private static Map<String, Function<Locale,String[]>> typeRegistryStatic = new HashMap<>();
	private static Map<String, BiFunction<Locale,String,Object>> typeRegistryStaticBack = new HashMap<>();
	private static Map<String, Function<Location,String[]>> typeRegistryDynamic = new HashMap<>();
	
	static {
		registerLocalizedOptionProvider("DIRECTION",
                Direction::values,
                Direction::valueOf
				);
		registerLocalizedOptionProvider("CHANNEL",
                CommunicationChannel::values,
                CommunicationChannel::valueOf
				);
		registerLocalizedOptionProvider("CONFIGOPTION",
				ConfigOption::values,
				ConfigOption::valueOf
		);
		registerLocalizedOptionProvider("DOOR_ACTION",
                DoorAction::values,
                DoorAction::valueOf
		);
		registerLocalizedOptionProvider("LOADTYPE",
				LoadCommand.LoadType::values,
				LoadCommand.LoadType::valueOf
		);
	}
	
	//-------------------------------------------------------------------
	public static void registerLocalizedOptionProvider(String name, Function<Locale,String[]> provider, BiFunction<Locale,String,Object> back) {
		typeRegistryStatic.put(name, provider);
		typeRegistryStaticBack.put(name, back);
	}
		
	//-------------------------------------------------------------------
	public static void registerSelector(String name, Function<Location,String[]> selector) {
		typeRegistryDynamic.put(name, selector);
	}
	
	
	//-------------------------------------------------------------------
	public static List<CommandElement> parse(String text) throws ParseException {
		List<CommandElement> ret = new ArrayList<>();
		StringTokenizer tok = new StringTokenizer(text);
		while (tok.hasMoreTokens()) {
			ret.add( parseElement(tok.nextToken()) );
		}
		return ret;
	}

	//-------------------------------------------------------------------
	private static CommandElement parseElement(String text) throws ParseException {
		if (text.charAt(0)=='[') {
			int endPos = text.lastIndexOf(']');
			text = text.substring(1, endPos);
			CommandElement ret = parseElement(text);
			ret.optional=true;
			return ret;
		}
		if (text.charAt(0)=='{') {
			int endPos = text.lastIndexOf('}');
			text = text.substring(1, endPos).trim();
			// Should start with $
			if (!(text.charAt(0)=='$'))
				throw new ParseException("Variable should start with $: "+text, -1);
			text=text.substring(1);
			//Remaining text may have up to 3 tokens separated by ,
			StringTokenizer tok = new StringTokenizer(text,",");
			String varName = tok.nextToken();
			Function<Locale,String[]> staticProv = null;
			BiFunction<Locale,String,Object> backProv = null;
			Function<Location, String[]> selector = null;
			if (tok.hasMoreTokens()) {
				String staticProvS = tok.nextToken().trim();
				if ("!RESTOFLINE".equals(staticProvS)) {
					return new RestOfLine(varName);
				}
				if (!("-".equals(staticProvS))) {
					staticProv = typeRegistryStatic.get(staticProvS);
					if (staticProv==null)
						throw new ParseException("Variable "+varName+": "+staticProvS+" is not a registered provider", -1);
					backProv = typeRegistryStaticBack.get(staticProvS);
				}
				if (tok.hasMoreTokens()) {
					String selectorS = tok.nextToken().trim();
					if (!("-".equals(staticProvS))) {
						selector = typeRegistryDynamic.get(selectorS);
						if (selector==null)
							throw new ParseException("Variable "+varName+": "+selectorS+" is not a registered selector", -1);
					}
				}
			}
			return new Variable(varName, staticProv, backProv,selector);
		}
		return new FixWord(text);
	}

}
