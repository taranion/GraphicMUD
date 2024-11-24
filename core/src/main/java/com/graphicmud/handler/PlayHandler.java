/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.handler;

import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;

import com.graphicmud.Identifier;
import com.graphicmud.MUD;
import com.graphicmud.action.cooked.CookedActionProcessor;
import com.graphicmud.action.cooked.ParameterType;
import com.graphicmud.action.cooked.Step;
import com.graphicmud.behavior.Context;
import com.graphicmud.commands.CommandCenter;
import com.graphicmud.commands.impl.StepMovement;
import com.graphicmud.map.OldMap;
import com.graphicmud.network.ClientConnection;
import com.graphicmud.network.ClientConnectionListener;
import com.graphicmud.network.ClientConnectionState;
import com.graphicmud.network.ClientConnection.Priority;
import com.graphicmud.network.impl.ConnectionManager;
import com.graphicmud.network.interaction.Form;
import com.graphicmud.player.PlayerCharacter;
import com.graphicmud.world.Location;
import com.graphicmud.world.NoSuchPositionException;
import com.graphicmud.world.Position;
import com.graphicmud.world.Range;
import com.graphicmud.world.Surrounding;
import com.graphicmud.world.WorldCenter;
import com.graphicmud.world.text.Direction;
import com.graphicmud.world.text.RoomPosition;
import com.graphicmud.world.tile.GridPosition;

/**
 *
 */
public class PlayHandler implements ClientConnectionListener {

	private final static Logger logger = System.getLogger("mud.game");

	private OldMap worldMap;
	private static WorldCenter wCenter;
	private static CommandCenter cCenter;

