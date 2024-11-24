/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.world.impl;

import de.rpgframework.genericrpg.data.Lifeform;
import de.rpgframework.genericrpg.items.PieceOfGear;
import org.prelle.ansi.commands.SelectGraphicRendition;
import org.prelle.mudansi.MarkupParser;
import org.prelle.simplepersist.Persister;

import com.graphicmud.Identifier;
import com.graphicmud.LoadStrategy;
import com.graphicmud.Localization;
import com.graphicmud.MUD;
import com.graphicmud.MUDException;
import com.graphicmud.ZoneIdentifier;
import com.graphicmud.action.cooked.ParameterType;
import com.graphicmud.action.script.EventProcessor;
import com.graphicmud.action.script.OnEvent;
import com.graphicmud.action.script.OnEventJS;
import com.graphicmud.game.EntityState;
import com.graphicmud.game.EntityType;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MUDEntityTemplate;
import com.graphicmud.game.MobileEntity;
import com.graphicmud.game.Pose;
import com.graphicmud.game.impl.MUDEntityTemplateList;
import com.graphicmud.io.text.TextUtil;
import com.graphicmud.map.GridMap;
import com.graphicmud.map.TMXImporter;
import com.graphicmud.map.ViewportMap;
import com.graphicmud.player.PlayerCharacter;
import com.graphicmud.symbol.Symbol;
import com.graphicmud.world.LoadEntity;
import com.graphicmud.world.Location;
import com.graphicmud.world.LocationFactory;
import com.graphicmud.world.NoSuchPositionException;
import com.graphicmud.world.Position;
import com.graphicmud.world.PositionFactory;
import com.graphicmud.world.Range;
import com.graphicmud.world.Surrounding;
import com.graphicmud.world.World;
import com.graphicmud.world.WorldCenter;
import com.graphicmud.world.Zone;
import com.graphicmud.world.impl.WorldImpl.ZoneLoad;
import com.graphicmud.world.text.AWorldCenter;
import com.graphicmud.world.text.Direction;
import com.graphicmud.world.text.Exit;
import com.graphicmud.world.text.RoomComponent;
import com.graphicmud.world.text.RoomPosition;
import com.graphicmud.world.text.TextWorldCenter;
import com.graphicmud.world.tile.GridPosition;
import com.graphicmud.world.tile.TileAreaComponent;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 *
 */
public class FileWorldCenter extends AWorldCenter implements WorldCenter, TextWorldCenter {

	private final static String WORLD_FILE = "world.xml";

	private Path staticDataDir;
	private Path worldsDir;
	private LoadStrategy strategy;

	private Persister persister;

	private Map<ZoneIdentifier,Zone> zones;
	private Map<Identifier,Location> rooms;
	private Map<Identifier,MUDEntityTemplate> mobiles;
	private Map<Identifier,MUDEntityTemplate> items;

	//-------------------------------------------------------------------
	public FileWorldCenter(Path dataDir, LoadStrategy strategy, LocationFactory roomFactory, PositionFactory positionFactory) {
		super(roomFactory, positionFactory);
		this.staticDataDir = dataDir;
		this.strategy     = strategy;
		this.worldsDir    = dataDir.resolve("world");

		zones  = new HashMap<>();
		rooms  = new HashMap<>();
		mobiles= new HashMap<Identifier, MUDEntityTemplate>();
		items  = new HashMap<Identifier, MUDEntityTemplate>();
	}

