/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.HashMap;
import java.util.Map;

/**
 * This class ony purpose is to register all components (like NetworkCenter
 * or ChannelCenter) and hand them to anyone that requests them.
 *
 * @author Stefan Prelle  <prelle@tzi.de>
 * @version $Id: ComponentManager.java,v 1.1 2004/07/04 08:58:20 prelle Exp $
 */

public class ComponentManager {
	
    private final static Logger logger = System.getLogger("mud.sys");

    public static final int PLAYER_CENTER     = 0;
    public static final int PERMISSION_CENTER = 1;
    public static final int NET_CENTER        = 2;
    public static final int CHANNEL_CENTER    = 3;
    public static final int COMMAND_CENTER    = 4;
    public static final int WORLD_CENTER      = 5;
    
	private static Map<Integer,Object> centers;
	
	//---------------------------------------------------------------
	static {
		centers = new HashMap<Integer, Object>();
	}
	
	//---------------------------------------------------------------
	private static void addComponent(Integer key, Object comp) {
		synchronized (centers) {
			centers.put(key, comp);
            logger.log(Level.INFO,"add component ("+key+"): "+comp);
		}
	}
	
	//---------------------------------------------------------------
	private static boolean removeComponent(Integer key) {
		synchronized (centers) {
			return (centers.remove(key)!=null);
		}
	}
	
	//---------------------------------------------------------------
	public synchronized static void register(Object comp, int type) {
		Integer key = new Integer(type);
		if (centers.get(key)==null)
			addComponent(key, comp);
	}
	
	//---------------------------------------------------------------
	public synchronized static void unregister(Object comp, int type) {
		Integer key = new Integer(type);
		if (centers.get(key)!=null)
			removeComponent(key);
	}
	
	//---------------------------------------------------------------
	public static Object getComponent(int type) {
		Integer key = new Integer(type);
		return centers.get(key);
	}
	
} // ComponentManager
