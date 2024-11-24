/**
 * 
 */
package com.graphicmud.world;

import com.graphicmud.game.ReactsOnTime;

/**
 * @author prelle
 *
 */
public interface World extends Comparable<World>, ReactsOnTime {
	
	//-------------------------------------------------------
	public int getID();
	
	//-------------------------------------------------------
	public String getTitle();

}
