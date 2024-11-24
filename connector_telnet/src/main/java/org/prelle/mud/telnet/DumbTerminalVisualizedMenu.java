package org.prelle.mud.telnet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.prelle.mud.telnet.impl.TelnetClientConnection;
import org.prelle.mudansi.FormatUtil;
import org.prelle.mudansi.MarkupElement;
import org.prelle.mudansi.MarkupParser;

import com.graphicmud.network.ConnectionVariables;
import com.graphicmud.network.interaction.ActionMenuItem;
import com.graphicmud.network.interaction.Menu;
import com.graphicmud.network.interaction.MenuItem;
import com.graphicmud.network.interaction.ToggleMenuItem;
import com.graphicmud.network.interaction.VisualizedMenu;

/**
 * 
 */
public class DumbTerminalVisualizedMenu implements VisualizedMenu {
	
	private TelnetClientConnection con;
	
	private Map<String,MenuItem> inputs = new HashMap<>();

	//-------------------------------------------------------------------
	public DumbTerminalVisualizedMenu(TelnetClientConnection con) {
		this.con = con;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.interaction.VisualizedMenu#updateImage(byte[], java.lang.String, int, int)
	 */
	@Override
	public void updateImage(byte[] data, String filename, int width, int height) {
		con.sendImage(data, filename, width, height);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.interaction.VisualizedMenu#writeScrollArea(java.lang.String, boolean)
	 */
	@Override
	public void writeScrollArea(String markup, boolean decorated) {
		// TODO Auto-generated method stub
		List<MarkupElement> cooked = MarkupParser.convertText(markup);
		System.err.println("DumpTerminalVisualizedMenu: "+cooked.size()+" elements");
		String block = FormatUtil.convertTextBlock(cooked, con.getNumericVariable(ConnectionVariables.VAR_COLUMNS, 80));
		try {
			con.getOutputStream().write(block);
			// TODO: Wait according to read
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.interaction.VisualizedMenu#updateChoices(java.util.List)
	 */
	@Override
	public void updateChoices(Menu menu, List<MenuItem<?>> choices) {
		int count=1;
		inputs.clear();
		StringBuffer mess = new StringBuffer();
		for (MenuItem item : choices) {
			if (item.getIdentifier()!=null)
				inputs.put(item.getIdentifier(), item);
			boolean selectable = true;
			if (item.getCheckIfSelectable()!=null) {
				selectable = item.getCheckIfSelectable().test(null);
			}
			String input = item.isExit()?"e":String.valueOf(count++);
			inputs.put(input, item);
			if (!selectable)
				mess.append("<strike>");
			String emoji = (item.getEmoji()!=null && con.getCharset()==StandardCharsets.UTF_8)?(item.getEmoji()+" "):"";
			if (item instanceof ToggleMenuItem) {
				Object selection = con.retrieveMenuItemData(menu, item);
				boolean selected = selection!=null;
				String format = String.format("(%d) %s[%s] %s\n", input, emoji, selected?"x":" ", item.getLabel());
				// The \n above already is mapped to <br/>
				mess.append(format);
			} else {
				mess.append("("+(input)+") "+emoji+item.getLabel()+"\n");
			}
			if (!selectable)
				mess.append("</strike>");
		}
		
		try {
			String converted = FormatUtil.convertTextBlock(MarkupParser.convertText(mess.toString()), 80);
			con.getOutputStream().write(converted+"\r\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.interaction.VisualizedMenu#close()
	 */
	@Override
	public void close() {
		con.sendPrompt("Done.\n");
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.interaction.VisualizedMenu#getMenuItemForInput(java.lang.String)
	 */
	@Override
	public MenuItem getMenuItemForInput(String input) {
		System.err.println("DumbTerminalVisualizedMenu: "+inputs);
		return inputs.get(input);
	}
}
