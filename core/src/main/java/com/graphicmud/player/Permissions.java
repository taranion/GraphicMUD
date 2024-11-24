/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.player;

import java.util.Collection;

public interface Permissions {

	//-------------------------------------------------------------------------
	public Collection<Permission> getPermissions();

	//-------------------------------------------------------------------------
	/**
	 * Fügt dem Spieler eine Berechtigung hinzu. Wurde sie schon gesetzt, so
	 * geschieht nichts.
	 *
	 * @param per Berechtigung, die hinzugef�gt werden soll
	 */
	public void addPermission(String per);

	//-------------------------------------------------------------------------
	/**
	 * Nimmt dem Spieler eine Berechtigung. War sie bisher nicht gesetzt, so
	 * geschieht gar nichts.
	 *
	 * @param per Berechtigung, die entfernt werden soll
	 */
	public void removePermission(String per);

	//-------------------------------------------------------------------------
	/**
	 * Pr�ft, ob ein Spieler eine bestimmte Berechtigung hat.
	 *
	 * @param id ID der Berechtigung, auf die gepr�ft werden soll. Die ID
	 *           entspricht der, aus der Klasse Permission
	 * @see DE.StefanPrelle.MUD.Basics.Permission
	 * @return true, wenn der Spieler die Berechtigung besitzt.
	 */
	public boolean hasPermissionTo(String id);

	//-------------------------------------------------------------------------
	/**
	 * Pr�ft, ob ein Spieler Schreibrechte auf eine bestimmte Zone hat.
	 *
	 * @param zone Nummer der Zone, die gepr�ft werden soll
	 * @return true, wenn der Spieler die Schreibrechte besitzt.
	 */
	public boolean hasWriteAccessToZone(int zone);

	//-------------------------------------------------------------------------
	/**
	 * Pr�ft, ob ein Spieler Schreibrechte auf eine bestimmte Welt hat.
	 *
	 * @param welt Nummer der Welt, die gepr�ft werden soll
	 * @return true, wenn der Spieler die Schreibrechte besitzt.
	 */
	public boolean hasWriteAccessToWelt(int welt);

	//-------------------------------------------------------------------------
	/**
	 * Setzt die Schreibrechte auf eine bestimmte Zone
	 *
	 * @param zone Nummer der Zone, die gesetzt werden soll
	 */
	public void setZone(int zone);

	//-------------------------------------------------------------------------
	public Collection<Integer> getZones();

	//-------------------------------------------------------------------------
	/**
	 * Setzt die Schreibrechte auf eine bestimmte Welt
	 *
	 * @param welt Nummer der Welt, die gesetzt werden soll
	 */
	public void setWelt(int welt);

	//-------------------------------------------------------------------------
	public Collection<Integer> getWorlds();

	//-------------------------------------------------------------------------
	/**
	 * Nimmt die Schreibrechte auf eine bestimmte Zone
	 *
	 * @param zone Nummer der Zone, die gel�scht werden soll
	 */
	public void takeZone(int zone);

	//-------------------------------------------------------------------------
	/**
	 * Nimmt die Schreibrechte auf eine bestimmte Welt
	 *
	 * @param welt Nummer der Welt, die gel�scht werden soll
	 */
	public void takeWelt(int welt);

}