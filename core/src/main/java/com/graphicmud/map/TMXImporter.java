/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.map;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Writer;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;

import org.prelle.simplepersist.Attribute;
import org.prelle.simplepersist.CData;
import org.prelle.simplepersist.Element;
import org.prelle.simplepersist.ElementList;
import org.prelle.simplepersist.Persister;
import org.prelle.simplepersist.Root;

import com.graphicmud.world.Location;
import com.graphicmud.world.World;
import com.graphicmud.world.Zone;
import com.graphicmud.world.tile.TileAreaComponent;

/**
 *
 */
public class TMXImporter {

	protected final static Logger logger = System.getLogger("mud.map");
//
//	public static void main(String[] args) throws Exception {
//		File tmxFile = new File("/home/prelle/git/MUD2024/Example MUD/src/main/resources/static/maps/Britain1.tmx");
//		Persister persister = new Persister();
//		persister.read(TMXMap.class, new FileInputStream(tmxFile));
//	}

	public static class Data {
		@Attribute private String encoding;
		@CData     private String data;
		@ElementList(entry = "chunk",type = Chunk.class, inline=true)   
		private List<Chunk> chunks;
	}

	public static class Chunk {
		@Attribute private int width;
		@Attribute private int height;
		@Attribute private int x;
		@Attribute private int y;
		@CData private String data;
	}

	public static class Layer {
		@Attribute private Integer id;
		@Attribute private String name;
		@Attribute private int width;
		@Attribute private int height;
		@Element   private Data data;
	}

	public static class Tileset {
		@Attribute(name="firstgid") private int firstGID;
		@Attribute private String source;
	}

	public static class ObjectProperty {
		@Attribute private String name;
		@Attribute private String type;
		@Attribute private String value;
	}

	public static class MapObject {
		@Attribute private Integer id;
		@Attribute private String name;
		@Attribute private Integer x;
		@Attribute private Integer y;
		@Attribute private Integer width;
		@Attribute private Integer height;
		@ElementList(entry = "property", type = ObjectProperty.class)
		private List<ObjectProperty> properties = new ArrayList<>();
	}

	public static class ObjectGroup {
		@Attribute private Integer id;
		@Attribute private String name;
		@ElementList(entry = "object", type = MapObject.class, inline=true)
		private List<MapObject> objects = new ArrayList<>();
	}

	@Root(name="map")
	public static class TMXMap {
		@Attribute private String version;
		@Attribute(name="tiledversion") private String tiledVersion;
		@Attribute private String orientation;
		@Attribute(name="renderorder") private String renderOrder;
		@Attribute private int    width;
		@Attribute private int    height;
		@Attribute(name="tilewidth") private int  tileWidth;
		@Attribute(name="tileheight") private int tileHeight;
		@Attribute private boolean infinite;
		@Attribute(name="nextlayerid") private int nextLayerId;
		@Attribute(name="nextobjectid") private int nextObjectId;

		@ElementList(entry = "tileset", type = Tileset.class, inline=true)
		private List<Tileset> tilesets = new ArrayList<>();
		@ElementList(entry = "layer", type = Layer.class, inline=true)
		private List<Layer> layers = new ArrayList<>();
		@ElementList(entry = "objectgroup", type = ObjectGroup.class, inline=true)
		private List<ObjectGroup> objectGroups = new ArrayList<>();
	}

	//-------------------------------------------------------------------
	public TMXImporter() {
		// TODO Auto-generated constructor stub
	}

