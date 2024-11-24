package com.graphicmud.telnet;

import java.nio.charset.Charset;

/**
 *
 */
public class TermCap {

	/** 3 Bit colors */
	public boolean color8;
	/** ANSI 4 Bit colors */
	public boolean color16;
	/** ANSI 256 colors */
	public boolean color256;
	/** True colors */
	public boolean colorTrue;
	/** Position cursor anywhere on screen */
	public boolean cursorPositioning;
	/** VT100 vertical scrolling */
	public boolean verticalScrolling;
	/** VT400 horizontal scrolling */
	public boolean horizontalScrolling;
	/** VT200 Sixel */
	public boolean sixelFont;
	/** VT200 Sixel */
	public boolean sixelGraphics;
	/** iTerm inline image protocol */
	public boolean iip;
	/** Kitty terminal graphics protocol : https://sw.kovidgoyal.net/kitty/graphics-protocol/ */
	public boolean kittyGraphics;
	/** Can use a rendered image as a graphic with a known character sitze */
	public boolean graphicMaps;

	public Charset charset;

}
