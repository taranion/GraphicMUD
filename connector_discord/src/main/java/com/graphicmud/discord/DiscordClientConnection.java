package com.graphicmud.discord;

import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.prelle.mudansi.MarkupElement;
import org.prelle.mudansi.MarkupParser;

import com.graphicmud.MUD;
import com.graphicmud.commands.CommunicationChannel;
import com.graphicmud.dialog.DialogueTree;
import com.graphicmud.map.ViewportMap;
import com.graphicmud.network.AClientConnection;
import com.graphicmud.network.ClientConnection;
import com.graphicmud.network.ClientConnectionListener;
import com.graphicmud.network.ConnectionVariables;
import com.graphicmud.network.MUDConnector;
import com.graphicmud.network.ClientConnection.Priority;
import com.graphicmud.network.interaction.Form;
import com.graphicmud.network.interaction.Menu;
import com.graphicmud.network.interaction.MenuItem;
import com.graphicmud.network.interaction.Table;
import com.graphicmud.network.interaction.TextField;
import com.graphicmud.network.interaction.VisualizedMenu;
import com.graphicmud.symbol.Symbol;
import com.graphicmud.symbol.SymbolManager;
import com.graphicmud.symbol.TileGraphicService;
import com.graphicmud.world.Surrounding;
import com.graphicmud.world.text.Direction;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;

/**
 *
 */
public class DiscordClientConnection extends AClientConnection implements ClientConnection {

	private DiscordConnector connector;
	private User discordUser;
	private JDA jda;
	private PrivateChannel channel;

	private transient GenericComponentInteractionCreateEvent eventToRespondTo;
	private Map<String, Form> forms = new HashMap<>();

