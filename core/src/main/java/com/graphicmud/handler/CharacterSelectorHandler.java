/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.handler;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;

import com.graphicmud.Localization;
import com.graphicmud.MUD;
import com.graphicmud.network.ClientConnection;
import com.graphicmud.network.ClientConnectionListener;
import com.graphicmud.network.interaction.ActionMenuItem;
import com.graphicmud.network.interaction.Menu;
import com.graphicmud.network.interaction.MenuHandler;
import com.graphicmud.player.PlayerCharacter;
import com.graphicmud.player.PlayerDatabase;

import de.rpgframework.character.RuleSpecificCharacterObject;

/**
 *
 */
public class CharacterSelectorHandler extends MenuHandler implements ClientConnectionListener {

	private final static Logger logger = System.getLogger("mud.character");

	private static String VAR_STATE = "State";
	private static String VAR_RULESPEC = "Rulespecific";
	private static String VAR_CHARACTER = "Character";
	static enum State {
		MENU,
		CHECK_CHARNAME_EXISTS,
		ASK_FOR_NAME,
		SAVE_CHAR_THAN_PLAY,
		CHAR_SELECTED
	}

	private ClientConnection con;
	private PlayHandler playHandler;

	//-------------------------------------------------------------------
	public CharacterSelectorHandler(ClientConnectionListener returnTo) {
		super(returnTo, null);
		playHandler = new PlayHandler();
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.interaction.MenuHandler#enter(com.graphicmud.network.ClientConnection)
	 */
	@Override
	public void enter(ClientConnection con) {
		this.con = con;
		con.setListenerVariable(this, VAR_STATE, State.MENU);

		menu = new Menu(Localization.fillString("menu.selectchar.title", con.getPlayer().getName()));
		menu.setMessage(Localization.getString("menu.selectchar.mess"));
		List<PlayerCharacter> chars = MUD.getInstance().getPlayerDatabase().getCharacters(con.getPlayer());
		if (chars!=null) {
			for (PlayerCharacter tmp : chars) {
				menu.add( ActionMenuItem.builder()
						.identifier(tmp.getName())
						.label(tmp.getName())
						.onActionPerform( (o,v) -> con.setCharacter(tmp) )
						.finallyGoTo(new PlayHandler())
						.build()
						);

			}
		}
		// Create
		menu.add(ActionMenuItem.builder()
				.identifier("create")
				.label(Localization.getString("menu.selectchar.createNew"))
				.onActionPerform( (o,v) -> con.setListenerVariable(this, VAR_STATE, State.CHECK_CHARNAME_EXISTS) )
				.finallyGoTo(MUD.getInstance().getCreateHandler())
				.build());
		// Exit MUD
		menu.add( ActionMenuItem.builder()
				.identifier("leave")
				.label(Localization.getString("menu.selectchar.back"))
				.emoji("🔙")
				.isExit(true)
				.onActionPerform((a,b) -> con.popConnectionListener(null))
				.build()
		);


		visualMenu = con.presentMenu(menu);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnectionListener#reenter(com.graphicmud.network.ClientConnection, java.lang.Object)
	 */
	@Override
	public void reenter(ClientConnection con, Object result) {
		logger.log(Level.DEBUG, "reenter with {0}", result);
		State state = con.getListenerVariable(this, VAR_STATE);
		logger.log(Level.DEBUG, "State is {0}", state);
		RuleSpecificCharacterObject<?, ?, ?, ?> model = (RuleSpecificCharacterObject<?, ?, ?, ?>)result;
		logger.log(Level.DEBUG, "char is {0}", model);

		if (state==State.CHECK_CHARNAME_EXISTS) {
			eventuallyAskForNameBeforeSaving(con, model);
		} else
			con.presentMenu(menu);
	}

	//-------------------------------------------------------------------
	private void enterState(State newState) {
		State state = con.getListenerVariable(this, VAR_STATE);
		logger.log(Level.DEBUG, "Change from state {0} to {1}", state, newState);
		con.setListenerVariable(this, VAR_STATE, newState);
		switch (newState) {
		case ASK_FOR_NAME:
			con.sendPrompt(Localization.getString("menu.selectchar.enterName.ask")+": ");
			break;
		case SAVE_CHAR_THAN_PLAY:
			MUD.getInstance().getPlayerDatabase().saveCharacter(con.getPlayer(), con.getCharacter());
			logger.log(Level.INFO, "Character saven - now enter game");
			con.pushConnectionListener(playHandler);
			// Should we ever return, use this state
			con.setListenerVariable(this, VAR_STATE, State.MENU);
			break;
		default:
			logger.log(Level.WARNING, "Don't know how to handle "+newState);
		}
	}

	//-------------------------------------------------------------------
	private void eventuallyAskForNameBeforeSaving(ClientConnection con, RuleSpecificCharacterObject<?, ?, ?, ?> model) {
		con.setListenerVariable(this, VAR_RULESPEC, model);
		PlayerDatabase playerDB = MUD.getInstance().getPlayerDatabase();
		// Check if the wizard already returned with a name
		String wizardName = model.getName();
		logger.log(Level.DEBUG, "Name from wizard: "+wizardName);
		if (wizardName==null || playerDB.doesCharacterExist(wizardName)) {
			// Need to ask for a valid character name
			logger.log(Level.DEBUG, "Need to ask for name");
			enterState(State.ASK_FOR_NAME);
		} else {
			logger.log(Level.DEBUG, "No character named ''{0}'' exists - create it for account ''{1}''", wizardName, con.getPlayer().getName());
			PlayerCharacter playerChar = playerDB.createCharacter(con.getPlayer(), wizardName);
			playerChar.setRuleObject(model);
			con.setCharacter(playerChar);
			// Store rule specific character
			playerDB.saveCharacter(con.getPlayer(), playerChar);
			enterState(State.SAVE_CHAR_THAN_PLAY);
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnectionListener#receivedInput(com.graphicmud.network.ClientConnection, java.lang.String)
	 */
	@Override
	public void receivedInput(ClientConnection con, String input) {
		State state = con.getListenerVariable(this, VAR_STATE);
		if (state==State.MENU) {
			super.receivedInput(con, input);
			return;
		}


		logger.log(Level.INFO, "TODO: process "+input+" in state "+state);
		PlayerDatabase playerDB = MUD.getInstance().getPlayerDatabase();
		if (state==State.ASK_FOR_NAME) {
			RuleSpecificCharacterObject<?, ?, ?, ?> model = con.getListenerVariable(this, VAR_RULESPEC);
			// Check if this name isn't in use yet
			if (playerDB.doesCharacterExist(input)) {
				// Player tried to use an already existing name
				logger.log(Level.DEBUG, "Player ''{0}'' tried to create character with existing name ''{1}''", con.getPlayer().getName(), input);
				con.sendPrompt(Localization.getString("menu.selectchar.enterName.otherName")+": ");
				return;
			}
			PlayerCharacter playerChar = playerDB.createCharacter(con.getPlayer(), input);
			playerChar.setRuleObject(model);
			con.setCharacter(playerChar);
			// Store rule specific character
			playerDB.saveCharacter(con.getPlayer(), playerChar);
			enterState(State.SAVE_CHAR_THAN_PLAY);
		}
	}

}
