package com.graphicmud.telnet;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
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
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

import org.prelle.ansi.ANSIOutputStream;
import org.prelle.ansi.DeviceControlFragment;
import org.prelle.ansi.commands.CursorPosition;
import org.prelle.ansi.commands.DECSetMode;
import org.prelle.ansi.commands.DECSetMode.DECMode;
import org.prelle.ansi.commands.SelectGraphicRendition;
import org.prelle.ansi.commands.SelectGraphicRendition.Meaning;
import org.prelle.ansi.commands.iterm.SendITermImage;
import org.prelle.ansi.commands.kitty.KittyGraphicsFragment;
import org.prelle.ansi.commands.kitty.KittyImageTransmission;
import org.prelle.ansi.control.CursorControls;
import org.prelle.mudansi.FormatUtil;
import org.prelle.mudansi.MarkupElement;
import org.prelle.mudansi.MarkupParser;
import org.prelle.mudansi.UIGridFormat;
import org.prelle.mudansi.UIGridFormat.Area;
import org.prelle.mudansi.UIGridFormat.AreaDefinition;

import com.graphicmud.MUD;
import com.graphicmud.dialog.ChoiceNode;
import com.graphicmud.dialog.DialogueTree;
import com.graphicmud.map.ANSIMap;
import com.graphicmud.map.ANSIMapper;
import com.graphicmud.map.LineOfSight;
import com.graphicmud.map.SixelEncoder;
import com.graphicmud.map.ViewportMap;
import com.graphicmud.map.ANSIMapper.ColorMapping;
import com.graphicmud.map.ANSIMapper.GraphemeMapping;
import com.graphicmud.network.MUDClientCapabilities.Color;
import com.graphicmud.network.MUDClientCapabilities.Graphic;
import com.graphicmud.network.MUDClientCapabilities.Layout;
import com.graphicmud.network.interaction.Table;
import com.graphicmud.network.interaction.TableColumn;
import com.graphicmud.player.ConfigOption;
import com.graphicmud.player.ImageProtocol;
import com.graphicmud.symbol.Symbol;
import com.graphicmud.symbol.SymbolManager;
import com.graphicmud.symbol.TileGraphicService;
import com.graphicmud.telnet.impl.TelnetClientConnection;
import com.graphicmud.world.Surrounding;
import com.graphicmud.world.text.Direction;

/**
 * 
 */
public class DynamicOutputFormat implements OutputFormat {

	private Logger logger = System.getLogger(DynamicOutputFormat.class.getPackageName());

	protected final static char[] BORDER       = "┌─┬┐│├┼┤└┴┘".toCharArray();
	protected final static char[] BORDER_ASCII = "+-++|++++++".toCharArray();
	
	private TelnetClientConnection con;
	private ANSIOutputStream out;
	private UIGridFormat grid;
	private int columns;
	private int rows;
	private SixelEncoder encoder;
	private TileGraphicService tileService;
	private SymbolManager symbols;
	
	private boolean withASCIIMap;
	private boolean withUIMap;
	private boolean withRoomInfo;
	private boolean withInputBuffer;

	protected int mapWidth = 22;
	protected int mapHeight= 11;

