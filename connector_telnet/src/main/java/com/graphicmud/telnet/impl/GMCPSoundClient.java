package com.graphicmud.telnet.impl;

import java.io.IOException;

import org.prelle.mud4j.gmcp.Client.ClientMediaPlay;

import com.graphicmud.MUD;
import com.graphicmud.media.SoundClient;
import com.graphicmud.telnet.gmcp.GMCP;
import com.graphicmud.web.WebServer;

/**
 *
 */
public class GMCPSoundClient implements SoundClient {

	private TelnetClientConnection con;

	//-------------------------------------------------------------------
	public GMCPSoundClient(TelnetClientConnection con) {
		this.con = con;
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.media.SoundClient#playMusic(java.lang.String, int, int, boolean)
	 */
	@Override
	public void playMusic(String filename, int volume, int repeats, boolean cont) {
		WebServer webserver = MUD.getInstance().getWeb();
		try {
			GMCP.sendClientMediaPlay(con, filename, webserver.getBaseURL(), ClientMediaPlay.Type.MUSIC);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.media.SoundClient#playSound(java.lang.String, int, int, int)
	 */
	@Override
	public void playSound(String filename, int volume, int repeats, int prio) {
		// TODO Auto-generated method stub

	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.media.SoundClient#stopMusic()
	 */
	@Override
	public void stopMusic() {
		// TODO Auto-generated method stub

	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.media.SoundClient#stopSounds()
	 */
	@Override
	public void stopSounds() {
		// TODO Auto-generated method stub

	}

}
