/**
 *
 */
package org.prelle.mud.telnet.impl;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.StringTokenizer;
import java.util.concurrent.CompletableFuture;

import org.prelle.ansi.ANSIInputStream;
import org.prelle.ansi.ANSIOutputStream;
import org.prelle.ansi.AParsedElement;
import org.prelle.ansi.C0Code;
import org.prelle.ansi.C0Fragment;
import org.prelle.ansi.C1Code;
import org.prelle.ansi.ControlSequenceFragment;
import org.prelle.ansi.DeviceAttributes.VT220Parameter;
import org.prelle.ansi.DeviceControlFragment;
import org.prelle.ansi.KeyCodeFragment;
import org.prelle.ansi.PrintableFragment;
import org.prelle.ansi.StringMessageFragment;
import org.prelle.ansi.commands.CursorBackward;
import org.prelle.ansi.commands.CursorDown;
import org.prelle.ansi.commands.CursorForward;
import org.prelle.ansi.commands.CursorPosition;
import org.prelle.ansi.commands.CursorUp;
import org.prelle.ansi.commands.MXPLine;
import org.prelle.ansi.commands.ScrollDown;
import org.prelle.ansi.commands.ScrollUp;
import org.prelle.ansi.commands.SelectGraphicRendition;
import org.prelle.ansi.commands.SelectGraphicRendition.Meaning;
import org.prelle.ansi.commands.SetMode;
import org.prelle.ansi.commands.iterm.SendITermImage;
import org.prelle.ansi.commands.kitty.KittyGraphicsFragment;
import org.prelle.ansi.commands.kitty.KittyImageTransmission;
import org.prelle.ansi.control.AreaControls;
import org.prelle.mud.telnet.ANSIOutputFormat;
import org.prelle.mud.telnet.DumbTerminalVisualizedMenu;
import org.prelle.mud.telnet.DynamicOutputFormat;
import org.prelle.mud.telnet.OutputFormat;
import org.prelle.mud.telnet.SimpleMapWithTextFormat;
import org.prelle.mud.telnet.TelnetConnector;
import org.prelle.mud.telnet.gmcp.GMCP;
import org.prelle.mud4j.gmcp.GMCPManager;
import org.prelle.mud4j.gmcp.Char.Vitals;
import org.prelle.mudansi.CapabilityDetector;
import org.prelle.mudansi.FormatUtil;
import org.prelle.mudansi.MarkupParser;
import org.prelle.mudansi.TerminalCapabilities;
import org.prelle.mudansi.UIGridFormat;
import org.prelle.mudansi.UIGridFormat.Area;
import org.prelle.mudansi.UIGridFormat.AreaDefinition;
import org.prelle.telnet.TelnetCommand;
import org.prelle.telnet.TelnetConfigOption;
import org.prelle.telnet.TelnetConstants;
import org.prelle.telnet.TelnetInputStream;
import org.prelle.telnet.TelnetOption;
import org.prelle.telnet.TelnetOptionCapabilities;
import org.prelle.telnet.TelnetOptionRegistry;
import org.prelle.telnet.TelnetOutputStream;
import org.prelle.telnet.TelnetSocket;
import org.prelle.telnet.TelnetSocket.State;
import org.prelle.telnet.TelnetSocketListener;
import org.prelle.telnet.mud.GenericMUDCommunicationProtocol.GMCPReceiver;
import org.prelle.telnet.mud.GenericMUDCommunicationProtocol.RawGMCPMessage;
import org.prelle.telnet.mud.MUDTerminalTypeData;
import org.prelle.telnet.mud.MUDTerminalTypeData.Capability;
import org.prelle.telnet.mud.MUDTilemapProtocol;
import org.prelle.telnet.option.LineMode.LineModeListener;
import org.prelle.telnet.option.LineMode.ModeBit;
import org.prelle.telnet.option.MXPOption;
import org.prelle.telnet.option.MXPOption.MXPFeatures;
import org.prelle.telnet.option.MXPOption.MXPListener;
import org.prelle.telnet.option.TelnetCharset.CharsetListener;
import org.prelle.telnet.option.TelnetEnvironmentOption.EnvironmentListener;
import org.prelle.telnet.option.TelnetWindowSize.TelnetNAWSListener;
import org.prelle.telnet.option.TerminalType.TerminalTypeData;
import org.prelle.telnet.option.TerminalType.TerminalTypeListener;

import com.graphicmud.MUD;
import com.graphicmud.commands.CommunicationChannel;
import com.graphicmud.dialog.DialogueTree;
import com.graphicmud.game.Game;
import com.graphicmud.game.Vital;
import com.graphicmud.game.Vital.VitalType;
import com.graphicmud.map.SixelEncoder;
import com.graphicmud.map.ViewportMap;
import com.graphicmud.network.AClientConnection;
import com.graphicmud.network.ClientConnection;
import com.graphicmud.network.ClientConnectionState;
import com.graphicmud.network.ConnectionVariables;
import com.graphicmud.network.MUDClientCapabilities;
import com.graphicmud.network.MUDConnector;
import com.graphicmud.network.MUDConnectorListener;
import com.graphicmud.network.MUDClientCapabilities.Audio;
import com.graphicmud.network.MUDClientCapabilities.Color;
import com.graphicmud.network.MUDClientCapabilities.Control;
import com.graphicmud.network.MUDClientCapabilities.Graphic;
import com.graphicmud.network.MUDClientCapabilities.Layout;
import com.graphicmud.network.interaction.Form;
import com.graphicmud.network.interaction.Menu;
import com.graphicmud.network.interaction.Table;
import com.graphicmud.network.interaction.VisualizedMenu;
import com.graphicmud.player.ConfigOption;
import com.graphicmud.player.ImageProtocol;
import com.graphicmud.symbol.Symbol;
import com.graphicmud.world.Location;
import com.graphicmud.world.NoSuchPositionException;
import com.graphicmud.world.Surrounding;

/**
 *
 */
