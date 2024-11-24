/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.commands;

import java.util.Locale;
import java.util.Map;

import com.graphicmud.game.MUDEntity;
import com.graphicmud.player.PlayerCharacter;

import de.rpgframework.MultiLanguageResourceBundle;

/**
 * Created at 09.06.2007, 17:51:07
 *
 * @author Stefan Prelle
 * @version $Id$
 *
 */
public interface Command {

     //-------------------------------------------------------------------
    /**
     * Returns a unique, untranslated identifier - not shown to players but to
     * distinguish commands internally
     * @return
     */
    public String getId();

    //---------------------------------------------------------------
	MultiLanguageResourceBundle getProperties();

    public CommandGroup getCommandGroup();

    public String getDescription();

    //-------------------------------------------------------------------------
    /**
     * Checks if a player is allowed to execute this command - meaning
     * any of its subtypes.
     *
     * @param  s Player
     * @return true, when the player has the permission to execute the command
     */
    public boolean canBeExecutedBy(PlayerCharacter s);

    //---------------------------------------------------------------
    public void execute(MUDEntity np, Map<String, Object> params);

    //---------------------------------------------------------------
	public String getI18N(String key);
    //---------------------------------------------------------------
	/**
	 * Like getI18N(), but prepends a "command.<id>" prefix to the key
	 */
	public String getString(String key);

    //---------------------------------------------------------------
	public String getI18N(String key, Locale loc);

	public String fillString(String key, Object...data);

}