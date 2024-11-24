package org.prelle.mud.discord;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.graphicmud.handler.PlayHandler;
import com.graphicmud.network.ConnectionVariables;
import com.graphicmud.network.MUDConnector;
import com.graphicmud.network.MUDConnectorListener;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;

/**
 *
 */
public class TelegramConnector implements MUDConnector, UpdatesListener, ConnectionVariables {

	private final static Logger logger = System.getLogger("network.discord");

	private String token;
	private TelegramBot bot;
	private MUDConnectorListener handler;

	/** Indexed via User.id */
	private Map<String, TelegramClientConnection> connections = new HashMap<>();

	//-------------------------------------------------------------------
	public static TelegramConnectorBuilder builder() {
		return new TelegramConnectorBuilder();
	}

	//-------------------------------------------------------------------
	private TelegramConnector(String token) {
		this.token = token;
	}

	//-------------------------------------------------------------------
	public String getProtocolIdentifier() {
		return MUDConnector.PROTO_TELEGRAM;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.MUDConnector#getName()
	 */
	@Override
	public String getName() {
		return "Telegram";
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.MUDConnector#start(com.graphicmud.network.MUDConnectorListener)
	 */
	@Override
	public void start(MUDConnectorListener listener) throws IOException {
		this.handler = listener;

		bot = new TelegramBot(token);
		logger.log(Level.DEBUG, "Bot built");
			bot.setUpdatesListener(this, e -> {
				logger.log(Level.WARNING, "Error {0}",e);
			    if (e.response() != null) {
			        // got bad response from telegram
			        e.response().errorCode();
			        e.response().description();
			    } else {
			        // probably network error
			        e.printStackTrace();
			    }
			});
			logger.log(Level.DEBUG, "Bot started");
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.MUDConnector#stop()
	 */
	@Override
	public void stop() {
		bot.shutdown();
	}

	public static class TelegramConnectorBuilder {

		private String token;

		//-------------------------------------------------------------------
		public TelegramConnectorBuilder withToken(String value) {
			this.token = value;
			return this;
		}

		public TelegramConnector build() {
			return new TelegramConnector(token);
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.pengrad.telegrambot.UpdatesListener#process(java.util.List)
	 */
	@Override
	public int process(List<Update> updates) {
		logger.log(Level.INFO, "Received {0} updates", updates.size());

		for (Update tmp : updates) {
			logger.log(Level.INFO, "RCV from {0}: {1}", tmp.message(), tmp);
			Message mess = tmp.message();
			User user = mess.from();
			//logger.log(Level.INFO, "  message was "+mess);
			logger.log(Level.INFO, "  user was "+user);
		    String userID = user.username();
		    TelegramClientConnection con = connections.get(userID);
		    if (con==null) {
		    	con = new TelegramClientConnection(this, bot, user, mess.chat().id());
		    	con.setVariable(VAR_LOGIN, user.username());
		    	con.setVariable(VAR_LANG , user.languageCode());
		    	con.setVariable("Fullname", user.firstName()+" "+user.lastName());
		    	connections.put(userID, con);
		    	if (handler!=null) {
		    		handler.incomingConnection(con);
		    	}
		    	con.pushConnectionListener(new PlayHandler());
		    } else
		    	con.receivedFromTelegram(mess.text());

		}

	    // return id of last processed update or confirm them all
	    return UpdatesListener.CONFIRMED_UPDATES_ALL;
	}
}