public class TelnetClientConnection extends AClientConnection implements ClientConnection, TelnetSocketListener,
	Runnable, ConnectionVariables, EnvironmentListener, TelnetNAWSListener, TerminalTypeListener,
	LineModeListener, GMCPReceiver, CharsetListener, MXPListener {

	private static enum MXPMode {
		OPEN_LINE,
		SECURE_LINE,
		LOCKED_LINE,
		SECURE_TEMP,
		OPEN,
		SECURE,
		LOCKED
	}
	
	private final static int EOR = 0xEF;

	private MUDConnector connector;

	private Charset charset = StandardCharsets.UTF_8;
	private TelnetSocket socket;
	private MUDConnectorListener handler;

	private TelnetOutputStream tout;
	private ANSIOutputStream out;
	private TelnetInputStream tin;
	private ANSIInputStream in;

	private boolean inTelnetDetectionPhase;
	private CompletableFuture<TelnetOptionCapabilities> telnetOptions;
	boolean inTerminalDetectionPhase;
	private CapabilityDetector detector;

	private boolean createEcho;
	private boolean characterMode;
	private boolean tempSupressEcho;
	private InputBuffer inputLineBuffer;
	private boolean sendEOR;

	private Menu currentMenu;
	private boolean gmcpStarted;
	private MXPMode mxpMode = MXPMode.LOCKED;
	private InputBuffer mxpBuffer;

	private OutputFormat format;

	//-----------------------------------------------------------------
	public TelnetClientConnection(TelnetSocket socket, TelnetConnector connector, MUDConnectorListener handler) throws IOException {
		this.socket = socket;
		this.connector = connector;
		this.handler   = handler;


		socket.addSocketListener(this);
		socket.setOptionListener( TelnetOption.LINEMODE, this);
		socket.setOptionListener( TelnetOption.GMCP, this);
		socket.setOptionListener( TelnetOption.NAWS, this);
		socket.setOptionListener( TelnetOption.NEW_ENVIRON, (EnvironmentListener)this);
		socket.setOptionListener( TelnetOption.MTT, this);
		socket.setOptionListener( TelnetOption.CHARSET, (CharsetListener)this);
		socket.setOptionListener( TelnetOption.MXP, (MXPListener)this);
		logger = System.getLogger("mud.network.telnet."+socket.getInetAddress().getHostAddress());
		// Start telnet option capability exchange


		tout = (TelnetOutputStream) socket.getOutputStream();
		out = new ANSIOutputStream(tout);
		if (logger.isLoggable(Level.DEBUG)) {
			out.setLoggingListener( (name,d) -> System.err.println("TCC: --> "+name+" with "+d));
		}

		tin = (TelnetInputStream) socket.getInputStream();
		inputLineBuffer = new InputBuffer();
		mxpBuffer       = new InputBuffer();

		inTelnetDetectionPhase = true;
		telnetOptions = socket.initialize();
		telnetOptions.thenAccept( (caps) -> telnetNegotiationDone(caps));

//		format = new TopMapOutputFormat();
		format = new SimpleMapWithTextFormat();
	}

	//-------------------------------------------------------------------
	public ANSIOutputStream getOutputStream() {
		return out;
	}

	//-------------------------------------------------------------------
	public TelnetOutputStream getTelnetOutputStream() {
		return tout;
	}

	//-------------------------------------------------------------------
	public Charset getCharset() {
		return charset;
	}

	//-------------------------------------------------------------------
	public boolean hasGMCPStarted() { return gmcpStarted; }
	public void setGMCPStarted(boolean val) { this.gmcpStarted=val; }

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#logOut()
	 */
	@Override
	public void logOut() {
		state = ClientConnectionState.DISCONNECTED;
		// Remove from world
		try {
			if (getCharacter()!=null) {
				Location loc = MUD.getInstance().getWorldCenter().getLocation(getCharacter().getPosition());
				loc.removeEntity(getCharacter());
			}
		} catch (NoSuchPositionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			out.close();
			tin.close();
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//-------------------------------------------------------------------
	private void eventuallyPrefillLogin() {
		// If we wait for user login, but just received
		String login = getVariable(VAR_LOGIN);
		if (state==ClientConnectionState.ENTER_LOGIN && createEcho && login!=null && !inputLineBuffer.toString().startsWith(login)) {
			logger.log(Level.DEBUG, "prefill username");
			try {
				out.write(login.getBytes(charset));
				inputLineBuffer.append(login);
				if (sendEOR) {
					out.write(EOR);
				}
				out.flush();
			} catch (IOException e) {
				logger.log(Level.ERROR, e);
			}
		}
	}


	//-------------------------------------------------------------------
	private void telnetNegotiationDone(TelnetOptionCapabilities capabilites) {
		logger.log(Level.ERROR, "telnetNegotiationDone");
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetSocketListener#telnetSocketChanged(org.prelle.telnet.TelnetSocket, org.prelle.telnet.TelnetSocket.State, org.prelle.telnet.TelnetSocket.State)
	 */
	@Override
	public void telnetSocketChanged(TelnetSocket nvt, State oldState, State newState) {
		logger.log(Level.INFO, "socket state changed from {0} to {1}", oldState, newState);
		if (newState!=State.READY) {
			return;
		}

		// Dump Telnet options
		if (logger.isLoggable(Level.INFO)) {
			List<Entry<TelnetOption,TelnetConfigOption>> list = socket.getCapabilities().stream().sorted(new Comparator<Entry<TelnetOption,TelnetConfigOption>>(){
				public int compare(Entry<TelnetOption, TelnetConfigOption> arg0,
						Entry<TelnetOption, TelnetConfigOption> arg1) {
					return Integer.compare( arg0.getKey().getCode(), arg1.getKey().getCode() );
				}}).toList();
			for (Entry<TelnetOption, TelnetConfigOption> entry : list) {
				if (entry.getValue()==null) {
					logger.log(Level.ERROR, "Missing TelnetConfigOption for "+entry.getKey());
					continue;
				}
				if (entry.getValue().isConfigurable()!=null && entry.getValue().isConfigurable()) {
					logger.log(Level.INFO, "{0}\t= {1}", entry.getKey().name(), entry.getValue());
				} else {
					logger.log(Level.DEBUG, "{0}\t= {1}", entry.getKey().name(), entry.getValue());
				}
			}
			logger.log(Level.INFO, "{0}\t= {1}", "Charset", this.charset);
		}
		
		// Check for active ECHO and SGA to assume character mode
		if (socket.getConfigOption(TelnetOption.ECHO).isActive() && socket.getConfigOption(TelnetOption.SGA).isActive()) {
			logger.log(Level.WARNING, "Client agreed to ECHO+SGA - assume character mode");
			characterMode = true;
			if (!capabilities.controlSupport.contains(Control.LINEMODE)) {
				capabilities.controlSupport.add(Control.LINEMODE);
			}
		}
		
		// Dump subnegotiation data
		TelnetOptionCapabilities caps = nvt.getNegotiationResult();
		for (Entry<Integer, Object> entry : caps.getOptionData()) {
			TelnetOption option = TelnetOption.valueOf(entry.getKey());
			String name = (option!=null)?option.name():String.valueOf(entry.getKey());
			logger.log(Level.INFO, "{0} \t? {1}", name, entry.getValue());
			if (TelnetOption.NAWS==option) {
				int[] size = (int[])entry.getValue();
				logger.log(Level.INFO, "WINDOW SIZE is {0}x{1}", size[0], size[1]);
				telnetWindowSizeChanged(size[0], size[1]);
			}
		}


		// Start detection
		logger.log(Level.INFO, "Start detection thread");
		detector = new CapabilityDetector(out);
//		in.setLoggingListener( (type,text) -> logger.log(Level.INFO, "MUD <-- {0} = {1}", type,text));
//		out.setLoggingListener( (type,text) -> {if (!"PRINT".equals(type)) logger.log(Level.INFO, "MUD --> {0} = {1}", type,text);});
		try { socket.setTcpNoDelay(true); } catch (SocketException e) { e.printStackTrace(); }
		CompletableFuture<Void> detection = CompletableFuture
				.runAsync(new TerminalCapabilityTask(this, detector))
				.thenRun( () -> {
					logger.log(Level.INFO, "detection done");
					// Check if interface may need a reset
					try {
						createEcho=false;
//						if (!socket.getConfigOption(TelnetOption.LINEMODE).isConfigurable()) {
						if (!capabilities.controlSupport.contains(Control.LINEMODE)) {
							logger.log(Level.WARNING, "LINEMODE not supported - disable ECHO creation");
							tout.sendWont(TelnetOption.ECHO.getCode());
							this.createEcho=false;
							logger.log(Level.WARNING, "User interface may be fucked up, since linemode did not work");
							// User interface may be fucked up
							out.write(new SelectGraphicRendition(List.of(Meaning.RESET)));
							out.write("\r\nPress ENTER ...\r\n");
						} else {
							logger.log(Level.WARNING, "LINEMODE supported - enable ECHO creation");
							out.write(new SelectGraphicRendition(List.of(Meaning.RESET)));
							tout.sendWill(TelnetOption.ECHO.getCode());
							this.createEcho=true;
						}
						if (getVariable(VAR_TERMTYPE)!=null && (getVariable(VAR_TERMTYPE).equals("ANSI") || getVariable(VAR_TERMTYPE).equals("VT100"))) {
							if (!capabilities.colorModes.contains(Color.COLOR_16)) {
								capabilities.colorModes.add(Color.COLOR_16);
							}
						}
					} catch (IOException e) {
						logger.log(Level.ERROR, "Failing sending",e);
					}
				})
				.thenRun( () -> {
					logger.log(Level.INFO, "ENTER: Prepare login process");
					try {
						out.write(new SetMode(SetMode.Mode.LNM_LINE_FEED_NEW_LINE));
						pushConnectionListener(MUD.getInstance().getLoginHandler());
						socket.setTcpNoDelay(false);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// MXP
					if (getProtocolCapabilities().containsKey("MXP")) {
						logger.log(Level.ERROR, "#######MXP FRAME################");
//						//String support = "\u001B[4z<FRAME Name=\"Map\" INTERNAL Align=\"Top\" Left=\"-21c\" Top=\"0\" Width=\"21c\" Height=\"21c\">\u001B[7c";
//						String support = "\u001B[6z";
//						support+="<FRAME Name=\"Map\" INTERNAL Align=\"Top\" Left=\"-21c\" Top=\"0\" Width=\"21c\" Height=\"21c\">";
//						support+="<DEST Map>";
//						support+="Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.";
//						support+="<IMAGE fName=\"U4TilesV.gif\" url=\"http://192.168.0.2:4080/symbols/\" align=left>";
//						support+="<h>Heading</h>\r\n";
//						support+="Hello <b>World</b>.<br>";
//						support+="Just that you know it: <a href=\"http://heise.de\">this</a> is a link.<br>";
//						support+="</DEST>";
//						support+="\u001B[7c";
//						//support += "\u001B[4z<IMG fName=\"U4TilesV.gif\" url=\"http://192.168.0.2:4080/symbols/\">\u001B[7c";
//						System.out.println("Send "+support);
//						try {
//							out.write(support.getBytes(StandardCharsets.US_ASCII));
//						} catch (IOException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}

					}
					logger.log(Level.INFO, "LEAVE: Prepare login process");
				})
				.exceptionally( (ex) -> { 
					logger.log(Level.ERROR, "Error "+ex,ex.getCause());
					return null;
					})
				;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetSocketListener#telnetOptionStatusChange(org.prelle.telnet.TelnetSocket, org.prelle.telnet.TelnetSubnegotiationHandler, boolean)
	 */
	@Override
	public void telnetOptionStatusChange(TelnetSocket nvt, TelnetOption option, boolean active) {
		if (option==TelnetOption.EOR) {
			if (inTelnetDetectionPhase) {
				socket.getConfigOption(TelnetOption.EOR).setActive(active);
			}
			sendEOR = active;
			logger.log(Level.INFO, active?"Send EOR":"Don't send EOR");
		} else if (option==TelnetOption.ECHO) {
			if (inTelnetDetectionPhase) {
				logger.log(Level.INFO, "Disabling local echo is {0}supported", active?"NOT ":"");
				socket.getConfigOption(TelnetOption.ECHO).setActive(active);
			}
			if (active && !capabilities.controlSupport.contains(Control.ECHO))
				capabilities.controlSupport.add(Control.ECHO);
			createEcho = active;
//			tin.setCharacterMode(active);
			logger.log(Level.INFO, "Client is in {0} echo mode (ECHO)", active?"remote":"local");
		} else if (option==TelnetOption.LINEMODE) {
			if (inTelnetDetectionPhase) {
				socket.getConfigOption(TelnetOption.LINEMODE).setConfigurable(active);
				logger.log(Level.INFO, "Confirmed: Client can {0} enter character mode (LINEMODE)", active?"":"NOT");
				if (active && !capabilities.controlSupport.contains(Control.LINEMODE))
					capabilities.controlSupport.add(Control.LINEMODE);
			}
		} else {
			logger.log(Level.ERROR, "Feature {0} is {1}", option.name(), active?"enabled":"disabled");
			if (active) {
				switch (option) {
				case MSP:
					if (!capabilities.audioSupport.contains(Audio.MSP))
						capabilities.audioSupport.add(Audio.MSP);
					break;
				case NAWS:
					if (!capabilities.layoutFeatures.contains(Layout.NAWS))
						capabilities.layoutFeatures.add(Layout.NAWS);
					break;
//				case MXP:
//					logger.log(Level.ERROR, "#########MXP###################");
//					logger.log(Level.ERROR, "listener = "+nvt.getOptionListener(option.getCode()));
//					String support = "\u001B[4z<SUPPORT>\u001B[7z";
//					try {
//						nvt.getOutputStream().write(support.getBytes(StandardCharsets.US_ASCII));
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
				}
			}
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.TelnetSocketListener#telnetCommandReceived(org.prelle.telnet.TelnetSocket, org.prelle.telnet.TelnetCommand)
	 */
	@Override
	public void telnetCommandReceived(TelnetSocket nvt, TelnetCommand command) {
		logger.log(Level.WARNING, "RCV {0}", command.getCode());
		switch (command.getCode()) {
		case IP:
			// Interrupt process - client wants to disconnect
			logger.log(Level.WARNING, "Disconnecting");
			logOut();
			return;
		}
	}

	//-------------------------------------------------------------------
	private void mapToTermCap(String clientName, String clientVersion, String terminalName, List<Capability> capabilities) {
		logger.log(Level.DEBUG, "mapToTermCap: {0}, {1}, {2}, {3}", clientName, clientVersion, terminalName, capabilities);
		if (clientName!=null)
			this.capabilities.clientName = clientName;
		if (clientVersion!=null)
			this.capabilities.clientVersion = clientVersion;
		if (terminalName!=null && this.capabilities.terminalType.equals("Not set"))
			this.capabilities.terminalType = terminalName;
		if (capabilities!=null) {
			for (Capability cap : capabilities) {
				switch (cap) {
				case VT100:
					logger.log(Level.WARNING, "Taking assumptions here what VT100 means");
					if (!this.capabilities.colorModes.contains(Layout.CURSOR_POSITIONING))
						this.capabilities.layoutFeatures.add(Layout.CURSOR_POSITIONING);
					break;
				case ANSI:
					if (!this.capabilities.colorModes.contains(Color.COLOR_16))
						this.capabilities.colorModes.add(Color.COLOR_16);
					break;
				case COLOR256:
					if (!this.capabilities.colorModes.contains(Color.COLOR_256))
						this.capabilities.colorModes.add(Color.COLOR_256);
					break;
				case TRUECOLOR:
					if (!this.capabilities.colorModes.contains(Color.COLOR_16M))
						this.capabilities.colorModes.add(Color.COLOR_16M);
					break;
				case UTF8:
					this.capabilities.supportsUnicode=true;
					break;
				case SCREEN_READER:
					this.capabilities.supportsScreenreader=true;
					break;
				case MOUSE_TRACKING:
					this.capabilities.supportsMouse=true;
					break;
				}
			}
		}

		if (terminalName!=null) {
			if (terminalName.toLowerCase().contains("vt100")) {
				if (!this.capabilities.colorModes.contains(Color.COLOR_16))
					this.capabilities.colorModes.add(Color.COLOR_16);
			}
			if (terminalName.toLowerCase().contains("xterm")) {
				if (!this.capabilities.colorModes.contains(Color.COLOR_16))
					this.capabilities.colorModes.add(Color.COLOR_16);
				if (!this.capabilities.colorModes.contains(Color.COLOR_256))
					this.capabilities.colorModes.add(Color.COLOR_256);
				if (!this.capabilities.colorModes.contains(Color.COLOR_16M))
					this.capabilities.colorModes.add(Color.COLOR_16M);
			}
			if (terminalName.toLowerCase().contains("kitty")) {
//				termCap.sixelGraphics = true;
//				termCap.kittyGraphics = true;
				if (!this.capabilities.colorModes.contains(Color.COLOR_16))
					this.capabilities.colorModes.add(Color.COLOR_16);
				if (!this.capabilities.colorModes.contains(Color.COLOR_256))
					this.capabilities.colorModes.add(Color.COLOR_256);
				if (!this.capabilities.colorModes.contains(Color.COLOR_16M))
					this.capabilities.colorModes.add(Color.COLOR_16M);
				logger.log(Level.DEBUG, "Assuming this terminal knows Sixel and Kitty graphics");
			}
		}
//		if (clientName!=null) {
//			if (clientName.toLowerCase().contains("xterm")) {
//				termCap.color16=true;
//				termCap.color256=true;
//				termCap.colorTrue=true;
//				termCap.cursorPositioning=true;
//				termCap.verticalScrolling=true;
//				termCap.horizontalScrolling=true;
//			}
//			if (clientName.toUpperCase().contains("VT220")) {
//				termCap.color16=true;
//				termCap.color256=true;
//				termCap.colorTrue=true;
//				termCap.cursorPositioning=true;
//				termCap.verticalScrolling=true;
//			}
//			if (clientName.toUpperCase().contains("MUDLET")) {
//				termCap.color16=true;
//				termCap.color256=true;
//				termCap.colorTrue=true;
//				termCap.cursorPositioning=false;
//			}
//			if (clientName.toUpperCase().contains("ZMUD")) {
//				termCap.color16=true;
//				termCap.color256=true;
////				termCap.colorTrue=true;
//				termCap.cursorPositioning=true;
//			}
//			if (clientName.toUpperCase().contains("-256COLOR")) {
//				termCap.color16=true;
//				termCap.color256=true;
//			}
//		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		logger.log(Level.INFO, "Start Virtual Thread");
		Thread.currentThread().setName("TCC");
		// Read data to allow option negotiation
		try {
			in  = new ANSIInputStream(this.tin);
			logger.log(Level.DEBUG, "Input stream in character mode = "+this.tin.isCharacterMode());
			in.setCollectPrintable(false);

			if (logger.isLoggable(Level.DEBUG)) {
				in.setLoggingListener( (name,d) -> System.err.println("TCC: <-- "+name+" with "+d));
			}
			while (true) {
				try {
					AParsedElement read = in.readFragment();
					if (read==null) {
						logger.log(Level.WARNING, "Connection closed by remote host: {0}",socket.getRemoteSocketAddress());
						getClientConnectionListener().connectionLost(this);
						handler.closedConnection(this);
						break;
					}
					logger.log(Level.TRACE, "read = {0} in state {1}  / {2}",read, state, inTelnetDetectionPhase);
					if (read instanceof MXPLine) {
						telnetReceivedMXP((MXPLine) read);
						continue;
					}
					
					if (inTerminalDetectionPhase && !(read instanceof MXPLine) && mxpMode==MXPMode.LOCKED) {
						detector.process(read);
						continue;
					}
					
					// Eventually end line-restricted MXP mode
					if (read instanceof C0Fragment) {
						C0Code c0 = ((C0Fragment)read).getCode();
						if (c0==C0Code.CR || c0==C0Code.LF) {
							if (mxpMode==MXPMode.LOCKED_LINE || mxpMode==MXPMode.SECURE_LINE || mxpMode==MXPMode.OPEN_LINE) {
								logger.log(Level.ERROR, "MXP line ends {0}", inputLineBuffer.getInput());
								mxpMode=MXPMode.LOCKED;
								logger.log(Level.INFO, "MXP mode is {0} after receiving {1}",mxpMode, c0);
								if (!inputLineBuffer.getInput().isBlank()) {
									processMXP(inputLineBuffer.getInput());
									((MXPOption)TelnetOptionRegistry.get(TelnetOption.MXP.getCode())).processMXP(inputLineBuffer.getInput());
									inputLineBuffer.clear();
								}
							}
						}
					}
					
					boolean consumed = inputLineBuffer.handle(read);
					if (consumed) {
						logger.log(Level.TRACE, "Linebuffer consumed {0} - echo mode is {1}", read, createEcho);
						if (createEcho && (read instanceof PrintableFragment || read instanceof C0Fragment)) {
							Character ch = null;
							if (read instanceof PrintableFragment) {
								ch =((PrintableFragment)read).getText().charAt(0);
							}
//							logger.log(Level.WARNING, "ch="+ch+"  and is Alphabetic "+( (ch!=null)?Character.isAlphabetic(ch):"-"));
//							logger.log(Level.WARNING, "tempSupressEcho="+tempSupressEcho);
							// If we do have a line buffer in the format, update it
							if (format instanceof DynamicOutputFormat && capabilities.controlSupport.contains(Control.LINEMODE)) {
								((DynamicOutputFormat)format).getGrid().showMarkupIn(UIGridFormat.ID_INPUT, inputLineBuffer.getInput());
							} else {
								// in character mode, send echo
								if (!tempSupressEcho || ch==null || !Character.isAlphabetic(ch)) {
									if ((read instanceof C0Fragment) && ((C0Fragment)read).getCode()==C0Code.DEL) {
										// Send Backspace + Space + Backspace
										out.write(new byte[] {8,32,8});
									} else {
										out.write(read);
									}
									out.flush();
								} else {
									out.write('*');
								}
							}
						}
						continue;
					}
					logger.log(Level.DEBUG, "Linebuffer did not consume {0} - echo mode is {1}", read+"/"+read.getClass(), createEcho);

					switch (read) {
					case DeviceControlFragment dcs -> processDeviceControl(dcs);
					case StringMessageFragment dcs when dcs.getCode()==C1Code.APC -> processApplicationCommand(dcs);
					case StringMessageFragment dcs when dcs.getCode()==C1Code.PM  -> processPrivacyMessage(dcs);
					case StringMessageFragment dcs when dcs.getCode()==C1Code.APC -> processOperatingSystemCommand(dcs);
					case ControlSequenceFragment seq -> processControlSequence( seq );
					case C0Fragment c0 -> processC0Fragment(c0);
					case KeyCodeFragment key -> processKeyCode(key);
					default -> logger.log(Level.WARNING, "TODO: Process ANSI control codes {0} / {1}", read, read.getClass());
					}
				} catch (IOException e) {
					throw e;
				} catch (Exception e) {
					logger.log(Level.ERROR, "Problem processing input",e);
				}
			}
			in.close();
			return;
		} catch (SocketException e) {
			logger.log(Level.WARNING, "Connection lost");
			state = ClientConnectionState.DISCONNECTED;
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			logger.log(Level.WARNING, "Stop virtual thread for {0}", socket.getInetAddress().getHostAddress());
			if (getCharacter()!=null) {
				MUD.getInstance().getGame().removePlayer(getCharacter());
			}
		}
	}

	//-------------------------------------------------------------------
	private void processC0Fragment(C0Fragment c0) {
		logger.log(Level.DEBUG, "processCO({0})",c0);
		switch (c0.getCode()) {
		case SOH: return;
		case NUL: return;
		case LF:
		case CR:
			// Commit input buffer
			String input = inputLineBuffer.getInput();
			logger.log(Level.DEBUG, "ENTER for linebuffer {0}",input);
			if (input.isEmpty()) return;
			// Filter PUEBLO
			if (input.startsWith("PUEBLOCLIENT")) {
				processPueblo(input);
				inputLineBuffer.clear();
				return;
			}
			
			if (createEcho) {
				inputLineBuffer.clear();
				try {
					out.write("\r\n".getBytes());
					out.flush();
				} catch (IOException e) {
					logger.log(Level.WARNING, "IO-Error sending",e);
					setState(ClientConnectionState.DISCONNECTED);
				}
				if (!listener.isEmpty()) {
					listener.peek().receivedInput(this, input);
				}
			} else {
				inputLineBuffer.clear();
				if (!listener.isEmpty()) {
					listener.peek().receivedInput(this, input);
				} else {
					logger.log(Level.WARNING, "Ignore ''{0}'' due to no handler", input);
				}
			}
			break;
		case DEL:
		case BS:
			// Commit input buffer
			if (createEcho) {
				logger.log(Level.DEBUG, "DEL for linebuffer "+inputLineBuffer);
				try {
					out.write(c0.getCode().code());
					out.flush();
				} catch (IOException e) {
					logger.log(Level.WARNING, "IO-Error sending",e);
					setState(ClientConnectionState.DISCONNECTED);
				}
			}
			inputLineBuffer.handle(c0);
			break;
		case ETX:
			logger.log(Level.DEBUG, "Ctrl-C (ETX) received");
			// Client is trying to close the connection
			logger.log(Level.WARNING, "TODO: Disconnecting client");
			this.logOut();
			break;
		case HT:
			input = inputLineBuffer.getInput();
			logger.log(Level.WARNING, "TAB pressed for "+inputLineBuffer);
//			List<ParsedCommand> poss = MUD.getInstance().getCommandManager().getPossibleCompletions(this, input);
//			poss.forEach(c -> System.out.println("---"+c));
			break;
		default:
			logger.log(Level.WARNING, "Unhandled C0 Code {0}", c0);
		}
	}

	//-------------------------------------------------------------------
	private void processKeyCode(KeyCodeFragment key) {
		logger.log(Level.WARNING, "TODO: processKeyCode "+key);
		try {
			switch (key.getKeyCode()) {
			case 0x21: // Page Up
//				out.write(new EscapeSequenceFragment('M', "Scoll Up", org.prelle.ansi.Level.ANSI));
//				out.write(new C1Fragment(C1Code.RI));
				out.write(new ScrollDown(10));
				out.flush();
				break;
			case 0x22: // Page Down
//				out.write(new EscapeSequenceFragment('D', "Scoll Down", org.prelle.ansi.Level.ANSI));
				out.write(new ScrollUp(10));
				out.flush();
				break;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//-------------------------------------------------------------------
	private void processControlSequence(ControlSequenceFragment read) {
		logger.log(Level.WARNING, "processControlSequence {0} with {1}", read, read.getArguments());
		switch (read) {
		case CursorUp cuu -> listener.peek().receivedKeyCode(this, 38, read.getArguments());
		case CursorDown cud -> listener.peek().receivedKeyCode(this, 40, read.getArguments());
		case CursorBackward cub -> listener.peek().receivedKeyCode(this, 37, read.getArguments());
		case CursorForward cuf -> listener.peek().receivedKeyCode(this, 39, read.getArguments());
//		case org.prelle.ansi.commands.DeviceAttributes da -> receivedDeviceAttributes(da);
		default ->
			logger.log(Level.ERROR, "ignore "+read.getClass());
		}
	}

//	//-------------------------------------------------------------------
//	private void receivedDeviceAttributes(org.prelle.ansi.commands.DeviceAttributes da) {
//		switch (da.getVariant()) {
//		case Primary:
//			// Primary Device Attributes
//			List<Integer> devAttr = da.getArguments();
//			int termType = devAttr.get(0);
//			OperatingLevel opLevel = DeviceAttributes.OperatingLevel.valueOf(termType);
//			logger.log(Level.INFO, "Device attributes identify terminal capabilities as {0}={1}", termType, opLevel);
////			try {
////				out.write(("Your terminal indicates compatibility to "+opLevel+"\r\n").getBytes());
////			} catch (IOException e) {}
//			switch (opLevel) {
//			case LEVEL1_VT100_PLAIN:
//			case LEVEL1_VT100:
//				termCap.color16=true;
//				termCap.cursorPositioning=true;
//				termCap.verticalScrolling=true;
//				termCap.horizontalScrolling=true;
//				break;
//			case LEVEL4_VT320:
//			}
//
//			logger.log(Level.DEBUG, "Check device capabilities from DA: {0}", devAttr);
//			if (devAttr.contains(1))  {} // 132 columns
//			if (devAttr.contains(VT220Parameter.ANSI_COLOR.code()))  termCap.color16=true;
//			if (devAttr.contains(VT220Parameter.SIXEL.code()))  {
//				termCap.sixelGraphics=true;
//				logger.log(Level.WARNING, "Is a Sixel graphics capable device");
//			}
//			if (devAttr.contains(VT220Parameter.HORIZONTAL_SCROLLING.code()))  termCap.horizontalScrolling=true;
//			if (devAttr.contains(VT220Parameter.SOFT_CHARACTER_SET.code()))  {
//				termCap.sixelFont=true;
//				logger.log(Level.WARNING, "Is a SIXEL fonts capable device");
//			}
//			break;
//		case Secondary:
//			// Secondary Device attributes
//			devAttr = da.getArguments();
//			try {
//				DeviceAttributes.TerminalType termType2 = DeviceAttributes.TerminalType.valueOf(devAttr.get(0));
//				logger.log(Level.INFO, "DA2: Device identifies as {0} terminal ({1})", termType2, devAttr);
////				if (termType2==null) {
////					out.write(("Your terminal does not identify as any known model ("+devAttr.get(0)+")\r\n").getBytes());
////				} else {
////					out.write(("Your terminal identifies as "+termType2+"\r\n").getBytes());
////				}
//			} catch (Exception e) {}
//			break;
//		case Tertiary:
//			// Tertiary Device attributes
//			logger.log(Level.INFO, "DA3: Firmware={0}", da.getArguments());
//			break;
//		}
//	}

	//-------------------------------------------------------------------
	private void processDeviceControl(DeviceControlFragment dcs) {
		logger.log(Level.WARNING, "TODO: Received DCS ", dcs.getText(charset));
	}

	//-------------------------------------------------------------------
	private void processApplicationCommand(StringMessageFragment apc) {
		logger.log(Level.DEBUG, "Received APC ", apc.toString());

		if (apc.getData().startsWith("G")) {
			// May be Kitty graphics protocol
			if (apc.getData().endsWith(";OK")) {
				logger.log(Level.INFO, "Kitty graphics confirmed");
				if (!this.capabilities.graphicSupport.contains(Graphic.KITTY))
					capabilities.graphicSupport.add(Graphic.KITTY);
			}
		}
	}

	//-------------------------------------------------------------------
	private void processPrivacyMessage(StringMessageFragment apc) {
		logger.log(Level.WARNING, "TODO: Received PM ", apc.toString());
	}

	//-------------------------------------------------------------------
	private void processOperatingSystemCommand(StringMessageFragment osc) {
		logger.log(Level.WARNING, "TODO: Received OSC ", osc.toString());
	}
	
	//-------------------------------------------------------------------
	private void processPueblo(String input) {
		logger.log(Level.WARNING, "TODO: processPueblo: {0}", input);
		System.err.println("PUEBLO "+input);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#sendScreen(java.lang.String)
	 */
	@Override
	public void sendScreen(String data) {
		logger.log(Level.DEBUG, "sendScreen in charset {0}", charset);
		if (state==ClientConnectionState.DISCONNECTED) return;
		byte[] buf = (data+"\r\n").getBytes(charset);
		try {
			out.write(buf);
		} catch (IOException e) {
			logger.log(Level.WARNING, "IO-Error sending",e);
			setState(ClientConnectionState.DISCONNECTED);
		}
	}

	//-------------------------------------------------------------------
	/**
	 * Sends a text message followed by a Telnet GA, to inform the client not to
	 * wait for the line end
	 * @see com.graphicmud.network.ClientConnection#sendPrompt(java.lang.String)
	 */
	@Override
	public void sendPrompt(String prompt) {
		if (state==ClientConnectionState.DISCONNECTED)
			return;
		if (this.isDoNotDisturb()) return;
		byte[] buf = (prompt+" " ).getBytes(charset);
		try {
			out.write(buf);
			if (socket.isFeatureActive(TelnetOption.EOR.getCode())) {
				out.write(TelnetConstants.IAC);
				out.write(EOR);
			}
			if (!socket.isFeatureActive(TelnetOption.SGA.getCode())) {
				if (tin.isInBinaryMode()) {
					out.write(TelnetConstants.IAC);
				}
				out.write(TelnetConstants.ControlCode.GA.code());
//			} else {
//				out.write("\r\n".getBytes(charset));
			}
			out.flush();
		} catch (IOException e) {
			logger.log(Level.WARNING, "IO-Error sending",e);
			setState(ClientConnectionState.DISCONNECTED);
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#sendPromptWithStats(com.graphicmud.game.Vital[])
	 */
	@Override
	public void sendPromptWithStats(Map<VitalType, Vital> vitals) {
		if (state==ClientConnectionState.DISCONNECTED) return;
		if (this.isDoNotDisturb()) return;
		StringBuilder buf = new StringBuilder("\r\n");
		Game game = MUD.getInstance().getGame();
		try {
			buf.append(game.getVitalName(VitalType.VITAL1)[1]+":"+vitals.get(VitalType.VITAL1).getCurrent());
			buf.append("  ");
			buf.append(game.getVitalName(VitalType.VITAL2)[1]+":"+vitals.get(VitalType.VITAL2).getCurrent());
			buf.append("  ");
			buf.append(game.getVitalName(VitalType.VITAL3)[1]+":"+vitals.get(VitalType.VITAL3).getCurrent());
			sendPrompt(buf.toString()+"> ");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		HashMap<String,Integer> list = getVariable(ConnectionVariables.VAR_GMCP_PACKAGES);
		if (list!=null && list.containsKey("Char")) {
			logger.log(Level.DEBUG, "TODO: send GMCP Char.Vitals  "+buf);
			Vitals gmcpVitals = new Vitals();
			gmcpVitals.setString(buf.toString());
			try {
				GMCP.sendCharVitals(this, gmcpVitals);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#sendShortText(java.lang.String)
	 */
	@Override
	public void sendShortText(Priority prio, String prompt) {
		if (state==ClientConnectionState.DISCONNECTED) return;
		if (prio==Priority.UNIMPORTANT && isDoNotDisturb()) return;
		prompt = prompt.replace("\n", "\r\n");
		byte[] buf = (prompt).getBytes(charset);
		try {
			out.write(buf);
			if (!prompt.endsWith("\n"))
//			if (socket.isFeatureActive(TelnetOption.EOR.getCode())) {
//				out.write(TelnetConstants.IAC);
//				out.write(EOR);
//			} else {
				out.write("\r\n");
//			}
		} catch (IOException e) {
			logger.log(Level.WARNING, "IO-Error sending",e);
			setState(ClientConnectionState.DISCONNECTED);
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#sendTextWithMarkup(java.lang.String)
	 */
	@Override
	public void sendTextWithMarkup(String text) {
		if (state==ClientConnectionState.DISCONNECTED) return;
		int cols = getNumericVariable(VAR_COLUMNS, 80);
		text = FormatUtil.convertTextBlock( MarkupParser.convertText(text), cols);
		logger.log(Level.INFO, "SEND "+text);
		byte[] buf = (text).getBytes(charset);
		try {
			out.write(buf);
			if (socket.isFeatureActive(TelnetOption.EOR.getCode())) {
				out.write(TelnetConstants.IAC);
				out.write(EOR);
			} else {
				out.write("\r\n");
			}
		} catch (IOException e) {
			logger.log(Level.WARNING, "IO-Error sending",e);
			setState(ClientConnectionState.DISCONNECTED);
		}
	}

	//-------------------------------------------------------------------
	public void send(String text) throws IOException {
		out.write(text.getBytes(charset));
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#sendRoom(com.graphicmud.world.Surrounding)
	 */
	@Override
	public void sendRoom(Surrounding room) {
		try {
			format.sendRoom(room);
			if (getObjectVariable(VAR_GMCP_PACKAGES)!=null && ((Map<String,Integer>)getObjectVariable(VAR_GMCP_PACKAGES)).containsKey("Room")) {
				logger.log(Level.INFO, "Send Room.INFO");
				GMCP.sendRoomInfo(this, room);
			}
			if (getObjectVariable(VAR_GMCP_PACKAGES)!=null && ((Map<String,Integer>)getObjectVariable(VAR_GMCP_PACKAGES)).containsKey("Beip.Tilemap")) {
				logger.log(Level.WARNING, "ToDo: Reactivate Beip");
				GMCP.sendBeipMapUpdate(this, "Surrounding", room.getMap());
			}
		} catch (IOException e) {
			logger.log(Level.WARNING, "IO-Error sending",e);
			setState(ClientConnectionState.DISCONNECTED);
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#sendMap(int[][])
	 */
	@Override
	public void sendMap(ViewportMap<Symbol> mapData) {
		logger.log(Level.INFO, "sendMap");
		if (socket.isFeatureActive(MUDTilemapProtocol.CODE)) {
//			try {
//				MUDTilemapProtocol.sendMap(socket, mapData);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			return;
		}

//		if (!(termCap.cursorPositioning && termCap.horizontalScrolling)) {
//			logger.log(Level.DEBUG, "Don't send map - terminal does not support it");
//			return;
//		}

		try {
			format.sendMapOnly(mapData);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		UseSymbol use = UseSymbol.ANSI;
////		if (termCap.color256)
////			use=UseSymbol.CP437;
////		if (termCap.colorTrue && termCap.charset==StandardCharsets.UTF_8)
////			use=UseSymbol.UNICODE;
//		List<String> mapLines = ANSIArtMapConverter.convertMap(mapData, use);
//		try {
//			CursorControls.savePositionDEC(out);
//			// Remove margins
////			out.writeCSI(ControlFunction.DECSTBM);
////			out.writeCSI(ControlFunction.DECSLRM);
//			for (int i=0; i<mapLines.size(); i++) {
////				logger.log(Level.INFO, "line {0} has {1} chars", i, mapLines.get(i).length());
//				CursorControls.setCursorPosition(out, 2, 2+i);
//				out.write(decompose(mapLines.get(i)).getBytes(charset));
//			}
//			// Reapply margins
////			int rows = getNumericVariable(VAR_ROWS, 80);
//////			AreaControls.setLeftAndRightMargins(out, 5, 0);
////			AreaControls.setTopAndBottomMargins(out, 14, rows);
//			out.reset();
//			CursorControls.restorePositionDEC(out);
//		} catch (IOException e) {
//			logger.log(Level.WARNING, "IO-Error sending",e);
//			setState(ClientConnectionState.DISCONNECTED);
//		}
	}

	//-------------------------------------------------------------------
	public static String decompose(String s) {
	    return java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+","");
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
		InetAddress addr = getVariable(VAR_IPADDRESS);
		if (addr==null) return "-";
		return addr.getHostAddress();
	}

	@Override
	public String getClient() {
		return getVariable(VAR_CLIENT);
	}

	@Override
	public String getTerminalSize() {
		return getVariable(VAR_TERMSIZE);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#getCapabilityString()
	 */
	@Override
	public String getCapabilityString() {
		List<Capability> caps = getVariable(VAR_MTT_CAPS);
		if (caps==null) return "";
		return String.join(",", caps.stream().map(e -> e.name()).toList());
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#getProtocolCapabilities()
	 */
	public LinkedHashMap<String, List<String>> getProtocolCapabilities() {
		LinkedHashMap<String, List<String>> ret = new LinkedHashMap<>();
		Map<Integer,String> list = new HashMap<>();
		list.put(  1,"ECHO");
		list.put( 34,"LINEMODE");
		list.put( 31,"NAWS");
		list.put( 39,"MNES");
		list.put(201,"GMCP");
		list.put( 24,"MTTS");
//		list.put( 25,"EOR" );
		list.put( 69,"MSDP");
		list.put( 90,"MSP");
		list.put( 91,"MXP");

		List<Integer> protoNums = new ArrayList<>(list.keySet());
		Collections.sort(protoNums);
		for (Integer code : protoNums) {
			String name = list.get(code);
			List<String> data = new ArrayList<>();
			if (socket.isFeatureActive(code)) {
				ret.put(name, data);
				// Build detail list
				Object anyData = socket.getOptionData(code);
				if (anyData!=null) {
					if (anyData instanceof List) {
						ret.put(name, (List<String>) anyData);
						continue;
					}
					if (anyData instanceof int[]) {
						ret.put(name,  List.of((int[])anyData).stream().map(i -> String.valueOf(i)).toList());
						continue;
					}
					if (anyData instanceof Map) {
						ret.put(name, ((Map)anyData).entrySet().stream().toList() );
						continue;
					}
					logger.log(Level.WARNING, "Don''t know how to deal with {0} for {1}",anyData.getClass(), list.get(code));
					data.add("TODO: "+anyData);
//					if (anyData instanceof TelnetWindowSizeData) {
//						data.add( getVariable(VAR_TERMSIZE));
//					} else if (anyData instanceof TelnetEnvironmentData) {
//						TelnetEnvironmentData environ = ((TelnetEnvironmentData)anyData);
//						environ.getVariables().forEach( (k,v) -> data.add(k+"="+v));
//					} else if (anyData instanceof MUDTerminalTypeData) {
//						MUDTerminalTypeData mtts = ((MUDTerminalTypeData)anyData);
//						data.add("Client="+mtts.getClientName());
//						data.add("TermType="+mtts.getTerminalType());
//						data.add("Capabilities="+mtts.getCapabilities());
//					} else if (anyData instanceof RawGMCPMessage) {
//						Map<String, Integer> packages = (Map<String, Integer>) getObjectVariable(VAR_GMCP_PACKAGES);
//						if (packages!=null) {
//							List<String> list2 = packages.keySet().stream().sorted().toList();
//							data.addAll(list2);
//						}
//					}
				}

			} else {
				ret.put("-"+name, data);
			}

		}

		return ret;
	}

	//-------------------------------------------------------------------
	private void decideOutputFormat() throws IOException {
		logger.log(Level.WARNING, "decide output format, charset is {0}", charset);
		System.err.println("TelnetClientConnection.decideOutputFormat  for "+out);
//		String clientName = getVariable(VAR_CLIENT);
		Map<String,Integer> gmcp = getVariable(VAR_GMCP_PACKAGES);
		if (gmcp==null) gmcp = new HashMap<>();
		
		// STEP 1: Screen reader do get minimal interface
		if (capabilities.supportsScreenreader) {
			logger.log(Level.INFO, "STEP 1: Screen Reader detected");
			format = new ANSIOutputFormat();
		} else {
			logger.log(Level.INFO, "STEP 1: No Screen Reader detected");
			/*
			 * Step 2: Check if at least cursor positioning and top/bottom margins 
			 *     are supported for a dynamic interface
			 */
			if (capabilities.layoutFeatures.contains(Layout.CURSOR_POSITIONING) 
					&& capabilities.layoutFeatures.contains(Layout.TOP_BOTTOM_MARGIN)) {
				// Yes, dynamic format
				logger.log(Level.INFO, "STEP 2: Cursor Movement and Top/Bottom margins supported - use dynamic format");
				format = new DynamicOutputFormat();
			} else {
				// No, old school format
				logger.log(Level.INFO, "STEP 2: No minimal VT support - use backward compatible format");
				format = new SimpleMapWithTextFormat();
			}
		}
		// Basic configuration
		format.initialize(this,out);
		format.configureSize(
				getNumericVariable(VAR_COLUMNS, 80),
				getNumericVariable(VAR_ROWS, 40),
				logger);
		
		// Special handling for dynamic format
		if (format instanceof DynamicOutputFormat) {
			if (capabilities.cellSize!=null && capabilities.cellSize[0]>1) {
				logger.log(Level.INFO, "STEP 3: Cell Size is "+capabilities.cellSize[0]+"x"+capabilities.cellSize[1]);
				int w=(32*11)/(capabilities.cellSize[0]+7);
				int h=(32*11)/(capabilities.cellSize[1]+10);
				logger.log(Level.INFO, "      : Mapping to {0}x{1}", w,h);
				((DynamicOutputFormat)format).getGrid().setLeftWidth(w);
				((DynamicOutputFormat)format).getGrid().setTopHeight(h);
			}
			
			// STEP 3: If there is support for character mode, add an input line
			if (capabilities.controlSupport.contains(Control.LINEMODE)) {
				logger.log(Level.INFO, "STEP 3: Character mode supported");
				((DynamicOutputFormat)format).setWithInputBuffer(true);
				((DynamicOutputFormat)format).getGrid().setBottomHeight(1);
				((DynamicOutputFormat)format).getGrid().join(UIGridFormat.ID_INPUT, Area.BOTTOM, Area.BOTTOM_LEFT, Area.BOTTOM_RIGHT);
			} else {
				logger.log(Level.INFO, "STEP 3: Character mode not supported");					
			}
			// STEP 4: 
			if (capabilities.layoutFeatures.contains(Layout.LEFT_RIGHT_MARGIN)) {
				((DynamicOutputFormat)format).getGrid().setOuterBorder(true);
				((DynamicOutputFormat)format).getGrid().join(UIGridFormat.ID_INPUT, Area.BOTTOM, Area.BOTTOM_LEFT, Area.BOTTOM_RIGHT);
			} else {
				((DynamicOutputFormat)format).getGrid().join(UIGridFormat.ID_SCROLL, Area.LEFT, Area.CENTER, Area.RIGHT);
			}
			if (capabilities.controlSupport.contains(Control.LINEMODE)) {
				((DynamicOutputFormat)format).getGrid().join(UIGridFormat.ID_INPUT, Area.BOTTOM, Area.BOTTOM_LEFT, Area.BOTTOM_RIGHT);
			}
			
			logger.log(Level.INFO, ((DynamicOutputFormat)format).getGrid().dump() );
			((DynamicOutputFormat)format).getGrid().recreate(charset);
			
			// Start in scrollable area
			AreaDefinition scroll = ((DynamicOutputFormat)format).getGrid().getArea(UIGridFormat.ID_SCROLL);
			out.write(new CursorPosition(scroll.getX(), scroll.getY()));

		}
		
		logger.log(Level.INFO, "Format is {0}", format.getClass().getSimpleName());
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#initializeInterface()
	 */
	@Override
	public void initializeInterface() {
		try {
			logger.log(Level.INFO, "initializeInterface");

			AreaControls.clearScreen(out);
			decideOutputFormat();
			if (getObjectVariable(VAR_GMCP_PACKAGES)!=null && ((Map<String,Integer>)getObjectVariable(VAR_GMCP_PACKAGES)).containsKey("Beip.Tilemap")) {
				logger.log(Level.WARNING, "Send BeipMap");
				GMCP.sendBeipMapDefinition(this, "Surrounding", "https://github.com/BeipDev/BeipMU/raw/master/images/Ultima5.png", 16, 16, 11, 11);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//-------------------------------------------------------------------
	public static List<String> splitIntoKittyChunks(byte[] imgData) {
		String encoded = Base64.getEncoder().encodeToString(imgData);
		int offset=0;
		List<String> ret = new ArrayList<>();
		do {
			int to = Math.min(offset+4096, encoded.length());
			ret.add( encoded.substring(offset,to) );
			offset += 4096;
		} while (offset<encoded.length());
		return ret;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#sendImage(byte[], java.lang.String, int, int)
	 */
	@Override
	public void sendImage(byte[] data, String filename, int width, int height) {
		logger.log(Level.WARNING, "ENTER sendImage({1}x{2}) for {0} bytes", data.length, width, height);
//		logger.log(Level.DEBUG, "Sixel = "+termCap.sixelGraphics);
//		logger.log(Level.DEBUG, "Kitty = "+termCap.kittyGraphics);
//		logger.log(Level.DEBUG, "IIP   = "+termCap.iip);

		ImageProtocol ipConfig = getCharacter().getConfigurationAsEnum(ConfigOption.IMAGE_PROTOCOL);
		if (capabilities.graphicSupport.contains(Graphic.ITERM) || ipConfig==ImageProtocol.ITERM) {
			logger.log(Level.INFO, "Send as iterm");
			SendITermImage iterm = new SendITermImage();
			iterm.setSize(data.length);
			iterm.setFileName(filename);
			iterm.setWidth(width);
			iterm.setHeight(height);
			iterm.setImgData(data);
			try {
				out.write(iterm);
				out.write("\r\n");
			} catch (IOException e) {
				logger.log(Level.ERROR, "Failed sending image",e);
			}
		} else if (capabilities.graphicSupport.contains(Graphic.KITTY) || ipConfig==ImageProtocol.KITTY) {
			boolean isFirst = true;
			try {
//				CursorControls.savePositionDEC(out);
//				CursorControls.setCursorPosition(out, 2, 2);
				for (Iterator<String> it = splitIntoKittyChunks(data).iterator(); it.hasNext(); ) {
					String chunk = it.next();
					KittyImageTransmission kitty = new KittyImageTransmission();
					kitty.setPayload(chunk);
					if (isFirst) {
						kitty.set('a',KittyGraphicsFragment.ACION_TRANSMIT_AND_DISPLAY);
						kitty.setFormat(KittyImageTransmission.FORMAT_PNG);
						kitty.setWidth(width);
						kitty.setHeight(height);
						kitty.set(KittyImageTransmission.KEY_ID, 32);
						kitty.setMedium(KittyImageTransmission.MEDIUM_DIRECT);
						kitty.set('c',11);
						kitty.set('r',11);
						isFirst=false;
					}
					kitty.setMoreChunksFollow(it.hasNext());
					logger.log(Level.ERROR, "Send Kitty PNG APC");
					out.write(kitty);
				}
//				CursorControls.restorePositionDEC(out);
				out.write("\r\n");
			} catch (IOException e) {
				logger.log(Level.ERROR, "Failed sending image",e);
			}
		} else if (capabilities.graphicSupport.contains(Graphic.SIXEL)|| ipConfig==ImageProtocol.SIXEL) {
			try {
//				InputStream ins = ClassLoader.getSystemResourceAsStream("static/transparent1.six");
//				out.write(ins.readAllBytes());
//				out.flush();
				Optional<SixelEncoder> encoder = ServiceLoader.load(SixelEncoder.class).findFirst();
				System.err.println("First encoder = "+encoder);
				if (encoder.isPresent()) {
					logger.log(Level.DEBUG, "Found Sixel encoder");
					String encoded = encoder.get().toSixel(data);
					DeviceControlFragment dcs = new DeviceControlFragment("q", List.of(0,1,0), encoded);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					dcs.encode(baos, true);
					FileOutputStream dummy = new FileOutputStream("/tmp/dummy.six");
					dummy.write(baos.toByteArray());
					dummy.flush();
					dummy.close();
	
					out.write(new DeviceControlFragment("q", List.of(0,1,0), encoded));
				} else {
					logger.log(Level.WARNING, "No Sixel encoder");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (capabilities.graphicSupport.contains(Graphic.MXP_IMAGE)|| ipConfig==ImageProtocol.MXP) {
			String support = "\u001B[4z";
			support+="<IMAGE fName=\"U4TilesV.gif\" url=\"http://192.168.0.2:4080/symbols/\" align=left>";
			support+="\u001B[7c\r\n";
			System.out.println("Send "+support);
			try {
				out.write(support.getBytes(StandardCharsets.US_ASCII));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


	}

	//-------------------------------------------------------------------
	void writeEOR() throws IOException {
		out.write(0xFF);
		out.write(EOR);
		out.flush();
	}

//	//-------------------------------------------------------------------
//	/**
//	 * @see org.prelle.mud.network.ClientConnection#sendRoom(org.prelle.mud.world.Surrounding, int[][])
//	 */
//	@Override
//	public void sendRoom(Surrounding room) {
//		format
//			.clear()
//			.setDescription( room.getDescription())
//			.send(this);
//
//		sendMap(room.getMap());
////		sendImage(new byte[0],"NoName",0,0);
////		sendShortText(room.getDescription());
//	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#presentMenu(com.graphicmud.network.interaction.Menu)
	 */
	@Override
	public VisualizedMenu presentMenu(Menu menu) {
		logger.log(Level.INFO, "presentMenu "+menu.getTitle());

		DumbTerminalVisualizedMenu visual = new DumbTerminalVisualizedMenu(this);
		visual.updateChoices(menu, menu.getItems());
		
//		currentMenu = menu;
//		StringBuffer mess = new StringBuffer();
//		if (menu.getTitle()!=null) {
//			mess.append("<b>"+menu.getTitle()+"</b><br/>");
//		}
//		if (menu.getMessage()!=null) {
//			mess.append(menu.getMessage()+"<br/>");
//		}
//		int count=0;
//		for (MenuItem item : menu.getItems()) {
//			boolean selectable = true;
//			if (item.getCheckIfSelectable()!=null) {
//				selectable = item.getCheckIfSelectable().test(null);
//			}
//			if (!selectable)
//				mess.append("<strike>");
//			String emoji = (item.getEmoji()!=null)?(item.getEmoji()+" "):"";
//			if (item instanceof ToggleMenuItem) {
//				Object selection = this.retrieveMenuItemData(menu, item);
//				boolean selected = selection!=null;
//				String format = String.format("(%d) %s[%s] %s\n", count++, emoji, selected?"x":" ", item.getLabel());
//				// The \n above already is mapped to <br/>
//				mess.append(format);
//			} else {
//				mess.append("("+(count++)+") "+emoji+item.getLabel()+"\n");
//			}
//			if (!selectable)
//				mess.append("</strike>");
//		}
//
//		logger.log(Level.DEBUG, "convert "+mess);
//		sendTextWithMarkup(mess.toString());
//		sendPrompt("? ");

		setState(ClientConnectionState.IN_MENU);
		return visual;
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
		try {
			format.sendTable(table);
		} catch (IOException e) {
			logger.log(Level.WARNING, "IO-Error sending",e);
			setState(ClientConnectionState.DISCONNECTED);
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#presentDialog(com.graphicmud.dialog.DialogueTree, java.lang.String)
	 */
	@Override
	public void presentDialog(DialogueTree tree, String image) {
		try {
			logger.log(Level.INFO, "Call "+format.getClass().getSimpleName()+".sendDialog");
			format.sendDialog(tree, image);
		} catch (IOException e) {
			logger.log(Level.WARNING, "IO-Error sending",e);
			setState(ClientConnectionState.DISCONNECTED);
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnection#suppressEcho(boolean)
	 */
	@Override
	public void suppressEcho(boolean suppress) {
		logger.log(Level.ERROR, "TODO: suppress echo {0}", suppress);
		tempSupressEcho = suppress;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetEnvironmentOption.EnvironmentListener#telnetLearnedEnvironmentVariables(java.util.Map)
	 */
	@Override
	public void telnetLearnedEnvironmentVariables(Map<String, String> variables) {
		for (Entry<String, String> pair : variables.entrySet()) {
			String value = pair.getValue();
			switch (pair.getKey()) {
			case "MTTS":
				setVariable(VAR_MTT_CAPS, MUDTerminalTypeData.convertToList(Integer.parseInt(value)));
				mapToTermCap(null, null, null, MUDTerminalTypeData.convertToList(Integer.parseInt(value)));
				break;
			case "CHARSET":
				Charset oldCharset = charset;
				try {
					if ("ISO 8859-15".equals(value))
						charset = StandardCharsets.ISO_8859_1;
					else if ("ISO 8859-1".equals(value))
						charset = StandardCharsets.ISO_8859_1;
					else
						charset = Charset.forName(value);
					logger.log(Level.INFO, "Connection charset is "+charset+"  old charset was "+oldCharset);
					if (oldCharset!=charset) {
						charsetChanged();
					}
				} catch (Exception e) {
					logger.log(Level.WARNING, "Unknown charset ''{0}'' in MNES respone", value);
				}
				break;
			case "CLIENT_NAME":
				setVariable(VAR_CLIENT, value);
				mapToTermCap(value, null, null, null);
				break;
			case "CLIENT_VERSION":
				mapToTermCap(null, value, null, null);
				break;
			case "TERMINAL_TYPE":
				mapToTermCap(null, null, value, null);
				break;
			case "USER":
				logger.log(Level.WARNING, "Client sends user {0} in ENVIRON data while in state {1}", value, state);
				setVariable(VAR_LOGIN, value);
				break;
			default:
				logger.log(Level.DEBUG, "Ignore MNES variables {0}={1}", pair.getKey(), value);
			}
		}
	}

	//-------------------------------------------------------------------
	private void charsetChanged() {
		logger.log(Level.WARNING, "TODO: Charset changed");
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetWindowSize.TelnetNAWSListener#telnetWindowSizeChanged(int, int)
	 */
	@Override
	public void telnetWindowSizeChanged(int width, int height) {
		this.capabilities.terminalSize = new int[] {width,height};
		setVariable(VAR_TERMSIZE, width+"x"+height);
		setVariable(VAR_COLUMNS , width);
		setVariable(VAR_ROWS    , height);
		format.configureSize( width, height , logger);
		if (getState()==ClientConnectionState.LOGGED_IN && listener!=null) {
			listener.peek().performScreenRefresh(this);
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TerminalType.TerminalTypeListener#telnetTerminalTypesLearned(org.prelle.telnet.option.TerminalType.TerminalTypeData)
	 */
	@Override
	public void telnetTerminalTypesLearned(TerminalTypeData raw) {
		logger.log(Level.INFO, "telnetTerminalTypesLearned {0}={2} while in detection={1}",raw.getAll(),inTelnetDetectionPhase, raw.getClass());
		logger.log(Level.INFO, "TTYPE "+raw.getFirstOption());

		if (raw instanceof MUDTerminalTypeData) {
			MUDTerminalTypeData bla = (MUDTerminalTypeData)raw;
			setVariable(VAR_CLIENT, bla.getClientName());
			setVariable(VAR_TERMTYPE, (bla.getTerminalType()!=null)?bla.getTerminalType():bla.getClientName());
			setVariable(VAR_MTT_CAPS, bla.getCapabilities());
			mapToTermCap(bla.getClientName(), null, bla.getTerminalType(), bla.getCapabilities());
		} else if (raw.getFirstOption()!=null) {
			setVariable(VAR_TERMTYPE, (raw.getFirstOption()));
			mapToTermCap(null, null, raw.getFirstOption(), null);
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.LineMode.LineModeListener#linemodeFlagsSuggested(java.util.List)
	 */
	@Override
	public List<ModeBit> linemodeFlagsSuggested(List<ModeBit> suggested) {
		logger.log(Level.INFO, "linesModesFlagsSuggested {0} while in detection={1}",suggested,inTelnetDetectionPhase);
		if (inTelnetDetectionPhase) {
			if (suggested.contains(ModeBit.MODE_ACK)) {
				if (suggested.contains(ModeBit.EDIT)) {
					socket.getConfigOption(TelnetOption.LINEMODE).setActive(false);
					logger.log(Level.INFO, "Confirmed: Client can NOT go into character mode");
				} else {
					socket.getConfigOption(TelnetOption.LINEMODE).setActive(true);
					logger.log(Level.INFO, "Confirmed: Client can go into character mode");
				}
			}

		}
		return suggested;
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.LineMode.LineModeListener#linemodeFlagsAcknowledged(java.util.List)
	 */
	@Override
	public void linemodeFlagsAcknowledged(List<ModeBit> acknowledged) {
		acknowledged.remove(ModeBit.MODE_ACK);
		logger.log(Level.DEBUG, "linemodeFlagsAcknowledged {0} while in detection={1}",acknowledged,inTelnetDetectionPhase);
		characterMode = !acknowledged.contains(ModeBit.EDIT);
		if (characterMode && !capabilities.controlSupport.contains(Control.LINEMODE)) {
			capabilities.controlSupport.add(Control.LINEMODE);
		}
		logger.log(Level.INFO, "RFC 1184 LineMode without Edit flag confirmed - Connection is {0}in character mode", characterMode?"":"not ");
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.LineMode.LineModeListener#sendFlushOn(java.util.List)
	 */
	@Override
	public void sendFlushOn(List<Integer> flushCodes) {
		// TODO Auto-generated method stub
		logger.log(Level.INFO, "sendFlushOn "+flushCodes);

	}

//	//-------------------------------------------------------------------
//	/**
//	 * @see org.prelle.telnet.mud.GenericMUDCommunicationProtocol.GMCPReceiver#telnetReceiveGMCP(org.prelle.telnet.mud.GenericMUDCommunicationProtocol.RawGMCPMessage)
//	 */
//	@Override
//	public void telnetReceiveGMCP(RawGMCPMessage gmcp) {
//		logger.log(Level.INFO, "RCV "+gmcp.getNamespace()+"  "+gmcp.getMessage());
//		Object mess = GMCPManager.decode(gmcp.getNamespace(), gmcp.getMessage());
//		if (mess==null) {
//			logger.log(Level.WARNING, "No parsing support for {0} {1}", gmcp.getNamespace(), gmcp.getMessage());
//			return;
//		}
//		try {
//			GMCP.rcvCommand(this, mess);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

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

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.option.TelnetCharset.CharsetListener#telnetCharsetNegotiated(java.nio.charset.Charset)
	 */
	@Override
	public void telnetCharsetNegotiated(Charset charset) {
		logger.log(Level.INFO, "Remote party agrees to use "+charset);
		if (charset!=null) {
			if (this.charset!=charset) {
				this.charset = charset;
				charsetChanged();
			}
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see org.prelle.telnet.mud.GenericMUDCommunicationProtocol.GMCPReceiver#telnetReceiveGMCP(org.prelle.telnet.mud.GenericMUDCommunicationProtocol.RawGMCPMessage)
	 */
	@Override
	public void telnetReceiveGMCP(RawGMCPMessage message) {
//		logger.log(Level.WARNING, "TODO: Received GMCP");
		Object decoded = GMCPManager.decode(message.getNamespace(), message.getMessage());
		logger.log(Level.DEBUG, "decoded GMCP as "+decoded);
		try {
			if (decoded!=null)
				GMCP.rcvCommand(this, decoded, message.getNamespace());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//-------------------------------------------------------------------
	private void telnetReceivedMXP(MXPLine mxp) {
		switch (mxp.getArguments().get(0)) {
		case 0:  mxpMode=MXPMode.OPEN_LINE; break;
		case 1:  mxpMode=MXPMode.SECURE_LINE; break;
		case 2:  mxpMode=MXPMode.LOCKED_LINE; break;
		case 4:  mxpMode=MXPMode.SECURE_TEMP; break;
		case 5:  mxpMode=MXPMode.OPEN; break;
		case 6:  mxpMode=MXPMode.SECURE; break;
		case 7:  mxpMode=MXPMode.LOCKED; break;
		}
		mxpBuffer.clear();
		logger.log(Level.INFO, "MXP mode is {0} after receiving {1}",mxpMode, mxp);
	}

	//-------------------------------------------------------------------
	private void processMXP(String line) {
		logger.log(Level.WARNING, "RCV MXP "+line);
		System.err.println(line);
		
		line = line.toLowerCase();
		if (line.startsWith("<supports ")) {
			StringTokenizer tok = new StringTokenizer( line.substring(10));
			List<String> supported = new ArrayList<>();
			while (tok.hasMoreTokens()) supported.add(tok.nextToken());
			socket.setOptionData(TelnetOption.MXP.getCode(), supported);
//			setVariable("MXP", supported);
			if (supported.contains("+image")) {
				if (!capabilities.graphicSupport.contains(Graphic.MXP_IMAGE))
					capabilities.graphicSupport.add(Graphic.MXP_IMAGE);
			} else if (supported.contains("-image")) {
				capabilities.graphicSupport.remove(Graphic.MXP_IMAGE);
			}
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @return the characterMode
	 */
	public boolean isCharacterMode() {
		return characterMode;
	}

	//-------------------------------------------------------------------
	/**
	 * @param characterMode the characterMode to set
	 */
	public void setCharacterMode(boolean characterMode) {
		this.characterMode = characterMode;
	}

	@Override
	public void telnetMXPLearned(MXPFeatures data) {
		// TODO Auto-generated method stub
		logger.log(Level.ERROR, "telnetMXPLearned");
		System.err.println("MXP");
	}

}

class TerminalCapabilityTask implements Runnable, ConnectionVariables {

	private final static Logger logger = System.getLogger(TerminalCapabilityTask.class.getPackageName());

	private TelnetClientConnection con;
	private CapabilityDetector detector;

	//-------------------------------------------------------------------
	public TerminalCapabilityTask(TelnetClientConnection con, CapabilityDetector detector) {
		this.con = con;
		this.detector = detector;
	}

	//-------------------------------------------------------------------
	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		logger.log(Level.INFO, "BEGIN: detection");
		con.inTerminalDetectionPhase = true;
		try {
			Instant before = Instant.now();
			TerminalCapabilities termCap = detector.performCheck(
					con.getNumericVariable(VAR_COLUMNS, 80), 
					con.getNumericVariable(VAR_ROWS, 40)
					);
//			synchronized (capabilities.capSubNegAwaitResponses) {
//				capabilities.capSubNegAwaitResponses.wait(500);
//			}
			Duration dura = Duration.between(before, Instant.now());
			logger.log(Level.DEBUG, "Terminal detection required {0} ms\n", dura.toMillis());

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			termCap.report(new ANSIOutputStream(baos));
			baos.flush();
			String mess = new String(baos.toByteArray(), StandardCharsets.US_ASCII);
			logger.log(Level.INFO, "Results:\n"+mess);

			mergeToConnection(termCap);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			con.inTerminalDetectionPhase = false;
		}
		logger.log(Level.INFO, "END: detection");
	}

	//-------------------------------------------------------------------
	private void mergeToConnection(TerminalCapabilities termCap) {
		logger.log(Level.DEBUG, "Merge terminal capabilities into connection capabilities");
		MUDClientCapabilities client = con.getCapabilities();

		if (client.terminalType==null && termCap.getTerminalName()!=null)
			client.terminalType=termCap.getTerminalName();
		if (client.terminalSize==null || termCap.getScreenSize()[0]!=0)
			client.terminalSize=termCap.getScreenSize();
		client.cellSize=termCap.getCellSize();

		if (termCap.isCursorPositioning() && !client.layoutFeatures.contains(Layout.CURSOR_POSITIONING)) {
			client.layoutFeatures.add(Layout.CURSOR_POSITIONING);
		}

		if (termCap.isMarginLeftRight() && !client.layoutFeatures.contains(Layout.LEFT_RIGHT_MARGIN)) {
			client.layoutFeatures.add(Layout.LEFT_RIGHT_MARGIN);
		}

		if (termCap.isMarginTopBottom() && !client.layoutFeatures.contains(Layout.TOP_BOTTOM_MARGIN)) {
			client.layoutFeatures.add(Layout.TOP_BOTTOM_MARGIN);
		}
		if (termCap.getGeneralCompatibility()!=null && termCap.getOperatingLevel()!=null) {
			client.emulation=termCap.getGeneralCompatibility().name()+" in "+termCap.getOperatingLevel()+" mode";
		} else if (termCap.getGeneralCompatibility()!=null && termCap.getOperatingLevel()==null) {
			client.emulation=termCap.getGeneralCompatibility().name();
		} else if (termCap.getOperatingLevel()!=null) {
			client.emulation=termCap.getOperatingLevel().name();
		}

		if (termCap.isInlineImageITerm() && !client.graphicSupport.contains(Graphic.ITERM)) {
			client.graphicSupport.add(Graphic.ITERM);
		}
		if (termCap.isInlineImageKitty() && !client.graphicSupport.contains(Graphic.KITTY)) {
			client.graphicSupport.add(Graphic.KITTY);
		}
		if (termCap.isInlineImageSixel() && !client.graphicSupport.contains(Graphic.SIXEL)) {
			client.graphicSupport.add(Graphic.SIXEL);
		}
		if (termCap.getFeatures().contains(VT220Parameter.SOFT_CHARACTER_SET) && !client.graphicSupport.contains(Graphic.DRCS)) {
			client.graphicSupport.add(Graphic.DRCS);
		}
		if (termCap.getFeatures().contains(VT220Parameter.ANSI_COLOR) && !client.colorModes.contains(Color.COLOR_16)) {
			client.colorModes.add(Color.COLOR_16);
		}
		if (termCap.isColor16() && !client.colorModes.contains(Color.COLOR_16)) {
			client.colorModes.add(Color.COLOR_16);
		}
		if (termCap.isColor256() && !client.colorModes.contains(Color.COLOR_256)) {
			client.colorModes.add(Color.COLOR_256);
		}
		if (termCap.isColor16m() && !client.colorModes.contains(Color.COLOR_16M)) {
			client.colorModes.add(Color.COLOR_16M);
		}

		if (con.getVariable(ConnectionVariables.VAR_GMCP_PACKAGES)!=null) {
			Map<String,Integer> supported = con.getVariable(ConnectionVariables.VAR_GMCP_PACKAGES);
			if (supported.containsKey("Beip.Tilemap") && !client.graphicSupport.contains(Graphic.GMCP_BEIP)) {
				client.graphicSupport.add(Graphic.GMCP_BEIP);
			}
			if (supported.containsKey("Client.Media") && !client.audioSupport.contains(Audio.GMCP_CLIENT_MEDIA)) {
				client.audioSupport.add(Audio.GMCP_CLIENT_MEDIA);
			}
		}
	}
}