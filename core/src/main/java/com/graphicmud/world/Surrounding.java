/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.world;

import java.nio.file.Path;
import java.util.List;

import org.prelle.ansi.commands.SelectGraphicRendition;
import org.prelle.mudansi.MarkupElement;

import com.graphicmud.game.MUDEntity;
import com.graphicmud.map.ViewportMap;
import com.graphicmud.symbol.Symbol;
import com.graphicmud.world.text.Direction;

import lombok.Data;

/**
 * A surrounding models the part of the MUD-world a player can see from his
 * current position. It may be a room in a textual MUD or a part of the
 * map for tile-graphic MUDs.
 *
 * @author Stefan Prelle  <prelle@tzi.de>
 * @version $Id: Surrounding.java,v 1.1 2004/07/04 08:58:20 prelle Exp $
 */

public interface Surrounding {

	public Location getLocation();

    //---------------------------------------------------------------
	public List<MarkupElement> getTitle();

    //---------------------------------------------------------------
	public Path getMoodImage();

    //---------------------------------------------------------------
	public List<Direction> getDirections();

    //---------------------------------------------------------------
    /**
     * Returns a description that is associated with this surrounding.
     *
     * @return String with description - may be NULL
     */
    public List<MarkupElement> getDescription();

    //---------------------------------------------------------------
    public ViewportMap<Symbol> getMap();

    //---------------------------------------------------------------
    public List<MUDEntity> getLifeforms();
    public List<IMUDItem> getItems();
//    public Track getAmbience();

	void addLifeform(MUDEntity value);

    List<ColorLine> getPlayerCharacterLines();
    List<ColorLine> getOtherMobileCharacterLines();
    List<ColorLine> getItemLines();

    @Data
    class ColorLine {
        final String text;
        final SelectGraphicRendition.Meaning colorMeaning;
    }



} // Surrounding
