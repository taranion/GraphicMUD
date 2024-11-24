/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.player;

import java.util.Locale;

import com.graphicmud.Localization;

import lombok.Getter;

/**
 * 
 */
public enum ConfigOption {

	/** Change screen format */
    FORMAT("screenFormat", ConfigOptionType.STRING, "auto"),
	/** Override how to display images */
	IMAGE_PROTOCOL("image.proto", ImageProtocol.class, ImageProtocol.AUTO),
	/** Send vitals on every tick, even if unchanged */
	HEALTH_EVERY_TICK("healthEveryTick", ConfigOptionType.BOOLEAN, false)
	;
	@Getter
	String id;
	@Getter
	ConfigOptionType type;
	@Getter
	Class<? extends Enum> enumType;
	@Getter
	Object defaultValue;
	
	//-------------------------------------------------------------------
	ConfigOption(String id, ConfigOptionType type, Object def) {
		this.id = id;
		this.type = type;
		this.defaultValue = def;
	}
	//-------------------------------------------------------------------
	ConfigOption(String id, Class<? extends Enum> enumType, Object def) {
		this.id = id;
		this.type = ConfigOptionType.ENUM;
		this.enumType = enumType;
		this.defaultValue = def;
	}
	public String getName(Locale loc) {
		return Localization.getString("enum.configoption."+this.name().toLowerCase());
	}
	public String getExplanation(Locale loc) {
		return Localization.getString("enum.configoption."+this.name().toLowerCase()+".explain");
	}
	public static String[] values(Locale loc) {
		String[] translated = new String[ConfigOption.values().length];
		for (int i=0; i<translated.length; i++) {
			translated[i]=ConfigOption.values()[i].getName(loc);
		}
		return translated;
	}
    public static ConfigOption valueOf(Locale loc, String val) {
        for (ConfigOption dir : ConfigOption.values()) {
            if (dir.getName(loc).equalsIgnoreCase(val))
                return dir;
        }
        return null;
    }
    public static ConfigOption startingWith(Locale loc, String val) {
        for (ConfigOption dir : ConfigOption.values()) {
            if (dir.getName(loc).equalsIgnoreCase(val))
                return dir;
        }
        return null;
    }

	public static enum ConfigOptionType {
		BOOLEAN,
		INTEGER,
		STRING,
		ENUM
	};
}
