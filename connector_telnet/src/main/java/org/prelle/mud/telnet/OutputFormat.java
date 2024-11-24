package org.prelle.mud.telnet;

import java.io.IOException;
import java.lang.System.Logger;

import org.prelle.ansi.ANSIOutputStream;
import org.prelle.mud.telnet.impl.TelnetClientConnection;

import com.graphicmud.dialog.DialogueTree;
import com.graphicmud.map.ViewportMap;
import com.graphicmud.network.interaction.Table;
import com.graphicmud.symbol.Symbol;
import com.graphicmud.world.Surrounding;

/**
 *
 */
public interface OutputFormat {

	//-------------------------------------------------------------------
	public void configureSize(int width, int height, Logger logger);

	//-------------------------------------------------------------------
	/**
	 * Called when the output format is switched to. Prepares the terminal
	 * screen
	 */
	public void initialize(TelnetClientConnection parent, ANSIOutputStream con) throws IOException;

	//-------------------------------------------------------------------
	public void sendMapOnly(ViewportMap<Symbol> data) throws IOException;

	//-------------------------------------------------------------------
	public void sendRoom(Surrounding room) throws IOException;
	
	//-------------------------------------------------------------------
	public <E> void sendTable(Table<E> table) throws IOException;
	
	//-------------------------------------------------------------------
	public <E> void sendDialog(DialogueTree tree, String image) throws IOException;
	
}
