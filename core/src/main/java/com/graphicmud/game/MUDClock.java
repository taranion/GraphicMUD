/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.game;

import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import com.graphicmud.MUD;
import com.graphicmud.combat.Combat;

/**
 * 
 */
public class MUDClock {
	
	/** Milliseconds for the pulse */
	private final static int PULSE=250;
	private final static int TICK_AFTER_X_PULSES=100; // Every 25 seconds

	protected static Timer timer;
	
	/**
	 * Active combats
	 */
	protected static Map<Combat,Integer> combats = new HashMap<Combat,Integer>();

	private static int pulseCount;
	
	//-------------------------------------------------------------------
	static {
		timer = new Timer("PulseTimer");
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				MUD.getInstance().getWorldCenter().pulse();
				pulseCount++;
				if (pulseCount%TICK_AFTER_X_PULSES==0) {
					pulseCount=0;
					System.getLogger("mud.game").log(Level.INFO, "TICK");
					MUD.getInstance().getWorldCenter().tick();
				}
				// Ended combats
				List<Combat> ended = combats.keySet().stream().filter(c -> c.isCombatFinished()).toList();
				ended.forEach(c -> combats.remove(c));
				
				for (Entry<Combat,Integer> entry : new ArrayList<>(combats.entrySet())) {
					Combat c = entry.getKey();
					int pulsesToWait = entry.getValue();
					if (pulsesToWait<=0) {
						try {
							c.next();
							if (c.isCombatFinished()) {
								combats.remove(c);
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						combats.put(c, 4);
					} else {
						combats.put(c, pulsesToWait-1);
					}
				}
			}
		}, PULSE, PULSE);
	}

	//-------------------------------------------------------------------
	public static void start() {
	}

	//-------------------------------------------------------------------
	public static void registerCombat(Combat combat) {
		if (!combats.containsKey(combat))
			combats.put(combat,0);
	}

	//-------------------------------------------------------------------
	public static void unregisterCombat(Combat combat) {
		combats.remove(combat);
	}
}
