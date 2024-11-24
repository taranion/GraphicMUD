/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.game;

import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.prelle.simplepersist.AttribConvert;
import org.prelle.simplepersist.Attribute;
import org.prelle.simplepersist.Element;
import org.prelle.simplepersist.ElementList;

import com.graphicmud.Identifier;
import com.graphicmud.action.script.OnEventXML;
import com.graphicmud.character.EquipmentPosition;
import com.graphicmud.ecs.ComponentList;
import com.graphicmud.io.IdentifierConverter;
import com.graphicmud.world.LoadEntity;
import com.graphicmud.world.Location;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * This is the base class of all mobiles or items as it would be defined
 * in static data on disk/in database.
 */
@Getter
@Setter
public class MUDEntityTemplate {

	private final static System.Logger logger = System.getLogger(MUDEntityTemplate.class.getPackageName());

	@Attribute(required = true)
	@AttribConvert(value = IdentifierConverter.class)
	private Identifier id;
	@Attribute(required = true)
	private EntityType type;
	@Attribute(required = true)
	private String name;
	@Element
	private String keywords;
	@Element(name="image")
	private String imageFilename;
	@Attribute(name="ruleData")
	private String ruleDataReference;
	@Element
	private ComponentList components = new ComponentList();
	@ElementList(entry="load",type = LoadEntity.class)
	private List<LoadEntity> loadlist = new ArrayList<>();
	@ElementList(entry="flag",type = EntityFlag.class, convert = EntityFlagConverter.class)	
	private List<EntityFlag> flags = new ArrayList<>();
    @ElementList(entry = "onEvent", type = OnEventXML.class, inline=true)
    @Getter(AccessLevel.PROTECTED)
    private List<OnEventXML> eventHandlers = new ArrayList<>();
	@Element
	private String equipmentSlot;
	@Element
	private String description;
	@Element
	private String descriptionInRoom;
	
	/**
	 * The resolved reference
	 */
	private transient Object ruleObject;
	private transient Location room;
	private transient Path zoneDir;

	
	//-------------------------------------------------------------------
	public String toString() {
		return id+":="+ruleDataReference+":="+ruleObject;
	}
	
	//-------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	public <C> C getComponent(Class<C> clazz) {
		return (C)components.stream().filter(c -> c.getClass()==clazz).findFirst().orElse(null);
	}

	//-------------------------------------------------------------------
	public void setContext(Location room, Path zoneDir) {
		this.room = room;
		this.zoneDir = zoneDir;
	}
	
	//-------------------------------------------------------------------
	public String[] getKeywords() {
		if (keywords == null) {
			return new String[0];
		}
		return keywords.split(",");
	}
	
	//-------------------------------------------------------------------
	public EquipmentPosition getEquipmentSlot() {
		if (equipmentSlot == null || equipmentSlot.isBlank()) {
			return EquipmentPosition.NONE;
		}
		try	{
			return EquipmentPosition.valueOf(equipmentSlot);
		} catch (IllegalArgumentException e) {
			logger.log(System.Logger.Level.WARNING, 
					MessageFormat.format("unknown Equipmentposition found for item {0} [{1}]", this.getName(), equipmentSlot));
			return EquipmentPosition.NONE;
		}  
		
	}
	
}
