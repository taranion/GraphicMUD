/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.symbol;

import java.util.ArrayList;
import java.util.List;

import org.prelle.simplepersist.Attribute;
import org.prelle.simplepersist.Element;
import org.prelle.simplepersist.ElementList;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Symbol.java
 *
 * <BR>Created: Thu Oct 29 11:45:25 1998
 *
 * @author Stefan Prelle  <stefan@prelle.org>
 */

public class Symbol {

	@Getter
	@Setter
	@EqualsAndHashCode
	public static class TextColor {
		@Attribute(name="ansi")
		private Integer ansi;
		@Attribute(name="c256")
		private Integer col256;
		@Attribute(name="rgb")
		private String rgb;
		public Integer getCol256() { return (col256==null)?ansi:col256; }
	}

	@Getter
	@Setter
	public static class Grapheme {
		@Attribute
		private char ascii = '?';
		@Attribute
		private Character cp437;
		@Attribute
		private String utf8;
	}

//	public static enum SymbolLevel {
//		/** CP437 and 16 color (fg and bg) */
//		ANSI,
//		/** CP437 and 256 color (fg and bg) */
//		VT100,
//		/** Unicode and true color (fg and bg) */
//		MODERN,
//		/** SIXEL and 256 color */
//		SIXEL,
//		/** PNG of 32x32 Pixel */
//		IMG,
//	}
//
//	public static class SimpleSymbol {
//		@Attribute(name="fg")
//		private int foregroundColor;
//		@Attribute(name="bg")
//		private int backgroundColor;
//		@Attribute(name="char")
//		private char c = ' ';
//		//-------------------------------------------------------------------
//		public int getForeground() { return foregroundColor;}
//		public void setForegroundColor(int data) {this.foregroundColor = data;}
//		//-------------------------------------------------------------------
//		public int getBackgroundColor() { return backgroundColor; }
//		public void setBackgroundColor(int data) { this.backgroundColor = data; }
//		//-------------------------------------------------------------------
//		public char getCharacter() { return c; }
//		public void setCharacter(char data) { this.c = data; }
//	}
//	public static class ModernSymbol {
//		@Attribute(name="fg")
//		private String foregroundColor;
//		@Attribute(name="bg")
//		private String backgroundColor;
//		@Attribute(name="char")
//		private String c;
//		//-------------------------------------------------------------------
//		public String getForeground() { return foregroundColor;}
//		public void setForegroundColor(String data) {this.foregroundColor = data;}
//		//-------------------------------------------------------------------
//		public String getBackgroundColor() { return backgroundColor; }
//		public void setBackgroundColor(String data) { this.backgroundColor = data; }
//		//-------------------------------------------------------------------
//		public String getCharacter() { return c; }
//		public void setCharacter(String data) { this.c = data; }
//	}
	public static class SixelSymbol {
		/** Instead of defining a new char (limited to 32), use other glyph */
		@Attribute
		private Integer useInstead;
		private transient byte[] raw;
	}
	public static class GraphicSymbol {
		@Attribute
		private Integer frames = null;
		private transient byte[][] raw;
		private transient Object uiInternal;
		//-------------------------------------------------------------------
		public int getFrames() { return (frames==null)?1:frames;}
		public void setFrames(int value) { this.frames = value; if (value==1) this.frames=null;}
		//-------------------------------------------------------------------
		public byte[][] getBytes() { return raw;}
		public void setBytes(byte[][] value) { this.raw = value;}
		//-------------------------------------------------------------------
		public Object getUiInternal() { return uiInternal;}
		public void setUiInternal(Object uiInternal) { this.uiInternal = uiInternal;}
	}


