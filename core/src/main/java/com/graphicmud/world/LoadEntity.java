/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.world;

import java.util.ArrayList;
import java.util.List;

import org.prelle.simplepersist.AttribConvert;
import org.prelle.simplepersist.Attribute;
import org.prelle.simplepersist.ElementList;

import com.graphicmud.Identifier;
import com.graphicmud.game.EntityType;
import com.graphicmud.io.IdentifierConverter;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 */
@Getter
@Setter
public class LoadEntity {

	@Attribute(required=true)
	private EntityType type;
	@Attribute(required=true)
	@AttribConvert(value = IdentifierConverter.class)
	private Identifier ref;
	// Coordinates within room
	@Attribute
	private Integer x;
	// Coordinates within room
	@Attribute
	private Integer y;
	
	@ElementList(entry="load",type = LoadEntity.class, inline=true)
	@Getter
	private List<LoadEntity> loadlist = new ArrayList<>();
	
}
