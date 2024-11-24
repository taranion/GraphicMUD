package com.graphicmud.telnet;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.prelle.ansi.ANSIOutputStream;
import org.prelle.ansi.commands.CursorPosition;
import org.prelle.ansi.commands.SelectGraphicRendition;
import org.prelle.ansi.commands.SelectGraphicRendition.Meaning;
import org.prelle.ansi.commands.SetLeftAndRightMargin;
import org.prelle.ansi.commands.SetTopAndBottomMargin;
import org.prelle.ansi.control.AreaControls;
import org.prelle.ansi.control.XTermOSControls;
import org.prelle.mudansi.FormatUtil;
import org.prelle.mudansi.MarkupElement;
import org.prelle.mudansi.MarkupParser;

import com.graphicmud.dialog.DialogueTree;
import com.graphicmud.map.ANSIMap;
import com.graphicmud.map.ANSIMapper;
import com.graphicmud.map.ViewportMap;
import com.graphicmud.map.ANSIMapper.ColorMapping;
import com.graphicmud.map.ANSIMapper.GraphemeMapping;
import com.graphicmud.network.ClientConnection;
import com.graphicmud.network.MUDClientCapabilities;
import com.graphicmud.network.MUDClientCapabilities.Color;
import com.graphicmud.network.interaction.Table;
import com.graphicmud.network.interaction.TableColumn;
import com.graphicmud.symbol.Symbol;
import com.graphicmud.telnet.impl.ANSIArtMapConverter;
import com.graphicmud.telnet.impl.TelnetClientConnection;
import com.graphicmud.telnet.impl.ANSIArtMapConverter.UseSymbol;
import com.graphicmud.world.Surrounding;
import com.graphicmud.world.text.Direction;

/**
 *
 */
public class ANSIOutputFormat implements OutputFormat {

	private Logger logger = System.getLogger(ANSIOutputFormat.class.getPackageName());
	public final static Logger LOGGER = System.getLogger(ANSIOutputFormat.class.getPackageName());

	protected final static char[] BORDER       = "┌─┬┐│├┼┤└┴┘".toCharArray();
	protected final static char[] BORDER_ASCII = "+-++|++++++".toCharArray();
	
	public final static String TESTDATA = "<b><u>The City Center</u></b><br/>\n"
			+ "The Main Street reaches Britains welcoming center and leaves you with a lot of possibilities.\n"
			+ "      To the south is the city gate, should you consider leaving. A <cyan>road</cyan> leads\n"
			+ "      to the west where you can smell the fires of a forge. To the east you can\n"
			+ "      see a large orchard and a hut. The Main Street continues north with a\n"
			+ "      bridge spanning a small lake.";

