package org.prelle.mud.discord;

import java.lang.System.Logger.Level;
import java.util.HashMap;

import com.graphicmud.commands.CommunicationChannel;
import com.graphicmud.dialog.DialogueTree;
import com.graphicmud.handler.LoginHandler;
import com.graphicmud.map.ViewportMap;
import com.graphicmud.network.AClientConnection;
import com.graphicmud.network.ClientConnection;
import com.graphicmud.network.ClientConnectionState;
import com.graphicmud.network.MUDConnector;
import com.graphicmud.network.interaction.Form;
import com.graphicmud.network.interaction.Menu;
import com.graphicmud.network.interaction.Table;
import com.graphicmud.network.interaction.VisualizedMenu;
import com.graphicmud.symbol.Symbol;
import com.graphicmud.world.Surrounding;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.SendResponse;

/**
 *
 */
public class TelegramClientConnection extends AClientConnection implements ClientConnection {

	private TelegramConnector connector;
	private User discordUser;
	private TelegramBot bot;
	private Long chatID;

	//-------------------------------------------------------------------
	public TelegramClientConnection(TelegramConnector connector, TelegramBot bot, User user, Long chatId) {
		this.connector = connector;
		this.bot       = bot;
		this.discordUser = user;
		this.variables = new HashMap<String, Object>();
		logger = System.getLogger("network.client."+user.username());
		this.chatID = chatId;

//		logger.log(Level.DEBUG, "open private channel to {0}", user);
//		channel = user.openPrivateChannel().complete();
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
		return discordUser.username();
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#getClient()
	 */
	@Override
	public String getClient() {
		return "Telegram";
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
//		logger.log(Level.DEBUG, "SND {0}", data);
		if (data.length()>1989)
			data = data.substring(0,1989);
		data = "```\n"+data+"```";
//		channel.sendMessage(data).queue();

		SendMessage request = new SendMessage(chatID, data)
		        .parseMode(ParseMode.HTML)
		        .disableWebPagePreview(true)
		        .disableNotification(true);

		// sync
		SendResponse sendResponse = bot.execute(request);
		boolean ok = sendResponse.isOk();
		logger.log(Level.DEBUG, "Send is ok={0}", ok);
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
		SendMessage request = new SendMessage(chatID, prompt)
		        .parseMode(ParseMode.Markdown)
		        .disableWebPagePreview(true)
		        .disableNotification(true);

		SendResponse sendResponse = bot.execute(request);
		if (!sendResponse.isOk()) {
			setState(ClientConnectionState.DISCONNECTED);
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#sendOnChannel(com.graphicmud.commands.CommunicationChannel, java.lang.String)
	 */
	@Override
	public void sendOnChannel(CommunicationChannel channel, String text) {
		sendTextWithMarkup(text);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#sendTextWithMarkup(java.lang.String)
	 */
	@Override
	public void sendTextWithMarkup(String text) {
		sendShortText(Priority.IMMEDIATE, text);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#sendMap()
	 */
	@Override
	public void sendMap(ViewportMap<Symbol> mapData) {
		logger.log(Level.DEBUG, "Sending maps not supported");
		SendPhoto request2 = new SendPhoto(chatID, "Karte")
				.allowSendingWithoutReply(true)
				.disableNotification(true);
//		StringBuffer out = new StringBuffer();
//		for (int[] line : mapData) {
//			for (int x : line) {
//				char c = (char)(64+x);
//				out.append(c);
//			}
//			out.append("\r\n");
//		}
//
//		sendScreen(out.toString());
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
		sendShortText(Priority.IMMEDIATE, String.format(" ", room.getDescription()));
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
		logger.log(Level.ERROR, "TODO: presentTable");
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#presentDialog(com.graphicmud.dialog.DialogueTree, java.lang.String)
	 */
	@Override
	public void presentDialog(DialogueTree tree, String image) {
		logger.log(Level.ERROR, "TODO: presentDialog");
	}

}
