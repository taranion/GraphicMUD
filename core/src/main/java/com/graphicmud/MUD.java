/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

import com.graphicmud.commands.CommandCenter;
import com.graphicmud.game.Game;
import com.graphicmud.game.MUDClock;
import com.graphicmud.handler.LoginHandler;
import com.graphicmud.network.ClientConnectionListener;
import com.graphicmud.network.MUDConnector;
import com.graphicmud.network.impl.ConnectionManager;
import com.graphicmud.player.PlayerDatabase;
import com.graphicmud.player.RPGConnector;
import com.graphicmud.symbol.SymbolManager;
import com.graphicmud.symbol.SymbolManagerSingleton;
import com.graphicmud.web.WebServer;
import com.graphicmud.web.WebServerImpl;
import com.graphicmud.world.WorldCenter;

import lombok.Getter;

/**
 *
 */
public class MUD {

	public final static Logger WizLog = System.getLogger("wizlog");

	private final static Logger logger = System.getLogger("mud");

	@Getter
	private static MUD instance;

	private String name;
	private List<MUDConnector> connectors;
	@Getter
	private ConnectionManager connectionManager;
	@Getter
	private SymbolManager symbolManager;
	@Getter
	private LoginHandler loginHandler = LoginHandler.builder().build();
	@Getter
	private ClientConnectionListener createHandler;
	@Getter
	private WorldCenter worldCenter;
	@Getter
	private CommandCenter commandManager;
	@Getter
	private Game game = new Game();
	@Getter
	private WebServer web;
	@Getter
	private RPGConnector<?,?,?> rpgConnector;
	@Getter
	private PlayerDatabase playerDatabase;

	//-------------------------------------------------------------------
	private MUD(MUDBuilder builder) throws IOException {
		this.name = builder.name;
		this.connectors = builder.connectors;
		this.createHandler = builder.createHandler;
		this.symbolManager= builder.symbolManager;
		this.worldCenter  = builder.worldCenter;
		this.commandManager= builder.commandCenter;
		this.rpgConnector = builder.characterFactory;
		this.playerDatabase   = builder.playerDatabase;

		InetAddress inet = InetAddress.getLocalHost();
		try {
			Optional<NetworkInterface> opt = NetworkInterface.networkInterfaces().filter(ifa -> {
				try {
					return ifa.isUp() && !ifa.isLoopback();
				} catch (SocketException e) {
					e.printStackTrace();
					return false;
				}
			}).findFirst();
			if (opt.isPresent()) {
				Enumeration<InetAddress> e = opt.get().getInetAddresses();
				if (e.hasMoreElements()) {
					InetAddress tmp = e.nextElement();
					System.err.println("Inet "+tmp);
					if (inet==null || inet instanceof Inet4Address)
						inet =tmp;
				}
			}
			inet = InetAddress.getByName("0.0.0.0");
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.web  = new WebServerImpl(builder.hostname,inet, 4080, Path.of("src", "main","resources","static").toAbsolutePath());
		MUD.instance = this;
	}

	//-------------------------------------------------------------------
	public void start() {
		worldCenter.start();
		connectionManager = new ConnectionManager(loginHandler);

		// Start all connectors
		for (MUDConnector connector : connectors) {
			try {
				logger.log(Level.INFO, "Starting {0} connector", connector.getName());
				connector.start(connectionManager);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			web.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		
		MUDClock.start();
	}

	//-------------------------------------------------------------------
	//-------------------------------------------------------------------
	public static class MUDBuilder {

		private String name;
		private String hostname;
		private List<MUDConnector> connectors;
		private ClientConnectionListener createHandler;
		private SymbolManager symbolManager;
		private WorldCenter worldCenter;
		private CommandCenter commandCenter;
		private RPGConnector<?,?,?> characterFactory;
		private PlayerDatabase playerDatabase;

		//-------------------------------------------------------------------
		public MUDBuilder() {
			name = "Unnamed MUD";
			connectors = new ArrayList<MUDConnector>();
		}

		//-------------------------------------------------------------------
		public MUDBuilder addConnector(MUDConnector value) {
			if (!connectors.contains(value)) {
				connectors.add(value);
			}
			return this;
		}

		//-------------------------------------------------------------------
		public MUDBuilder setName(String value) {
			this.name = value;
			return this;
		}

		//-------------------------------------------------------------------
		public MUDBuilder setHostName(String value) {
			this.hostname = value;
			return this;
		}

		//-------------------------------------------------------------------
		public MUDBuilder setCreateHandler(ClientConnectionListener value) {
			this.createHandler = value;
			return this;
		}

		//-------------------------------------------------------------------
		public MUDBuilder addSymbolManager(SymbolManager value) {
			this.symbolManager = value;
			SymbolManagerSingleton.setInstance(value);
			return this;
		}

		//-------------------------------------------------------------------
		public MUDBuilder setWorldCenter(WorldCenter value) {
			this.worldCenter = value;
			return this;
		}

		//-------------------------------------------------------------------
		public MUDBuilder setCommandManager(CommandCenter value) {
			this.commandCenter = value;
			return this;
		}

		//-------------------------------------------------------------------
		public MUDBuilder setCharacterFactory(RPGConnector<?,?,?> value) {
			this.characterFactory = value;
			return this;
		}

		//-------------------------------------------------------------------
		public MUD build() throws IOException {
			return new MUD(this);
		}

		//-------------------------------------------------------------------
		public MUDBuilder setPlayerDatabase(PlayerDatabase value) {
			this.playerDatabase = value;
			return this;
		}

	}

}
