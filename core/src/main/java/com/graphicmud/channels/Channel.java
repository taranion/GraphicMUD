/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.channels;

/**
 * Created at 09.06.2007, 18:25:49
 *
 * @author Stefan Prelle
 * @version $Id$
 *
 */
public interface Channel {

    /** Channel-State Ready - Channel can be used */
    public final static int STATE_READY     = -1;

    /** Channel-State Closed - Channel could be used
     *  but is closed temporarily */
    public final static int STATE_CLOSED    = -2;

    /** Channel-State Not Ready - Channel cannot be used */
    public final static int STATE_NOT_READY = -3;

    //---------------------------------------------------------------
    /**
     * Sets the ID of the channel
     *
     * @param id New ID of the channel
     */
    public abstract void setID(int id);

    //---------------------------------------------------------------
    /**
     * Sets the state of the channel
     *
     * @param channelState New State of the channel
     */
    public abstract void setState(int channelState);

    //---------------------------------------------------------------
    public abstract int getID();

    public abstract String getName();

    public abstract String getDescription();

    public abstract int getMinLevel();

    public abstract int getMaxLevel();

    public abstract int getState();

    //------------------------------------------------
    /**
     * @return Returns the color.
     */
    public abstract int getColor();

    //------------------------------------------------
    public abstract String getMessageOthers();

    public abstract String getMessageSelf();

    public abstract String getMessageTarget();

}