	public PlayHandler() {
//	    U5alikeInterpreter inter = new U5alikeInterpreter();
//	    worldMap = new OldMap(0,256,256,inter);
//
//	    logger.log(Level.DEBUG,"Load data ...");
//	    try {
////	      FileInputStream fin = new FileInputStream(new File(mapDir,"Old2.map"));
//	      InputStream fin = ClassLoader.getSystemResourceAsStream("static/maps/Old2.map");
//	      worldMap.load(fin);
//	    } catch (IOException ioe) {
//	      ioe.printStackTrace();
//	    }

	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.interaction.MenuHandler#enter(com.graphicmud.network.ClientConnection)
	 */
	@Override
	public void enter(ClientConnection con) {
		logger.log(Level.DEBUG, "ENTER enter()");
		con.setState(ClientConnectionState.LOGGED_IN);
		if (wCenter==null) {
			wCenter = MUD.getInstance().getWorldCenter();
		}
		if (cCenter==null) {
			cCenter = MUD.getInstance().getCommandManager();
		}

		this.buildMenu(con);
		try {
			Position target = con.getCharacter().getPosition();
			if (con.getCharacter().getPosition()==null) {
				target = wCenter.createPosition();
				Identifier startRoom = new Identifier("1/1/1");
				logger.log(Level.WARNING, "Implement start rooms");
				target.setRoomPosition(new RoomPosition(startRoom));
				target.setZoneNumber(startRoom.getWorldId(), startRoom.getZoneId());
				con.getCharacter().setPosition(target);
			}
			Location room = wCenter.getLocation(target.getRoomPosition().getRoomNumber());
			if (room==null) {
				logger.log(Level.WARNING, "Did not find start room {0}", target.getRoomPosition().getRoomNumber());
			}
			
			if (room!=null && room.getTileAreaComponent().isPresent()) {
				GridPosition grid = new GridPosition();
				grid.setX( room.getTileAreaComponent().get().getCenterX());
				grid.setY( room.getTileAreaComponent().get().getCenterY());
				target.setTilePosition(grid);
			}

			logger.log(Level.INFO, "Going to set position to "+target+" old was "+con.getCharacter().getPosition());
			MUD.getInstance().getGame().addPlayer(con.getCharacter());
			logger.log(Level.WARNING, "TODO: Inform everyone");
			room.addEntity(con.getCharacter());
			for (PlayerCharacter other : wCenter.getPlayersInRangeExceptSelf(con.getCharacter(), Range.SURROUNDING)) {
				other.getConnection().sendShortText(Priority.UNIMPORTANT, con.getCharacter().getName()+" erscheint aus dem Nichts");
			}

			Surrounding surrounding = MUD.getInstance().getWorldCenter().generateSurrounding(con.getCharacter(), target);
			con.sendRoom(surrounding);
		} catch (NoSuchPositionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		con.sendShortText("\r\nDu spielst jetzt.\r\n");
		this.sendPrompt(con);


		InputStream ins = ClassLoader.getSystemResourceAsStream("static/PixelArt1.png");
		logger.log(Level.DEBUG, "Send Image stream "+ins);
//		try {
//			if (ins!=null)
//				con.sendImage(ins.readAllBytes(), "Test.png", 640, 359);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		logger.log(Level.DEBUG, "LEAVE initialize()");
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnectionListener#performScreenRefresh(com.graphicmud.network.ClientConnection)
	 */
	@Override
	public void performScreenRefresh(ClientConnection con) {
		logger.log(Level.DEBUG, "performScreenRefresh");
		this.buildMenu(con);
			Surrounding surrounding = MUD.getInstance().getWorldCenter().generateSurrounding(con.getCharacter(), con.getCharacter().getPosition());
			logger.log(Level.INFO, "Send "+surrounding.getDescription());
			con.sendRoom(surrounding);
	}

	private void sendPrompt(ClientConnection con) {
		con.sendPrompt("> ");
	}

	@Override
	public void receivedInput(ClientConnection con, String input) {

		logger.log(Level.INFO, "INPUT: \""+input+"\"");
		if (input.isBlank()) {
			con.sendPrompt("Huh?\r\n");
			return;
		}

		logger.log(Level.DEBUG, "Calling {0}.parse", cCenter.getClass());
		cCenter.parse(con, input);

//		if ("connections".startsWith(input)) {
//			commandConnections(con);
//		} else if ("north".startsWith(input)) {
//			GridPosition gPos = con.getCharacter().getPosition();
//			gPos.setY(gPos.getY()-1);
//			updateMap(con);
//		} else if ("south".startsWith(input)) {
//			GridPosition gPos = con.getCharacter().getPosition();
//			gPos.setY(gPos.getY()+1);
//			updateMap(con);
//		} else if ("east".startsWith(input)) {
//			GridPosition gPos = con.getCharacter().getPosition();
//			gPos.setX(gPos.getX()+1);
//			updateMap(con);
//		} else if ("west".startsWith(input)) {
//			GridPosition gPos = con.getCharacter().getPosition();
//			gPos.setX(gPos.getX()-1);
//			updateMap(con);
//		} else {
//			con.sendPrompt("Das verstehe ich nicht.\r\n> ");
//		}

		con.sendPrompt(">");
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnectionListener#receivedKeyCode(com.graphicmud.network.ClientConnection, int)
	 */
	@Override
	public void receivedKeyCode(ClientConnection con, int code, List<Integer> arguments) {
		logger.log(Level.INFO, "Received key code {0}",code);
		int modifier = 0;
		if (!arguments.isEmpty() && arguments.size()>1)
			modifier = arguments.get(1);
		logger.log(Level.INFO, "Modifier {0}",modifier);
		
		Position pos = con.getCharacter().getPosition();
		Context context = new Context();
		context.put(ParameterType.POSITION_CURRENT, pos);
		switch (code) {
		case 37: // Arrow left
			processCursorKey(con, modifier, Direction.WEST);
			break;
		case 38: // Arrow up
			processCursorKey(con, modifier, Direction.NORTH);
			break;
		case 39: // Arrow Right
			processCursorKey(con, modifier, Direction.EAST);
			updateMap(con);
			break;
		case 40: // Arrow Down
			processCursorKey(con, modifier, Direction.SOUTH);
			updateMap(con);
			break;
		}
	}
	
	//-------------------------------------------------------------------
	private void processCursorKey(ClientConnection con, int modifier, Direction dir) {
		Position pos = con.getCharacter().getPosition();
		Context context = new Context();
		context.put(ParameterType.POSITION_CURRENT, pos);

		switch (modifier) {
		case 0: 
			StepMovement.step(con, con.getCharacter(), dir); 
			updateMap(con);
			break;
		case 2: // Shift 
			CookedActionProcessor.perform((new Step(dir))::lookInDirection, con.getCharacter(), context);
			break;
		case 3: // Alt
		case 5: // Control
		}
	}

//	//-------------------------------------------------------------------
//	private int[][] getMap(ClientConnection con) {
//		GridPosition gPos = con.getCharacter().getPosition();
//		logger.log(Level.INFO, "getMap for {0},{1} in zone {2}", gPos.getX(), gPos.getX(), gPos.getZoneNumber());
//		int posX = gPos.getX();
//		int posY = gPos.getY();
//
//		int[] line = worldMap.getArea(posX, posY, 5);
//		int[][] map = new int[11][11];
//		for (int y=0; y<11; y++) {
//			for (int x=0; x<11; x++) {
//				map[y][x] = line[y*11+x];
//				System.out.print(map[y][x]+",");
//			}
//			System.out.println("");
//		}
//		return map;
//	}

	//-------------------------------------------------------------------
	private void updateMap(ClientConnection con) {
		logger.log(Level.INFO, "sendMap");
			Surrounding surrounding = MUD.getInstance().getWorldCenter().generateSurrounding(null, con.getCharacter().getPosition());
			con.sendMap(surrounding.getMap());
//			try {
//				((OutputFormat<ClientConnection>)con.getOutputFormat())
//					.clear()
//					.setMap(surrounding.getMap())
//					.send(con);
//			} catch (IOException e) {
//				logger.log(Level.WARNING, "IO-Error sending",e);
//			}
//			int[][] map = surrounding.getMap();
//			con.sendMap(map);

	}

	//-------------------------------------------------------------------
	private void buildMenu(ClientConnection con) {
		logger.log(Level.INFO, "buildMenu");
		con.initializeInterface();
	}

	//-------------------------------------------------------------------
	private void commandConnections(ClientConnection con) {
		ConnectionManager conMan = MUD.getInstance().getConnectionManager();
		StringBuffer mess = new StringBuffer();

		mess.append(String.format("%15s %6s %15s %14s %11s %s\r\n",
				"Account",
				"Proto",
				"IP-Address",
				"Client",
				"State",
				"Capabilities"
				));
		mess.append("===================================================================================\r\n");
		for (ClientConnection tmp : conMan.getConnections()) {
			mess.append(String.format("%15s %6s %15s %14s %11s %s\r\n",
					tmp.getAccount(),
					tmp.getConnector().getName(),
					tmp.getNetworkId(),
					tmp.getClient(),
					tmp.getState(),
					tmp.getCapabilityString()
					));
		}

		con.sendScreen(mess.toString());
		this.sendPrompt(con);
	}

	@Override
	public void reenter(ClientConnection con, Object result) {
		logger.log(Level.WARNING, "returnFromHandler with {0}", result);
		logger.log(Level.WARNING, "TODO: Check for DO_NOT_DISTURB cache");
		con.setDoNotDisturb(false);
		sendPrompt(con);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnectionListener#receivedFormResponse(com.graphicmud.network.ClientConnection, com.graphicmud.network.interaction.Form, java.util.Map)
	 */
	@Override
	public void receivedFormResponse(ClientConnection con, Form form, java.util.Map<String, String> answers) {
		// TODO Auto-generated method stub

	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.network.ClientConnectionListener#connectionLost()
	 */
	@Override
	public void connectionLost(ClientConnection con) {
		logger.log(Level.WARNING, "connectionLost");
		MUD.getInstance().getPlayerDatabase().saveCharacter(con.getPlayer(), con.getCharacter());
		try {
			Location loc = MUD.getInstance().getWorldCenter().getLocation(con.getCharacter().getPosition());
			if (loc!=null) {
				loc.removeEntity(con.getCharacter());
			}
		} catch (NoSuchPositionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
