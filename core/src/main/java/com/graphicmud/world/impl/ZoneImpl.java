/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.world.impl;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.prelle.simplepersist.Attribute;
import org.prelle.simplepersist.Element;
import org.prelle.simplepersist.ElementList;
import org.prelle.simplepersist.Root;

import com.graphicmud.game.MUDEntity;
import com.graphicmud.map.GridMap;
import com.graphicmud.world.Location;
import com.graphicmud.world.World;
import com.graphicmud.world.Zone;

import lombok.Getter;
import lombok.Setter;


/**
 * Created at 02.07.2004, 19:05:04
 *
 * @author Stefan Prelle
 *
 */
@Root(name="zone")
public class ZoneImpl implements Comparable<ZoneImpl>, Zone {
	
	private final static Logger logger = System.getLogger(ZoneImpl.class.getPackageName());

	private final static DecimalFormat FORMAT = new DecimalFormat("000");

	@Attribute(name="nr")
	protected int nr;
	@Element(name="title")
	protected String title;
	@Element(name="descr")
	protected String description;
	@Element(name="mapfile")
	protected String mapFile;
	@Element(name="flags")
	protected List<Object> flags;
	@ElementList(entry = "location", type = Location.class, addMethod = "addRoom", convert=UseLocationFactoryConverter.class)
	protected Map<Integer,Location> locations;
//	protected Map<Integer,Item> items;
	
	protected transient List<Consumer<Zone>> pulseHooks = new ArrayList<Consumer<Zone>>();
	protected transient List<Consumer<Zone>> tickHooks = new ArrayList<Consumer<Zone>>();

	private transient GridMap map;
	/** 
	 * Cached list of lifeforms in this zone. Updated every pulse
	 */
	private transient List<MUDEntity> mobs;
	
	@Getter
	@Setter
	private transient Path zoneDir;
	@Getter
	@Setter
	private transient World world;

	//------------------------------------------------
	public ZoneImpl() {
		locations = new HashMap<Integer, Location>();
//		items = new HashMap<Integer, Item>();
		mobs  = new ArrayList<MUDEntity>();
	}

	//------------------------------------------------
	public ZoneImpl(int nr) {
		this();
		this.nr = nr;
	}

	//------------------------------------------------
	public String toString() {
		return "("+FORMAT.format(nr)+") "+title;
	}

	//------------------------------------------------
	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}
	//------------------------------------------------
	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	//------------------------------------------------
	/**
	 * @return Returns the title.
	 */
	public String getTitle() {
		return title;
	}

	//------------------------------------------------
	/**
	 * @param title The title to set.
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	//------------------------------------------------
	public void setNr(int nr) {
		this.nr = nr;
	}

	//------------------------------------------------
	/**
	 * @return Returns the zone number.
	 */
	public int getNr() {
		return nr;
	}

	//------------------------------------------------
	/**
	 * Add a room to the zone. Make sure that there is no
	 * previous room with the same ID.
	 *
	 * @param room SimpleRoom to add
	 * @return TRUE, if room has been added successfully.
	 */
	@Override
	public boolean addRoom(Location room) {
		Integer key = room.getNr().getLocalAsNumber();
		if (locations.get(key)==null) {
			locations.put(key, room);
			return true;
		}
		return false;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.Zone#getRoom(int)
	 */
	@Override
	public Location getRoom(int nr) {
		return (Location) locations.get(nr);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.Zone#getRooms()
	 */
	@Override
	public List<Location> getRooms() {
		return new ArrayList<Location>(locations.values());
	}

	//------------------------------------------------
	public void setRooms(Collection<Location> toSet) {
		locations.clear();
		for (Location room : toSet)
			locations.put(room.getNr().getLocalAsNumber(), room);
	}

//	//--------------------------------------------------
//	/**
//	 * TODO: Comment method
//	 *
//	 * @param items
//	 */
//	public void addItems(List<Item> itemList) {
//		for (Item item : itemList)
//			items.put(new Integer(item.getID()), item);
//	}
//
//	//------------------------------------------------
//	public Item getItem(int nr) {
//		return (Item) items.get(new Integer(nr));
//	}
//
//	//------------------------------------------------
//	public List<Item> getItems() {
//		return new ArrayList<Item>(items.values());
//	}

	//--------------------------------------------------
	public void addLifeform(List<MUDEntity> mobList) {
		System.err.println("TODO: Zone.addMobs");
//		Iterator it = mobList.iterator();
//		while (it.hasNext()) {
//		Lifeform mob = (Lifeform) it.next();
//		mobs.put(new Integer(mob.getID()), mob);
//		}
	}

	//------------------------------------------------
	public MUDEntity getMob(int nr) {
		return (MUDEntity) mobs.get(new Integer(nr));
	}

	//------------------------------------------------
	public List<MUDEntity> getMobs() {
		return new ArrayList<MUDEntity>(mobs);
	}

	//------------------------------------------------
	/*
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(ZoneImpl other) {
		if (this.getNr()<other.getNr()) return -1;
		if (this.getNr()>other.getNr()) return  1;
		return 0;
	}

	//-------------------------------------------------------
	/**
	 * @return the flags
	 */
	public List<Object> getFlags() {
		return flags;
	}

	//-------------------------------------------------------
	/**
	 * @param flags the flags to set
	 */
	public void setFlags(List<Object> flags) {
		this.flags = flags;
	}

	//-------------------------------------------------------------------
	public String getMapFile() {
		return mapFile;
	}

	//-------------------------------------------------------------------
	public void setMapFile(String mapFile) {
		this.mapFile = mapFile;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.Zone#setCachedMap(com.graphicmud.map.InMemoryMap)
	 */
	@Override
	public void setCachedMap(GridMap value) {
		this.map = value;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.world.Zone#getCachedMap()
	 */
	@Override
	public GridMap getCachedMap() {
		return map;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.game.Customizable#addPulseHook(java.util.function.Consumer)
	 */
	@Override
	public void addPulseHook(Consumer<Zone> hook) {
		if (!pulseHooks.contains(hook))
			pulseHooks.add(hook);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.game.Customizable#addTickHook(java.util.function.Consumer)
	 */
	@Override
	public void addTickHook(Consumer<Zone> hook) {
		if (!tickHooks.contains(hook))
			tickHooks.add(hook);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.game.ReactsOnTime#pulse()
	 */
	@Override
	public void pulse() {
		// Update lifeforms
		mobs.clear();
		for (Location room : locations.values()) {
			// Let room handle pulse
			try { room.pulse(); } catch (Exception e) {
				logger.log(Level.ERROR, "Failed PULSE in room "+room,e);
			}
			
			for (MUDEntity lf : new ArrayList<>(room.getEntities())) {
				if (!mobs.contains(lf)) {
					mobs.add(lf);
				}
			}
		}
		
		// Run extra tasks
		for (Consumer<Zone> task : pulseHooks) {
			try {
				task.accept(this);
			} catch (Throwable e) {
				logger.log(Level.ERROR, "Failed PULSE task "+task.getClass(),e);
			}
		}	
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.game.ReactsOnTime#tick()
	 */
	@Override
	public void tick() {
		for (Location room : locations.values()) {
			// Let room handle pulse
			try { room.tick(); } catch (Exception e) {
				logger.log(Level.ERROR, "Failed TICK in room "+room,e);
			}
		}
		
		// Run extra tasks
		for (Consumer<Zone> task : tickHooks) {
			try {
				task.accept(this);
			} catch (Throwable e) {
				logger.log(Level.ERROR, "Failed TICK task "+task.getClass(),e);
			}
		}		
	}

}