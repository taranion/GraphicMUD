package com.graphicmud.discord;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.graphicmud.network.MUDConnector;
import com.graphicmud.network.MUDConnectorListener;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

/**
 *
 */
public class DiscordConnector extends ListenerAdapter implements MUDConnector {

	private final static Logger logger = System.getLogger("network.discord");

	private String token;
	private JDA jda;
	private MUDConnectorListener handler;

	/** Indexed via User.id */
	private Map<String, DiscordClientConnection> connections = new HashMap<>();

	//-------------------------------------------------------------------
	public static DiscordConnectorBuilder builder() {
		return new DiscordConnectorBuilder();
	}

	//-------------------------------------------------------------------
	private DiscordConnector(String token) {
		this.token = token;
	}

	//-------------------------------------------------------------------
	public String getProtocolIdentifier() {
		return MUDConnector.PROTO_DISCORD;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.MUDConnector#getName()
	 */
	@Override
	public String getName() {
		return "Discord";
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.MUDConnector#start(com.graphicmud.network.MUDConnectorListener)
	 */
	@Override
	public void start(MUDConnectorListener listener) throws IOException {
		this.handler = listener;

		jda = JDABuilder.createLight(token,
				GatewayIntent.DIRECT_MESSAGES,
				GatewayIntent.GUILD_MESSAGES)
			.addEventListeners(this)
			.setActivity(Activity.customStatus("is investigating"))
			.build();
		logger.log(Level.DEBUG, "JDA built");
		try {
			jda.awaitReady();
			System.out.println("ready");
			logger.log(Level.DEBUG, "Bot started");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		jda.upsertCommand("sup", "Sup wassup to someone").queue();
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.MUDConnector#stop()
	 */
	@Override
	public void stop() {
		jda.shutdown();

	}

	//-------------------------------------------------------------------
	/**
	 * @see net.dv8tion.jda.api.hooks.ListenerAdapter#onMessageReceived(net.dv8tion.jda.api.events.message.MessageReceivedEvent)
	 */
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getAuthor() instanceof SelfUser) {
			return;
		}
		logger.log(Level.DEBUG, "RCV from {0}: {1}", event.getAuthor(), event.getMessage().getContentDisplay());
		logger.log(Level.DEBUG, "Effective name {0}", event.getAuthor().getEffectiveName());
		logger.log(Level.DEBUG, "Name {0}", event.getAuthor().getName());
		logger.log(Level.DEBUG, "Global Name {0}", event.getAuthor().getGlobalName());
	    String userID = event.getAuthor().getId();
	    // Check for an existing conenction
	    DiscordClientConnection con = connections.get(userID);
	    if (con==null) {
	    	con = new DiscordClientConnection(this, event.getAuthor(), event.getAuthor().getGlobalName());
	    	connections.put(userID, con);
	    	if (handler!=null) {
	    		handler.incomingConnection(con);
	    	}
	    } else
	    	con.receivedFromDiscord(event.getMessage().getContentDisplay());

	}

	//-------------------------------------------------------------------
	/**
	 * @see net.dv8tion.jda.api.hooks.ListenerAdapter#onButtonInteraction(net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent)
	 */
	@Override
	public void onButtonInteraction(ButtonInteractionEvent event) {
	    String userID = event.getUser().getId();
	    DiscordClientConnection con = connections.get(userID);
	    if (con==null) {
	    	con = new DiscordClientConnection(this, event.getUser(), event.getMember().getNickname());
	    	connections.put(userID, con);
	    	if (handler!=null) {
	    		handler.incomingConnection(con);
	    	}
	    }
	    final DiscordClientConnection con2 = con;
		Thread.ofVirtual().start( () -> con2.onButtonInteraction(event));
	}

	//-------------------------------------------------------------------
	/**
	 * @see net.dv8tion.jda.api.hooks.ListenerAdapter#onModalInteraction(net.dv8tion.jda.api.events.interaction.ModalInteractionEvent)
	 */
	@Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
		logger.log(Level.WARNING, "onModalInteraction {0}",event.getModalId());

	    String userID = event.getUser().getId();
	    // Check for an existing conenction
	    DiscordClientConnection con = connections.get(userID);
	    if (con==null) {
	    	logger.log(Level.WARNING, "Received ModalInteractionEvent {0} for unknown user connection - ignore it",event.getModalId());
	    	return;
	    }

	    con.onModalInteraction(event);
//	    final DiscordClientConnection con2 = con;
//		Thread.ofVirtual().start( () -> con2.onModalInteraction(event));
 	}

	public static class DiscordConnectorBuilder {

		private String token;

		//-------------------------------------------------------------------
		public DiscordConnectorBuilder withToken(String value) {
			this.token = value;
			return this;
		}

		public DiscordConnector build() {
			return new DiscordConnector(token);
		}
	}
}
