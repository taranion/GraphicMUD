package org.prelle.mud.telnet.impl;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import com.graphicmud.MUD;
import com.graphicmud.media.SoundClient;
import com.graphicmud.web.WebServer;

/**
 *
 */
public class MSPSoundClient implements SoundClient {

	private final static Logger logger = System.getLogger(MSPSoundClient.class.getPackageName());

	private TelnetClientConnection con;

	//-------------------------------------------------------------------
	public MSPSoundClient(TelnetClientConnection con) {
		this.con = con;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.media.SoundClient#playMusic(java.lang.String, int, int, boolean)
	 */
	@Override
	public void playMusic(String filename, int volume, int loops, boolean cont) {
		WebServer webserver = MUD.getInstance().getWeb();
		try {
			filename = filename.replace(" ", "%20");
			String base = webserver.getBaseURL().replace(" ", "%20");
			String command = String.format("!!MUSIC(%s V=%d L=%d C=%d U=%s)\r\n", filename, volume, loops, cont?1:0, base);
			con.getOutputStream().write(command);
		} catch (IOException e) {
			logger.log(Level.ERROR, "Failed sending MSP command",e);
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.media.SoundClient#playSound(java.lang.String, int, int, int)
	 */
	@Override
	public void playSound(String filename, int volume, int loops, int prio) {
		WebServer webserver = MUD.getInstance().getWeb();
		try {
			filename = filename.replace(" ", "%20");
			String base = webserver.getBaseURL().replace(" ", "%20");
			String command = String.format("!!SOUND(%s V=%d L=%d P=%d U=%s)\r\n", filename, volume, loops, prio, base);
			con.getOutputStream().write(command);
		} catch (IOException e) {
			logger.log(Level.ERROR, "Failed sending MSP command",e);
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.media.SoundClient#stopMusic()
	 */
	@Override
	public void stopMusic() {
		try {
			String command = "!!MUSIC(Off)";
			con.getOutputStream().write(command);
		} catch (IOException e) {
			logger.log(Level.ERROR, "Failed sending MSP command",e);
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.media.SoundClient#stopSounds()
	 */
	@Override
	public void stopSounds() {
		try {
			String command = "!!SOUND(Off)";
			con.getOutputStream().write(command);
		} catch (IOException e) {
			logger.log(Level.ERROR, "Failed sending MSP command",e);
		}
	}

}