	//-------------------------------------------------------------------
	public DynamicOutputFormat() {
		Optional<SixelEncoder> encoder = ServiceLoader.load(SixelEncoder.class).findFirst();
		if (encoder.isPresent())
			this.encoder = encoder.get();
		symbols = MUD.getInstance().getSymbolManager();
		tileService = symbols.getTileGraphicService();
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.telnet.OutputFormat#initialize(com.graphicmud.network.ClientConnection, org.prelle.ansi.ANSIOutputStream)
	 */
	@Override
	public void initialize(TelnetClientConnection con, ANSIOutputStream out) throws IOException {		
		this.con = con;
		this.out = out;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.telnet.OutputFormat#configureSize(int, int, java.lang.System.Logger)
	 */
	@Override
	public void configureSize(int width, int height, Logger logger) {
		logger.log(Level.DEBUG, "configureSize()");
		grid = new UIGridFormat(out, width, height, con.getCapabilities().layoutFeatures.contains(Layout.RECTANGULAR_EDITING));
		this.rows = height;
		this.columns = width;
		grid.setTopHeight(mapHeight);
		grid.setLeftWidth(mapWidth);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.telnet.OutputFormat#sendMapOnly(int[][])
	 */
	@Override
	public void sendMapOnly(ViewportMap<Symbol> mapData) throws IOException {
		logger.log(Level.DEBUG, "ENTER: sendMap");
		if (mapData==null) {
			return;
		}
		mapData
			.setPositionSelf(10, 5)
			.apply(LineOfSight::floodFill);
		CursorControls.savePositionDEC(out);
		try {
			if (tileService==null) {
				return;
			}
			CursorControls.setCursorPosition(out, grid.hasOuterBorder()?2:1, grid.hasOuterBorder()?2:1);
			ImageProtocol ipConfig = con.getCharacter().getConfigurationAsEnum(ConfigOption.IMAGE_PROTOCOL);
			if (con.getCapabilities().graphicSupport.contains(Graphic.ITERM) || ipConfig==ImageProtocol.ITERM) {
				logger.log(Level.INFO, "Send as iterm");
				mapData.reduceSize(5, 5);
				byte[] data = tileService.renderMap(mapData, symbols.getSymbolSet(4));
				SendITermImage iterm = new SendITermImage();
				iterm.setSize(data.length);
				iterm.setFileName("map.png");
				iterm.setWidth(mapWidth);
				iterm.setHeight(mapHeight);
				iterm.setImgData(data);
				try {
					logger.log(Level.DEBUG, "Send iTerm PNG APC");
					out.write(iterm);
				} catch (IOException e) {
					logger.log(Level.ERROR, "Failed sending image",e);
				}
				CursorControls.restorePositionDEC(out);
			} else if (con.getCapabilities().graphicSupport.contains(Graphic.KITTY) || ipConfig==ImageProtocol.KITTY) {
				mapData.reduceSize(5, 5);
				byte[] data = tileService.renderMap(mapData, symbols.getSymbolSet(4));
				boolean isFirst = true;
				for (Iterator<String> it = TelnetClientConnection.splitIntoKittyChunks(data).iterator(); it.hasNext(); ) {
					String chunk = it.next();
					KittyImageTransmission kitty = new KittyImageTransmission();
					kitty.setPayload(chunk);
					if (isFirst) {
						kitty.set('a',KittyGraphicsFragment.ACION_TRANSMIT_AND_DISPLAY);
						kitty.setFormat(KittyImageTransmission.FORMAT_PNG);
						kitty.set(KittyImageTransmission.KEY_ID, 33);
						kitty.setMedium(KittyImageTransmission.MEDIUM_DIRECT);
						kitty.set('c',mapWidth);
						kitty.set('r',mapHeight);
						isFirst=false;
					}
					kitty.setMoreChunksFollow(it.hasNext());
					logger.log(Level.DEBUG, "Send Kitty PNG APC");
					out.write(kitty);
				}
				CursorControls.restorePositionDEC(out);
			} else if ((con.getCapabilities().graphicSupport.contains(Graphic.SIXEL) || ipConfig==ImageProtocol.SIXEL) && encoder!=null) {
				logger.log(Level.DEBUG, "Found Sixel encoder");
				mapData.reduceSize(5, 5);
				byte[] data = tileService.renderMap(mapData, symbols.getSymbolSet(4));
				String encoded = encoder.toSixel(data);
				DeviceControlFragment dcs = new DeviceControlFragment("q", List.of(0,1,0), encoded);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				dcs.encode(baos, true);
				FileOutputStream dummy = new FileOutputStream("/tmp/map.six");
				dummy.write(baos.toByteArray());
				dummy.flush();
				dummy.close();
				out.write(new DeviceControlFragment("q", List.of(0,1,0), encoded));
				CursorControls.restorePositionDEC(out);
			} else {
				// ASCII map
				GraphemeMapping use = GraphemeMapping.ASCII;
				if (con.getCharset()==StandardCharsets.UTF_8) use=GraphemeMapping.UNICODE;
				if (con.getCharset()==StandardCharsets.ISO_8859_1) use=GraphemeMapping.CP437;
				ColorMapping color = ColorMapping.COL16;
				if (con.getCapabilities().colorModes.contains(Color.COLOR_256)) color = ColorMapping.COL256;
				if (con.getCapabilities().colorModes.contains(Color.COLOR_16M)) color = ColorMapping.COL16M;
				ANSIMap ansi = (ANSIMap) mapData.convert(new ANSIMapper(use, color));
				List<String> mapLines = ansi.getAsLines();
//				List<String> mapLines = ANSIArtMapConverter.convertMap(mapData, use, MUDClientCapabilities.Color.COLOR_16);
				CursorControls.restorePositionDEC(out);
				grid.showRawIn(Area.TOP_LEFT.name(), mapLines);
				
			}
		} finally {
			logger.log(Level.DEBUG, "LEAVE: sendMap");
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.telnet.OutputFormat#sendRoom(com.graphicmud.world.Surrounding)
	 */
	@Override
	public void sendRoom(Surrounding room) throws IOException {
		logger.log(Level.DEBUG, "ENTER: sendRoom(withASCIIMap={0})",withASCIIMap);
		try {
//			logger.log(Level.WARNING, "sendRoom()"+grid.dump());
			// Obtain the area to use
			AreaDefinition area = grid.getArea("RoomDesc");
			if (area==null)
				area = grid.getArea(Area.TOP.name());
			
			// Format text lines
			int descColumns = area.getW();
//			AreaControls.fillArea(out, 'x', 
//					area.getX(), 
//					area.getY(), 
//					descColumns, 
//					mapHeight);
			StringBuffer title = new StringBuffer(FormatUtil.convertTextBlock(room.getTitle(), descColumns));
			title.append(" ");
			if (room.getDirections()!=null) {
				StringBuffer toConvert = new StringBuffer("<cyan> ");
				Iterator<Direction> it = room.getDirections().iterator();
				while (it.hasNext()) {
					Direction dir = it.next();
					toConvert.append( dir.name().toUpperCase().charAt(0) );
					if (it.hasNext())
						toConvert.append(' ');
				}
				toConvert.append("</cyan>");
				title.append( FormatUtil.convertText(MarkupParser.convertText(toConvert.toString()),20));
			}
			List<String> textLines = new ArrayList<>();
			textLines.add(title.toString());
			logger.log(Level.INFO, "Desc = "+room.getDescription());
			textLines.addAll(FormatUtil.convertText(room.getDescription(), descColumns));
			// Show converted text
			grid.showRawIn(area, textLines);
			
			// TODO Auto-generated method stub
			if (withASCIIMap) {
				sendRoomWithMap(room);
			} else {
				sendRoomWithoutMap(room);
			}
		} catch (Exception e) {
			logger.log(Level.ERROR, "Failed sending room",e);
		} finally {
			logger.log(Level.DEBUG, "LEAVE: sendRoom()");
		}
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
		return FormatUtil.convertText(MarkupParser.convertText(output.toString()), 200);
	}

	//-------------------------------------------------------------------
	private void sendRoomWithMap(Surrounding room) throws IOException {
		StringBuffer output = new StringBuffer();

//		UseSymbol use = UseSymbol.ANSI;
//		if (con.getCharset()==StandardCharsets.UTF_8) use=UseSymbol.UNICODE;
//		if (con.getCharset()==StandardCharsets.ISO_8859_1) use=UseSymbol.CP437;

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
		if (con.getCharset()==StandardCharsets.UTF_8) use=GraphemeMapping.UNICODE;
		if (con.getCharset()==StandardCharsets.ISO_8859_1) use=GraphemeMapping.CP437;
		ColorMapping color = ColorMapping.COL16;
		if (con.getCapabilities().colorModes.contains(Color.COLOR_256)) color = ColorMapping.COL256;
		if (con.getCapabilities().colorModes.contains(Color.COLOR_16M)) color = ColorMapping.COL16M;
		ANSIMap ansi = (ANSIMap) room.getMap().convert(new ANSIMapper(use, color));
		List<String> mapLines = ansi.getAsLines();
//		List<String> mapLines = ANSIArtMapConverter.convertMap(room.getMap(), use, MUDClientCapabilities.Color.COLOR_16);
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
		out.write(output.toString().getBytes(con.getCharset()));
	}

	//-------------------------------------------------------------------
	private void sendRoomWithoutMap(Surrounding room) throws IOException {
//		out.write(new DeviceStatusReport(DeviceStatusReport.Type.CURSOR_POS));
		sendMapOnly(room.getMap());
//		out.write(new DeviceStatusReport(DeviceStatusReport.Type.CURSOR_POS));

		// Obtain the area to use
		AreaDefinition area = grid.getArea("RoomDesc");
		if (area==null)
			area = grid.getArea(Area.TOP.name());
		
		
		StringBuffer output = new StringBuffer();

		List<String> textLines = new ArrayList<>();

		StringBuffer title = new StringBuffer(FormatUtil.convertTextBlock(room.getTitle(), area.getW()-21));
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

		textLines.addAll(FormatUtil.convertText(room.getDescription(), area.getW()));
		textLines.addAll(getLifeformLines(room));


		String msg =  String.join("\r\n", textLines);
		output.append(msg);

		logger.log(Level.DEBUG, "Send\n"+ output);
		out.write("\r\n");
		out.write(output.toString().getBytes(con.getCharset()));
		out.write("\r\n");
	}

	//-------------------------------------------------------------------
	/**
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
		Charset charset = con.getCharset();
		char[] signs = (charset==StandardCharsets.UTF_8)?BORDER:BORDER_ASCII;
		StringBuilder top    = new StringBuilder();
		StringBuilder bottom = new StringBuilder();
		StringBuilder delim  = new StringBuilder();
		StringBuilder content= new StringBuilder();
		top    .append(signs[0]);
		content.append(signs[4]);
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
			content.append(" %-"+w+"s ");
			if (it.hasNext()) {
				top.append(signs[2]);
				delim.append(signs[6]);
				bottom.append(signs[9]);
				content.append(signs[4]);
			}			
		}
		top.append(signs[3]);
		delim.append(signs[7]);
		bottom.append(signs[10]);
		content  .append(signs[4]);
		String[] headArray = new String[headContent.size()];
		headArray = headContent.toArray(headArray);
		String head = String.format(content.toString(), headArray);		
				
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
		for (int l=0; l<table.getData().size(); l++) {
			List<String> lineData = new ArrayList<String>();
			it = table.getColumns().iterator();
			while (it.hasNext()) {
				lineData.add( renderedContentByColumn.get(it.next()).get(l));
			}
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
		logger.log(Level.INFO, "sendDialog "+image);
		out.write(new DECSetMode(DECMode.ALTERNATE_BUFFER_SAVE_CURSOR));
		
		UIGridFormat dialogFormat = new UIGridFormat(out, columns, rows, con.getCapabilities().layoutFeatures.contains(Layout.RECTANGULAR_EDITING));
		dialogFormat.setLeftWidth(24);
		dialogFormat.setTopHeight(12);
		dialogFormat.join(UIGridFormat.ID_SCROLL, Area.TOP, Area.CENTER);
		dialogFormat.setOuterBorder(true);
		dialogFormat.recreate(StandardCharsets.UTF_8);
		
		out.write(new CursorPosition(2, 2));
		byte[] data = Files.readAllBytes(Paths.get("/home/prelle/git/MUD2024/Example MUD/src/main/resources/static/world/01_Dragorea/03/Rattling.png"));
		con.sendImage(data, "Rattling.png", 20, 10);
		
		int y = 15;
		int i=0;
		for (ChoiceNode choice : tree.getChoices()) {
			i++;
			int lineNr = 0;
			for (String line : makeMultiLineLabel(choice.getOption(), 20)) {
				out.write(new CursorPosition(2, y));
				out.write( (lineNr==0)?("\u001b[92m"+i+"\u001b[0m) "+line):"  "+line);
				lineNr++;
				y++;
			}
		}
		
		
		out.write(new CursorPosition(dialogFormat.getLeftWidth()+4, 2));
//		out.write(new DECResetMode(DECMode.ALTERNATE_BUFFER_SAVE_CURSOR));
	}

	//-------------------------------------------------------------------
	private static List<String> makeMultiLineLabel(String text, int width) {
		List<MarkupElement> markup = MarkupParser.convertText(text);
		return FormatUtil.convertText(markup, width);
	}

	//-------------------------------------------------------------------
	public void setASCIIMap(boolean value) {
		this.withASCIIMap = value;
		try {
			grid.recreate(con.getCharset());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	//-------------------------------------------------------------------
	public void setUIMap(boolean value) {
		this.withUIMap = value;
		logger.log(Level.INFO, "Dump "+grid.dump());
		try {
			grid.recreate(con.getCharset());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	//-------------------------------------------------------------------
	public void setRoomInfo(boolean value) {
		grid.setTopHeight(11);
		logger.log(Level.INFO, "Dump "+grid.dump());
//		grid.setOuterBorder(true);
		try {
			grid.recreate(con.getCharset());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	//-------------------------------------------------------------------
	/**
	 * @param withInputBuffer the withInputBuffer to set
	 */
	public void setWithInputBuffer(boolean withInputBuffer) {
		this.withInputBuffer = withInputBuffer;
	}

	//-------------------------------------------------------------------
	/**
	 * @return the grid
	 */
	public UIGridFormat getGrid() {
		return grid;
	}

//	//-------------------------------------------------------------------
//	public void setMapCorner(boolean value) {
//		grid.setTopHeight(11);
//		logger.log(Level.INFO, "Dump "+grid.dump());
////		grid.setOuterBorder(true);
//		try {
//			grid.recreate();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}		
//	}

}
