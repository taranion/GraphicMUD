/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.channels;


/**
 * ChannelParticipant.java
 *
 * @author Stefan Prelle  <prelle@tzi.de>
 * @version $Id: ChannelParticipant.java,v 1.2 2004/07/18 22:14:59 prelle Exp $
 */

public interface ChannelParticipant {
	
	public static enum State {
		/** This state means that is impossible for the participant to
		 *  use the channel */
		NOT_AVAILABLE,
		/** This state means that the participant uses the channel */
		READY,
		/** This state means that the participant could use the channel
		 *  but that he has been excluded by the administration. */
		EXCLUDED,
		/** This state means that the participant doesn't want to listen
		 *  to the channel */
		MUTED
	}

//	/** This state means that is impossible for the participant to
//	 *  use the channel */
//	public final static int NOT_AVAILABLE = -4;
//	/** This state means that the participant uses the channel */
//	public final static int READY         = -5;
//	/** This state means that the participant could use the channel
//	 *  but that he has been excluded by the administration. */
//	public final static int EXCLUDED      = -6;
//	/** This state means that the participant doesn't want to listen
//	 *  to the channel */
//	public final static int MUTED         = -7;


	//---------------------------------------------------------------
	/**
	 * Returns the current state of the channel. If an unknown ID
	 * was given the state NOT_AVAILABLE will be returned.
	 *
	 * @param channelID ID of the channel to inquire
	 * @return Channel-State
	 */
	public abstract State getChannelState(int channelID);

	//---------------------------------------------------------------
	/**
	 * Sets the current state of the channel. Setting a channel to
	 * NOT_AVAILABLE deletes the channel from the participant. Any
	 * other values add the channel.
	 *
	 * @param channelID ID of the channel to set
	 * @param state     New state
	 */
	public abstract void setChannelState(int channelID, State state);

	//---------------------------------------------------------------
	public abstract void send(String mess);


} // ChannelParticipant