	public final static void main(String[] args) {
		List<MarkupElement> markUp = MarkupParser.convertText(TESTDATA);
		String toWrite =  String.join("\r\n", FormatUtil.convertText(markUp, 70));
		System.out.println(toWrite);
		try {
			FileWriter fout = new FileWriter("/tmp/foo.txt");
			fout.append(toWrite);
			fout.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	protected ClientConnection con;
	protected ANSIOutputStream out;
	protected Charset charset = StandardCharsets.UTF_8;
	protected int columns;
	protected int rows;
	protected Locale loc;

	//-------------------------------------------------------------------
	public ANSIOutputFormat() {
		loc = Locale.getDefault();
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.telnet.OutputFormat#configureSize(int, int, java.lang.System.Logger)
	 */
	@Override
	public void configureSize(int width, int height, Logger logger) {
		this.columns = Math.max(width,30);
		this.rows   = height;
		this.logger = logger;
		logger.log(Level.INFO, "screen size now {0}x{1}", width, height);
	}

	//-------------------------------------------------------------------
	/**
	 * Called when the output format is switched to. Prepares the terminal
	 * screen
	 */
	@Override
	public void initialize(TelnetClientConnection parent, ANSIOutputStream con) throws IOException {
		this.out = con;
		this.con = parent;
		logger.log(Level.DEBUG, "ENTER: initialize");
		XTermOSControls.setWindowTitle(out, "Lorakis");
		//DisplayControl.setLinefeedNewlineMode(out, ModeState.SET);
		AreaControls.clearScreen(out);
		logger.log(Level.DEBUG, "LEAVE: initialize");
	}

	//-------------------------------------------------------------------
	protected String makeHeaderLine(Surrounding room) {
		StringBuffer output = new StringBuffer();
		String title = FormatUtil.convertTextBlock(room.getTitle(), columns);
		if (title!=null) {
			output.append(title);
			output.append(" ");
		}

		if (room.getDirections()!=null) {
			StringBuffer toConvert = new StringBuffer("<cyan> ");
			for (Direction dir : room.getDirections()) {
				toConvert.append( dir.name().toUpperCase().charAt(0) );
				toConvert.append(' ');
			}
			toConvert.append("</cyan>");
			output.append( FormatUtil.convertText(MarkupParser.convertText(toConvert.toString()),20));
		}

		return output.toString();
	}

    //-------------------------------------------------------------------
    private static String toStartTag(String text) {
        return "<" + text + ">";
    }

    //-------------------------------------------------------------------
    private static String toEndTag(String text) {
        return "</" + text + ">";
    }

    //-------------------------------------------------------------------
    private String getTextLine(Surrounding.ColorLine line) {
        String colorName = line.getColorMeaning().toTagName();
        String result = toStartTag(colorName);
        result += line.getText();
        result += toEndTag(colorName);
        result += "</br>";
        return result;
    }

    //-------------------------------------------------------------------
	protected List<String> getLifeformLines(Surrounding room) {
		StringBuilder output = new StringBuilder();
        room.getItemLines().forEach(e -> output.append(getTextLine(e)));
        room.getOtherMobileCharacterLines().forEach(e -> output.append(getTextLine(e)));
        room.getPlayerCharacterLines().forEach(e -> output.append(getTextLine(e)));
		return FormatUtil.convertText(MarkupParser.convertText(output.toString()), columns);
	}

	//-------------------------------------------------------------------
	public void sendRoom(Surrounding room) throws IOException {
		logger.log(Level.INFO, "SendRoom for {0}x{1}  {2}", columns, rows, this.getClass());
		StringBuffer output = new StringBuffer();


		List<String> textLines = new ArrayList<>();

		StringBuffer title = new StringBuffer(FormatUtil.convertTextBlock(room.getTitle(), columns));
		title.append(" ");
		if (room.getDirections()!=null) {
			StringBuffer toConvert = new StringBuffer("<cyan> ");
			for (Direction dir : room.getDirections()) {
				toConvert.append( dir.name().toUpperCase().charAt(0) );
				toConvert.append(' ');
			}
			toConvert.append("</cyan>");
			title.append( FormatUtil.convertText(MarkupParser.convertText(toConvert.toString()),20));
		}
		textLines.add(title.toString());

		GraphemeMapping use = GraphemeMapping.ASCII;
		if (charset==StandardCharsets.UTF_8) use=GraphemeMapping.UNICODE;
		if (charset==StandardCharsets.ISO_8859_1) use=GraphemeMapping.CP437;
		ColorMapping color = ColorMapping.COL16;
		if (con.getCapabilities().colorModes.contains(Color.COLOR_256)) color = ColorMapping.COL256;
		if (con.getCapabilities().colorModes.contains(Color.COLOR_16M)) color = ColorMapping.COL16M;
		ANSIMap ansi = (ANSIMap) room.getMap().convert(new ANSIMapper(use, color));
		List<String> mapLines = ansi.getAsLines(); //ANSIArtMapConverter.convertMap(room.getMap(), use, MUDClientCapabilities.Color.COLOR_16);
		textLines.addAll(FormatUtil.convertText(room.getDescription(), columns));
		textLines.addAll(getLifeformLines(room));

		List<String> mergedLines = new ArrayList<>();
		int mapWidth = ansi.getWidth();
		for (int i=0; i<Math.max(mapLines.size(), textLines.size()); i++) {
			StringBuffer line = new StringBuffer();
			if (i<mapLines.size()) {
				line.append(mapLines.get(i));
			} else {
				line.repeat(" ", mapWidth);
			}
			line.append(' ');
			if (i<textLines.size()) {
				line.append(textLines.get(i));
			}
			mergedLines.add(line.toString());
		}

		String msg =  String.join("\r\n", mergedLines);
		output.append(msg);

		logger.log(Level.INFO, "Send\n"+ output);
		out.write("\r\n");
		out.write(output.toString().getBytes(charset));
	}

	
	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.telnet.OutputFormat#sendMapOnly(int[][])
	 */
	@Override
	public void sendMapOnly(ViewportMap<Symbol> data) throws IOException {
		GraphemeMapping use = GraphemeMapping.ASCII;
		if (charset==StandardCharsets.UTF_8) use=GraphemeMapping.UNICODE;
		if (charset==StandardCharsets.ISO_8859_1) use=GraphemeMapping.CP437;
		ColorMapping color = ColorMapping.COL16;
		if (con.getCapabilities().colorModes.contains(Color.COLOR_256)) color = ColorMapping.COL256;
		if (con.getCapabilities().colorModes.contains(Color.COLOR_16M)) color = ColorMapping.COL16M;

		ANSIMap ansi = (ANSIMap) data.convert(new ANSIMapper(use, color));
		List<String> mapLines = ansi.getAsLines();
//		List<String> mapLines = ANSIArtMapConverter.convertMap(data, use, MUDClientCapabilities.Color.COLOR_16);
		String msg =  String.join("\r\n", mapLines);
		out.write(msg.getBytes(charset));
	}

	//-------------------------------------------------------------------
	/**
	 * @throws IOException 
	 * @see com.graphicmud.telnet.OutputFormat#sendTable(com.graphicmud.network.interaction.Table)
	 */
	@Override
	public <E> void sendTable(Table<E> table) throws IOException {
		Map<TableColumn<E, ?>, List<String>> renderedContentByColumn = new LinkedHashMap<>();
		Map<TableColumn<E, ?>, Integer> columnWidth = new LinkedHashMap<>();
		for (TableColumn<E, ?> column : table.getColumns()) {
			List<String> columnContent = new ArrayList<String>();
			renderedContentByColumn.put(column, columnContent);
			// Render all column content
			for (E lineData : table.getData()) {
				Object cellData = column.getValueProvider().apply(table, lineData);
				String rendered = null;
				// If configured, use the cell renderer
				if (column.getRenderer()!=null) {
					rendered = column.getRenderer().apply(cellData);
				}
				// Ensure there is a rendered string
				if (rendered==null)
					rendered = String.valueOf(cellData);
				// Add as rendered column content
				columnContent.add(rendered);
			}
			
			// Now that all content is rendered, determine the maximum width
			// The ideal width is either the column name width or the longest cell width
			// - whatever is higher
			int width = Math.max(
					columnContent.stream().mapToInt(s->s.length()).max().orElse(0),
					column.getName().length()
					);
			// ... maybe limited by configured maximum width
			if (column.getMaxWidth()>0) {
				width = Math.min(width, column.getMaxWidth());
			}
			columnWidth.put(column, width);
		}
		
		// Now that we know all rendered data and column width, build the output
		
		char[] signs = (charset==StandardCharsets.UTF_8)?BORDER:BORDER_ASCII;
		StringBuilder top    = new StringBuilder();
		StringBuilder bottom = new StringBuilder();
		StringBuilder delim  = new StringBuilder();
		StringBuilder content= new StringBuilder();
		StringBuilder contentWoXMP= new StringBuilder();
		top    .append(signs[0]);
		content.append(signs[4]);
		contentWoXMP.append(signs[4]);
		delim  .append(signs[5]);
		bottom .append(signs[8]);
		List<String> headContent = new ArrayList<String>();
		Iterator<TableColumn<E,?>> it = table.getColumns().iterator();
		while (it.hasNext()) {
			TableColumn<E, ?> col = it.next();
			headContent.add(col.getName());
			int w = columnWidth.get(col);
			top.repeat(signs[1], w+2);
			bottom.repeat(signs[1], w+2);
			delim.repeat(signs[1], w+2);
			contentWoXMP.append(" %-"+w+"s ");
			content.append(' ');
			if (col.getMxpLinkProvider()!=null && con.getProtocolCapabilities().containsKey("MXP")) {
				content.append("\u001B[4z<send href=\"%s\">\u001B[7z");
			}
			content.append("%-"+w+"s");
			if (col.getMxpLinkProvider()!=null && con.getProtocolCapabilities().containsKey("MXP")) {
				content.append("\u001B[4z</send>\u001B[7z");
			}
			content.append(' ');
			if (it.hasNext()) {
				top.append(signs[2]);
				delim.append(signs[6]);
				bottom.append(signs[9]);
				content.append(signs[4]);
				contentWoXMP.append(signs[4]);
			}			
		}
		top.append(signs[3]);
		delim.append(signs[7]);
		bottom.append(signs[10]);
		content  .append(signs[4]);
		contentWoXMP.append(signs[4]);
		String[] headArray = new String[headContent.size()];
		headArray = headContent.toArray(headArray);
		String head = String.format(contentWoXMP.toString(), headArray);		
				
		StringBuilder buf = new StringBuilder();
		if (table.getTitle()!=null) {
			out.write(new SelectGraphicRendition(Meaning.BOLD_ON, Meaning.UNDERLINE_ON));
			out.write(table.getTitle());
			out.write(new SelectGraphicRendition(Meaning.RESET));
			out.write("\r\n");
		}
		buf.append(top+"\r\n");
		buf.append(head+"\r\n");
		buf.append(delim+"\r\n");
		System.err.println( "ANSIOutputFormat.sendTable "+con.getProtocolCapabilities());
		for (int l=0; l<table.getData().size(); l++) {
			List<String> lineData = new ArrayList<String>();
			it = table.getColumns().iterator();
			while (it.hasNext()) {
				TableColumn<E, ?> tc = it.next();
				String pre = "";
				String post= "";
				if (tc.getMxpLinkProvider()!=null && con.getProtocolCapabilities().containsKey("MXP")) {
					String cmd = tc.getMxpLinkProvider().apply(table.getData().get(l));
					lineData.add(cmd);
					//pre = "\u001B[4z<send href=\""+cmd+"\">\u001B[7z";
					//post= "\u001B[4z</send>\u001B[7z";
				}
				String toAdd = renderedContentByColumn.get(tc).get(l);
				toAdd = pre+toAdd+post;
				lineData.add( toAdd);
			}
			System.err.println( "ANSIOutputFormat.sendTable... "+content.toString());
			buf.append(String.format(content.toString()+"\r\n", lineData.toArray(new String[lineData.size()])));

		}
		buf.append(bottom+"\r\n");
		
		out.write(buf.toString());
		out.flush();
		
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.telnet.OutputFormat#sendDialog(com.graphicmud.dialog.DialogueTree, java.lang.String)
	 */
	@Override
	public <E> void sendDialog(DialogueTree tree, String image) throws IOException {
		logger.log(Level.ERROR, "TODO: sendDialog");
		out.write(new SetTopAndBottomMargin(1, rows));
		out.write(new SetLeftAndRightMargin(1, columns));
		AreaControls.clearScreen(out);

		int toDistribute = columns-3;
		int column1 = Math.max(25, toDistribute/3);
		int column2 = toDistribute-column1;
		toDistribute = rows-3;
		int rows1 = 14;
		int rows2 = toDistribute-rows1;
		StringBuffer buf = new StringBuffer();
		buf.append('┌');
		buf.repeat('─', column1);
		buf.append('┬');
		buf.repeat('─', column2);
		buf.append('┐');
		buf.append("\r\n");
		
		for (int i=0; i<rows1; i++) {
			buf.append('│');
			buf.repeat(' ', column1);
			buf.append('│');
			buf.repeat(' ', column2);
			buf.append('│');
			buf.append("\r\n");
		}
		buf.append('├');
		buf.repeat('─', column1);
		buf.append('┤');
		buf.repeat(' ', column2);
		buf.append('│');
		buf.append("\r\n");
		
		for (int i=0; i<rows2; i++) {
			buf.append('│');
			buf.repeat(' ', column1);
			buf.append('│');
			buf.repeat(' ', column2);
			buf.append('│');
			buf.append("\r\n");
		}
		buf.append('└');
		buf.repeat('─', column1);
		buf.append('┴');
		buf.repeat('─', column2);
		buf.append('┘');
		buf.append("\r\n");		
		out.write(buf.toString());
		
		out.write(new CursorPosition(2, 2));
		
		byte[] data = Files.readAllBytes(Paths.get("/home/prelle/git/MUD2024/Example MUD/src/main/resources/static/world/01_Dragorea/03/Rattling.png"));
		con.sendImage(data, "Rattling.png", 20, 20);
		
		out.write(new SetTopAndBottomMargin(2, rows-1));
		out.write(new SetLeftAndRightMargin(column1+3, columns-2));
		out.write(new CursorPosition(column1+3, 2));
	}
}
