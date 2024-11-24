/**
 *
 */
package com.graphicmud.network;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.graphicmud.commands.CommunicationChannel;
import com.graphicmud.dialog.DialogueTree;
import com.graphicmud.game.Vital;
import com.graphicmud.game.Vital.VitalType;
import com.graphicmud.map.ViewportMap;
import com.graphicmud.media.SoundClient;
import com.graphicmud.network.interaction.Form;
import com.graphicmud.network.interaction.Menu;
import com.graphicmud.network.interaction.MenuItem;
import com.graphicmud.network.interaction.Table;
import com.graphicmud.network.interaction.VisualizedMenu;
import com.graphicmud.player.PlayerAccount;
import com.graphicmud.player.PlayerCharacter;
import com.graphicmud.symbol.Symbol;
import com.graphicmud.world.Surrounding;

/**
 * This is the common class for all connections to a player client
 */
public interface ClientConnection {

	public static enum Priority {
		/** Not sent or cached when player is busy */
		UNIMPORTANT,
		/** Not sent, but cached when player is busy */
		PERSONAL,
		/** Always delivered */
		IMMEDIATE
	}

	public Locale getLocale();
	public MUDConnector getConnector();
	public String getAccount();
	public PlayerAccount getPlayer();
	public void setPlayer(PlayerAccount value);

	public PlayerCharacter getCharacter();
	public void setCharacter(PlayerCharacter value);

	//-------------------------------------------------------------------
	public void logOut();

	//-------------------------------------------------------------------
	/**
	 * Return an identifier that defines the origin in the source network
	 * @return
	 */
	public String getNetworkId();
	public String getClient();
	public String getTerminalSize();
	public String getCapabilityString();
	public LinkedHashMap<String, List<String>> getProtocolCapabilities();

	public ClientConnectionState getState();
	public void setState(ClientConnectionState value);

	//-------------------------------------------------------------------
	/**
	 * Add a new handler on the stack and call the initialize method
	 * @param handler
	 * @param withInit
	 */
	public void pushConnectionListener(ClientConnectionListener handler);
	ClientConnectionListener getClientConnectionListener();

	//-------------------------------------------------------------------
	/**
	 * Switch back to the previous connection listener - if there is any.
	 * If there was a previous handler, call the reentry() method
	 * @return
	 */
	public ClientConnectionListener popConnectionListener(Object returnValue);

	public void storeMenuItemData(Menu menu, MenuItem<?> tmp, Object data);
	public <V> V retrieveMenuItemData(Menu menu, MenuItem<?> tmp);
	public void removeMenuItem(Menu menu, MenuItem<?> item);

	public void setVariable(String key, Object value);
	public <E> E getVariable(String key);
	public void clearVariable(String key);
	public List<String> getVariableNames();
	public void setListenerVariable(ClientConnectionListener handler, String key, Object value);
	public <E> E getListenerVariable(ClientConnectionListener handler, String key);

	public void sendRoom(Surrounding room);

	//public void sendDescription(Location loc);
	public void sendScreen(String data);

	public void sendPrompt(String prompt);
	public void sendPromptWithStats(Map<VitalType, Vital> vitals);
	public void sendShortText(Priority prio, String prompt);
	public void sendTextWithMarkup(String text);
	public void sendOnChannel(CommunicationChannel channel, String text);

	public void initializeInterface();

	public void sendMap(ViewportMap<Symbol> mapData);
	public void sendImage(byte[] data, String filename, int width, int height);

	public VisualizedMenu presentMenu(Menu menu);
	public void presentForm(Form form);
	public <E >void presentTable(Table<E> table);
	public void presentDialog(DialogueTree tree, String image);
	public void suppressEcho(boolean value);

	public SoundClient getSoundClient();

	public MUDClientCapabilities getCapabilities();

	public void setDoNotDisturb(boolean state);
	public boolean isDoNotDisturb();

}