	//---------------------------------------------------------------
	/**
	 * Start loading data
	 */
	public void start() {
		loadData();
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.text.AWorldCenter#createWorld(java.lang.String)
	 */
	@Override
	public World createWorld(String name) throws MUDException {
		World created = createWorld(name);
		Path dir = staticDataDir.resolve(String.format("%2s",created.getID()));
		try {
			Files.createDirectories(dir);
			Path worldFile = dir.resolve(WORLD_FILE);

			persister.write(created, new FileWriter(worldFile.toFile(), StandardCharsets.UTF_8));
		} catch (IOException e) {
			logger.log(Level.ERROR, "Failed creating world in "+dir,e);
		}
		return created;
	}

	//-------------------------------------------------------------------
	private void loadData() {
		logger.log(Level.INFO, "ENTER: loadData");
		try {
			persister = new Persister();
			List<Path> worldDirectories = findWorldDirectories();
			int worldsLoaded = 0;
			for (Path worldDir : worldDirectories) {
				loadWorld(worldDir);
				worldsLoaded++;
			}
			if (worldsLoaded==0) {
				logger.log(Level.WARNING, "No worlds - that won't work");
			}
		} finally {
			logger.log(Level.INFO, "LEAVE: loadData");
		}
	}

	//-------------------------------------------------------------------
	private List<Path> findWorldDirectories() {
		logger.log(Level.INFO, "ENTER: findWorldDirectories");
		List<Path> ret = new ArrayList<Path>();
		/*
		 * If there is a worlds file, read allowed worlds from there,
		 * otherwise interpret all directories as world numbers
		 */
		Path worldsIndex = worldsDir.resolve(WORLD_FILE);
//		if (Files.exists(worldsIndex)) {
//			List<WorldImpl> list = new ArrayList<WorldImpl>();
//			list = gson.fromJson(new FileReader(worldsIndex.toFile(), StandardCharsets.UTF_8), list.getClass());
//
//		}

		try {
			DirectoryStream<Path> files = Files.newDirectoryStream(worldsDir, p ->
					Files.isDirectory(p)
					);
			files.forEach(p -> ret.add(p));
		} catch (IOException e) {
			logger.log(Level.ERROR, "findWorldDirectories failed",e);
		}
		logger.log(Level.INFO, "LEAVE: findWorldDirectories");
		return ret;
	}

	//-------------------------------------------------------------------
	private void loadWorld(Path worldDir) {
		try {
			// Search for a world file
			DirectoryStream<Path> stream = Files.newDirectoryStream(worldDir, p ->
						Files.isRegularFile(p)
						&& p.getFileName().toString().startsWith("world")
						&& p.getFileName().toString().endsWith(".xml"));
			Iterator<Path> it = stream.iterator();
			if (!it.hasNext()) {
				logger.log(Level.ERROR, "World directory {0} missed a world*.xml file", worldDir);
			}
			Path worldFile = it.next();
			// Load and parse the information about the world
			WorldImpl data = persister.read(WorldImpl.class, new FileInputStream(worldFile.toFile()));
			logger.log(Level.INFO, "Read {0} - {1}", data.getID(), data.getTitle());
			knownWorlds.put(data.getID(), data);

			// Now load each zone in this world
			for (ZoneLoad zoneLoad : data.getZonesToLoad()) {
				try {
					Path zoneDir = worldDir.resolve( String.format("%02d",zoneLoad.getZoneId()));
					loadZone(zoneDir, data, zoneLoad.getZoneId(), data);
				} catch (Exception e) {
					logger.log(Level.ERROR, "Failed loading zone {0} of world {1}/{2}: "+e, zoneLoad.getZoneId(), data.getID(), data.getTitle());
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//-------------------------------------------------------------------
	private void loadWorldFile(Path worldFile) {
		logger.log(Level.DEBUG, "Trying to load world file {0}", worldFile);
		try {
			WorldImpl data = persister.read(WorldImpl.class, new FileInputStream(worldFile.toFile()));
			logger.log(Level.INFO, "Read {0} - {1}", data.getID(), data.getTitle());
			knownWorlds.put(data.getID(), data);

			for (ZoneLoad zoneLoad : data.getZonesToLoad()) {
				try {
					loadZone(worldFile.getParent(), data, zoneLoad.getZoneId(), data);
				} catch (Exception e) {
					logger.log(Level.ERROR, "Failed loading zone {0} of world {1}/{2}: "+e, zoneLoad.getZoneId(), data.getID(), data.getTitle());
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//-------------------------------------------------------------------
	private void loadZone(Path dir, World data, int zoneId, World world) {
		// Prepare error logging
		StringWriter errorWriter = new StringWriter();
		Path errorFile = dir.resolve("errors.txt");
		try { Files.deleteIfExists(errorFile); } catch (Exception e) {}

		String fileName = String.format("zone_%02d%02d.xml", data.getID(), zoneId);
		Path zoneFile = dir.resolve(fileName);
		if (!Files.exists(zoneFile)) {
			zoneFile = dir.resolve("rooms.xml");
		}
		if (!Files.exists(zoneFile)) {
			logger.log(Level.ERROR, "Missing zonefile {0} for world ''{1}''", zoneFile, data.getTitle());
			return;
		}

		try {
			Zone zone = persister.read(ZoneImpl.class, new FileReader(zoneFile.toFile()));
			((ZoneImpl)zone).setZoneDir(dir);
			((ZoneImpl)zone).setWorld(world);
			logger.log(Level.INFO, "Successfully loaded zone {0} - {1} with {2} rooms", zone.getNr(), zone.getTitle(), zone.getRooms().size());
			zones.put(new ZoneIdentifier(data.getID(),zone.getNr()), zone);
			// Load all mobile definitions
			loadMobiles(dir, data, zone, errorWriter);
			// Load all item definitions
			loadItems(dir, data, zone, errorWriter);
			// Now load rooms
			loadRooms(dir, data, zone, zoneFile, errorWriter);

			// Write error file if necessary
			if (!errorWriter.toString().isEmpty()) {
				logger.log(Level.WARNING, "Errors exist in zone {0} - check {1}",zoneId, errorFile.toString());
				Files.write(errorFile, errorWriter.toString().getBytes(StandardCharsets.UTF_8));
			}
		} catch (IOException e) {
			errorWriter.write("Failed loading zonefile: "+e);
			logger.log(Level.ERROR, "Failed loading zone "+zoneFile,e);
		}
	}

	//-------------------------------------------------------------------
	private void loadMobiles(Path dir, World data, Zone zone, StringWriter errorWriter) {
		int fullZoneId = data.getID()*100 + zone.getNr()%100;
		Path mobFile = dir.resolve("mobiles.xml");
		if (!Files.exists(mobFile)) {
			logger.log(Level.DEBUG, "no mobiles file found at {0}", mobFile);
			return;
		}

		ArrayList<MUDEntityTemplate> list = new ArrayList<MUDEntityTemplate>();
		try {
			FileReader in = new FileReader(mobFile.toFile());
			list = persister.read(MUDEntityTemplateList.class, in);
			in.close();
			logger.log(Level.INFO, "Loaded {0} mobs", list.size());
			for (MUDEntityTemplate temp : list) {
				Identifier key = temp.getId();
				key.setZone(zone);
				key.setWorld(data);
				logger.log(Level.INFO, "Found mob {0}", key);
				if (temp.getRuleDataReference()!=null) {
					Object life = MUD.getInstance().getRpgConnector().getByReference(temp.getRuleDataReference());
					if (life==null) {
						errorWriter.write(temp.getRuleDataReference()+" does not resolve");
						logger.log(Level.WARNING, "Mobile {0} has invalid reference {1}", temp.getId().toZoneLocalId(), temp.getRuleDataReference());
					} else {
						temp.setRuleObject(life);
					}
					logger.log(Level.INFO, "Loaded mob {0}", temp.getName());
				} else {
					// Find file
					Path dataFile = dir.resolve("MOBILE-"+temp.getId().toZoneLocalId()+".dat");
					if (!Files.exists(dataFile)) {
						logger.log(Level.WARNING, "Mobile {0} expects a file {1}", temp.getId().toZoneLocalId(), dataFile);
						errorWriter.write("Mobile "+temp.getId()+" expects a file "+dataFile+"\n");
					} else {
						Lifeform<?, ?, ?> life = MUD.getInstance().getRpgConnector().deserializeLifeform(Files.readAllBytes(dataFile));
						if (life==null) {
							errorWriter.write("Custom datafile "+dataFile+" is not parsable\n");
							logger.log(Level.WARNING, "Mobile {0} has unparsable data file {1}", temp.getId().toZoneLocalId(), dataFile);
						} else {
							temp.setRuleObject(life);
						}
					}
				}
				// Store
				logger.log(Level.DEBUG, "Store "+key);
				mobiles.put(key, temp);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
	}

	//-------------------------------------------------------------------
	private void loadItems(Path dir, World data, Zone zone, StringWriter errorWriter) {
		int fullZoneId = data.getID()*100 + zone.getNr()%100;
		Path itemFile = dir.resolve("items.xml");
		if (!Files.exists(itemFile)) {
			logger.log(Level.DEBUG, "no items file found at {0}", itemFile);
			return;
		}

		ArrayList<MUDEntityTemplate> list = new ArrayList<MUDEntityTemplate>();
		try {
			FileReader in = new FileReader(itemFile.toFile());
			list = persister.read(MUDEntityTemplateList.class, in);
			in.close();
			logger.log(Level.INFO, "Loaded {0} items", list.size());
			for (MUDEntityTemplate temp : list) {
				Identifier key = temp.getId();
				key.setZone(zone);
				key.setWorld(data);
				logger.log(Level.INFO, "Found item {0}", key);
				if (temp.getRuleDataReference()!=null) {
					Object life = MUD.getInstance().getRpgConnector().getByReference(temp.getRuleDataReference());
					if (life==null) {
						errorWriter.write(temp.getRuleDataReference()+" does not resolve");
						logger.log(Level.WARNING, "Item {0} has invalid reference {1}", temp.getId().toZoneLocalId(), temp.getRuleDataReference());
					} else {
						temp.setRuleObject(life);
						logger.log(Level.INFO, "Found item {0} and associated it with rule object {1}", key, temp);
					}
				} else {
					// Find file
					Path dataFile = dir.resolve("ITEM-"+temp.getId().toZoneLocalId()+".dat");
					if (!Files.exists(dataFile)) {
						logger.log(Level.WARNING, "Item {0} expects a file {1}", temp.getId().toZoneLocalId(), dataFile);
						errorWriter.write("Item "+temp.getId()+" expects a file "+dataFile);
					} else {
						PieceOfGear life = MUD.getInstance().getRpgConnector().deserializeItem(Files.readAllBytes(dataFile));
						if (life==null) {
							errorWriter.write("Custom datafile "+dataFile+" is not parsable");
							logger.log(Level.WARNING, "Item {0} has unparsable data file {1}", temp.getId().toZoneLocalId(), dataFile);
						} else {
							temp.setRuleObject(life);
						}
					}
				}
				// Store
				logger.log(Level.DEBUG, "Store "+key);
				items.put(key, temp);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
	}

	//-------------------------------------------------------------------
	private void loadRooms(Path dir, World data, Zone zone, Path zoneFile, StringWriter errorWriter) {
		ZoneIdentifier fullZoneId = new ZoneIdentifier(data.getID(),zone.getNr());
		try {
			for (Location room : zone.getRooms()) {
				Identifier realRoomNr = room.getNr().makeGlobal(data, zone);
				logger.log(Level.DEBUG, "Add room {0}",realRoomNr);
				rooms.put(realRoomNr, room);

				for (LoadEntity load : room.getLoadlist()) {
					Identifier ref = load.getRef().asGlobalId(data, zone);
					
					logger.log(Level.WARNING, "Load into room {0}: {1}",load.getType(),ref);
					MUDEntityTemplate temp = null;
					MUDEntity entity = null;
					switch (load.getType()) {
					case MOBILE:
						temp = mobiles.get(ref);
						if (temp==null) {
							logger.log(Level.WARNING, "Zone {0}, Room {1} has unresolvable TTRPG reference {2}", fullZoneId, room.getNr(), ref);
							logger.log(Level.WARNING, "Mobiles = "+mobiles.keySet());
							errorWriter.append("Room "+room.getNr()+" has unresolvable reference "+ref);
						} else {
							temp.setContext(room, dir);
						}
						entity = MUD.getInstance().getGame().instantiate(temp);
						entity.setPosition(MUD.getInstance().getWorldCenter().createPosition());
						entity.getPosition().setRoomPosition(new RoomPosition(realRoomNr));
						room.addEntity(entity);
						break;
					case ITEM:
						temp = items.get(ref);
						if (temp==null) {
							logger.log(Level.WARNING, "Zone {0}, Room {1} has unresolvable TTRPG reference {2}", fullZoneId, room.getNr(), load.getRef());
						} else {
							temp.setContext(room, dir);
						}
						entity = MUD.getInstance().getGame().instantiate(temp);
						entity.setPosition(MUD.getInstance().getWorldCenter().createPosition());
						entity.getPosition().setRoomPosition(new RoomPosition(realRoomNr));
						room.addEntity(entity);
						break;
					}
					
					// Recurse loading
					loadInventory(dir, fullZoneId,data,zone,room, load.getLoadlist(), entity, errorWriter);
				}
			}

			validateExits(data, (ZoneImpl) zone);
			validateScripts(data, (ZoneImpl) zone);
			
			// Import mapfile, should there be one
			if (zone.getMapFile()!=null) {
				Path mapFile = dir.resolve(zone.getMapFile());
				if (!Files.exists(mapFile)) {
					mapFile = dir.resolve("maps").resolve(zone.getMapFile());
				}
				if (!Files.exists(mapFile)) {
					logger.log(Level.ERROR, "Zonefile {0} references mapfile that does not exist: ''{1}''", zoneFile, mapFile.toAbsolutePath());
					return;
				}

				logger.log(Level.INFO, "Importing map file {0}", mapFile);
				GridMap imported = TMXImporter.importMap(data, zone, mapFile, errorWriter);
				logger.log(Level.INFO, "..Importing finished - validating it against zone");
				zones.put(fullZoneId, zone);
				validateMap(data, (ZoneImpl) zone, imported);
				zone.setCachedMap(imported);
			}

		} catch (IOException e) {
			logger.log(Level.ERROR, "Failed loading zone "+zoneFile,e);
			System.exit(1);
		}
	}

	//-------------------------------------------------------------------
	private void loadInventory(Path dir, ZoneIdentifier fullZoneId, World data, Zone zone, Location room, List<LoadEntity> loadlist, MUDEntity loadInto, StringWriter errorWriter) {
		Identifier realRoomNr = room.getNr().makeGlobal(data, zone);
		for (LoadEntity load : loadlist) {
			Identifier ref = load.getRef().asGlobalId(data, zone);
			
			logger.log(Level.DEBUG, "Load into container {0}: {1}",load.getType(),ref);
			MUDEntityTemplate temp = null;
			MUDEntity entity = null;
			switch (load.getType()) {
			case MOBILE:
				temp = mobiles.get(ref);
				if (temp==null) {
					logger.log(Level.WARNING, "Zone {0}, Room {1} has unresolvable TTRPG reference {2}", fullZoneId, room.getNr(), ref);
					logger.log(Level.WARNING, "Mobiles = "+mobiles.keySet());
					errorWriter.append("Room "+room.getNr()+" has unresolvable reference "+ref);
				} else {
					temp.setContext(room, dir);
				}
				entity = MUD.getInstance().getGame().instantiate(temp);
				entity.setPosition(MUD.getInstance().getWorldCenter().createPosition());
				entity.getPosition().setRoomPosition(new RoomPosition(realRoomNr));
				loadInto.addToInventory(entity);
				break;
			case ITEM:
				temp = items.get(ref);
				if (temp==null) {
					logger.log(Level.WARNING, "Zone {0}, Room {1} has unresolvable TTRPG reference {2}", fullZoneId, room.getNr(), load.getRef());
				} else {
					temp.setContext(room, dir);
				}
				entity = MUD.getInstance().getGame().instantiate(temp);
				entity.setPosition(MUD.getInstance().getWorldCenter().createPosition());
				entity.getPosition().setRoomPosition(new RoomPosition(realRoomNr));
				loadInto.addToInventory(entity);
				break;
			}
			
			// Recurse loading
//			loadInventory(load.getLoadlist(), entity);
		}	
	}

	//-------------------------------------------------------------------
	private void validateExits(World data, ZoneImpl zone) {
		logger.log(Level.DEBUG, "validateExits");

		for (Location room : zone.getRooms()) {
			room.getNr().setWorld(data);
			room.getNr().setZone(zone);

			if (room.getRoomComponent().isPresent()) {
				for (Exit exit : room.getRoomComponent().get().getExitList()) {
					exit.getTargetRoom().makeGlobal(data, zone);
				}
			}
		}
	}

	//-------------------------------------------------------------------
	private void validateScripts(World data, ZoneImpl zone) {
		logger.log(Level.DEBUG, "validateScripts");

		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("js");
		logger.log(Level.INFO, "check for scripts");
		for (Location room : zone.getRooms()) {
			room.getNr().setWorld(data);
			room.getNr().setZone(zone);

			for (OnEvent onEv : room.getEventHandlers()) {
			    if (onEv instanceof OnEventJS) {
			    	OnEventJS onEvJS = (OnEventJS)onEv;
					StringBuilder content = new StringBuilder();
					content.append("function runScript() {\r\n");
					content.append("load(\"nashorn:mozilla_compat.js\");\n");
					content.append("importClass(org.prelle.mud.character.EquipmentPosition);\n");
					content.append("importClass(org.prelle.mud.network.ClientConnection);\n");
					content.append(onEvJS.getScriptContent());
					content.append("\n};\n");
					try {
						CompiledScript script = ((Compilable) engine).compile(content.toString());
						onEvJS.setCompiled(script);
					} catch (ScriptException e) {
						logger.log(Level.ERROR, "Error compiling script in room "+room.getNr(),e);
					}
			    }
			}
		}
	}

	//-------------------------------------------------------------------
	private void validateMap(World data, ZoneImpl zone, GridMap imported) {
		logger.log(Level.DEBUG, "Validating {0}", imported);

	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.WorldCenter#getLocations(com.graphicmud.world.Position)
	 */
	@Override
	public Location[] getLocations(Position pos) {
		List<Location> ret = new ArrayList<>();
		ZoneIdentifier zoneNr = pos.getZoneNumber();
		Zone zone = zones.get(zoneNr);
		if (zone==null) {
			logger.log(Level.ERROR, "No zone for zonenumber {0} in position {1}\nCurrent zones are: {2}", zoneNr, pos, zones.keySet());
//			return new Location[0];
		}

		// If there is a tile position, get all locations based
		// on that position only - otherwise use room position
		if (pos.getTilePosition()!=null) {
			int x = pos.getTilePosition().getX();
			int y = pos.getTilePosition().getY();
			for (Location loc : zone.getRooms()) {
				if (loc.getTileAreaComponent().isPresent()) {
					TileAreaComponent area = loc.getTileAreaComponent().get();
					if (x>=area.getStartX() && x<=area.getEndX() && y>=area.getStartY() && y<=area.getEndY()) {
						ret.add(loc);
						continue;
					}
				}
			}
		} else if (pos.getRoomPosition()!=null) {
			Location loc = zone.getRoom( Integer.parseInt(pos.getRoomPosition().getRoomNumber().toZoneLocalId()));
			if (loc==null) {
				logger.log(Level.WARNING, "Unknown room number {0}\nKnown are {1}", pos.getRoomPosition().getRoomNumber(), zone.getRooms());
			} else {
				ret.add(loc);
			}
		}

		Location[] retA = new Location[ret.size()];
		ret.toArray(retA);
		return retA;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.WorldCenter#generateSurrounding(com.graphicmud.player.PlayerCharacter, com.graphicmud.world.Position)
	 */
	@Override
	public Surrounding generateSurrounding(PlayerCharacter player, Position pos) {
		logger.log(Level.DEBUG, "When generating surrounding, room position is "+pos.getRoomPosition());
		logger.log(Level.DEBUG, "When generating surrounding, tile position is "+pos.getTilePosition());
		Location[] locations = getLocations(pos);
		SurroundingImpl view = new SurroundingImpl();

		for (Location loc : locations) {
			logger.log(Level.WARNING, "  possible location "+loc.getNr());
			view.setLocation(loc);
			int key = Integer.parseInt(loc.getNr().toZoneLocalId());
			String raw = loc.getDescription();
			if (raw!=null) {
				raw = raw.replace("\r\n", "").replace("\n", "");
			}
			view.setDescription(MarkupParser.convertText(raw));
			// Add room data
			if (loc.getRoomComponent().isPresent()) {
				RoomComponent room = loc.getRoomComponent().get();
				if (room==null)
					continue;
					//throw new NoSuchPositionException("Roomnumber does not exist: "+rooms.keySet(), pos);

				view.setTitle(MarkupParser.convertText("<b><u>"+room.getTitle().trim()+"</u></b>"));

				// Collect all usable exists
				// TODO: implement looked doors
				List<Direction> exits = new ArrayList<>();
				for (Exit exit : room.getExitList()) {
					exits.add(exit.getDirection());
				}
				view.setDirections(exits);
				logger.log(Level.INFO, "Exits of {0} are {1}", key, exits);
				// Copy others in room
				List<MUDEntity> entities = loc.getEntities();
				logger.log(Level.INFO, "Lifeforms are "+ entities);
				entities.stream().filter(lf -> !lf.equals(player)).forEach(view::addLifeform);
				view.setItemLines(getLinesForItems(view.getLifeforms()));
				view.setPlayerCharacterLines(getLinesForMobiles(view.getLifeforms(), player, EntityType.PLAYER));
				view.setOtherMobileCharacterLines(getLinesForMobiles(view.getLifeforms(), player, EntityType.MOBILE));
			}

			if (pos.getTilePosition()!=null) {
				Zone zone = zones.get(pos.getZoneNumber());
				GridPosition gPos = pos.getTilePosition();

				logger.log(Level.WARNING, "TODO: create a map for position {0},{1} in zone {2}", gPos.getX(), gPos.getY(), zone.getNr());
				GridMap map = zone.getCachedMap();
				logger.log(Level.WARNING, "cached map is {0}", map);
				logger.log(Level.WARNING, "Calling getArea({0}, {1}, 0,5) on {2}", gPos.getX(), gPos.getY(), map.getClass().getSimpleName());
				ViewportMap<Symbol> realMap = map.getArea(gPos.getX(), gPos.getY(), 0, 10,5, -1);
				realMap.setPositionSelf(5, 10);
				// Set the player avatar
//				realMap[5][10]=284;
				view.setMap(realMap);
			}

		}
		if (pos.getTilePosition()!=null && view.getMap()==null) {
			Zone zone = zones.get(pos.getZoneNumber());
			GridPosition gPos = pos.getTilePosition();

			logger.log(Level.WARNING, "TODO: create a map for position {0},{1} in zone {2}", gPos.getX(), gPos.getY(), zone.getNr());
			GridMap map = zone.getCachedMap();
			logger.log(Level.WARNING, "cached map is {0}", map);
			logger.log(Level.WARNING, "Calling getArea({0}, {1}, 0,5) on {2}", gPos.getX(), gPos.getY(), map.getClass().getSimpleName());
			try {
				ViewportMap<Symbol> realMap = map.getArea(gPos.getX(), gPos.getY(), 0,10, 5, -1);
				// Set the player avatar
				realMap.setPositionSelf(5, 10);
				view.setMap(realMap);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return view;
	}


	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.WorldCenter#getLifeformsInRange(com.graphicmud.world.Position, com.graphicmud.world.Range)
	 */
	@Override
	public List<MUDEntity> getLifeformsInRange(Position pos, Range range) {
		List<MUDEntity> result = new ArrayList<MUDEntity>();
		switch (range) {
		case SURROUNDING:
			Location loc = getLocation(pos.getRoomPosition().getRoomNumber());
			result.addAll(loc.getEntities());
			return result;
		case SHOUT: // Neighbor rooms
			loc = getLocation(pos.getRoomPosition().getRoomNumber());
			// If there is a detailed tile map postion, include all
			// lifeforms with a distance of <15 and ignore rooms
			if (loc.getTileAreaComponent().isPresent()) {
				Zone zone = getZone(pos.getZoneNumber());
				if (zone==null) {
					logger.log(Level.ERROR, "Someone has a position with an invalid zone "+pos);
				} else {
					for (MUDEntity mob : zone.getMobs()) {
						GridPosition gpos = mob.getPosition().getTilePosition();
						int distance = pos.getTilePosition().distance(gpos);
						if (distance<15 && !result.contains(mob))
							result.add(mob);
					}
				}
			} else if (loc.getRoomComponent().isPresent()) {
				// ..otherwise include lifeforms from neighboring rooms
				result.addAll(loc.getEntities());
				for (Exit exit : loc.getRoomComponent().get().getExitList()) {
					Location neighborRoom = getLocation(exit.getTargetRoom());
					// May need to avoid duplicates
					result.addAll(neighborRoom.getEntities());
				}
			}
			return result;
		case MAP: // Zone
			Zone zone = getZone(pos.getZoneNumber());
			if (zone==null) {
				logger.log(Level.ERROR, "Someone has a position with an invalid zone "+pos);
			} else {
				result.addAll( zone.getMobs() );
			}
			return result;
		case EVERYWHERE:
			return new ArrayList<MUDEntity>(MUD.getInstance().getGame().getPlayers());
		default:
			logger.log(Level.ERROR,"Don't know how to handle range "+range);
			return new ArrayList<MUDEntity>(MUD.getInstance().getGame().getPlayers());
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.WorldCenter#getLifeformsInRangeExceptSelf(com.graphicmud.player.PlayerCharacter, com.graphicmud.world.Position, com.graphicmud.world.Range)
	 */
	@Override
	public List<MUDEntity> getLifeformsInRangeExceptSelf(MUDEntity player, Range range) {
		return getLifeformsInRange(player.getPosition(), range).stream().filter(lf -> lf!=player).toList();
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.WorldCenter#getNamedLifeformInRange(java.lang.String, com.graphicmud.world.Position, com.graphicmud.world.Range)
	 */
	@Override
	public MUDEntity getNamedLifeformInRange(String name, Position pos, Range range) throws NoSuchPositionException {
		// TODO Auto-generated method stub
		return null;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.WorldCenter#getLocation(com.graphicmud.Identifier)
	 */
	@Override
	public Location getLocation(Identifier id) {
		return rooms.get( id);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.WorldCenter#getLocation(com.graphicmud.world.Position)
	 */
	@Override
	public Location getLocation(Position pos) {
		if (pos.getRoomPosition()==null)
			return null;
		return rooms.get(pos.getRoomPosition().getRoomNumber());
	}


	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.WorldCenter#getZone(int)
	 */
	@Override
	public Zone getZone(ZoneIdentifier nr) {
		return zones.get(nr);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.text.AWorldCenter#pulse()
	 */
	@Override
	public void pulse() {
		logger.log(Level.TRACE, "pulse");
		for (Zone zone : zones.values()) {
			long before=System.currentTimeMillis();
			zone.pulse();
			long diff=System.currentTimeMillis()-before;
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.text.AWorldCenter#pulse()
	 */
	@Override
	public void tick() {
		logger.log(Level.TRACE, "tick");
		for (Zone zone : zones.values()) {
			long before=System.currentTimeMillis();
			zone.tick();
			long diff=System.currentTimeMillis()-before;
		}
	}

	//-------------------------------------------------------------------
	@Override
	public void saveZone(Zone zone) {
		logger.log(Level.DEBUG, "ENTER: saveZone({0})", zone.getNr());
		try {
			int worldNo = zone.getNr()/100;
			Path zoneDir = ((ZoneImpl)zone).getZoneDir();
			String fileName = String.format("zone_%02d%02d.xml", worldNo, zone.getNr()%100);
			Path zoneFile = zoneDir.resolve(fileName);
			Path oldZoneFile = zoneDir.resolve(fileName+".bak");
			Files.deleteIfExists(oldZoneFile);
			Files.move(zoneFile, oldZoneFile, StandardCopyOption.REPLACE_EXISTING);
			logger.log(Level.WARNING, "Save zone {0} to {1}", zone.getNr(), zoneFile);
			persister.write(zone, zoneFile.toFile());
		} catch (Exception e) {
			logger.log(Level.ERROR, "Failed writing zonefile",e);
		} finally {
			logger.log(Level.DEBUG, "LEAVE: saveZone({0})", zone.getNr());
		}

	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.WorldCenter#getItemTemplate(com.graphicmud.Identifier)
	 */
	@Override
	public MUDEntityTemplate getItemTemplate(Identifier ref) {
		MUDEntityTemplate mudEntityTemplate = items.get(ref);
		if (mudEntityTemplate == null) {
			logger.log(Level.WARNING, "No item template found for ref: " + ref);
		}
		return mudEntityTemplate;
	}
	
	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.WorldCenter#getMobileTemplate(com.graphicmud.Identifier)
	 */
	@Override
	public MUDEntityTemplate getMobileTemplate(Identifier ref){
		MUDEntityTemplate mudEntityTemplate = mobiles.get(ref);
		if (mudEntityTemplate == null) {
			logger.log(Level.WARNING, "No mobile template found for ref: " + ref);
		}
		return mudEntityTemplate;
	}

	//-------------------------------------------------------------------
	private List<Surrounding.ColorLine> getLinesForMobiles(List<MUDEntity> entities, MobileEntity onlooker, EntityType type) {
		SelectGraphicRendition.Meaning color = type == EntityType.PLAYER ? SelectGraphicRendition.Meaning.FOREGROUND_CYAN : SelectGraphicRendition.Meaning.FOREGROUND_YELLOW;
		List<MobileEntity> mobs = entities.stream().filter(e -> e.getType() == type).map(e -> (MobileEntity)e).toList();
		List<Surrounding.ColorLine> lines = new ArrayList<>();
		for (MobileEntity mob : mobs) {
			lines.add(getPoseText(mob, onlooker, color));
		}
		return lines;
	}

	//-------------------------------------------------------------------
	private List<Surrounding.ColorLine> getLinesForItems(List<MUDEntity> entities) {
		List<MUDEntity> items = entities.stream().filter(e -> e.getType() == EntityType.ITEM).toList();
		List<Surrounding.ColorLine> lines = new ArrayList<>();
		items.forEach(e -> lines.add(new Surrounding.ColorLine(getDescriptionInRoom(e), SelectGraphicRendition.Meaning.FOREGROUND_GREEN)));
		return lines;
	}

	private static String getDescriptionInRoom(MUDEntity e) {
		if (e.getDescriptionInRoom() != null && !e.getDescriptionInRoom().isBlank()) {
			return e.getDescriptionInRoom();
		}
		String str = e.getName();
		if (str!= null && !str.isBlank()) {
			String result = TextUtil.capitalize(str);
			String onGroundString;
			if (e.getType() == EntityType.ITEM) {
				onGroundString = Localization.getString("outputformat.item.onfloor");
			} else {
				onGroundString = Localization.getString("outputformat.mobile.onfloor");
			}
            result += " " + onGroundString;
			return result;
		}
		return e.getName();
	}

	//-------------------------------------------------------------------
	private static Surrounding.ColorLine getPoseText(MobileEntity mob, MobileEntity onlooker, SelectGraphicRendition.Meaning colorMeaning) {
		Pose pose = mob.getPose();
		if (pose==null) {
			pose=Pose.STANDING;
		}
		String text;
		SelectGraphicRendition.Meaning color = SelectGraphicRendition.Meaning.FOREGROUND_CYAN;
		if (mob.getState() == EntityState.FIGHTING) {
			color = SelectGraphicRendition.Meaning.FOREGROUND_RED;
			MUDEntity target = getOpponent(mob);
			String lookedAtName = mob.getName();
			String opponentName;
			if (target != null && target.equals(onlooker)) {
				opponentName = "YOU!!!";
			} else if (target != null && !(target.equals(onlooker))){
				opponentName = target.getName();
			} else {
				opponentName = "???";
			}
			 text = MessageFormat.format(Localization.getString("outputformat.mobile.fighting"),
					lookedAtName, opponentName);
		} else {
			if (mob.getDescriptionInRoom() != null && !mob.getDescriptionInRoom().isBlank()) {
				String str = mob.getDescriptionInRoom();
				text = TextUtil.capitalize(str);
			} else {
				String pattern = pose.getPosePattern();
				String str = mob.getName();
				text = MessageFormat.format(pattern, TextUtil.capitalize(str));
			}
		}
		return new Surrounding.ColorLine(text, color);
	}

	//-------------------------------------------------------------------
	private static MUDEntity getOpponent(MobileEntity mob) {
		List<MUDEntity> oppositeParty = mob.getCurrentCombat().getOppositeParty(mob);
		return oppositeParty.stream().findFirst().orElse(null);
	}
	
}
