/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.player;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.prelle.simplepersist.Element;
import org.prelle.simplepersist.ElementConvert;

import com.graphicmud.MUD;
import com.graphicmud.commands.CommunicationChannel;
import com.graphicmud.game.MobileEntity;
import com.graphicmud.game.Vital;
import com.graphicmud.game.Vital.VitalType;
import com.graphicmud.io.ConfigurationConverter;
import com.graphicmud.network.ClientConnection;
import com.graphicmud.network.ClientConnection.Priority;

/**
 * Player.java
 *
 * @author Stefan Prelle
 * @version $Id: Player.java,v 1.6 2004/07/19 22:44:21 prelle Exp $
 */

public class PlayerCharacter extends MobileEntity {
	
	private final static Logger logger = System.getLogger(PlayerCharacter.class.getPackageName());

	/** Identifier */
	private String accountId;
	private String name;
	
	@Element
	@ElementConvert(ConfigurationConverter.class)
	private Map<ConfigOption, String> configuration = new HashMap<>();

	private transient ClientConnection con;

	//-------------------------------------------------------------------
	public PlayerCharacter() {
		super(null);
	}

	//-------------------------------------------------------------------
	PlayerCharacter(String accountId, String name) {
		super(null);
		this.accountId = accountId;
		this.name = name;
		setPosition( MUD.getInstance().getWorldCenter().createPosition() );
		MUD.getInstance().getGame().addPlayer(this);
	}

    //------------------------------------------------
	public String toString() { return "player:"+name; }

    //------------------------------------------------
	public ClientConnection getConnection() { return con; }
	public PlayerCharacter setConnection(ClientConnection value) { this.con=value; return this; }

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void sendShortText(Priority prio, String text) {
		if (getConnection() != null) {
			getConnection().sendShortText(prio,text);
		}
	}

	@Override
	public void sendTextWithMarkup(String text) {
		if (getConnection() != null) {
			getConnection().sendTextWithMarkup(text);
		}
	}

	@Override
	public Locale getLocale() {
		return getConnection().getLocale();
	}

	@Override
	public void sendOnChannel(CommunicationChannel channel, String text) {
		getConnection().sendOnChannel(channel, text);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.game.ReactsOnTime#tick()
	 */
	@Override
	public void tick() {
		super.tick();
		Map<VitalType,Vital> vitals = getVitals();
		if (getConfigurationAsBoolean(ConfigOption.HEALTH_EVERY_TICK) || vitals.get(VitalType.VITAL1).damage>0 || vitals.get(VitalType.VITAL2).damage>0) {
			con.sendPromptWithStats(getVitals());			
		}
	}

	//-------------------------------------------------------------------
	public void setConfiguration(ConfigOption key, Object value) {
		configuration.put(key, String.valueOf(value));
	}

	//-------------------------------------------------------------------
	public void resetConfiguration(ConfigOption key) {
		configuration.remove(key);
	}

	//-------------------------------------------------------------------
	public Object getConfiguration(ConfigOption key) {
		if (!configuration.containsKey(key))
			return key.getDefaultValue();
		
		String value = configuration.get(key);
		switch (key.getType()) {
		case BOOLEAN:
			return Boolean.parseBoolean(value);
		case INTEGER:
			return Integer.parseInt(value);
		case STRING:
			return value;
		case ENUM:
			for (Enum e : key.enumType.getEnumConstants()) {
				if (e.name().startsWith(value.toUpperCase()))
					return e;
			}
			logger.log(Level.ERROR, "Error converting ''{0}'' to a value of enum {1}", getConfiguration(key), key.enumType);
			return null;
		}
		throw new IllegalArgumentException("Type not support: "+key.getType());
	}

	//-------------------------------------------------------------------
	public boolean getConfigurationAsBoolean(ConfigOption key) {
		return (boolean) getConfiguration(key);
	}

	//-------------------------------------------------------------------
	public <E extends Enum> E getConfigurationAsEnum(ConfigOption key) {
		return (E) getConfiguration(key);
	}

	@Override
	public String getDescription() {
		return "A very handsome being";
	}
} // Player
