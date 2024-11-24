/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.world;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.prelle.simplepersist.AttribConvert;
import org.prelle.simplepersist.Attribute;
import org.prelle.simplepersist.Element;
import org.prelle.simplepersist.ElementList;
import org.prelle.simplepersist.ElementListUnion;

import com.graphicmud.Identifier;
import com.graphicmud.action.script.OnEvent;
import com.graphicmud.action.script.OnEventJS;
import com.graphicmud.action.script.OnEventXML;
import com.graphicmud.game.Customizable;
import com.graphicmud.game.MUDEntity;
import com.graphicmud.game.MUDEvent;
import com.graphicmud.game.ReactsOnTime;
import com.graphicmud.io.IdentifierConverter;
import com.graphicmud.world.text.RoomComponent;
import com.graphicmud.world.tile.TileAreaComponent;

import lombok.Getter;
import lombok.Setter;

/**
 * This class models a part of the virtual world, that has some
 * special information, like a description or a non-magic-field.
 * For Text-MUDs this will surely be a room but for 2D-MUDs its
 * just a variable sized part of the map.<BR>
 * Special locations usually have a description, flags or
 * event-handler that make the location special.
 *
 * @author Stefan Prelle  <prelle@tzi.de>
 * @version $Id: Location.java,v 1.1 2004/07/04 08:58:20 prelle Exp $
 */

public class Location implements ReactsOnTime, Customizable<Location>, HasRPGData { //extends EventAware {
	
	private final static Logger logger = System.getLogger(Location.class.getPackageName());

	/** Mudwide unique roomnumber */
	@Attribute(name="nr")
	@AttribConvert(value = IdentifierConverter.class)
	protected Identifier nr;
	/** Roomdescription */
	@Element(name="descr")
	protected String desc;

	@Element
	protected RoomComponent room;
	@Element
	protected TileAreaComponent area;
	@Getter
	private List<MUDEntity> entities = new ArrayList<>();
	@Getter
	@Setter
	@Element(name="rpg")
	private String rpgData;
	@Element
	protected AudioComponent audio;
	
	@ElementList(entry="load",type = LoadEntity.class)
	@Getter
	private List<LoadEntity> loadlist = new ArrayList<>();
    @ElementListUnion(value= {
    	    @ElementList(entry = "onEvent", type = OnEventXML.class, inline=true),
    	    @ElementList(entry = "onEventJS", type = OnEventJS.class, inline=true)
    })
	@Getter
    private List<OnEvent> eventHandlers = new ArrayList<>();
	
	protected transient List<Consumer<Location>> pulseHooks = new ArrayList<Consumer<Location>>();
	protected transient List<Consumer<Location>> tickHooks = new ArrayList<Consumer<Location>>();
	protected transient List<Consumer<MUDEvent>> eventListener = new ArrayList<Consumer<MUDEvent>>();

	//---------------------------------------------------------------
	public String toString() {
		return String.valueOf(nr);
	}

	//---------------------------------------------------------------
	/**
	 * Returns the mudwide unique number of the room.
	 *
	 * @return Roomnumber
	 */
	public Identifier getNr() { return nr; }

	//---------------------------------------------------------------
	/**
	 * Sets the mudwide unique number of the room.
	 */
	public void setNr(Identifier nr) { this.nr = nr; }

    //---------------------------------------------------------------
    /**
     * Returns a description that is associated with this location.
     *
     * @return String with description - may be NULL
     */
    public String getDescription() {
    	return desc;
    }

	//---------------------------------------------------------------
	public void setDescription(String desc) {
		this.desc = desc;
	}

	//---------------------------------------------------------------
	public Optional<RoomComponent> getRoomComponent() {
		return Optional.ofNullable(room);
	}

	//---------------------------------------------------------------
	public Optional<TileAreaComponent> getTileAreaComponent() {
		return Optional.ofNullable(area);
	}

	//-------------------------------------------------------------------
	public void setTileAreaComponent(TileAreaComponent mapArea) {
		area = mapArea;
	}

	//-------------------------------------------------------------------
	public void addEntity(MUDEntity lifeform) {
		if (!entities.contains(lifeform))
			entities.add(lifeform);
	}

	//-------------------------------------------------------------------
	public void removeEntity(MUDEntity lifeform) {
		entities.remove(lifeform);
	}


	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.game.Customizable#addPulseHook(java.util.function.Consumer)
	 */
	@Override
	public void addPulseHook(Consumer<Location> hook) {
		if (!pulseHooks.contains(hook))
			pulseHooks.add(hook);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.game.Customizable#addTickHook(java.util.function.Consumer)
	 */
	@Override
	public void addTickHook(Consumer<Location> hook) {
		if (!tickHooks.contains(hook))
			tickHooks.add(hook);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.game.ReactsOnTime#pulse()
	 */
	@Override
	public void pulse() {
		for (MUDEntity lf :new ArrayList<MUDEntity>(entities)) {
			lf.pulse();
		}
		
		// Run extra tasks
		for (Consumer<Location> task : pulseHooks) {
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
		for (MUDEntity lf :new ArrayList<MUDEntity>(entities)) {
			try {
				lf.tick();
			} catch (Exception e) {
				logger.log(Level.ERROR, "Failed TICK task on entity "+lf+" in location "+getNr(),e);
			}
		}
		// Run extra tasks
		for (Consumer<Location> task : tickHooks) {
			try {
				task.accept(this);
			} catch (Throwable e) {
				logger.log(Level.ERROR, "Failed TICK task "+task.getClass(),e);
			}
		}
		
	}

	//-------------------------------------------------------------------
	public void registerEventListener(Consumer<MUDEvent> callback) {
		if (!eventListener.contains(callback))
			eventListener.add(callback);
	}

	//-------------------------------------------------------------------
	public void unregisterEventListener(Consumer<MUDEvent> callback) {
		eventListener.remove(callback);
	}

	//-------------------------------------------------------------------
	public void fireEvent(MUDEvent event) {
		for (MUDEntity entity : entities) {
			try { entity.receiveEvent(event); } catch (Exception e) {
				logger.log(Level.ERROR, "Error processing event "+event,e);
			}
		}
		for (Consumer<MUDEvent> callback : eventListener) {
			try { callback.accept(event); } catch (Exception e) {
				logger.log(Level.ERROR, "Error processing event in registered listener "+event,e);
			}
		}
	}

} // Location
