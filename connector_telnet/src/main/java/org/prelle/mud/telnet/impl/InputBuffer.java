package org.prelle.mud.telnet.impl;

import java.nio.charset.Charset;

import org.prelle.ansi.AParsedElement;
import org.prelle.ansi.C0Code;
import org.prelle.ansi.C0Fragment;
import org.prelle.ansi.PrintableFragment;

/**
 *
 */
public class InputBuffer {

	private StringBuilder buf;

	//-------------------------------------------------------------------
	public InputBuffer() {
		buf = new StringBuilder();
	}

	//-------------------------------------------------------------------
	/**
	 * @param input
	 * @return TRUE, if the input was consumed, FALSE if the input wasn't relevant for the buffer.
	 */
	public boolean handle(AParsedElement input) {
		switch (input) {
		case PrintableFragment text: buf.append(text.getText()); return true;
		case C0Fragment c0 when c0.getCode()==C0Code.DEL:
			if (buf.length()==0) return true;
			buf.deleteCharAt(buf.length()-1); return true;
		default:
			break;
		}
		return false;
	}

	//-------------------------------------------------------------------
	public String getInput() {
		return buf.toString();
	}

	//-------------------------------------------------------------------
	public void append(String text) {
		buf.append(text);
	}

	//-------------------------------------------------------------------
	public void clear() {
		buf.delete(0, buf.length());
	}
}
