/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.network;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

import com.graphicmud.MUD;
import com.graphicmud.game.Game;
import com.graphicmud.game.Vital;
import com.graphicmud.game.Vital.VitalType;
import com.graphicmud.handler.LoginHandler;
import com.graphicmud.media.DoNothingSoundClient;
import com.graphicmud.media.SoundClient;
import com.graphicmud.network.interaction.Menu;
import com.graphicmud.network.interaction.MenuItem;
import com.graphicmud.player.PlayerAccount;
import com.graphicmud.player.PlayerCharacter;
import com.graphicmud.world.Location;
import com.graphicmud.world.Position;
import com.graphicmud.world.Range;

import lombok.Builder;
import lombok.EqualsAndHashCode;

/**
 *
 */
public abstract class AClientConnection implements ClientConnection {

	@Builder()
	@EqualsAndHashCode
	private static class MenuDataKey {
		private Menu menu;
		private MenuItem<?> item;
	}


	protected Logger logger = System.getLogger("mud.network");

	protected MUDClientCapabilities capabilities;
	protected ClientConnectionState state = ClientConnectionState.ENTER_LOGIN;
	protected Stack<ClientConnectionListener> listener = new Stack<>();
	protected Map<String,Object> variables;
	protected Map<ClientConnectionListener,Map<String,Object>> handlerVariables;
	protected PlayerAccount player;
	protected PlayerCharacter character;
	protected SoundClient soundClient;

	protected Map<MenuDataKey, Object> menuData = new HashMap<>();
	protected boolean doNotDisturb;

	//-------------------------------------------------------------------
	protected AClientConnection() {
		this.capabilities = new MUDClientCapabilities();
		this.variables = new HashMap<String, Object>();
		this.handlerVariables = new HashMap<ClientConnectionListener, Map<String,Object>>();
		soundClient = new DoNothingSoundClient();
	}
	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#storeMenuItemData(com.graphicmud.network.interaction.Menu, com.graphicmud.network.interaction.MenuItem, java.lang.Object)
	 */
	@Override
	public void storeMenuItemData(Menu menu, MenuItem<?> item, Object toStore) {
		menuData.put(MenuDataKey.builder().menu(menu).item(item).build(), toStore);
	}
	@Override
	public <V> V retrieveMenuItemData(Menu menu, MenuItem<?> item) {
		return (V)menuData.get(MenuDataKey.builder().menu(menu).item(item).build());
	}
	@Override
	public void removeMenuItem(Menu menu, MenuItem<?> item) {
		menuData.remove(MenuDataKey.builder().menu(menu).item(item).build());
	}

