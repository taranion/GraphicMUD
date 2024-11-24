package org.prelle.mud.telnet;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import org.prelle.mud.telnet.impl.TelnetClientConnection;
import org.prelle.mud4j.gmcp.GMCPManager;
import org.prelle.mud4j.gmcp.Char.CharPackage;
import org.prelle.mud4j.gmcp.CharLogin.CharLoginPackage;
import org.prelle.mud4j.gmcp.Client.ClientMediaPackage;
import org.prelle.mud4j.gmcp.Client.ClientPackage;
import org.prelle.mud4j.gmcp.Core.CorePackage;
import org.prelle.mud4j.gmcp.Room.RoomPackage;
import org.prelle.mud4j.gmcp.beip.TilemapPackage;
import org.prelle.mud4j.gmcp.ire.IREMiscPackage;
import org.prelle.telnet.TelnetConstants;
import org.prelle.telnet.TelnetOption;
import org.prelle.telnet.TelnetServerSocket;
import org.prelle.telnet.TelnetSocket;

import com.graphicmud.network.MUDConnector;
import com.graphicmud.network.MUDConnectorListener;

/**
 *
 */
public class TelnetConnector implements MUDConnector {

	private final static Logger logger = System.getLogger("network.telnet");

	private int port;
	private TelnetServerSocket serverSocket;
	private Thread thread;
	private List<TelnetClientConnection> connections = new ArrayList<TelnetClientConnection>();
	private MUDConnectorListener handler;

	//-------------------------------------------------------------------
	public static TelnetConnectorBuilder builder() {
		return new TelnetConnectorBuilder();
	}

	//-------------------------------------------------------------------
	TelnetConnector(TelnetConnectorBuilder builder) {
		port = builder.port;
	}

	//-------------------------------------------------------------------
	public String getProtocolIdentifier() {
		return MUDConnector.PROTO_TELNET;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.MUDConnector#getName()
	 */
	@Override
	public String getName() {
		return "TELNET";
	}


	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.MUDConnector#start()
	 */
	@Override
	public void start(MUDConnectorListener listener) throws IOException {
		this.handler = listener;
		GMCPManager.registerPackage(new CharLoginPackage());
		GMCPManager.registerPackage(new CharPackage());
		GMCPManager.registerPackage(new ClientPackage());
		GMCPManager.registerPackage(new ClientMediaPackage());
		GMCPManager.registerPackage(new CorePackage());
		GMCPManager.registerPackage(new RoomPackage());
		GMCPManager.registerPackage(new TilemapPackage());
		GMCPManager.registerPackage(new IREMiscPackage());

		serverSocket = new TelnetServerSocket(port)
				.support(TelnetOption.ECHO.getCode(), TelnetConstants.ControlCode.WILL)
				.support(TelnetOption.SGA.getCode(), TelnetConstants.ControlCode.DO)
				.support(TelnetOption.LINEMODE.getCode(), TelnetConstants.ControlCode.DO)
				.support(TelnetOption.CHARSET.getCode(), TelnetConstants.ControlCode.WILL)
				.support(TelnetOption.NEW_ENVIRON.getCode(), TelnetConstants.ControlCode.DO)
//				.support(new TelnetSubnegotiationHandler(4, "STATUS"), Role.PROVIDER)
				.support(TelnetOption.TERMINAL_TYPE.getCode(), TelnetConstants.ControlCode.DO)
				.support(TelnetOption.EOR.getCode(), TelnetConstants.ControlCode.WILL)
				.support(TelnetOption.NAWS.getCode(), TelnetConstants.ControlCode.DO)
				.support(TelnetOption.MSP.getCode(), TelnetConstants.ControlCode.WILL)
				.support(TelnetOption.GMCP.getCode(), TelnetConstants.ControlCode.WILL)
				.support(TelnetOption.MXP.getCode(), TelnetConstants.ControlCode.WILL)
				.support(TelnetOption.ZMP.getCode(), TelnetConstants.ControlCode.WILL)
				.support(TelnetOption.MUSHCLIENT.getCode(), TelnetConstants.ControlCode.WILL)
				.support(TelnetOption.MSSP.getCode(), TelnetConstants.ControlCode.WILL)
				.support(TelnetOption.MSDP.getCode(), TelnetConstants.ControlCode.WILL)
				;
		thread = new Thread( () -> run(),"NetworkService");
		thread.start();
		logger.log(Level.INFO, "Started listening on port {0}", port);
	}


	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.MUDConnector#stop()
	 */
	@Override
	public void stop() {
		try {
			thread.interrupt();
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//-----------------------------------------------------------------
	private void run() {
		while (true) {
			try {
				TelnetSocket socket = (TelnetSocket)serverSocket.accept();
				logger.log(Level.DEBUG, "Incoming connection from {0} Port {1}", socket.getInetAddress().getHostAddress(), socket.getPort());
				TelnetClientConnection con = new TelnetClientConnection(socket, this, handler);
				con.setVariable(TelnetClientConnection.VAR_IPADDRESS, socket.getInetAddress());
				connections.add(con);
				// Start reading from stream and by this learn of Telnet options
//				Thread.ofVirtual().start(con);
				Thread.ofPlatform().start(con);

				logger.log(Level.DEBUG, "Finished new connection: "+handler);
				if (handler!=null)
					handler.incomingConnection(con);
				else
					logger.log(Level.ERROR, "No handler for new connections set");
				
				con.send("This world is Pueblo 2.61 enhanced.\r\n");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}


	//-------------------------------------------------------------------
	//-------------------------------------------------------------------
	public static class TelnetConnectorBuilder {

		private int port = 60000;

		//-------------------------------------------------------------------
		public TelnetConnectorBuilder setPort(int value) {
			this.port = value;
			return this;
		}

		//-------------------------------------------------------------------
		public TelnetConnector build() {
			return new TelnetConnector(this);
		}
	}

}
