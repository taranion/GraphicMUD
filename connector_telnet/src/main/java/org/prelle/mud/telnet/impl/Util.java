package org.prelle.mud.telnet.impl;

import java.util.Timer;
import java.util.TimerTask;

/**
 *
 */
public class Util {

	private static Timer timer = new Timer();

	//-------------------------------------------------------------------
	public Util() {
	}

	public static void delayTask(TimerTask task, int millis) {
		timer.schedule(task, millis);
	}

}
