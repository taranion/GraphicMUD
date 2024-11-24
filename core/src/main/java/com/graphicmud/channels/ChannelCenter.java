/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.channels;

import java.util.List;

/**
 * Created at 09.06.2007, 18:24:53
 *
 * @author Stefan Prelle
 * @version $Id$
 *
 */
public interface ChannelCenter {

    //---------------------------------------------------------------
    public abstract int registerChannel(Channel chan);

    //---------------------------------------------------------------
    public abstract void unregisterChannel(int id)
            throws NoSuchChannelException;

    //---------------------------------------------------------------
    public abstract void openChannel(int id) throws NoSuchChannelException;

    //---------------------------------------------------------------
    public abstract void closeChannel(int id) throws NoSuchChannelException;

    //---------------------------------------------------------------
    public abstract void sendOnChannel(ChannelParticipant sender,
            List<ChannelParticipant> receivers, int id, String data)
            throws NoSuchChannelException, ChannelException;

    //---------------------------------------------------------------
    public abstract void sendOnChannel(ChannelParticipant sender,
            List<ChannelParticipant> receivers, int id, ChannelParticipant target, String data)
            throws NoSuchChannelException, ChannelException;

    //---------------------------------------------------------------
    public abstract void sendOnChannel(int id, Object data)
            throws NoSuchChannelException;

    //---------------------------------------------------------------
    public abstract void sendOnChannel(int id, String target, Object data)
            throws NoSuchChannelException;

    //---------------------------------------------------------------
    public abstract Channel getChannel(int id)
            throws NoSuchChannelException;

    //---------------------------------------------------------------
    public abstract String getChannelStates();

}