	//-------------------------------------------------------------------
	public Locale getLocale() {
		return Locale.getDefault();
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#getAccount()
	 */
	@Override
	public String getAccount() {
		return getVariable(LoginHandler.VAR_LOGIN);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#getSoundClient()
	 */
	@Override
	public SoundClient getSoundClient() {
		return soundClient;
	}
	//-------------------------------------------------------------------
	public void setSoundClient(SoundClient value) {
		this.soundClient = value;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#getPlayer()
	 */
	@Override
	public PlayerAccount getPlayer() {
		return player;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#setPlayer(com.graphicmud.player.PlayerAccount)
	 */
	@Override
	public void setPlayer(PlayerAccount value) {
		this.player  = value;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#getCharacter()
	 */
	@Override
	public PlayerCharacter getCharacter() {
		return character;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#setCharacter(com.graphicmud.player.PlayerCharacter)
	 */
	@Override
	public void setCharacter(PlayerCharacter value) {
		this.character = value;
		value.setConnection(this);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#getState()
	 */
	@Override
	public ClientConnectionState getState() {
		return state;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#setState(com.graphicmud.network.ClientConnectionState)
	 */
	@Override
	public void setState(ClientConnectionState newState) {
		if (newState==state) return;
		logger.log(Level.INFO, "State changes from {0} to {1}", state, newState);
		this.state = newState;

		if (newState==ClientConnectionState.DISCONNECTED) {
			MUD.getInstance().getGame().removePlayer(getCharacter());
			Position pos = getCharacter().getPosition();
			for (Location loc : MUD.getInstance().getWorldCenter().getLocations(pos)) {
				MUD.getInstance().getWorldCenter().getLifeformsInRangeExceptSelf(character, Range.SURROUNDING)
					.stream()
					.filter(ent -> ent instanceof PlayerCharacter)
					.map(ent -> (PlayerCharacter)ent)
					.forEach(pc -> pc.getConnection().sendShortText(Priority.UNIMPORTANT, character.getName()+" is linkless"))
					;
				loc.removeEntity(character);
			}
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#setConnectionListener(com.graphicmud.network.ClientConnectionListener)
	 */
	@Override
	public void pushConnectionListener(ClientConnectionListener handler) {
		logger.log(Level.DEBUG, "pushConnectionListener {0}", handler.getClass().getSimpleName());
		if (listener.contains(handler))
			throw new IllegalArgumentException("Handler is already on stack\n"+listener);
		listener.push(handler);
		logger.log(Level.INFO, "Give control to {0}", handler.getClass().getSimpleName());
		try {
			handler.enter(this);
		} catch (Exception e) {
			logger.log(Level.ERROR, "Error entering "+handler.getClass().getSimpleName(),e);
		}
		logger.log(Level.DEBUG, "enter done");
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#getClientConnectionListener()
	 */
	@Override
	public ClientConnectionListener getClientConnectionListener() {
		return listener.peek();
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#popConnectionListener()
	 */
	@Override
	public ClientConnectionListener popConnectionListener(Object returnValue) {
		// Remove all eventually stored variables
		ClientConnectionListener removed = listener.pop();
		logger.log(Level.INFO, "Removed control from {0}", removed);
		if (removed!=null) {
			handlerVariables.remove(removed);
		}

		ClientConnectionListener returnTo = listener.peek();
		logger.log(Level.INFO, "Return control to {0}", returnTo);
		if (returnTo!=null) {
			returnTo.reenter(this, returnValue);
		}

		return returnTo;
	}


	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#setVariable(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setVariable(String key, Object value) {
		if (variables.containsKey(key) && variables.get(key).equals(value))
			return;
		variables.put(key, value);
		logger.log(Level.DEBUG, "Variable {0} changed to {1}  in state {2}", key, value, state);
		if (!listener.isEmpty()) {
			listener.peek().onVariableChange(this, key);
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#clearVariable(java.lang.String)
	 */
	@Override
	public void clearVariable(String key) {
		variables.remove(key);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#getVariable(java.lang.String)
	 */
	@Override
	public <E> E getVariable(String key) {
		return (E)variables.get(key);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#getVariableNames()
	 */
	@Override
	public List<String> getVariableNames() {
		List<String> ret = new ArrayList<String>(variables.keySet());
		Collections.sort(ret);
		return ret;
	}

	//-------------------------------------------------------------------
	public int getNumericVariable(String varName, int defaultValue) {
		if (!variables.containsKey(varName))
			return defaultValue;

		Object o = variables.get(varName);
		if (o instanceof Integer) return (Integer)o;
		try {
			if (o instanceof String) {
				return Integer.parseInt((String)o);
			}
		} catch (NumberFormatException e) {
			logger.log(Level.ERROR, "Telnet variable {0} is not a valid integer: {1}", varName, o);
		}
		return defaultValue;
	}

	//-------------------------------------------------------------------
	public Object getObjectVariable(String varName) {
		return variables.get(varName);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#setListenerVariable(com.graphicmud.network.ClientConnectionListener, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setListenerVariable(ClientConnectionListener handler, String key, Object value) {
		Map<String,Object> map = handlerVariables.getOrDefault(handler, new HashMap<String,Object>());
		map.put(key, value);
		handlerVariables.put(handler, map);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#getListenerVariable(com.graphicmud.network.ClientConnectionListener, java.lang.String)
	 */
	@Override
	public <E> E getListenerVariable(ClientConnectionListener handler, String key) {
		Map<String,Object> map = handlerVariables.getOrDefault(handler, new HashMap<String,Object>());
		return (E) map.get(key);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#getProtocolCapabilities()
	 */
	public LinkedHashMap<String, List<String>> getProtocolCapabilities() {
		return new LinkedHashMap<>();
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#getCapabilities()
	 */
	@Override
	public MUDClientCapabilities getCapabilities() {
		return capabilities;
	}

	//-------------------------------------------------------------------
	public void setDoNotDisturb(boolean state) {
		this.doNotDisturb = state;
	}

	//-------------------------------------------------------------------
	public boolean isDoNotDisturb() {
		return doNotDisturb;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#sendPromptWithStats(com.graphicmud.game.Vital[])
	 */
	@Override
	public void sendPromptWithStats(Map<VitalType, Vital> vitals) {
		StringBuilder buf = new StringBuilder("\r\n");
		Game game = MUD.getInstance().getGame();
		buf.append(game.getVitalName(VitalType.VITAL1)[1]+":"+vitals.get(VitalType.VITAL1).getCurrent());
		buf.append("  ");
		buf.append(game.getVitalName(VitalType.VITAL2)[1]+":"+vitals.get(VitalType.VITAL2).getCurrent());
		buf.append("  ");
		buf.append(game.getVitalName(VitalType.VITAL3)[1]+":"+vitals.get(VitalType.VITAL3).getCurrent());
		buf.append("> ");
		sendPrompt(buf.toString());

	}

}
