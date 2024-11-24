package com.graphicmud.telnet.gmcp;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.prelle.mud4j.gmcp.Char.Vitals;
import org.prelle.mud4j.gmcp.CharLogin.Credentials;
import org.prelle.mud4j.gmcp.CharLogin.Result;
import org.prelle.mud4j.gmcp.Client.ClientMediaPlay;
import org.prelle.mud4j.gmcp.Core.Hello;
import org.prelle.mud4j.gmcp.Core.Ping;
import org.prelle.mud4j.gmcp.Core.SupportsSet;
import org.prelle.mud4j.gmcp.Room.GMCPRoomInfo;
import org.prelle.mud4j.gmcp.Room.RoomPackage;
import org.prelle.mud4j.gmcp.beip.BeipTilemapDef;
import org.prelle.mud4j.gmcp.beip.BeipTilemapInfo;
import org.prelle.mudansi.FormatUtil;
import org.prelle.telnet.mud.GenericMUDCommunicationProtocol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.graphicmud.Localization;
import com.graphicmud.MUD;
import com.graphicmud.commands.WhoCommand;
import com.graphicmud.map.ViewportMap;
import com.graphicmud.network.ConnectionVariables;
import com.graphicmud.player.PlayerAccount;
import com.graphicmud.player.PlayerCharacter;
import com.graphicmud.symbol.Symbol;
import com.graphicmud.telnet.impl.GMCPSoundClient;
import com.graphicmud.telnet.impl.TelnetClientConnection;
import com.graphicmud.world.Surrounding;
import com.graphicmud.world.text.Direction;
import com.graphicmud.world.text.Exit;

/**
 *
 */
public class GMCP implements ConnectionVariables {

	private final static Logger logger = System.getLogger(GMCP.class.getPackageName());

	public final static String PACKAGE_CHAR = "Char";
	public final static String PACKAGE_CHAR_SKILLS = "Char.Skills";
	public final static String PACKAGE_CHAR_ITEMS  = "Char.Items";
	public final static String PACKAGE_CHAR_LOGIN  = "Char.Login";
	public final static String PACKAGE_CLIENT_MEDIA = "Client.Media";
	public final static String PACKAGE_COMM_CHANNEL = "Comm.Channel";
	public final static String PACKAGE_EXTERNAL_DISCORD = "External.Discord";
	public final static String PACKAGE_REDIRECT = "Redirect";
	public final static String PACKAGE_ROOM = "Room";

	private static Gson gson;

	//-------------------------------------------------------------------
	static {
		prepareGson();
	}

	//-------------------------------------------------------------------
	private static void prepareGson() {
		class LocalDateAdapter extends TypeAdapter<LocalDateTime> {
		    @Override
		    public void write(final JsonWriter jsonWriter, final LocalDateTime localDate) throws IOException {
		        if (localDate == null) {
		            jsonWriter.nullValue();
		        } else {
		            jsonWriter.value(localDate.toString());
		        }
		    }

		    @Override
		    public LocalDateTime read(final JsonReader jsonReader) throws IOException {
		        if (jsonReader.peek() == JsonToken.NULL) {
		            jsonReader.nextNull();
		            return null;
		        } else {
		            return LocalDateTime.parse(jsonReader.nextString());
		        }
		    }
		}

		gson = (new GsonBuilder())
				//.setPrettyPrinting()
				.registerTypeAdapter(LocalDateTime.class, new LocalDateAdapter())
				.create();
	}


	//-------------------------------------------------------------------
	private static void sendGMCP(TelnetClientConnection nvt, String packName, String command) throws IOException {
		GenericMUDCommunicationProtocol.send(nvt.getTelnetOutputStream(), packName, command);
	}