	@Attribute
	private int id;
	@Attribute
	private String title;
	@ElementList(entry="flag",type = SymbolFlags.class, inline=true)
	private List<SymbolFlags> flags = new ArrayList<SymbolFlags>();
	@Element
	private TextColor fore = new TextColor();
	@Element
	private TextColor back = new TextColor();
	@Element
	private Grapheme text = new Grapheme();
//	@Element
//	private SimpleSymbol ansi;
//	@Element
//	private SimpleSymbol vt100;
//	@Element
//	private ModernSymbol modern;
	@Element
	private SixelSymbol sixel;
	@Element
	private GraphicSymbol image;

	public int getId() { return id; }
	public Symbol setTitle(String value) { this.title = value; return this; }
	public String getTitle() { return title; }
	public boolean hasFlag(SymbolFlags value) { return flags.contains(value); }

	//-------------------------------------------------------------------
	public Symbol() {
	}

	//-------------------------------------------------------------------
	public Symbol(int id) {
		this.id = id;
	}

	//-------------------------------------------------------------------
	public Symbol(int id, String title) {
		this(id);
		this.title = title;
	}
//	//-------------------------------------------------------------------
//	public SimpleSymbol getAnsi() {
//		return ansi;
//	}
//	//-------------------------------------------------------------------
//	public Symbol setAnsi(SimpleSymbol ansi) {
//		this.ansi = ansi;
//		return this;
//	}
//	//-------------------------------------------------------------------
//	/**
//	 * @return the vt100
//	 */
//	public SimpleSymbol getVt100() {
//		return vt100;
//	}
//	//-------------------------------------------------------------------
//	/**
//	 * @param vt100 the vt100 to set
//	 */
//	public Symbol setVt100(SimpleSymbol vt100) {
//		this.vt100 = vt100;
//		return this;
//	}
//	//-------------------------------------------------------------------
//	/**
//	 * @return the modern
//	 */
//	public ModernSymbol getModern() {
//		return modern;
//	}
//	//-------------------------------------------------------------------
//	/**
//	 * @param modern the modern to set
//	 */
//	public Symbol setModern(ModernSymbol modern) {
//		this.modern = modern;
//		return this;
//	}
	//-------------------------------------------------------------------
	/**
	 * @return the sixel
	 */
	public SixelSymbol getSixel() {
		return sixel;
	}
	//-------------------------------------------------------------------
	/**
	 * @param sixel the sixel to set
	 */
	public Symbol setSixel(SixelSymbol sixel) {
		this.sixel = sixel;
		return this;
	}
	//-------------------------------------------------------------------
	/**
	 * @return the image
	 */
	public GraphicSymbol getImage() {
		return image;
	}
	//-------------------------------------------------------------------
	/**
	 * @param image the image to set
	 */
	public Symbol setImage(GraphicSymbol image) {
		this.image = image;
		return this;
	}
	//-------------------------------------------------------------------
	/**
	 * @return the fore
	 */
	public TextColor getFore() {
		return fore;
	}
	//-------------------------------------------------------------------
	/**
	 * @param fore the fore to set
	 */
	public void setFore(TextColor fore) {
		this.fore = fore;
	}
	//-------------------------------------------------------------------
	/**
	 * @return the back
	 */
	public TextColor getBack() {
		return back;
	}
	//-------------------------------------------------------------------
	/**
	 * @param back the back to set
	 */
	public void setBack(TextColor back) {
		this.back = back;
	}
	//-------------------------------------------------------------------
	/**
	 * @return the text
	 */
	public Grapheme getText() {
		return text;
	}
	//-------------------------------------------------------------------
	/**
	 * @param text the text to set
	 */
	public void setText(Grapheme text) {
		this.text = text;
	}

	//-------------------------------------------------------------------
	public Integer getANSIForeground() { return fore.getAnsi(); }
	public Integer getANSIBackground() { return back.getAnsi(); }
	public Integer getC256Foreground() { return fore.getCol256(); }
	public Integer getC256Background() { return back.getCol256(); }
	public String getTrueForeground() { return fore.getRgb(); }
	public String getTrueBackground() { return back.getRgb(); }

}
