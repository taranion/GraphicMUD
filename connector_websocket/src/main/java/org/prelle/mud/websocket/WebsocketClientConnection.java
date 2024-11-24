package org.prelle.mud.websocket;

import java.io.IOException;
import java.lang.System.Logger.Level;
import java.net.InetAddress;

import com.graphicmud.commands.CommunicationChannel;
import com.graphicmud.dialog.DialogueTree;
import com.graphicmud.handler.LoginHandler;
import com.graphicmud.map.ViewportMap;
import com.graphicmud.network.AClientConnection;
import com.graphicmud.network.ClientConnection;
import com.graphicmud.network.ClientConnectionState;
import com.graphicmud.network.ConnectionVariables;
import com.graphicmud.network.MUDConnector;
import com.graphicmud.network.interaction.Form;
import com.graphicmud.network.interaction.Menu;
import com.graphicmud.network.interaction.Table;
import com.graphicmud.network.interaction.VisualizedMenu;
import com.graphicmud.symbol.Symbol;
import com.graphicmud.world.Surrounding;

import robaho.net.httpserver.websockets.WebSocket;

/**
 *
 */
public class WebsocketClientConnection extends AClientConnection implements ClientConnection, ConnectionVariables {

	private WebsocketConnector connector;
	private WebSocket socket;