	//-------------------------------------------------------------------
	public static void gmcpEvaluateSupportedSet(TelnetClientConnection nvt, SupportsSet message) {
		Map<String, Integer> packages = new HashMap<>();
		for (Entry<String,Integer> entry : message.getAsNameVersionList()) {
			packages.put(entry.getKey(), entry.getValue());
			nvt.getCapabilities().gmcpPackages.put(entry.getKey(), entry.getValue());

		}
		nvt.setVariable(VAR_GMCP_PACKAGES, packages);
		logger.log(Level.INFO, "Client supports {0}", packages.keySet());

		if (packages.containsKey(PACKAGE_CLIENT_MEDIA)) {
			logger.log(Level.INFO, "Set GMCP sound client");
			nvt.setSoundClient(new GMCPSoundClient(nvt));
		}


		if (!nvt.hasGMCPStarted()) {
			try {
				startGMCP(nvt);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @param nvt
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static void startGMCP(TelnetClientConnection nvt) throws IOException {
		Map<String, Integer> packages = (Map<String, Integer>) nvt.getObjectVariable(VAR_GMCP_PACKAGES);
		if (packages==null) {
			logger.log(Level.WARNING, "No GMCP packages known at this point");
			return;
		}
		if (packages.containsKey(PACKAGE_CHAR_LOGIN)) {
			logger.log(Level.DEBUG, "Indicate login option");
			GenericMUDCommunicationProtocol.send(nvt.getTelnetOutputStream(), "Char.Login.Default", "{\"type\": [\"password-credentials\"]}");
		}

		nvt.setGMCPStarted(true);
	}

	//-------------------------------------------------------------------
	public static void rcvCommand(TelnetClientConnection con, Object mess, String namespace) throws IOException {
		switch (mess) {
		case Credentials cred -> rcvCredentials(con,cred);
		case Ping p -> GMCP.sendPing(con);
		case Integer p -> GMCP.sendPing(con);
		case Hello hello -> {
			con.setVariable(ConnectionVariables.VAR_CLIENT, hello.getClient());
		}
		case SupportsSet supSet -> gmcpEvaluateSupportedSet(con, supSet);
		case String ip when namespace.equals("ire.misc.ip") -> {
			logger.log(Level.DEBUG, "Player IP behind proxy is "+ip);
			con.setVariable(ConnectionVariables.VAR_IPADDRESS, ip);
		}
		default -> {
			logger.log(Level.WARNING, "Received unsupported GMCP message ''{0}''", mess);
		}
		}
//		switch (namespace) {
//		case "Core.Ping":
//			GMCP.sendPing(con);
//			break;
//		case "Core.Supports.Set":
//			GMCP.gmcpEvaluateSupportedSet(con, message);
//			break;
//		case "Comm.Channel.Players":
//			GMCP.sendCommChannelPlayers(con);
//			break;
//		default:
//			logger.log(Level.WARNING, "Unsupported namespace ''{0}''", namespace);
//		}
	}

	//-------------------------------------------------------------------
	private static void rcvCredentials(TelnetClientConnection con, Credentials cred) {
		logger.log(Level.INFO, "Received credentials "+cred.getAccount()+"/"+cred.getPassword());
		PlayerAccount account = MUD.getInstance().getPlayerDatabase().authenticate(cred.getAccount(), cred.getPassword());
		Result result = null;
		if (account==null) {
			result = new Result(false, Localization.getString("loginhandler.mess.wrong_password", con.getLocale()));
		} else {
			result = new Result(true, "Welcome "+cred.getAccount());
			con.setPlayer(account);
			con.setVariable(VAR_LOGIN, cred.getAccount());
			logger.log(Level.INFO, "Successfully logged in via GMCP");
		}

		String toSend = gson.toJson(result);
		logger.log(Level.DEBUG, toSend);
		try {
			GenericMUDCommunicationProtocol.send(con.getTelnetOutputStream(), "Char.Login.Result", toSend);
		} catch (IOException e) {
			logger.log(Level.ERROR, "Error sending GMCP",e);
		}
	}


	//-------------------------------------------------------------------
	private static void sendPing(TelnetClientConnection nvt) throws IOException {
		GenericMUDCommunicationProtocol.send(nvt.getTelnetOutputStream(), "Core.Ping", null);
	}

	//-------------------------------------------------------------------
	private static void sendCommChannelPlayers(TelnetClientConnection nvt) throws IOException {
		List<PlayerCharacter> list =  WhoCommand.getVisiblePlayers(nvt.getCharacter());
		List<ChannelPlayer> chPlayers = new ArrayList<>();
		for (PlayerCharacter pc : list) {
			chPlayers.add( new ChannelPlayer(pc.getName()) );
		}

		String toSend = gson.toJson(chPlayers);
		logger.log(Level.DEBUG, toSend);
		GenericMUDCommunicationProtocol.send(nvt.getTelnetOutputStream(), "Comm.Channel.Players", toSend);
	}

	//-------------------------------------------------------------------
	public static void sendRoomInfo(TelnetClientConnection nvt, Surrounding room) throws IOException {
		GMCPRoomInfo info = new GMCPRoomInfo();
		// Minimal direction
		Map<String,Integer> exits = new HashMap<>();
		for (Direction dir : room.getDirections()) {
			exits.put(dir.name().charAt(0)+"", 0);
		}

		if (room.getLocation()!=null) {
			info.setNum(room.getLocation().getNr().getLocalAsNumber());
			if (room.getLocation().getRoomComponent().isPresent()) {
				// Better room exits
				exits.clear();
				for (Exit exit : room.getLocation().getRoomComponent().get().getExitList()) {
					exits.put(exit.getDirection().name().charAt(0)+"", exit.getTargetRoom().getLocalAsNumber());
				}
			}
		}
		info.setName(FormatUtil.convertTextBlock(room.getTitle(), 80));
		info.setDesc(FormatUtil.convertTextBlock(room.getDescription(), 80));
		RoomPackage.sendInfo(nvt.getTelnetOutputStream(), info);
	}

	//-------------------------------------------------------------------
	public static void sendBeipMapDefinition(TelnetClientConnection nvt, String name, String url, int tileWidth, int tileHeight, int w, int h) throws IOException {
		BeipTilemapDef def = new BeipTilemapDef();
		def.tileUrl=url;
		def.tileSize=tileWidth+","+tileHeight;
		def.mapSize=w+","+h;
		def.encoding="Hex_8";
		BeipTilemapInfo map = new BeipTilemapInfo();
		map.put(name, def);
		String toSend = gson.toJson(map);
		logger.log(Level.DEBUG, toSend);
		GenericMUDCommunicationProtocol.send(nvt.getTelnetOutputStream(), "beip.tilemap.info", toSend);
	}

	//-------------------------------------------------------------------
	public static void sendBeipMapUpdate(TelnetClientConnection nvt, String name, ViewportMap<Symbol> map) throws IOException {
		StringBuffer buf = new StringBuffer();
		for (int y=0; y<map.getHeight(); y++) {
			for (int x=0; x<map.getWidth(); x++) {
				if (map.get(x, y).getId()>255) {
					logger.log(Level.WARNING, "Cannot transfer symbol >255 in Beip: symbol was {0}", map.get(x, y));
					buf.append("00");
				} else {
					buf.append( String.format("%02X", map.get(x, y)));
				}
			}
		}
		logger.log(Level.INFO, "Buffer has "+buf.length()+" bytes");

		HashMap<String, String> data = new HashMap<>();
		data.put(name, buf.toString());
		String toSend = gson.toJson(data);
		logger.log(Level.DEBUG, toSend);
		GenericMUDCommunicationProtocol.send(nvt.getTelnetOutputStream(), "beip.tilemap.data", toSend);
	}

	//-------------------------------------------------------------------
	public static void sendClientMediaPlay(TelnetClientConnection nvt, String name, String url, ClientMediaPlay.Type type) throws IOException {
		ClientMediaPlay play = new ClientMediaPlay();
		play.name = name;
		play.url  = url;
		play.type = type;
		String toSend = gson.toJson(play);
		logger.log(Level.DEBUG, toSend);
		GenericMUDCommunicationProtocol.send(nvt.getTelnetOutputStream(), "Client.Media.Play", toSend);
	}


	//-------------------------------------------------------------------
	public static void sendCharVitals(TelnetClientConnection nvt, Vitals gmcpVitals) throws IOException {
		String toSend = gson.toJson(gmcpVitals);
//		logger.log(Level.DEBUG, toSend);
		GenericMUDCommunicationProtocol.send(nvt.getTelnetOutputStream(), "Char.Vitals", toSend);
	}

}

class ChannelPlayer {
	String name;
	List<String> channels = new ArrayList<>();
	public ChannelPlayer(String name) {
		this.name = name;
	}
}