	//-------------------------------------------------------------------
	public DiscordClientConnection(DiscordConnector connector, User user, String nickname) {
		this.connector = connector;
		this.discordUser = user;
		setVariable(ConnectionVariables.VAR_CONNECTION_ID, user.getId());
		setVariable(ConnectionVariables.VAR_LOGIN, nickname);
		logger = System.getLogger("network.client."+user.getName());

		logger.log(Level.DEBUG, "open private channel to {0}", user);
		channel = user.openPrivateChannel().complete();
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
	 * @see com.graphicmud.network.ClientConnection#getNetworkId()
	 */
	@Override
	public String getNetworkId() {
		return discordUser.getName();
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#getClient()
	 */
	@Override
	public String getClient() {
		return "Discord JDA";
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
	private void checkAcknowledge() {
		if (eventToRespondTo!=null && !eventToRespondTo.isAcknowledged()) {
			logger.log(Level.DEBUG, "remove buttons");
			eventToRespondTo.editComponents(List.of()).queue();
			//eventToRespondTo.deferReply().queue();
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#setConnectionListener(com.graphicmud.network.ClientConnectionListener)
	 */
	@Override
	public void pushConnectionListener(ClientConnectionListener handler) {
		super.pushConnectionListener(handler);
		checkAcknowledge();
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#sendScreen(java.lang.String)
	 */
	@Override
	public void sendScreen(String data) {
		checkAcknowledge();
		logger.log(Level.DEBUG, "SND {0}", data);
		if (data.length()>1989)
			data = data.substring(0,1989);
//		data = "```ansi\n"+data+"```";
		channel.sendMessage(data).queue();
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#sendPrompt(java.lang.String)
	 */
	@Override
	public void sendPrompt(String prompt) {
		logger.log(Level.DEBUG, "SND {0}", prompt);
		checkAcknowledge();
		//channel.sendMessage(prompt).complete();
	}

	//-------------------------------------------------------------------
	@Override
	public void sendShortText(Priority prio, String prompt) {
		checkAcknowledge();
		logger.log(Level.DEBUG, "SND {0}", prompt);
		channel.sendMessage(prompt).complete();
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#sendTextWithMarkup(java.lang.Stringn)
	 */
	@Override
	public void sendTextWithMarkup(String text) {
		checkAcknowledge();
		text = convertText( MarkupParser.convertText(text));
		channel.sendMessage(text).complete();
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
	 * @see com.graphicmud.network.ClientConnection#sendMap()
	 */
	@Override
	public void sendMap(ViewportMap<Symbol> mapData) {
		checkAcknowledge();

		SymbolManager symbols = MUD.getInstance().getSymbolManager();
		TileGraphicService tileService = symbols.getTileGraphicService();
		if (tileService==null) {
			return;
		}

		byte[] data = tileService.renderMap(mapData, symbols.getSymbolSet(4));

//		try {
//			FileOutputStream fout = new FileOutputStream("/tmp/map.png");
//			fout.write(data);
//			fout.flush();
//			fout.close();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		channel.sendFiles(FileUpload.fromData(data, "map.png")).queue();

		EmbedBuilder embed = new EmbedBuilder();
		embed.setImage("attachment://map.png");
		channel.sendMessageEmbeds(embed.build())
			.addFiles(FileUpload.fromData(data, "map.png"))
			.queue();
//		channel.sendFiles(FileUpload.fromData(data, "map.png")).addEmbeds(embed.build()).queue();

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
	public void receivedFromDiscord(String text) {
		// TODO Auto-generated method stub
		Thread.ofVirtual().start( () -> {
			logger.log(Level.DEBUG, "RCV "+text);
			if (listener!=null)
				listener.peek().receivedInput(this, text);
		});

	}

	//-------------------------------------------------------------------
	void onButtonInteraction(ButtonInteractionEvent event) {
		logger.log(Level.DEBUG, "onButtonInteraction {0}", event.getButton());
		logger.log(Level.DEBUG, "message was "+event.getMessageId());
		logger.log(Level.DEBUG, "message was "+event.getButton().getId()+" /" +event.getButton().getLabel());
		eventToRespondTo = event;

//		logger.log(Level.DEBUG, "remove buttons");
//		event.editComponents(List.of()).queue();
//		event.deferReply().queue();

	    switch (event.getButton().getId()) {
	    case "arrowUp": listener.peek().receivedInput(this, "mapNorth"); break;
	    case "arrowDown": listener.peek().receivedInput(this, "mapSouth"); break;
	    case "arrowLeft": listener.peek().receivedInput(this, "mapWest"); break;
	    case "arrowRight": listener.peek().receivedInput(this, "mapEast"); break;
	    default:
	    	listener.peek().receivedInput(this, event.getButton().getId());
	    }
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
		logger.log(Level.WARNING, "Can we do anything in case of log-off");
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#suppressEcho(boolean)
	 */
	@Override
	public void suppressEcho(boolean value) {
	}

	@Override
	public void initializeInterface() {
		// TODO Auto-generated method stub

	}

	//-------------------------------------------------------------------
	private static String convertText(List<MarkupElement> markup) {
		StringBuffer buf = new StringBuffer();

		for (MarkupElement tmp : markup) {

			switch (tmp.getType()) {
			case TEXT: buf.append(tmp.getText()); buf.append(' '); break;
			case SPACING: buf.append(' '); break;
			case FLOW:
				if (tmp.getText().equals("br")) {
					buf.append("\r\n");
				}
				break;
			case STYLE:
				switch (tmp.getText()) {
				case "b": buf.append("**"); break;
				case "i": buf.append("*"); break;
				case "u": buf.append("__"); break;
				}
			case COLOR:
				break;
			default:
			}
		}

		return buf.toString();
	}

	@Override
	public void sendImage(byte[] data, String filename, int width, int height) {
		logger.log(Level.DEBUG, "sendImage {0}",filename);
		EmbedBuilder builder = new EmbedBuilder();
		builder.setImage("attachment://"+filename);
		channel.sendMessageEmbeds(builder.build())
			.addFiles(FileUpload.fromData(data, filename))
			.queue();

	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#sendRoom(com.graphicmud.world.Surrounding)
	 */
	@Override
	public void sendRoom(Surrounding room) {
		List<Button> buttons = new ArrayList<>();
		if (room.getDirections()!=null && !room.getDirections().isEmpty()) {
			for (Direction dir : room.getDirections()) {
				buttons.add(Button.of(ButtonStyle.PRIMARY, dir.name().toLowerCase(),dir.name()));
			}
		}

		String title = convertText(room.getTitle());
		String roomDesc = convertText(room.getDescription());
		MessageCreateAction create = channel.sendMessage(title+"\r\n"+roomDesc);
		if (!buttons.isEmpty())
			create.addActionRow(ActionRow.of(buttons).getActionComponents());
////		.addActionRow(movements.getActionComponents())

		if (room.getMap()!=null) {
			SymbolManager symbols = MUD.getInstance().getSymbolManager();
			TileGraphicService tileService = symbols.getTileGraphicService();
			if (tileService!=null) {
				byte[] mapImage = tileService.renderMap(room.getMap(), symbols.getSymbolSet(4));
				EmbedBuilder embed = new EmbedBuilder();
				embed.setImage("attachment://map.png");
				create.addEmbeds(embed.build())
					.addFiles(FileUpload.fromData(mapImage, "map.png"));
			}
		}

		create.queue();
	}

//	@Override
//	public void sendRoom(Surrounding room) {
//		checkAcknowledge();
//
//		// If there is an mood picture, send it now
//		InputStream ins = ClassLoader.getSystemResourceAsStream("static/PixelArt1.png");
//		logger.log(Level.DEBUG, "Send Image stream "+ins);
//		try {
//			sendImage(ins.readAllBytes(), "Test.png", 640, 359);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//
//
//		logger.log(Level.INFO, "ToDo: Convert map to image");
//
//		String data = room.getDescription();
//		if (data.length()>1989)
//			data = data.substring(0,1989);
//		//data = "```ansi\n"+data+"```";
//
//
//		ActionRow directions = ActionRow.of(
//				Button.of(ButtonStyle.PRIMARY, "north","North"),
//				Button.of(ButtonStyle.PRIMARY, "south","South"),
//				Button.of(ButtonStyle.PRIMARY, "west","West"),
//				Button.of(ButtonStyle.PRIMARY, "east","East")
//				//Button.danger("exit" , "Stop playing")
//				);
//		ActionRow movements = ActionRow.of(
//				Button.of(ButtonStyle.SECONDARY, "arrowUp",Emoji.fromUnicode("\u2b06")),
//				Button.of(ButtonStyle.SECONDARY, "arrowDown",Emoji.fromUnicode("\u2B07")),
//				Button.of(ButtonStyle.SECONDARY, "arrowLeft",Emoji.fromUnicode("\u2B05")),
//				Button.of(ButtonStyle.SECONDARY, "arrowRight",Emoji.fromUnicode("➡️")),
//				Button.of(ButtonStyle.SECONDARY, "look", "Look")
//				);
//		logger.log(Level.DEBUG, "sendRoom");
//		channel.sendMessage(data)
//		.addActionRow(directions.getActionComponents())
////		.addActionRow(movements.getActionComponents())
//		.complete();
//	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#presentMenu(com.graphicmud.network.interaction.Menu)
	 */
	@Override
	public VisualizedMenu presentMenu(Menu menu) {
		List<ActionComponent> buttons = new ArrayList<>();
		for (MenuItem item : menu.getItems()) {
			Button btn = Button.of(ButtonStyle.PRIMARY, item.getIdentifier(), item.getLabel());
			if (item.getEmoji()!=null)
				btn = Button.of(ButtonStyle.PRIMARY, item.getIdentifier(), item.getLabel(), Emoji.fromUnicode(item.getEmoji()));
			buttons.add(btn);
		}
		List<ActionRow> rows = ActionRow.partitionOf(buttons);

		MessageCreateAction req = channel.sendMessage(menu.getMessage());
		for (ActionRow row : rows) {
			req.addActionRow(row.getActionComponents());
		}
		req.queue();
		// TODO
		return null;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#presentForm(com.graphicmud.network.interaction.Form)
	 */
	@Override
	public void presentForm(Form form) {
		List<ActionRow> rows = new ArrayList<>();
		for (TextField tf : form.getItems()) {
			TextInput.Builder inputBuilder = TextInput.create(tf.getId(), tf.getName(), TextInputStyle.SHORT);
			if (tf.getPlaceholder()!=null) inputBuilder.setPlaceholder(tf.getPlaceholder());
			if (tf.getMinLength()>0) inputBuilder.setMinLength(tf.getMinLength());
			if (tf.getMaxLength()>0) inputBuilder.setMaxLength(tf.getMaxLength());
			rows.add(ActionRow.of(inputBuilder.build()));
		}

	    Modal modal = Modal.create(form.getId(), form.getTitle())
	    		.addComponents(rows)
	    		.build();

	    forms.put(form.getId(), form);
	    eventToRespondTo.replyModal(modal).queue();

	}

	//-------------------------------------------------------------------
	void onModalInteraction(ModalInteractionEvent event) {
		Form form = forms.get(event.getModalId());
		if (form==null) {
			logger.log(Level.WARNING, "Form {0} was unexpected - ignore it", event.getModalId());
			return;
		}
		forms.remove(event.getModalId());

		Map<String,String> answers = new HashMap<>();
		for (ModalMapping mapping : event.getValues()) {
			logger.log(Level.DEBUG, "Value "+mapping);
			answers.put(mapping.getId(), mapping.getAsString());
		}

		event.deferEdit().queue();
		listener.peek().receivedFormResponse(this, form, answers);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#presentTable(com.graphicmud.network.interaction.Table)
	 */
	@Override
	public <E> void presentTable(Table<E> table) {
		// TODO Auto-generated method stub
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
