/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.network;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.graphicmud.Localization;

/**
 *
 */
public class MUDClientCapabilities {

	public static enum Color {
		COLOR_16,
		COLOR_256,
		COLOR_16M
	}

	public static enum Layout {
		NAWS,
		CURSOR_POSITIONING,
		TOP_BOTTOM_MARGIN,
		LEFT_RIGHT_MARGIN,
		RECTANGULAR_EDITING
	}


	public static enum Graphic {
		/** Dynamically Redefinable Character Set */
		DRCS,
		/** DEC Sixel */
		SIXEL,
		/** Kitty Inline PNG protocol */
		KITTY,
		ITERM,
		GMCP_BEIP,
		MXP_IMAGE
	}

	public static enum Audio {
		/** MUD Sound Protocol */
		MSP,
		GMCP_CLIENT_MEDIA
	}

	public static enum Control {
		ECHO,
		LINEMODE
	}

	public Charset charset = StandardCharsets.US_ASCII;
	public boolean supportsUnicode;
	public boolean supportsScreenreader;
	public boolean supportsMouse;
	public boolean supportsLinks;
	public String terminalType = "Not set";
	public String emulation    = "Not set";
	public String clientName = "Not set";
	public String clientVersion = "Not set";

	public List<Color>   colorModes     = new ArrayList<Color>();
	public List<Layout>  layoutFeatures = new ArrayList<Layout>();
	public List<Graphic> graphicSupport = new ArrayList<Graphic>();
	public List<Audio>   audioSupport   = new ArrayList<Audio>();
	public List<Control> controlSupport = new ArrayList<Control>();

	public int[] terminalSize;
	public int[] cellSize;
	public Map<String, Integer> gmcpPackages = new HashMap<>();

	//-------------------------------------------------------------------
	/**
	 */
	public MUDClientCapabilities() {
		// TODO Auto-generated constructor stub
	}

	private static String convert(boolean value) {
		if (value)
			return "<green>X</green>";
		else
			return "<red>-</red>";
	}

	//-------------------------------------------------------------------
	public String toString(Locale loc, int width) {
		StringBuffer buf = new StringBuffer();

		buf.append(Localization.getString("capabilities.intro", loc)+"<br/><br/>");
		buf.append(String.format("<b>%20s</b>: %s<br/>", Localization.getString("capabilities.client.name", loc), clientName));
		buf.append(String.format("<b>%20s</b>: %s<br/>", Localization.getString("capabilities.client.version", loc), clientVersion));
		buf.append("<br/>");

		buf.append(String.format("<u>%s</u><br/>", Localization.getString("capabilities.terminal", loc)));
		buf.append(String.format("<b>%20s</b>: %s<br/>", Localization.getString("capabilities.terminal.type", loc), terminalType));
		buf.append(String.format("<b>%20s</b>: %s<br/>", Localization.getString("capabilities.terminal.emulates", loc), emulation));
		String termSize = (terminalSize!=null)?(terminalSize[0]+"x"+terminalSize[1]):"?";
		String termCellSize = (cellSize!=null)?(cellSize[0]+"x"+cellSize[1]):"?";
		buf.append(String.format("<reset><b>%20s</b>: [%s]<br/>", Localization.getString("capabilities.terminal.charset", loc), charset.displayName(loc)));
		buf.append(String.format("<reset><b>%20s</b>: [%s]<br/>", Localization.getString("capabilities.terminal.unicode", loc), convert(supportsUnicode)));
		buf.append(String.format("<reset><b>%20s</b>: [%s]<br/>", Localization.getString("capabilities.terminal.mouse", loc), convert(supportsMouse)));
		buf.append(String.format("<reset><b>%20s</b>: [%s]<br/>", Localization.getString("capabilities.terminal.links", loc), convert(supportsLinks)));
		buf.append("<br/>");

		buf.append(String.format("<u>%s</u><br/>", Localization.getString("capabilities.color", loc)));
		buf.append(String.format("<reset><b>%20s</b>: [%s]<br/>", Localization.getString("capabilities.color.c16", loc), convert(colorModes.contains(Color.COLOR_16))));
		buf.append(String.format("<reset><b>%20s</b>: [%s]<br/>", Localization.getString("capabilities.color.c256", loc), convert(colorModes.contains(Color.COLOR_256))));
		buf.append(String.format("<reset><b>%20s</b>: [%s]<br/>", Localization.getString("capabilities.color.c16M", loc), convert(colorModes.contains(Color.COLOR_16M))));
		buf.append("<br/>");

		buf.append(String.format("<u>%s</u><br/>", Localization.getString("capabilities.layout", loc)));
		buf.append(String.format("<reset><b>%20s</b>: %s<br/>", Localization.getString("capabilities.layout.size", loc), termSize));
		buf.append(String.format("<reset><b>%20s</b>: [%s]<br/>", Localization.getString("capabilities.layout.cursor", loc), convert(layoutFeatures.contains(Layout.CURSOR_POSITIONING))));
		buf.append(String.format("<reset><b>%20s</b>: [%s]<br/>", Localization.getString("capabilities.layout.leftright", loc), convert(layoutFeatures.contains(Layout.LEFT_RIGHT_MARGIN))));
		buf.append(String.format("<reset><b>%20s</b>: [%s]<br/>", Localization.getString("capabilities.layout.topbottom", loc), convert(layoutFeatures.contains(Layout.TOP_BOTTOM_MARGIN))));
		buf.append("<br/>");

		buf.append(String.format("<u>%s</u><br/>", Localization.getString("capabilities.graphic", loc)));
		buf.append(String.format("<reset><b>%20s</b>: %s<br/>", Localization.getString("capabilities.graphic.cellsize", loc), termCellSize));
		buf.append(String.format("%s<br/>", Localization.getString("capabilities.graphic.mess", loc)));
		buf.append(String.format("<reset><b>%20s</b>: [%s]<br/>", Localization.getString("capabilities.graphic.kitty", loc), convert(graphicSupport.contains(Graphic.KITTY))));
		buf.append(String.format("<reset><b>%20s</b>: [%s]<br/>", Localization.getString("capabilities.graphic.iterm", loc), convert(graphicSupport.contains(Graphic.ITERM))));
		buf.append(String.format("<reset><b>%20s</b>: [%s]<br/>", Localization.getString("capabilities.graphic.sixel", loc), convert(graphicSupport.contains(Graphic.SIXEL))));
		buf.append(String.format("<reset><b>%20s</b>: [%s]<br/>", Localization.getString("capabilities.graphic.beipmap", loc), convert(graphicSupport.contains(Graphic.GMCP_BEIP))));
		buf.append(String.format("<reset><b>%20s</b>: [%s]<br/>", Localization.getString("capabilities.graphic.drcs", loc), convert(graphicSupport.contains(Graphic.DRCS))));
		buf.append("<br/>");

		buf.append(String.format("<u>%s</u><br/>", Localization.getString("capabilities.audio", loc)));
		buf.append(String.format("<reset><b>%20s</b>: [%s]<br/>", Localization.getString("capabilities.audio.gmcp", loc), convert(audioSupport.contains(Audio.GMCP_CLIENT_MEDIA))));
		buf.append(String.format("<reset><b>%20s</b>: [%s]<br/>", Localization.getString("capabilities.audio.msp", loc), convert(audioSupport.contains(Audio.MSP))));

		return buf.toString();
	}

}
