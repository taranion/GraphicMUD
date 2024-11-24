package org.prelle.mud.websocket;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.CharacterCodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogManager;

import com.graphicmud.network.ConnectionVariables;
import com.graphicmud.network.MUDConnector;
import com.graphicmud.network.MUDConnectorListener;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import robaho.net.httpserver.websockets.CloseCode;
import robaho.net.httpserver.websockets.OpCode;
import robaho.net.httpserver.websockets.WebSocket;
import robaho.net.httpserver.websockets.WebSocketException;
import robaho.net.httpserver.websockets.WebSocketFrame;
import robaho.net.httpserver.websockets.WebSocketHandler;

/**
 *
 */
public class WebsocketConnector implements MUDConnector, ConnectionVariables {

	private final static Logger logger = System.getLogger("network.websocket");

	private int port;
	private MUDConnectorListener handler;
	private InetSocketAddress socket;
	private HttpServer server;
	private String baseURL;

	private List<WebsocketClientConnection> connections = new ArrayList<WebsocketClientConnection>();

	//-------------------------------------------------------------------
	public static WebsocketConnectorBuilder builder() {
		return new WebsocketConnectorBuilder();
	}

	//-------------------------------------------------------------------
	private WebsocketConnector(InetAddress inet, int port) {
		this.port = port;
		socket = new InetSocketAddress(inet, port);
//		this.dataDir = dataDir;
		baseURL = String.format("http://%s:%d/", inet.getHostName(), port);
		logger.log(Level.INFO, "Initialized for "+baseURL);
	}

	//-------------------------------------------------------------------
	public String getProtocolIdentifier() {
		return MUDConnector.PROTO_WEBSOCKET;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.MUDConnector#getName()
	 */
	@Override
	public String getName() {
		return "Websocket";
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.MUDConnector#start(com.graphicmud.network.MUDConnectorListener)
	 */
	@Override
	public void start(MUDConnectorListener listener) throws IOException {
		this.handler = listener;

		WebSocketHandler webSocketHandler = new WebSocketHandler() {
			@Override
			protected WebSocket openWebSocket(HttpExchange exchange) {
				return incomingConnection(exchange);
			}
          };
  		server = HttpServer.create(socket, 5);
		server.createContext("/ws", webSocketHandler);
		server.start();
		logger.log(Level.INFO, "Starting websocket server on port {0}", socket.getPort());
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.MUDConnector#stop()
	 */
	@Override
	public void stop() {
	}

	//-------------------------------------------------------------------
	private WebSocket incomingConnection(HttpExchange exchange) {
		WebSocket socket = new WebSocket(exchange) {

			@Override
			protected void onClose(CloseCode code, String reason, boolean initiatedByRemote) {
				// TODO Auto-generated method stub
				logger.log(Level.INFO, "onClose "+code+"   "+reason);
			}

			@Override
			protected void onMessage(WebSocketFrame message) throws WebSocketException {
				logger.log(Level.INFO, "onMessage "+message);
				try {
					this.sendFrame(new WebSocketFrame(OpCode.Text, true, "Test"));
				} catch (CharacterCodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			protected void onPong(WebSocketFrame pong) throws WebSocketException {
				logger.log(Level.INFO, "onPong "+pong);

			}
		};
		WebsocketClientConnection con = new WebsocketClientConnection(socket, WebsocketConnector.this);
		con.setVariable(ConnectionVariables.VAR_IPADDRESS, exchange.getRemoteAddress().getAddress());
		connections.add(con);
		return socket;
	}


	public static class WebsocketConnectorBuilder {

		private int port;
		private InetAddress inet;

		//-------------------------------------------------------------------
		public WebsocketConnectorBuilder withAddress(InetAddress inet) {
			this.inet = inet;
			return this;
		}

		//-------------------------------------------------------------------
		public WebsocketConnectorBuilder withPort(int port) {
			this.port = port;
			return this;
		}

		public WebsocketConnector build() {
			return new WebsocketConnector(inet, port);
		}
	}

}
