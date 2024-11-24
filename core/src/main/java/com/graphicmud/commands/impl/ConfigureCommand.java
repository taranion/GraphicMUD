/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.commands.impl;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.graphicmud.Localization;
import com.graphicmud.commands.ACommand;
import com.graphicmud.commands.CommandGroup;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.network.MUDClientCapabilities;
import com.graphicmud.network.ClientConnection.Priority;
import com.graphicmud.network.MUDClientCapabilities.Graphic;
import com.graphicmud.player.ConfigOption;
import com.graphicmud.player.ImageProtocol;
import com.graphicmud.player.PlayerCharacter;
import com.graphicmud.player.ConfigOption.ConfigOptionType;

/**
 * @author Stefan Prelle
 *
 */
public class ConfigureCommand extends ACommand {

	private final static Logger logger = System.getLogger(ConfigureCommand.class.getPackageName());

	//------------------------------------------------
	public ConfigureCommand() {
		super(CommandGroup.HELP, "configure", Localization.getI18N());
 	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.commands.Command#execute(com.graphicmud.player.PlayerCharacter, java.util.regex.Matcher)
	 */
	@Override
	public void execute(MUDEntity np, Map<String, Object> params) {
		logger.log(Level.TRACE, "Execute for {0} with {1}", np.getName(), params);
		if (params.isEmpty()) {
			overview((PlayerCharacter) np);
			return;
		}
		Locale loc = np.getLocale();
		ConfigOption option = ConfigOption.startingWith(np.getLocale(), (String)params.get("option"));
		// Check it was a valid option
		if (option==null) {
			np.sendShortText(Priority.IMMEDIATE, fillString("no_such_option", loc, Arrays.toString(ConfigOption.values(loc))));
			return;
		}
		String value = (String)params.get("value");
		switch (option) {
		case FORMAT:
			switchFormat((PlayerCharacter) np, value);
			return;
		case IMAGE_PROTOCOL:
			imageProtocolSupport((PlayerCharacter) np, option, value);
			return;
		case HEALTH_EVERY_TICK:
			health((PlayerCharacter) np, value);
			return;
		default:
			np.sendShortText(Priority.IMMEDIATE, fillString("not_implemented", loc, option.getName(loc)));
		}
		logger.log(Level.TRACE, "LEAVE execute()");
	}

	//-------------------------------------------------------------------
	/**
	 * Send an overview of the current settings
	 */
	private void overview(PlayerCharacter performedBy) {
		List<ConfigOption> list = new ArrayList<ConfigOption>(List.of(ConfigOption.values()));
		// Sort alphabetical
		Locale loc = performedBy.getLocale();
		Collections.sort(list, new Comparator<ConfigOption>() {
			public int compare(ConfigOption a1, ConfigOption a2) {				
				return a1.getName(loc).compareTo(a2.getName(loc));
			}
		});
		
		StringBuilder buf = new StringBuilder();
		for (ConfigOption opt : list) {
			buf.append("<b>"+opt.getName(loc)+"</b> : "+performedBy.getConfiguration(opt)+"<br/>");
			buf.append("<span width=\"3\">   </span>");
			buf.append(opt.getExplanation(loc));
			if (opt.getType()==ConfigOptionType.ENUM) {
				buf.append("<br/><span width=\"3\">   </span>Valid values are: ");
				buf.append( Arrays.toString( opt.getEnumType().getEnumConstants() ));
				buf.append("<br/>");
			}
			buf.append("<br/>");
		}
		performedBy.sendTextWithMarkup(buf.toString());
	}

	//-------------------------------------------------------------------
	private static boolean asBoolean(String value) {
		value = value.toLowerCase();
		switch (value) {
		case "1" :
		case "on": 
		case "true": 
		case "yes": 
		case "ja": 
		case "an": 
		case "active": 
		case "aktiv": 
			return true;
		}
		
		return false;
	}

	//-------------------------------------------------------------------
	private void switchFormat(PlayerCharacter performedBy, String value) {
		performedBy.sendShortText(Priority.IMMEDIATE, value+" Not implemented yet\r\n");
		
	}

	//-------------------------------------------------------------------
	private void health(PlayerCharacter performedBy, String value) {
		boolean state = asBoolean(value);
		performedBy.setConfiguration(ConfigOption.HEALTH_EVERY_TICK, state);
		performedBy.sendShortText(Priority.IMMEDIATE, 
				state?getString("health_every_tick_enabled", performedBy.getLocale())
						:getString("health_every_tick_disabled", performedBy.getLocale())
					);
		
	}

	//-------------------------------------------------------------------
	private void imageProtocolSupport(PlayerCharacter performedBy, ConfigOption option, String value) {
		ImageProtocol proto = ImageProtocol.valueOf(value.toUpperCase());
		
		performedBy.setConfiguration(option, proto);
		List<MUDClientCapabilities.Graphic> supported = performedBy.getConnection().getCapabilities().graphicSupport;
		
		switch (proto) {
		case AUTO:
			performedBy.sendShortText(Priority.IMMEDIATE, getString("imageProtocolAuto", performedBy.getLocale()));
			performedBy.resetConfiguration(option);
			break;
		case KITTY:
			performedBy.sendShortText(Priority.IMMEDIATE, getString("imageProtocolForceKitty", performedBy.getLocale()));
			if (!supported.contains(Graphic.KITTY)) {
				performedBy.sendTextWithMarkup("<red>"+getString("imageProtocolKittyNotDetected", performedBy.getLocale())+"</red><reset>");
			}
			break;
		case ITERM:
			performedBy.sendShortText(Priority.IMMEDIATE, getString("imageProtocolForceIIP", performedBy.getLocale()));
			if (!supported.contains(Graphic.ITERM)) {
				performedBy.sendTextWithMarkup("<red>"+getString("imageProtocolIIPNotDetected", performedBy.getLocale())+"</red><reset>");
			}
			break;
		case SIXEL:
			performedBy.sendShortText(Priority.IMMEDIATE, getString("imageProtocolForceSixel", performedBy.getLocale()));
			if (!supported.contains(Graphic.SIXEL)) {
				performedBy.sendTextWithMarkup("<red>"+getString("imageProtocolSixelNotDetected", performedBy.getLocale())+"</red><reset>");
			}
			break;
		}
	}

}
