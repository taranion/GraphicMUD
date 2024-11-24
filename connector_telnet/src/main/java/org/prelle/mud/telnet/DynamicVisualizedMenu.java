package org.prelle.mud.telnet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.prelle.ansi.ANSIOutputStream;
import org.prelle.ansi.commands.CursorPosition;
import org.prelle.mud.telnet.impl.TelnetClientConnection;
import org.prelle.mudansi.FormatUtil;
import org.prelle.mudansi.MarkupElement;
import org.prelle.mudansi.MarkupParser;
import org.prelle.mudansi.UIGridFormat;
import org.prelle.mudansi.UIGridFormat.Area;

import com.graphicmud.dialog.ChoiceNode;
import com.graphicmud.network.ConnectionVariables;
import com.graphicmud.network.MUDClientCapabilities.Layout;
import com.graphicmud.network.interaction.Menu;
import com.graphicmud.network.interaction.MenuItem;
import com.graphicmud.network.interaction.VisualizedMenu;

/**
 * 
 */
public class DynamicVisualizedMenu implements VisualizedMenu {
	
	private TelnetClientConnection con;
	private UIGridFormat dialogFormat;
	
	private Map<String,MenuItem> inputs = new HashMap<>();

	//-------------------------------------------------------------------
	/**
	 * @throws IOException 
	 */
	public DynamicVisualizedMenu(TelnetClientConnection con) throws IOException {
		this.con = con;
		dialogFormat = new UIGridFormat(con.getOutputStream(), 
				con.getNumericVariable(ConnectionVariables.VAR_COLUMNS, 80), 
				con.getNumericVariable(ConnectionVariables.VAR_ROWS, 40), 
				con.getCapabilities().layoutFeatures.contains(Layout.RECTANGULAR_EDITING));
		dialogFormat.setLeftWidth(24);
		dialogFormat.setTopHeight(12);
		dialogFormat.join(UIGridFormat.ID_SCROLL, Area.TOP, Area.CENTER);
		dialogFormat.setOuterBorder(true);
		dialogFormat.recreate(StandardCharsets.UTF_8);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.interaction.VisualizedMenu#updateImage(byte[], java.lang.String, int, int)
	 */
	@Override
	public void updateImage(byte[] data, String filename, int width, int height) {
		// TODO Auto-generated method stub

	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.interaction.VisualizedMenu#writeScrollArea(java.lang.String, boolean)
	 */
	@Override
	public void writeScrollArea(String markup, boolean decorated) {
		// TODO Auto-generated method stub

	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.interaction.VisualizedMenu#updateChoices(com.graphicmud.network.interaction.Menu, java.util.List)
	 */
	@Override
	public void updateChoices(Menu menu, List<MenuItem<?>> choices) throws IOException {
		ANSIOutputStream out = con.getOutputStream();
		int y = 15;
		int i=0;
		int count=1;
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

			i++;
			int lineNr = 0;
			for (String line : makeMultiLineLabel(item.getLabel(), 20)) {
				out.write(new CursorPosition(2, y));
				out.write( (lineNr==0)?("\u001b[92m"+i+"\u001b[0m) "+line):"  "+line);
				lineNr++;
				y++;
			}
		}
		// TODO Auto-generated method stub

	}

	//-------------------------------------------------------------------
	private static List<String> makeMultiLineLabel(String text, int width) {
		List<MarkupElement> markup = MarkupParser.convertText(text);
		return FormatUtil.convertText(markup, width);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.interaction.VisualizedMenu#close()
	 */
	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.interaction.VisualizedMenu#getMenuItemForInput(java.lang.String)
	 */
	@Override
	public MenuItem getMenuItemForInput(String input) {
		// TODO Auto-generated method stub
		return null;
	}

}
