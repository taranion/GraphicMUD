/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.player;

import java.util.List;

/**
 *
 */
public interface PlayerDatabase {

	//-------------------------------------------------------------------
	public boolean doesAccountExist(String login);

	public PlayerAccount authenticate(String login, String secret);

	//-------------------------------------------------------------------
	/**
	 * @param protcol "telnet", "discord", "telegram" or whatever
	 * @param login Connector specific identifier or username
	 * @return
	 */
	public PlayerAccount getAccount(String protcol, String identifier);

	//-------------------------------------------------------------------
	public PlayerAccount createAccount(String login, String secret);

	//-------------------------------------------------------------------
	public void save(PlayerAccount account);

	//-------------------------------------------------------------------
	public boolean doesCharacterExist(String name);

	//-------------------------------------------------------------------
	public List<PlayerCharacter> getCharacters(PlayerAccount account);

	//-------------------------------------------------------------------
	public PlayerCharacter createCharacter(PlayerAccount account, String name);

	//-------------------------------------------------------------------
	public void saveCharacter(PlayerAccount account, PlayerCharacter character);

	//-------------------------------------------------------------------
	public PlayerCharacter loadCharacter(PlayerAccount account, PlayerCharacter character);

	//-------------------------------------------------------------------
	public void deleteCharacter(PlayerAccount account, PlayerCharacter character);

}