	//-------------------------------------------------------------------
	public WebsocketClientConnection(WebSocket socket, WebsocketConnector connector) {
		this.connector = connector;
		this.socket    = socket;
//		logger = System.getLogger("network.client."+user.username());
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#getConnector()
	 */
	@Override
	public MUDConnector getConnector() {
		return connector;
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
	 * @see com.graphicmud.network.ClientConnection#getNetworkId()
	 */
	@Override
	public String getNetworkId() {
		InetAddress addr = getVariable(VAR_IPADDRESS);
		if (addr==null) return "-";
		return addr.getHostAddress();
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#getClient()
	 */
	@Override
	public String getClient() {
		return "WebSocket";
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#getTerminalSize()
	 */
	@Override
	public String getTerminalSize() {
		return "unknown";
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#suppressEcho(boolean)
	 */
	@Override
	public void suppressEcho(boolean value) {
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#sendScreen(java.lang.String)
	 */
	@Override
	public void sendScreen(String data) {
		logger.log(Level.DEBUG, "SND {0}", data);
		if (data.length()>1989)
			data = data.substring(0,1989);
		data = "```\n"+data+"```";
//		channel.sendMessage(data).queue();

//		SendMessage request = new SendMessage(chatID, data)
//		        .parseMode(ParseMode.HTML)
//		        .disableWebPagePreview(true)
//		        .disableNotification(true);
//
//		// sync
//		SendResponse sendResponse = bot.execute(request);
//		boolean ok = sendResponse.isOk();
//		logger.log(Level.DEBUG, "Send is ok={0}", ok);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#sendPrompt(java.lang.String)
	 */
	@Override
	public void sendPrompt(String prompt) {
//		logger.log(Level.DEBUG, "SND {0}", prompt);
//		SendMessage request = new SendMessage(chatID, prompt)
//		        .parseMode(ParseMode.Markdown)
//		        .disableWebPagePreview(true)
//		        .disableNotification(true);
//
//		// sync
//		SendResponse sendResponse = bot.execute(request);
//		boolean ok = sendResponse.isOk();
//		logger.log(Level.INFO, "Send is ok={0}   response was {1}", ok, sendResponse);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#sendShortText(java.lang.String)
	 */
	@Override
	public void sendShortText(Priority prio, String prompt) {
		logger.log(Level.DEBUG, "SND {0}", prompt);
//		SendMessage request = new SendMessage(chatID, prompt)
//		        .parseMode(ParseMode.Markdown)
//		        .disableWebPagePreview(true)
//		        .disableNotification(true);
//
//		SendResponse sendResponse = bot.execute(request);
//		if (!sendResponse.isOk()) {
//			setState(ClientConnectionState.DISCONNECTED);
//		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#sendTextWithMarkup(java.lang.String)
	 */
	@Override
	public void sendTextWithMarkup(String text) {
		if (state==ClientConnectionState.DISCONNECTED) return;
		int cols = getNumericVariable(VAR_COLUMNS, 80);
//		text = FormatUtil.convertTextBlock( MarkupParser.convertText(text), cols);
		logger.log(Level.INFO, "TODO: SEND "+text);
//		byte[] buf = (text).getBytes(charset);
//		try {
//			out.write(buf);
//			if (socket.isFeatureActive(TelnetOption.EOR.getCode())) {
//				out.write(TelnetConstants.IAC);
//				out.write(EOR);
//			} else {
//				out.write("\r\n");
//			}
//		} catch (IOException e) {
//			logger.log(Level.WARNING, "IO-Error sending",e);
//			setState(ClientConnectionState.DISCONNECTED);
//		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#sendMap()
	 */
	@Override
	public void sendMap(ViewportMap<Symbol> mapData) {
		logger.log(Level.DEBUG, "Sending maps not supported");
	}

	//-------------------------------------------------------------------
	public void receivedFromTelegram(String text) {
		// TODO Auto-generated method stub
		Thread.ofVirtual().start( () -> {
			logger.log(Level.DEBUG, "RCV "+text);
			if (listener!=null)
				listener.peek().receivedInput(this, text);
		});

	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#getCapabilityString()
	 */
	@Override
	public String getCapabilityString() {
		return "To Do";
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#logOut()
	 */
	@Override
	public void logOut() {
		logger.log(Level.INFO, "logOut");
	}

	@Override
	public void initializeInterface() {
		// TODO Auto-generated method stub
		logger.log(Level.INFO, "initializeInterface with listener "+listener);
	}

	@Override
	public void sendImage(byte[] data, String filename, int width, int height) {
		// TODO Auto-generated method stub
		logger.log(Level.INFO, "sendImage");

	}

	@Override
	public void sendRoom(Surrounding room) {
		logger.log(Level.INFO, "ToDo: Convert map to image");
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#presentMenu(com.graphicmud.network.interaction.Menu)
	 */
	@Override
	public VisualizedMenu presentMenu(Menu menu) {
		logger.log(Level.INFO, "ToDo: presentMenu");
		return null;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#presentForm(com.graphicmud.network.interaction.Form)
	 */
	@Override
	public void presentForm(Form form) {
		logger.log(Level.INFO, "ToDo: presentForm");

	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#presentTable(com.graphicmud.network.interaction.Table)
	 */
	@Override
	public <E> void presentTable(Table<E> table) {
//		try {
//			format.sendTable(table);
//		} catch (IOException e) {
//			logger.log(Level.WARNING, "IO-Error sending",e);
//			setState(ClientConnectionState.DISCONNECTED);
//		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#presentDialog(com.graphicmud.dialog.DialogueTree, java.lang.String)
	 */
	@Override
	public void presentDialog(DialogueTree tree, String image) {
//		try {
//			logger.log(Level.INFO, "Call "+format.getClass().getSimpleName()+".sendDialog");
//			format.sendDialog(tree, image);
//		} catch (IOException e) {
//			logger.log(Level.WARNING, "IO-Error sending",e);
//			setState(ClientConnectionState.DISCONNECTED);
//		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#sendOnChannel(com.graphicmud.commands.CommunicationChannel, java.lang.String)
	 */
	@Override
	public void sendOnChannel(CommunicationChannel channel, String text) {
		if (isDoNotDisturb()) {
			logger.log(Level.WARNING, "ToDo: Store messages in DO_NOT_DISTURB");
			return;
		}

		switch (channel) {
		case GOSSIP:
			sendTextWithMarkup("<yellow>"+text+"</yellow>");
			break;
		case AUCTION:
			sendTextWithMarkup("<blue>"+text+"</blue>");
			break;
		case PRAY:
			sendTextWithMarkup("<cyan>"+text+"</cyan>");
			break;
		case SAY:
			sendTextWithMarkup(text);
			break;
		default:
			sendTextWithMarkup(text);
		}
	}

}
