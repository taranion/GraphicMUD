package org.prelle.mud.telnet;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.prelle.mud.telnet.impl.TelnetClientConnection;
import org.prelle.mudansi.FormatUtil;
import org.prelle.mudansi.MarkupParser;

import com.graphicmud.map.ANSIMap;
import com.graphicmud.map.ANSIMapper;
import com.graphicmud.map.ANSIMapper.ColorMapping;
import com.graphicmud.map.ANSIMapper.GraphemeMapping;
import com.graphicmud.network.MUDClientCapabilities.Color;
import com.graphicmud.world.Surrounding;

public class SimpleMapWithTextFormat extends ANSIOutputFormat {

	private final static Logger logger = System.getLogger(SimpleMapWithTextFormat.class.getPackageName());

	//-------------------------------------------------------------------
	public SimpleMapWithTextFormat() {
		// TODO Auto-generated constructor stub
	}

	//-------------------------------------------------------------------
	public void sendRoom(Surrounding room) throws IOException {
		logger.log(Level.INFO, "SendRoom for {0}x{1}  {2}", columns, rows, this.getClass().getSimpleName());
		StringBuffer output = new StringBuffer();


		int columnsNextToMap;
		if (room.getMap() == null) {
			columnsNextToMap = columns;
		} else {
			columnsNextToMap = columns - room.getMap().getWidth() - 1;
		}

        List<String> textLines = new ArrayList<>();

		StringBuffer title = new StringBuffer(FormatUtil.convertTextBlock(room.getTitle(), columnsNextToMap));
		title.append(" ");
		if (room.getDirections()!=null) {
			title.append("[");
			StringBuffer toConvert = new StringBuffer("<cyan>");
			String exits = room.getDirections().stream().map(d -> d.name().toUpperCase().substring(0, 1)).collect(Collectors.joining(" "));
			toConvert.append(exits);
			toConvert.append("</cyan>");
			title.append(FormatUtil.convertTextBlock(MarkupParser.convertText(toConvert.toString()), 20));
			title.append("]");
		}
        textLines.add(title.deleteCharAt(title.lastIndexOf(" ")).toString());

		GraphemeMapping use = GraphemeMapping.UNICODE;
        if (con instanceof TelnetClientConnection) {
			use = GraphemeMapping.ASCII;
			if (((TelnetClientConnection)con).getCharset()==StandardCharsets.UTF_8) use=GraphemeMapping.UNICODE;
			if (((TelnetClientConnection)con).getCharset()==StandardCharsets.ISO_8859_1) use=GraphemeMapping.CP437;
        }
		ColorMapping color = ColorMapping.COL16;
		if (con.getCapabilities().colorModes.contains(Color.COLOR_256)) color = ColorMapping.COL256;
		if (con.getCapabilities().colorModes.contains(Color.COLOR_16M)) color = ColorMapping.COL16M;
        logger.log(Level.ERROR, "-------------------"+use);
		ANSIMap ansi = (ANSIMap) room.getMap().convert(new ANSIMapper(use, color));
        List<String> mapLines = ansi.getAsLines();//ANSIArtMapConverter.convertMap(room.getMap(), use, MUDClientCapabilities.Color.COLOR_16);
        textLines.addAll(FormatUtil.convertText(room.getDescription(), columnsNextToMap));
        textLines.addAll(getLifeformLines(room));

        List<String> mergedLines = new ArrayList<>();
        if (room.getMap()!=null) {
            int mapWidth = room.getMap().getWidth();
            for (int i=0; i<Math.max(mapLines.size(), textLines.size()); i++) {
                StringBuffer line = new StringBuffer();
                if (i<mapLines.size()) {
                    line.append(mapLines.get(i));
                } else {
                    line.repeat(" ", mapWidth);
                }
                line.append("\u001b[0m");
                line.append(' ');
                if (i<textLines.size()) {
                    line.append(textLines.get(i));
                }
                mergedLines.add(line.toString());
            }
        } else
            mergedLines = textLines;
        String msg =  String.join("\r\n", mergedLines);
        output.append(msg);


        logger.log(Level.INFO, "Send\n"+ output);
		out.write("\r\n");
		out.write(output.toString().getBytes(charset));
		Files.write( Paths.get("/tmp/capture.ansi"),output.toString().getBytes(charset)) ;
	}
	
}