	public static GridMap importMap(World data, Zone zone, Path mapFile, Writer errorWriter) throws IOException {
		Persister persister = new Persister();
		TMXMap imported = persister.read(TMXMap.class, new FileInputStream(mapFile.toFile()));

		int tileWidth = imported.tileWidth;
		int tileHeight = imported.tileHeight;
		for (ObjectGroup objGrp : imported.objectGroups) {
			logger.log(Level.DEBUG, "Checking objects of group {0} - {1}", objGrp.id, objGrp.name);
			for (MapObject obj : objGrp.objects) {
				int startX = obj.x / tileWidth;
				int startY = obj.y / tileHeight;
				int width  = obj.width/tileWidth;
				int height = obj.height/tileHeight;
				int endX   = startX +width;
				int endY   = startY +height;
				TileAreaComponent mapArea = new TileAreaComponent(startX, startY, width, height);

//				logger.log(Level.DEBUG, "Object {0} - {1}: from {2},{3} to {4},{5}",
//					obj.id, obj.name,
//					startX,startY,
//					endX, endY);

				// Make sure a 'room' property exists
				int roomNr = -1;
				Optional<ObjectProperty> prop = obj.properties.stream().filter(p-> "room".equals(p.name)).findFirst();
				if (prop.isEmpty()) {
					if (obj.name.indexOf(" ")>0) {
						String first = obj.name.substring(0,obj.name.indexOf(" "));
						try {
							roomNr = Integer.parseInt(first);
						} catch (NumberFormatException e) {}
					}
					if (roomNr==-1) {
						errorWriter.write(MessageFormat.format("In map {0}: object {1} - {2} misses the ''room'' property\t\n", mapFile,obj.id, obj.name));
						continue;
					}
				} else {
					// Convert room property into integer
					try {
						roomNr = Integer.parseInt(prop.get().value);
					} catch (NumberFormatException e) {
						errorWriter.write(MessageFormat.format("In map {0}: object {1} - {2} ''room'' property is not an integer\r\n", mapFile,obj.id, obj.name));
						continue;
					}
				}
				// Check if the room exists
				Location room = zone.getRoom(roomNr);
				if (room==null) {
					errorWriter.write(MessageFormat.format("In map {0}: object {1} - {2} wants to be mapped to unknown room {3}\r\n", mapFile,obj.id, obj.name, roomNr));
					continue;
				}

				logger.log(Level.DEBUG, "Connect room {0} - {1} with map object {2}", roomNr, room.getRoomComponent().isPresent()?room.getRoomComponent().get().getTitle():"?", obj.name);
				room.setTileAreaComponent(mapArea);
			}
		}

		InMemoryMap map = new InMemoryMap(imported.width, imported.height);
		logger.log(Level.DEBUG, "Created a {0}x{1} map", map.getWidth(), map.getHeight());
		for (Layer layer : imported.layers) {
			logger.log(Level.DEBUG, "Layer ''{0}'' has {1} data", layer.name, layer.data.encoding);
			GridMap.Layer gLayer = new GridMap.Layer(layer.id, layer.name);
			if (!layer.data.data.isEmpty()) {
				int[] buf = decode(layer.data.data, layer.data.encoding);
				map.addLayer(gLayer, buf);
			} else if (!layer.data.chunks.isEmpty()) {
				int[] buf = readChunks(imported.width, imported.height, layer.data.chunks, layer.data.encoding);
				map.addLayer(gLayer, buf);
			}

			// Check if the map already exists as binary
			String tmp = mapFile.getFileName().toString();
			if (tmp.lastIndexOf('.')>0)
				tmp = tmp.substring(0,tmp.lastIndexOf('.'));
			tmp+="-"+layer.id+".map";
			Path binaryMapFile = mapFile.getParent().resolve(tmp);
			if (!Files.exists(binaryMapFile)) {
				logger.log(Level.INFO, "Creating binary version of layer {1}: {0}", binaryMapFile, layer.id);
				FileChannel chan = FileChannel.open(binaryMapFile, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
				ByteBuffer buf = ByteBuffer.allocate((map.getRawLayerData(gLayer).length*4));
				IntBuffer iBuf = buf.asIntBuffer();
				iBuf.put(0, map.getRawLayerData(gLayer));
				chan.write(buf);
				chan.close();
			} else {
				logger.log(Level.DEBUG, "Binary map version does already exist {0}", binaryMapFile);
			}
		}

		return map;
	}

	//-------------------------------------------------------------------
	private static int[] decode(String data, String encoding) {
		if ("base64".equalsIgnoreCase(encoding)) {
			byte[] raw = Base64.getDecoder().decode(data);
			/*
			 * Tiled's TMX file start numbering symbols with 1, but we use 0 -
			 * Apply -1 to all
			 */
			ByteBuffer buf = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
			IntBuffer iBuf = buf.asIntBuffer();
			int[] ret = new int[raw.length/4];
			for (int i=0; i<raw.length/4; i++) {
				int symbolCode = iBuf.get(i);
				ret[i] = symbolCode-1;
			}
			return ret;
		} else if ("csv".equalsIgnoreCase(encoding)) {
			data = data.replace("\n", "");
			StringTokenizer tok = new StringTokenizer(data,",");
			int[] ret = new int[tok.countTokens()];
			int i=0;
			while (tok.hasMoreTokens()) {
				ret[i++] = Integer.parseInt(tok.nextToken());
			}
			return ret;
		} else
			throw new IllegalArgumentException("Unsupported encoding '"+encoding+"'");
	}

	//-------------------------------------------------------------------
	private static int[] readChunks(int totalWidth, int totalHeight, List<Chunk> chunks, String encoding) {
		logger.log(Level.DEBUG, "readChunks");
		int[] fullMap = new int[totalWidth*totalHeight];
		
		for (Chunk chunk : chunks) {
			if (chunk.data.isEmpty()) {
				logger.log(Level.ERROR, "Chunk {0},{1} has no data", chunk.x, chunk.y);
				continue;
			}
			int[] tmpRaw = decode(chunk.data, encoding);
			for (int y=0; y<chunk.height; y++) {
				// Make sure to ignore chunk lines not belonging to the map
				if ((chunk.y+y)>=totalHeight)
					break;
				
				for (int x=0; x<chunk.width; x++) {
					// Make sure to ignore chunk columns not belonging to the map
					if ((chunk.x +x)>=totalWidth)
						break;

					int pos = y*chunk.width+x;
					try {
						int symbolCode = tmpRaw[pos];
						int bigPos = (chunk.y+y)*totalWidth + (chunk.x+x);
						fullMap[bigPos] = symbolCode-1;
					} catch (IndexOutOfBoundsException e) {
						logger.log(Level.ERROR, "Getting {0} of {1}",pos, tmpRaw.length);
						logger.log(Level.INFO, "Would have ({0}+{1})*{2} + ({3}+{4}) ", chunk.y, y, totalWidth, chunk.x,x);
					}
				}
			}
		}
		return fullMap;
	}
	
	//-------------------------------------------------------------------
	public IntBuffer getLayerData(TMXMap imported, int layerNr) {
		for (Layer layer : imported.layers) {
			if (layer.id!=layerNr)
				continue;
			logger.log(Level.DEBUG, "Layer {0} has {2} data {1}", layer.name, layer.data.data, layer.data.encoding);
			if ("base64".equals(layer.data.encoding)) {
				byte[] raw = Base64.getDecoder().decode(layer.data.data);
				logger.log(Level.DEBUG, "Was "+raw.length+" bytes - expect "+(layer.height*layer.width));
				IntBuffer buf = IntBuffer.allocate(4*(layer.height*layer.width));
				buf.put(ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer());
//				InMemoryMap.Chunk chunk = new InMemoryMap.Chunk(layer.width, layer.height, 0, 0, buf.array());
//				map.addChunk(chunk);
//				for (int y=0; y<layer.height; y++) {
//					for (int x=0; x<layer.width; x++) {
//						System.out.print( chunk.data()[y*layer.width+x] +", ");
//					}
//					System.out.println("");
//				}
				return buf;
			}
		}
		return null;
	}

}
